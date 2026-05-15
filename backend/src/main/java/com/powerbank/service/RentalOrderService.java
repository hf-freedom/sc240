package com.powerbank.service;

import com.powerbank.entity.*;
import com.powerbank.entity.enums.*;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RentalOrderService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private UserService userService;

    @Autowired
    private PowerBankService powerBankService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private AbnormalRecordService abnormalRecordService;

    @Autowired
    private OperationLogService operationLogService;

    public RentalOrder createOrder(String userId, String cabinetId) {
        User user = userService.getUserById(userId);
        Cabinet cabinet = cabinetService.getCabinetById(cabinetId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (cabinet == null) {
            throw new RuntimeException("柜机不存在");
        }
        if (cabinet.getStatus() != CabinetStatus.ONLINE) {
            abnormalRecordService.createAbnormalRecord("CABINET_OFFLINE", cabinetId, "CABINET", "柜机不在线，无法租借");
            throw new RuntimeException("柜机不在线");
        }
        if (cabinet.getOccupiedSlots() == 0) {
            throw new RuntimeException("柜机无可借充电宝");
        }

        BigDecimal requiredDeposit = billingService.getRequiredDeposit();
        
        if (!userService.lockDeposit(userId, requiredDeposit)) {
            if (user.getCreditScore() < 60) {
                throw new RuntimeException("信用分不足");
            }
            if (user.getDeposit().compareTo(requiredDeposit) < 0) {
                throw new RuntimeException("押金不足");
            }
            throw new RuntimeException("押金已被占用");
        }

        String pbId = cabinet.getPowerBankIds().stream()
                .filter(id -> {
                    PowerBank pb = dataStorage.powerBankMap.get(id);
                    return pb != null && pb.getStatus() == PowerBankStatus.AVAILABLE;
                })
                .findFirst()
                .orElse(null);

        if (pbId == null) {
            userService.unlockDeposit(userId);
            throw new RuntimeException("无可借充电宝");
        }

        int beforeAvailable = cabinet.getAvailableSlots();
        int beforeOccupied = cabinet.getOccupiedSlots();
        
        if (!cabinetService.removePowerBankFromCabinet(cabinetId, pbId)) {
            userService.unlockDeposit(userId);
            throw new RuntimeException("取出充电宝失败");
        }

        int afterAvailable = beforeAvailable + 1;
        int afterOccupied = beforeOccupied - 1;

        RentalOrder order = new RentalOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setPowerBankId(pbId);
        order.setRentCabinetId(cabinetId);
        order.setStatus(RentalOrderStatus.RENTING);
        order.setRentTime(LocalDateTime.now());
        order.setDepositAmount(requiredDeposit);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        dataStorage.rentalOrderMap.put(order.getId(), order);

        powerBankService.updateStatus(pbId, PowerBankStatus.OCCUPIED, "用户租借");
        operationLogService.log("ORDER", order.getId(), "CREATE", null, "RENTING", "SYSTEM", "创建租借订单");
        
        operationLogService.logDepositLock(userId, requiredDeposit, order.getOrderNo());
        operationLogService.logCabinetStockLock(cabinetId, beforeAvailable, afterAvailable, beforeOccupied, afterOccupied, order.getOrderNo());

        return order;
    }

    public RentalOrder returnPowerBank(String orderId, String returnCabinetId) {
        RentalOrder order = dataStorage.rentalOrderMap.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != RentalOrderStatus.RENTING && order.getStatus() != RentalOrderStatus.OVERDUE) {
            throw new RuntimeException("订单状态不允许归还");
        }

        Cabinet returnCabinet = cabinetService.getCabinetById(returnCabinetId);
        if (returnCabinet == null) {
            throw new RuntimeException("归还柜机不存在");
        }
        if (returnCabinet.getStatus() != CabinetStatus.ONLINE) {
            abnormalRecordService.createAbnormalRecord("CABINET_OFFLINE", returnCabinetId, "CABINET", "归还柜机不在线");
            throw new RuntimeException("归还柜机不在线");
        }
        if (returnCabinet.getAvailableSlots() <= 0) {
            abnormalRecordService.createAbnormalRecord("CABINET_FULL", returnCabinetId, "CABINET", "归还柜机槽位已满");
            throw new RuntimeException("归还柜机槽位已满");
        }

        LocalDateTime returnTime = LocalDateTime.now();
        BigDecimal rentalFee = billingService.calculateFee(order.getRentTime(), returnTime);

        if (!order.getRentCabinetId().equals(returnCabinetId)) {
            abnormalRecordService.createAbnormalRecord("CROSS_SITE_RETURN", orderId, "ORDER", 
                "跨网点归还，从" + order.getRentCabinetId() + "到" + returnCabinetId);
        }

        int beforeAvailable = returnCabinet.getAvailableSlots();
        int beforeOccupied = returnCabinet.getOccupiedSlots();

        cabinetService.addPowerBankToCabinet(returnCabinetId, order.getPowerBankId(), 1);
        powerBankService.updateStatus(order.getPowerBankId(), PowerBankStatus.AVAILABLE, "用户归还");

        int afterAvailable = beforeAvailable - 1;
        int afterOccupied = beforeOccupied + 1;

        order.setStatus(RentalOrderStatus.RETURNED);
        order.setReturnCabinetId(returnCabinetId);
        order.setReturnTime(returnTime);
        order.setRentalFee(rentalFee);
        order.setUpdateTime(LocalDateTime.now());

        userService.unlockDeposit(order.getUserId());

        operationLogService.log("ORDER", orderId, "RETURN", order.getStatus().toString(), "RETURNED", "SYSTEM", "归还完成，租金: ¥" + rentalFee);
        operationLogService.logDepositUnlock(order.getUserId(), order.getOrderNo());
        operationLogService.logCabinetStockUnlock(returnCabinetId, beforeAvailable, afterAvailable, beforeOccupied, afterOccupied, order.getOrderNo());

        return order;
    }

    public void reportLost(String orderId) {
        RentalOrder order = dataStorage.rentalOrderMap.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != RentalOrderStatus.RENTING && order.getStatus() != RentalOrderStatus.OVERDUE) {
            throw new RuntimeException("订单状态不允许报失");
        }

        BigDecimal compensation = billingService.getCompensationAmount();
        order.setStatus(RentalOrderStatus.LOST);
        order.setCompensationAmount(compensation);
        order.setUpdateTime(LocalDateTime.now());

        powerBankService.markLost(order.getPowerBankId());
        userService.deductDeposit(order.getUserId(), compensation);

        abnormalRecordService.createAbnormalRecord("POWERBANK_LOST", orderId, "ORDER", "充电宝丢失，已扣除押金赔偿");
        operationLogService.log("ORDER", orderId, "LOST", order.getStatus().toString(), "LOST", "SYSTEM", "报失处理完成，赔偿: ¥" + compensation);
        operationLogService.logDepositDeduct(order.getUserId(), compensation, order.getOrderNo());
    }

    public void closeAbnormalOrder(String orderId) {
        RentalOrder order = dataStorage.rentalOrderMap.get(orderId);
        if (order != null) {
            String beforeStatus = order.getStatus().toString();
            order.setStatus(RentalOrderStatus.ABNORMAL_CLOSED);
            order.setUpdateTime(LocalDateTime.now());
            userService.unlockDeposit(order.getUserId());
            operationLogService.log("ORDER", orderId, "CLOSE", beforeStatus, "ABNORMAL_CLOSED", "SYSTEM", "异常关闭订单");
            operationLogService.logDepositUnlock(order.getUserId(), order.getOrderNo());
        }
    }

    public RentalOrder getOrderById(String id) {
        return dataStorage.rentalOrderMap.get(id);
    }

    public List<RentalOrder> getAllOrders() {
        return dataStorage.rentalOrderMap.values().stream().collect(Collectors.toList());
    }

    public List<RentalOrder> getRentingOrders() {
        return dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getStatus() == RentalOrderStatus.RENTING)
                .collect(Collectors.toList());
    }

    public List<RentalOrder> getOverdueOrders() {
        return dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getStatus() == RentalOrderStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    public List<OperationLog> getOrderLogs(String orderId) {
        return dataStorage.operationLogMap.values().stream()
                .filter(log -> log.getRemark() != null && log.getRemark().contains(orderId))
                .collect(Collectors.toList());
    }
}

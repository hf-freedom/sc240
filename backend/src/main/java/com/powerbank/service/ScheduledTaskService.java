package com.powerbank.service;

import com.powerbank.entity.Cabinet;
import com.powerbank.entity.RentalOrder;
import com.powerbank.entity.enums.CabinetStatus;
import com.powerbank.entity.enums.RentalOrderStatus;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ScheduledTaskService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private BillingService billingService;

    @Autowired
    private AbnormalRecordService abnormalRecordService;

    @Autowired
    private OperationLogService operationLogService;

    @Scheduled(fixedRate = 60000)
    public void checkOverdueOrders() {
        LocalDateTime now = LocalDateTime.now();
        for (RentalOrder order : dataStorage.rentalOrderMap.values()) {
            if (order.getStatus() == RentalOrderStatus.RENTING) {
                if (billingService.isOverdue(order.getRentTime(), now)) {
                    order.setStatus(RentalOrderStatus.OVERDUE);
                    order.setOverdueTime(now);
                    order.setUpdateTime(now);
                    abnormalRecordService.createAbnormalRecord("ORDER_OVERDUE", order.getId(), "ORDER", 
                        "订单超时未归还，租借时间：" + order.getRentTime());
                    operationLogService.log("ORDER", order.getId(), "OVERDUE", "RENTING", "OVERDUE", "SYSTEM", "订单超时");
                }
            }
        }
    }

    @Scheduled(fixedRate = 120000)
    public void checkOfflineCabinets() {
        LocalDateTime now = LocalDateTime.now();
        for (Cabinet cabinet : dataStorage.cabinetMap.values()) {
            if (cabinet.getStatus() == CabinetStatus.ONLINE) {
                Duration duration = Duration.between(cabinet.getLastHeartbeat(), now);
                if (duration.toMinutes() >= 5) {
                    cabinet.setStatus(CabinetStatus.OFFLINE);
                    cabinet.setUpdateTime(now);
                    abnormalRecordService.createAbnormalRecord("CABINET_OFFLINE", cabinet.getId(), "CABINET", 
                        "柜机离线超过5分钟，最后心跳：" + cabinet.getLastHeartbeat());
                    operationLogService.log("CABINET", cabinet.getId(), "OFFLINE", "ONLINE", "OFFLINE", "SYSTEM", "柜机离线");
                }
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanLongTermAbnormalOrders() {
        LocalDateTime now = LocalDateTime.now();
        for (RentalOrder order : dataStorage.rentalOrderMap.values()) {
            if (order.getStatus() == RentalOrderStatus.OVERDUE) {
                Duration duration = Duration.between(order.getOverdueTime(), now);
                if (duration.toDays() >= 7) {
                    order.setStatus(RentalOrderStatus.LOST);
                    order.setCompensationAmount(billingService.getCompensationAmount());
                    order.setUpdateTime(now);
                    abnormalRecordService.createAbnormalRecord("AUTO_LOST", order.getId(), "ORDER", 
                        "超时超过7天，自动标记为丢失");
                    operationLogService.log("ORDER", order.getId(), "AUTO_LOST", "OVERDUE", "LOST", "SYSTEM", "自动标记丢失");
                }
            }
        }
    }
}
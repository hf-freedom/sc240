package com.powerbank.service;

import com.powerbank.entity.*;
import com.powerbank.entity.enums.*;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AbnormalRecordService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private BillingService billingService;

    public AbnormalRecord createAbnormalRecord(String type, String relatedId, String relatedType, String description) {
        AbnormalRecord record = new AbnormalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setType(type);
        record.setRelatedId(relatedId);
        record.setRelatedType(relatedType);
        record.setDescription(description);
        record.setStatus("PENDING");
        record.setCreateTime(LocalDateTime.now());
        dataStorage.abnormalRecordMap.put(record.getId(), record);
        return record;
    }

    public void handleAbnormalRecord(String recordId, String handler, String remark) {
        AbnormalRecord record = dataStorage.abnormalRecordMap.get(recordId);
        if (record != null) {
            record.setStatus("HANDLED");
            record.setHandler(handler);
            record.setHandleTime(LocalDateTime.now());
            operationLogService.log("ABNORMAL", recordId, "HANDLE", "PENDING", "HANDLED", handler, remark);
        }
    }

    public Map<String, Object> getAbnormalDetail(String recordId) {
        AbnormalRecord record = dataStorage.abnormalRecordMap.get(recordId);
        if (record == null) {
            return null;
        }
        Map<String, Object> detail = new HashMap<>();
        detail.put("record", record);
        
        if ("CABINET".equals(record.getRelatedType())) {
            Cabinet cabinet = dataStorage.cabinetMap.get(record.getRelatedId());
            if (cabinet != null) {
                detail.put("cabinet", cabinet);
                List<PowerBank> powerBanks = dataStorage.powerBankMap.values().stream()
                        .filter(pb -> cabinet.getId().equals(pb.getCabinetId()))
                        .collect(Collectors.toList());
                detail.put("powerBanks", powerBanks);
            }
        } else if ("ORDER".equals(record.getRelatedType())) {
            RentalOrder order = dataStorage.rentalOrderMap.get(record.getRelatedId());
            if (order != null) {
                detail.put("order", order);
                User user = dataStorage.userMap.get(order.getUserId());
                detail.put("user", user);
            }
        }
        
        return detail;
    }

    public void recoverCabinetOnline(String recordId, String cabinetId, String handler) {
        Cabinet cabinet = dataStorage.cabinetMap.get(cabinetId);
        if (cabinet != null) {
            cabinet.setStatus(CabinetStatus.ONLINE);
            cabinetService.heartbeat(cabinetId);
            handleAbnormalRecord(recordId, handler, "手动恢复柜机在线");
            operationLogService.log("CABINET", cabinetId, "STATUS_CHANGE", "OFFLINE", "ONLINE", handler, "异常处理：恢复柜机在线");
        }
    }

    public void rollbackResources(String recordId, String handler) {
        AbnormalRecord record = dataStorage.abnormalRecordMap.get(recordId);
        if (record == null) return;

        if ("CABINET_OFFLINE".equals(record.getType())) {
            Cabinet cabinet = dataStorage.cabinetMap.get(record.getRelatedId());
            if (cabinet != null) {
                List<PowerBank> powerBanks = dataStorage.powerBankMap.values().stream()
                        .filter(pb -> cabinet.getId().equals(pb.getCabinetId()))
                        .filter(pb -> pb.getStatus() == PowerBankStatus.OCCUPIED)
                        .collect(Collectors.toList());
                
                for (PowerBank pb : powerBanks) {
                    pb.setStatus(PowerBankStatus.AVAILABLE);
                    operationLogService.log("POWERBANK", pb.getId(), "STATUS_CHANGE", "OCCUPIED", "AVAILABLE", handler, "异常处理：回滚充电宝状态");
                }
                
                List<RentalOrder> relatedOrders = dataStorage.rentalOrderMap.values().stream()
                        .filter(o -> cabinet.getId().equals(o.getRentCabinetId()))
                        .filter(o -> o.getStatus() == RentalOrderStatus.RENTING)
                        .collect(Collectors.toList());
                
                for (RentalOrder order : relatedOrders) {
                    order.setStatus(RentalOrderStatus.ABNORMAL_CLOSED);
                    order.setUpdateTime(LocalDateTime.now());
                    User user = dataStorage.userMap.get(order.getUserId());
                    if (user != null) {
                        user.setDepositLocked(false);
                    }
                    operationLogService.log("ORDER", order.getId(), "STATUS_CHANGE", "RENTING", "ABNORMAL_CLOSED", handler, "异常处理：关闭异常订单并释放押金");
                    operationLogService.logDepositUnlock(order.getUserId(), order.getOrderNo());
                }
            }
        }
        
        handleAbnormalRecord(recordId, handler, "已回滚占用资源");
    }

    public void createProcessingTask(String recordId, String handler, String taskDesc, String assignee) {
        AbnormalRecord record = dataStorage.abnormalRecordMap.get(recordId);
        if (record != null) {
            record.setStatus("PROCESSING");
            record.setHandler(assignee);
            operationLogService.log("ABNORMAL", recordId, "CREATE_TASK", "PENDING", "PROCESSING", handler, "生成处理任务：" + taskDesc + "，指派给：" + assignee);
        }
    }

    public void closeOrderAndRefund(String recordId, String orderId, String handler, String reason) {
        RentalOrder order = dataStorage.rentalOrderMap.get(orderId);
        if (order != null) {
            String beforeStatus = order.getStatus().toString();
            order.setStatus(RentalOrderStatus.ABNORMAL_CLOSED);
            order.setUpdateTime(LocalDateTime.now());
            
            User user = dataStorage.userMap.get(order.getUserId());
            if (user != null) {
                user.setDepositLocked(false);
            }
            
            PowerBank pb = dataStorage.powerBankMap.get(order.getPowerBankId());
            if (pb != null && pb.getStatus() == PowerBankStatus.OCCUPIED) {
                pb.setStatus(PowerBankStatus.AVAILABLE);
            }
            
            operationLogService.log("ORDER", orderId, "STATUS_CHANGE", beforeStatus, "ABNORMAL_CLOSED", handler, "异常处理：" + reason);
            operationLogService.logDepositUnlock(order.getUserId(), order.getOrderNo());
            handleAbnormalRecord(recordId, handler, reason);
        }
    }

    public void processLostCompensation(String recordId, String orderId, String handler) {
        RentalOrder order = dataStorage.rentalOrderMap.get(orderId);
        if (order != null) {
            BigDecimal compensation = billingService.getCompensationAmount();
            order.setStatus(RentalOrderStatus.LOST);
            order.setCompensationAmount(compensation);
            order.setUpdateTime(LocalDateTime.now());
            
            PowerBank pb = dataStorage.powerBankMap.get(order.getPowerBankId());
            if (pb != null) {
                pb.setStatus(PowerBankStatus.LOST);
            }
            
            User user = dataStorage.userMap.get(order.getUserId());
            if (user != null) {
                user.setDeposit(user.getDeposit().subtract(compensation));
                user.setDepositLocked(false);
            }
            
            operationLogService.log("ORDER", orderId, "STATUS_CHANGE", order.getStatus().toString(), "LOST", handler, "异常处理：执行丢失赔偿");
            operationLogService.logDepositDeduct(order.getUserId(), compensation, order.getOrderNo());
            handleAbnormalRecord(recordId, handler, "已执行丢失赔偿");
        }
    }

    public List<AbnormalRecord> getPendingRecords() {
        return dataStorage.abnormalRecordMap.values().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public List<AbnormalRecord> getProcessingRecords() {
        return dataStorage.abnormalRecordMap.values().stream()
                .filter(r -> "PROCESSING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public List<AbnormalRecord> getAllRecords() {
        return dataStorage.abnormalRecordMap.values().stream().collect(Collectors.toList());
    }
}

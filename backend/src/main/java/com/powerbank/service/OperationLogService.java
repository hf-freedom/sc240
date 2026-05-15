package com.powerbank.service;

import com.powerbank.entity.OperationLog;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OperationLogService {

    @Autowired
    private DataStorage dataStorage;

    public void log(String targetType, String targetId, String operationType,
                    String beforeStatus, String afterStatus, String operator, String remark) {
        OperationLog log = new OperationLog();
        log.setId(UUID.randomUUID().toString());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOperationType(operationType);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperator(operator);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        dataStorage.operationLogMap.put(log.getId(), log);
    }

    public void logDepositLock(String userId, BigDecimal amount, String orderId) {
        log("DEPOSIT", userId, "LOCK", "UNLOCKED", "LOCKED", "SYSTEM", 
            "订单[" + orderId + "] 锁定押金: ¥" + amount);
    }

    public void logDepositUnlock(String userId, String orderId) {
        log("DEPOSIT", userId, "UNLOCK", "LOCKED", "UNLOCKED", "SYSTEM", 
            "订单[" + orderId + "] 释放押金");
    }

    public void logDepositDeduct(String userId, BigDecimal amount, String orderId) {
        log("DEPOSIT", userId, "DEDUCT", "LOCKED", "DEDUCTED", "SYSTEM", 
            "订单[" + orderId + "] 扣除押金赔偿: ¥" + amount);
    }

    public void logCabinetStockLock(String cabinetId, int beforeAvailable, int afterAvailable, 
                                     int beforeOccupied, int afterOccupied, String orderId) {
        log("CABINET_STOCK", cabinetId, "LOCK", 
            "可用槽:" + beforeAvailable + ", 可借:" + beforeOccupied,
            "可用槽:" + afterAvailable + ", 可借:" + afterOccupied,
            "SYSTEM", "订单[" + orderId + "] 借出充电宝, 占用槽位");
    }

    public void logCabinetStockUnlock(String cabinetId, int beforeAvailable, int afterAvailable,
                                       int beforeOccupied, int afterOccupied, String orderId) {
        log("CABINET_STOCK", cabinetId, "UNLOCK",
            "可用槽:" + beforeAvailable + ", 可借:" + beforeOccupied,
            "可用槽:" + afterAvailable + ", 可借:" + afterOccupied,
            "SYSTEM", "订单[" + orderId + "] 归还充电宝, 释放槽位");
    }

    public java.util.List<OperationLog> getAllLogs() {
        return new java.util.ArrayList<>(dataStorage.operationLogMap.values());
    }
}
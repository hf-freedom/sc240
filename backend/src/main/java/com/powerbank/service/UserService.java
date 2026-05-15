package com.powerbank.service;

import com.powerbank.entity.User;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private OperationLogService operationLogService;

    public User createUser(String username, String phone) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPhone(phone);
        user.setCreditScore(100);
        user.setDeposit(BigDecimal.ZERO);
        user.setDepositLocked(false);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        dataStorage.userMap.put(user.getId(), user);
        operationLogService.log("USER", user.getId(), "CREATE", null, "ACTIVE", "SYSTEM", "创建用户");
        return user;
    }

    public User getUserById(String id) {
        return dataStorage.userMap.get(id);
    }

    public List<User> getAllUsers() {
        return dataStorage.userMap.values().stream().collect(Collectors.toList());
    }

    public void rechargeDeposit(String userId, BigDecimal amount) {
        User user = dataStorage.userMap.get(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String beforeStatus = user.getDeposit().toString();
        user.setDeposit(user.getDeposit().add(amount));
        user.setUpdateTime(LocalDateTime.now());
        operationLogService.log("USER", userId, "RECHARGE", beforeStatus, user.getDeposit().toString(), "SYSTEM", "押金充值");
    }

    public boolean lockDeposit(String userId, BigDecimal amount) {
        User user = dataStorage.userMap.get(userId);
        if (user == null || user.getDepositLocked()) {
            return false;
        }
        if (user.getDeposit().compareTo(amount) < 0) {
            return false;
        }
        if (user.getCreditScore() < 60) {
            return false;
        }
        user.setDepositLocked(true);
        user.setUpdateTime(LocalDateTime.now());
        operationLogService.log("USER", userId, "LOCK_DEPOSIT", "UNLOCKED", "LOCKED", "SYSTEM", "锁定押金");
        return true;
    }

    public void unlockDeposit(String userId) {
        User user = dataStorage.userMap.get(userId);
        if (user != null && user.getDepositLocked()) {
            user.setDepositLocked(false);
            user.setUpdateTime(LocalDateTime.now());
            operationLogService.log("USER", userId, "UNLOCK_DEPOSIT", "LOCKED", "UNLOCKED", "SYSTEM", "释放押金");
        }
    }

    public void deductDeposit(String userId, BigDecimal amount) {
        User user = dataStorage.userMap.get(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String beforeStatus = user.getDeposit().toString();
        user.setDeposit(user.getDeposit().subtract(amount));
        user.setDepositLocked(false);
        user.setUpdateTime(LocalDateTime.now());
        operationLogService.log("USER", userId, "DEDUCT", beforeStatus, user.getDeposit().toString(), "SYSTEM", "扣除押金");
    }
}
package com.powerbank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private String id;
    private String username;
    private String phone;
    private Integer creditScore;
    private BigDecimal deposit;
    private Boolean depositLocked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public Boolean getDepositLocked() { return depositLocked; }
    public void setDepositLocked(Boolean depositLocked) { this.depositLocked = depositLocked; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
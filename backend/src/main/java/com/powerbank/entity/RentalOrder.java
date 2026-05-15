package com.powerbank.entity;

import com.powerbank.entity.enums.RentalOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentalOrder {
    private String id;
    private String orderNo;
    private String userId;
    private String powerBankId;
    private String rentCabinetId;
    private String returnCabinetId;
    private RentalOrderStatus status;
    private LocalDateTime rentTime;
    private LocalDateTime returnTime;
    private LocalDateTime overdueTime;
    private BigDecimal rentalFee;
    private BigDecimal depositAmount;
    private BigDecimal compensationAmount;
    private Integer rentalHours;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPowerBankId() { return powerBankId; }
    public void setPowerBankId(String powerBankId) { this.powerBankId = powerBankId; }
    public String getRentCabinetId() { return rentCabinetId; }
    public void setRentCabinetId(String rentCabinetId) { this.rentCabinetId = rentCabinetId; }
    public String getReturnCabinetId() { return returnCabinetId; }
    public void setReturnCabinetId(String returnCabinetId) { this.returnCabinetId = returnCabinetId; }
    public RentalOrderStatus getStatus() { return status; }
    public void setStatus(RentalOrderStatus status) { this.status = status; }
    public LocalDateTime getRentTime() { return rentTime; }
    public void setRentTime(LocalDateTime rentTime) { this.rentTime = rentTime; }
    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
    public LocalDateTime getOverdueTime() { return overdueTime; }
    public void setOverdueTime(LocalDateTime overdueTime) { this.overdueTime = overdueTime; }
    public BigDecimal getRentalFee() { return rentalFee; }
    public void setRentalFee(BigDecimal rentalFee) { this.rentalFee = rentalFee; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }
    public Integer getRentalHours() { return rentalHours; }
    public void setRentalHours(Integer rentalHours) { this.rentalHours = rentalHours; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
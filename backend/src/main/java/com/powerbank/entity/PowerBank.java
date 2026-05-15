package com.powerbank.entity;

import com.powerbank.entity.enums.PowerBankStatus;
import java.time.LocalDateTime;

public class PowerBank {
    private String id;
    private String code;
    private String cabinetId;
    private Integer slotNumber;
    private PowerBankStatus status;
    private Integer batteryLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCabinetId() { return cabinetId; }
    public void setCabinetId(String cabinetId) { this.cabinetId = cabinetId; }
    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }
    public PowerBankStatus getStatus() { return status; }
    public void setStatus(PowerBankStatus status) { this.status = status; }
    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
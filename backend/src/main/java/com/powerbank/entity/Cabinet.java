package com.powerbank.entity;

import com.powerbank.entity.enums.CabinetStatus;
import java.time.LocalDateTime;
import java.util.List;

public class Cabinet {
    private String id;
    private String code;
    private String name;
    private String location;
    private String address;
    private CabinetStatus status;
    private Integer totalSlots;
    private Integer availableSlots;
    private Integer occupiedSlots;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> powerBankIds;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public CabinetStatus getStatus() { return status; }
    public void setStatus(CabinetStatus status) { this.status = status; }
    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }
    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }
    public Integer getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(Integer occupiedSlots) { this.occupiedSlots = occupiedSlots; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<String> getPowerBankIds() { return powerBankIds; }
    public void setPowerBankIds(List<String> powerBankIds) { this.powerBankIds = powerBankIds; }
}
package com.powerbank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillingRule {
    private String id;
    private String name;
    private BigDecimal firstHourFee;
    private BigDecimal hourlyFee;
    private BigDecimal maxDailyFee;
    private Integer freeMinutes;
    private Integer overdueHours;
    private Boolean isDefault;
    private LocalDateTime createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getFirstHourFee() { return firstHourFee; }
    public void setFirstHourFee(BigDecimal firstHourFee) { this.firstHourFee = firstHourFee; }
    public BigDecimal getHourlyFee() { return hourlyFee; }
    public void setHourlyFee(BigDecimal hourlyFee) { this.hourlyFee = hourlyFee; }
    public BigDecimal getMaxDailyFee() { return maxDailyFee; }
    public void setMaxDailyFee(BigDecimal maxDailyFee) { this.maxDailyFee = maxDailyFee; }
    public Integer getFreeMinutes() { return freeMinutes; }
    public void setFreeMinutes(Integer freeMinutes) { this.freeMinutes = freeMinutes; }
    public Integer getOverdueHours() { return overdueHours; }
    public void setOverdueHours(Integer overdueHours) { this.overdueHours = overdueHours; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
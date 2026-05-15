package com.powerbank.service;

import com.powerbank.entity.BillingRule;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BillingService {

    @Autowired
    private DataStorage dataStorage;

    public BillingRule createDefaultRule() {
        BillingRule rule = new BillingRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName("默认计费规则");
        rule.setFirstHourFee(new BigDecimal("2.00"));
        rule.setHourlyFee(new BigDecimal("1.00"));
        rule.setMaxDailyFee(new BigDecimal("10.00"));
        rule.setFreeMinutes(5);
        rule.setOverdueHours(24);
        rule.setIsDefault(true);
        rule.setCreateTime(LocalDateTime.now());
        dataStorage.billingRuleMap.put(rule.getId(), rule);
        return rule;
    }

    public BillingRule getDefaultRule() {
        return dataStorage.billingRuleMap.values().stream()
                .filter(BillingRule::getIsDefault)
                .findFirst()
                .orElseGet(this::createDefaultRule);
    }

    public BigDecimal calculateFee(LocalDateTime rentTime, LocalDateTime returnTime) {
        BillingRule rule = getDefaultRule();
        Duration duration = Duration.between(rentTime, returnTime);
        long minutes = duration.toMinutes();
        if (minutes <= rule.getFreeMinutes()) {
            return BigDecimal.ZERO;
        }
        long hours = (long) Math.ceil((minutes - rule.getFreeMinutes()) / 60.0);
        BigDecimal fee;
        if (hours <= 1) {
            fee = rule.getFirstHourFee();
        } else {
            fee = rule.getFirstHourFee().add(rule.getHourlyFee().multiply(new BigDecimal(hours - 1)));
        }
        if (fee.compareTo(rule.getMaxDailyFee()) > 0) {
            fee = rule.getMaxDailyFee();
        }
        return fee;
    }

    public boolean isOverdue(LocalDateTime rentTime, LocalDateTime currentTime) {
        BillingRule rule = getDefaultRule();
        Duration duration = Duration.between(rentTime, currentTime);
        return duration.toHours() >= rule.getOverdueHours();
    }

    public BigDecimal getCompensationAmount() {
        return new BigDecimal("99.00");
    }

    public BigDecimal getRequiredDeposit() {
        return new BigDecimal("99.00");
    }
}
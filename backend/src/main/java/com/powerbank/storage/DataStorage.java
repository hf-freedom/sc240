package com.powerbank.storage;

import com.powerbank.entity.*;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStorage {
    public final Map<String, PowerBank> powerBankMap = new ConcurrentHashMap<>();
    public final Map<String, Cabinet> cabinetMap = new ConcurrentHashMap<>();
    public final Map<String, User> userMap = new ConcurrentHashMap<>();
    public final Map<String, RentalOrder> rentalOrderMap = new ConcurrentHashMap<>();
    public final Map<String, OperationLog> operationLogMap = new ConcurrentHashMap<>();
    public final Map<String, AbnormalRecord> abnormalRecordMap = new ConcurrentHashMap<>();
    public final Map<String, BillingRule> billingRuleMap = new ConcurrentHashMap<>();
}
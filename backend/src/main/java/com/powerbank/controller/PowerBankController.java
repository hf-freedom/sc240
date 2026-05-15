package com.powerbank.controller;

import com.powerbank.entity.*;
import com.powerbank.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PowerBankController {

    @Autowired
    private UserService userService;

    @Autowired
    private PowerBankService powerBankService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private RentalOrderService rentalOrderService;

    @Autowired
    private AbnormalRecordService abnormalRecordService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private BillingService billingService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return statisticsService.getDashboardStats();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody Map<String, String> params) {
        return userService.createUser(params.get("username"), params.get("phone"));
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PostMapping("/users/{id}/deposit")
    public void rechargeDeposit(@PathVariable String id, @RequestBody Map<String, BigDecimal> params) {
        userService.rechargeDeposit(id, params.get("amount"));
    }

    @PostMapping("/powerbanks")
    public PowerBank createPowerBank(@RequestBody Map<String, String> params) {
        return powerBankService.createPowerBank(params.get("code"));
    }

    @GetMapping("/powerbanks")
    public List<PowerBank> getPowerBanks() {
        return powerBankService.getAllPowerBanks();
    }

    @PostMapping("/cabinets")
    public Cabinet createCabinet(@RequestBody Map<String, Object> params) {
        return cabinetService.createCabinet(
                (String) params.get("code"),
                (String) params.get("name"),
                (String) params.get("location"),
                (String) params.get("address"),
                (Integer) params.get("totalSlots")
        );
    }

    @GetMapping("/cabinets")
    public List<Cabinet> getCabinets() {
        return cabinetService.getAllCabinets();
    }

    @PostMapping("/cabinets/{cabinetId}/add-powerbank")
    public boolean addPowerBankToCabinet(@PathVariable String cabinetId, @RequestBody Map<String, Object> params) {
        return cabinetService.addPowerBankToCabinet(cabinetId, (String) params.get("powerBankId"), (Integer) params.get("slotNumber"));
    }

    @PostMapping("/cabinets/{cabinetId}/heartbeat")
    public void heartbeat(@PathVariable String cabinetId) {
        cabinetService.heartbeat(cabinetId);
    }

    @PostMapping("/orders")
    public RentalOrder createOrder(@RequestBody Map<String, String> params) {
        return rentalOrderService.createOrder(params.get("userId"), params.get("cabinetId"));
    }

    @GetMapping("/orders")
    public List<RentalOrder> getOrders() {
        return rentalOrderService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public RentalOrder getOrder(@PathVariable String id) {
        return rentalOrderService.getOrderById(id);
    }

    @PostMapping("/orders/{id}/return")
    public RentalOrder returnPowerBank(@PathVariable String id, @RequestBody Map<String, String> params) {
        return rentalOrderService.returnPowerBank(id, params.get("cabinetId"));
    }

    @PostMapping("/orders/{id}/lost")
    public void reportLost(@PathVariable String id) {
        rentalOrderService.reportLost(id);
    }

    @GetMapping("/orders/{id}/logs")
    public List<OperationLog> getOrderLogs(@PathVariable String id) {
        return rentalOrderService.getOrderLogs(id);
    }

    @GetMapping("/abnormals")
    public List<AbnormalRecord> getAbnormalRecords() {
        return abnormalRecordService.getAllRecords();
    }

    @GetMapping("/abnormals/pending")
    public List<AbnormalRecord> getPendingAbnormalRecords() {
        return abnormalRecordService.getPendingRecords();
    }

    @PostMapping("/abnormals/{id}/handle")
    public void handleAbnormal(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.handleAbnormalRecord(id, params.get("handler"), params.get("remark"));
    }

    @GetMapping("/abnormals/{id}/detail")
    public Map<String, Object> getAbnormalDetail(@PathVariable String id) {
        return abnormalRecordService.getAbnormalDetail(id);
    }

    @PostMapping("/abnormals/{id}/recover-cabinet")
    public void recoverCabinet(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.recoverCabinetOnline(id, params.get("cabinetId"), params.get("handler"));
    }

    @PostMapping("/abnormals/{id}/rollback")
    public void rollbackResources(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.rollbackResources(id, params.get("handler"));
    }

    @PostMapping("/abnormals/{id}/create-task")
    public void createTask(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.createProcessingTask(id, params.get("handler"), params.get("taskDesc"), params.get("assignee"));
    }

    @PostMapping("/abnormals/{id}/close-order")
    public void closeOrderAndRefund(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.closeOrderAndRefund(id, params.get("orderId"), params.get("handler"), params.get("reason"));
    }

    @PostMapping("/abnormals/{id}/lost-compensation")
    public void processLostCompensation(@PathVariable String id, @RequestBody Map<String, String> params) {
        abnormalRecordService.processLostCompensation(id, params.get("orderId"), params.get("handler"));
    }

    @GetMapping("/logs")
    public List<OperationLog> getLogs() {
        return operationLogService.getAllLogs();
    }

    @PostMapping("/transfer")
    public void transferPowerBank(@RequestBody Map<String, String> params) {
        cabinetService.transferPowerBank(params.get("fromCabinetId"), params.get("toCabinetId"), params.get("powerBankId"));
    }

    @GetMapping("/billing-rule")
    public BillingRule getBillingRule() {
        return billingService.getDefaultRule();
    }
}
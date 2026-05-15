package com.powerbank.service;

import com.powerbank.entity.*;
import com.powerbank.entity.enums.*;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private DataStorage dataStorage;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = dataStorage.userMap.size();
        long totalPowerBanks = dataStorage.powerBankMap.size();
        
        // 获取所有在线柜机ID
        java.util.Set<String> onlineCabinetIds = dataStorage.cabinetMap.values().stream()
                .filter(c -> c.getStatus() == CabinetStatus.ONLINE)
                .map(Cabinet::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        // 可用充电宝 = 状态AVAILABLE + 在在线柜机中
        long availablePowerBanks = dataStorage.powerBankMap.values().stream()
                .filter(pb -> pb.getStatus() == PowerBankStatus.AVAILABLE 
                        && pb.getCabinetId() != null 
                        && onlineCabinetIds.contains(pb.getCabinetId()))
                .count();
        
        // 离线柜机中的充电宝（不可实际租借）
        long offlineCabinetPbs = dataStorage.powerBankMap.values().stream()
                .filter(pb -> pb.getStatus() == PowerBankStatus.AVAILABLE 
                        && pb.getCabinetId() != null 
                        && !onlineCabinetIds.contains(pb.getCabinetId()))
                .count();
        
        long totalCabinets = dataStorage.cabinetMap.size();
        long onlineCabinets = onlineCabinetIds.size();
        long totalOrders = dataStorage.rentalOrderMap.size();
        long rentingOrders = dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getStatus() == RentalOrderStatus.RENTING)
                .count();
        long overdueOrders = dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getStatus() == RentalOrderStatus.OVERDUE)
                .count();
        long lostOrders = dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getStatus() == RentalOrderStatus.LOST)
                .count();
        
        BigDecimal totalRevenue = dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getRentalFee() != null)
                .map(RentalOrder::getRentalFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCompensation = dataStorage.rentalOrderMap.values().stream()
                .filter(o -> o.getCompensationAmount() != null)
                .map(RentalOrder::getCompensationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long pendingAbnormals = dataStorage.abnormalRecordMap.values().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalPowerBanks", totalPowerBanks);
        stats.put("availablePowerBanks", availablePowerBanks);
        stats.put("offlineCabinetPbs", offlineCabinetPbs);
        stats.put("totalCabinets", totalCabinets);
        stats.put("onlineCabinets", onlineCabinets);
        stats.put("totalOrders", totalOrders);
        stats.put("rentingOrders", rentingOrders);
        stats.put("overdueOrders", overdueOrders);
        stats.put("lostOrders", lostOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCompensation", totalCompensation);
        stats.put("pendingAbnormals", pendingAbnormals);

        return stats;
    }
}
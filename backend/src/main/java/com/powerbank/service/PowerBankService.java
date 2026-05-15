package com.powerbank.service;

import com.powerbank.entity.PowerBank;
import com.powerbank.entity.enums.PowerBankStatus;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PowerBankService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private OperationLogService operationLogService;

    public PowerBank createPowerBank(String code) {
        PowerBank pb = new PowerBank();
        pb.setId(UUID.randomUUID().toString());
        pb.setCode(code);
        pb.setStatus(PowerBankStatus.AVAILABLE);
        pb.setBatteryLevel(100);
        pb.setCreateTime(LocalDateTime.now());
        pb.setUpdateTime(LocalDateTime.now());
        dataStorage.powerBankMap.put(pb.getId(), pb);
        operationLogService.log("POWERBANK", pb.getId(), "CREATE", null, "AVAILABLE", "SYSTEM", "创建充电宝");
        return pb;
    }

    public PowerBank getPowerBankById(String id) {
        return dataStorage.powerBankMap.get(id);
    }

    public List<PowerBank> getAllPowerBanks() {
        return dataStorage.powerBankMap.values().stream().collect(Collectors.toList());
    }

    public List<PowerBank> getAvailablePowerBanks() {
        return dataStorage.powerBankMap.values().stream()
                .filter(pb -> pb.getStatus() == PowerBankStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public void updateStatus(String pbId, PowerBankStatus newStatus, String remark) {
        PowerBank pb = dataStorage.powerBankMap.get(pbId);
        if (pb != null) {
            String beforeStatus = pb.getStatus().toString();
            pb.setStatus(newStatus);
            pb.setUpdateTime(LocalDateTime.now());
            operationLogService.log("POWERBANK", pbId, "STATUS_CHANGE", beforeStatus, newStatus.toString(), "SYSTEM", remark);
        }
    }

    public void markLost(String pbId) {
        updateStatus(pbId, PowerBankStatus.LOST, "标记丢失");
    }

    public void markAbnormal(String pbId) {
        updateStatus(pbId, PowerBankStatus.ABNORMAL, "标记异常");
    }
}
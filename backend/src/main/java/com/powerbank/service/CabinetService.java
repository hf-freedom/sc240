package com.powerbank.service;

import com.powerbank.entity.Cabinet;
import com.powerbank.entity.PowerBank;
import com.powerbank.entity.enums.CabinetStatus;
import com.powerbank.entity.enums.PowerBankStatus;
import com.powerbank.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CabinetService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private PowerBankService powerBankService;

    public Cabinet createCabinet(String code, String name, String location, String address, Integer totalSlots) {
        Cabinet cabinet = new Cabinet();
        cabinet.setId(UUID.randomUUID().toString());
        cabinet.setCode(code);
        cabinet.setName(name);
        cabinet.setLocation(location);
        cabinet.setAddress(address);
        cabinet.setStatus(CabinetStatus.ONLINE);
        cabinet.setTotalSlots(totalSlots);
        cabinet.setAvailableSlots(totalSlots);
        cabinet.setOccupiedSlots(0);
        cabinet.setLastHeartbeat(LocalDateTime.now());
        cabinet.setCreateTime(LocalDateTime.now());
        cabinet.setUpdateTime(LocalDateTime.now());
        cabinet.setPowerBankIds(new ArrayList<>());
        dataStorage.cabinetMap.put(cabinet.getId(), cabinet);
        operationLogService.log("CABINET", cabinet.getId(), "CREATE", null, "ONLINE", "SYSTEM", "创建柜机");
        return cabinet;
    }

    public Cabinet getCabinetById(String id) {
        return dataStorage.cabinetMap.get(id);
    }

    public List<Cabinet> getAllCabinets() {
        return dataStorage.cabinetMap.values().stream().collect(Collectors.toList());
    }

    public boolean addPowerBankToCabinet(String cabinetId, String pbId, Integer slotNumber) {
        Cabinet cabinet = dataStorage.cabinetMap.get(cabinetId);
        PowerBank pb = dataStorage.powerBankMap.get(pbId);
        if (cabinet == null || pb == null) {
            return false;
        }
        if (cabinet.getAvailableSlots() <= 0) {
            return false;
        }
        pb.setCabinetId(cabinetId);
        pb.setSlotNumber(slotNumber);
        pb.setStatus(PowerBankStatus.AVAILABLE);
        pb.setUpdateTime(LocalDateTime.now());
        cabinet.getPowerBankIds().add(pbId);
        cabinet.setAvailableSlots(cabinet.getAvailableSlots() - 1);
        cabinet.setOccupiedSlots(cabinet.getOccupiedSlots() + 1);
        cabinet.setUpdateTime(LocalDateTime.now());
        operationLogService.log("CABINET", cabinetId, "ADD_POWERBANK", null, pbId, "SYSTEM", "添加充电宝到柜机");
        return true;
    }

    public boolean removePowerBankFromCabinet(String cabinetId, String pbId) {
        Cabinet cabinet = dataStorage.cabinetMap.get(cabinetId);
        PowerBank pb = dataStorage.powerBankMap.get(pbId);
        if (cabinet == null || pb == null) {
            return false;
        }
        pb.setCabinetId(null);
        pb.setSlotNumber(null);
        pb.setUpdateTime(LocalDateTime.now());
        cabinet.getPowerBankIds().remove(pbId);
        cabinet.setAvailableSlots(cabinet.getAvailableSlots() + 1);
        cabinet.setOccupiedSlots(cabinet.getOccupiedSlots() - 1);
        cabinet.setUpdateTime(LocalDateTime.now());
        operationLogService.log("CABINET", cabinetId, "REMOVE_POWERBANK", pbId, null, "SYSTEM", "从柜机移除充电宝");
        return true;
    }

    public void updateStatus(String cabinetId, CabinetStatus newStatus, String remark) {
        Cabinet cabinet = dataStorage.cabinetMap.get(cabinetId);
        if (cabinet != null) {
            String beforeStatus = cabinet.getStatus().toString();
            cabinet.setStatus(newStatus);
            cabinet.setUpdateTime(LocalDateTime.now());
            operationLogService.log("CABINET", cabinetId, "STATUS_CHANGE", beforeStatus, newStatus.toString(), "SYSTEM", remark);
        }
    }

    public void heartbeat(String cabinetId) {
        Cabinet cabinet = dataStorage.cabinetMap.get(cabinetId);
        if (cabinet != null) {
            cabinet.setLastHeartbeat(LocalDateTime.now());
            if (cabinet.getStatus() == CabinetStatus.OFFLINE) {
                cabinet.setStatus(CabinetStatus.ONLINE);
                operationLogService.log("CABINET", cabinetId, "ONLINE", "OFFLINE", "ONLINE", "SYSTEM", "柜机恢复在线");
            }
        }
    }

    public void transferPowerBank(String fromCabinetId, String toCabinetId, String pbId) {
        if (removePowerBankFromCabinet(fromCabinetId, pbId)) {
            Cabinet toCabinet = dataStorage.cabinetMap.get(toCabinetId);
            if (toCabinet != null && toCabinet.getAvailableSlots() > 0) {
                int slot = findAvailableSlot(toCabinet);
                addPowerBankToCabinet(toCabinetId, pbId, slot);
                operationLogService.log("TRANSFER", pbId, "TRANSFER", fromCabinetId, toCabinetId, "SYSTEM", "网点调拨");
            }
        }
    }

    private int findAvailableSlot(Cabinet cabinet) {
        for (int i = 1; i <= cabinet.getTotalSlots(); i++) {
            int slot = i;
            boolean occupied = cabinet.getPowerBankIds().stream()
                    .map(pbId -> dataStorage.powerBankMap.get(pbId))
                    .anyMatch(pb -> pb != null && pb.getSlotNumber() != null && pb.getSlotNumber() == slot);
            if (!occupied) {
                return slot;
            }
        }
        return 1;
    }
}
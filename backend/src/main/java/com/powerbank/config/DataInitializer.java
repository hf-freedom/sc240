package com.powerbank.config;

import com.powerbank.entity.*;
import com.powerbank.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PowerBankService powerBankService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private BillingService billingService;

    @Override
    public void run(String... args) throws Exception {
        billingService.createDefaultRule();

        User user1 = userService.createUser("张三", "13800138001");
        userService.rechargeDeposit(user1.getId(), new BigDecimal("200.00"));

        User user2 = userService.createUser("李四", "13800138002");
        userService.rechargeDeposit(user2.getId(), new BigDecimal("100.00"));

        PowerBank pb1 = powerBankService.createPowerBank("PB001");
        PowerBank pb2 = powerBankService.createPowerBank("PB002");
        PowerBank pb3 = powerBankService.createPowerBank("PB003");
        PowerBank pb4 = powerBankService.createPowerBank("PB004");
        PowerBank pb5 = powerBankService.createPowerBank("PB005");
        PowerBank pb6 = powerBankService.createPowerBank("PB006");

        Cabinet cabinet1 = cabinetService.createCabinet("C001", "中关村店", "北京市海淀区", "中关村大街1号", 8);
        Cabinet cabinet2 = cabinetService.createCabinet("C002", "国贸店", "北京市朝阳区", "国贸中心B座", 6);

        cabinetService.addPowerBankToCabinet(cabinet1.getId(), pb1.getId(), 1);
        cabinetService.addPowerBankToCabinet(cabinet1.getId(), pb2.getId(), 2);
        cabinetService.addPowerBankToCabinet(cabinet1.getId(), pb3.getId(), 3);
        cabinetService.addPowerBankToCabinet(cabinet2.getId(), pb4.getId(), 1);
        cabinetService.addPowerBankToCabinet(cabinet2.getId(), pb5.getId(), 2);
        cabinetService.addPowerBankToCabinet(cabinet2.getId(), pb6.getId(), 3);

        System.out.println("========================================");
        System.out.println("数据初始化完成！");
        System.out.println("用户ID1: " + user1.getId());
        System.out.println("用户ID2: " + user2.getId());
        System.out.println("柜机ID1: " + cabinet1.getId());
        System.out.println("柜机ID2: " + cabinet2.getId());
        System.out.println("========================================");
    }
}
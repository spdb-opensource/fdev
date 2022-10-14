package com.spdb.fdev.fdevenvconfig.spdb.quartz;

import com.spdb.fdev.fdevenvconfig.spdb.service.ModelEnvUpdateApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UpdateModelEnvUpdateApplyStatusCallable implements Runnable {

    @Autowired
    private ModelEnvUpdateApplyService modelEnvUpdateApplyService;

    @Override
    public void run() {
        // 5天前的凌晨
        String dateTime = LocalDate.now().minusDays(5) + " 00:00:00";
        modelEnvUpdateApplyService.updateStatus(dateTime);
    }

}

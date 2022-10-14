package com.spdb.fdev.fdevinterface.spdb.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RefreshScope
public class FdevInterfaceSchedulingConfigurer implements SchedulingConfigurer {

    private Logger logger = LoggerFactory.getLogger(FdevInterfaceSchedulingConfigurer.class);

    @Value("${download.esb.excel.cron}")
    private String downloadEsbExcelCron;
    @Value("${clear.scan.record.cron}")
    private String clearScanRecordCron;
    @Autowired
    private DownloadEsbExcelCallable downloadEsbExcelCallable;
    @Autowired
    private ClearScanRecordCallable clearScanRecordCallable;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addCronTask(downloadEsbExcelCallable, downloadEsbExcelCron);
        taskRegistrar.addCronTask(clearScanRecordCallable, clearScanRecordCron);
    }

}

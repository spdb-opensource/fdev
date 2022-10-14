package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.ReportDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportCacheSonarProjectAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(ReportCacheSonarProjectAction.class);

    @Autowired
    private ReportDataService reportDataService;

    @Override
    public void execute(Context context) {
        try {
            reportDataService.cacheSonarProject();
        } catch (Exception e) {
            logger.error("度量定时获取sonar数据异常", e);
        }
    }
}
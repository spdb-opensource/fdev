package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.ConfService;
import com.spdb.executor.service.ReportDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfAssessAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(ConfAssessAction.class);

    @Autowired
    private ConfService confService;

    @Override
    public void execute(Context context) {
        try {
            confService.syncConfState();
        } catch (Exception e) {
            logger.error("Conflence定时获取数据异常", e);
        }
    }
}
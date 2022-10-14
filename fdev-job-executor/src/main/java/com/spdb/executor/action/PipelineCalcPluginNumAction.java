package com.spdb.executor.action;


import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.PipelineScheduleService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class PipelineCalcPluginNumAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(PipelineCalcPluginNumAction.class);

    @Autowired
    private PipelineScheduleService pipelineScheduleService;

    @Override
    public void execute(Context context) {
        pipelineScheduleService.calcPluginNumCi();
    }
}

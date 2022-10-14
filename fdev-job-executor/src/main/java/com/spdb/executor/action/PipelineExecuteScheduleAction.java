package com.spdb.executor.action;


import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.PipelineScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PipelineExecuteScheduleAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(PipelineExecuteScheduleAction.class);

    @Autowired
    private PipelineScheduleService pipelineScheduleService;

    @Override
    public void execute(Context context) {
        pipelineScheduleService.pipelineExecuteCi();
    }
}

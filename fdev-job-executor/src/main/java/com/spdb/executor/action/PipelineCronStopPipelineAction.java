package com.spdb.executor.action;


import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.PipelineScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PipelineCronStopPipelineAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(PipelineCronStopPipelineAction.class);

    @Autowired
    private PipelineScheduleService pipelineScheduleService;

    @Override
    public void execute(Context context) {
        //定时停止流水线
        pipelineScheduleService.cronStopPipeline();
    }
}

package com.spdb.executor.service.impl;


import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.PipelineScheduleService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Service
public class PipelineScheduleServiceImpl implements PipelineScheduleService {

    private static Logger logger = LoggerFactory.getLogger(PipelineScheduleServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Async
    @Override
    public void pipelineExecuteCi() {
        logger.info(">>>>>>>>> execute pipelineExecuteSchedule begin <<<<<<<<");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "pipelineExecuteScheduleCi");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("定时发送执行流水线失败", e);
            throw new FdevException("定时发送执行流水线失败");
        }
        logger.info(">>>>>>>>> execute pipelineExecuteSchedule end <<<<<<<<<<<");
    }

    @Async
    @Override
    public void calcPluginNumCi() {
        logger.info(">>>>>>>>> execute PipelineCalcPluginNum begin <<<<<<<<");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "pipelineCalcPluginNumCi");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("查询插件使用失败", e);
            throw new FdevException("查询插件使用失败");
        }
        logger.info(">>>>>>>>> execute PipelineCalcPluginNum end <<<<<<<<<<<");
    }


    @Override
    public void cronStopPipeline() {
        logger.info(">>>>>>>>> execute cronStopPipeline begin <<<<<<<<");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "cronStopPipeline");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("定时停止流水线失败", e);
            throw new FdevException("定时停止流水线失败");
        }
        logger.info(">>>>>>>>> execute cronStopPipeline end <<<<<<<<<<<");
    }
}

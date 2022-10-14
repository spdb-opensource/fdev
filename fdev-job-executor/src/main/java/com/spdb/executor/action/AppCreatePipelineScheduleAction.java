package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class AppCreatePipelineScheduleAction extends BaseExecutableAction {
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute AppCreatePipelineSchedule begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "appCreatePipelineSchedule");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("应用添加定时pipeline失败", e);
            throw new FdevException("应用添加定时pipeline失败");
        }
        logger.info("execute AppCreatePipelineSchedule end");
    }
}

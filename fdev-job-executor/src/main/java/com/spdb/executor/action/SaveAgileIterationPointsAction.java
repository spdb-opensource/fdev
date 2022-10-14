package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class SaveAgileIterationPointsAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(SaveAgileIterationPointsAction.class);
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute saveAgileIterationPoints begin！");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "saveAgileIterationPoints");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("保存每日未完成迭代故事点数失败", e);
            throw new FdevException("保存每日未完成迭代故事点数");
        }
        logger.info("execute saveAgileIterationPoints end！");
    }
}

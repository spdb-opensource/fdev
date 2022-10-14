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

public class UpdateApplicationTaskFlagAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(UpdateApplicationTaskFlagAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("刷新组件和应用关联关系开始");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "updateApplicationTaskFlag");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("刷新组件和应用关联关系失败", e);
            throw new FdevException("刷新组件和应用关联关系失败");
        }
        logger.info("刷新组件和应用关联关系结束");
    }
}


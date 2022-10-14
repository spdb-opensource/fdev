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

public class ArchetypeVersionNotifyAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(ArchetypeVersionNotifyAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("检查骨架使用组件版本开始");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "archetypeVersionNotify");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("检查骨架使用组件版本失败", e);
            throw new FdevException("检查骨架使用组件版本失败");
        }
        logger.info("检查骨架使用组件版本结束");
    }
}

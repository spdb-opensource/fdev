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

public class IamsCronAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(IamsCronAction.class);
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute IamsCron begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "iamsDbDelLog");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("需求定时发送邮件失败", e);
            throw new FdevException("需求定时发送邮件失败");
        }
        logger.info("execute IamsCron end");
    }
}

package com.spdb.executor.action;

import java.util.HashMap;

import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;

public class KfOffBatchNotifyAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(KfOffBatchNotifyAction.class);
    
    @Autowired
    private RestTransport restTransport;
    
    @Override
    public void execute(Context context) {
        logger.info("execute KfOffBatchNotify begin");
    	//发送用户模块接口  没n个月
    	HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "sendKfOffEmail");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("发送邮件提醒批量关闭错误", e);
            throw new FdevException("发送邮件提醒批量关闭错误");
        }
        logger.info("execute KfOffBatchNotify end");
    }
}

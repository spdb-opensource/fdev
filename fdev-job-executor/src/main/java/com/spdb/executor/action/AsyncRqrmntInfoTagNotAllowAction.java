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

public class AsyncRqrmntInfoTagNotAllowAction extends BaseExecutableAction{
	private static Logger logger = LoggerFactory.getLogger(AsyncRqrmntInfoTagNotAllowAction.class);
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute asyncRqrmntInfoTagNotAllow begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "asyncRqrmntInfoTagNotAllow");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("async rqrmnt not allow error", e);
            throw new FdevException("async rqrmnt not allow error");
        }
        logger.info("execute asyncRqrmntInfoTagNotAllow end");
    }
	
}

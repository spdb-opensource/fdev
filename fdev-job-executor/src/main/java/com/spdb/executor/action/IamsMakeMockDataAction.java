package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class IamsMakeMockDataAction extends BaseExecutableAction {
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute IamsMakeMockData begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "iamsMakeMockData");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("定时更新挡板mock数据", e);
            throw new FdevException("定时更新挡板mock数据");
        }
        logger.info("execute IamsMakeMockData end");
    }
}

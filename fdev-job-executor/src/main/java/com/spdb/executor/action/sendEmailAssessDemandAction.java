package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class sendEmailAssessDemandAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(sendEmailAssessDemandAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("发送需求评估超期通知邮件开始");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "sendEmailAssessDemand");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("发送需求评估超期通知邮件,{}", e.getMessage());
        }
        logger.info("发送需求评估超期通知邮件结束");
    }
}

package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeException;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: fdev-job-executor
 * @description:
 * @author: c-gaoys
 * @create: 2021-07-21 10:08
 **/
public class ClearTaskStatusAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(ClearTaskStatusAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) throws PeException {
        logger.info("清除任务看板无效状态");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "clearTaskStatus");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("清除任务看板无效状态失败,{}", e.getMessage());
        }
        logger.info("清除任务看板无效状态结束");
    }
}

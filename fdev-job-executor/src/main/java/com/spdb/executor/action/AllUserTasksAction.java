package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.UserRealTimeTasksService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllUserTasksAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(AllUserTasksAction.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private UserRealTimeTasksService userRealTimeTasksService;

    @Override
    public void execute(Context context) {
        logger.info("execute AllUserTasks begin");
        Map map = new HashMap();
        List<Map> users = new ArrayList<>();
        map.put(Dict.STATUS, "0");
        map.put(Dict.REST_CODE, "queryAllUsers");
        try {
            users = (List<Map>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("查找所有用户失败");
            throw new FdevException("查找所有用户失败");
        }
        try {
            userRealTimeTasksService.realTimeTaskNotiy(users);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException("发送通知失败!");
        }
        logger.info("execute AllUserTasks end");
    }
}

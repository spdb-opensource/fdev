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
import java.util.Map;

public class UpdateLeaveUserAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(UpdateLeaveUserAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute UpdateLeaveUserAction begin");
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "updateAllLeaveUser");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("批量更新用户离职信息错误", e);
            throw new FdevException("批量更新用户离职信息错误");
        }
        logger.info("execute UpdateLeaveUserAction end");
    }
}

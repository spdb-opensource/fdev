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

public class DelAppManagerAndMemberAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(DelAppManagerAndMemberAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("清理过期的项目成员与负责人开始");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "delAppManagerAndMember");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("清理过期的项目成员与负责人,{}", e.getMessage());
        }
        logger.info("清理过期的项目成员与负责人结束");
    }
}

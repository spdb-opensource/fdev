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

public class UpdateConponentDependencyTreeAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(UpdateConponentDependencyTreeAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("刷新组件依赖树开始");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "updateConponentDependencyTree");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("刷新组件依赖树开始失败", e);
            throw new FdevException("刷新组件依赖树开始失败");
        }
        logger.info("刷新组件依赖树开始结束");
    }
}

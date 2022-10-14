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

public class ArchetypeAutoScanAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(ArchetypeAutoScanAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("刷新最新骨架使用组件版本开始");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "archetypeAutoScan");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("刷新最新骨架使用组件版本失败,{}", e.getMessage());
            throw new FdevException("刷新最新骨架使用组件版本失败");
        }
        logger.info("刷新最新骨架使用组件版本结束");
    }
}

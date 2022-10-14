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

public class ClearScanRecordAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(ClearScanRecordAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("接口扫描记录清理开始");
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "timingClearScanRecord");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("接口扫描记录清理,{}", e.getMessage());
        }
        logger.info("接口扫描记录清理结束");
    }
}

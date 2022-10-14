package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeException;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

public class BatchUpdateTaskDocAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(BatchUpdateTaskDocAction.class);

    @Autowired
    private RestTransport restTransport;

    @Value("${fdev.resource}")
    private String fdevResources;

    @Override
    public void execute(Context context) throws PeException {
        logger.info("execute BatchUpdateTaskDoc begin");
        //发送用户模块接口  没n个月
        HashMap requestParams = new HashMap();
        requestParams.put(Dict.REST_CODE, "batchUpdateTaskDoc");
        requestParams.put("path",fdevResources);
        try {
            restTransport.submit(requestParams);
        } catch (Exception e) {
            logger.error("定时任务同步文档数据批量任务非正常停止", e);
            throw new FdevException("定时任务同步文档数据批量任务非正常停止");
        }
        logger.info("execute BatchUpdateTaskDoc end");
    }
}

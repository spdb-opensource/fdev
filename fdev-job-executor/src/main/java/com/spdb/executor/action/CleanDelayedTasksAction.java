package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class CleanDelayedTasksAction extends BaseExecutableAction {
    @Autowired
    private RestTransport restTransport;
    @Override
    public void execute(Context context) {
        logger.info("execute CleanDelayedTasks begin");
        String date=CommonUtils.formatDate("yyyyMMdd");
        HashMap<String,Object> requestParams=new HashMap();
        requestParams.put(Dict.RELEASE_DATE,date);
        requestParams.put(Dict.REST_CODE,"batchCleanDelayedTasks");
        try {
            restTransport.submit(requestParams);
        } catch (Exception e) {
            logger.error("批量清理未投产任务失败",e);
            throw new FdevException("批量清理未投产任务失败");
        }
        logger.info("execute CleanDelayedTasks end");
    }

}

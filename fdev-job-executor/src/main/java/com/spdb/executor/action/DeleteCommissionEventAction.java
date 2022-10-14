package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;

public class DeleteCommissionEventAction extends BaseExecutableAction {

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public void execute(Context context) {
        this.deleteCommissionEvent();
        this.deleteCommissionEventNew();
    }

    @Async
    private void deleteCommissionEventNew() {
        logger.info("execute DeleteCommissionEventNew begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "batchDeleteCommissionEventNew");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("批量删除新用户代办事项错误", e);
            throw new FdevException("批量删除新用户代办事项错误");
        }
        logger.info("execute DeleteCommissionEventNew end");
    }

    @Async
    private void deleteCommissionEvent() {
        logger.info("execute DeleteCommissionEvent begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "batchDeleteCommissionEvent");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("批量删除用户代办事项错误", e);
            throw new FdevException("批量删除用户代办事项错误");
        }
        logger.info("execute DeleteCommissionEvent end");
    }
}

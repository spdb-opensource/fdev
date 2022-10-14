package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class UpdateProdTaskStatusAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(UpdateProdTaskStatusAction.class);

    public static final int THREE_DAY_AGO = -4;

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute UpdateProdTaskStatus begin");
        String date = CommonUtils.calculationDay(CommonUtils.DATE_PARSE, THREE_DAY_AGO);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.START_DATE, date);
        map.put(Dict.END_DATE, date);
        map.put(Dict.REST_CODE, "updateTaskArchivedBatch");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("批量更新投产任务错误", e);
            throw new FdevException("批量更新投产任务错误");
        }
        logger.info("execute UpdateProdTaskStatus end");
    }
}

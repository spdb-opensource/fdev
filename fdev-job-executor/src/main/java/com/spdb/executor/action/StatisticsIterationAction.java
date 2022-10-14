package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.FstatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class StatisticsIterationAction extends BaseExecutableAction {

    @Autowired
    FstatisticsService service;

    private static Logger logger = LoggerFactory.getLogger(StatisticsIterationAction.class);

    /**
     * 大屏后端每天更新用户信息、需求迭代,每天0:10执行更新当天数据
     * 此接口需查询当前任务各状态数量,不能根据日期更新,只能更新当前数据
     * @param context
     */
    @Override
    public void execute(Context context) {
        service.refreshUserIteration();
        service.refreshDemandStatus();
        service.refreshDevCodesRepeat();
        service.refreshInterfaceTestData();
        service.refreshPipelineData();
        logger.info("异步发起大屏后端接口调用完成");
    }

}

package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.AutoTestService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 部署自动化测试环境
 *
 * @author xxx
 * @date 2020/5/18 15:11
 */
public class AutoTestAction extends BaseExecutableAction {

    @Autowired
    private AutoTestService autoTestService;

    @Override
    public void execute(Context context) {
        logger.info("execute AutoTest begin");
        autoTestService.autoTest();
        logger.info("execute AutoTest end");
    }

}

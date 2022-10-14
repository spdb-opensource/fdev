package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.action.IamsCronAction;
import com.spdb.executor.service.FstatisticsService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FstatisticsServiceImpl implements FstatisticsService {

    @Autowired
    RestTransport restTransport;

    private static Logger logger = LoggerFactory.getLogger(IamsCronAction.class);

    @Async
    @Override
    public void refreshUserIteration() {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.REST_CODE, "refreshIteration");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("大屏后端更新需求迭代异常", e);
        }

        param.put(Dict.REST_CODE, "saveProductCompanyUserEvalution");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("大屏后端更新用户信息异常", e);
        }

        param.put(Dict.REST_CODE, "savePublishInfo");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("大屏后端更新变更信息异常", e);
        }
    }

    @Async
    @Override
    public void refreshDemandStatus() {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.REST_CODE, "saveDemandTurnRelateInfo");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("保存新增与投产需求数据异常", e);
        }
    }

    @Async
    @Override
    public void refreshDevCodesRepeat() {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.REST_CODE, "saveDevCodesRepeat");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新代码重复率数据异常", e);
        }
    }

    @Async
    @Override
    public void refreshInterfaceTestData() {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.REST_CODE, "saveInterfaceTestData");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新接口自动化覆盖率与成功率数据异常", e);
        }
    }

    @Async
    @Override
    public void refreshPipelineData() {
        Map<String, String> param = new HashMap<>();
        param.put("date", DateUtils.getLastDay());
        param.put(Dict.REST_CODE, "savePipelineRestoreTimeByDate");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新pipeline相关数据异常", e);
        }
    }
}

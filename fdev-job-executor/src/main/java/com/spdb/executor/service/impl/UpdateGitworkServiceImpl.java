package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.action.UpdateGitworkAction;
import com.spdb.executor.service.UpdateGitworkService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: zhangxt
 * @Date: 2021/4/13 18:22
 **/
@Service
public class UpdateGitworkServiceImpl implements UpdateGitworkService {


    private static Logger logger = LoggerFactory.getLogger(UpdateGitworkServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    /**
     * 因为接口发送时间过长，所以发异步
     */
    @Async
    @Override
    public void batchCodeNew() {
        logger.info("execute updateGitworkEvent begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "batchCodeStatisticsNew");
        try {
            restTransport.submit(requstParams);

        } catch (Exception e) {
            logger.error("批量gitlabwork-new代码统计失败", e);
            throw new FdevException("批量gitlabwork-new代码统计失败");
        }
        logger.info("execute updateGitworkEvent end");
    }

    /**
     * 因为接口发送时间过长，所以发异步
     */
    @Async
    @Override
    public void batchCode() {
        logger.info("execute updateGitworkEvent begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "batchCodeStatistics");
        try {
            restTransport.submit(requstParams);

        } catch (Exception e) {
            logger.error("批量gitlabwork代码统计失败", e);
            throw new FdevException("批量gitlabwork代码统计失败");
        }
        logger.info("execute updateGitworkEvent end");
    }
}

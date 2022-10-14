package com.spdb.fdev.component.strategy.webhook.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IMpassRelaseIssueService;
import com.spdb.fdev.component.strategy.webhook.WebhookEventStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/2/10 10:43
 */
@Component(Dict.PIPELINE_MPASS_COMPONENT)
@RefreshScope
public class MpassComponentPipelineEventStrategyImpl implements WebhookEventStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MpassComponentPipelineEventStrategyImpl.class);

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;

    @Override
    public void eventExecutor(Map<String, Object> parse) throws Exception {
        // 1、解析参数
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        String status = (String) attributes.get(Dict.STATUS);
        String pipeId = String.valueOf(attributes.get(Dict.ID));
        Boolean tag = (Boolean) attributes.get(Constants.TAG);//是否tag分支
        if (Dict.SUCCESS.equals(status) && tag) {
            String ref = (String) attributes.get(Dict.REF);//当前分支名
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String gitlab_url = (String) project.get(Constants.WEB_URL);//项目gitlab地址
            String gitlabId = project.get(Dict.ID) + "";
            logger.info("pipeId: {}, status: {}, gitlabId:{}, ref: {}", pipeId, status, gitlabId, ref);
            mpassRelaseIssueService.pipiCallBack(gitlab_url, gitlabId, ref);
            logger.info("前端组件回调执行成功");
        }
    }
}

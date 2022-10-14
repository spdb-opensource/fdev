package com.spdb.fdev.component.strategy.webhook.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IBaseImageRecordService;
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
@Component(Dict.PIPELINE_IMAGE)
@RefreshScope
public class BaseImagePipelineEventStrategyImpl implements WebhookEventStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BaseImagePipelineEventStrategyImpl.class);

    @Autowired
    private IBaseImageRecordService baseImageRecordService;


    @Override
    public void eventExecutor(Map<String, Object> parse) throws Exception {
        // 1、解析参数
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        String status = (String) attributes.get(Dict.STATUS);
        String pipeId = String.valueOf(attributes.get(Dict.ID));

        Boolean tag = (Boolean) attributes.get(Constants.TAG);//是否tag分支
        if (Dict.SUCCESS.equals(status) && tag) {
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String ref = (String) attributes.get(Dict.REF);//当前分支名
            //项目gitlab id
            String gitlabId = project.get(Dict.ID) + "";
            logger.info("gitlabId:{}, ref:{}, pipeId; {}, status: {}", gitlabId, ref, pipeId, status);
            baseImageRecordService.pipiCallBack(gitlabId, ref);
            logger.info("镜像回调执行成功");
        }
    }
}

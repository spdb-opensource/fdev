package com.spdb.fdev.component.strategy.webhook.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IArchetypeRecordService;
import com.spdb.fdev.component.strategy.webhook.WebhookEventStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/2/10 10:43
 */
@Component(Dict.PIPELINE_ARCHETYPE)
@RefreshScope
public class ArchetypePipelineEventStrategyImpl implements WebhookEventStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ArchetypePipelineEventStrategyImpl.class);

    @Autowired
    private IArchetypeRecordService archetypeRecordService;


    @Override
    public void eventExecutor(Map<String, Object> parse) throws Exception {
        // 1、解析参数
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        String status = (String) attributes.get(Dict.STATUS);
        String pipeId = String.valueOf(attributes.get(Dict.ID));
        if (Dict.SUCCESS.equals(status)) {
            String version = null;
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String gitlabId = project.get(Dict.ID) + "";
            List<Map> mapList = (List<Map>) attributes.get("variables");
            if (mapList != null && mapList.size() > 0) {
                for (Map map : mapList) {
                    if ("version".equals(map.get("key"))) {
                        version = (String) map.get("value");
                    }
                }
            }
            logger.info("gitlabId:{}, version:{}, pipeId; {}, status: {}", gitlabId, version, pipeId, status);
            archetypeRecordService.pipiCallBack(version, pipeId);
            logger.info("骨架回调执行成功");
        }
    }
}

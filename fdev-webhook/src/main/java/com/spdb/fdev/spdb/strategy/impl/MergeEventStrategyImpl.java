package com.spdb.fdev.spdb.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.KafkaService;
import com.spdb.fdev.spdb.service.MergeEventService;
import com.spdb.fdev.spdb.strategy.WebhookEventStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(Dict.MERGE_REQUEST_HOOK)
@RefreshScope
public class MergeEventStrategyImpl implements WebhookEventStrategy {

    @Value("${kafka.merge.topic}")
    private String kafkaMergeTopic;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private List<MergeEventService> mergeEventServiceList;

    @Override
    public Object eventExecutor(Map<String, Object> parse) {
        Map<String, Object> projectInfo = (Map<String, Object>) parse.get(Dict.PROJECT);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        //获取merge iid
        Integer iid = (Integer) attributes.get(Dict.IID);
        // 合并的目标分支
        String targetBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        String state = (String) attributes.get(Dict.STATE);
        // 发送数据
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(Dict.PROJECT_ID, projectId);
        logMap.put(Dict.IID, iid);
        logMap.put(Dict.TARGET_BRANCH, targetBranch);
        logMap.put(Dict.STATE, state);
        kafkaService.sendData(kafkaMergeTopic, JSON.toJSONString(parse).getBytes(), logMap);

        for (MergeEventService mergeEventService : mergeEventServiceList) {
            // sonar扫描、记录mergedRequest信息
            mergeEventService.doMerge(parse);
        }
        return null;
    }
}

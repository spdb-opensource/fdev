package com.spdb.fdev.fdevtask.spdb.listener;

import com.google.gson.Gson;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.GitApiService;
import com.spdb.fdev.fdevtask.spdb.service.MergeInfoCallBack;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class KafkaMsgListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MergeInfoCallBack mergeInfoCallBack;
    @Autowired
    private GitApiService gitApiService;

    @KafkaListener(topics = {"#{ '${topicName}' }"}, groupId = "#{ '${groupId}' }")
    public void receiveMergeSit(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        String params = (String) consumerRecord.value();
        logger.info("kafka data:{}", params);
        // 获得kafka消息，做后续逻辑处理
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map = gson.fromJson(params, map.getClass());
        Map<String, Object> projectInfo = (Map<String, Object>) map.get(Dict.PROJECT);
        Map<String, Object> object_attributes = (Map<String, Object>) map.get(Dict.OBJECT_ATTRIBUTES);
        String iid = String.valueOf(((Double) object_attributes.get(Dict.IID)).intValue());
        String state = (String) object_attributes.get(Dict.STATE);
        Map<String, Object> userInfo = (Map<String, Object>) map.get(Dict.USER);
        String username = (String) userInfo.get(Dict.USERNAME);
        String project_id = String.valueOf(((Double) projectInfo.get("id")).intValue());
        if ("merged".equals(state) || "closed".equals(state)) {
            mergeInfoCallBack.mergeInfoCallBack(project_id, iid, state, username);
        } else if ("unchecked".equals(state)) {
            logger.info(">>>>merge request state is unchecked,projectId:{},iid:{}",project_id,iid);
            Thread.sleep(1000);
            //重新查询合并请求信息
            Map<String, Object> mergeInfo = gitApiService.getMergeInfo(project_id, iid);
            state = (String) mergeInfo.get(Dict.STATE);
            if ("merged".equals(state) || "closed".equals(state)) {
                mergeInfoCallBack.mergeInfoCallBack(project_id, iid, state, username);
            }
        }
    }
}

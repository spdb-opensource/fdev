package com.spdb.fdev.base.listener;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.service.IMergedInfoCallBackService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafaMsgListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private IMergedInfoCallBackService mergedInfoCallBackService;

    @KafkaListener(topics = {"#{'${topicName}'}"}, groupId = "fdev-release")
    public void receiveMergeMessage(ConsumerRecord<?, ?> consumerRecord) throws Exception{
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        JSONObject message = JSONObject.parseObject(params);
        logger.info("收到kafka消息: " + params);
        Map<String, Object> projectInfo = (Map<String, Object>) message.get(Dict.PROJECT);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        Map<String, Object> attributes = (Map<String, Object>) message.get(Dict.OBJECT_ATTRIBUTES);
        Integer iid = (Integer) attributes.get(Dict.IID);
        String state = (String) attributes.get(Dict.STATE);
        // 合并的目标分支
        String mergeBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        Map<String, Object> userInfo = (Map<String, Object>) message.get(Dict.USER);
        String username = (String) userInfo.get(Dict.USERNAME);
        Map<String,Object> requestMergeCallBack = new HashMap<>();
        requestMergeCallBack.put(Dict.GITLAB_PROJECT_ID,projectId);
        requestMergeCallBack.put(Dict.MERGE_STATE,state);
        requestMergeCallBack.put(Dict.MERGE_REQUEST_ID,iid);
        requestMergeCallBack.put(Dict.USERNAME, username);
        if(Dict.MASTER.equals(mergeBranch)) {
        	 mergedInfoCallBackService.mergedCallBack(requestMergeCallBack);
        } else if(mergeBranch.startsWith(Dict.TRANSITION)) {
        	 mergedInfoCallBackService.testRunMergedCallBack(requestMergeCallBack);
        } else {
            logger.info("其他分支合并不做处理，目标分支名：{}", mergeBranch);
        }
       
    }

}

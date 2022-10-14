package com.fdev.database.listener;

import com.alibaba.fastjson.JSONObject;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.service.MergedInfoCallBackService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMsgListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MergedInfoCallBackService mergedInfoCallBackService;

    @KafkaListener(topics = {"${topicName}"}, groupId = "fdev-database")
    public void receiveMergeMessage(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        try{
            String params = (String) consumerRecord.value();
            // 获得kafka消息，做后续逻辑处理
            JSONObject message = JSONObject.parseObject(params);
            Map<String, Object> projectInfo = (Map<String, Object>) message.get(Dict.PROJECT);
            Integer projectId = (Integer) projectInfo.get(Dict.ID);
            Map<String, Object> attributes = (Map<String, Object>) message.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            // 合并的分支
            String sourceBranch = (String) attributes.get(Dict.SOURCE_BRANCH);
            // 合并的目标分支
            String mergeBranch = (String) attributes.get(Dict.TARGET_BRANCH);
            //只记录最后一个commit记录，多次提交合并不能显示出来
    //        Map lastCommit = (Map) attributes.get("last_commit");
    //        String commitId = (String) lastCommit.get(Dict.ID);


            if (Dict.MASTER.equals(mergeBranch) && Dict.MERGED.equals(state)) {
                mergedInfoCallBackService.mergedCallBack(projectId, sourceBranch, mergeBranch);
            }
    //        else if (Dict.SIT.equals(mergeBranch) && Dict.MERGED.equals(state)) {
    //            mergedInfoCallBackService.mergedCallBackSit(projectId, sourceBranch, mergeBranch);
    //        }
            else {
                logger.info("只对分支状态为merged做处理，目标分支名：{},状态：{}", mergeBranch, state);
            }
        } catch (Exception e) {
            logger.error("获取kafka消息异常", e);
        }
    }

}

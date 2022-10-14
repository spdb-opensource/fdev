package com.fdev.notify.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fdev.notify.dict.Dict;
import com.fdev.notify.entity.Message;
import com.fdev.notify.service.MessageService;
import com.fdev.notify.util.CommonUtils;
import com.fdev.notify.websocket.WebSocketServer;
import com.spdb.fdev.common.util.SpringContextUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@RefreshScope
public class KafkaMsgListener {
    private static Logger logger = LoggerFactory.getLogger(KafkaMsgListener.class);// 日志打印

    @Value("${kafka.refresh.app}")
    private List<String> refreshApps;//固定监控的应用

    @Resource
    private MessageService messageService;

    @KafkaListener(topics = {"${kafka.push.topic}"}, groupId = "${kafka.listener.group}")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        byte[] bytes = (byte[]) consumerRecord.value();
        Map<String, Object> parse = JSON.parseObject(new String(bytes), Map.class); //整个 msg数据
        Map object_attributes = (Map) parse.get(Dict.OBJECTATTRIBUTES);
        Map project = (Map) parse.get(Dict.PROJECT);
        String status = object_attributes.get(Dict.STATUS).toString();  //  状态  成功，失败，padding
        String name = project.get(Dict.NAME).toString();   //应用名
        if (!refreshApps.contains(name))    //是否在监测应用内
            return;
        if (!status.equalsIgnoreCase(Dict.SUCCESS)) //是否执行成功
            return;

        String ref = object_attributes.get(Dict.REF).toString();    // 目标名 ref
        List variables = (List) object_attributes.get(Dict.VARIABLES);    // 参数
        String activeProfile = SpringContextUtil.getActiveProfile();    //目前环境

        //当环境与目标ref相同（sit、uat）， 或 环境为rel 目标分支为 pro，且不携带参数，  或 环境为 pro目标分支为pro，且携带参数
        if (activeProfile.toUpperCase().contains(ref.toUpperCase()) ||
            activeProfile.toUpperCase().contains(Dict.REL.toUpperCase()) && ref.toUpperCase().startsWith(Dict.PRO.toUpperCase()) && CommonUtils.isNullOrEmpty(variables) ||
            activeProfile.toUpperCase().contains(Dict.PRO.toUpperCase()) && ref.toUpperCase().startsWith(Dict.PRO.toUpperCase()) && !CommonUtils.isNullOrEmpty(variables)
        ){
            Message message = new Message("fdev应用发生变更，请刷新后使用", Dict.ALWAYSEXPIRYTIME);
            message.setType(Dict.VERSIONREFRESHTYPE);
            message.setDesc("版本更新");
            messageService.addMessage(message);
            WebSocketServer.sendAnnounceRealTime(message);
            logger.info("---->" + message.toString());

        }

    }


}

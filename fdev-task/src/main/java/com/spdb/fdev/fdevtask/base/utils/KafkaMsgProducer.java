package com.spdb.fdev.fdevtask.base.utils;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @program: fdev-task
 * @description:
 * @author: c-gaoys
 * @create: 2020-12-15 10:39
 **/
@Component
public class KafkaMsgProducer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private RestTransport restTransport;


    /**
     * 发送消息时通知玉衡，发送失败时调用相关接口
     * @param parse
     */
    public void sendMessage(Map parse){
        String kafkaTopic = (String)parse.remove("kafkaTopic");
        String restCode = (String)parse.remove(Dict.REST_CODE);
        try {
            kafkaTemplate.send(kafkaTopic,JSON.toJSONString(parse).getBytes());
            logger.info("topic:{}发送成功,params:{}",kafkaTopic,parse);
        } catch (Exception e) {
            logger.info("topic:{}发送失败,params:{}",kafkaTopic,parse);
            parse.put(Dict.REST_CODE, restCode);
            try {
                restTransport.submit(parse);
            } catch (Exception ex) {
                logger.error("发送玉衡接口失败,topic:{},interface:{},params:{}",kafkaTopic,restCode,parse);
            }
        }
    }
}

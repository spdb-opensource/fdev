package com.spdb.fdev.fdevinterface.base.utils;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceStatisticsService;
import com.spdb.fdev.fdevinterface.spdb.service.ScanSkipService;
import com.spdb.fdev.fdevinterface.spdb.service.ScannerService;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class KafkaMsgListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ScannerService scannerService;
    @Resource
    private ScanSkipService scanSkipService;
    @Resource
    private InterfaceStatisticsService interfaceStatisticsService;

    @KafkaListener(topics = {"${kafka.merge.topic}"}, groupId = "fdev-interface")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
    	byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        logger.info("kafka data:{}", params);
        // 获得kafka消息，做后续逻辑处理
        JSONObject jsonObject=JSONObject.parseObject(params);
        Map<String, Object> projectInfo = (Map<String, Object>) jsonObject.get(Dict.PROJECT);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        String projectName = (String) projectInfo.get(Dict.NAME);
        Map<String, Object> attributes = (Map<String, Object>) jsonObject.get(Dict.OBJECT_ATTRIBUTES);
        // 合并的目标分支
        String mergeBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        String state = (String) attributes.get(Dict.STATE);
        if ((Dict.SIT.equals(mergeBranch) || Dict.MASTER.equals(mergeBranch)) && Dict.MERGED.equals(state)) {
            // 合并到SIT或者master，并且合并成功，则进行接口扫描
            if (StringUtils.isEmpty(projectName) || StringUtils.isEmpty(mergeBranch)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
            }
            //fdev和玉衡接口扫描  内部使用 仅扫描master分支
            if((projectName.startsWith(Dict.FDEV_) || projectName.startsWith(Dict.TEST_MANAGE_)) && Dict.MASTER.equals(mergeBranch)){
            	if (Dict.TEST_MANAGE_UI.equals(projectName) || Dict.FDEV_VUE_ADMIN.equals(projectName)) {//前端vue应用采用扫描serviceMap.js文件方式
            		scannerService.scanInterface(projectName, mergeBranch, "09", projectId);
				}else {
            		interfaceStatisticsService.scanInterfaceStatistics(projectName);
				}
            	return;
            }
            if (scanSkipService.skipAllScanFlag(projectName)) {
                throw new FdevException(projectName+"不需要扫描接口！");
            }
            scannerService.scanInterface(projectName, mergeBranch, "09", projectId);
        }

    }


}

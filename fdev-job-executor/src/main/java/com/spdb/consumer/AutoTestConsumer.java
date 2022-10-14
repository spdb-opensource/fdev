package com.spdb.consumer;

import com.csii.pe.pojo.PipelineRecord;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.dao.PipelineRecordDao;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.time.LocalDate;
import java.util.*;

@Configuration
public class AutoTestConsumer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PipelineRecordDao pipelineRecordDao;
    @Value("${auto.test.pipeline.time}")
    private String autoTestPipelineTime;

    @KafkaListener(topics = "${kafka.topic}", groupId = "fdev-job-executor")
    public void consumer(String msg, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        JSONObject parse = JSONObject.fromObject(msg);
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        // pipeline id
        Integer pipelineId = (Integer) attributes.get(Dict.ID);
        // 触发pipeline的分支
        String pipelineBranch = (String) attributes.get(Dict.REF);
        // pipeline状态
        String status = (String) attributes.get(Dict.STATUS);
        if (!Dict.SUCCESS.equals(status)) {
            return;
        }
        logger.info("kafka topic:{},pipeline id:{}", topic, pipelineId);
        Map<String, Object> projectInfo = (Map<String, Object>) parse.get(Dict.PROJECT);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        String projectName = (String) projectInfo.get(Dict.NAME);
        // 获取前一天指定时间到现在的数据
        String startTime = LocalDate.now().minusDays(1) + " " + autoTestPipelineTime;
        PipelineRecord existPipelineRecord = pipelineRecordDao.getPipelineRecord(projectId, pipelineBranch, startTime, DateUtils.getDate(DateUtils.FORMAT_DATE_TIME));
        if (existPipelineRecord == null) {
            PipelineRecord pipelineRecord = new PipelineRecord();
            pipelineRecord.setProjectId(projectId);
            pipelineRecord.setProjectName(projectName);
            pipelineRecord.setBranch(pipelineBranch);
            Set<Integer> pipelineIdList = new HashSet<>();
            pipelineIdList.add(pipelineId);
            pipelineRecord.setPipelineIds(pipelineIdList);
            pipelineRecordDao.savePipelineRecord(pipelineRecord);
        } else {
            Set<Integer> pipelineIds = existPipelineRecord.getPipelineIds();
            pipelineIds.add(pipelineId);
            pipelineRecordDao.updatePipelineRecord(existPipelineRecord.getId(), pipelineIds);
        }
    }

}

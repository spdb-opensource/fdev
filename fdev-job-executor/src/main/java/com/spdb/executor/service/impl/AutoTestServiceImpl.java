package com.spdb.executor.service.impl;

import com.csii.pe.pojo.PipelineRecord;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.dao.PipelineRecordDao;
import com.spdb.executor.service.AutoTestService;
import com.spdb.executor.service.GitlabService;
import com.spdb.executor.service.RestTransportService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/5/18 17:00
 */
@Service
public class AutoTestServiceImpl implements AutoTestService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${auto.test.pipeline.time}")
    private String autoTestPipelineTime;
    @Value("${gitlab.api}")
    private String gitapi;
    @Autowired
    private GitlabService gitlabService;
    @Autowired
    private PipelineRecordDao pipelineRecordDao;
    @Autowired
    private RestTransportService restTransportService;

    @Override
    public void autoTest() {
        // 获取前一天指定时间到当天指定时间的数据
        String startTime = LocalDate.now().minusDays(1) + " " + autoTestPipelineTime;
        String endTime = LocalDate.now() + " " + autoTestPipelineTime;
        List<PipelineRecord> pipelineRecords = pipelineRecordDao.listPipelineRecords(startTime, endTime);
        for (PipelineRecord pipelineRecord : pipelineRecords) {
            // 调应用模块接口，获取自动测试环境
            Map<String, Object> autoTestEnv = restTransportService.getAutoTestEnv(pipelineRecord.getProjectId(), pipelineRecord.getBranch());
            if (autoTestEnv == null || autoTestEnv.size() == 0) {
                logger.info("{}获取的自动测试环境为空！", pipelineRecord.getProjectName());
                continue;
            }
            boolean autoTestSwitch = (boolean) autoTestEnv.get("autoSwitch");
            if (!autoTestSwitch) {
                logger.info("{}对接自动测试平台开关：{}", pipelineRecord.getProjectName(), autoTestSwitch);
                continue;
            }
            // 自动测试环境
            List<String> autoTestEnvName = (List<String>) autoTestEnv.get(Dict.ENV_NAME);
            for (String envName : autoTestEnvName) {
                // 调环境配置模块接口，获取环境变量
                Map<String, Object> variablesMapping = restTransportService.getVariablesMapping(pipelineRecord.getProjectId(), envName);
                if (variablesMapping == null || variablesMapping.size() == 0) {
                    logger.info("{}获取的环境变量为空！", pipelineRecord.getProjectName());
                    continue;
                }
                List<Map<String, String>> variables = getAutoTestVariables(variablesMapping, envName);
                createPipeline(variables, pipelineRecord.getBranch(), pipelineRecord.getProjectId(), pipelineRecord.getProjectName());
            }
        }
    }

    private List<Map<String, String>> getAutoTestVariables(Map<String, Object> variablesMapping, String autoTestEnvName) {
        List<Map<String, String>> autoTestVariables = new ArrayList<>();
        for (Map.Entry<String, Object> m : variablesMapping.entrySet()) {
            Map<String, String> variables = new HashMap<>();
            variables.put(Dict.KEY, m.getKey());
            variables.put(Dict.VALUE, String.valueOf(m.getValue()));
            autoTestVariables.add(variables);
        }
        Map<String, String> autoTest = new HashMap<>();
        autoTest.put(Dict.KEY, Dict.AUTO_TEST_DEPLOY);
        autoTest.put(Dict.VALUE, "true");
        autoTestVariables.add(autoTest);
        Map<String, String> envSlug = new HashMap<>();
        envSlug.put(Dict.KEY, Dict.CI_ENVIRONMENT_SLUG);
        envSlug.put(Dict.VALUE, autoTestEnvName);
        autoTestVariables.add(envSlug);
        return autoTestVariables;
    }

    private void createPipeline(List variables, String ref, Integer gitlabId, String appNameEn) {
        Map param = new HashMap();
        param.put(Dict.ID, gitlabId);
        param.put(Dict.REF, ref);
        param.put(Dict.VARIABLES, variables);
        StringBuilder url = new StringBuilder(gitapi);
        url.append("/projects/").append(gitlabId).append("/pipeline");
        JSONObject paramJson = JSONObject.fromObject(param);
        try {
            Object result = gitlabService.post(url.toString(), paramJson);
            JSONObject jsonObject = JSONObject.fromObject(result);
            logger.info("创建pipeline成功 id:{} appNameEn:{} gitlabId:{} ref:{} status:{}", jsonObject.get(Dict.ID), appNameEn, gitlabId, jsonObject.get(Dict.REF), jsonObject.get(Dict.STATUS));
        } catch (Exception e) {
            logger.error("{}自动化测试环境pipeline创建失败! msg : {}", appNameEn, e.getMessage());
        }
    }

}

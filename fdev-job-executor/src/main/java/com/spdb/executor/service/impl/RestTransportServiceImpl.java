package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.RestTransportService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestTransportServiceImpl implements RestTransportService {

    private Logger logger = LoggerFactory.getLogger(RestTransportServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    @Value("${testPipelineUrl}")
    private String testPipelineUrl;
    
    @Value("${bk_app_code}")
    private String bk_app_code;
    
    @Value("${bk_app_secret}")
    private String bk_app_secret;
    
    @Value("${bk_username}")
    private String bk_username;
    
    @Value("${userId}")
    private String userId;
    
    @Value("${projectCode}")
    private String projectCode;
    
    @Override
    public Map<String, Object> getAutoTestEnv(Integer gitlabProjectId, String branch) {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.GITLAB_PROJECT_ID, gitlabProjectId);
        map.put(Dict.BRANCH, branch);
        map.put(Dict.REST_CODE, "autoTestEnv");
        Map<String, Object> autoTestMap = new HashMap<>();
        try {
            autoTestMap = (Map<String, Object>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("调应用模块接口，获取自动测试环境失败：{}", e.getMessage());
        }
        return autoTestMap;
    }

    @Override
    public Map<String, Object> getVariablesMapping(Integer gitlabProjectId, String env) {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.GITLABID, gitlabProjectId);
        map.put(Dict.ENV, env);
        map.put(Dict.REST_CODE, "queryVariablesMapping");
        Map<String, Object> variablesMap = new HashMap<>();
        try {
            variablesMap = (Map<String, Object>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("调环境配置模块接口，获取环境变量失败：{}", e.getMessage());
        }
        return variablesMap;
    }

    @Override
    public List querySitMergeInfo(String app) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        SimpleDateFormat sf = new SimpleDateFormat(CommonUtils.INPUT_DATE);

        Map<String, Object> map = new HashMap<>();
        map.put("appName", app);
        map.put("mergeDate", sf.format(calendar.getTime()));
        map.put(Dict.REST_CODE, "querySitMerged");
        List result = new ArrayList();
        try {
            result = (List) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("调环境配置模块接口，获取环境变量失败：{}", e.getMessage());
        }
        return result;
    }

    @Override
    public Object createTestPipeline(String application,String pipelineId, String mergeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        Map<String, Object> values = new HashMap<>();
        values.put("application", application);
        values.put("version", mergeId);
        Map<String, Object> map = new HashMap<>();
        map.put("bk_app_code", bk_app_code);
        map.put("bk_app_secret", bk_app_secret);
        map.put("bk_username", bk_username);
        map.put("userId", userId);
        map.put("values", values);
        map.put("pipelineId", pipelineId);
        String[] split = projectCode.split(";");
        if (application.equals("msper-web-webaccount-parent")) {
        	map.put("projectCode", split[1]);
		}else if (application.equals("nbh-host-unionpay")) {
			map.put("projectCode", split[2]);
		}else if (application.equals("spdb-icard-server")) {
			map.put("projectCode", split[3]);
		}else if (application.equals("esfe-product-efxonline")) {
			map.put("projectCode", split[4]);
		}else if (application.equals("msemk-web-biospherebank")) {
			map.put("projectCode", split[5]);
		}else {
			map.put("projectCode", split[0]);
		}
        HttpEntity<Map<String, String>> request = new HttpEntity(map, headers);
        ResponseEntity<Map> responseEntity = null;
        logger.info("begin create testpipeline , body:{}",map.keySet().stream().map(k -> k + "=" + map.get(k)).collect(Collectors.joining(",", "{", "}")));
        try {
            responseEntity = restTemplate.exchange(testPipelineUrl, HttpMethod.POST, request, Map.class);
        } catch (Exception e) {
            logger.error("调用测试中心流水线失败：{}", e.getMessage());
            return null;
        }
        Map result = responseEntity.getBody();
        logger.info("url:{} status:{} result:{}", testPipelineUrl, responseEntity.getStatusCode(), result.keySet().stream().map(k -> k + "=" + result.get(k)).collect(Collectors.joining(",", "{", "}")));
        return result;
    }


}

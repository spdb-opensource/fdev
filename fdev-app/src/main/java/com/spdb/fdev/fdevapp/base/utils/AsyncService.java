package com.spdb.fdev.fdevapp.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabVariablesService;
import com.spdb.fdev.fdevapp.spdb.service.IKafkaService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("asyncService")
public class AsyncService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/
    @Value("${gitlab.token}")
    private String token;
    @Value("${interface.api}")
    private String interfaceApi;
    @Value("${delete.app.topic}")
    private String deleteMessageTopic;          //delete-app-sit

    @Autowired
    private GitlabTransport gitlabTransport;
    @Autowired
    private IAppEntityDao appEntityDao;
    @Autowired
    private IGitlabVariablesService gitlabVariablesService;
    @Autowired
    private IGitlabAPIService gitlabAPIService;
    @Autowired
    private IKafkaService kafkaService;
    @Autowired
    RestTransport restTransport;

    @Async
    public void autoScan(AppEntity appEntity) throws Exception {
        String type = Dict.NOT_WEBT_TYPE;
        String project_name = appEntity.getName_en();
        if (project_name.contains(Dict.WEB)) {
            type = Dict.WEB_TYPE;
        }
        Map param = new HashMap();
        param.put(Dict.NAME_EN, project_name);
        param.put(Dict.BRANCH, Dict.MASTER);
        param.put(Dict.TYPE, type);
        param.put(Dict.AUTOSCAN_PROJECT_ID, appEntity.getGitlab_project_id());
        param.put(Dict.REST_CODE, Dict.AUTOSCAN);
        //扫描master分支
        restTransport.submit(param);
    }

    @Async
    public void scheduleJob(AppEntity appEntity) throws Exception {
        String id = appEntity.getGitlab_project_id().toString();
        //获取前一天时间
        String theDayBefore = TimeUtils.getTheDayBefore();
        //查询应用前一天SIT分支是否有提交记录
        String mergeRequest_url = CommonUtils.projectUrl(url) + "/" + id + "/merge_requests?state=merged&target_branch=SIT&updated_after=" + theDayBefore;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        //查询merge_request信息
        Object submitGet = gitlabTransport.submitGet(mergeRequest_url, header);
        List list = JSONArray.parseArray((String) submitGet);
        if (!CommonUtils.isNullOrEmpty(list)) {
            // 创建pipeline
            String pipeline_url = CommonUtils.projectUrl(url) + "/" + id + "/pipeline";
            // 配置 variables参数 将其重新组装为map再加入到list中
            Map<String, String> variable_param = new HashMap<>();
            variable_param.put(Dict.KEY, Dict.CI_SCHEDULE);
            variable_param.put(Dict.VALUE, Dict.TRUE);
            List<Map<String, String>> variables = new ArrayList<>();
            variables.add(variable_param);
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.ID, id);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            param.put(Dict.REF, Dict.SIT_UP);
            param.put(Dict.VARIABLES, variables);
            gitlabTransport.submitPost(pipeline_url, param);
            logger.info("run pipeline success,appName:" + appEntity.getName_en());
        }
    }

    /**
     * 删除应用时删除接口相关数据
     *
     * @param appNameEn
     * @throws Exception
     */
    @Async
    public void deleteDataforApp(String appNameEn) throws Exception {
        Map param = new HashMap();
        param.put(Dict.SERVICEID, appNameEn);
        param.put(Dict.REST_CODE, Dict.DELETEDATAFORAPP);
        restTransport.submit(param);
    }


    /**
     * 当删除/废弃应用的时候，异步发送kafka废弃消息
     *
     * @param param
     */
    @Async
    public void sendKafkaMessage(Map<String, Object> param) {
        //为了给接收方进行map的转换而做的处理
        String sendData = JSON.toJSONString(param);
        //发送kafka消息
        this.kafkaService.sendData((String) param.get(Dict.USER_NAME_EN), (String) param.get(Dict.NAME_EN), sendData, deleteMessageTopic);
    }

}

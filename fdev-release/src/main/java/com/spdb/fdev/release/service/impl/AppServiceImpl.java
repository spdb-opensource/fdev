package com.spdb.fdev.release.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.release.dao.IReleaseBatchDao;
import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.transport.GitlabTransport;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RefreshScope
public class AppServiceImpl implements IAppService {
    @Autowired
    private RestTransport restTransport;
    @Value("${gitlab.manager.token}")
    private String token;
    @Value("${gitlab.url}")
    private String gitlab_url;

    @Autowired
    private GitlabTransport gitlabTransport;
    @Autowired
    private IReleaseBatchDao iReleaseBatchDao;

    @Override
    public void createReleaseBranch(String id, String name, String ref, String token) throws Exception {
        // 发APP模块创建分支
        Map<String, String> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.NAME, name);
        map.put(Dict.REF, ref);
        map.put(Dict.TOKEN, token);// 发送app模块 创建新的release分支
        map.put(Dict.REST_CODE, "createBranch");
        restTransport.submit(map);
    }

    @Override
    public List<Map<String, Object>> getBranchList(String id, String token) throws Exception {
        // 发app模块获取项目分支列表
        Map<String, String> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.TOKEN, token);// 发app模块获取项目分支列表
        map.put(Dict.REST_CODE, "getProjectBranchList");
        List list = (List) restTransport.submit(map);
        return list;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "frelease.appinfo.{id}")
    public Map<String, Object> queryAPPbyid(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);// 发app模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "findbyId");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public String getMergeRequestInfo(String token, String id, String iid) throws Exception {
        Map<String, String> mapMergeReq = new HashMap<>();
        mapMergeReq.put(Dict.TOKEN, token);
        mapMergeReq.put(Dict.ID, id);
        mapMergeReq.put(Dict.IID, iid);// 发app模块获取MR的详细信息
        mapMergeReq.put(Dict.REST_CODE, "getMergeRequestInfo");
        Map submit = (Map) restTransport.submit(mapMergeReq);
        return (String) submit.get(Dict.STAGE);
    }

    public String commitMergeRequest(String application_id, String token, String source_branch, String desc) throws Exception {
        //发app模块创建新分支
        Map<String, String> mapMergeReq = new HashMap<>();
        mapMergeReq.put(Dict.ID, application_id);
        mapMergeReq.put(Dict.TARGET_BRANCH, Dict.MASTER);
        mapMergeReq.put(Dict.SOURCE_BRANCH, source_branch);
        mapMergeReq.put(Dict.TOKEN, token);
        mapMergeReq.put(Dict.DESCRIPTION, desc);
        mapMergeReq.put(Dict.TITLE, desc+"-提交准生产测试");
        mapMergeReq.put(Dict.REST_CODE, "createMergeRequest");
        Map result = (Map) restTransport.submit(mapMergeReq);
        return String.valueOf(result.get(Dict.IID));
    }

    @Override
    public Map createTags(String id, String name, String ref, String token) throws Exception {
        // 发app模块创建新tag
        Map<String, String> mapMergeReq = new HashMap<>();
        mapMergeReq.put(Dict.ID, id);
        mapMergeReq.put(Dict.NAME, name);
        mapMergeReq.put(Dict.REF, ref);
        mapMergeReq.put(Dict.TOKEN, token);
        mapMergeReq.put(Dict.REST_CODE, "createTags");
        return (Map) restTransport.submit(mapMergeReq);
    }

    @Override
    public List<Map> getProjectTagsList(String id, String token) throws Exception {
        // 发app模块获取项目tag列表
        Map<String, String> mapMergeReq = new HashMap<>();
        mapMergeReq.put(Dict.ID, id);
        mapMergeReq.put(Dict.TOKEN, token);
        mapMergeReq.put(Dict.ORDER_BY, Dict.NAME);
        mapMergeReq.put(Dict.REST_CODE, "getProjectTagsList");
        return (List<Map>) restTransport.submit(mapMergeReq);
    }

    @Override
    public void createProjectPipeline(String id, String ref) throws Exception {
        // 发app模块创建新pipeline
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.REF, ref);
        map.put(Dict.REST_CODE, "createPipeline");
        restTransport.submit(map);
    }

    @Override
    public String createProject(String path, String name) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.PATH, path);
        map.put(Dict.PROJECT_NAME, name);
        map.put(Dict.TOKEN, token);
        map.put(Dict.REST_CODE, "createProject");
        Map parseObject = (Map) restTransport.submit(map);
        return ((Integer) parseObject.get(Dict.ID)).toString();
    }

    public String queryFileCommitId(String url) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam(Dict.REF, Dict.MASTER);
        URI uri = builder.build(true).toUri();
        Object submitGet = gitlabTransport.submitGet(uri, header);
        JSONObject parseObject = JSONObject.parseObject((String) submitGet);
        Map javaObject = parseObject.toJavaObject(Map.class);
        return (String) javaObject.get(Dict.LAST_COMMIT_ID);
    }

    @Override
    public void updateApplication(Map map) throws Exception {
        map.put(Dict.REST_CODE, "updateForEnv");
        restTransport.submit(map);
    }

    @Override
    public String queryByLabelsFuzzy(String type, String network) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<String> twoParam = new ArrayList();
        twoParam.add(network);
        twoParam.add(type);
        twoParam.add("default");
        map.put(Dict.LABELS, twoParam);
        map.put(Dict.REST_CODE, "queryByLabelsFuzzy");
        List result = (List) restTransport.submit(map);
        if (!CommonUtils.isNullOrEmpty(result)) {
            Map env_map = (Map) result.get(0);
            return (String) env_map.get(Dict.NAME_EN);
        }
        return  "";
    }

    @Override
    public Map<String, Object> queryVariablesMapping(Integer gitlabId, String env) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.GITLABID, gitlabId);
        map.put(Dict.ENV, env);
        map.put(Dict.REST_CODE, "queryVariablesMapping");
        Map<String, Object> result = (Map<String, Object>) restTransport.submit(map);
        return result;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "frelease.detail.{app_name_en}")
    public Map<String, Object> queryAPPbyNameEn(String app_name_en) throws Exception {
        Map<String, Object> send_map = new HashMap<String, Object>();
        send_map.put(Dict.NAME_EN, app_name_en);// 发app模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "search");
        Object submit = restTransport.submit(send_map);
        if (CommonUtils.isNullOrEmpty(submit)) {
            return null;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) submit;
        Map<String, Object> map = list.get(0);
        return map;
    }

    public String getGitFileContentById(String git_id, String filename, String ref) throws Exception {
        String url = gitlab_url + "api/v4/projects/" + git_id + "/repository/files/" + filename + "?ref=" + ref;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        JSONObject parseObject = JSONObject.parseObject((String) submitGet);
        Map map = parseObject.toJavaObject(Map.class);
        return (String) map.get(Dict.CONTENT);
    }

    public String getGitFileContent(String git_id, String filename, String ref) throws Exception {
        String url = gitlab_url + "api/v4/projects/" + git_id + "/repository/files/" + filename + "/raw?ref=" + ref;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        return (String) submitGet;
    }

    public void updateGitFile(String git_id, String filename, String ref, String content, String commit_message, String token) throws Exception {
        String url = gitlab_url + "api/v4/projects/" + git_id + "/repository/files/" + filename;
        Map<String, String> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN, token);
        param.put(Dict.BRANCH, ref);
        param.put(Dict.CONTENT, content);
        param.put(Dict.COMMIT_MESSAGE, commit_message);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        this.gitlabTransport.submitPut(uri, token, param);
    }

    /**
     * 根据应用ids查应用信息集合
     * @param appIds
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getAppByIdsOrGitlabIds(List<String> appIds, String releaseNodeName) throws Exception {
        Map send = new HashMap();
        send.put(Dict.IDS, appIds);
        send.put(Dict.TYPE, "id");
        send.put(Dict.REST_CODE, "getAppByIdsOrGitlabIds");
        List<Object> result = (List<Object>)restTransport.submit(send);
        List<Map<String, String>> apps = new ArrayList<>();
        Map<String, String> app;
        for(Object o : result){
            app = new HashMap<>();
            String batchId = "";
            String appId = String.valueOf(((Map<String, Object>)o).get(Dict.ID));
            app.put(Dict.APPLICATION_ID, appId);
            app.put(Dict.APPLICATION_NAME_EN, String.valueOf(((Map<String, Object>)o).get(Dict.NAME_EN)));
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
            if(!CommonUtils.isNullOrEmpty(releaseBatchRecord)){
                batchId = releaseBatchRecord.getBatch_id();
            }
            app.put(Dict.BATCH_ID, batchId);
            apps.add(app);
        }
        return apps;
    }
    @Override
    public List<Map<String, Object>> getNewYaml(String deployName, String tag) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.DEPLOY_NAME, deployName);
        map.put(Dict.TAG, tag);
        map.put(Dict.REST_CODE, "getCaasNewYaml");
        List<Map<String,Object>> result = (List<Map<String, Object>>) restTransport.submit(map);
        return result;
    }

    @Override
    public List<Map<String, Object>> getSccNewYaml(String deployName, String tag) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.DEPLOY_NAME, deployName);
        map.put(Dict.TAG, tag);
        map.put(Dict.REST_CODE, "getSccNewYaml");
        List<Map<String,Object>> result = (List<Map<String, Object>>) restTransport.submit(map);
        return result;
    }

    @Override
    public Map<String, Object> getEnvValues(String gitlabId,String applicationId, String envEnName, List<String> keys) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(gitlabId)){
            send_map.put(Dict.GITLABID, gitlabId); // gitlabId
        }else{
            send_map.put("appId", applicationId); // applicationId
        }
        send_map.put("envName", envEnName); // 环境英文名
        send_map.put("keyList", keys);
        send_map.put(Dict.REST_CODE, "querySpecifiedVariablesMapping");
        return  (Map<String, Object>) restTransport.submit(send_map);
    }


    @Override
    public List<Map<String, Object>> getApps() throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, "queryApps");
        return (List<Map<String, Object>>) restTransport.submit(send_map);
    }


}

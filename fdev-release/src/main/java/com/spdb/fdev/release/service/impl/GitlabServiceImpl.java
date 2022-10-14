
package com.spdb.fdev.release.service.impl;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csii.pe.redis.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IGitlabService;
import com.spdb.fdev.release.transport.GitlabTransport;

@Service
@RefreshScope
public class GitlabServiceImpl implements IGitlabService {

    private static Logger logger = LoggerFactory.getLogger(GitlabServiceImpl.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String GROUP_SEARCH = "groups?search=";

    @Autowired
    GitlabTransport gitlabTransport;
    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;
    @Autowired
    IAppService appService;
    @Value("${gitlab.manager.token}")
    private String gitlabManagerToken;
    @Value("${gitlab.rootUrl}")
    private String gitlabRootUrl;

    public String projectUrl() {
        return gitlabApiUrl + Dict.PROJECTS;
    }


    @Override
    public List getProjectTagsList(String id, String releaseNodeName, String token) throws Exception {
        Map<String, Object> app = appService.queryAPPbyid(id);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        // 发app模块获取项目tag列表
        StringBuilder sb = new StringBuilder();
        String mergereq_url = sb.append(gitlabApiUrl).append("projects/").append(app.get(Dict.GITLAB_PROJECT_ID)).append("/repository/tags?order_by=name&search=").append(releaseNodeName).toString();
        String tagList = (String) gitlabTransport.submitGet(mergereq_url, header);
        return objectMapper.readValue(tagList, List.class);
    }

    public boolean checkTagExisted(String gitlabId, String tag) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        String url = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId)
                .append("/repository/tags?order_by=name&search=").append(tag).toString();
        String tagList = (String) gitlabTransport.submitGet(url, header);
        List<Map<String, Object>> list = objectMapper.readValue(tagList, List.class);
        boolean flag = false;
        for(Map<String, Object> map : list) {
            if(tag.equals(map.get(Dict.NAME))) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 检查组路径是否存在
     *
     * @param groupFullPath 组全路径
     */
    @Override
    public boolean checkGroupExist(String groupFullPath) throws FdevException {
        boolean isExist = false;
        String newGroupPath = groupFullPath.trim();
        // 获取group地址
        String[] split = newGroupPath.split("/");
        // 拼装地址
        String searchGroup_url = gitlabApiUrl + GROUP_SEARCH + split[split.length - 1];
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            String group = (String) gitlabTransport.submitGet(searchGroup_url, header);
            JSONArray jsonarray = JSON.parseArray(group);
            for (Object object : jsonarray) {
                Map map = (Map) object;
                if (newGroupPath.equals(map.get(Dict.FULL_PATH))) {
                    isExist = true;
                    logger.info("gitlab组地址存在!");
                    break;
                } else {
                    isExist = false;
                    continue;
                }
            }
        } catch (HttpClientErrorException e) {
            logger.error(Dict.STATUS, ((HttpClientErrorException) e).getResponseBodyAsString());
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return isExist;
    }

    public Map queryGroupInfo(String groupFullPath) throws FdevException {
        boolean isExist = false;
        String newGroupPath = groupFullPath.trim();
        // 获取group地址
        String[] split = newGroupPath.split("/");
        // 拼装地址
        String searchGroup_url = gitlabApiUrl + GROUP_SEARCH + split[split.length - 1];
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        Map groupInfo = new HashMap();
        try {
            String group = (String) gitlabTransport.submitGet(searchGroup_url, header);
            JSONArray jsonarray = JSON.parseArray(group);
            for (Object object : jsonarray) {
                Map map = (Map) object;
                //FULL_PATH不包括http://xxx/
                if (newGroupPath.equals(map.get(Dict.FULL_PATH))) {
                    isExist = true;
                    groupInfo = map;
                    logger.info("gitlab组地址存在!");
                    break;
                } else {
                    isExist = false;
                    continue;
                }
            }

        } catch (HttpClientErrorException e) {
            logger.error(Dict.STATUS, ((HttpClientErrorException) e).getResponseBodyAsString());
        } catch (Exception e) {
            logger.error(e.toString());
        }
        if (!isExist) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"组信息不存在"});
        }
        return groupInfo;
    }

    /**
     * 根据组id和项目名称判断项目是否存在
     *
     * @param namespaceId 组id
     * @param name        项目名称
     * @return
     * @throws Exception
     */
    public boolean isProjectExist(Integer namespaceId, String name) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        String searce_pro_url = projectUrl() + "?search=" + name + "&per_page=100";// 获取所以该名称的项目信息
        Object projects = gitlabTransport.submitGet(searce_pro_url, header);
        if (CommonUtils.isNullOrEmpty(projects)) {// 若未查询到 则说明不存在
            return false;
        }
        JSONArray project_list = JSONObject.parseArray((String) projects);
        boolean flag = false;
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get(Dict.NAMESPACE);
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            if (namespaceId.equals(id) && name.equalsIgnoreCase(project_name)) {// 判断groupid是否相同
                // 判断name是否相同
                flag = true;
                break;
            }
            Map forkedMap = (Map) map.get("forked_from_project");
            if(!Util.isNullOrEmpty(forkedMap)) {
            	Map forkedNamespace = (Map) forkedMap.get(Dict.NAMESPACE);
            	String forkedProjectName = (String) forkedMap.get(Dict.NAME);
            	Integer forkedId = (Integer) forkedNamespace.get(Dict.ID);
                if (namespaceId.equals(forkedId) && name.equalsIgnoreCase(forkedProjectName)) {// 判断groupid是否相同
                     // 判断name是否相同
                     flag = true;
                     break;
                 }
            }
            
        }
        return flag;
    }

    /**
     * 根据项目名查询项目id
     *
     * @param namespaceId
     * @param name
     * @return
     * @throws Exception
     */
    public String returnProjectId(Integer namespaceId, String name) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        String searce_pro_url = projectUrl() + "?search=" + name + "&per_page=100";// 获取所有该名称的项目信息
        Object projects = gitlabTransport.submitGet(searce_pro_url, header);
        if (CommonUtils.isNullOrEmpty(projects)) {// 若未查询到 则说明不存在
            throw new FdevException(ErrorConstants.RESOURCE_IS_NOT_EXIST);
        }
        JSONArray project_list = JSONObject.parseArray((String) projects);

        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get(Dict.NAMESPACE);
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            if (namespaceId.equals(id) && name.equalsIgnoreCase(project_name)) {// 判断groupid是否相同
                // 判断name是否相同
                return ((Integer) map.get(Dict.ID)).toString();
            }
            Map forkedMap = (Map) map.get("forked_from_project");
            if(!Util.isNullOrEmpty(forkedMap)) {
            	Map forkedNamespace = (Map) forkedMap.get(Dict.NAMESPACE);
            	String forkedProjectName = (String) forkedMap.get(Dict.NAME);
            	Integer forkedId = (Integer) forkedNamespace.get(Dict.ID);
                if (namespaceId.equals(forkedId) && name.equalsIgnoreCase(forkedProjectName)) {// 判断groupid是否相同
                	 // 判断name是否相同
                    return ((Integer) map.get(Dict.ID)).toString();
                 }
            }
        }
        return null;
    }

    public String queryProjectIdByUrl(String project_url) throws Exception {
        String tmp = project_url.replace(gitlabRootUrl, "");
        if (tmp.endsWith("/")) {
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        String projectName = tmp.substring(tmp.lastIndexOf('/') + 1);
        String path = tmp.substring(0, tmp.lastIndexOf('/'));
        
        Map map1 = queryGroupInfo(path);
        Integer namespaceid = (Integer) map1.get(Dict.ID);
        if (CommonUtils.isNullOrEmpty(namespaceid)) {
            logger.error("can't find this path! @@@@ path={}", path);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"can't find this path"});
        }
        boolean projectExist = isProjectExist(namespaceid, projectName);
        if (!projectExist) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"项目不存在"});
        }
        return returnProjectId(namespaceid, projectName);
    }


//    @LazyInitProperty(redisKeyExpression = "frelease.resource.branches.{projectId}")
    public List queryBranches(String projectId) throws Exception {
        ArrayList result = new ArrayList();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        Object projects = gitlabTransport.submitGet(gitlabApiUrl + "projects/" + projectId + "/repository/branches?per_page=100",
                header);
        if (CommonUtils.isNullOrEmpty(projects)) {// 若未查询到 则说明不存在
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"不存在"});
        }
        JSONArray project_list = JSONObject.parseArray((String) projects);
        for (Object project : project_list) {
            Map map = (Map) project;
            result.add(map.get(Dict.NAME));
        }
        return result;
    }

    public void countAllResourceFiles(String projectId, String branch, List<Map> list, HashSet<String> resultSet) throws Exception {
        for (Map map : list) {
            switch ((String) map.get(Dict.TYPE)) {
                case "tree":
                    List<Map> result = getResourceFile(projectId, (String) map.get(Dict.PATH), branch);
                    countAllResourceFiles(projectId, branch, result, resultSet);
                    break;
                case "blob":
                    resultSet.add((String) map.get(Dict.PATH));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "frelease.resource.files.{projectId}.{branch}")
    public Set<String> queryResourceFiles(String projectId, String branch) throws Exception {
        HashSet<String> resultSet = new HashSet();
        List<Map> list = getResourceFile(projectId, "", branch);
        countAllResourceFiles(projectId, branch, list, resultSet);
        return resultSet;
    }

    public List<Map> getResourceFile(String projectId, String path, String branch) throws Exception {
        String fileUrl = gitlabApiUrl + "projects/" + projectId + "/repository/tree?per_page=100"
                + (CommonUtils.isNullOrEmpty(path) ? "" : ("&path=" + path)) + "&ref=" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        Object projects = gitlabTransport.submitGet(fileUrl, header);
        if (CommonUtils.isNullOrEmpty(projects)) {// 若未查询到 则说明不存在
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"目录不存在"});
        }
        return objectMapper.readValue((String) projects, List.class);
    }

    @Override
    public boolean isFileExist(String filepath, String projectId, String branch) throws Exception {
        String encodeFileUrl = URLEncoder.encode(filepath, "UTF-8").replace(".", "%2E");
        String searchFile_url = (gitlabApiUrl + "projects/" + projectId + "/repository/files/" + encodeFileUrl + "?ref=" + branch);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            gitlabTransport.submitGet(searchFile_url, header);
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                logger.error(Dict.STATUS, ((HttpClientErrorException) e).getResponseBodyAsString());
            } else {
                logger.error(searchFile_url, e);
            }
            return false;
        }
        return true;
    }


    /**
     * 创建 一个组
     *
     * @param group 组名字 /ebank/devops /ebank/devops/
     * @return
     */
    @Override
    public Map createGroup(String group) throws Exception {
        // 创建组
        if (group.endsWith("/")) {
            group = group.substring(0, group.length() - 1);
        }
        if (group.startsWith("/")) {
            //   ebank/devops/testGroup/vs
            group = group.substring(1);
        }
        String[] split = group.split("/");
        // 查询 父group 是否存在
        String parentGroup = split[split.length - 2];
        String fullPathParentGroup = group.substring(0, group.length() - split[split.length - 1].length() - 1);

        Map parentGroupMap = getGroup(parentGroup, fullPathParentGroup, gitlabManagerToken);
        if (CommonUtils.isNullOrEmpty(parentGroupMap)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{parentGroup + " 不存在"});
        }
        Integer parent_id = (Integer) parentGroupMap.get(Dict.ID);
        String url = this.gitlabApiUrl + Dict.GROUPS;
        Map<String, Object> param = new HashMap<>();
        String createGroup = split[split.length - 1];
        param.put(Dict.PRIVATE_TOKEN_L, gitlabManagerToken);
        param.put(Dict.NAME, createGroup);
        param.put(Dict.PATH, createGroup);
        param.put(Dict.PARENT_ID, parent_id);
        Object object;
        try {
            object = this.gitlabTransport.submitPost(url, param);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("####### 组的完整信息 [{}] , 要添加 [{}] 组存在", group, createGroup);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{createGroup + " 已经存在"});
        }
        Map parse = (Map) JSONObject.parse((String) object);
        if (CommonUtils.isNullOrEmpty(parse.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"创建" + parentGroup + " 组失败"});
        }
        return parse;
    }

    public Map getGroup(String group, String fullPath, String gitlab_token) throws Exception {
        String searchGroup_url = this.gitlabApiUrl + GROUP_SEARCH + group;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object submitGet = gitlabTransport.submitGet(searchGroup_url, header);
        JSONArray grouplist = JSONObject.parseArray((String) submitGet);
        if (CommonUtils.isNullOrEmpty(grouplist)) {
            return null;
        }
        for (Object groupl : grouplist) {
            Map map = (Map) groupl;
            if (map.get(Dict.FULL_PATH).equals(fullPath)) {
                return map;
            }
        }
        return null;
    }

    @Override
    @RemoveCachedProperty(redisKeyExpression = "frelease.resource.branches.{projectId}")
    public void cleanCacheBranches(String projectId) throws Exception {

    }

    @Override
    @RemoveCachedProperty(redisKeyExpression = "frelease.resource.files.{projectId}.{branch}")
    public void cleanCacheFiles(String projectId, String branch) throws Exception {

    }

    @Override
    public void updateProjectName(String projectId , String newName) throws Exception {
        StringBuilder sb =new StringBuilder();
        String updateProjectName_url = sb.append(gitlabApiUrl).append("projects/").append(projectId).append("?name=").append(newName).append("&path=").append(newName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            gitlabTransport.submitPut(updateProjectName_url, header);
        } catch (HttpClientErrorException e) {
            logger.error("更新id为{}的项目名失败", projectId);
        }
    }

    @Override
    public void updateGroup(String groupId, String newValue) throws Exception {
        String updateGroup_url = this.gitlabApiUrl + "groups/" + groupId + "?name=" + newValue + "&path=" + newValue;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            gitlabTransport.submitPut(updateGroup_url, header);
        } catch (HttpClientErrorException e) {
            logger.error("更新id为{}的小组名及路径失败", groupId);
        }
    }

    @Override
    public void createBranch(Integer gitlabId, String branchName, String ref) throws Exception {
        if(!branchExists(gitlabId, ref)) {
            throw new FdevException(ErrorConstants.BRANCH_NOT_EXIST, new String[]{"",ref});
        }
        if(branchExists(gitlabId, branchName)) {
            throw new FdevException(ErrorConstants.NEED_DELETE_SAME_BRANCH, new String[]{branchName});
        }
        String createBranch_url = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId).append("/repository/branches?branch=").append(branchName).append("&ref=").append(ref).toString();
        Map<String, String> header = new HashMap<>();
        header.put(Dict.PRIVATE_TOKEN_L, gitlabManagerToken);
        try {
            gitlabTransport.submitPost(createBranch_url, header);
        } catch (HttpClientErrorException e) {
            throw new FdevException(ErrorConstants.CREATE_BRANCH_FAILED,new String[]{branchName});
        }
    }

    @Override
    public void setProtectedBranches(Integer id, String branchName) {
        String protected_branchesUrl = projectUrl() + "/" + id + "/protected_branches/";
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, String.valueOf(id));
        param.put(Dict.NAME, branchName);
        param.put(Dict.PRIVATE_TOKEN_L, gitlabManagerToken);
        try {
            this.gitlabTransport.submitPost(protected_branchesUrl, param);
        } catch (Exception e) {
            logger.error("gitlabId:{}设置受保护的分支失败,分支名:{}", id, branchName);
        }
    }

    private boolean branchExists(Integer gitlabId, String branchName) {
        String url = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId).append("/repository/branches/")
                .append(branchName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        boolean flag = false;
        try {
            gitlabTransport.submitGet(url, header);
            flag = true;
        } catch (Exception e) {
            logger.error("gitlabId:{}对应{}分支不存在", gitlabId, branchName);
        }
        return flag;
    }

    @Override
    public Map<String, Object> compareBranches(String configGitlabId, String old_tag, String new_tag) {
        Map<String, Object> map = new HashMap<>();
        String compareBranches_url = new StringBuilder(gitlabApiUrl).append("projects/").append(configGitlabId).append("/repository/compare?from=").append(old_tag).append("&to=").append(new_tag).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            Object submitGet = gitlabTransport.submitGet(compareBranches_url, header);
            JSONObject jsonObject = JSONObject.parseObject((String) submitGet);
            map.put("commit", jsonObject.get("commit"));
            map.put("commits", jsonObject.get("commits"));
            map.put("diffs", jsonObject.get("diffs"));
            map.put("compare_timeout", jsonObject.get("compare_timeout"));
            map.put("compare_same_ref", jsonObject.get("compare_same_ref"));
            map.put(Dict.COMPAREBRANCHES_URL, "/compare/" + old_tag + "..." + new_tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.BRANCH_NOT_EXIST,new String[]{configGitlabId, old_tag});
        }
        return map;
    }

    @Override
    public String findWebUrlByGitlabId(String configGitlabId) {
        String weburl;
        String get_url = new StringBuilder(gitlabApiUrl).append("projects/").append(configGitlabId).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            Object submitGet = gitlabTransport.submitGet(get_url, header);
            JSONObject jsonObject = JSONObject.parseObject((String) submitGet);
            weburl = (String) jsonObject.get("web_url");

        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"gitlab" + configGitlabId + "不存在"});
        }
        return weburl;
    }

    @Override
    public void deleteBranch(Integer gitlabId, String branchName) throws Exception {
        String deleteBranch_url = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId).append("/repository/branches/").append(branchName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            gitlabTransport.submitDelete(deleteBranch_url, header);
        } catch (HttpClientErrorException e) {
            throw new FdevException(ErrorConstants.DELETE_BRANCH_FAILED,new String[]{branchName});
        }
    }

    @Override
    public List queryPiplineWithJobs(String project_id, String ref, String pages) throws Exception {
        List<Map> newResult = new ArrayList();
        try {
            String url = this.gitlabApiUrl + "projects/" + project_id + "/pipelines";
            if (!CommonUtils.isNullOrEmpty(ref))
                url = url + "?ref=" + ref;

            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
            Object submitGet = gitlabTransport.submitGet(url, header);
            JSONArray jsonA = JSONArray.parseArray((String) submitGet);
            int num = 0;
            if (CommonUtils.isNullOrEmpty(pages)) {
                num = jsonA.size();
            } else {
                num = Math.min(Integer.parseInt(pages), jsonA.size());
            }
            for (int i = 0; i < num; i++) {
                Map pipeline = (Map) jsonA.get(i);
                Map tempParam = new HashMap();
                tempParam.put(Dict.PROJECT_ID, project_id);
                tempParam.put("gitlab_token", gitlabManagerToken);
                tempParam.put("pipeline_id", String.valueOf(pipeline.get(Dict.ID)));
                Map jobResult = (Map) this.querySimpleJobs(tempParam);
                Object pipelineTemp = this.querySinglePipeline(tempParam);
                Map pipelineMap = new ObjectMapper().convertValue(pipelineTemp, Map.class);
                pipelineMap.put("jobs", jobResult.get("jobs"));
                pipelineMap.put("commit", jobResult.get("commit"));
                newResult.add(pipelineMap);
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab 查询异常", e.toString()});
        }
        return newResult;
    }

    /**
     * *   查询gitlab项目pipeine下简单数据的jobs，自定义封装
     *
     * @param param project_id
     * @return pipeline数组
     * @throws Exception
     */
    public Object querySimpleJobs(Map param) throws Exception {
        JSONArray jsonA = (JSONArray) this.queryJobs(param);
        List<Map> resultList = new ArrayList<>();
        Map commit = new HashMap();
        for (Object job : jsonA) {
            Map jsonObject = (Map) job;
            Map map = new LinkedHashMap();
            map.put("stage", jsonObject.get("stage"));
            map.put("name", jsonObject.get("name"));
            map.put("status", jsonObject.get("status"));
            map.put("id", jsonObject.get("id"));
            map.put("duration", jsonObject.get("duration"));
            map.put("finished_at", jsonObject.get("finished_at"));
            resultList.add(map);
            if (CommonUtils.isNullOrEmpty(commit)) {
                commit = (Map) jsonObject.get("commit");
            }
        }
        Map result = new HashMap();
        result.put("commit", commit);
        result.put("jobs", resultList);
        return result;
    }

    /**
     * *   查询gitlab按项目pipeine，自定义封装
     *
     * @param param project_id
     * @return pipeline数组
     * @throws Exception
     */
    public Object querySinglePipeline(Map param) throws Exception {
        String project_id = (String) param.get(Dict.PROJECT_ID);
        String pipeline_id = (String) param.get("pipeline_id");
        String gitlab_token = (String) param.get("gitlab_token");
        String url = this.gitlabApiUrl + "projects/" + project_id + "/pipelines/" + pipeline_id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        JSONObject jsonObject = JSONObject.parseObject((String) submitGet);
        JSONObject result = new JSONObject();
        result.put("duration", jsonObject.get("duration"));
        result.put("ref", jsonObject.get("ref"));
        result.put("id", jsonObject.get("id"));
        result.put("user", jsonObject.get("user"));
        result.put("status", jsonObject.get("status"));
        result.put("sha", jsonObject.get("sha"));
        result.put("finished_at", jsonObject.get("finished_at"));
        result.put("web_url", jsonObject.get("web_url"));
        return result;
    }

    /**
     * *   查询gitlab项目pipeine下jobs
     *
     * @param param
     * @return pipeline数组
     * @throws Exception
     */
    @Override
    public Object queryJobs(Map param) throws Exception {
        String project_id = (String) param.get(Dict.PROJECT_ID);
        String pipeline_id = (String) param.get("pipeline_id");
        String gitlab_token = (String) param.get("gitlab_token");
        String url = this.gitlabApiUrl + "projects/" + project_id + "/pipelines/" + pipeline_id + "/jobs";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        return JSONArray.parseArray((String) submitGet);
    }

    /**
     * 根据gitlab_id 检查分支是否存在
     *
     * @param id
     * @param branch
     * @throws Exception
     */
    public void ValidateBranch(Integer id, String branch) throws Exception {
        String branchUrl = URLEncoder.encode(branch, "UTF-8");
        String mergerequest_url = this.gitlabApiUrl + "projects/" + id + "/repository/branches/" + branchUrl;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mergerequest_url);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            this.gitlabTransport.submitGet(uri, header);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{branch + "分支不存在"});
        }
    }

    /**
     * @param id      应用id
     * @param sbranch 源分支
     * @param tbranch 目标分支
     * @param title   填写信息
     * @return 合并信息
     * @throws Exception
     */
    @Override
    public Object createMergeRequest(Integer id, String sbranch, String tbranch, String title, String desc) throws Exception {
        String mergerequest_url = this.gitlabApiUrl + "projects/" + id + "/merge_requests";
        //检验源分支是否存在
        ValidateBranch(id, sbranch);
        //检验目标分支是否存在
        ValidateBranch(id, tbranch);
        Map<String, String> param = new HashMap<String, String>();
        if (!CommonUtils.isNullOrEmpty(desc)) {
            param.put(Dict.DESCRIPTION, desc);
        }
        param.put(Dict.SOURCE_BRANCH, sbranch);
        param.put(Dict.TARGET_BRANCH, tbranch);
        param.put(Dict.TITLE, title);
        param.put(Dict.PRIVATE_TOKEN_L, gitlabManagerToken);
        JSONObject jsonObject = JSONObject.parseObject((String) gitlabTransport.submitPost(mergerequest_url, param));
        return jsonObject;
    }

    @Override
    public void addMember(String id, String userId, String role) throws Exception {
        String member_url = this.gitlabApiUrl + "projects/" + id + "/members";
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.USER_ID, userId);
        param.put(Dict.ACCESS_LEVEL, role);
        param.put(Dict.PRIVATE_TOKEN_L, this.gitlabManagerToken);
        this.gitlabTransport.submitPost(member_url, param);
    }

    @Override
    public Object createTag(String id, String name, String ref, String token) throws Exception {
        String tags_url = projectUrl() + "/" + id + "/repository/tags";
        Map<String, String> param = new HashMap<>();
        param.put(Dict.TAG_NAME, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        return gitlabTransport.submitPost(tags_url, param);
    }
    @Override
    public void deleteTag(Integer gitlabId, String tagName) throws Exception {
        String deleteBranch_url = new StringBuilder(gitlabApiUrl)
                .append("projects/")
                .append(gitlabId)
                .append("/repository/tags/")
                .append(tagName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlabManagerToken);
        try {
            gitlabTransport.submitDelete(deleteBranch_url, header);
        } catch (HttpClientErrorException e) {
            throw new FdevException(ErrorConstants.DELETE_TAG_ERROR,new String[]{tagName});
        }
    }

    @Override
    public boolean checkBranchExists(Integer id, String branchName) {
        boolean flag = false;
        try {
            ValidateBranch(id, branchName);
            flag = true;
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return flag;
    }

    /**
     * 创建项目pipline
     *
     * @param id        应用id
     * @param ref       所属分支 exl：master
     * @param variables 所需参数[{ "key" : "CI_DEPLOY", "value" : "deploy" }]
     * @param token     AccessTokens验证用户
     * @return pipline相关信息
     * @throws Exception
     */
    @Override
    public Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token)
            throws Exception {
        String pipeline_url = projectUrl() + "/" + id + "/pipeline";
        // 拆分 variables参数 将其重新组装为map再加入到list中
        Map<String, Object> param = new HashMap<String, Object>();
        if (!CommonUtils.isNullOrEmpty(variables)) {
            param.put(Dict.VARIABLES, variables);
        }
        param.put(Dict.ID, id);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.REF, ref);
        return gitlabTransport.submitPost(pipeline_url, param);
    }

}

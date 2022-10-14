package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.GitlabTransport;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.IGitlabApiService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.omg.CORBA.OBJ_ADAPTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装 gitlab api 请求
 *
 * @author xxx
 * @date 2019/7/31 9:27
 */
@Component
@RefreshScope
public class GitlabApiServiceImpl implements IGitlabApiService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${gitlib.path}")
    private String gitlabUrl;
    private static String FILECONTEXT = "/repository/files/";
    private static String BRANCHCONTEXT = "/repository/branches/";
    private static String COMMITS = "/repository/commits";
    @Autowired
    private GitlabTransport gitlabTransport;

    private String getProjectsUrl() {
        return this.gitlabUrl + "projects/";
    }

    /**
     * 向 对应的分支 创建文件,有则执行更新,无则创建
     *
     * @param token
     * @param gitlabId
     * @param branch
     * @param fileDir
     * @param content
     * @param commitMessage
     */
    @Override
    public void createFile(String token, String gitlabId, String branch, String fileDir, String content,
                           String commitMessage) {
        String url = this.getProjectsUrl() + gitlabId + FILECONTEXT + fileDir;
        Map<String, String> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.BRANCH, branch);
        param.put(Dict.CONTENT, content);
        param.put(Dict.COMMIT_MESSAGE, commitMessage);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        try {
            this.gitlabTransport.submitPost(uri, param);
        } catch (Exception e) {
            try {
                param.remove(Dict.PRIVATE_TOKEN_L);
                uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
                this.gitlabTransport.submitPut(uri, token, param);
            } catch (Exception e1) {
                this.logger.error("####Update file error! rul=[{}], data=[{}]", url, param);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"文件存在，请删除"});
            }
        }
    }

    /**
     * 检验分支是否存在
     *
     * @param token
     * @param gitlabId
     * @param branch
     */
    @Override
    public void checkBranch(String token, String gitlabId, String branch) throws Exception {
        String url = this.getProjectsUrl() + gitlabId + BRANCHCONTEXT + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        // 如果没有查到当前分支,此处会抛错
        this.gitlabTransport.submitGet(url, header);
    }

    /**
     * 获取 gitlab 文件内容
     *
     * @param token
     * @param gitlabId
     * @param featureBranch
     * @param fileDir
     * @return
     */
    @Override
    public String getFileContent(String token, String gitlabId, String featureBranch, String fileDir) throws Exception {
        String url = this.getProjectsUrl() + gitlabId + FILECONTEXT + fileDir + "/raw";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParam(Dict.REF, featureBranch).build(true).toUri();
        Object content = null;
        try {
            content = this.gitlabTransport.submitGet(uri, headers);
        } catch (FdevException e) {
            if (e.getCode().equals(ErrorConstants.INVAILD_OPERATION_DATA)) {
                logger.error("应用:[{}], 分支:[{}], 文件路径:[{}], 未配置外部参数,出错信息:[{}]", gitlabId, featureBranch, fileDir, e.getCode());
            }
        }
        if (content == null) {
            content = "";
        }
        return content.toString();
    }

    /**
     * 判断该分支与文件是否存在
     * @param token
     * @param gitlabId
     * @param featureBranch
     * @param fileDir
     * @return
     */
    @Override
    public boolean checkFileExists(String token, String gitlabId, String featureBranch, String fileDir) {
        boolean flag = false;
        String url = this.getProjectsUrl() + gitlabId + FILECONTEXT + fileDir + "/raw";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParam(Dict.REF, featureBranch).build(true).toUri();
        try {
            this.gitlabTransport.submitGet(uri, headers);
            flag = true;
        } catch (FdevException e) {
            if (e.getCode().equals(ErrorConstants.INVAILD_OPERATION_DATA)) {
                logger.error("应用:[{}], 分支:[{}], 文件路径:[{}], 未配置外部参数,出错信息:[{}]", gitlabId, featureBranch, fileDir, e.getCode());
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        }
        return flag;
    }

    /**
     * 根据项目id和分支获取这个分支的提交信息(commit)
     *
     * @param token
     * @param gitlabProjectId
     * @param branch
     * @return
     */
    @Override
    public List<Map<String, Object>> getCommits(String token, String gitlabProjectId, String branch) throws Exception {
        String url = this.getProjectsUrl() + gitlabProjectId + COMMITS + "?ref_name=" + branch;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        String commits = (String) this.gitlabTransport.submitGet(url, headers);
        JSONArray jsonArray = JSONArray.fromObject(commits);
        List<Map<String, Object>> list = new ArrayList<>();
        if (!jsonArray.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = objectMapper.readValue(jsonObject.toString(), Map.class);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 获取gitlab项目信息
     *
     * @param token
     * @param gitlabProjectId
     * @return
     */
    @Override
    public Map<String, Object> getProjectById(String token, String gitlabProjectId) throws Exception {
        String url = this.getProjectsUrl() + gitlabProjectId;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        String appInfo = (String) this.gitlabTransport.submitGet(url, headers);
        JSONObject jsonObject = JSONObject.fromObject(appInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonObject.toString(), Map.class);
    }

    /**
     * 获取提交请求的结果
     *
     * @param gitlabProjectId
     * @param commitId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String,Object> > getCommitDetail(String token, String gitlabProjectId, String commitId) throws Exception {
        String url = this.getProjectsUrl() + gitlabProjectId+COMMITS+"/"+commitId+"/diff";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        String commits = (String) this.gitlabTransport.submitGet(url, headers);
        JSONArray jsonArray = JSONArray.fromObject(commits);
        List<Map<String, Object>> list = new ArrayList<>();
        if (!jsonArray.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map map = objectMapper.readValue(jsonObject.toString(), Map.class);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 创建 tag
     * @param gitlabId
     * @param name
     * @param ref
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public Object createTag(String gitlabId, String name, String ref, String token) throws Exception {
        String url =this.getProjectsUrl()  + "/" + gitlabId + "/repository/tags";
        Map<String, String> param = new HashMap<>();
        param.put(Dict.TAG_NAME, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        return gitlabTransport.submitPost(url, param);
    }


    /**
     * 校验 tag 是否存在
     * @param gitlabId
     * @param tag
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkTag(String gitlabId, String tag, String token) throws Exception {
        String url =this.getProjectsUrl()  + "/" + gitlabId + "/repository/tags/" + tag;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            this.gitlabTransport.submitGet(url, header);
            return true;
        } catch (FdevException e) {
            if (e.getCode().equals(ErrorConstants.INVAILD_OPERATION_DATA)) {
                return false;
            } else {
                throw new FdevException(e.getCode(), e.getArgs());
            }
        }
    }

    /**
     * 删除指定 tag
     * @param gitlabId
     * @param tag
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public Object deleteTag(String gitlabId, String tag, String token) throws Exception {
        String url =this.getProjectsUrl()  + "/" + gitlabId + "/repository/tags/" + tag;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return this.gitlabTransport.submitDelete(url, header);
    }

    /**
     * 根据组id和项目名称判断项目是否存在
     *
     * @param namespaceId 组id
     * @param name        项目名称
     * @return
     * @throws Exception
     */
    @Override
    public Integer isProjectExist(Integer namespaceId, String name,String token) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String searce_pro_url = this.gitlabUrl+"projects" + "?search=" + name;// 获取所以该名称的项目信息
        Object projects = gitlabTransport.submitGet(searce_pro_url, header);
        if (CommonUtils.isNullOrEmpty(projects)) {// 若未查询到 则说明不存在
            return null;
        }
        JSONArray project_list = JSONArray.fromObject(projects);
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get(Dict.NAMESPACE);
            Integer gitlabId=(Integer)map.get(Dict.ID);
            String project_name = (String) map.get(Dict.NAME);
            Integer namespaceid = (Integer) namespace.get(Dict.ID);
            if (namespaceId.equals(namespaceid) && name.equalsIgnoreCase(project_name)) {// 判断groupid是否相同
                // 判断name是否相同
                return gitlabId;
            }
        }
        return null;
    }
}

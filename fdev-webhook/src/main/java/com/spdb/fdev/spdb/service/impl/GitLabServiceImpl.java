package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.GitlabTransportUtil;
import com.spdb.fdev.spdb.service.GitLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class GitLabServiceImpl implements GitLabService {

    @Value("${git.api.url}")
    private String gitApiUrl;
    @Value("${git.token}")
    private String gitToken;
    @Value("${git.api.base.url}")
    private String gitUrl;
    @Autowired
    private GitlabTransportUtil gitlabTransportUtil;

    @Override
    public boolean mavenProFlag(Integer projectId) {
        Map<String, Object> ymlMap = getGitlabCiYml(projectId);
        boolean mavenFlag = false;
        if (ymlMap.containsKey(Dict.MAVEN_BUILD)) {
            mavenFlag = true;
        }
        return mavenFlag;
    }

    @Override
    public boolean jsProFlag(Integer projectId) {
        String url = gitApiUrl + projectId + "/repository/files/package-lock.json?ref=master";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        try {
            gitlabTransportUtil.submitGet(url, header);
        } catch (HttpClientErrorException httpClientErrorException) {
            if (httpClientErrorException.getRawStatusCode() == 404) {
                return false;
            }
            throw httpClientErrorException;
        }
        return true;
    }

    @Override
    public Map<String, Object> getGitlabCiYml(Integer projectId) {
        String filePath = ".gitlab-ci.yml";
        String fileContent = getGitLabFileContent(projectId, filePath);
        Yaml yaml = new Yaml();
        return yaml.load(fileContent);
    }

    /**
     * 通过username来查询gitlab用户信息
     *
     * @param username
     * @return
     */
    @Override
    public List<Map> getUserByUsername(String username) {
        String url = gitUrl + "users?username=" + username;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONArray.parseArray(result).toJavaList(Map.class);
    }

    /**
     * 根据merge来获取当前
     *
     * @param iid
     * @param projectId
     * @return
     */
    @Override
    public List<Map> getCommitsByIid(Integer iid, Integer projectId) {
        String url = this.gitApiUrl + projectId + "/merge_requests/" + iid + "/commits";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONArray.parseArray(result).toJavaList(Map.class);
    }

    /**
     * 根据sha来查询commit的信息，查看是否存在有冲突
     *
     * @param projectId
     * @param sha
     * @return
     */
    @Override
    public Map getCommitBySha(Integer projectId, String sha) {
        String url = this.gitApiUrl + projectId + "/repository/commits/" + sha;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONObject.parseObject(result).toJavaObject(Map.class);
    }

    @Override
    public Map<String, Object> compare(Integer projectId, String from, String to) {
        String url = this.gitApiUrl + projectId + "/repository/compare?from=" + from + "&to=" + to;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONObject.parseObject(result).toJavaObject(Map.class);
    }

    /**
     * 通过gitlab_id来查询gitlab的用户信息
     * @param creator
     * @return
     */
    @Override
    public Map getUserInfoById(Integer creator) {
        String url = this.gitUrl + "users/" + creator;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONObject.parseObject(result).toJavaObject(Map.class);
    }

    private String getGitLabFileContent(Integer projectId, String filePath) {
        String url = gitApiUrl + projectId + "/repository/files/" + filePath + "/raw?ref=master";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        return (String) gitlabTransportUtil.submitGet(uri, header);
    }

}

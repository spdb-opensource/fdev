package com.spdb.fdev.gitlabwork.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.GitlabTransportUtil;
import com.spdb.fdev.gitlabwork.service.GitLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class GitLabServiceImpl implements GitLabService {

    @Value("${git.api.project.url}")
    private String gitApiUrl;
    @Value("${gitlab.token}")
    private String gitToken;
    @Value("${gitlab.api.url}")
    private String gitUrl;
    @Autowired
    private GitlabTransportUtil gitlabTransportUtil;

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

}

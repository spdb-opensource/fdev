package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.GitlabTransportUtil;
import com.spdb.fdev.common.exception.FdevException;
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
import java.util.Map;

@Service
@RefreshScope
public class GitLabServiceImpl implements GitLabService {

    @Value("${git.api.url}")
    private String gitApiUrl;
    @Value("${git.token}")
    private String gitToken;
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

    private String getGitLabFileContent(Integer projectId, String filePath) {
        String url = gitApiUrl + projectId + "/repository/files/" + filePath + "/raw?ref=master";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        return (String) gitlabTransportUtil.submitGet(uri, header);
    }

    @Override
    public Map getProjectInfo(String id, String token) {
        String projectUrl = gitApiUrl + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Map map;
        try {
            Object resultInfo = this.gitlabTransportUtil.submitGet(projectUrl, header);
            map = (Map) JSONObject.parse((String) resultInfo);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        return map;
    }

    @Override
    public Map<String, Object> compare(Integer projectId, String from, String to) {
        String url = this.gitApiUrl + projectId + "/repository/compare?from=" + from + "&to=" + to;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        String result = (String) gitlabTransportUtil.submitGet(url, header);
        return JSONObject.parseObject(result).toJavaObject(Map.class);
    }

}

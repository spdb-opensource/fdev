package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.GitlabTransportUtil;
import com.spdb.fdev.fdevinterface.spdb.service.GitLabService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitLabServiceImpl implements GitLabService {

    private Logger logger = LoggerFactory.getLogger(GitLabServiceImpl.class);

    @Value(value = "${git.api.url}")
    public String gitLabApiUrl;
    @Value(value = "${git.token}")
    public String gitToken;
    @Resource
    public GitlabTransportUtil gitlabTransportUtil;

    @Override
    public List<Map> getProjectsRepository(Integer projectId, String appServiceId, String branch, String path) {
        List<Map> repositoryList = new ArrayList<>();
        // GitLab page默认为1，per_page默认为20，最大为100
        for (int i = 1; i <= 20; i++) {
            List<Map> repoList = getProjectsRepositoryByPage(projectId, appServiceId, branch, path, i);
            if (CollectionUtils.isEmpty(repoList)) {
                return repositoryList;
            }
            repositoryList.addAll(repoList);
            if (repoList.size() != Constants.HUNDRED) {
                break;
            }
        }
        return repositoryList;
    }

    public List<Map> getProjectsRepositoryByPage(Integer projectId, String appServiceId, String branch, String path, Integer page) {
        List<Map> repositoryList = new ArrayList<>();
        String gitLabUrl = gitLabApiUrl + projectId + "/repository/tree?ref=" + branch + "&path=" + path + "&page=" + page + "&per_page=" + Constants.HUNDRED;
        MultiValueMap header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        try {
            String submitGet = (String) gitlabTransportUtil.submitGet(gitLabUrl, header);
            repositoryList = JSONArray.parseArray(submitGet, Map.class);
        } catch (Exception e) {
            logger.info("获取{}的Repository ID失败：{}", appServiceId, e.getMessage());
        }
        return repositoryList;
    }

    /**
     * 获取 gitlab 文件内容
     *
     * @param gitlabId
     * @param branch
     * @param fileDir
     * @return
     */
    @Override
    public String getFileContent(Integer gitlabId, String branch, String fileDir) {
        String url = gitLabApiUrl + gitlabId + Constants.FILECONTEXT + fileDir + "/raw";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, gitToken);
        try {
            branch = URLEncoder.encode(branch, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"参数URL编码出错！"});
        }
        URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParam( Dict.REF, branch).build(true).toUri();
        Object content = null;
        try {
            content = gitlabTransportUtil.submitGet(uri, headers);
        } catch (FdevException e) {
            logger.info("获取{}的内容失败：{}", fileDir, e.getMessage());
        }
        if (content == null) {
            content = "";
        }
        return content.toString();
    }

}

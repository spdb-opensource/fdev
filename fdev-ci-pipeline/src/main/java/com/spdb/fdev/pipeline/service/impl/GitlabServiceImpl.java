package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.service.IGitlabService;
import com.spdb.fdev.pipeline.transport.GitlabTransport;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class GitlabServiceImpl implements IGitlabService {

    @Autowired
    GitlabTransport gitlabTransport;

    @Value("${gitlab.url}")
    String gitlaUrl;

    private static final Logger logger = LoggerFactory.getLogger(GitlabServiceImpl.class);

    @Override
    public List<String> queryBranches(Integer projectId, String userToken) throws Exception {
        List<String> result = new ArrayList();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, userToken);
        JSONArray project_list = new JSONArray();
        for(int page = 1;page <= 100;page ++) {
            String url = new StringBuilder(gitlaUrl).append("api/v4/projects/").append(projectId)
                    .append("/repository/branches?page=").append(page).append("&per_page=100&order_by=name").toString();
            logger.info("****************************queryBranches时请求的url"+url+"************************************");
            logger.info("*******************queryBranches时gitlabTransport请求头 :" + JSONObject.toJSONString(header));
            Object projects = gitlabTransport.submitGet(url, header);
            logger.info("*******************queryBranches时gitlabTransport提交返回的数据"+JSONObject.toJSONString(projects));
            JSONArray page_list = JSONObject.parseArray((String) projects);
            if (!CommonUtils.isNullOrEmpty(page_list)) {// 若未查询到 则说明不存在
                project_list.addAll(page_list);
            } else {
                break;
            }
        }
        for (Object project : project_list) {
            Map map = (Map) project;
            result.add((String) map.get(Dict.NAME));
        }
        return result;
    }

    @Override
    public List<String> queryTags(Integer projectId, String userToken) throws Exception {
        List<String> result = new ArrayList();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, userToken);
        JSONArray project_list = new JSONArray();
        for(int page = 1;page <= 20;page ++) {
            String url = new StringBuilder(gitlaUrl).append("api/v4/projects/").append(projectId)
                    .append("/repository/tags?page=").append(page).append("&per_page=100&order_by=name").toString();
            logger.info("****************************queryTags时请求的url"+url+"************************************");
            logger.info("*******************queryTags时gitlabTransport请求头 :" + JSONObject.toJSONString(header));
            Object projects = gitlabTransport.submitGet(url, header);
            JSONArray page_list = JSONObject.parseArray((String) projects);
            if (!CommonUtils.isNullOrEmpty(page_list)) {// 若未查询到 则说明不存在
                project_list.addAll(page_list);
            } else {
                break;
            }
        }
        for (Object project : project_list) {
            Map map = (Map) project;
            result.add((String) map.get(Dict.NAME));
        }
        return result;
    }

    @Override
    public String queryShortId(Integer projectId, String userToken,String branch) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, userToken);
        String url = new StringBuilder(gitlaUrl).append("api/v4/projects/").append(projectId)
                .append("/repository/commits?ref_name=").append(branch).toString();
        logger.info("****************************queryShortId时请求的url"+url+"************************************");
        List resultList = JSONObject.parseArray((String) gitlabTransport.submitGet(url, header));
        String commitsShortId = null;
        if (!CommonUtils.isNullOrEmpty(resultList)){
            Map resultMap = (Map)resultList.get(0);
            commitsShortId = String.valueOf(resultMap.get(Dict.SHORT_ID));
        }
        return commitsShortId;
    }

    @Override
    public Map queryTypeAndContent(Integer gitlabId, String git_token, String path, String branch) throws Exception {
        String filePath = CommonUtils.fileUtil(path);
        String uri = gitlaUrl + "api/v4/projects/" + gitlabId + "/repository/files/" + filePath + "?ref=" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, git_token);
        String result = null;
        try {
            result = (String) this.gitlabTransport.submitGet(uri, header);
        } catch (Exception e) {
            logger.error("文件不存在 :" + path);
            return null;
        }
        Map resultMap = new HashMap();
        Map fileContent = new HashMap();
        if (!CommonUtils.isNullOrEmpty(result)){
            Object parse = JSONObject.parse(result);
            fileContent = (Map) parse;
            String content = new String(Base64.decode(String.valueOf(fileContent.get("content"))), "UTF-8");
            resultMap.put("msg",content);
        }
        resultMap.put("filePath",path);
        resultMap.put("type","GITLAB");
        return resultMap;
    }
}

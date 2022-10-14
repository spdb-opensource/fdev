package com.spdb.executor.service;

import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

@Service
@RefreshScope
public class GitlabService {

    @Value("${gitlab.url}")
    private String gitlabUrl;

    @Value("${gitlab.token}")
    private String token;

    private Logger logger = LoggerFactory.getLogger(GitlabService.class);

    public static final String GITLAB_PROJECTS_API_PREFIX = "/api/v4/projects/";

    public static final String GITLAB_BRANCHES_API = "/repository/branches/";

    public static final String GITLAB_TAGS_API = "/repository/tags/";

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;


    public void deleteBranch(String projectId, String branch) throws Exception {
        this.submitDelete(gitlabUrl + GITLAB_PROJECTS_API_PREFIX + projectId + GITLAB_BRANCHES_API + branch);
    }

    public void deleteTag(String projectId, String tag) throws Exception {
        this.submitDelete(gitlabUrl + GITLAB_PROJECTS_API_PREFIX + projectId + GITLAB_TAGS_API + tag);
    }

    public void submitDelete(String url) throws Exception {
        long startTime = System.currentTimeMillis();
        this.logger.info(">>>>>>>>> begin transport: {} , random: {}", url, startTime);
        String result = "";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Private-Token", token);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        try {
            ResponseEntity<String> exchange = this.restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
            result = exchange.getStatusCode().toString();
        } catch (RestClientException e) {
            this.logger.error("rest request error, url: {}", url, e);
            throw new FdevException(ErrorConstants.ERROR_SYS_INNER);
        }
        long lastTime = System.currentTimeMillis() - startTime;
        this.logger.info(">>>>>>>>> receive data: {} , random: {}", result, startTime);
        this.logger.info(">>>>>>>>> end transport: {}  on time: {}ms, random: {}", new Object[]{url, lastTime, startTime});
    }

    public Object post(String url, JSONObject data) throws Exception {
        long startTime = System.currentTimeMillis();
        this.logger.info(">>>>>>>>> begin transport: {} , random: {}", url, startTime);
        String status = "";
        String result = "";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Private-Token", token);
        HttpEntity httpEntity = new HttpEntity(data,httpHeaders);
        try {
            ResponseEntity<String> exchange = this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            status = exchange.getStatusCode().toString();
            result = exchange.getBody();
        } catch (RestClientException e) {
            this.logger.error("rest request error, url: {}", url, e);
            throw new FdevException(ErrorConstants.ERROR_SYS_INNER);
        }
        long lastTime = System.currentTimeMillis() - startTime;
        this.logger.info(">>>>>>>>> receive data: {} , random: {}", status, startTime);
        this.logger.info(">>>>>>>>> end transport: {}  on time: {}ms, random: {}", new Object[]{url, lastTime, startTime});
        return result;
    }
}

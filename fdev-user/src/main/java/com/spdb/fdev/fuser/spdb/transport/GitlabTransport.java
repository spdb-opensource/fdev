package com.spdb.fdev.fuser.spdb.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
@RefreshScope
public class GitlabTransport {
    @Autowired
    RestTemplate restTemplate;

    @Value("${fdev.transport.log.data.enabled:true}")
    private boolean logDataEnabled = true;

    private Logger logger = LoggerFactory.getLogger(GitlabTransport.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String BEGIN_REQUEST = ">>>>>>>>> begin request:{}";

    private static final String REQUEST_DATA = ">>>>>>>>> request data:{}";

    private static final String RESPONSE_DATA = "<<<<<<<<< response data:{}";


    public Object submitGet(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(BEGIN_REQUEST, url);
        if (logDataEnabled) {
            logger.info(REQUEST_DATA, objectMapper.writeValueAsString(header));
        }
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            result = restTemplate.exchange(new URI(url), HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            throw resolveGitlabException(e, url);
        } finally{
            if (logDataEnabled) {
                logger.info(RESPONSE_DATA, objectMapper.writeValueAsString(result));
            }
        }
    }


    public Object submitDelete(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(BEGIN_REQUEST, url);
        if (logDataEnabled) {
            logger.info(REQUEST_DATA, objectMapper.writeValueAsString(header));
        }
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            result = restTemplate.exchange(new URI(url), HttpMethod.DELETE, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            throw resolveGitlabException(e, url);
        } finally{
            if (logDataEnabled) {
                logger.info(RESPONSE_DATA, objectMapper.writeValueAsString(result));
            }
        }
    }

    public Object submitGet(URI url, MultiValueMap<String, String> header) throws Exception {
        logger.info(BEGIN_REQUEST, url.toString());
        if (logDataEnabled) {
            logger.info(REQUEST_DATA, objectMapper.writeValueAsString(header));
        }
        ResponseEntity<String> result = null;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            throw resolveGitlabException(e, url.toString());
        } finally {
            if (logDataEnabled) {
                logger.info(RESPONSE_DATA, objectMapper.writeValueAsString(result));
            }
        }
    }


    private FdevException resolveGitlabException(HttpClientErrorException e, String url) throws Exception {
        if (e.getRawStatusCode() == 401) {
            logger.error("gitlab error>>>Unauthorized:the role error>>>>url:{}", url);
            return new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR,
                    new String[]{"操作无权限：" + e.getRawStatusCode()});
        } else if (e.getRawStatusCode() == 403) {
            logger.error("GitLab error>>>Forbidden:this action is forbidden for this user>>>>url:{}" , url);
            return new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR,
                    new String[]{"操作无权限：" + e.getRawStatusCode()});
        } else if (e.getRawStatusCode() == 404) {
            logger.error("GitLab error>>>Not Found:can't find this resource>>>>url:{}", url);
            return new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR,
                    new String[]{"请求资源不存在：" + e.getRawStatusCode()});
        } else if (e.getRawStatusCode() == 409) {
            logger.error("GitLab error>>>Conflict:this request already exist>>>>url:{}", url);
            return new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR,
                    new String[]{"请求已存在：" + e.getRawStatusCode()});
        } else {
            logger.error("GitLab error>>>Bad Request:wrong request data>>>>url:{}", url);
            return new FdevException(ErrorConstants.GITLAB_REQUEST_ERROR,
                    new String[]{new StringBuilder(e.getRawStatusCode())
                            .append("(")
                            .append(e.getResponseBodyAsString())
                            .append(")").toString()});
        }
    }
}

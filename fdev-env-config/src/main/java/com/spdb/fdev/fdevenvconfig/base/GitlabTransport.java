package com.spdb.fdev.fdevenvconfig.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
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
import java.util.Map;

@Component
@RefreshScope
public class GitlabTransport {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${fdev.transport.log.data.enabled:true}")
    private boolean logDataEnabled = true;

    private Logger logger = LoggerFactory.getLogger(GitlabTransport.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fdev调用gitlab接口api
     *
     * @param url  调用api url
     * @param send 发送map数据
     * @return
     * @throws Exception
     */
    public Object submitPost(String url, Map send) throws Exception {
        return submitPost(url, null, send);
    }

    public Object submitPost(String url, String token, Map send) throws Exception {
        this.logDataEnabledStart(url, send);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        if (token != null) {
            httpHeaders.add(Dict.PRIVATE_TOKEN, token);
        }
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, httpHeaders);
        try {
            result = restTemplate.postForEntity(url, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitPut(URI url, String token, Map send) throws Exception {
        this.logDataEnabledStart(url, send);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add(Dict.PRIVATE_TOKEN, token);
        HttpEntity<Object> request = new HttpEntity<Object>(objectMapper.writeValueAsString(send), httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitGet(String url, MultiValueMap<String, String> header) throws Exception {
        this.logDataEnabledStart(url, header);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitDelete(String url, MultiValueMap<String, String> header) throws Exception {
        this.logDataEnabledStart(url, header);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitPost(URI url, Map send) throws Exception {
        this.logDataEnabledStart(url, send);
        HttpHeaders httpHeaders = new HttpHeaders();
        ResponseEntity<String> result = null;
        httpHeaders.add("Content-Type", "application/json");
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, httpHeaders);
        try {
            result = restTemplate.postForEntity(url, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitGet(URI url, MultiValueMap<String, String> header) throws Exception {
        this.logDataEnabledStart(url, header);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    public Object submitDelete(URI url, MultiValueMap<String, String> header) throws Exception {
        this.logDataEnabledStart(url, header);
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw this.gitlabException(url, e);
        } finally {
            this.logDataEnabledEnd(result);
        }
    }

    /**
     * 开始日常日志打印
     *
     * @param url  url路径
     * @param data 数据信息
     */
    public void logDataEnabledStart(Object url, Object data) throws JsonProcessingException {
        logger.info(">>>>>>>>> begin request:{}", url.toString());
        if (logDataEnabled) {
            logger.info(">>>>>>>>> request data:{}", objectMapper.writeValueAsString(data));
        }
    }

    /**
     * 方法执行完成以后的数据打印
     *
     * @param data
     * @throws JsonProcessingException
     */
    public void logDataEnabledEnd(Object data) throws JsonProcessingException {
        if (logDataEnabled) logger.info(">>>>>>>>> request data:{}", objectMapper.writeValueAsString(data));
    }

    /**
     * 请求gitlab api出现的异常
     *
     * @param url url
     * @param e
     * @return 返回异常类
     */
    public FdevException gitlabException(Object url, HttpClientErrorException e) {
        if (e.getRawStatusCode() == 401) {
            logger.error("gitlab error>>>Unauthorized:the role error>>>>url:{}", url.toString());
            return new FdevException(ErrorConstants.ROLE_ERROR,
                    new String[]{"@@@@@ gitlab role error " + e.getMessage()});
        } else if (e.getRawStatusCode() == 403) {
            logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:{}", url.toString());
            return new FdevException(ErrorConstants.ROLE_ERROR,
                    new String[]{"@@@@@ gitlab role error " + e.getMessage()});
        } else if (e.getRawStatusCode() == 404) {
            logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:{}", url.toString());
            return new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                    new String[]{"@@@@@" + e.getMessage()});
        } else if (e.getRawStatusCode() == 409) {
            logger.error("gitlab error>>>Conflict:this request already exist>>>>url:{}", url.toString());
            return new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                    new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
        } else {
            logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:{}", url.toString());
            return new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{" @@@@@ gitlab request param is missing" + e.getMessage()});
        }
    }
}

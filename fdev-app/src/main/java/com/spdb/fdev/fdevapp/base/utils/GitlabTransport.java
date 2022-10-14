package com.spdb.fdev.fdevapp.base.utils;

import java.net.URI;
import java.util.Map;

import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;

@Component
public class GitlabTransport {
    @Autowired
    RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger("gitlab.api.log");

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
        logger.info(">>>>>>>>> begin request:" + "method:post  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(send));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitPost error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 400) {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            } else if (e.getRawStatusCode() == 401) {
                if (message.contains("invalid_token")) {
                    throw new FdevException(ErrorConstants.ROLE_ERROR,
                            new String[]{"@@@@@当前用户token已失效"});
                } else {
                    logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                    throw new FdevException(ErrorConstants.ROLE_ERROR,
                            new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
                }
            } else if (e.getRawStatusCode() == 403) {
                logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url);
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getRawStatusCode()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {

            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(request));

        }
    }

    public Object submitPut(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:put  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitPut error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url);
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getRawStatusCode()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {

            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));

        }
    }

    public Object submitGet(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitGet error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url);
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getRawStatusCode()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }

    public Object submitDelete(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:delete  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitDelete error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error("gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url);
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getRawStatusCode()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url);
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getRawStatusCode()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }

    public Object submitPost(URI url, Map send) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:post  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(send));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitPost error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url);
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error(
                        "gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@" + e.getMessage()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url.toString());
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(request));
        }
    }

    public Object submitGet(URI url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitGet error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error(
                        "gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getMessage()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url.toString());
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }

    public Object submitDelete(URI url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:delete  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            Object returnData = result.getBody();
            return returnData;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitDelete erroe");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error(
                        "gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getMessage()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url.toString());
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }

    public Object submitGetGetHeader(URI url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            HttpHeaders headers = result.getHeaders();
            return headers;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitGet error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error(
                        "gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getMessage()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url.toString());
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }

    public Object submitGetGetHeader(String url, MultiValueMap<String, String> header) throws Exception {
        logger.info(">>>>>>>>> begin request:" + "method:get  url:" + url);
        logger.debug(">>>>>>>>> request data:" + objectMapper.writeValueAsString(header));
        ResponseEntity<String> result = null;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            HttpHeaders headers = result.getHeaders();
            return headers;
        } catch (HttpClientErrorException e) {
            logger.error("sumbitGet error");
            String message = new String(e.getResponseBodyAsByteArray());
            logger.error("e:" + e);
            logger.error("error message:" + message);
            if (e.getRawStatusCode() == 401) {
                logger.error("gitlab error>>>Unauthorized:the role error>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 403) {
                logger.error(
                        "gitlab error>>>Forbidden:this action is forbidden for this user>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.ROLE_ERROR,
                        new String[]{"@@@@@ gitlab role error" + e.getMessage()});
            } else if (e.getRawStatusCode() == 404) {
                logger.error("gitlab error>>>Not Found:can't find this resource>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,
                        new String[]{"@@@@@" + e.getMessage()});
            } else if (e.getRawStatusCode() == 409) {
                logger.error("gitlab error>>>Conflict:this request already exist>>>>url:" + url.toString());
                throw new FdevException(ErrorConstants.REQUEST_ALREADY_EXIST,
                        new String[]{" @@@@@ gitlab request is already exist" + e.getMessage()});
            } else {
                logger.error("gitlab error>>>Bad Request:wrong request data>>>>url:" + url.toString());
                JSONObject jsonObject = JSONObject.parseObject(message);
                Map<String, Object> map = jsonObject.getInnerMap();
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,
                        new String[]{" @@@@@ gitlab bad request" + e.getRawStatusCode() + ",message: " + map.get("message")});
            }
        } finally {
            logger.debug("<<<<<<<<< response data:" + objectMapper.writeValueAsString(result));
        }
    }
}

package com.spdb.fdev.fdevinterface.base.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
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

import java.net.URI;

@Component
public class GitlabTransportUtil {

    @Autowired
    RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(GitlabTransportUtil.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    public Object submitGet(String url, MultiValueMap<String, String> header) {
        logRequest(Dict.GET, url, header);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logResponse(result);
            return result.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            logger.error("code:{} msg:{}", httpClientErrorException.getRawStatusCode(), httpClientErrorException.getMessage());
            throw httpClientErrorException;
        }
    }

    public Object submitGet(URI uri, MultiValueMap<String, String> header) {
        logRequest(Dict.GET, uri, header);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(header);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            logResponse(result);
            return result.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            logger.error("code:{} msg:{}", httpClientErrorException.getRawStatusCode(), httpClientErrorException.getMessage());
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA);
        }
    }

    /**
     * 打印请求参数
     *
     * @param url
     * @param header
     */
    private void logRequest(String httpMethod, Object url, MultiValueMap<String, String> header) {
        logger.info(">>>>>>>>> begin request: method:{} url:{}", httpMethod, url);
        try {
            String data = objectMapper.writeValueAsString(header);
            logger.debug(">>>>>>>>> request data:{}", data);
        } catch (JsonProcessingException e) {
            logger.error("error msg:{}", e.getMessage());
        }
    }

    /**
     * 打印响应参数
     *
     * @param result
     */
    private void logResponse(ResponseEntity<String> result) {
        try {
            String data = objectMapper.writeValueAsString(result);
            logger.debug("<<<<<<<<< response data:{}", data);
        } catch (JsonProcessingException e) {
            logger.error("error msg:{}", e.getMessage());
        }
    }

}

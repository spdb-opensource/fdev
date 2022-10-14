package com.plan.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.plan.dict.Dict;
@Service
public class MantisRestTemplate {
	@Autowired
	private  RestTemplate restTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(MantisRestTemplate.class);
	
	public String sendPost(String url,String token,Map sendMap) {
		this.logger.info(">>>>>>>>> begin request:" + url);
		this.logger.info(">>>>>>>>> request data:" + token);
		ResponseEntity<String> result = restTemplate.postForEntity(url, sendDataPrepare(token,sendMap), String.class);
		this.logger.info("<<<<<<<<< response data:" + result.getBody());
        return result.getBody();
	}
	
	public String sendGet(String url,String token) {
		this.logger.info(">>>>>>>>> begin request:" + url);
		this.logger.info(">>>>>>>>> request data:" + token);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, sendDataPrepare(token,null), String.class);
		this.logger.info("<<<<<<<<< response data:" + result.getBody());
        return result.getBody();
	}
	
	
	public HttpEntity<Object> sendDataPrepare(String token,Map sendMap) {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=utf-8"); 
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.AUTHORIZATION, token);
        httpHeaders.addAll(header);
        HttpEntity<Object> request = null;
        if(Utils.isEmpty(sendMap)) {
        	request = new HttpEntity<Object>(httpHeaders);
        }else {
        	JSONObject parse = JSONObject.parseObject(JSON.toJSONString(sendMap));
        	request = new HttpEntity<Object>(parse, httpHeaders);
        }
		return request;
	}

}

package com.mantis.util;

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
import com.mantis.dict.Dict;
@Service
public class MantisRestTemplate {
	@Autowired
	private RestTemplate restTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(RestTemplate.class);
	
	public String sendPost(String url,String token,Map sendMap) {
		 logger.info("===post url"+url);
		 ResponseEntity<String> result = restTemplate.postForEntity(url, sendDataPrepare(token,sendMap), String.class);
         return result.getBody();
	}
	
	public String sendGet(String url,String token) {
		logger.info("===get url"+url);
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, sendDataPrepare(token,null), String.class);
        return result.getBody();
	}
	
	public String sendPatch(String url,String token,Map sendMap) {
		logger.info("===patch url"+url);
		 String patchForObject = restTemplate.patchForObject(url, sendDataPrepare(token,sendMap),  String.class);
		 return patchForObject;
	}
	
	public String sendDelete(String url,String token) {
		logger.info("===delete url"+url);
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.DELETE, sendDataPrepare(token,null), String.class);
        return result.getBody();
	}
	
	
	public HttpEntity<Object> sendDataPrepare(String token,Map sendMap) {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
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

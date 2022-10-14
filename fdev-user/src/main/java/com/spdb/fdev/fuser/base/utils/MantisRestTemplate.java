package com.spdb.fdev.fuser.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MantisRestTemplate {
	@Autowired
	private RestTemplate restTemplate;
	
	public String sendPost(String url,String token,Map sendMap) {
		 ResponseEntity<String> result = restTemplate.postForEntity(url, sendDataPrepare(token,sendMap), String.class);
         return result.getBody();
	}
	
	public String sendGet(String url,String token) {
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, sendDataPrepare(token,null), String.class);
        return result.getBody();
	}


	public HttpEntity<Object> sendDataPrepare(String token, Map sendMap) {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add("Authorization", token);
        httpHeaders.addAll(header);
        HttpEntity<Object> request = null;
        if(CommonUtils.isNullOrEmpty(sendMap)) {
        	request = new HttpEntity<Object>(httpHeaders);
        }else {
        	JSONObject parse = JSONObject.parseObject(JSON.toJSONString(sendMap));
        	request = new HttpEntity<Object>(parse, httpHeaders);
        }
		return request;
	}

}

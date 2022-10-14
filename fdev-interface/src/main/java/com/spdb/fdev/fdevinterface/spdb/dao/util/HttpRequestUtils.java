package com.spdb.fdev.fdevinterface.spdb.dao.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class HttpRequestUtils {
	public static ResponseEntity sendGET(String url, MultiValueMap<String, String> header) throws Exception {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.addAll(header);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
		ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, request, String.class);	
		return exchange;
	}

	public static ResponseEntity sendGET(URI url, MultiValueMap<String, String> header) throws Exception {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.addAll(header);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
		ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		return exchange;
	}
	
	public static ResponseEntity sendPOST(String url, JSONObject param) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
		ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
		return postForEntity;
	}
	
	public static ResponseEntity sendPOST(URI url, JSONObject param) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
		ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
		return postForEntity;
	}
	
	public static ResponseEntity sendPUT(String url,JSONObject param){
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
		ResponseEntity<String> postForEntity = restTemplate.exchange(url,HttpMethod.PUT,request, String.class);
		return postForEntity;
	}
	
	public static ResponseEntity sendPUT(URI url,MultiValueMap<String, String> header){
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.addAll(header);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
		ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
		return exchange;	
	}
	
	
	public static ResponseEntity sendDELETE(String url,JSONObject param){
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
		ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
		return exchange;
	}

	public static int sendDELETE(URI uri, MultiValueMap<String, String> header) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.addAll(header);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
		ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
		int statusCode = exchange.getStatusCodeValue();
		return statusCode;
	}

	public static String sendPUT(URI url,JSONObject param){

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
		ResponseEntity<String> postForEntity;
		postForEntity = restTemplate.exchange(url,HttpMethod.PUT,request,String.class);
		String result = postForEntity.getBody();
		return result;
	}
	
	
	
}

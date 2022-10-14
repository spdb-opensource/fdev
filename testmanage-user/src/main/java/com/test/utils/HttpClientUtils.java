package com.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings("all")
public class HttpClientUtils {
	//post请求
	@SuppressWarnings("rawtypes")
	public static List<Map<String,Object>> httpPostWithJson(String implementNoList,String url){
		BaseDataResult result = new BaseDataResult();
		CloseableHttpResponse response=null;
		CloseableHttpClient httpClients=null;
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		String data=null;
		try{
			httpClients =  HttpClients.createDefault();
			HttpPost httPost = new HttpPost(url);
			httPost.addHeader("source","back");
			JSONObject requestParamJson = new JSONObject();
			requestParamJson.put("id", implementNoList);//在这里我需要把自己的数据库数据查询出来List
			httPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
			StringEntity entity = new  StringEntity(requestParamJson.toString(),"utf-8");
			entity.setContentType("application/json");
			httPost.setEntity(entity);
			response = httpClients.execute(httPost);
			HttpEntity httpEntity = response.getEntity();
			ObjectMapper objectMapper=new ObjectMapper();
			String s = EntityUtils.toString(httpEntity,"UTF-8");
			JSONObject json = JSONObject.fromObject(s) ;	
			if("AAAAAAA".equals(json.getString("code"))) {
			    //data=json.getString("data");
			    //json转换成Map
				//最外层解析
				for(Object k:json.keySet()){
					Object v=json.get(k);
					//还是数组继续解析
					if(v instanceof JSONArray){
						Iterator <JSONObject> it=((JSONArray)v).iterator();
						while(it.hasNext()){
							JSONObject json2=it.next();
							System.out.println(json2.toString());
							for(Object k1:json2.keySet()){
								 Map<String,Object> resultMap=new HashMap<String,Object>();
								resultMap.put(String.valueOf(k1), json2.get(k1));
								list.add(resultMap);
							}
						}
						
					}
				}
				return list;

			}
		
		}catch (ClientProtocolException e) {
			System.out.println("Http协议出现问题");
			e.printStackTrace();
		}catch (IOException e) {
			System.out.println("IO异常");
			e.printStackTrace();
		}finally{
			//释放连接
			if(null!=response){
				try{
					response.close();
					httpClients.close();	
				}catch(IOException e){
					System.err.print("释放资源失败");
					e.printStackTrace();
				}
			}
		}
		return list;
	}


	public String getJson(HttpServletRequest request,HttpServletResponse response){
		StringBuffer json=new StringBuffer();
		String line=null;
		try{
			BufferedReader reader=request.getReader();
			while((line=reader.readLine())!=null){
				json.append(line);
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return json.toString();
		
		
	}
	
	//Oauth POST
	public static ResponseEntity sendPOST(String url, com.alibaba.fastjson.JSONObject param) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.add("source", "back");
		org.springframework.http.HttpEntity<Object> request = new org.springframework.http.HttpEntity(param, httpHeaders);
		ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
		return postForEntity;
	}  
	
		public static String[] str2List(String str){
			String[] split = str.split(",");
			return split;
		}
		
		public static String httpPostWithJsonUser(Object object,String url) throws Exception{
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONArray ids = (JSONArray) object;
			StringBuffer list=new StringBuffer();
			loop:for(int i=0 ;i<ids.size();i++){
				JSONObject requestParamJson = new JSONObject();
				requestParamJson.put("id", ids.get(i));//在这里我需要把自己的数据库数据查询出来List
				org.springframework.http.HttpEntity request = new org.springframework.http.HttpEntity<>(requestParamJson.toString(),headers);
				ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
				String s = response.getBody();
				JSONObject json = JSONObject.fromObject(s);	
				if("AAAAAAA".equals(json.getString("code"))) {
				    //data=json.getString("data");
				    //json转换成Map
					//最外层解析
					for(Object k:json.keySet()){
						Object v=json.get(k);
						//还是数组继续解析
						if(v instanceof JSONArray){
							Iterator <JSONObject> it=((JSONArray)v).iterator();
							while(it.hasNext()){
								JSONObject json2=it.next();
								for(Object k1:json2.keySet()){
									if("user_name_cn".equals(String.valueOf(k1))){
										list.append(json2.get(k1).toString()+" ");
									}
								}
							}
						}
					}
				}
			}
			return list.toString();
		}
}

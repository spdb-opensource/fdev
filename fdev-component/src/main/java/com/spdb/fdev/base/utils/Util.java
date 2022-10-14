package com.spdb.fdev.base.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Util
 * <p>
 * 工具类
 *
 * @blame Android Team
 */
public class Util {

    private static final String pattern = "yyyy-MM-dd HH:mm:ss";

    private static final String timestamp = "yyMMddHHmm";

    public static final String datastamp="yyyyMMdd";

    /**
     * 日期格式化
     *
     * @param date
     * @return
     */
    public static String simdateFormat(Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 根据当前时间生成时间戳
     */
    public static String getTimeStamp(Date date) {
        return new SimpleDateFormat(timestamp).format(date);
    }

    /**
     * 根据当前时间生成时间戳
     */
    public static String getDateStamp(Date date) {
        return new SimpleDateFormat(datastamp).format(date);
    }

    /**
     * http请求工具
     *
     * @param url
     * @return
     */
    public static String httpMethodGetExchange(String url, RestTemplate restTemplate) {
        System.out.println("url:" + url);
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");
        //http body配置
        /*MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("search", "CommonModules");
        HttpEntity httpEntity = new HttpEntity(httpHeaders，multiValueMap);*/
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> result;
        String body;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            body = result.getBody();
        } catch (Exception e) {
            return null;
        }
        return body;
    }

    /**
     * http请求工具，通过gitlab接口获取相关信息
     *
     * @param url
     * @return
     */
    public static String httpMethodGetExchange(String url, RestTemplate restTemplate, String token) {
        System.out.println("url:" + url);
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(Dict.PRIVATE_TOKEN, token);
        //http body配置
        /*MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("search", "CommonModules");
        HttpEntity httpEntity = new HttpEntity(httpHeaders，multiValueMap);*/
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> result;
        String body;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            body = result.getBody();
        } catch (Exception e) {
            return null;
        }
        return body;
    }

    /**
     * json字符串转list集合
     *
     * @param body
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> stringToList(String body) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(body);
        if (jsonArray.length() > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = objectMapper.readValue(jsonObject.toString(), Map.class);
                list.add(map);
            }
        }
        return list;
    }

}

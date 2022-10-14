package com.spdb.fdev.fdemand.base.utils;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class IpmpUtils {
    @Autowired
    RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(IpmpUtils.class);

    public Object send(Map sendMap, String url) throws Exception {
        JSONObject param = (JSONObject) JSONObject.toJSON(sendMap);
        ResponseEntity responseEntity = sendPOST(url, param);
        return responseEntity.getBody();
    }

    public ResponseEntity sendPOST(String url, JSONObject param) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
        ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
        return postForEntity;
    }

    public Object send(List sendList, String url) throws Exception {
        ResponseEntity responseEntity = sendPOST(url, sendList);
        return responseEntity.getBody();
    }

    public ResponseEntity sendPOST(String url, List sendList) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        HttpEntity<Object> request = new HttpEntity<Object>(sendList, httpHeaders);
        ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
        return postForEntity;
    }

    public Object sendGet(String url) throws Exception {
        ResponseEntity<String> result = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        httpHeaders.add("Content-Type", "application/json");
        result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return result.getBody();
    }

    //ipmp时间截取年月日
    public IpmpUnit subTimeStr(IpmpUnit ipmpUnit){
        String planDevelopDate = ipmpUnit.getPlanDevelopDate();
        String planProductDate = ipmpUnit.getPlanProductDate();
        String planTestStartDate = ipmpUnit.getPlanTestStartDate();
        String planTestFinishDate = ipmpUnit.getPlanTestFinishDate();
        String acturalDevelopDate = ipmpUnit.getActuralDevelopDate();
        String acturalProductDate = ipmpUnit.getActuralProductDate();
        String acturalTestStartDate = ipmpUnit.getActuralTestStartDate();
        String acturalTestFinishDate = ipmpUnit.getActuralTestFinishDate();
        if(!CommonUtils.isNullOrEmpty(planDevelopDate)){
            ipmpUnit.setPlanDevelopDate(planDevelopDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(planProductDate)){
            ipmpUnit.setPlanProductDate(planProductDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(planTestStartDate)){
            ipmpUnit.setPlanTestStartDate(planTestStartDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(planTestFinishDate)){
            ipmpUnit.setPlanTestFinishDate(planTestFinishDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(acturalDevelopDate)){
            ipmpUnit.setActuralDevelopDate(acturalDevelopDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(acturalProductDate)){
            ipmpUnit.setActuralProductDate(acturalProductDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(acturalTestStartDate)){
            ipmpUnit.setActuralTestStartDate(acturalTestStartDate.substring(0,10));
        }
        if(!CommonUtils.isNullOrEmpty(acturalTestFinishDate)){
            ipmpUnit.setActuralTestFinishDate(acturalTestFinishDate.substring(0,10));
        }
        return ipmpUnit;
    }
}

package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.IpmpService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IpmpServiceImpl implements IpmpService {

    private Logger logger = LoggerFactory.getLogger(IpmpServiceImpl.class);

    @Value("${ipmp.url}")
    private String ipmpUrl;

    @Resource(name = "getRestTemplate")
    RestTemplate restTemplate;


    @Override
    public List<Map<String, String>> queryIpmpSystemInfo() {
        //1.发送ipmp系统获取ipmp系统信息数据
        String url = ipmpUrl + "/ipmp/exchange/gateway";
        Map<String, Object> sendIpmp = new HashMap();

        Map<String, Object> commonMap = new HashMap<>();
        commonMap.put(Dict.APPNO, Constants.APPNO);
        commonMap.put(Dict.METHOD, Constants.METHOD);
        commonMap.put(Dict.APPKEY, Constants.APPKEY);
        commonMap.put(Dict.ISAUTH, true);
        commonMap.put(Dict.VERSION, Constants.VERSION);
        sendIpmp.put("common", commonMap);

        Map<String, String> reqMap = new HashMap<>();
        reqMap.put(Dict.CATEGORY, Constants.CATEGORY);
        sendIpmp.put("request", reqMap);
        logger.info("sendData:" + url + "&&" +  JSONObject.toJSONString(sendIpmp));
        Object result = sendIpmpSystem(url, sendIpmp);
        Map parse = (Map) JSONObject.parse((String) result);
        List<Map<String, String>> ipmpList = (List<Map<String, String>>) parse.get(Dict.DATA);
        return ipmpList;
    }

    private Object sendIpmpSystem(String url, Map send) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        JSONObject parse = JSONObject.parseObject(JSON.toJSONString(send));
        HttpEntity<Object> request = new HttpEntity<>(parse, httpHeaders);
        Object returnData = null;
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
            returnData = result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("send systemBaseInfo/getSystemBaseInfo trans error!", e);
            throw new FdevException(ErrorConstants.IPMP_ERROR);
        }
        return returnData;
    }
}

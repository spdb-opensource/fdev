package com.mantis.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mantis.dao.MantisUserDao;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.service.IMantisUserService;
import com.mantis.util.MyUtil;
import com.mantis.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class MantisUserServiceImpl implements IMantisUserService {

    private static final Logger logger = LoggerFactory.getLogger(MantisUserServiceImpl.class);
    @Autowired
    private RestTemplate restTemplate;
    @Value("${manits.admin.token}")
    private String mantis_token;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private MantisUserDao mantisUserDao;
    @Autowired
    private MyUtil myUtil;



    @Override
    public void regeisterMantisUser(String userName, String password, String userNameCn,  String email) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Dict.USERNAME, userName);
        map.put(Dict.PASSWORD, password);
        map.put(Dict.REAL_NAME, userNameCn);
        map.put(Dict.EMAIL, email);
        Map<String, String> levelMap = new HashMap<String, String>();
        levelMap.put(Dict.NAME, Dict.DEVELOPER);
        map.put(Dict.ACCESS_LEVEL, levelMap);
        map.put(Dict.ENABLED, true);
        map.put(Dict.PROTECTED, false);
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(restTransport.getUrl("mantis.add.user"), sendDataPrepare(mantis_token,map), String.class);
            //将用户的生产问题提交权限取消
        } catch (Exception e) {
            logger.error("add mantis user error,user ="+ userName);
            logger.error(""+e);
            throw  new FtmsException(ErrorConstants.ADD_MANTIS_USER_ERROR);
        }
        cancelProjectAccessLevel(userName, myUtil.getProIssueProjectId(), "10");
    }


    @Override
    public void cancelProjectAccessLevel(String userName, String projectId, String level) throws Exception {
        if(Util.isNullOrEmpty(queryProjectUserLevel(myUtil.getProIssueProjectId(), userName))){
            mantisUserDao.addProjectAccessLevel(userName, myUtil.getProIssueProjectId(), "10");
        }else{
            mantisUserDao.changeProjectAccessLevel(userName, myUtil.getProIssueProjectId(), "10");
        }
    }

    @Override
    public void cancelAllUserProjectLevel() throws Exception {
        List<String> userNames = mantisUserDao.queryAllMantisUser();
        for (String userName : userNames) {
            try{
            cancelProjectAccessLevel(userName,  myUtil.getProIssueProjectId(), "10");
            }catch (Exception e){
                logger.error("changer mantis user project accsee level error, username = " +userName);
                logger.error("e:"+e);
            }
        }
    }

    @Override
    public void queryAllMantisUser() throws Exception {
        List<String> users = mantisUserDao.queryAllMantisUser();
    }

    @Override
    public String queryProjectUserLevel(String projectId, String userName) throws Exception {
        return mantisUserDao.queryProjectUserLevel(projectId, userName);
    }

    public HttpEntity<Object> sendDataPrepare(String token, Map sendMap) {
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

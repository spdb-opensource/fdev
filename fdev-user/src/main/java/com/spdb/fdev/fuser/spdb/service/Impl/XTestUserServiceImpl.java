package com.spdb.fdev.fuser.spdb.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IXTestUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.TestManagerUser;
import com.spdb.fdev.fuser.spdb.service.IXTestUserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class XTestUserServiceImpl implements IXTestUserService {
    @Autowired
    private IXTestUserDao xTestUserDao;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${xtest.api.url}")
    private String xTestUrl;
    @Value("${xtest.getTestManagerInfo}")
    private String getTestManagerInfo;

    @Override
    public void syncTestManagerInfo() {
        //调用云测试接口查询用户信息

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(xTestUrl + getTestManagerInfo, request,String.class);
        Map responseMap = JSONObject.parseObject(response.getBody(), Map.class);
        boolean result = (boolean) responseMap.get("result");
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) responseMap.get(Dict.MSG));
        }
        List<Map> list = (List<Map>) responseMap.get("content");
        if( !CommonUtils.isNullOrEmpty(list) ) {
            List<TestManagerUser> testManagerUserList = new ArrayList<>();
            list.stream().forEach(map -> {
                TestManagerUser testManagerUser = new TestManagerUser();
                ObjectId objectId = new ObjectId();
                testManagerUser.set_id(objectId);
                testManagerUser.setUser_name_en((String) map.get(Dict.USER_NAME_EN));
                testManagerUser.setUser_name_cn((String) map.get(Dict.USER_NAME_CN));
                testManagerUser.setUser_email((String) map.get("user_email"));
                testManagerUserList.add(testManagerUser);
            });
            xTestUserDao.addTestManagerInfo(testManagerUserList);
        }
    }

    @Override
    public List<TestManagerUser> getTestManagerInfo(String userNameEN) {
        return  xTestUserDao.getTestManagerInfo(userNameEN);
    }
}

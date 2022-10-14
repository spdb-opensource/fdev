package com.test.controller;


import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.test.dict.Dict;
import com.test.dict.ErrorConstants;
import com.test.entity.User;
import com.test.service.UserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    
    @RequestValidate(NotEmptyFields = {Dict.USERTOKEN})
    @PostMapping(value = "/logout")
    public JsonResult logout() throws Exception {
    	String userTokenKey = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
    			.getRequest().getHeader("userToken");
    	String redis_token = "tuser.LoginToken" + userTokenKey;
    	redisTemplate.delete(redis_token);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/login")
    public JsonResult login(@RequestBody Map<String,String> requestMap) throws Exception {
        String userNameEn = requestMap.get(Dict.USER_NAME_EN);
        String password = requestMap.get(Dict.PASSWORD);
        return JsonResultUtil.buildSuccess(userService.login(userNameEn, password));
    }


}

package com.test.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.test.dict.Dict;
import com.test.dict.ErrorConstants;
import com.test.service.FdevUserService;
import com.test.service.UserService;
import com.test.service.impl.UserServiceImpl;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.utils.HttpClientUtils;
import com.test.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@RequestMapping("/oauth")
@RestController
@RefreshScope
public class OauthController {

	@Autowired
	private UserServiceImpl userServiceImpl;
	@Autowired
	private RestTransport restTransport;
    //当前系统名
    @Value("${oauth.name}")
    private String oauthName;
    //回调页面
	@Value("${oauth.host}")
	private String oauthHost;
    @Value("${testmanage.init.password}")
    private String initPassword;

    public static final int MAX_AGE = 3600*24*5;
    
    @Autowired
    private FdevUserService fdevUserService;

    private static final Logger logger = LoggerFactory.getLogger(OauthController.class);
    @Autowired
    UserService userService;

    @PostMapping(value="/oauthGetToken")
    public JsonResult toFdevAuth(HttpServletRequest request) {
        String result;
        //发送post请求
        try {
        	result = fdevUserService.getUserToken(oauthHost, oauthName);
            logger.info("**** 获取Token" + String.valueOf(result));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.LOGIN_ERROR,new String[]{"获取Token失败 -oauthGetToken"});
        }
        return JsonResultUtil.buildSuccess(result);
        
    }
    
	@PostMapping("/oauthGetAuthorization")
	public JsonResult getAuthorization(@RequestBody Map<String,String> requestMap,HttpServletResponse response) throws Exception {
		
		JSONObject param1 = new JSONObject();
		JSONObject param2 = new JSONObject();
		ResponseEntity<String> responseEntity = null;
		String authorization = null;
		Map map = null;
		//获取token
		String token= requestMap.get(Dict.TOKEN);
		if(MyUtils.isEmpty(token)) {
			logger.error("can't get token");
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		param1.put(Dict.TOKEN, token);		
		//发送post请求
		try {
			responseEntity = HttpClientUtils.sendPOST(restTransport.getUrl("get.auth.url"),param1);//根据授权凭证获取登录凭证
		} catch (Exception e) {
			logger.error("get authorization error,"+e.getMessage());
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		JSONObject jsonobject = JSONObject.parseObject(responseEntity.getBody()); 
		if(MyUtils.isEmpty(jsonobject)) { 
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		//将response转为map
		map = JSONObject.toJavaObject(jsonobject,Map.class); 
		if(MyUtils.isEmpty(map)) {
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		//取authorization
		authorization = (String) map.get(Dict.DATA);
		if(MyUtils.isEmpty(authorization)) {
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		
		//发送post请求
		param2.put("Authorization", authorization);
		try {
			responseEntity = HttpClientUtils.sendPOST(restTransport.getUrl("get.user.url"), param2);//根据登录凭证获取当前用户信息
		} catch (Exception e) {
			logger.error("get fdev user error,"+e.getMessage());
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		JSONObject parseObject = JSONObject.parseObject(responseEntity.getBody());
		if(MyUtils.isEmpty(parseObject)) {
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		JSONArray data =parseObject.getJSONArray(Dict.DATA);
		if(MyUtils.isEmpty(data)) {
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		Map<String,Object> user = (Map<String,Object>)data.get(0);
		if(MyUtils.isEmpty(user)) {
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}
		Map result = null;
		try{
			result = userService.loginSaveUser(user);
		}catch(Exception e){
			logger.error("redis save user error");
			throw new FtmsException(ErrorConstants.FDEV_LOGIN_FAILURE);
		}

		return JsonResultUtil.buildSuccess(result);
	}
}

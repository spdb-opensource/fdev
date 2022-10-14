package com.fdev.docmanage.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fdev.docmanage.entity.UserInfo;
import com.fdev.docmanage.service.UserInfoService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "用户及文档信息")
@RequestMapping("/api/userInfo")
@RestController
public class UserInfoController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

	@Resource
    private UserInfoService userInfoService;
    
	@ApiOperation(value = "查询用户及文件信息")
    @RequestMapping(value = "/queryInfo", method = RequestMethod.POST)
    public JsonResult queryInfo(@RequestBody Map map) throws Exception{
		List<UserInfo> list = userInfoService.queryUserInfo(map);
        return JsonResultUtil.buildSuccess(list);
    }


}

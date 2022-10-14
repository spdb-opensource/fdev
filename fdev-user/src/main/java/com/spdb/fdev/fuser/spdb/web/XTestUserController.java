package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.IXTestUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class XTestUserController {
    @Autowired
    private IXTestUserService xTestUserService;

    @RequestMapping(value = "/syncTestManagerInfo", method = RequestMethod.POST)
    @ApiOperation(value = "同步测试经理信息")
    public JsonResult syncTestManagerInfo() throws Exception {
        xTestUserService.syncTestManagerInfo();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/getTestManagerInfo", method = RequestMethod.POST)
    @ApiOperation(value = "查询测试经理信息")
    public JsonResult getTestManagerInfo(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(xTestUserService.getTestManagerInfo(param.get(Dict.USER_NAME_EN)));
    }
}

package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.IIpmpUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ipmpUser")
public class IpmpUserController {
    @Autowired
    private IIpmpUserService ipmpUserService;

    @RequestMapping(value = "/syncAllIpmpUser", method = RequestMethod.POST)
    @ApiOperation(value = "同步全量人员信息")
    public JsonResult syncAllIpmpUser() throws Exception {
        ipmpUserService.syncAllIpmpUser();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryIpmpUser", method = RequestMethod.POST)
    @ApiOperation(value = "查询人员信息")
    public JsonResult queryIpmpUser(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUserService.queryIpmpUser(param.get(Dict.USER_NAME_EN)));
    }
}

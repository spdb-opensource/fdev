package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.IIpmpDeptService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ipmpDept")
public class IpmpDeptController {
    @Autowired
    private IIpmpDeptService ipmpDeptService;

    @RequestMapping(value = "/syncAllIpmpDept", method = RequestMethod.POST)
    @ApiOperation(value = "同步全量组织机构信息")
    public JsonResult syncAllIpmpDept() throws Exception {
        ipmpDeptService.syncAllIpmpDept();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryIpmpDept", method = RequestMethod.POST)
    @ApiOperation(value = "查询组织机构信息")
    public JsonResult queryIpmpDept(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpDeptService.queryIpmpDept(param.get(Dict.DEPT_ID)));
    }
}

package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.service.IIpmpProjectService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class IpmpProjectController {
    @Autowired
    private IIpmpProjectService ipmpProjectService;

    @RequestMapping(value = "/syncAllIpmpProject", method = RequestMethod.POST)
    @ApiOperation(value = "同步全量项目和任务集信息")
    public JsonResult syncAllIpmpProject() throws Exception {
        ipmpProjectService.syncAllIpmpProject();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryIpmpProject", method = RequestMethod.POST)
    @ApiOperation(value = "查询项目和任务集信息")
    public JsonResult queryIpmpProject(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpProjectService.queryIpmpProject(param.get(Dict.PROJECT_NO)));
    }
}

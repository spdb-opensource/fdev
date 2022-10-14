package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.entity.report.DashBoardUserConfig;
import com.spdb.fdev.freport.spdb.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "度量公共")
@RestController
@RequestMapping(value = "/api/dashboard")
public class DashboardController {

    private static Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @ApiOperation(value = "仪表盘 - 查询用户自定义看板信息")
    @RequestMapping(value = "/queryUserConfig", method = RequestMethod.POST)
    public JsonResult queryUserConfig() throws Exception {
        return JsonResultUtil.buildSuccess(dashboardService.queryUserConfig());
    }

    @ApiOperation(value = "仪表盘 - 新增用户自定义看板信息")
    @RequestMapping(value = "/addUserConfig", method = RequestMethod.POST)
    public JsonResult addUserConfig(@RequestBody DashBoardUserConfig userConfig) throws Exception {
        dashboardService.addUserConfig(userConfig.getConfigs());
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "查询默认组id集合")
    @RequestMapping(value = "/queryDefaultGroupIds", method = RequestMethod.POST)
    public JsonResult queryDefaultGroupIds() throws Exception {
        return JsonResultUtil.buildSuccess(dashboardService.queryDefaultGroupIds());
    }

}

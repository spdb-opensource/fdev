package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.dto.StatisticsDto;
import com.spdb.fdev.freport.spdb.service.ResourceManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "资源管理")
@RestController
@RequestMapping(value = "/api/resourceManage")
public class ResourceManageController {

    @Autowired
    private ResourceManageService resourceManageService;

    @ApiOperation(value = "统计报表 - 查询项目组规模")
    @RequestMapping(value = "/queryPersonStatistics", method = RequestMethod.POST)
    public JsonResult queryPersonStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(resourceManageService.queryPersonStatistics(dto.getGroupIds(), dto.getIncludeChild()));
    }

    @ApiOperation(value = "统计报表 - 查询人员闲置统计")
    @RequestMapping(value = "/queryPersonFreeStatistics", method = RequestMethod.POST)
    public JsonResult queryPersonFreeStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(resourceManageService.queryPersonFreeStatistics(dto.getGroupIds(), dto.getIncludeChild()));
    }
}

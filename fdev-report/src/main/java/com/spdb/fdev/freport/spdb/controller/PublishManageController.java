package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.dto.DashboardDto;
import com.spdb.fdev.freport.spdb.service.PublishManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "投产管理")
@RequestMapping(value = "/api/publishManage")
public class PublishManageController {

    @Autowired
    private PublishManageService publishManageService;

    @ApiOperation(value = "仪表盘 - 投产数量变化趋势")
    @RequestMapping(value = "/queryPublishCountTrend", method = RequestMethod.POST)
    public JsonResult queryPublishCountTrend(@RequestBody DashboardDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.publishManageService.queryPublishCountTrend(dto.getCycle()));
    }
}

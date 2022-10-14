package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.dto.DashboardDto;
import com.spdb.fdev.freport.spdb.service.QualityManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "质量管理")
@RestController
@RequestMapping(value = "/api/qualityManage")
public class QualityManageController {

    private static Logger logger = LoggerFactory.getLogger(QualityManageController.class);

    @Autowired
    private QualityManageService qualityManageService;

    @ApiOperation(value = "仪表盘 - 生产问题变化趋势")
    @RequestMapping(value = "/queryProIssueTrend", method = RequestMethod.POST)
    public JsonResult queryProIssueTrend(@RequestBody DashboardDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.qualityManageService.queryProIssueTrend(dto.getCycle()));
    }
}

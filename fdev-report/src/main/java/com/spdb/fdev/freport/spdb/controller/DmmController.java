package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.dto.DmmDto;
import com.spdb.fdev.freport.spdb.service.DmmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "dmm接口模块")
@RestController
@RequestMapping(value = "/api/dmm")
public class DmmController {

    @Autowired
    private DmmService dmmService;

    @ApiOperation(value = "查询集成率")
    @RequestMapping(value = "/queryIntegrationRate", method = RequestMethod.POST)
    public JsonResult queryIntegrationRate(@RequestBody DmmDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(dmmService.queryIntegrationRate(dto));
    }
}

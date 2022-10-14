package com.spdb.fdev.fdevapp.spdb.web;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;
import com.spdb.fdev.fdevapp.spdb.service.IDeployNetService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(tags = "应用部署网段实体接口")
@RequestMapping("/api/deployNet")
@RestController
public class DeployNetController {

    @Autowired
    private IDeployNetService deployNetService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @NoNull(require = {Dict.ID, Dict.NAME}, parameter = DeployNet.class)
    public JsonResult save(@RequestBody @ApiParam DeployNet deployNet) throws Exception {
        return JsonResultUtil.buildSuccess(this.deployNetService.save(deployNet));
    }

    @RequestMapping(value = "/findbyid", method = RequestMethod.POST)
    @NoNull(require = {Dict.ID}, parameter = DeployNet.class)
    public JsonResult findById(@RequestBody @ApiParam DeployNet deployNet) throws Exception {
        return JsonResultUtil.buildSuccess(this.deployNetService.findById(deployNet.getId()));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @NoNull(require = {Dict.ID}, parameter = DeployNet.class)
    public JsonResult update(@RequestBody @ApiParam DeployNet deployNet) throws Exception {
        return JsonResultUtil.buildSuccess(this.deployNetService.update(deployNet));
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public JsonResult query(@RequestBody @ApiParam DeployNet deployNet) throws Exception {
        return JsonResultUtil.buildSuccess(this.deployNetService.query(deployNet));
    }

}

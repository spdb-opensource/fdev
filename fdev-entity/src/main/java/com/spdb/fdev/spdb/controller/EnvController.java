package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.entity.Env;
import com.spdb.fdev.spdb.entity.EnvType;
import com.spdb.fdev.spdb.service.IEnvService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @ClassName EnvController
 * @DESCRIPTION 环境控制层
 * @Author xxx
 * @Date 2021/5/8 14:04
 * @Version 1.0
 */
@RequestMapping("/api/env")
@RestController
public class EnvController {

    @Autowired
    IEnvService envService;

    @ApiOperation(value = "查询环境列表")
    @PostMapping(value = "/queryList")
    public JsonResult queryList(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<Env> envList = envService.queryList(requestParam);
        return JsonResultUtil.buildSuccess(envList);
    }

    @ApiOperation(value = "新增环境")
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody Map<String, Object> requestParam) throws Exception {
        envService.add(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }



    @ApiOperation(value = "新增环境类型")
    @PostMapping(value = "/addEnvType")
    public JsonResult addEnvType(@RequestBody Map<String, Object> requestParam) throws Exception {
        envService.addEnvType(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }



    @ApiOperation(value = "查询环境类型列表")
    @PostMapping(value = "/queryEnvTypeList")
    public JsonResult queryEnvTypeList(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<EnvType> envTypeList = envService.queryEnvTypeList(requestParam);
        return JsonResultUtil.buildSuccess(envTypeList);
    }

}

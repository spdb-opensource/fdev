package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.spdb.entity.EntityTemplate;
import com.spdb.fdev.spdb.service.IEntityTemplateService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @ClassName EntityTemplate
 * @DESCRIPTION 实体模板
 * @Author xxx
 * @Date 2021/5/8 15:44
 * @Version 1.0
 */
@RequestMapping("/api/template")
@RestController
public class EntityTemplateController {

    @Autowired
    IEntityTemplateService entityTemplateService;

    @ApiOperation(value = "批量新增环境")
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody Map<String, Object> requestParam) throws Exception {
        entityTemplateService.add(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }

    @ApiOperation(value = "查询实体模板列表")
    @PostMapping(value = "/queryTemplate")
    public JsonResult queryTemplate(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<EntityTemplate> result = entityTemplateService.queryList(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "根据id查询实体模板详情")
    @RequestValidate(NotEmptyFields = { Dict.ID })
    @PostMapping(value = "/queryById")
    public JsonResult queryById(@RequestBody Map<String, Object> requestParam) throws Exception {
        EntityTemplate entityTemplate = entityTemplateService.queryById(requestParam);
        return JsonResultUtil.buildSuccess(entityTemplate);
    }


}

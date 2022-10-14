package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.entity.OutSideTemplate;
import com.spdb.fdev.spdb.service.IOutsideTemplateService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/outsideTemplate")
public class OutsideTemplateController {

    @Autowired
    private IOutsideTemplateService outSideTemplate;

    /**
     * 查询外部参数配置模版
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/query")
    public JsonResult queryOutsideTemplate(@RequestBody  OutSideTemplate outSideTemplate) throws Exception {
        return JsonResultUtil.buildSuccess(this.outSideTemplate.queryOutsideTemplate(outSideTemplate));
    }
}

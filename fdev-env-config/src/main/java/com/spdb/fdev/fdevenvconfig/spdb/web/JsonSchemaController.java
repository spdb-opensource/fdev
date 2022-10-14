package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevenvconfig.spdb.service.JsonSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/jsonSchema")
public class JsonSchemaController {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    /**
     * 获取高级属性模板
     *
     * @return
     */
    @PostMapping("/getJsonSchema")
    public JsonResult getJsonSchema(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(jsonSchemaService.getJsonSchema(requestMap));
    }

}

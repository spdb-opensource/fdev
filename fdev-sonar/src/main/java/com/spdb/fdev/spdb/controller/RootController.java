package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/root")
public class RootController {

    private static Logger logger = LoggerFactory.getLogger(RootController.class);

    @PostMapping(value = "/")
    public JsonResult query(@RequestBody @ApiParam Map<String, String> requestParam) {
        return JsonResultUtil.buildSuccess();
    }

}

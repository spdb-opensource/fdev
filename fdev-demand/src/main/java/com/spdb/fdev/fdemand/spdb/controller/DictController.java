package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import com.spdb.fdev.fdemand.spdb.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Set;

/**
 * @author zhanghp4
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @PostMapping("/query")
    public JsonResult query(@RequestBody DictEntity dictEntity) throws Exception {
        return JsonResultUtil.buildSuccess(dictService.query(dictEntity));
    }

    @PostMapping("/queryByTypes")
    public JsonResult queryByTypes(@RequestBody HashMap<String, Set<String>> param) throws Exception {
        return JsonResultUtil.buildSuccess(dictService.queryByTypes(param.get("types")));
    }

}

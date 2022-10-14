package com.spdb.fdev.codeReview.spdb.controller;

import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.entity.DictEntity;
import com.spdb.fdev.codeReview.spdb.service.IDictService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@CrossOrigin
@RestController()
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    IDictService dictService;

    @PostMapping("/add")
    @NoNull(require = {Dict.TYPE,Dict.TYPNAME,Dict.KEY,Dict.VALUE},parameter = DictEntity.class)
    public JsonResult add(@RequestBody DictEntity dictEntity){
        dictService.add(dictEntity);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/query")
    public JsonResult query(@RequestBody Map param){
        return JsonResultUtil.buildSuccess(dictService.query(param));
    }
}

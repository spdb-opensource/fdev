package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.gitlabwork.service.IStatisticCodeLineService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/12 10:16
 */
@RequestMapping("/api/statisticCodeLine")
@RestController
public class StatisticCodeLineController {

    @Autowired
    IStatisticCodeLineService statisticCodeLineService;

    @RequestMapping(method = RequestMethod.POST, value = "/query")
    public JsonResult query(@RequestBody Map<String, String> param) throws Exception {
        String month = null;
        if (param.containsKey(Dict.MONTH)){
            month = param.get(Dict.MONTH);
        }
        if (StringUtils.isNotBlank(month)){
            return JsonResultUtil.buildSuccess(statisticCodeLineService.query(month));
        }
        return JsonResultUtil.buildSuccess(statisticCodeLineService.query());
    }
}

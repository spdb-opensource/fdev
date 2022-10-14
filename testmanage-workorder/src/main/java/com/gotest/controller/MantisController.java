package com.gotest.controller;

import com.gotest.dict.Dict;
import com.gotest.service.MantisService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 审核工单 控制器层
 */
@SuppressWarnings("all")
@RestController
@RequestMapping(value="/mantis")
public class MantisController {

    private Logger logger = LoggerFactory.getLogger(MantisController.class);

    @Autowired
    private MantisService mantisService;


    /**
     * 根据主任务编号s，查询工单编号s
     */
    @RequestMapping("/queryWorkNosByTaskNos")
    public JsonResult queryWorkNosByTaskNos(@RequestBody Map map) throws Exception {

        List<String> list = (List<String>) map.get(Dict.TASKLIST);
        list.remove(null);
        if (Util.isNullOrEmpty(list)){
            return JsonResultUtil.buildSuccess(null);
        }
        return JsonResultUtil.buildSuccess(mantisService.queryWorkNoByTaskNo(list));
    }

}

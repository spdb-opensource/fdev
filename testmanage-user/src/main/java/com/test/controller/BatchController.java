package com.test.controller;

import com.test.service.BatchService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private BatchService batchService;

    @RequestMapping("/batchConfigUser")
    public JsonResult batchConfigUser(@RequestBody Map map) throws Exception {
        batchService.batchConfigUser();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping("/batchUserRoleLevelMantisToken")
    public JsonResult batchUserRoleLevelMantisToken(@RequestBody Map map) throws Exception {
        batchService.batchUserRoleLevelMantisToken();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 定时跑批老fdev新增用户至新fdev
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/timingSyncFuser")
    public JsonResult timingSyncFuser() throws Exception {
        return JsonResultUtil.buildSuccess(batchService.timingSyncFuser());
    }
}

package com.spdb.fdev.codeReview.spdb.controller;

import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.entity.CodeLog;
import com.spdb.fdev.codeReview.spdb.service.ILogService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@CrossOrigin
@RestController
@RequestMapping("/api/log")
public class LogController {

    @Autowired
    ILogService logService;

    @PostMapping("/queryLogs")
    @NoNull(require = {Dict.ORDERID},parameter = CodeLog.class)
    public JsonResult queryLogs(@RequestBody CodeLog codeLog) throws Exception {
        return JsonResultUtil.buildSuccess(logService.queryLogs(codeLog.getOrderId()));
    }
}

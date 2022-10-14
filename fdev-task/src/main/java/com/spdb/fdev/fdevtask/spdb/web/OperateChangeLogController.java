package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.OperateChangeLogService;
import com.spdb.fdev.fdevtask.spdb.service.OperateRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-18 13:25
 **/
@Api(tags = "任务动态接口")
@RequestMapping("/api/operateChangeLog")
@RestController
@RefreshScope
public class OperateChangeLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    OperateChangeLogService operateChangeLogService;

    @OperateRecord(operateDiscribe = "任务模块-查询任务动态")
    @ApiOperation(value = "查询任务动态")
    @RequestMapping(value = "/getTaskLog", method = RequestMethod.POST)
    public JsonResult createTask(@RequestBody Map request) throws Exception {
        Object taskIdObject = request.get(Dict.TASKID);
        if (CommonUtils.isNullOrEmpty(taskIdObject)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"taskId不能为空"});
        }
        return JsonResultUtil.buildSuccess(operateChangeLogService.getOperateChangeLogByTaskId((String)taskIdObject));
    }
}

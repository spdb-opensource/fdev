package com.spdb.fdev.fdevtask.spdb.web;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:
 * @author: c-jiangl2
 * @create: 2021-01-25 13:27
 **/
@Api(tags = "流程任务接口")
@RequestMapping("/api/processTask")
@RestController
@RefreshScope
public class TaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private TaskService taskService;

    @OperateRecord(operateDiscribe = "任务模块-新增任务")
    @ApiOperation(value = "新增任务")
    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    public JsonResult createTask(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(taskService.createTask(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-修改任务")
    @ApiOperation(value = "修改任务")
    @RequestMapping(value = "/updateTask", method = RequestMethod.POST)
    public JsonResult updateTask(@RequestBody Map request) throws Exception {
        taskService.updateTask(request);
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe = "任务模块-任务详情")
    @ApiOperation(value = "任务详情")
    @RequestMapping(value = "/getTaskDetail", method = RequestMethod.POST)
    public JsonResult getTaskDetail(@RequestBody Map request)throws Exception{
        Map taskList = taskService.getTaskDetail(request);
        return JsonResultUtil.buildSuccess(taskList);
    }

    @OperateRecord(operateDiscribe = "任务模块-删除任务")
    @ApiOperation(value = "删除任务")
    @RequestMapping(value = "/deleteTask", method = RequestMethod.POST)
    public JsonResult deleteTask(@RequestBody Map request) throws Exception {
        taskService.deleteTask(request);
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe = "任务模块-获取任务列表")
    @ApiOperation(value = "获取任务列表")
    @RequestMapping(value = "/queryUserTask", method = RequestMethod.POST)
    public JsonResult queryUserTask(@RequestBody Map request)throws Exception{
        Map taskList = taskService.getTaskList(request);
        return JsonResultUtil.buildSuccess(taskList);
    }


    @OperateRecord(operateDiscribe = "任务模块-获取任务信息")
    @ApiOperation(value = "获取任务信息")
    @RequestMapping(value = "/getTaskById", method = RequestMethod.POST)
    public JsonResult getTaskById(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getTaskById(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-更新或上传")
    @ApiOperation(value = "更新或上传")
    @RequestMapping(value = "/updateTaskDoc", method = RequestMethod.POST)
    public JsonResult updateTaskDoc(@RequestParam(value = "files") MultipartFile[] files,
                                    @RequestParam(value = "taskId") String taskId,
                                    @RequestParam(value = "datas") String datas
    )throws Exception{
        List<Map<String, String>> doc = (List) JSONArray.parseArray(datas);
        return JsonResultUtil.buildSuccess(taskService.uploadFile(taskId, doc, files));
    }

    @ApiOperation(value = "任务doc详情")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryDocDetail", method = RequestMethod.POST)
    public JsonResult queryDocDetail(@RequestBody @ApiParam Map request) throws Exception {
        Map result = taskService.queryDocDetail((String)request.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(result)) {
            return JsonResultUtil.buildSuccess(null);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "任务模块-获取用户所有任务流程状态")
    @ApiOperation(value = "获取用户所有任务流程状态")
    @RequestMapping(value = "/getProcessList", method = RequestMethod.POST)
    public JsonResult getProcessList(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getProcessList(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-改变任务实际结束时间")
    @ApiOperation(value = "改变任务实际结束时间")
    @RequestMapping(value = "/updateEndTime", method = RequestMethod.POST)
    public JsonResult updateEndTime(@RequestBody Map request)throws Exception{
        taskService.updateEndTime(request);
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe = "任务模块-改变任务状态")
    @ApiOperation(value = "改变任务状态")
    @RequestMapping(value = "/changeTaskStatus", method = RequestMethod.POST)
    public JsonResult changeTaskStatus(@RequestBody Map request)throws Exception{
        taskService.changeTaskStatus(request);
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe = "任务模块-根据任务ID列表查询FDEV研发单元列表")
    @ApiOperation(value = "根据任务ID列表查询FDEV研发单元列表")
    @RequestMapping(value = "/getImplUnitNoByIds", method = RequestMethod.POST)
    public JsonResult getImplUnitNoByIds(@RequestBody Map request)throws Exception{
        taskService.getImplUnitNoByIds(request);
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe = "任务模块-根据应用id获取所有该应用任务")
    @ApiOperation(value = "根据应用id获取所有该应用任务")
    @RequestMapping(value = "/queryTaskByApplicationId", method = RequestMethod.POST)
    public JsonResult queryTaskByApplicationId(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryTaskByApplicationId(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-根据研发单元id查询是否有非废弃的任务")
    @ApiOperation(value = "根据研发单元id查询是否有非废弃的任务")
    @RequestMapping(value = "/queryTaskByFdevUnitId", method = RequestMethod.POST)
    public JsonResult queryTaskByFdevUnitId(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryTaskByFdevUnitId(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-根据需求id查询任务")
    @ApiOperation(value = "根据需求id查询任务")
    @RequestMapping(value = "/queryTaskByRequirementId", method = RequestMethod.POST)
    public JsonResult queryTaskByRequirementId(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryTaskByRequirementId(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-根据userId查询任务")
    @ApiOperation(value = "根据userId查询任务")
    @RequestMapping(value = "/queryTaskByUserId", method = RequestMethod.POST)
    public JsonResult queryTaskByUserId(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryTaskByUserId(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-修改任务(模块调用)")
    @ApiOperation(value = "修改任务(模块调用)")
    @RequestMapping(value = "/updateTaskInner", method = RequestMethod.POST)
    public JsonResult updateTaskInner(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.updateTaskInner(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-根据研发单元ids查询任务列表")
    @ApiOperation(value = "根据研发单元ids查询任务列表")
    @RequestMapping(value = "/querTaskListByImplUnitIds", method = RequestMethod.POST)
    public JsonResult querTaskListByImplUnitIds(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.querTaskListByImplUnitIds(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-根据版本ids查询版本下的任务列表")
    @ApiOperation(value = "根据版本ids查询版本下的任务列表")
    @RequestMapping(value = "/queryTaskByVersionIds", method = RequestMethod.POST)
    public JsonResult queryTaskByVersionIds(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryTaskByVersionIds(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-更新挂载状态")
    @ApiOperation(value = "更新挂载状态")
    @RequestMapping(value = "/updateMountStatus", method = RequestMethod.POST)
    public JsonResult updateMountStatus(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.updateMountStatus(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-获取任务信息")
    @ApiOperation(value = "获取任务信息")
    @RequestMapping(value = "/getTaskByTask", method = RequestMethod.POST)
    public JsonResult getTaskByTask(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getTaskByTask(request));
    }

    @OperateRecord(operateDiscribe = "任务模块-获取任务列表（需求下调用）")
    @ApiOperation(value = "获取任务列表（需求下调用）")
    @RequestMapping(value = "/getTaskByRequirementId", method = RequestMethod.POST)
    public JsonResult getTaskByRequirementId(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getTaskByRequirementId(request));
    }

    @OperateRecord(operateDiscribe = "获得任务信息byIds-获取任务信息")
    @ApiOperation(value = "获得任务信息byIds")
    @RequestMapping(value = "/getTaskByIds", method = RequestMethod.POST)
    public JsonResult getTaskByIds(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getTaskByIds(request));
    }

    @ApiOperation(value = "根据流程和组查询是否有未结束的任务")
    @RequestMapping(value = "/queryNotEndTask", method = RequestMethod.POST)
    public JsonResult queryNotEndTask(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.queryNotEndTask((String)request.get(Dict.PROCESS_ID),(String)request.get(Dict.GROUPID)));
    }

    @ApiOperation(value = "根据研发单元id列表查询任务完成情况")
    @RequestMapping(value = "/getCompleteTaskByImplIds", method = RequestMethod.POST)
    public JsonResult getCompleteTaskByImplIds(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.getCompleteTaskByImplIds(request));
    }

    @ApiOperation(value = "批量更新挂载版本相同开发分支确认信息")
    @RequestMapping(value = "/updateMountSameFBConfirms", method = RequestMethod.POST)
    public JsonResult updateMountSameFBConfirms(@RequestBody Map request)throws Exception{
        return JsonResultUtil.buildSuccess(taskService.updateMountSameFBConfirms(request));
    }
}

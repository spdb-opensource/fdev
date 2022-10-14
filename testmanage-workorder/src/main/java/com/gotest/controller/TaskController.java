package com.gotest.controller;

import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.TaskListService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/task")
public class TaskController {

    @Autowired
    private TaskListService taskListService;

    private Logger logger = LoggerFactory.getLogger(TaskController.class);

    /**
     * 根据工单编号（必填）和任务id（可不填）查询任务列表
     * 查询出任务详情集合和工单
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @PostMapping(value = "/queryTasks")
    public JsonResult queryTasks(@RequestBody Map<String, String> map) throws Exception {
        String workNo = map.get(Dict.WORKNO);
        String taskNo = map.get(Dict.TASKNO);
        Map<String, Object> result = taskListService.queryTasks(workNo, taskNo);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据工单编号查询所有与之关联的任务列表
     * 查询出任务详情集合
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @PostMapping(value = "/queryTasksByWorkNo")
    public JsonResult queryTasksByWorkNo(@RequestBody Map map) {
        String workNo = (String) map.get(Dict.WORKNO);
        List<Map> result = null;
        try {
            result = taskListService.queryTasksByWorkNo(workNo);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{e.getMessage()});
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据工单号查所有fdev任务信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryFdevTaskByWorkNo")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult queryFdevTaskByWorkNo(@RequestBody Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        return JsonResultUtil.buildSuccess(taskListService.queryFdevTaskByWorkNo(workNo));
    }
}

package com.spdb.fdev.release.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.service.IBatchTaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(tags = "批量任务接口")
@RequestMapping("/api/batch")
@RestController
public class BatchTaskController {

	@Autowired
	private IBatchTaskService batchTaskService;
	
	@OperateRecord(operateDiscribe="投产模块-新建批量任务")
	@PostMapping(value = "/createBatchTask")
	public JsonResult createNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		batchTaskService.createBatchTask(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-修改批量任务")
	@PostMapping(value = "/updateBatchTask")
	public JsonResult updateBatchTask(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		batchTaskService.updateBatchTask(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-删除批量任务")
	@PostMapping(value = "/deteleBatchTask")
	public JsonResult deteleBatchTask(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		batchTaskService.deteleBatchTask(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-获取批量任务")
	@PostMapping(value = "/queryBatchTask")
	public JsonResult queryBatchTask(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		List<Map<String, Object>> batchTaskList =  batchTaskService.queryBatchTask(requestParam);
		return JsonResultUtil.buildSuccess(batchTaskList);
	}
	
	@OperateRecord(operateDiscribe="投产模块-添加批量任务")
	@PostMapping(value = "/addBatchTask")
	public JsonResult addBatchTask(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		batchTaskService.addBatchTask(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询批量任务列表")
	@PostMapping(value = "/queryBatchTaskList")
	public JsonResult queryBatchTaskList(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		List<Map<String, Object>> batchTaskInfoList = batchTaskService.queryBatchTaskList(requestParam);
		return JsonResultUtil.buildSuccess(batchTaskInfoList);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询变更管理批量任务列表")
	@PostMapping(value = "/queryBatchTaskByProdId")
	public JsonResult queryBatchTaskByProdId(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		List<Map<String, Object>> batchTaskInfoList = batchTaskService.queryBatchTaskByProdId(requestParam);
		return JsonResultUtil.buildSuccess(batchTaskInfoList);
	}
	
	@OperateRecord(operateDiscribe = "投产模块-更新发布说明数据库文件序号")
    @PostMapping(value = "/updateNoteBatchNo")
    public JsonResult updateNoteBatchNo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(batchTaskService.updateNoteBatchNo(requestParam));
    }
	
	@OperateRecord(operateDiscribe = "投产模块-查询已添加批量任务的应用")
    @PostMapping(value = "/queryBatchAppIdByNoteId")
    public JsonResult queryBatchAppIdByNoteId(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(batchTaskService.queryBatchAppIdByNoteId(requestParam));
    }
}

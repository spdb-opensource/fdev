package com.mantis.controller;

import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.entity.MantisIssue;
import com.mantis.service.MantisFdevService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mantisFdev")
public class MantisFdevController {
	@Autowired
	private MantisFdevService mantisFdevService;


	/**
	 * 用户维度 查询用户缺陷信息
	 */
	@RequestValidate(NotEmptyFields = {Dict.CURRENTPAGE , Dict.PAGESIZE })
	@PostMapping(value = "/queryFuserMantis")
	public JsonResult queryFuserMantis(@RequestBody Map map) throws Exception {
		String currentPage = (String) map.get(Dict.CURRENTPAGE);
		String pageSize = (String) map.get(Dict.PAGESIZE);
		List<Map<String, String>> userList = (List<Map<String, String>>) map.get(Dict.USERLIST);
		userList.remove(null);
		if (Util.isNullOrEmpty(userList)){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"用户集合"});
		}
		List<MantisIssue> list = mantisFdevService.queryFuserMantis(currentPage, pageSize, userList);
		return JsonResultUtil.buildSuccess(list);
	}

	/**
	 * 用户维度 查询用户缺陷信息 总数---分页用
	 */
	@PostMapping(value = "/queryFuserMantisCount")
	public JsonResult queryFuserMantisCount(@RequestBody Map map) throws Exception {
		List<Map<String, String>> userList = (List<Map<String, String>>) map.get(Dict.USERLIST);
		userList.remove(null);
		if (Util.isNullOrEmpty(userList)){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"用户集合"});
		}
		Integer count = mantisFdevService.queryFuserMantisCount(userList);
		return JsonResultUtil.buildSuccess(count);
	}


	/**
	 * 用户维度 查询用户缺陷信息--- 全量数据
	 */
	@PostMapping(value = "/queryFuserMantisAll")
	@RequestValidate(NotEmptyFields = {Dict.USERLIST})
	public JsonResult queryFuserMantisAll(@RequestBody Map map) throws Exception {
		List<Map<String, String>> userList = (List<Map<String, String>>) map.get(Dict.USERLIST);
		String includeCloseFlag = (String)map.get(Dict.INCLUDECLOSEFLAG);//是否包括已关闭 及 确认拒绝状态缺陷
		userList.remove(null);
		List<MantisIssue> list = mantisFdevService.queryFuserMantisAll(userList, includeCloseFlag);
		return JsonResultUtil.buildSuccess(list);
	}



	/**
	 * 应用维度 查询任务缺陷信息
	 */
	@RequestValidate(NotEmptyFields = {Dict.CURRENTPAGE , Dict.PAGESIZE })
	@PostMapping(value = "/queryFtaskMantis")
	public JsonResult queryFtaskMantis(@RequestBody Map map) throws Exception {
		String currentPage = (String) map.get(Dict.CURRENTPAGE);
		String pageSize = (String) map.get(Dict.PAGESIZE);
		List<String> taskList = (List<String>) map.get(Dict.TASKLIST);
		taskList.remove(null);
		if (Util.isNullOrEmpty(taskList)){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"任务集合"});
		}
		List<MantisIssue> list = mantisFdevService.queryFtaskMantis(currentPage, pageSize, taskList);
		return JsonResultUtil.buildSuccess(list);
	}


	/**
	 * 用户维度 查询用户缺陷信息 总数---分页用
	 */
	@PostMapping(value = "/queryFtaskMantisCount")
	public JsonResult queryFtaskMantisCount(@RequestBody Map map) throws Exception {
		List<String> taskList = (List<String>) map.get(Dict.TASKLIST);
		taskList.remove(null);
		if (Util.isNullOrEmpty(taskList)){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"任务集合"});
		}
		Integer count = mantisFdevService.queryFtaskMantisCount(taskList);
		return JsonResultUtil.buildSuccess(count);
	}


	/**
	 * 应用维度 查询任务缺陷信息 --全量数据
	 */
	@PostMapping(value = "/queryFtaskMantisAll")
    @RequestValidate(NotEmptyFields = {Dict.TASKLIST})
	public JsonResult queryFtaskMantisAll(@RequestBody Map map) throws Exception {
		List<String> taskList = (List<String>) map.get(Dict.TASKLIST);
		String includeCloseFlag = (String)map.get(Dict.INCLUDECLOSEFLAG);//是否包括已关闭 及 确认拒绝状态缺陷
		taskList.remove(null);
		List<MantisIssue> list = mantisFdevService.queryFtaskMantisAll(taskList, includeCloseFlag);
		return JsonResultUtil.buildSuccess(list);
	}


	/**
	 * fdev缺陷修改
	 */
	@RequestValidate(NotEmptyFields = {Dict.ID  })
	@PostMapping(value = "/updateFdevMantis")
	public JsonResult updateFdevMantis(@RequestBody Map map) throws Exception {
		String result = mantisFdevService.updateFdevMantis(map);
		return JsonResultUtil.buildSuccess(result);
	}

	@PostMapping(value = "/deleteTaskIssue")
	public JsonResult deleteTaskIssue(@RequestBody Map<String, String> map) throws Exception {
		String task_id = map.get(Dict.TASK_ID);
		mantisFdevService.deleteTaskIssue(task_id);
		return JsonResultUtil.buildSuccess(null);
	}

	/**
	 * fdev缺陷查询返回任务相关信息
	 */
	@PostMapping(value = "/queryMantisTask")
	public JsonResult queryMantisTask(@RequestBody Map map){
		return JsonResultUtil.buildSuccess(mantisFdevService.queryMantisTask(map));
	}

	/**
	 * 设置缺陷所属环境字段
	 */
	@RequestValidate(NotEmptyFields = {Dict.ID})
	@PostMapping(value = "/setMantisEnv")
	public JsonResult setMantisEnv(@RequestBody Map<String, String> param) {
		mantisFdevService.setMantisEnv(param.get(Dict.ID));
		return JsonResultUtil.buildSuccess();
	}

	/**
	 * 修改存量缺陷的所属小组为工单小组
	 */
	@PostMapping(value = "/updateAllMantisGroup")
	public JsonResult updateAllMantisGroup(@RequestBody Map<String, String> param) {
		mantisFdevService.updateAllMantisGroup();
		return JsonResultUtil.buildSuccess();
	}

	/**
	 * 修改工单下缺陷所属小组
	 */
	@RequestValidate(NotEmptyFields = {Dict.WORKNO, Dict.FDEVGROUPID})
	@PostMapping(value = "/updateMantisGroupByWorkNo")
	public JsonResult updateMantisGroupByWorkNo(@RequestBody Map<String, String> param) throws Exception {
		mantisFdevService.updateMantisGroupByWorkNo(param.get(Dict.WORKNO), param.get(Dict.FDEVGROUPID));
		return JsonResultUtil.buildSuccess();
	}
}

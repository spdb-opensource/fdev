package com.plan.controller;

import java.util.List;
import java.util.Map;

import com.test.testmanagecommon.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plan.dict.Dict;
import com.plan.service.MantisFlawService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/mantis")
public class MantisFlawController {
	@Autowired
	private MantisFlawService mantisFlawService;
	
	@RequestValidate(NotEmptyFields = {Dict.SUMMARY,Dict.DESCRIPTION,Dict.PROJECT,Dict.SEVERITY,Dict.PRIORITY
			,Dict.WORKNO,Dict.HANDLER,Dict.STAGE,Dict.FLAW_SOURCE,Dict.PLAN_FIX_DATE,
			Dict.FLAW_TYPE,Dict.PLANLIST_TESTCASE_ID})
	@PostMapping(value = "/add")
	public JsonResult addMantisFlaw(@RequestBody Map<String,Object> map) throws Exception {
		mantisFlawService.addMantisFlaw(map);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@RequestValidate(NotEmptyFields = {Dict.REQUIREMENT_NAME, Dict.MODULE, Dict.OCCURRED_TIME
			, Dict.IS_TRIGGER_ISSUE, Dict.PROBLEM_PHENOMENON, Dict.INFLUENCE_AREA, Dict.ISSUE_REASON
			, Dict.ISSUE_TYPE, Dict.DISCOVER_STAGE, Dict.IS_UAT_REPLICATION, Dict.IS_REL_REPLICATION
			, Dict.IS_GRAY_REPLICATION, Dict.IS_INVOLVE_URGENCY, Dict.IMPROVEMENT_MEASURES, Dict.ISSUE_LEVEL
			, Dict.REVIEWER_STATUS, Dict.DEAL_STATUS,Dict.ORFANIZER})
	@PostMapping(value = "/addProIssue")
	public JsonResult addProIssue(@RequestBody Map<String,Object> map) throws Exception {
		mantisFlawService.addProIssue(map);
		return JsonResultUtil.buildSuccess(null);
	}

	@PostMapping(value = "/queryMantisProjects")
	public JsonResult queryMantisProjects() throws Exception {
		return JsonResultUtil.buildSuccess(mantisFlawService.queryMantisProjects());
	}
	
	@RequestValidate(NotEmptyFields = {Dict.PLANLIST_TESTCASE_ID})
	@PostMapping(value = "/isTestcaseAddIssue")
	public JsonResult isTestcaseAddIssue(@RequestBody Map<String,String> map) throws Exception {
		String planlist_testcase_id = map.get(Dict.PLANLIST_TESTCASE_ID);
		return JsonResultUtil.buildSuccess(mantisFlawService.isTestcaseAddIssue(planlist_testcase_id));
	}

	@PostMapping(value = "/updateMantisEmail")
	public JsonResult updateMantisEmail(@RequestBody Map map) throws Exception {
		return JsonResultUtil.buildSuccess(mantisFlawService.updateMantisEmail(map));
	}

	/**
	 * 根据多个工单号查询多个任务id
	 * @return
	 */
	@RequestValidate(NotEmptyFields = {Dict.WORKNOS})
	@PostMapping(value = "/queryTaskNoByWorkNos")
	public JsonResult queryTaskNoByWorkNos(@RequestBody Map requestParam) throws Exception {
		List<String> workNos = (List<String>) requestParam.get(Dict.WORKNOS);
		return JsonResultUtil.buildSuccess(mantisFlawService.queryTaskNoByWorkNos(workNos));
	}

	/**
	 * 根据单个工单号查询主任务id
	 * @return
	 */
	@RequestValidate(NotEmptyFields = {Dict.WORKNO})
	@PostMapping(value = "/querymainTaskNoByWorkNo")
	public JsonResult querymainTaskNoByWorkNo(@RequestBody Map requestParam) throws Exception {
		String workNo = (String) requestParam.get(Dict.WORKNO);
		return JsonResultUtil.buildSuccess(mantisFlawService.querymainTaskNoByWorkNo(workNo));
	}

	/**
	 * 根据工单号查询所有应用
	 * @return
	 */
	@RequestValidate(NotEmptyFields = {Dict.WORKNO})
	@PostMapping(value = "/queryAppByWorkNo")
	public JsonResult queryAppByWorkNo(@RequestBody Map requestParam) throws Exception {
		String workNo = (String) requestParam.get(Dict.WORKNO);
		return JsonResultUtil.buildSuccess(mantisFlawService.queryAppByWorkNo(workNo));
	}

}

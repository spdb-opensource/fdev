package com.mantis.controller;

import com.mantis.dict.Constants;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.entity.MantisIssue;
import com.mantis.service.MantisService;
import com.mantis.service.NewFdevService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mantis")
public class MantisController {
	@Autowired
	private MantisService mantisService;
	@Autowired
	private NewFdevService newFdevService;

	@RequestValidate(NotEmptyFields = {Dict.CURRENTPAGE , Dict.PAGESIZE })
	@PostMapping(value = "/query")
	public JsonResult query(@RequestBody Map<String,Object> map) throws Exception {
		String currentPage = String.valueOf(map.get(Dict.CURRENTPAGE)) ;
		String pageSize = String.valueOf(map.get(Dict.PAGESIZE));
		String reporter = (String) map.get(Dict.REPORTER);
		String handler = (String) map.get(Dict.HANDLER);
		String status = (String) map.get(Dict.STATUS);
		String workNo = (String) map.get(Dict.WORKNO);
		String startDate = (String) map.get(Dict.STARTDATE);
		String endDate = (String) map.get(Dict.ENDDATE);
		String id = (String) map.get(Dict.ID);
		String includeCloseFlag = (String) map.get(Dict.INCLUDECLOSEFLAG);//是否包括已关闭 及 确认拒绝状态缺陷
		List<String> groupIds = (List<String>) map.get(Dict.FDEV_GROUP_ID);  //fdev所属小组id
		String project_name = (String) map.get(Dict.PROJECT_NAME);
		String redmine_id = (String) map.get(Dict.REDMINE_ID);      //需求编号
		String app_name = (String) map.get(Dict.APP_NAME);   //应用英文名
		String task_no = (String) map.get(Dict.TASK_NO);//任务id
		String openTimes = (String) map.get(Dict.OPENTIMES);//区分一次性修复还是重复打开
		String auditFlag = (String) map.get(Dict.AUDITFLAG);//审核标志,0-待审核,1-已审核
		Boolean isIncludeChildren = (Boolean) map.getOrDefault(Dict.ISINCLUDECHILDREN, false);
		Map result = mantisService.query(currentPage,pageSize,reporter,handler,status,workNo,startDate,endDate,
				includeCloseFlag,id,groupIds, redmine_id, app_name, project_name,task_no,openTimes, auditFlag, isIncludeChildren);
		return JsonResultUtil.buildSuccess(result);
	}

	@PostMapping(value = "/update")
	public JsonResult update(@RequestBody Map<String, Object> map) throws Exception {
		mantisService.update(map);
		return JsonResultUtil.buildSuccess(null);
	}

	@PostMapping(value = "/countReporterSum")
	public JsonResult countReporter(@RequestBody Map<String, String> map) throws Exception {
		String startDate = map.get(Dict.STARTDATE);
		String endDate = map.get(Dict.ENDDATE);
		List<Map<String,String>> list = mantisService.countReporterSum(startDate, endDate);
		return JsonResultUtil.buildSuccess(list);
	}

	/**
	 * 查询测试人员在指定时间段内提出的有效缺陷
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/countReporterSumNew")
	@RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
	public JsonResult countReporterSumNew(@RequestBody Map<String, Object> map) throws Exception {
		String startDate = (String) map.get(Dict.STARTDATE);
		String endDate = (String) map.get(Dict.ENDDATE);
		List<String> userNameEnList = (List<String>) map.get(Dict.USERLIST);
		return JsonResultUtil.buildSuccess(mantisService.countReporterSumNew(startDate, endDate, userNameEnList));
	}

	@PostMapping(value = "/queryIssueDetail")
	public JsonResult queryIssueDetail(@RequestBody Map<String,String> map) throws Exception {
		String id = map.get(Dict.ID);
		Map<String,Object> list = mantisService.queryIssueDetail(id);
		return JsonResultUtil.buildSuccess(list);
	}

	@PostMapping(value = "/queryIssueByPlanResultId")
	@RequestValidate(NotEmptyFields = {Dict.ID})
	public JsonResult queryIssueByPlanResultId(@RequestBody Map<String,String> map) throws Exception {
		String id = map.get(Dict.ID);
		List<MantisIssue> list = mantisService.queryIssueByPlanResultId(id);
		return JsonResultUtil.buildSuccess(list);
	}

	@PostMapping(value = "/delete")
	public JsonResult delete(@RequestBody Map<String, String> map) throws Exception {
		mantisService.delete(map);
		return JsonResultUtil.buildSuccess(null);
	}

	//mantis缺陷列表导出
	@PostMapping(value = "/exportMantisList")
	public void exportMantis(@RequestBody Map<String, Object> requestMap,HttpServletResponse resp)throws Exception {
		String reporter = (String) requestMap.get(Dict.REPORTER);
		String handler = (String) requestMap.get(Dict.HANDLER);
		String status = (String) requestMap.get(Dict.STATUS);
		String workNo = (String) requestMap.get(Dict.WORKNO);
		String startDate = (String) requestMap.get(Dict.STARTDATE);
		String endDate = (String) requestMap.get(Dict.ENDDATE);
		String includeCloseFlag = (String) requestMap.get(Dict.INCLUDECLOSEFLAG);//是否包括已关闭 及 确认拒绝状态缺陷
		List<String> groupIds = (List<String>) requestMap.get(Dict.FDEV_GROUP_ID);  //fdev所属小组id
		String redmine_id = (String) requestMap.get(Dict.REDMINE_ID);      //需求编号
		String app_name = (String) requestMap.get(Dict.APP_NAME);   //应用英文名
		String project_name = (String) requestMap.get(Dict.PROJECT_NAME);
		String auditFlag = (String) requestMap.get(Dict.AUDITFLAG);//审核标志,0-待审核,1-已审核
		Boolean isIncludeChildren = (Boolean) requestMap.getOrDefault(Dict.ISINCLUDECHILDREN, false);
		mantisService.exportMantis(reporter,handler,status,workNo,startDate,endDate,includeCloseFlag,
				groupIds, redmine_id, app_name, project_name, auditFlag, isIncludeChildren, resp);
	}

	@PostMapping(value = "/countWorkOrderSum")
	public JsonResult countWorkOrder(@RequestBody Map<String, String> map) throws Exception {
		String startDate = map.get(Dict.STARTDATE);
		String endDate = map.get(Dict.ENDDATE);
		List<Map<String,String>> list = mantisService.countWorkOrderSum(startDate, endDate);
		return JsonResultUtil.buildSuccess(list);
	}

	@PostMapping(value = "/queryWorkOrderIssues")
	public JsonResult queryWorkOrderIssues(@RequestBody Map<String, String> map) throws Exception {
		String workNo = map.get(Dict.WORKNO);
		String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, Constants.FAIL_GET));
		String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, Constants.FAIL_GET));
		Map<String,Integer> issues = mantisService.queryWorkOrderIssues(workNo,startDate, endDate);
		return JsonResultUtil.buildSuccess(issues);
	}

	@PostMapping(value = "/queryOrderUnderwayIssues")
	public JsonResult queryOrderUnderwayIssues(@RequestBody Map<String, String> map) throws Exception {
		String workNo = map.get(Dict.WORKNO);
		Map<String,Integer> issues = mantisService.queryOrderUnderwayIssues(workNo);
		return JsonResultUtil.buildSuccess(issues);
	}

	/**
	 * 根据工单编号和缺陷状态查询其下缺陷数量
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/countMantisByWorkNo")
	@RequestValidate(NotEmptyFields = {Dict.WORKNO})
	public JsonResult countMantisByWorkNo(@RequestBody Map<String, String> map) throws Exception {
		String workNo = map.get(Dict.WORKNO);
		List<Map<String, String>> resultList = mantisService.countMantisByWorkNo(workNo);
		return JsonResultUtil.buildSuccess(resultList);
	}

	/**
	 * 根据用户和选择时间查缺陷总数，未修复缺陷数，当前该用户新建缺陷数
	 * @param map
	 * @return
	 */
	@RequestMapping("/queryIssueByTimeUser")
	@RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.USER_NAME_EN, Dict.WORKNO})
	public JsonResult queryIssueByTimeUser(@RequestBody Map map) throws Exception {
		return JsonResultUtil.buildSuccess(mantisService.queryIssueByTimeUser(map));
	}

	/**
	 * 根据用户、工单集合和选择时间查缺陷总数，未修复缺陷数，当前该用户新建缺陷数
	 * @param map
	 * @return
	 */
	@RequestMapping("/queryIssueByTimeUserNew")
	@RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.USER_NAME_EN, Dict.WORKNOS})
	public JsonResult queryIssueByTimeUserNew(@RequestBody Map<String, Object> map) throws Exception {
		List<String> workNoList = (List<String>) map.get(Dict.WORKNOS);
		String userEnName = String.valueOf(map.get(Dict.USER_NAME_EN));
		String startDate = String.valueOf(map.get(Dict.STARTDATE));
		String endDate = String.valueOf(map.get(Dict.ENDDATE));
		return JsonResultUtil.buildSuccess(mantisService.queryIssueByTimeUserNew(workNoList, userEnName, startDate, endDate));
	}

    /**
     * 根据工单号列表查缺陷详情
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/countIssueDetailByOrderNos")
    public JsonResult countIssueDetailByOrderNos(@RequestBody Map map) throws Exception {
	    List<String> workNo = (List<String>)map.getOrDefault(Dict.WORKNO, new ArrayList<>());
	    String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, ""));
        String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, ""));
		List<String> groupIds = (List<String>) map.get(Dict.GROUPIDS);
        return JsonResultUtil.buildSuccess(mantisService.countIssueDetailByOrderNos(workNo, startDate, endDate, groupIds));
    }

	/**
	 * 批量维护fdev组信息及应用信息
	 * @return
	 */
	@RequestMapping("/batchGroupInfo")
	public JsonResult batchGroupInfo() throws Exception {
		mantisService.batchGroupInfo();
		return JsonResultUtil.buildSuccess();
	}

	/**
	 * 根据fdev小组id查询各组有效缺陷数量
	*/
	@RequestMapping("/queryGroupIssueInfo")
	public JsonResult queryGroupIssueInfo(@RequestBody Map map) throws Exception {
		List<String> groupIds = (List<String>) map.get(Dict.GROUPIDS);
		return JsonResultUtil.buildSuccess(mantisService.queryGroupIssueInfo(groupIds));
	}

	 /**
	 * 质量报表
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/qualityReport")
	@RequestValidate(NotEmptyFields = {Dict.GROUP})
	public JsonResult qualityReport(@RequestBody Map map) throws Exception {
		String fdevGroup = String.valueOf(map.get(Dict.GROUP));
		String startDate = String.valueOf(map.getOrDefault(Dict.STARTDATE, ""));
		String endDate = String.valueOf(map.getOrDefault(Dict.ENDDATE, ""));
		Map<String, Object> result = mantisService.qualityReport(fdevGroup, startDate,endDate);
		return JsonResultUtil.buildSuccess(result);
    }

	/**
	 * 根据缺陷id修改缺陷的工单号或实施单元编号
	 * @return
	 */
    @RequestMapping("/updateMantis")
	public JsonResult updateMantis(@RequestBody Map map){
    	mantisService.updateMantis(map);
    	return JsonResultUtil.buildSuccess();
	}

	/**
	 * 质量报表全量数据
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/qualityReportAll")
	public JsonResult qualityReportAll(@RequestBody Map map) throws Exception {
		List<Map<String, String>> result = mantisService.qualityReportAll();
		return JsonResultUtil.buildSuccess(result);
	}

	/**
	 * 实施单元查询缺陷
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/queryFuserMantis")
	public JsonResult queryFuserMantisAll(@RequestBody Map<String,Object> map ) throws Exception {
		//根据实施单元查询所有的缺陷
		List<String>  unitNos = (List<String>) map.get(Dict.UNITNO); //实施单元id集合
		if (Util.isNullOrEmpty(unitNos)){
			return null;
		}
		String status =(String) map.get(Dict.STATUS);  //状态
		String userNameEn =(String) map.get(Dict.USERNAMEEN);  //当前用户的英文名
		List<MantisIssue> mantisIssues= newFdevService.queryFuserMantisAll(unitNos,userNameEn,status);
		return JsonResultUtil.buildSuccess(mantisIssues);
	}

	/**
	 * 根据缺陷id查询缺陷详情
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/queryMatisDetailById")
	public JsonResult queryMatisDetailById(@RequestBody Map<String,String> map ) throws Exception {
			String id = map.get(Dict.ID);
		if (Util.isNullOrEmpty(id)){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"缺陷id"});
		}
		Map<String,Object> list = mantisService.queryIssueDetail(id);
		return JsonResultUtil.buildSuccess(list);
	}

	/**
	 * 查询任务相关缺陷id
	 * @param map
	 * @return
	 */
	@RequestMapping("/queryMantisIdByTaskNo")
	public JsonResult queryMantisIdByTaskNo(@RequestBody Map<String,String> map) {
		return JsonResultUtil.buildSuccess(mantisService.queryMantisIdByTaskNo(map.get(Dict.TASKNO)));
	}

	/**
	 * 查询任务相关缺陷id
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/auditMantis", method = RequestMethod.POST)
	@RequestValidate(NotEmptyFields = {Dict.ID})
	public JsonResult auditMantis(@RequestBody Map<String,String> param) throws Exception {
		mantisService.auditMantis(param.get(Dict.ID), param.get(Dict.AUDITFLAG));
		return JsonResultUtil.buildSuccess();
	}

	/**
	 * 校验审核权限
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/checkAuditAuthority", method = RequestMethod.POST)
	@RequestValidate(NotEmptyFields = {Dict.ID})
	public JsonResult checkAuditAuthority(@RequestBody Map<String,String> param) throws Exception {
		return JsonResultUtil.buildSuccess(mantisService.checkAuditAuthority(param.get(Dict.ID), ""));
	}

	/**
	 * 查询待审核缺陷信息
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/queryAuditMantisInfo", method = RequestMethod.POST)
	@RequestValidate(NotEmptyFields = {Dict.ID})
	public JsonResult queryAuditMantisInfo(@RequestBody Map<String,String> param) throws Exception {
		return JsonResultUtil.buildSuccess(mantisService.queryAuditMantisInfo(param.get(Dict.ID)));
	}

	/**
	 * 查询时间段内各小组的缺陷数量
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/countMantisByGroup")
	@RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.GROUPIDS})
	public JsonResult countMantisByGroup(@RequestBody Map<String, Object> param) throws Exception {
		return JsonResultUtil.buildSuccess(mantisService.countMantisByGroup((String)param.get(Dict.STARTDATE),
				(String)param.get(Dict.ENDDATE), (List<String>) param.get(Dict.GROUPIDS)));
	}
}

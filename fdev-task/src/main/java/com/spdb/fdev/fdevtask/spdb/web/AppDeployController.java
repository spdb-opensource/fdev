package com.spdb.fdev.fdevtask.spdb.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.DemandService;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IAppDeployService;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "环境部署申请接口")
@RequestMapping("/api/deploy")
@RestController
@RefreshScope
public class AppDeployController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

	@Autowired
	private IFdevTaskService fdevTaskService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private IAppDeployService appDeployService;

	@Autowired
	private IUserApi userApi;
	
	@Autowired
	private IAppApi appApi;
	

	@ApiOperation(value = "查询部署信息")
	@RequestMapping(value = "/queryAppDeploy", method = RequestMethod.POST)
	public JsonResult queryTaskInfoById(@RequestBody Map<String, Object> param) throws Exception {
		Map<String,Object> resultMap = appDeployService.queryApproved(param);
		return JsonResultUtil.buildSuccess(resultMap);
	}

	@ApiOperation(value = "申请环境部署")
	@RequestMapping(value = "/applayDeploy", method = RequestMethod.POST)
	public JsonResult applayDeploy(@RequestBody Map<String, Object> param) throws Exception {
		String taskId = (String) param.get(Dict.TASKID);
		String userId = (String) param.get("applayUserId");
		String overdueReason = (String) param.get("applyDesc");
		String mergeReason = (String) param.get("merge_reason");
		// 根据任务id查询任务详情
		FdevTask fdevTask = fdevTaskService.queryTaskById(taskId);

		AppDeployReview applayDeploy = new AppDeployReview();
		ObjectId id = new ObjectId();
		applayDeploy.setId(id.toString());
		applayDeploy.setTaskId(taskId);
		applayDeploy.setTaskName(fdevTask.getName());
		applayDeploy.setApplicationId(fdevTask.getProject_id());
		applayDeploy.setApplicationNameEn(fdevTask.getProject_name());
		applayDeploy.setDemandId(fdevTask.getRqrmnt_no());
		
		Map<String,Object> sendAppMap = new HashMap<String,Object>();
		sendAppMap.put("id", fdevTask.getProject_id());
		Map<String,Object> resultMap = appApi.queryAppById(sendAppMap);
		String git = (String) resultMap.get("git");
		Integer gitProId = (Integer)resultMap.get("gitlab_project_id");
		
		applayDeploy.setAppGitUrl(git);
		applayDeploy.setGitProId(gitProId);
		
		// 根据需求id查询需求详情
		Map demandInfo = demandService.queryDemandInfoDetail(fdevTask.getRqrmnt_no());
		// 需求编号
		applayDeploy.setOaContactNo((String) demandInfo.get("oa_contact_no"));
		// 需求名称
		applayDeploy.setOaContactName((String) demandInfo.get("oa_contact_name"));
		// 根据用户id查询用户详情
		Map<String, Object> sendUserMap = new HashMap<String, Object>();
		sendUserMap.put("id", userId);
		Map<String, Object> resultUserMap = userApi.queryUser(sendUserMap);
		applayDeploy.setApplayUserId(userId);
		applayDeploy.setApplayUserNameZh((String) resultUserMap.get("user_name_cn"));
		// applayDeploy.setApplayUserNameEn((String)resultUserMap.get("user_name_en"));//用户英文名称

		// 根据用户组id查询用户组详情
		String groupId = (String) resultUserMap.get("group_id");
		String groupName = userApi.getGroupNameById(groupId);
		applayDeploy.setApplayUserOwnerGroupId(groupId);
		applayDeploy.setApplayUserOwnerGroup(groupName);

		applayDeploy.setReviewStatus("0");// 审核状态 0:未审核 1:已审核
		applayDeploy.setOverdueReason(overdueReason);// 申请原因
		applayDeploy.setMergeReason(mergeReason);// 申请原因en
		applayDeploy.setDeployEnv("sit2");// 部署环境
		applayDeploy.setDeploy_status("0");//0:未部署 1:部署失败 2:部署成功 3:部署中
		
		String dateTime = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN);
		applayDeploy.setApplayDate(dateTime.substring(0,10));
		applayDeploy.setApplayTime(dateTime.substring(11,19));

		return JsonResultUtil.buildSuccess(appDeployService.applayDeploy(applayDeploy));
	}

	@ApiOperation(value = "保存部署信息")
	@RequestMapping(value = "/saveAppDeployInfo", method = RequestMethod.POST)
	public JsonResult saveAppDeployInfo(@RequestBody Map<String, Object> param) throws Exception {
		String deployEnv = (String) param.get("deployEnv");
		String appName = (String) param.get("appName");
		String imageCaasUrl = (String) param.get("imageCaasUrl");
		String imageSccUrl = (String) param.get("imageSccUrl");
		String imagePushStatus = (String) param.get("imagePushStatus");// 0：已推送caas 1：已推送scc 2：caas及scc均已推送

		AppDeployReview appDeployReview = new AppDeployReview();
		ObjectId id = new ObjectId();
		appDeployReview.setId(id.toString());
		appDeployReview.setDeployEnv(deployEnv);
		appDeployReview.setApplicationNameEn(appName);
		appDeployReview.setImageCaasUrl(imageCaasUrl);
		appDeployReview.setImageSccUrl(imageSccUrl);
		appDeployReview.setImagePushStatus(imagePushStatus);
		
		appDeployReview.setPushTime(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));

		return JsonResultUtil.buildSuccess(appDeployService.savePiplineAppInfo(appDeployReview));
	}

	@ApiOperation(value = "批量部署")
	@RequestMapping(value = "/deployApps", method = RequestMethod.POST)
	public JsonResult deployApps(@RequestBody Map<String, Object> param) throws Exception {
		List<String> ids = (List<String>) param.get("ids");
		
		List<AppDeployReview> appDeployList = new ArrayList<AppDeployReview>();
		if (!Util.isNullOrEmpty(ids)) {
			appDeployList = appDeployService.queryAppDeploy(ids);
				String userId = (String) param.get("userId");
				// 根据用户id查询用户详情
				Map<String, Object> sendUserMap = new HashMap<String, Object>();
				sendUserMap.put("id", userId);
				Map<String, Object> resultUserMap = userApi.queryUser(sendUserMap);
				if (!Util.isNullOrEmpty(appDeployList)) {
					for (Iterator iterator = appDeployList.iterator(); iterator.hasNext();) {
						AppDeployReview appDeployReview = (AppDeployReview) iterator.next();
						appDeployReview.setReviewStatus("1");						
						appDeployReview.setDeploy_status("3");//0:未部署 1:部署失败 2:部署成功 3:部署中
						appDeployReview.setReviewUserId(userId);
						appDeployReview.setReviewUserNameZh((String) resultUserMap.get("user_name_cn"));
						String dateTime = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN);
						appDeployReview.setReviewTime(dateTime.substring(0,10));
						appDeployService.update(appDeployReview);
						
						String appName = appDeployReview.getApplicationNameEn();
						String appGitUrl = appDeployReview.getAppGitUrl();
						Integer appId = appDeployReview.getGitProId();
						
						Map<String,Object> appMap = appApi.queryAppByGitlabIds(String.valueOf(appId));
						List<String> managerEmailList = new ArrayList<String>();
						List<Map<String,Object>> spdbManagerList = new ArrayList<Map<String,Object>>();
						List<Map<String,Object>> devManagerList = new ArrayList<Map<String,Object>>();
						
						Map<String,Object> sendReleaseMap = new HashMap<String,Object>();
						sendReleaseMap.put("appName", appName);//应用名称
						sendReleaseMap.put("appGitUrl", appGitUrl);//git地址
						sendReleaseMap.put("branch", "SIT");//分支
						sendReleaseMap.put("deployBranch", "sit2");//部署环境
						sendReleaseMap.put("appId", appId.toString());
						if(!Util.isNullOrEmpty(appMap)) {
							spdbManagerList = (List<Map<String, Object>>) appMap.get("spdb_managers");
							if(!Util.isNullOrEmpty(spdbManagerList)) {
								for (Iterator iterator2 = spdbManagerList.iterator(); iterator2.hasNext();) {
									Map<String, Object> spdbManagerMap = (Map<String, Object>) iterator2.next();
									String userEmail = (String) spdbManagerMap.get("user_name_en")+"xxx";
									managerEmailList.add(userEmail);
								}
							}
							devManagerList = (List<Map<String, Object>>) appMap.get("dev_managers");
							if(!Util.isNullOrEmpty(devManagerList)) {
								for (Iterator iterator3 = devManagerList.iterator(); iterator3.hasNext();) {
									Map<String, Object> devManagerMap = (Map<String, Object>) iterator3.next();
									String userEmail = (String) devManagerMap.get("user_name_en")+"xxx";
									managerEmailList.add(userEmail);
								}
							}
						}
						sendReleaseMap.put("emails", managerEmailList);
						appDeployService.deploy(sendReleaseMap);
					}
				}
		}
		return JsonResultUtil.buildSuccess();
	}

	@ApiOperation(value = "根据任务id查询部署信息")
	@RequestMapping(value = "/queryAppDeployByTaskId", method = RequestMethod.POST)
	public JsonResult queryAppDeployByTaskId(@RequestBody Map<String, Object> param) throws Exception {
		String taskId = (String) param.get(Dict.TASKID);
		AppDeployReview appDeployReview = appDeployService.queryAppDeployByTaskId(taskId);// 0:未部署 1:部署失败 2:部署成功
		return JsonResultUtil.buildSuccess(appDeployReview);
	}

	@ApiOperation(value = "查询应用镜像")
	@RequestMapping(value = "/queryAppImage", method = RequestMethod.POST)
	public JsonResult queryAppImage(@RequestBody Map<String, Object> param) throws Exception {
		String appName = (String) param.get(Dict.APPNAME);
		AppDeployReview appDeployReview = appDeployService.queryAppImage(appName, null);
		return JsonResultUtil.buildSuccess(appDeployReview);
	}

	@ApiOperation(value = "根据任务id查询已申请部署应用下其他未勾选任务")
	@RequestMapping(value = "/queryOtherTasks", method = RequestMethod.POST)
	public JsonResult queryOtherTasks(@RequestBody Map<String, Object> param) throws Exception {
		String ids = (String) param.get(Dict.IDS);
		String appNames = (String) param.get("appNames");
		List<AppDeployReview> appDeployList = appDeployService.queryOtherTasks(appNames, ids);
		return JsonResultUtil.buildSuccess(appDeployList);
	}

	@ApiOperation(value = "查询应用关联任务")
	@RequestMapping(value = "/queryDeployTask", method = RequestMethod.POST)
	public JsonResult queryDeployTask(@RequestBody Map<String, Object> param) throws Exception {
		String appNames = (String) param.get("appNames");
		List<AppDeployReview> appDeployReview = appDeployService.queryDeployTask(appNames, "0");
		return JsonResultUtil.buildSuccess(appDeployReview);
	}
	
	@ApiOperation(value = "导出excel")
	@RequestMapping(value = "/export", method = RequestMethod.POST)
	public void export(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
		Map<String,Object> resultMap = appDeployService.queryApproved(param);
		appDeployService.export(resultMap,resp);
	}
	
	@ApiOperation(value = "修改部署状态")
	@RequestMapping(value = "/modifyDeployStatus", method = RequestMethod.POST)
	public JsonResult modifyDeployStatus(@RequestBody Map<String, Object> param) throws Exception {
		appDeployService.modifyDeloyStatus(param);
		return JsonResultUtil.buildSuccess();
	}

	
	

}

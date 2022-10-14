package com.mantis.service.impl;

import com.mantis.dao.MantisDao;
import com.mantis.dict.Constants;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.entity.MantisIssue;
import com.mantis.service.ApiService;
import com.mantis.service.MantisFdevService;
import com.mantis.service.MantisService;
import com.mantis.util.MantisRestTemplate;
import com.mantis.util.MyUtil;
import com.mantis.util.Utils;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class MantisFdevServiceImpl implements MantisFdevService {

	private static final Logger logger = LoggerFactory.getLogger(MantisFdevServiceImpl.class);

	@Autowired
	private MantisDao mantisDao;
	@Autowired
	private MyUtil myUtil;
	@Autowired
	private RestTransport restTransport;
	@Autowired
	private MantisRestTemplate mantisRestTemplate;
	@Autowired
	private MantisService mantisService;
	@Autowired
	private ApiService apiService;

	@Value("${manits.issue.url}")
	private String mantis_url;
	@Value("${manits.fdev.token}")
	private String mantis_fdev_token;
	@Value("${spring.profiles.active}")
	private String env;
	@Value("${testmanage.mantis.domain}")
	private String issueUrl;


	/**
	 * 用户维度 查询用户缺陷信息
	 * "userList":[
	 *         {"user_name_cn":"xxx", "user_name_en":"xxxx"}
	 *     ]
	 */
	@Override
	public List<MantisIssue> queryFuserMantis(String currentPage, String pageSize, List<Map<String, String>> userList) throws Exception {
		Integer current_page = Integer.parseInt(currentPage);
		Integer page_size = Integer.parseInt(pageSize);
		Integer start_page = page_size * (current_page - 1);//起始
		List<MantisIssue> list = null;
		try {
			list = mantisDao.queryFuserMantis(start_page, page_size, userListToStr(userList),env);
		}catch (Exception e){
			logger.error(e.getMessage());
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
		return list;
	}


	/**
	 * 用户维度 查询用户缺陷信息 总数---分页用
	 */
	@Override
	public Integer queryFuserMantisCount(List<Map<String, String>> userList) throws Exception {
		Integer count = null;
		try {
			count = mantisDao.queryFuserMantisCount(userListToStr(userList),env);
		}catch (Exception e){
			logger.error(e.getMessage());
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
		return count;
	}

	/**
	 * 用户维度 查询用户缺陷信息--- 全量数据
	 */
	@Override
	public List<MantisIssue> queryFuserMantisAll(List<Map<String, String>> userList, String includeCloseFlag) throws  Exception{
		List<MantisIssue> list = null;
		try {
			list = mantisDao.queryFuserMantisAll(userListToStr(userList),env, includeCloseFlag);
		}catch (Exception e){
			logger.error(e.getMessage());
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
		//批量查询任务信息
		Set<String> taskIds = list.stream().map(issue -> issue.getTask_id()).collect(Collectors.toSet());
		taskIds.remove(null);
		List<Map> taskList = apiService.queryTaskBaseInfoByIds(taskIds);
		Map<String, String> taskNameMap = new HashMap<>();
		for (Map task : taskList) {
			taskNameMap.put((String) task.get(Dict.ID), (String) task.get(Dict.NAME));
		}
		//批量查询工单信息
		Set<String> workNos = list.stream().map(issue -> issue.getWorkNo()).collect(Collectors.toSet());
		workNos.remove(null);
		List<Map> workOrderList = apiService.queryWorkOrderByNos(workNos, null);
		Map<String, Map> workOrderMap = new HashMap<>();
		for (Map workOrder : workOrderList) {
			workOrderMap.put((String) workOrder.get(Dict.WORKORDERNO), workOrder);
		}
		Iterator<MantisIssue> iterator = list.iterator();
		while(iterator.hasNext()){
			MantisIssue mantisIssue = iterator.next();
			Map workOrder = workOrderMap.get(mantisIssue.getWorkNo());
			if(Util.isNullOrEmpty(workOrder)){
				continue;
			}else{
				String mainTaskNo = (String) workOrder.get(Dict.MAINTASKNO);
				String workOrderFlag = (String) workOrder.get(Dict.WORKORDERFLAG);
				String taskName =(String)workOrder.get(Dict.MAINTASKNAME);
				if (!Util.isNullOrEmpty(mainTaskNo) && "1".equals(workOrderFlag)){
					//fdev老工单
					mantisIssue.setTask_id(mainTaskNo);
					mantisIssue.setTask_name(taskName);
				}else if(Util.isNullOrEmpty(mainTaskNo) && "1".equals(workOrderFlag)){
					//fdev新工单
					String taskId = mantisIssue.getTask_id();
					if(Util.isNullOrEmpty(taskId)){
						//未分配,任务名暂定为工单名
						mantisIssue.setTask_name(taskName);
					}else {
						//已分配
						String name = taskNameMap.get(taskId);
						if(Util.isNullOrEmpty(name)) {
							mantisIssue.setTask_name("");
						}else {
							mantisIssue.setTask_name(name);
						}
					}
				}else {
					//自建工单
					mantisIssue.setTask_id("");
					mantisIssue.setTask_name((String)workOrder.get(Dict.MAINTASKNAME));
				}
				//如果当前状态为打开并且打开次数不止一次，则状态返回51，前端渲染成重复打开
				if(mantisIssue.getStatus().equals("50")&&mantisIssue.getOpen_num()>1){
					mantisIssue.setStatus("51");
				}
			}
		}
		return list;
	}

	@LazyInitProperty(redisKeyExpression = "tmantis.workorder.${workNo}")
	private Map queryWorkOrder(String workNo)throws Exception{
		return  (Map)restTransport.submit(restTransport.getUrl("query.ftms.workorder"), workNo);
	}




	/**
	 * 应用维度 查询任务缺陷信息
	 */
	@Override
	public List<MantisIssue> queryFtaskMantis(String currentPage, String pageSize, List<String> taskList) throws Exception {
		Integer current_page = Integer.parseInt(currentPage);
		Integer page_size = Integer.parseInt(pageSize);
		Integer start_page = page_size * (current_page - 1);//起始
		return mantisDao.queryTasksMantisPage(String.join(",",taskList), env, start_page, page_size);

	}


	/**
	 * 用户维度 查询用户缺陷信息 总数---分页用
	 */
	@Override
	public Integer queryFtaskMantisCount(List<String> taskList) throws Exception {
        return mantisDao.countTasksMantisPage(String.join(",",taskList), env);
	}

	/**
	 * 应用维度 查询任务缺陷信息 --全量数据
	 */
	@Override
	public List<MantisIssue> queryFtaskMantisAll(List<String> taskList, String includeCloseFlag) throws Exception {
		List<MantisIssue> result = mantisDao.queryTasksMantis(String.join(",",taskList), env,includeCloseFlag);
        for(MantisIssue mantisIssue : result){
            //如果当前状态为打开并且打开次数不止一次，则状态返回51，前端渲染成重复打开
            if(mantisIssue.getStatus().equals("50")&&mantisIssue.getOpen_num()>1){
                mantisIssue.setStatus("51");
            }
        }
	    return result;
	}

	/**
	 * 缺陷更新
	 */
	@Override
	public String updateFdevMantis(Map map) throws Exception {
		String id = String.valueOf(map.get(Dict.ID));
		String serverity = (String) map.get(Dict.SEVERITY);	//严重性
		String priority = (String) map.get(Dict.PRIORITY);	//优先级
		String summary = (String) map.get(Dict.SUMMARY);	//摘要
		String description = (String) map.get(Dict.DESCRIPTION);	//描述
		Integer project = (Integer) map.get(Dict.PROJECT_ID);	//项目编号
		String handler_en_name = (String) map.get(Dict.HANDLER_EN_NAME);//分配给英文名 xxx
		String stage = (String) map.get(Dict.STAGE);//归属阶段
		String reason = (String) map.get(Dict.REASON); //开发原因分析
		String flaw_source = (String) map.get(Dict.FLAW_SOURCE); //缺陷来源
		String system_version = (String) map.get(Dict.SYSTEM_VERSION); //系统版本
		system_version = system_version == null ? "" :system_version;
		String developer = (String) map.get(Dict.DEVELOPER);//开发人员
		String developer_cn = (String) map.get(Dict.DEVELOPER_CN);//开发人员中文名
		String plan_fix_date = (String) map.get(Dict.PLAN_FIX_DATE);//遗留缺陷预计修复时间
		String flaw_type = (String) map.get(Dict.FLAW_TYPE);//缺陷类型
		String status = (String) map.get(Dict.STATUS);
		String taskNo = (String) map.get(Dict.TASK_ID);//任务编号
		String appName_en = (String) map.getOrDefault(Dict.APP_NAME, "");//所属应用
		String reopen_reason = (String) map.getOrDefault(Dict.REOPEN_REASON, "");
		String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(id).toString();
		//原 数据， 用于判断不同
//		Map<String, Object> oldData = mantisService.queryIssueDetail(id);
		//查询缺陷的状态、报告人英文名、处理人英文名和工单号
		Map<String, Object> oldData = mantisDao.queryIssueById(id);
		String oldStatus = String.valueOf(oldData.get(Dict.STATUS));
		if("10".equals(status)){
			//如果修改状态为10，则检测缺陷原状态是否为10，如果不是10,说明数据已经刷新，让用户刷新页面再修改
			if(!"10".equals(oldStatus)){
				logger.error("old status is not 10, please refresh the page");
				throw new FtmsException(ErrorConstants.MANTIS_STATUS_REFRESH);
			}
		}
//		map.put(Dict.OLDMANTISDATA, oldData);
		//判断缺陷 修改流程

		String statusChange = "缺陷状态变化："+ myUtil.getChStatus(oldStatus) + " -> "+ myUtil.getChStatus(status);
		//原始为 新建，后修改 不为 新建 或 打开 时 抛出异常
		if (oldStatus.equals("10") && (!status.equals("10") && !status.equals("50"))){
			logger.error(statusChange);
			throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
		} else if (oldStatus.equals("40") && (!status.equals("40") && !status.equals("80"))){
			//原始为 延期修复，后修改 不为 延期修复 或 已修复 时 抛出异常
			logger.error(statusChange);
			throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
		}else if (oldStatus.equals("50") && (!status.equals("50") && !status.equals("40") && !status.equals("80")&& !status.equals("20"))){
			//原始为 打开，后修改 不为 打开 或 延期修复 或 已修复 或 拒绝 时 抛出异常
			logger.error(statusChange);
			throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
		}
		else if (!oldStatus.equals(status) && (oldStatus.equals("20") || oldStatus.equals("80") || oldStatus.equals("30") || oldStatus.equals("90"))){
			//原始为 拒绝、已修复、确认拒绝、关闭， 当状态发生改变时 抛出异常
			logger.error(statusChange);
			throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
		}

		//判断分配人是否报告人
		if(oldData.get(Dict.REPORTER_EN_NAME).equals(map.get(Dict.HANDLER_EN_NAME))){
			logger.error("can not allocate reporter as handler");
			throw new FtmsException(ErrorConstants.BAD_ALLOCATE_ERROR);
		}
		//如果是将缺陷状态修改为“拒绝”、“延迟修复”，需要先审批，指定小组才支持
		if (false) {
			//auditFlag:审核标志，0-待审核、1-审核完成
			mantisDao.updateMantisAudit(id, "0");
			//查询82、83、84字段数据是否已存在，不存在新增，存在就修改
			int count = mantisDao.queryFieldString(id, new ArrayList<String>(){{
				add("82");//审批后想变成的缺陷状态
				add("83");//审批缺陷开发原因分析
				add("84");//审批后想改的缺陷来源
			}});
			if (count > 0) {
				mantisDao.updateFieldString(id, status, reason, flaw_source);
			} else {
				mantisDao.addFieldString(id, status, reason, flaw_source);
			}
			return "";
		} else {
			Map<String,Object> sendMap = new HashMap<String,Object>();
			sendMap.put(Dict.SUMMARY, summary);
			sendMap.put(Dict.DESCRIPTION, description);
			sendMap.put(Dict.STATUS, myUtil.assemblyParamMap(Dict.ID,status));
			sendMap.put(Dict.PROJECT, myUtil.assemblyParamMap(Dict.ID,project));
			sendMap.put(Dict.HANDLER, myUtil.assemblyParamMap(Dict.NAME,handler_en_name));
			sendMap.put(Dict.PRIORITY, myUtil.assemblyParamMap(Dict.ID,priority));
			sendMap.put(Dict.SEVERITY, myUtil.assemblyParamMap(Dict.ID,serverity));
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
//			if(!Util.isNullOrEmpty(taskNo)){
//				//查询任务的组信息
//				Map taskDetail = apiService.queryInfoByTaskNo(taskNo);
//				if (Util.isNullOrEmpty(taskDetail)) {
//					throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
//				}
//				Map<String, String> group = (Map<String, String>) taskDetail.get(Dict.GROUP);
//				String fdev_groupId = group.get(Dict.ID);
//				String fdev_groupName = group.get(Dict.NAME);
//				list.add(myUtil.assemblyCustomMap(21, fdev_groupName));   //所属fdev小组名称
//				list.add(myUtil.assemblyCustomMap(73, fdev_groupId));   //所属fdev小组id
//			}
			list.add(myUtil.assemblyCustomMap(3,stage));//'归属阶段'
			list.add(myUtil.assemblyCustomMap(4,reason));//'开发原因分析'
			list.add(myUtil.assemblyCustomMap(5,flaw_source));//'缺陷来源'
			list.add(myUtil.assemblyCustomMap(6,system_version));//'系统版本'
			list.add(myUtil.assemblyCustomMap(65,developer));//'开发人员'
			list.add(myUtil.assemblyCustomMap(8,developer_cn));//'开发人员
			list.add(myUtil.assemblyCustomMap(9, Utils.dateStrToLong(plan_fix_date)));//'遗留缺陷预计修复时间'
			list.add(myUtil.assemblyCustomMap(11,flaw_type));//'缺陷类型'
			list.add(myUtil.assemblyCustomMap(19, taskNo));//fdev任务编号
			list.add(myUtil.assemblyCustomMap(72, appName_en));//所属应用
			list.add(myUtil.assemblyCustomMap(81, reopen_reason));//重新打开原因
			sendMap.put(Dict.CUSTOM_FIELDS, list);
			String res;
			try {
				res = mantisRestTemplate.sendPatch(url, mantis_fdev_token, sendMap);
			} catch (Exception e) {
				logger.error("======"+e.getMessage());
				throw new FtmsException(ErrorConstants.UPDATE_MANTIS_ISSUES_ERROR);
			}
			//如果改为以修复或者拒绝,通知玉衡相关人员
			if("80".equals(status)||"20".equals(status)){
				//异步发送玉衡通知
				sendTestManageNotify(String.valueOf(map.get(Dict.REPORTER_EN_NAME)), summary, id);
			}
			//如果分配的指派人变更，发fdev消息通知
			if(!oldData.get(Dict.HANDLER_EN_NAME).equals(map.get(Dict.HANDLER_EN_NAME))){
				sendFdevNotify(String.valueOf(map.get(Dict.HANDLER_EN_NAME)), summary, String.valueOf(oldData.get(Dict.WORKNO)));
			}
			return res;
		}

	}

	/**
	 * 异步发送FDEV通知
	 * @param handlerEnName
	 * @param summary
	 */
	@Async
	private void sendFdevNotify(String handlerEnName, String summary, String workNo) {
		Map<String, String> data = new HashMap<>();
		try {
			data = (Map<String, String>)restTransport.submit(restTransport.getUrl("query.ftms.workorder"), workNo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> target = new ArrayList<>();
		target.add(handlerEnName);
		Map<String, Object> sendData = new HashMap<>();
		sendData.put(Dict.TARGET, target);
		//发送fdev消息接口
		sendData.put(Dict.CONTENT, summary + "-缺陷通知");
		sendData.put(Dict.DESC, Constants.MANTISNOTICE);//缺陷通知
		sendData.put(Dict.TYPE, Constants.DEFAULT_0);
		sendData.put(Dict.REST_CODE,"fdev.fnotify.sendUserNotify");
		sendData.put(Dict.HYPERLINK, restTransport.getUrl("fdev.task.detail.page") + data.getOrDefault(Dict.MAINTASKNO, ""));
		try {
			restTransport.submitSourceBack(sendData);
		} catch (Exception e) {
			logger.error("fail to send fdev nofity");
		}
	}

	/**
	 * 异步发送通知
	 * @param reporterEnName
	 * @param summary
	 * @param id
	 */
	@Async
	public void sendTestManageNotify(String reporterEnName, String summary, String id) {
		List<String> target = new ArrayList<>();
		target.add(reporterEnName);
		Map<String, Object> sendData = new HashMap<>();
		sendData.put(Dict.TARGET, target);
		sendData.put(Dict.CONTENT, "【缺陷变更】" + summary);
		sendData.put(Dict.TYPE, "缺陷变更");
		sendData.put(Dict.REST_CODE,"ftms.notify.url");
		String linkUrl = issueUrl+"/tui/#/MantisIssue/"+id;
		sendData.put(Dict.LINKURI,linkUrl);
		try {
			restTransport.submitSourceBack(sendData);
		} catch (Exception e) {
			logger.error("fail to send ftms nofity"+e.getMessage());
		}
	}


	//将 用户集合的英文名 抽取出 并且用 ，分割开返回拼接后的string
	private String userListToStr(List<Map<String, String>> userList){
		StringBuffer sb = new StringBuffer();
		Iterator<Map<String,String>> iterator = userList.iterator();
		while (iterator.hasNext()){
			Map<String,String> str = iterator.next();
			try {
				sb.append(str.get(Dict.USER_NAME_EN));
			}catch (Exception e){
				logger.error(e.getMessage());
				throw new FtmsException(ErrorConstants.PARAM_ERROR, new String[]{str.toString()});
			}
			if (iterator.hasNext()){
				sb.append(",");
			}
		}
		return sb.toString();
	}


	//将 任务编号 集合抽取出，并且用 ， 分隔开 返回拼接后的string
	private String workNoListToStr(Map<String, String> workNoMap){
		StringBuffer sb = new StringBuffer();
		Set<String> res = workNoMap.keySet();
		Iterator iterator = res.iterator();
		while (iterator.hasNext()){
			sb.append(workNoMap.get(iterator.next()));
			if (iterator.hasNext()){
				sb.append(",");
			}
		}
		return sb.toString();
	}

	@LazyInitProperty(redisKeyExpression = "tuser.fdevuser.mantis.queryuser.${user_name_en}")
	@Override
	public Map<String,Object> queryUser(String user_name_en) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		map.put(com.mantis.dict.Dict.USER_NAME_EN, user_name_en);
		map.put(Dict.REST_CODE, "query.fdev.user.core");
		List list = (List) restTransport.submitSourceBack(map);
		if(Util.isNullOrEmpty(list)) {
			return null;
		}
		return (Map<String,Object>)list.get(0);
	}

	@Override
	public void deleteTaskIssue(String task_id) throws Exception {
		List tasks = new ArrayList();
		tasks.add(task_id);
		List<MantisIssue> issues = queryFtaskMantisAll(tasks, "0");
		for (MantisIssue mantisIssue : issues) {
			String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(mantisIssue.getId()).toString();
			try {
				mantisRestTemplate.sendDelete(url, mantis_fdev_token);
			} catch (Exception e) {
				logger.error("id" + mantisIssue.getId() + e);
			}
		}
	}

	@Override
	public List<Map> queryMantisTask(Map map) {
		String workNo = (String) map.get(Dict.WORKNO);
		Map<String,String> param=new HashMap<>();
		param.put(Dict.WORKNO,workNo);
		param.put(Dict.REST_CODE,"queryAllTaskListByWorkNo");
		List<Map> result=new ArrayList<>();
		try {
			result= (List<Map>) restTransport.submit(param);
		} catch (Exception e) {
			logger.error("fail to query tasks");
		}
		return result;
	}

	@Override
	public void setMantisEnv(String id) {
		mantisDao.setMantisEnv(id, env);
	}

	@Override
	public void updateAllMantisGroup() {
		//查询一年内缺陷
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, -1);
		long time = calendar.getTimeInMillis()/1000;
		List<Map> mantisIssueList = mantisDao.queryMantisLastYear(env, time);
		//查询工单的小组
		Set<String> workNos = mantisIssueList.stream().map(issue -> (String)issue.get(Dict.WORKNO)).collect(Collectors.toSet());
		workNos.remove(null);
		List<Map> workOrderList = apiService.queryWorkOrderByNos(workNos, Arrays.asList(Dict.GROUPNAME));
		Map<String, Map> workOrderMap = new HashMap<>();
		for (Map workOrder : workOrderList) {
			workOrderMap.put((String) workOrder.get(Dict.WORKORDERNO), workOrder);
		}
		//修改缺陷的小组信息
		for (Map issue : mantisIssueList) {
			long id = (long) issue.get(Dict.ID);
			String workNo = (String) issue.get(Dict.WORKNO);
			Map workOrder = workOrderMap.get(workNo);
			if (!Util.isNullOrEmpty(workOrder)) {
				//小组id和名称数据，有则修改，没有添加
				mantisDao.updateMantisGroup(id, String.valueOf(workOrder.get(Dict.FDEVGROUPID)), String.valueOf(workOrder.get(Dict.FDEVGROUPNAME)));
			}
		}
	}

	@Override
	public void updateMantisGroupByWorkNo(String workNo, String groupId) {
		//查询工单下缺陷
		List<Long> mantisIds = mantisDao.queryMantisByWorkNo(workNo);
		Map groupInfo = new HashMap();
		try {
			groupInfo = apiService.queryFdevGroupInfo(groupId);
		} catch (Exception e) {
			logger.info(">>>>>>>updateMantisGroupByWorkNo queryFdevGroupInfo fail,{}", groupId);
		}
		if (!Util.isNullOrEmpty(groupInfo) && !Util.isNullOrEmpty(mantisIds)) {
			String groupName = (String) groupInfo.get(Dict.NAME);
			for (long mantisId : mantisIds) {
				mantisDao.updateMantisGroup(mantisId, groupId, groupName);
			}
		}
	}

}

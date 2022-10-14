package com.spdb.fdev.fdevtask.spdb.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.dao.JiraIssuesDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TaskJira;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.JiraIssuesService;

@Service
@RefreshScope
@SuppressWarnings("all")
public class JiraIssuesServiceImpl implements JiraIssuesService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

	@Autowired
	private JiraIssuesDao jiraIssuesDao;

	@Autowired
	private IFdevTaskService fdevTaskService;

	@Value("${jira.query.url}")
    private String queryUrl;
	@Value("${jira.update.url}")
    private String updateUrl;
	@Value("${jira.fdev.userPwd}")
    private String jiraUserAndPwd;
	@Autowired
    @Lazy
    private IFdevTaskDao fdevTaskDao;
	@Autowired
    private IAppApi iappApi;
	@Autowired
    private IUserApi iUserApi;

	//查询自己名下jira缺陷
	@Override
	public JsonResult queryJiraIssues(Map<String, Object> requestParam) {
		String jql = (String) requestParam.get(Dict.JQL);//查询条件
		String taskId = (String) requestParam.get(Dict.TASKID);//任务ID
		if(CommonUtils.isNullOrEmpty(jql) && CommonUtils.isNullOrEmpty(taskId)) {//查询条件任务ID不可都为空
			 throw new FdevException(ErrorConstants.JQL_TASKID_ERROR);
		}
		List issuesList = new ArrayList();
		if( !CommonUtils.isNullOrEmpty(taskId)) {
			List<TaskJira> keyList = jiraIssuesDao.queryTaskAndJiraIssues(taskId);//查询该任务下缺陷
			if(!CommonUtils.isNullOrEmpty(keyList)) {
				for(TaskJira taskJira: keyList ) {
					//按缺陷编号查询任务详情
					jql = Constants.KEY + taskJira.getJira_key();
					String body = jiraIssuesDao.queryJiraIssues(jql,queryUrl,jiraUserAndPwd);
					if(!CommonUtils.isNullOrEmpty(body)) {
						JSONObject responseJson = JSONObject.parseObject( body );
						issuesList.addAll((List) responseJson.get(Dict.ISSUES));
					}
				}
			}
		} else {
			//查询用户名下所有缺陷详情
			String body = jiraIssuesDao.queryJiraIssues(jql,queryUrl,jiraUserAndPwd);
			if(!CommonUtils.isNullOrEmpty(body)) {
				JSONObject responseJson = JSONObject.parseObject( body );
				issuesList.addAll((List) responseJson.get(Dict.ISSUES));
			}
		}
		
		Map map = new HashMap();
		if(!CommonUtils.isNullOrEmpty(issuesList)) {
			//组装jira缺陷响应报文
			map = responseList(issuesList,taskId);
		}else{
			//为空 默认数量都为0
			List numList = new ArrayList();
			Map numMap = new HashMap();
			if(!CommonUtils.isNullOrEmpty(taskId)) {
				numMap.put(Dict.NEWISSUENUM, 0);
				numMap.put(Dict.OPENISSUENUM, 0);
				numMap.put(Dict.DELAYISSUENUM, 0);
				numMap.put(Dict.WAITISSUENUM, 0);
				numMap.put(Dict.REFUSEISSUENUM, 0);
				numMap.put(Dict.REPAIRISSUENUM, 0);
				numMap.put(Dict.CLOSEDISSUENUM, 0);
				numMap.put(Dict.CLOSEISSUENUM, 0);
				numMap.put(Dict.AFFREFISSUENUM, 0);
				numMap.put(Dict.LEAOVISSUENUM, 0);
				numMap.put(Dict.REOPENISSUENUM, 0);
				numList.add(numMap);
				map.put(Dict.NUMLIST, numList);
			}
		}
		return JsonResultUtil.buildSuccess(map);
	}

	//修改缺陷状态及其他信息
	@Override
	public JsonResult updateJiraIssues(Map<String, Object> requestParam) throws Exception {
		JSONObject updateJson = new JSONObject();
		JSONObject putJson = new JSONObject();
		String status = (String) requestParam.get(Dict.STATUS);//状态码
		String issuekey  = (String) requestParam.get(Dict.ISSUEKEY);//缺陷编号
		updateJson = updateJson(requestParam , issuekey);//组装update请求报文
		
		if(!CommonUtils.isNullOrEmpty(status) && codeList().contains(status)) {//判断是否需要调用put编辑接口
			jiraIssuesDao.updateJiraIssues(updateJson.toJSONString(),updateUrl + issuekey + Constants.TRANSITIONS ,jiraUserAndPwd);
		}else if( !CommonUtils.isNullOrEmpty(status)) {
			putJson = putJson(requestParam , issuekey);//put请求报文
			jiraIssuesDao.putJiraIssues(putJson.toJSONString(),updateUrl + issuekey ,jiraUserAndPwd);
			jiraIssuesDao.updateJiraIssues(updateJson.toJSONString(),updateUrl + issuekey + Constants.TRANSITIONS ,jiraUserAndPwd);
		}else {//分配受理人
			putJson = putJson(requestParam , issuekey);//put请求报文
			jiraIssuesDao.putJiraIssues(putJson.toJSONString(),updateUrl + issuekey ,jiraUserAndPwd);
		}
		return JsonResultUtil.buildSuccess();
	}

	//查询测试中的任务
	@Override
	public JsonResult queryTestTask(Map<String, Object> requestParam) {
		String id = (String) requestParam.get(Dict.ID);//人员ID
		List<FdevTask> fdevTask = jiraIssuesDao.queryTestTask(id);
		return JsonResultUtil.buildSuccess(fdevTask);
	}

	//保存任务与jira缺陷
	@Override
	public JsonResult saveTaskAndJiraIssues(Map<String, Object> requestParam) {
		TaskJira taskJira =new TaskJira();
		taskJira.setJira_id((String) requestParam.get(Dict.ISSUEID));//缺陷ID
		taskJira.setJira_key(((String) requestParam.get(Dict.ISSUEKEY)));//缺陷编号
		taskJira.setTask_id(((String) requestParam.get(Dict.TASKID)));//任务ID
		taskJira.setTask_name(((String) requestParam.get(Dict.TASKNAME)));//任务名称
		taskJira.setDemand_id(((String) requestParam.get(Dict.RQRMNT_NO)));//需求ID
		jiraIssuesDao.saveTaskAndJiraIssues(taskJira);
		return JsonResultUtil.buildSuccess();
	}
	
	//已修正状态码List
	private List  codeList() {
		List codeList = new ArrayList();
		codeList.add("11");//已修正
		codeList.add("131");//已修正
		codeList.add("121");//已修正
		return codeList;
	}
	
	//put请求报文
	private JSONObject putJson(Map<String, Object> requestParam, String issuekey) throws Exception {
		JSONObject requestJson = new JSONObject();	
		JSONObject fields = new JSONObject();
		JSONObject assignee = new JSONObject();//受理人
		JSONObject develop = new JSONObject();//开发人员
		JSONObject orderSeverity = new JSONObject();//严重程度
		JSONObject defectType = new JSONObject();//缺陷类型
		JSONObject defectSource = new JSONObject();//缺陷来源
		JSONObject iterations = new JSONObject();//迭代批次
		JSONObject belongStage = new JSONObject();//归属阶段
		JSONObject priority = new JSONObject();//优先级
		assignee.put(Dict.NAME,requestParam.get(Dict.ASSIGNEEEN));
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEVELOPEN))) {
			develop.put(Dict.NAME,requestParam.get(Dict.DEVELOPEN));
			fields.put(Dict.DEVELOP1, develop);//开发人员
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ORDERSEVERITY))) {
			orderSeverity.put(Dict.ID,requestParam.get(Dict.ORDERSEVERITY));
			fields.put(Dict.ORDERSEVERITY1, orderSeverity);//严重程度
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEFECTTYPE))) {
			defectType.put(Dict.ID,requestParam.get(Dict.DEFECTTYPE));
			fields.put(Dict.DEFECTTYPE1, defectType);//缺陷类型
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEFECTSOURCE))) {
			defectSource.put(Dict.ID,requestParam.get(Dict.DEFECTSOURCE));
			fields.put(Dict.DEFECTSOURCE1, defectSource);//缺陷来源
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ITERATIONS))) {
			iterations.put(Dict.ID,requestParam.get(Dict.ITERATIONS));
			fields.put(Dict.ITERATIONS1, iterations);//迭代批次
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.BELONGSTAGE))) {
			belongStage.put(Dict.ID,requestParam.get(Dict.BELONGSTAGE));
			fields.put(Dict.BELONGSTAGE1, belongStage);//归属阶段
		}
		if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.PRIORITY))) {
			priority.put(Dict.ID,requestParam.get(Dict.PRIORITY));
			fields.put(Dict.PRIORITY, priority);//优先级
		}
		
		fields.put(Dict.ASSIGNEE, assignee );//受理人
		fields.put(Dict.SUMMARY, requestParam.get(Dict.SUMMARY));//摘要
		fields.put(Dict.DESCRIPTION, requestParam.get(Dict.DESCRIPTION));//描述
		fields.put(Dict.UNITNO1, requestParam.get(Dict.UNITNO));//对应研发单元编号
		fields.put(Dict.DEFECTCAUSE1, requestParam.get(Dict.DEFECTCAUSE));//缺陷原因
		fields.put(Dict.ISSUESYSTEM, queryTask(issuekey));//缺陷跟因系统
		requestJson.put(Dict.FIELDS,fields );//修改字段
		return requestJson;
	}

	//组装update请求报文
	private JSONObject updateJson(Map<String, Object> requestParam, String issuekey) throws Exception {
		JSONObject requestJson = new JSONObject();	
		String status = (String) requestParam.get(Dict.STATUS);//状态码
		if(codeList().contains(status)) {
			JSONObject fields = new JSONObject();
			JSONObject assignee = new JSONObject();//受理人
			JSONObject develop = new JSONObject();//开发人员
			JSONObject orderSeverity = new JSONObject();//严重程度
			JSONObject defectType = new JSONObject();//缺陷类型
			JSONObject defectSource = new JSONObject();//缺陷来源
			JSONObject iterations = new JSONObject();//迭代批次
			JSONObject belongStage = new JSONObject();//归属阶段
			JSONObject priority = new JSONObject();//优先级
			assignee.put(Dict.NAME,requestParam.get(Dict.ASSIGNEEEN));
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.PRIORITY))) {
				priority.put(Dict.ID,requestParam.get(Dict.PRIORITY));
				fields.put(Dict.PRIORITY, priority);//优先级
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEVELOPEN))) {
				develop.put(Dict.NAME,requestParam.get(Dict.DEVELOPEN));
				fields.put(Dict.DEVELOP1, develop);//开发人员
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ORDERSEVERITY))) {
				orderSeverity.put(Dict.ID,requestParam.get(Dict.ORDERSEVERITY));
				fields.put(Dict.ORDERSEVERITY1, orderSeverity);//严重程度
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEFECTTYPE))) {
				defectType.put(Dict.ID,requestParam.get(Dict.DEFECTTYPE));
				fields.put(Dict.DEFECTTYPE1, defectType);//缺陷类型
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.DEFECTSOURCE))) {
				defectSource.put(Dict.ID,requestParam.get(Dict.DEFECTSOURCE));
				fields.put(Dict.DEFECTSOURCE1, defectSource);//缺陷来源
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ITERATIONS))) {
				iterations.put(Dict.ID,requestParam.get(Dict.ITERATIONS));
				fields.put(Dict.ITERATIONS1, iterations);//迭代批次
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.BELONGSTAGE))) {
				belongStage.put(Dict.ID,requestParam.get(Dict.BELONGSTAGE));
				fields.put(Dict.BELONGSTAGE1, belongStage);//归属阶段
			}
			if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.PRIORITY))) {
				priority.put(Dict.ID,requestParam.get(Dict.PRIORITY));
				fields.put(Dict.PRIORITY, priority);//优先级
			}
			fields.put(Dict.ASSIGNEE, assignee );//受理人
			fields.put(Dict.SUMMARY, requestParam.get(Dict.SUMMARY));//摘要
			fields.put(Dict.DESCRIPTION, requestParam.get(Dict.DESCRIPTION));//描述
			fields.put(Dict.UNITNO1, requestParam.get(Dict.UNITNO));//对应研发单元编号
			fields.put(Dict.DEFECTCAUSE1, requestParam.get(Dict.DEFECTCAUSE));//缺陷原因
			
			fields.put(Dict.ISSUESYSTEM, queryTask(issuekey));//缺陷跟因系统
			requestJson.put(Dict.FIELDS,fields );//修改字段
		}
		JSONObject transition = new JSONObject();
		transition.put(Dict.ID, status);//状态码
		requestJson.put(Dict.TRANSITION,transition );//状态码JSON
		return requestJson;
	}
	
	//获取缺陷跟因系统
	private String  queryTask(String issuekey) throws Exception {
		String issueSystem = "个人手机银行系统";
		TaskJira taskJira = jiraIssuesDao.queryTaskName(issuekey);//查询任务信息
		if(!CommonUtils.isNullOrEmpty(taskJira)) {
			String taskId = taskJira.getTask_id();//任务ID
			FdevTask task = new FdevTask();
			task.setId(taskId);
			List<FdevTask> list = fdevTaskDao.queryAll(task);
			if (!CommonUtils.isNullOrEmpty(list)) {
				FdevTask fdevTask = list.get(0);
				String projectId = fdevTask.getProject_id();//项目ID
				String appType = fdevTask.getApplicationType();//项目类型
				Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(projectId, appType);
		        if(!CommonUtils.isNullOrEmpty(projectInfo)) {
					 issueSystem = (String) projectInfo.getOrDefault(Dict.ISSUESYSTEM,"个人手机银行系统");
		        }
	        }
		}
		return issueSystem;
	}
	
	//组装jira缺陷响应报文
	private Map responseList( List issuesList ,String taskId ) {
		Map dataMap = new HashMap();
		List list = new ArrayList();
		
		int newIssueNum = 0;//新建缺陷的数量
		int openIssueNum = 0;//打开缺陷的数量
		int delayIssueNum = 0;//延迟修复缺陷的数量
		int waitIssueNum = 0;//待确认缺陷的数量
		int refuseIssueNum = 0;//拒绝缺陷的数量
		int repairIssueNum = 0;//已修复缺陷的数量
		int closedIssueNum = 0;//已关闭缺陷的数量
		int closeIssueNum = 0;//关闭缺陷的数量
		int affRefIssueNum = 0;//确认拒绝缺陷的数量
		int leaOvIssueNum = 0;//遗留缺陷的数量
		int reopenIssueNum = 0;//重新打开缺陷的数量
		for(Object issuesObj : issuesList) {
			JSONObject issuesJson = JSONObject.parseObject(String.valueOf(issuesObj));
			JSONObject issuesFields = JSONObject.parseObject(String.valueOf(issuesJson.get(Dict.FIELDS)));
			Object issuetype = issuesFields.get(Dict.ISSUETYPE);
			if(!CommonUtils.isNullOrEmpty(issuetype)&&
					"10602".equals(JSONObject.parseObject(String.valueOf(issuetype)).get(Dict.ID))) {//判断是否是UAT缺陷
				Map<String, Object> map = new HashMap();
				map.put(Dict.CREATED, issuesFields.get(Dict.CREATED));//创建时间
				map.put(Dict.SUMMARY, issuesFields.get(Dict.SUMMARY));//摘要
				map.put(Dict.DESCRIPTION, issuesFields.get(Dict.DESCRIPTION));//描述
				map.put(Dict.ISSUEKEY, issuesJson.get(Dict.KEY));//缺陷编号
				map.put(Dict.ID, issuesJson.get(Dict.ID));//缺陷ID
				map.put(Dict.UNITNO, issuesFields.get(Dict.UNITNO1));//对应研发单元编号
				map.put(Dict.DEFECTCAUSE, issuesFields.get(Dict.DEFECTCAUSE1));//缺陷原因
				Object creator = issuesFields.get(Dict.CREATOR);
				Object status = issuesFields.get(Dict.STATUS);
				Object priority = issuesFields.get(Dict.PRIORITY);
				Object assignee = issuesFields.get(Dict.ASSIGNEE);
				Object develop = issuesFields.get(Dict.DEVELOP1);
				Object orderSeverity = issuesFields.get(Dict.ORDERSEVERITY1);
				Object defectType = issuesFields.get(Dict.DEFECTTYPE1);
				Object defectSource = issuesFields.get(Dict.DEFECTSOURCE1);
				Object belongStage = issuesFields.get(Dict.BELONGSTAGE1);
				Object iterations = issuesFields.get(Dict.ITERATIONS1);
				String key = (String) issuesJson.get(Dict.KEY);
				if(!CommonUtils.isNullOrEmpty(key)) {
					TaskJira taskJira = jiraIssuesDao.queryTaskName(key);//查询任务名称
					if(!CommonUtils.isNullOrEmpty(taskJira)) {
						map.put(Dict.TASKNAME, taskJira.getTask_name());//任务名称
						map.put(Dict.TASKID, taskJira.getTask_id());//任务ID
					}
				}
				map.put(Dict.ISSUEKEY, key);//缺陷编号
				String statusId = "";
				if(!CommonUtils.isNullOrEmpty(iterations)) {
					map.put(Dict.ITERATIONS, JSONObject.parseObject(String.valueOf(iterations)).get(Dict.ID));//迭代批次
				}
				if(!CommonUtils.isNullOrEmpty(creator)) {
					map.put(Dict.CREATOR, JSONObject.parseObject(String.valueOf(creator)).get(Dict.DISPLAYNAME));//提出者
				}
				if(!CommonUtils.isNullOrEmpty(status)) {
					statusId = (String) JSONObject.parseObject(String.valueOf(status)).get(Dict.ID);
					map.put(Dict.STATUS, statusId);//状态
				}
				if(!CommonUtils.isNullOrEmpty(priority)) {
					map.put(Dict.PRIORITY, JSONObject.parseObject(String.valueOf(priority)).get(Dict.ID));//优先级
				}
				if(!CommonUtils.isNullOrEmpty(assignee)) {
					map.put(Dict.ASSIGNEE, JSONObject.parseObject(String.valueOf(assignee)).get(Dict.DISPLAYNAME));//受理人
					map.put(Dict.ASSIGNEEEN, JSONObject.parseObject(String.valueOf(assignee)).get(Dict.NAME));//受理人英文名
				}
				if(!CommonUtils.isNullOrEmpty(develop)) {
					map.put(Dict.DEVELOP, JSONObject.parseObject(String.valueOf(develop)).get(Dict.DISPLAYNAME));//开发人员
					map.put(Dict.DEVELOPEN, JSONObject.parseObject(String.valueOf(develop)).get(Dict.NAME));//开发人员英文名
				}
				if(!CommonUtils.isNullOrEmpty(orderSeverity)) {
					map.put(Dict.ORDERSEVERITY, JSONObject.parseObject(String.valueOf(orderSeverity)).get(Dict.ID));//严重程度
				}
				if(!CommonUtils.isNullOrEmpty(defectType)) {
					map.put(Dict.DEFECTTYPE, JSONObject.parseObject(String.valueOf(defectType)).get(Dict.ID));//缺陷类型
				}
				if(!CommonUtils.isNullOrEmpty(defectSource)) {
					map.put(Dict.DEFECTSOURCE, JSONObject.parseObject(String.valueOf(defectSource)).get(Dict.ID));//缺陷来源
				}
				if(!CommonUtils.isNullOrEmpty(belongStage)) {
					map.put(Dict.BELONGSTAGE, JSONObject.parseObject(String.valueOf(belongStage)).get(Dict.ID));//归属阶段
				}
				list.add(map);
				if( !CommonUtils.isNullOrEmpty(taskId) ) {
					switch (statusId) {
						case "10013"://新建
							newIssueNum++;//新建缺陷的数量
							break;
						case "1"://打开
							openIssueNum++;//打开缺陷的数量
							break;
						case "10307"://重新打开
							reopenIssueNum++;//重新打开缺陷的数量
							break;
						case "10310"://延迟修复
							delayIssueNum++;//延迟修复缺陷的数量
							break;
						case "10306"://拒绝
							waitIssueNum++;//待确认缺陷的数量
							refuseIssueNum++;//拒绝缺陷的数量
							break;
						case "10015"://已修复
							waitIssueNum++;//待确认缺陷的数量
							repairIssueNum++;//已修复缺陷的数量
							break;
						case "10012"://关闭
							closedIssueNum++;//已关闭缺陷的数量
							closeIssueNum++;//关闭缺陷的数量
							break;
						case "10308"://确认拒绝
							closedIssueNum++;//已关闭缺陷的数量
							affRefIssueNum++;//确认拒绝缺陷的数量
							break;
						case "10737"://遗留
							leaOvIssueNum++;//遗留缺陷的数量
							break;
						default:
							break;
					}
				}
			
			}
			dataMap.put(Dict.ISSUESDATA, list);
			List numList = new ArrayList();
			Map numMap = new HashMap();
			if(!CommonUtils.isNullOrEmpty(taskId)) {
				numMap.put(Dict.NEWISSUENUM, newIssueNum);
				numMap.put(Dict.OPENISSUENUM, openIssueNum);
				numMap.put(Dict.DELAYISSUENUM, delayIssueNum);
				numMap.put(Dict.WAITISSUENUM, waitIssueNum);
				numMap.put(Dict.REFUSEISSUENUM, refuseIssueNum);
				numMap.put(Dict.REPAIRISSUENUM, repairIssueNum);
				numMap.put(Dict.CLOSEDISSUENUM, closedIssueNum);
				numMap.put(Dict.CLOSEISSUENUM, closeIssueNum);
				numMap.put(Dict.AFFREFISSUENUM, affRefIssueNum);
				numMap.put(Dict.LEAOVISSUENUM, leaOvIssueNum);
				numMap.put(Dict.REOPENISSUENUM, reopenIssueNum);
				numList.add(numMap);
				dataMap.put(Dict.NUMLIST, numList);
			}
		}
			
		return dataMap ;
	}

	/**
	 * 根据key值查询故事
	 * @param key
	 * @return
	 */
	@Override
	public Map<String,Object> queryJiraStoryByKey(String key) {
		String jql = Constants.KEY + key;
		String body = null;
		try {
			body = jiraIssuesDao.queryJiraIssues(jql,queryUrl,jiraUserAndPwd);
		} catch (Exception e) {
			logger.info("JIRA服务异常或该故事id不存在:" + key);
			throw new FdevException("JIRA服务异常或该故事id不存在");
		}
		JSONObject jsonObject = JSONObject.parseObject(body);
		List<JSONObject> issues = (List<JSONObject>)jsonObject.get(Dict.ISSUES);
		if(CommonUtils.isNullOrEmpty(issues)){
			throw new FdevException("该故事id不存在");
		}
		JSONObject jsonObject1 = issues.get(0);
		JSONObject fields = (JSONObject) jsonObject1.get(Dict.FIELDS);
		JSONObject issueType = (JSONObject)fields.get(Dict.ISSUETYPE);
		String issueTypeId = (String)issueType.get(Dict.ID);
		if(!"10001".equals(issueTypeId)){
			throw new FdevException("该id对应的不是一个故事,请确认故事id是否正确");
		}
		//构造返回数据
		return new HashMap<String, Object>(){{
			put(Dict.ISSUETYPE,issueType);
			put(Dict.STATUS,fields.get(Dict.STATUS));
			put(Dict.PROJECT,fields.get(Dict.PROJECT));
			put(Dict.SUMMARY,fields.get(Dict.SUMMARY));
		}};
	}

	/**
	 * 根据key值查询故事,前端调用
	 * @param key
	 * @return
	 */
	@Override
	public Map<String, Object> queryJiraStory(String key) {
		Map<String, Object> result = new HashMap<>();
		String jql = Constants.KEY + key;
		String body = null;
		try {
			body = jiraIssuesDao.queryJiraIssues(jql,queryUrl,jiraUserAndPwd);
		} catch (Exception e) {
			logger.info("JIRA服务异常或该故事id不存在:" + key);
			result.put(Dict.RESULT,false);
			result.put(Dict.ERR_MSG,"JIRA服务异常或该故事id不存在:" + key);
			return result;
		}
		JSONObject jsonObject = JSONObject.parseObject(body);
		List<JSONObject> issues = (List<JSONObject>)jsonObject.get(Dict.ISSUES);
		if(CommonUtils.isNullOrEmpty(issues)){
			result.put(Dict.RESULT,false);
			result.put(Dict.ERR_MSG,"该故事id不存在:" + key);
			return result;
		}
		JSONObject jsonObject1 = issues.get(0);
		JSONObject fields = (JSONObject) jsonObject1.get(Dict.FIELDS);
		JSONObject issueType = (JSONObject)fields.get(Dict.ISSUETYPE);
		String issueTypeId = (String)issueType.get(Dict.ID);
		if(!"10001".equals(issueTypeId)){
			result.put(Dict.RESULT,false);
			result.put(Dict.ERR_MSG,"该id对应的不是一个故事,请确认故事id是否正确");
			return result;
		}
		//构造返回数据
		return new HashMap<String, Object>(){{
			put(Dict.RESULT,true);
			put(Dict.ERR_MSG,"");
			put(Dict.ISSUETYPE,issueType);
			put(Dict.STATUS,fields.get(Dict.STATUS));
			put(Dict.PROJECT,fields.get(Dict.PROJECT));
			put(Dict.SUMMARY,fields.get(Dict.SUMMARY));
		}};
	}

	/**
	 * 通过故事key创建子任务
	 * @param storyKey
	 */
	@Override
	public String createJiraSubTask(FdevTask task) {
		logger.info("创建jira子任务:" + task.getId());
		String creator = task.getCreator();
		String[] developer = task.getDeveloper();
		String storyKey = task.getStoryId();
		Map<String, Object> story = queryJiraStoryByKey(storyKey);
		JSONObject fields = new JSONObject();
		JSONObject project = new JSONObject();//项目
		JSONObject parent = new JSONObject();//父故事
		JSONObject issuetype = new JSONObject();//类型：子任务
		JSONObject reporter = new JSONObject();//报告人
		//构造请求参数
		project.put(Dict.KEY,((Map)(story.get(Dict.PROJECT))).get(Dict.KEY));
		parent.put(Dict.KEY,storyKey);
		reporter.put(Dict.NAME,name(creator));
		issuetype.put(Dict.ID,Constants.SUB_TASK);
		fields.put(Dict.PROJECT,project);
		fields.put(Dict.PARENT,parent);
		fields.put(Dict.ISSUETYPE,issuetype);
		fields.put(Dict.SUMMARY,task.getName());
		fields.put(Dict.REPORTER,reporter);
		JSONObject jsonObjectParams = new JSONObject() {{
			put(Dict.FIELDS, fields);
		}};
		String body = jiraIssuesDao.createJiraSubTask(jsonObjectParams.toJSONString(), updateUrl, jiraUserAndPwd);
		JSONObject jsonObject = JSONObject.parseObject(body);
		logger.info("创建jira子任务成功:" + jsonObject.get(Dict.KEY));
		return (String) jsonObject.get(Dict.KEY);
	}

	/**
	 * 更新故事或子任务状态
	 * @param key 故事/子任务key
	 * @param destStatusId 目标状态  11-待办  21-处理中  31-完成  41-Testing
	 */
	@Override
	public void updateJiraTaskStatus(String key, String destStatusId) {
		JSONObject transition = new JSONObject();
		transition.put(Dict.ID,destStatusId);
		JSONObject jsonObjectParams = new JSONObject() {{
			put(Dict.TRANSITION, transition);
		}};
		//String s = jiraIssuesDao.queryJiraIssues("", updateUrl + key + Constants.TRANSITIONS, jiraUserAndPwd);
		jiraIssuesDao.updateJiraIssues(jsonObjectParams.toJSONString(),updateUrl + key + Constants.TRANSITIONS,jiraUserAndPwd);
	}

	/**
	 * 删除子任务
	 * @param key
	 */
	@Override
	public void deleteJiraSubTask(String key) {
		jiraIssuesDao.deleteJiraSubTask(key,updateUrl,jiraUserAndPwd);
	}

	private String name(String id){
		Map params = new HashMap();
		params.put(Dict.ID,id);
		Map map = null;
		try {
			map = iUserApi.queryUser(params);
		} catch (Exception e) {
			logger.error("查询用户信息失败");
			throw new FdevException("查询用户信息失败");
		}
		return (String) map.get(Dict.USER_NAME_EN);
	}

	@Override
	public void updateJiraSubTask(String key,String developerId) {
		JSONObject fields = new JSONObject();
		JSONObject assignee = new JSONObject();
		assignee.put(Dict.NAME,CommonUtils.isNullOrEmpty(developerId)?"":name(developerId));
		fields.put(Dict.ASSIGNEE,assignee);
		JSONObject jsonObjectParams = new JSONObject() {{
			put(Dict.FIELDS, fields);
		}};
		jiraIssuesDao.putJiraIssues(jsonObjectParams.toJSONString(),updateUrl + key,jiraUserAndPwd);
	}
}

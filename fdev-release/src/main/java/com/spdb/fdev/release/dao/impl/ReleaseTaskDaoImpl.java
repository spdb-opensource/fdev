package com.spdb.fdev.release.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.IComponenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IReleaseTaskDao;

@Repository
public class ReleaseTaskDaoImpl extends FreleaseDao implements IReleaseTaskDao {
	@Resource
	private MongoTemplate mongoTemplate;
	@Autowired
    private IComponenService componenService;

	@Override
	public Map<String, Object> queryDetailByTaskId(String taskid, String type) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).is(taskid).and(Dict.TASK_STATUS).ne("2"));
		if(!CommonUtils.isNullOrEmpty(type)) {
			query.addCriteria(Criteria.where(Dict.TYPE).is(type));
		} else {
			query.addCriteria(Criteria.where(Dict.TYPE).in("1", "2"));
		}
		// 通过taskid查询releasetask对象
		ReleaseTask releaseTask = this.mongoTemplate.findOne(query, ReleaseTask.class);
		if (CommonUtils.isNullOrEmpty(releaseTask)) {
			return null;
		}
		Query queryApp = new Query(Criteria.where(Dict.APPLICATION_ID).is(releaseTask.getApplication_id())
				.and(Dict.RELEASE_NODE_NAME).is(releaseTask.getRelease_node_name()));
		// 通过releasetask相关属性查询releaseapplication信息
		ReleaseApplication releaseApplication = this.mongoTemplate.findOne(queryApp, ReleaseApplication.class);
		
		Query queryNode = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseTask.getRelease_node_name()));
		// 通过releasetask相关属性查询releaseNode信息
		ReleaseNode releaseNode = this.mongoTemplate.findOne(queryNode, ReleaseNode.class);
		Map<String, Object> nodemap = new HashMap<>();
		if(!CommonUtils.isNullOrEmpty(releaseNode)) {
			nodemap.put(Dict.RELEASE_NODE_NAME, releaseNode.getRelease_node_name());
			nodemap.put(Dict.RELEASE_DATE, releaseNode.getRelease_date());
			nodemap.put(Dict.CREATE_USER, releaseNode.getCreate_user());
			nodemap.put(Dict.OWNER_GROUPID, releaseNode.getOwner_groupId());
			nodemap.put(Dict.RELEASE_MANAGER, releaseNode.getRelease_manager());
			nodemap.put(Dict.RELEASE_SPDB_MANAGER, releaseNode.getRelease_spdb_manager());
			nodemap.put(Dict.NODE_STATUS, releaseNode.getNode_status());
			if(!CommonUtils.isNullOrEmpty(releaseApplication)) {
				nodemap.put(Dict.UAT_ENV_NAME, releaseApplication.getUat_env_name());
				nodemap.put(Dict.REL_ENV_NAME, releaseApplication.getRel_env_name());
			}else {
				nodemap.put(Dict.UAT_ENV_NAME, "");
				nodemap.put(Dict.REL_ENV_NAME, "");
			}
		}
		Map<String, Object> appmap = new HashMap<>();
		List<String> tag_list=new ArrayList<String>();
		List<String> uri_list=new ArrayList<String>();
		if(!CommonUtils.isNullOrEmpty(releaseApplication)) {
			if ("4".equals(type) || "5".equals(type) || "6".equals(type)) {
				tag_list = componenService.queryTagList(releaseApplication.getRelease_node_name(), releaseApplication.getApplication_id());
			} else {
				Query queryRel=new Query(Criteria.where(Dict.APPLICATION_ID)
						.is(releaseApplication.getApplication_id())
						.and(Dict.RELEASE_NODE_NAME)
						.is(releaseApplication.getRelease_node_name()));
				queryRel.with(Sort.by(Sort.Order.desc(Dict.PRODUCT_TAG)));
				List<RelDevopsRecord> relDevopsRecords = mongoTemplate.find(queryRel, RelDevopsRecord.class);
				tag_list=new ArrayList<String>();
				uri_list=new ArrayList<String>();
				for (RelDevopsRecord relDevopsRecord : relDevopsRecords) {
					if (!CommonUtils.isNullOrEmpty(relDevopsRecord.getProduct_tag())) {
						tag_list.add(relDevopsRecord.getProduct_tag());
					}
					if (!CommonUtils.isNullOrEmpty(relDevopsRecord.getPro_image_uri())) {
						uri_list.add(relDevopsRecord.getPro_image_uri());
					}
				}
			}
			appmap.put(Dict.APPLICATION_ID, releaseApplication.getApplication_id());
			appmap.put(Dict.RELEASE_BRANCH, releaseApplication.getRelease_branch());
			appmap.put(Dict.PRODUCT_TAG, tag_list);
			appmap.put(Dict.PRO_IMAGE_URI, uri_list);
		}
		nodemap.put(Dict.RELEASE_APPLICATION, appmap);
		Map<String, String> taskMap = new HashMap<>();
		taskMap.put(Dict.TASK_ID, releaseTask.getTask_id());
		taskMap.put(Dict.TASK_STATUS, releaseTask.getTask_status());
		taskMap.put(Dict.REJECT_REASON, releaseTask.getReject_reason());
		nodemap.putAll(taskMap);
		return nodemap;
	}

	@Override
	public ReleaseTask addTask(String application_id, String task_id, String release_node_name, String type, String rqrmntNo) throws Exception {
		ReleaseTask releaseTask = new ReleaseTask();
		releaseTask.setTask_id(task_id);
		releaseTask.setApplication_id(application_id);
		releaseTask.setRelease_node_name(release_node_name);
		releaseTask.setTask_status(Constants.TASK_RLS_NEED_AUDIT);// 0-投产待审核
        releaseTask.setType(type);
        releaseTask.setCreate_time(CommonUtils.formatDate("yyyy-MM-dd"));
        releaseTask.setRqrmnt_no(rqrmntNo);
		return this.mongoTemplate.save(releaseTask);// 插入投产任务
	}

	@Override
	public ReleaseTask deleteTask(String task_id, String type) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).is(task_id).and(Dict.TYPE).is(type));
		// 查询条件为taskid
		// 删除投产应用下对应的投产任务
		return mongoTemplate.findAndRemove(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryTasks(Map<String, String> requestParam) throws Exception {
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		String application_id = requestParam.get(Dict.APPLICATION_ID);
		String type = requestParam.get(Dict.TYPE);
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(release_node_name);
		if(CommonUtils.isNullOrEmpty(type)) {
			criteria = criteria.and(Dict.TYPE).in("1", "2");
		} else {
			criteria = criteria.and(Dict.TYPE).is(type);
		}
		List<ReleaseTask> list = new ArrayList<>();
		if (application_id == null) {// 若无应用id 则条件只为release_node_name
			Query query = new Query(criteria);
			list = this.mongoTemplate.find(query, ReleaseTask.class);
			return list;
		} else {// 若有应用id 则条件只为release_node_name和application_id
			criteria = criteria.and(Dict.APPLICATION_ID).is(application_id);
			Query query = new Query(criteria);
			if ("4".equals(type) || "5".equals(type) || "6".equals(type)) {
				list = this.mongoTemplate.find(query, ReleaseTask.class);
			} else {
				ReleaseTask findOne = this.mongoTemplate.findOne(query, ReleaseTask.class);
				if (!CommonUtils.isNullOrEmpty(findOne)) {
					list.add(findOne);
				}
			}
			return list;
		}

	}

	@Override
	public ReleaseTask auditAdd(String task_id, String release_node_name, String operation_type,String reject_reason) throws Exception {
		if(CommonUtils.isNullOrEmpty(reject_reason)) {
			reject_reason = "";
		}
		Query query = new Query(
				Criteria.where(Dict.TASK_ID).is(task_id).and(Dict.RELEASE_NODE_NAME).is(release_node_name));
		Update update = Update.update(Dict.TASK_ID, task_id).set(Dict.RELEASE_NODE_NAME, release_node_name)
				.set(Dict.TASK_STATUS, operation_type).set(Dict.REJECT_REASON, reject_reason)
				.set(Dict.UPDATE_TIME,CommonUtils.formatDate("yyyy-MM-dd"));
		// 审核投产任务 修改起task_status
		this.mongoTemplate.findAndModify(query, update, ReleaseTask.class);
		return this.mongoTemplate.findOne(query, ReleaseTask.class);
	}

	@Override
	public ReleaseTask findOneTask(String task_id, String type) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).is(task_id));
		if(!CommonUtils.isNullOrEmpty(type)) {
			query.addCriteria(Criteria.where(Dict.TYPE).is(type));
		} else {
			query.addCriteria(Criteria.where(Dict.TYPE).in("1", "2"));
		}
		return mongoTemplate.findOne(query, ReleaseTask.class);
	}

	@Override
	public ReleaseTask updateReleaseTask(ReleaseTask releaseTask) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).is(releaseTask.getTask_id())
                .and(Dict.TYPE).is(releaseTask.getType()));
		Update update = Update.update(Dict.TASK_ID, releaseTask.getTask_id()).set(Dict.TASK_STATUS,
				releaseTask.getTask_status()).set(Dict.REJECT_REASON, releaseTask.getReject_reason())
				.set(Dict.RELEASE_NODE_NAME, releaseTask.getRelease_node_name());
		mongoTemplate.findAndModify(query, update, ReleaseTask.class);
		return mongoTemplate.findOne(query, ReleaseTask.class);
	}


	public List<ReleaseTask> queryTaskStatusOfApp(String release_node_name, String application_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name)
				.and(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.TASK_STATUS).is(Constants.TASK_RLS_NEED_AUDIT));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryReleaseTaskByAppid(String appid, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(appid)
				.and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name)
				.and(Dict.TASK_STATUS)
				.nin(Constants.TASK_RLS_REJECTED));
		return this.mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
    public List<ReleaseTask> querySameAppTask(String application_id, String release_node_name) throws Exception {
		Query query =new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(application_id)
				.and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryTaskByYestDayNode() throws Exception {
		List<ReleaseTask> taskList=new ArrayList<>();
		String yesterday = CommonUtils.formatYesterDay(CommonUtils.DATE_PARSE);
		Query query =new Query(Criteria.where(Dict.RELEASE_DATE).is(yesterday));
		List<ReleaseNode> nodes = mongoTemplate.find(query, ReleaseNode.class);
		for (ReleaseNode releaseNode : nodes) {		
			Query querytask = new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
					.is(releaseNode.getRelease_node_name())
					.and(Dict.TASK_STATUS)
					.is(Constants.OPERATION_TYPR_ACCESS));
			 List<ReleaseTask> tasks = mongoTemplate.find(querytask, ReleaseTask.class);
			 taskList.addAll(tasks);
		}
		return taskList;
	}

	@Override
	public List<String> releaseTaskByNodeName(List<String> release_node_names) throws Exception {
		List<String> taskList =new ArrayList<>();
		for (String relase_node_name : release_node_names) {		
			Query querytask = new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
					.is(relase_node_name));
			 List<ReleaseTask> tasks = mongoTemplate.find(querytask, ReleaseTask.class);
			 for (ReleaseTask releaseTask : tasks) {
				 taskList.add(releaseTask.getTask_id());
			}
		}
		return taskList;
	}

	@Override
	public void updateTaskProduction(String task_id) throws Exception {
		Query query =new Query(Criteria.where(Dict.TASK_ID).is(task_id).and(Dict.TYPE).in("1", "2"));
		Update update=Update.update(Dict.TASK_STATUS, Constants.TASK_RLS_PRODUCTION);
		mongoTemplate.findAndModify(query, update, ReleaseTask.class);
	}

	@Override
	public void updateTaskNode(ReleaseTask releaseTask, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).is(releaseTask.getTask_id())
                .and(Dict.TYPE).is(releaseTask.getType()));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
		mongoTemplate.findAndModify(query, update, ReleaseTask.class);
	}

	@Override
	public List<String> queryTaskByNodeDate(String archivedDay) throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_DATE).is(archivedDay));
		List<ReleaseNode> releaseNodes = mongoTemplate.find(query, ReleaseNode.class);
		List<String> task_ids= new ArrayList<>();
		for (ReleaseNode releaseNode : releaseNodes) {
			Query queryTask=new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
					.is(releaseNode.getRelease_node_name()));
			List<ReleaseTask> releaseTasks = mongoTemplate.find(queryTask, ReleaseTask.class);
			for (ReleaseTask releaseTask : releaseTasks) {
				task_ids.add(releaseTask.getTask_id());
			}
			
		}
		return task_ids;
	}

	@Override
	public Set<String> queryTaskByInterval(Map<String, String> requestParam) throws Exception {
		String startDate = requestParam.get(Dict.START_DATE);
		String endData = requestParam.get(Dict.END_DATE);
		Criteria criteria = new Criteria();
		Criteria dateCriteria = new Criteria(Dict.DATE);
		if (!CommonUtils.isNullOrEmpty(startDate)) {
		   dateCriteria.gte(startDate);
		}
		if (!CommonUtils.isNullOrEmpty(endData)) {
		   dateCriteria.lte(endData);
		}else {
			dateCriteria.lte(CommonUtils.formatDate(CommonUtils.INPUT_DATE));
		}
		if (!CommonUtils.isNullOrEmpty(endData)||!CommonUtils.isNullOrEmpty(startDate)) {
			criteria.andOperator(dateCriteria);
		}
		Query query = new Query(criteria);
		Set<String> task_ids = new HashSet<>();
		Set<ProdApplication> prodApplications = new HashSet<>();
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		Query queryApp;
		for (ProdRecord prodRecord : prodRecords) {
			queryApp = new Query(Criteria.where(Dict.PROD_ID).is(prodRecord.getProd_id()));
			List<ProdApplication> apps = mongoTemplate.find(queryApp, ProdApplication.class);
			prodApplications.addAll(apps);
		}
		for (ProdApplication prodApplication : prodApplications) {
			queryApp = new Query(Criteria.where(Dict.PROD_ID).is(prodApplication.getProd_id()));
			ProdRecord prodRecord = mongoTemplate.findOne(queryApp, ProdRecord.class);
			queryApp = new Query(Criteria.where(Dict.APPLICATION_ID)
					.is(prodApplication.getApplication_id())
					.and(Dict.RELEASE_NODE_NAME)
					.is(prodRecord.getRelease_node_name()));
			List<ReleaseTask> tasks = mongoTemplate.find(queryApp, ReleaseTask.class);
			for (ReleaseTask releaseTask : tasks) {
				task_ids.add(releaseTask.getTask_id());
			}
		}
		return task_ids;
	}

	@Override
	public void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
			Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
			Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
			mongoTemplate.updateMulti(query, update, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryTasksByExecutor(String application_id, String release_node_name) throws Exception {
		List<ReleaseTask> list = new ArrayList<>();
		if(CommonUtils.isNullOrEmpty(application_id)){
			Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
			list = this.mongoTemplate.find(query, ReleaseTask.class);
			return list;
		}
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name)
				.and(Dict.APPLICATION_ID).is(application_id));
		ReleaseTask releaseTask = this.mongoTemplate.findOne(query, ReleaseTask.class);
		if(!CommonUtils.isNullOrEmpty(releaseTask)){
			list.add(releaseTask);
		}
		return list;
	}

	@Override
	public List<ReleaseTask> queryReleaseTaskByNames(List<String> release_node_names) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).in(release_node_names)
				.and(Dict.TASK_STATUS).nin(Constants.TASK_RLS_PRODUCTION));
		List<ReleaseTask> releaseTaskList = this.mongoTemplate.find(query, ReleaseTask.class);
		return releaseTaskList;
	}

	@Override
	public void updateReleaseTaskProductions(List<String> task_ids) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).in(task_ids).and(Dict.TYPE).in("1", "2"));
		Update update = Update.update(Dict.TASK_STATUS, Constants.TASK_RLS_PRODUCTION);
		this.mongoTemplate.updateMulti(query, update, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryReleaseTaskByAppIdNoTaskId(String appId, String release_time, String task_id) throws Exception {
		Pattern pattern = Pattern.compile("^.*"+release_time+".*$",Pattern.CASE_INSENSITIVE);
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(appId).and(Dict.TASK_STATUS).ne(Constants.TASK_RLS_REJECTED)
			.and(Dict.TASK_ID).nin(task_id).and(Dict.RELEASE_NODE_NAME).regex(pattern));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryReleaseByNodeName(String date) {
		Pattern pattern = Pattern.compile("^.*" + date + ".*$", Pattern.CASE_INSENSITIVE);
		Query query = new Query(Criteria.where(Dict.TASK_STATUS)
				.in(Constants.TASK_RLS_NEED_AUDIT, Constants.TASK_RLS_CONFIRMED)
				.and(Dict.RELEASE_NODE_NAME).regex(pattern));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryCurrentTaskByNodeName(String release_node_name, List<String> status, List<String> ids) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name)
				.and(Dict.TASK_STATUS).in(status).and(Dict.TASK_ID).in(ids));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public ReleaseTask queryByTaskIdNode(String id, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.TASK_ID).is(id));
		return mongoTemplate.findOne(query, ReleaseTask.class);
	}

	@Override
	public List<String> findReleaseTaskByReleaseNodeName(String release_node_name) {
		List<String> taskList = new ArrayList<>();
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name)
				.and(Dict.TASK_STATUS).in(Constants.TASK_RLS_CONFIRMED, Constants.TASK_RLS_PRODUCTION));
		List<ReleaseTask> tasks = mongoTemplate.find(query, ReleaseTask.class);
		for (ReleaseTask releaseTask : tasks) {
			taskList.add(releaseTask.getTask_id());
		}
		return taskList;
	}

	@Override
	public List<ReleaseTask> queryByReleaseNodeName(String release_node_name) {
		Query query = new Query(Criteria.where(Dict.TASK_STATUS)
				.in(Constants.TASK_RLS_NEED_AUDIT, Constants.TASK_RLS_CONFIRMED)
				.and(Dict.RELEASE_NODE_NAME).is(release_node_name));
		return mongoTemplate.find(query, ReleaseTask.class);
	}

	@Override
	public List<String> queryCounldBindAppIds(String rqrmntNo, String release_node_name) {
		Criteria criteria = Criteria.where(Dict.RQRMNT_NO).is(rqrmntNo).and(Dict.RELEASE_NODE_NAME).is(release_node_name);
		Query query = new Query(criteria);
		List<ReleaseTask> releaseTask = mongoTemplate.find(query, ReleaseTask.class);
		List<String> appIds = new ArrayList<>();
		if(!CommonUtils.isNullOrEmpty(releaseTask)){
			for(ReleaseTask r : releaseTask){
				appIds.add(r.getApplication_id());
			}
		}
		return appIds;
	}

	@Override
	public void updateReleaseTaskRqrmntId(String task_id, String rqrmntId) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID));
		Update update = Update.update(Dict.RQRMNT_NO, rqrmntId);
		mongoTemplate.findAndModify(query, update, ReleaseTask.class);
	}

	public String queryRqrmntNoByAppIdReleaseNode(String appId, String releaseNodeName) {
		Criteria criteria = Criteria.where(Dict.APPLICATION_ID).is(appId).and(Dict.RELEASE_NODE_NAME).is(releaseNodeName);
		Query query = new Query(criteria);
		ReleaseTask releaseTask = mongoTemplate.findOne(query, ReleaseTask.class);
		if(!CommonUtils.isNullOrEmpty(releaseTask)){
			return releaseTask.getRqrmnt_no();
		}
		return "";
	}

	@Override
	public ReleaseTask findConfirmTask(String task_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.TASK_ID).
				is(task_id)
				.and(Dict.TASK_STATUS)
				.is(Constants.TASK_RLS_CONFIRMED));
		return mongoTemplate.findOne(query, ReleaseTask.class);
	}

	@Override
	public List<ReleaseTask> queryNecessaryTask(String release_node_name) {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).
				and(Dict.TASK_STATUS).ne(Constants.RELEASE_TASK_STATUS_REFUSED);
		Query query = new Query(criteria);
		List<ReleaseTask> releaseTasks = mongoTemplate.find(query, ReleaseTask.class);
		return releaseTasks;
	}

	@Override
	public List<ReleaseTask> queryRefusedTask(String release_node_name) {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).
				and(Dict.TASK_STATUS).is(Constants.RELEASE_TASK_STATUS_REFUSED);
		Query query = new Query(criteria);
		List<ReleaseTask> releaseTasks = mongoTemplate.find(query, ReleaseTask.class);
		return releaseTasks;
	}
	
	@Override
	public void updateTaskByComponen(String task_id, String type) throws Exception {
		Query query =new Query(Criteria.where(Dict.TASK_ID).is(task_id).and(Dict.TYPE).is(type));
		Update update=Update.update(Dict.TASK_STATUS, Constants.TASK_RLS_PRODUCTION);
		mongoTemplate.findAndModify(query, update, ReleaseTask.class);
	}

}

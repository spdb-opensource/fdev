package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.spdb.fdev.common.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IAppDeployReviewDao;
import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;

/**
 * @description:
 * @author: t-lik5
 * @create: 2022-03-22 00:31
 **/
@Repository
public class AppDeployReviewDaoImpl implements IAppDeployReviewDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public AppDeployReview applayDeploy(AppDeployReview applayDeploy) throws Exception {
		return mongoTemplate.save(applayDeploy);
	}

	@Override
	public Map<String, Object> queryApproved(Map<String, Object> param) throws Exception {
		String queryType = (String) param.get("queryType");
		String userId = (String) param.get("userId");
		String taskName = (String) param.get("taskName");
		String groupId = (String) param.get("groupId");
		String reviewUserId = (String) param.get("reviewUserId");
		String reviewTime = (String) param.get("reviewTime");

		Criteria criteria = Criteria.where("review_status").is(queryType);
		if (!Util.isNullOrEmpty(userId)) {
			criteria.and("applay_user_id").is(userId);
		}
		if (!Util.isNullOrEmpty(taskName)) {
			Pattern pattern = Pattern.compile("^.*" + taskName + ".*$", Pattern.CASE_INSENSITIVE);
			criteria.and(Dict.TASK_NAME).regex(pattern);
		}
		if (!Util.isNullOrEmpty(groupId)) {
			criteria.and("applay_user_owner_group_id").is(groupId);
		}
		if (!Util.isNullOrEmpty(reviewUserId)) {
			criteria.and("review_user_id").is(reviewUserId);
		}
		if (!Util.isNullOrEmpty(reviewTime)) {
			criteria.and("reviewTime").is(reviewTime);
		}

		Integer page = (Integer) param.get(Dict.PAGE);
		Integer pageNum = (Integer) param.get(Dict.PAGE_NUM);

		Query query = new Query(criteria);
		Long total = mongoTemplate.count(query, AppDeployReview.class);
		query.with(new Sort(Sort.Direction.DESC, "applay_time"));
		if(!Util.isNullOrEmpty(pageNum) && !Util.isNullOrEmpty(page)) {
			query.skip((page - 1L) * pageNum).limit(pageNum);
		}
		List<AppDeployReview> list = mongoTemplate.find(query, AppDeployReview.class);
		Map<String, Object> resultMap = new HashMap<>();

		Query queryNotApprovedTotal = new Query();
		queryNotApprovedTotal.addCriteria(Criteria.where("review_status").is("0"));
		Long notApprovTotal = mongoTemplate.count(queryNotApprovedTotal, AppDeployReview.class);
		resultMap.put("notApprovTotal", notApprovTotal);

		Query queryApprovedTotal = new Query();
		queryApprovedTotal.addCriteria(Criteria.where("review_status").is("1"));
		Long approvTotal = mongoTemplate.count(queryApprovedTotal, AppDeployReview.class);
		resultMap.put("approvTotal", approvTotal);
		
		resultMap.put(Dict.TOTAL, total);
		resultMap.put(Dict.LIST, list);

		return resultMap;
	}

	@Override
	public List<AppDeployReview> queryNotApproved() throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("review_status").is("0"));
		return mongoTemplate.find(query, AppDeployReview.class);
	}

	@Override
	public List<AppDeployReview> queryAppDeploy(List<String> ids) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(ids));
		return mongoTemplate.find(query, AppDeployReview.class);
	}

	@Override
	public AppDeployReview queryAppDeployByTaskId(String taskId) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where(Dict.TASK_ID).is(taskId));
		return mongoTemplate.findOne(query, AppDeployReview.class);
	}

	@Override
	public List<AppDeployReview> queryAppImage(String appName, String queryType) throws Exception {
		Query query = new Query();
		Criteria criteria = Criteria.where("application_name_en").is(appName);
		if (!CommonUtils.isNullOrEmpty(queryType)) {
			if("caas".equals(queryType)) {
				criteria.and("image_push_status").is("0");
			}else if("scc".equals(queryType)) {
				criteria.and("image_push_status").is("1");
			}
		}
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Order.desc("push_time")));
		return mongoTemplate.find(query, AppDeployReview.class);
	}
	
	@Override
	public List<AppDeployReview> queryDeployTask(String appName, String queryType) throws Exception {
		Query query = new Query();
		Criteria criteria = Criteria.where("application_name_en").is(appName);
		if (!CommonUtils.isNullOrEmpty(queryType)) {
			if("0".equals(queryType)) {
				criteria.and("review_status").is("0");
			}else if("1".equals(queryType)) {
				criteria.and("review_status").is("1");
			}
		}
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Order.desc("push_time")));
		return mongoTemplate.find(query, AppDeployReview.class);
	}

	@Override
	public AppDeployReview savePiplineAppInfo(AppDeployReview applayDeploy) throws Exception {
		return mongoTemplate.save(applayDeploy);
	}

	@Override
	public void update(AppDeployReview applayDeploy) throws Exception {
		mongoTemplate.save(applayDeploy);
	}

	@Override
	public void modifyDeployStatus(Map<String, Object> param) {
		String appName = (String) param.get("appName");
		String deployStatus = (String) param.get("deployStatus");
		Query query = new Query();
		Criteria criteria = Criteria.where("application_name_en").is(appName);
		criteria.and("review_status").is("1");
		query.addCriteria(criteria);
		Update update = Update.update("deploy_status", deployStatus);
		mongoTemplate.updateMulti(query, update, AppDeployReview.class);
	}

}

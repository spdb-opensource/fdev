package com.spdb.fdev.release.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IReleaseApplicationDao;
import com.spdb.fdev.release.entity.RelDevopsRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseNode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class ReleaseApplicationDaoImpl extends FreleaseDao implements IReleaseApplicationDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public void deleteApplication(String application_id, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id).and(Dict.RELEASE_NODE_NAME).is(release_node_name));
		mongoTemplate.findAndRemove(query, ReleaseApplication.class);
	}

	@Override
	public List<ReleaseApplication> queryApplications(String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		return mongoTemplate.find(query, ReleaseApplication.class);
	}

	@Override
	public List<ReleaseNode> queryReleaseNodesByAppId(String application_id, String node_status) throws Exception {
		if (CommonUtils.isNullOrEmpty(node_status)) {
			node_status = Constants.NODE_CREATED;//若未传node_stastus状态则设为默认值1
		}
		//查询为该应用id的所有投产应用
		Query query = new Query(
				Criteria.where(Dict.APPLICATION_ID)
						.is(application_id));
		List<ReleaseApplication> reApplication =
				this.mongoTemplate.find(query, ReleaseApplication.class);
		List<ReleaseNode> node_list = new ArrayList<>();
		//遍历该列表  查询所有投产窗口
		for (ReleaseApplication releaseApplication : reApplication) {
			String node_name = releaseApplication.getRelease_node_name();
			Query query1 = new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
					.is(node_name)
					.and(Dict.NODE_STATUS)
					.is(node_status));
			ReleaseNode releaseNode = this.mongoTemplate.findOne(query1, ReleaseNode.class);
			if(!CommonUtils.isNullOrEmpty(releaseNode)) {
				node_list.add(releaseNode);
			}
		}
		return node_list;
	}

	@Override
	public ReleaseApplication queryApplicationDetail(ReleaseApplication releaseApplication) {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(releaseApplication.getApplication_id())
				.and(Dict.RELEASE_NODE_NAME).is(releaseApplication.getRelease_node_name()));
		return this.mongoTemplate.findOne(query, ReleaseApplication.class);
	}

	@Override
	public ReleaseApplication queryAppByIdAndBranch(String applicationId, String release_branch) throws Exception {
		//通过应用id和release分支名查询唯一的投产应用
		Query query = new Query(
				Criteria.where(Dict.APPLICATION_ID).is(applicationId).and(Dict.RELEASE_BRANCH).is(release_branch));
		ReleaseApplication findOne = mongoTemplate.findOne(query, ReleaseApplication.class);
		if (findOne == null) {
			return null;
		}
		//返回其UAT环境名
		return findOne;
	}

	@Override
	public ReleaseApplication updateReleaseApplication(ReleaseApplication releaseApplication) throws Exception {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(releaseApplication.getApplication_id())
				.and(Dict.RELEASE_NODE_NAME).is(releaseApplication.getRelease_node_name()));
		String json = objectMapper.writeValueAsString(releaseApplication);
		JSONObject pJson = JSONObject.parseObject(json);
		Iterator<String> it = pJson.keySet().iterator();
		Update up = Update.update(Dict.APPLICATION_ID, releaseApplication.getApplication_id());
		while (it.hasNext()) {
			String key = it.next();
			Object value = pJson.get(key);
			//release_node_name objectid application_id不可被修改
			if (Dict.RELEASE_NODE_NAME.equals(key) || Dict.OBJECTID.equals(key) || Dict.APPLICATION_ID.equals(key)) {
				continue;
			}
			up.set(key, value);
		}
		mongoTemplate.findAndModify(query, up, ReleaseApplication.class);
		return mongoTemplate.findOne(query, ReleaseApplication.class);
	}

	@Override
	public ReleaseApplication findOneReleaseApplication(String appliction_id, String relase_node_name) throws Exception {
		Query query = new Query(
				Criteria.where(Dict.APPLICATION_ID).is(appliction_id).and(Dict.RELEASE_NODE_NAME).is(relase_node_name));
		return mongoTemplate.findOne(query, ReleaseApplication.class);
	}

	@Override
	public ReleaseApplication saveReleaseApplication(ReleaseApplication releaseApplication) throws Exception {
		return this.mongoTemplate.save(releaseApplication);
	}

	@Override
	public ReleaseApplication queryAppByIdAndTag(String applicationId, String product_tag) throws Exception {
		//通过应用id和tag名查询唯一的投产应用
		Query query = new Query(
				Criteria.where(Dict.APPLICATION_ID).is(applicationId).and(Dict.PRODUCT_TAG).is(product_tag));
		RelDevopsRecord findOne = mongoTemplate.findOne(query, RelDevopsRecord.class);
		if (findOne == null) {
			return null;
		}
		//查询该应用的投产窗口对象
		Query find = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(findOne.getRelease_node_name()).and(Dict.APPLICATION_ID).is(findOne.getApplication_id()));
		ReleaseApplication releaseApplication = mongoTemplate.findOne(find, ReleaseApplication.class);
		if (releaseApplication == null) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
		}
		//返回其rel环境名
		return releaseApplication;
	}

	@Override
	public void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id,
									  String release_branch) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(release_node_name)
				.and(Dict.APPLICATION_ID)
				.is(application_id));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name_new).set(Dict.RELEASE_BRANCH, release_branch);
		mongoTemplate.findAndModify(query, update, ReleaseApplication.class);
	}

	@Override
	public void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
		mongoTemplate.updateMulti(query, update, ReleaseApplication.class);
	}

	@Override
	public void updateReleaseApplicationConfig(ReleaseApplication releaseApplication) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseApplication.getRelease_node_name())
				.and(Dict.APPLICATION_ID).is(releaseApplication.getApplication_id()));
		Update update = Update.update(Dict.FDEV_CONFIG_CHANGED, releaseApplication.isFdev_config_changed())
				.set(Dict.COMPARE_URL, releaseApplication.getCompare_url())
				.set(Dict.LAST_RELEASE_TAG, releaseApplication.getLast_release_tag())
				.set(Dict.DEVOPS_TAG, releaseApplication.getDevops_tag())
				.set(Dict.FDEV_CONFIG_CONFIRM, releaseApplication.getFdev_config_confirm());
		mongoTemplate.updateMulti(query, update, ReleaseApplication.class);
	}

	@Override
	public ReleaseApplication updateConfigConfirm(ReleaseApplication releaseApplication) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseApplication.getRelease_node_name())
				.and(Dict.APPLICATION_ID).is(releaseApplication.getApplication_id()));
		Update update = Update.update(Dict.FDEV_CONFIG_CONFIRM, releaseApplication.getFdev_config_confirm());
		mongoTemplate.findAndModify(query, update, ReleaseApplication.class);
		return mongoTemplate.findOne(query, ReleaseApplication.class);
	}

	@Override
	public List<ReleaseApplication> queryOldApplication(String application_id, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.RELEASE_NODE_NAME).lt(release_node_name));
		return mongoTemplate.find(query, ReleaseApplication.class);
	}

	@Override
	public ReleaseApplication queryByIdAndNodeName(String application_id, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.RELEASE_NODE_NAME).is(release_node_name));
		return mongoTemplate.findOne(query, ReleaseApplication.class);
	}

	@Override
	public void updateApplicationPath(ReleaseApplication application) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(application.getRelease_node_name())
				.and(Dict.APPLICATION_ID).is(application.getApplication_id()));
		Update update = Update.update(Dict.PATH, application.getPath());
		mongoTemplate.findAndModify(query, update, ReleaseApplication.class);
	}

	@Override
	public void updateStaticResource(ReleaseApplication application) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(application.getRelease_node_name())
				.and(Dict.APPLICATION_ID).is(application.getApplication_id()));
		Update update = new Update();
		update.set("static_resource", application.getStatic_resource())
				.set("app_sql",application.getApp_sql());
		mongoTemplate.findAndModify(query, update, ReleaseApplication.class);
	}

}
package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IReleaseBatchDao;
import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.entity.ReleaseBindRelation;
import com.spdb.fdev.release.entity.ReleaseTask;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReleaseBatchDaoImpl extends FreleaseDao implements IReleaseBatchDao {
	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public ReleaseBatchRecord queryBatchRecordByAppId(String releaseNodeName, String appId) throws Exception {
		Criteria criteria = Criteria.where(Dict.APPLICATION_ID).is(appId).and(Dict.RELEASE_NODE_NAME).is(releaseNodeName);
		Query query = new Query(criteria).with(Sort.by(Sort.Order.desc(Dict.CREATE_TIME)));
		return this.mongoTemplate.findOne(query, ReleaseBatchRecord.class);
	}

	@Override
	public ReleaseBindRelation queryBindAppByAppId(String releaseNodeName, String appId) throws Exception {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName).and(Dict.APPLICATIONS).in(appId);
		Query query = new Query(criteria);
		return this.mongoTemplate.findOne(query, ReleaseBindRelation.class);
	}

	@Override
	public ReleaseBindRelation queryBindAppByBatchId(String releaseNodeName, String batchId) throws Exception {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName).and(Dict.BATCH_ID).is(batchId);
		Query query = new Query(criteria);
		return this.mongoTemplate.findOne(query, ReleaseBindRelation.class);
	}

	@Override
	public List<String> queryBindPeople(List<String> appIds, String releaseNodeName) throws Exception {
		List<String> bindPeople = new ArrayList<>();
		for (String appId : appIds){
			Criteria criteria = Criteria.where(Dict.APPLICATION_ID).is(appId).and(Dict.RELEASE_NODE_NAME).is(releaseNodeName);
			Query query = new Query(criteria).with(Sort.by(Sort.Order.desc(Dict.CREATE_TIME)));
			ReleaseBatchRecord releaseBatchRecord = mongoTemplate.findOne(query, ReleaseBatchRecord.class);
			bindPeople.add(releaseBatchRecord.getUser_name_cn());
		}
		return bindPeople;
	}

	@Override
	public ReleaseBatchRecord addBatchRecord(String appId, String releaseNodeName, String batchId, String modifyReason, String userNameEn, String userNameCn, String dateStr) throws Exception {
		ReleaseBatchRecord releaseBatchRecord = new ReleaseBatchRecord();
		releaseBatchRecord.setApplication_id(appId);
		releaseBatchRecord.setBatch_id(batchId);
		releaseBatchRecord.setCreate_time(dateStr);
		releaseBatchRecord.setModify_reason(modifyReason);
		releaseBatchRecord.setRelease_node_name(releaseNodeName);
		releaseBatchRecord.setUser_name_cn(userNameCn);
		releaseBatchRecord.setUser_name_en(userNameEn);
		return mongoTemplate.save(releaseBatchRecord);

	}

	@Override
	public void updateBindRelation(String releaseNodeName, String batchId, List<String> bindAppIds) throws  Exception {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName).and(Dict.BATCH_ID).is(batchId);
		Query query = new Query(criteria);
		Update update =Update.update(Dict.APPLICATIONS, bindAppIds);
		mongoTemplate.updateMulti(query, update, ReleaseBindRelation.class);
	}

	@Override
	public ReleaseBindRelation addBindRelation(ReleaseBindRelation releaseBindRelation) throws Exception {
		return mongoTemplate.save(releaseBindRelation);
	}

	@Override
    public List<ReleaseTask> batchReleaseTask(String date) throws Exception {
	    Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).gte(date);
	    Query query = new Query(criteria);
	    return mongoTemplate.find(query, ReleaseTask.class);
    }

    @Override
	public void updateReleaseTask(ReleaseTask releaseTask) throws Exception {
		Criteria criteria = Criteria.where(Dict.TASK_ID).is(releaseTask.getTask_id());
		Query query = new Query(criteria);
		Update update = Update.update(Dict.RQRMNT_NO, releaseTask.getRqrmnt_no());
		mongoTemplate.updateMulti(query, update, ReleaseTask.class);
	}
}

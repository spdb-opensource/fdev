package com.spdb.fdev.release.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IBatchTaskDao;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.NoteSql;

@Repository
public class BatchTaskDaoImpl implements IBatchTaskDao {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public BatchTaskInfo create(BatchTaskInfo batchTaskInfos) throws Exception {
		try {
			return mongoTemplate.save(batchTaskInfos);
		}catch(DuplicateKeyException e)
		{
			throw  new FdevException(ErrorConstants.REPET_INSERT_REEOR);
		}
	}

	@Override
	public void updateBatchTaskInfo(String id, String type, String executorId, String transName, String jobGroup,
			String description, String cronExpression, String misfireInstr, String fireTime, String batchInfo)
					throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		Update update=Update.update(Dict.TYPE, type).set("executorId", executorId).set("transName", transName)
				.set("jobGroup", jobGroup).set("description", description).set("cronExpression", cronExpression)
				.set("misfireInstr", misfireInstr).set("fireTime", fireTime).set("batchInfo", batchInfo);
		this.mongoTemplate.findAndModify(query, update, BatchTaskInfo.class);
	}

	@Override
	public void deleteBatchTaskInfo(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		mongoTemplate.findAndRemove(query, BatchTaskInfo.class);
		
	}

	@Override
	public List<BatchTaskInfo> queryBatchTypeList(Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		Query query = new Query (Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID).is(application_id));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public void updateBatchTaskProdId(String id, String prod_id,String batchNo) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		Update update=Update.update(Dict.PROD_ID, prod_id).set("batch_no", batchNo);
		this.mongoTemplate.findAndModify(query, update, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoList(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		Query query = new Query();
		if(!CommonUtils.isNullOrEmpty(note_id)) {
			query.addCriteria(Criteria.where(Dict.NOTE_ID).is(note_id));
		} else if (!CommonUtils.isNullOrEmpty(prod_id)) {
			query.addCriteria(Criteria.where(Dict.PROD_ID).is(prod_id));
		}
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoByProdId(String prod_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.PROD_ID).is(prod_id));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public BatchTaskInfo queryBatchTaskInfoById(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		return  mongoTemplate.findOne(query, BatchTaskInfo.class);
	}
	
	@Override
	public BatchTaskInfo updateNoteBatchNo(String id, String batch_no) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		Update update = Update.update("batch_no", batch_no);
		mongoTemplate.findAndModify(query, update, BatchTaskInfo.class);
		return mongoTemplate.findOne(query, BatchTaskInfo.class);
	}

	@Override
	public BatchTaskInfo queryBatchTaskInfoByAppId(String id, String application_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id).and(Dict.APPLICATION_ID).is(application_id));
		return  mongoTemplate.findOne(query, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoListByAppId(Map<String, Object> requestParam)throws Exception {
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		Query query = new Query();
		if(!CommonUtils.isNullOrEmpty(note_id)) {
			query.addCriteria(Criteria.where(Dict.NOTE_ID).is(note_id));
		} else if (!CommonUtils.isNullOrEmpty(prod_id)) {
			query.addCriteria(Criteria.where(Dict.PROD_ID).is(prod_id));
		}
		query.addCriteria(Criteria.where(Dict.APPLICATION_ID).is(application_id));
		query.with(new Sort(Sort.Direction.ASC, "batch_no"));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoByReleaseNodeName(String release_node_name) throws Exception {
		Query query = new Query (Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

	@Override
	public List<BatchTaskInfo> queryBatchTaskInfoByProdIdAndAppId(String prod_id,String application_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		return  mongoTemplate.find(query, BatchTaskInfo.class);
	}

}

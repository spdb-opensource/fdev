package com.spdb.fdev.fuser.spdb.dao.Impl;

import java.util.List;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.DuplicateKeyException;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.CommissionEventDao;
import com.spdb.fdev.fuser.spdb.entity.user.UserCommissionEvent;

@Repository
public class CommissionEventDaoImpl implements CommissionEventDao {
	
	private static final Logger logger = LoggerFactory.getLogger(CommissionEventDaoImpl.class);// 控制台日志打印

	@Resource
	private MongoTemplate mongoTemplate;
	
	@Override
	public UserCommissionEvent addCommissionEvent(UserCommissionEvent userCommissionEvent) throws Exception {
		ObjectId objectId = new ObjectId();
		userCommissionEvent.set_id(objectId);
		userCommissionEvent.setId(objectId.toString());
		userCommissionEvent.setCreateTime(CommonUtils.formatDate(CommonUtils.DATE_PARSE));
		userCommissionEvent.setStatus("0");
		userCommissionEvent.setLabel("todo");
		try {
			return mongoTemplate.save(userCommissionEvent);
		}catch (DuplicateKeyException e) {
			logger.error("待办事项添加失败: ", e);
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "待办已存在,请勿重复添加" });
		}
	}

	@Override
	public UserCommissionEvent updateByTargetIdAndType(UserCommissionEvent userCommissionEvent) throws Exception {
		Query query =Query.query(Criteria.where(Dict.ID).is(userCommissionEvent.getId()));
		Update update = Update.update(Dict.STATUS, "1")
				.set("executor_id", userCommissionEvent.getExecutor_id())
				.set("executor_name_cn", userCommissionEvent.getExecutor_name_cn())
				.set("executor_name_en", userCommissionEvent.getExecutor_name_en())
				.set("executeTime", userCommissionEvent.getExecuteTime())
				.set("label", userCommissionEvent.getLabel());
		mongoTemplate.findAndModify(query, update, UserCommissionEvent.class);
		return mongoTemplate.findOne(query, UserCommissionEvent.class);
	}

	@Override
	public UserCommissionEvent queryDetailBytargetIdAndType(String targetId,String type,String module) throws Exception {
		Query query = new Query(Criteria.where("target_id").is(targetId)
				.and("type").is(type)
				.and("module").is(module));
        return mongoTemplate.findOne(query, UserCommissionEvent.class);
	}

	@Override
	public List<UserCommissionEvent> queryCommissionEvent(UserCommissionEvent userCommissionEvent) throws Exception {
		Query query = new Query(Criteria.where("user_ids").in(userCommissionEvent.getUser_ids())
				.and("label").is(userCommissionEvent.getLabel()));
		query.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        return mongoTemplate.find(query, UserCommissionEvent.class);
	}
	
	@Override
	public List<UserCommissionEvent> queryCommissionEventByStatus(UserCommissionEvent userCommissionEvent) throws Exception {
		Query query = new Query(Criteria.where("user_ids").in(userCommissionEvent.getUser_ids())
				.and(Dict.STATUS).is(userCommissionEvent.getStatus())
				.and("label").is(userCommissionEvent.getLabel()));
		query.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        return mongoTemplate.find(query, UserCommissionEvent.class);
	}

	@Override
	public UserCommissionEvent queryEventById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, UserCommissionEvent.class);
	}

	@Override
	public UserCommissionEvent updateLabelById(UserCommissionEvent newEvent) {
		Query query =Query.query(Criteria.where(Dict.ID).is(newEvent.getId()));
		Update update = Update.update("label", newEvent.getLabel());
		mongoTemplate.findAndModify(query, update, UserCommissionEvent.class);
		return mongoTemplate.findOne(query, UserCommissionEvent.class);
	}

	@Override
	public Long countLabelNum(List<String> userIds, String label) {
		Query query = new Query(Criteria.where("user_ids").in(userIds)
				.and("label").is(label));
        return mongoTemplate.count(query, UserCommissionEvent.class);
	}

	@Override
	public Long totalLabelNum(List<String> userIds) {
		Query query = new Query(Criteria.where("user_ids").in(userIds)
				.and("label").exists(true));
        return mongoTemplate.count(query, UserCommissionEvent.class);
	}

	@Override
	public UserCommissionEvent upsertCommissionEvent(UserCommissionEvent commissionEvent,
			UserCommissionEvent userCommissionEvent) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(commissionEvent.getId())
				.and("target_id").is(commissionEvent.getTarget_id())
				.and("type").is(commissionEvent.getType())
				.and("module").is(commissionEvent.getModule()));
		Update update = Update.update("link",userCommissionEvent.getLink())
				.set("createTime", CommonUtils.formatDate(CommonUtils.DATE_PARSE))
				.set("user_ids", userCommissionEvent.getUser_ids())
				.set("description", userCommissionEvent.getDescription())
				.set("status", "0")
				.set("label", "todo");
		//数据存在就修改,没有就新增
		mongoTemplate.upsert(query, update, UserCommissionEvent.class);
		return mongoTemplate.findOne(query, UserCommissionEvent.class);
	}

	@Override
	public Long deleteCommissionEventById(String id) {
		Query query = new Query(Criteria.where(Dict.ID).in(id));
        return mongoTemplate.remove(query, UserCommissionEvent.class).getDeletedCount();
	}

	@Override
	public List<UserCommissionEvent> queryListByStatusOrLabel(UserCommissionEvent userCommissionEvent)
			throws Exception {
		Query query = new Query(new Criteria().orOperator(Criteria.where("status").is(userCommissionEvent.getStatus()),
				Criteria.where("label").is(userCommissionEvent.getLabel())));
        return mongoTemplate.find(query, UserCommissionEvent.class);
	}
	
	
}

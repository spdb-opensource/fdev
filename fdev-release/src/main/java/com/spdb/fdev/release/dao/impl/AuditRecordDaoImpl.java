package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IAuditRecordDao;
import com.spdb.fdev.release.entity.AuditRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
@Repository
public class AuditRecordDaoImpl extends FreleaseDao implements IAuditRecordDao{
	
	@Resource
	private MongoTemplate mongoTemplate;

	public void auditRecord(AuditRecord auditRecord) throws Exception {
		String formatDate = CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN);
		String requestURI = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
		.getRequest().getRequestURI();
		auditRecord.setOperation(requestURI);
		auditRecord.setOperator_name_en(CommonUtils.getSessionUser().getUser_name_en());//保存操作者英文名
		auditRecord.setOperation_time(formatDate);//保存当前操作时间
		mongoTemplate.save(auditRecord);
	}

	@Override
	public String queryRecordOperation(AuditRecord auditRecord) throws Exception {
		Query query=new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(auditRecord.getRelease_node_name()
				).and(Dict.TASK_ID).is(auditRecord.getTask_id())
				.and(Dict.OPERATION_TYPE).is(auditRecord.getOperation_type()));
		auditRecord = mongoTemplate.findOne(query, AuditRecord.class);	
		return auditRecord.getOperator_name_en();
	}

	@Override
	public void updateNode(String old_release_node_name, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
		Update update = Update.update(Dict.RELEASE_NODE_NAME,release_node_name);
		mongoTemplate.updateMulti(query, update, AuditRecord.class);
	}

	@Override
	public void save(AuditRecord auditRecord) {
		mongoTemplate.save(auditRecord);
	}

}

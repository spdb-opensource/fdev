package com.spdb.fdev.fdevapp.spdb.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.fdevapp.spdb.dao.ICommonDao;
@Repository
public class CommonDaoImpl implements ICommonDao {
	@Resource
	private MongoTemplate mongoTemplate;
	@Override
	public <T>List<T> commonQuery(Map<Object,Object> parmMap,String operator,Class<T> clazz) throws Exception {
		Query query = new Query();
		Criteria [] criterias = new Criteria[parmMap.size()];
		int index = 0;
		for(Map.Entry<Object,Object> entry:parmMap.entrySet()) {
			criterias[index]=Criteria.where(entry.getKey().toString()).is(entry.getValue());
			index++;
		}
		if("and".equals(operator)) {
			query.addCriteria(new Criteria().andOperator(criterias));
		}else if("or".equals(operator)) {
			query.addCriteria(new Criteria().orOperator(criterias));
		}
		return mongoTemplate.find(query, clazz);
	}

}

package com.spdb.fdev.release.dao.impl;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.INoteSqlDao;
import com.spdb.fdev.release.entity.NoteSql;


@Repository
public class NoteSqlDaoImpl implements INoteSqlDao {

    @Resource
    private MongoTemplate mongoTemplate;
    

	@Override
	public NoteSql save(NoteSql noteSql) {
		try {
            return this.mongoTemplate.save(noteSql);
        } catch (DuplicateKeyException e) {
            throw new FdevException("发布说明添加变更应用失败");
        }
	}

	@Override
	public List<NoteSql> queryNoteSql(String noteId) {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(noteId));
		return mongoTemplate.find(query, NoteSql.class);
	}

	
   

}

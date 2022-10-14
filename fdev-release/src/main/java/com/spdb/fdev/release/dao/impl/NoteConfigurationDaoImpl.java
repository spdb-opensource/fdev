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
import com.spdb.fdev.release.dao.INoteConfigurationDao;
import com.spdb.fdev.release.dao.INoteServiceDao;
import com.spdb.fdev.release.entity.NoteConfiguration;
import com.spdb.fdev.release.entity.NoteService;


@Repository
public class NoteConfigurationDaoImpl implements INoteConfigurationDao {

    @Resource
    private MongoTemplate mongoTemplate;
    
    @Override
	public NoteConfiguration save(NoteConfiguration noteConfig) {
    	try {
            return this.mongoTemplate.save(noteConfig);
        } catch (DuplicateKeyException e) {
            throw new FdevException("发布说明添加配置文件失败");
        }
	}

	@Override
	public List<NoteConfiguration> queryNoteConfiguration(String noteId) {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(noteId));
		return mongoTemplate.find(query, NoteConfiguration.class);
	}

	@Override
	public List<NoteConfiguration> queryByModule(String noteId, String module) {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(noteId).and("module_name").is(module));
		return mongoTemplate.find(query, NoteConfiguration.class);
	}
   

}

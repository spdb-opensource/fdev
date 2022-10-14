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
import com.spdb.fdev.release.dao.INoteServiceDao;
import com.spdb.fdev.release.entity.NoteService;


@Repository
public class NoteServiceDaoImpl implements INoteServiceDao {

    @Resource
    private MongoTemplate mongoTemplate;
    
    @Override
	public NoteService save(NoteService noteService) {
    	try {
            return this.mongoTemplate.save(noteService);
        } catch (DuplicateKeyException e) {
            throw new FdevException("发布说明添加变更应用失败");
        }
	}

	@Override
	public List<NoteService> queryNoteService(String noteId) {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(noteId));
		return mongoTemplate.find(query, NoteService.class);
	}

	

   

}

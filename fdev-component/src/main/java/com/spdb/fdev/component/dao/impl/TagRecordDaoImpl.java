package com.spdb.fdev.component.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.TagRecord;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TagRecordDaoImpl implements ITagRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public TagRecord save(TagRecord tagRecord) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        tagRecord.set_id(objectId);
        tagRecord.setId(id);
        return mongoTemplate.save(tagRecord);
    }

    @Override
    public TagRecord findByMidAndGid(String application_id, String merge_request_id) throws Exception {
        Query query =new Query(Criteria.where(Dict.GITLAB_ID)
                .is(application_id)
                .and(Dict.MERGE_REQUEST_ID)
                .is(merge_request_id));
        return mongoTemplate.findOne(query, TagRecord.class);
    }

}

package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.spdb.dao.IFileDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeFile;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/11/12
 */
@Component
public class FileDaoImpl implements IFileDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<CodeFile> query(CodeFile codeFile){
        Query query = new Query();
        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(codeFile.getId())){
            criteria.and(Dict.ID).is(codeFile.getId());
        }
        if(!CommonUtils.isNullOrEmpty(codeFile.getOrderId())){
            criteria.and(Dict.ORDER_ID).is(codeFile.getOrderId());
        }
        if(!CommonUtils.isNullOrEmpty(codeFile.getFileName())){
            criteria.and(Dict.FILE_NAME).is(codeFile.getFileName());
        }
        if(!CommonUtils.isNullOrEmpty(codeFile.getFileType())){
            criteria.and(Dict.FILE_TYPE).is(codeFile.getFileType());
        }
        if(!CommonUtils.isNullOrEmpty(codeFile.getFilePath())){
            criteria.and(Dict.FILE_PATH).is(codeFile.getFilePath());
        }
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.find(query, CodeFile.class);
    }

    @Override
    public void save(CodeFile codeFile) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        codeFile.setId(id);
        mongoTemplate.save(codeFile);
    }

    @Override
    public void updateById(CodeFile codeFile) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(codeFile.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(codeFile);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, codeFile.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECT_ID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, CodeFile.class);
    }

    @Override
    public void delete(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECT_ID);
        mongoTemplate.findAndRemove(query, CodeFile.class);
    }

    @Override
    public List<CodeFile> queryFilesByOrderId(String orderId) {
        Query query = Query.query(Criteria.where(Dict.ORDER_ID).is(orderId));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.find(query, CodeFile.class);
    }
}

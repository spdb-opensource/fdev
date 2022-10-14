package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.JsonSchemaDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.JsonSchema;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class JsonSchemaDaoImpl implements JsonSchemaDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public String saveJsonSchema(JsonSchema jsonSchema) {
        ObjectId objectId = new ObjectId();
        jsonSchema.set_id(objectId);
        jsonSchema.setId(objectId.toString());
        jsonSchema.setStatus(1);
        jsonSchema.setCreateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        jsonSchema.setUpdateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.save(jsonSchema);
        return objectId.toString();
    }

    @Override
    public List<JsonSchema> getJsonSchema(Map<String, Object> requestMap) {
        String dataType = (String) requestMap.get(Dict.DATA_TYPE);
        String title = (String) requestMap.get(Dict.TITLE);
        Integer template = (Integer) requestMap.get(Dict.IS_TEMPLATE);
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(1);
        if (StringUtils.isNotEmpty(title)) {
            criteria.and(Dict.TITLE).is(title);
        }
        if (StringUtils.isNotEmpty(dataType)) {
            criteria.and(Dict.DATA_TYPE).is(dataType);
        }
        if (template != null) {
            criteria.and(Dict.IS_TEMPLATE).is(template);
        }
        return mongoTemplate.find(new Query(criteria), JsonSchema.class);
    }

    @Override
    public JsonSchema getJsonSchema(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), JsonSchema.class);
    }

    @Override
    public void updateJsonSchema(Map<String, Object> requestMap, String opno) {
        String id = (String) requestMap.get(Dict.JSON_SCHEMA_ID);
        String description = (String) requestMap.get(Dict.NAME_CN);
        String jsonSchemaString = (String) requestMap.get(Dict.JSON_SCHEMA);
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.DESCRIPTION, description)
                .set(Dict.JSON_SCHEMA, jsonSchemaString)
                .set(Constants.OPNO, opno)
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, JsonSchema.class);
    }
}

package com.spdb.fdev.pipeline.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IYamlConfigDao;
import com.spdb.fdev.pipeline.entity.YamlConfig;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Service
public class YamlConfigDaoImpl implements IYamlConfigDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据id查询
     *
     * @param configId
     * @return
     */
    @Override
    public YamlConfig queryById(String configId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.CONFIGID).is(configId);
        Query query = new Query(criteria);
        List<YamlConfig> yamlConfig = this.mongoTemplate.find(query, YamlConfig.class, Dict.YAMLCONFIG);
        if (yamlConfig.size() == 1)
            return yamlConfig.get(0);
        else
            return null;
    }

    /**
     * 新增
     *
     * @param yamlEntity
     * @return
     * @throws Exception
     */
    @Override
    public String add(YamlConfig yamlEntity) {
        //如果模板ID为空，且又是私有的配置，报错
        if(CommonUtils.isNullOrEmpty(yamlEntity.getConfigTemplateId()) && Dict.PRIVATE.equals(yamlEntity.getType())){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.CONFIGTEMPLATEID});
        }

        ObjectId objectId = new ObjectId();
        String configId = objectId.toString();
        yamlEntity.setConfigId(configId);
        yamlEntity.setStatus(Constants.ONE);
        this.mongoTemplate.insert(yamlEntity, Dict.YAMLCONFIG);
        return configId;
    }

    /**
     *
     * @param configId
     * @return
     * @throws Exception
     */
    @Override
    public String updateStatusClose(String configId) {
        if (CommonUtils.isNullOrEmpty(configId)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.CONFIGID});
        }

        Query queryUpdate = new Query(new Criteria().and(Dict.CONFIGID).is(configId));
        Update update = new Update();
        update.set(Dict.STATUS, Constants.ZERO);
        this.mongoTemplate.findAndModify(queryUpdate, update, YamlConfig.class);
        return configId;

       /* String configId = null;
        if (CommonUtils.isNullOrEmpty(yamlEntity.getConfigId())) {
            ObjectId objectId = new ObjectId();
            configId = objectId.toString();
            yamlEntity.setConfigId(configId);
            this.mongoTemplate.insert(yamlEntity, Dict.YAMLCONFIG);
            return configId;
        }else {
            configId = yamlEntity.getConfigId();
        }
        Query query = new Query(new Criteria().and(Dict.CONFIGID).is(configId));
        *//************************************//*
        //该方式只有java 实体和数据库的key一致才可以使用
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(yamlEntity);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update up = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.CONFIGID.equals(key) || Dict._ID.equals(key)) {
                continue;
            }
            up.set(key, value);
        }
        *//****************************************//*
        this.mongoTemplate.upsert(query, up, YamlConfig.class);
        return configId;*/
    }

}

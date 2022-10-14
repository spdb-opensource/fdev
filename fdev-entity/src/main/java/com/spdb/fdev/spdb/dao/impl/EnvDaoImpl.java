package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.dao.IEnvDao;
import com.spdb.fdev.spdb.entity.Env;
import com.spdb.fdev.spdb.entity.EnvType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EnvDaoImpl
 * @DESCRIPTION 环境
 * @Author xxx
 * @Date 2021/5/8 14:30
 * @Version 1.0
 */
@Repository
@RefreshScope
public class EnvDaoImpl implements IEnvDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void save(List<Env> envs) throws Exception {
        mongoTemplate.insert(envs, Env.class);
    }

    @Override
    public List<Env> queryList(Map<String, Object> req) throws Exception {
        String likeKey = (String) req.get(Dict.LIKEKEY);
        Query query = new Query();
        if (!CommonUtil.isNullOrEmpty(likeKey)) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(Dict.NAMEEN).regex(likeKey, "i"),
                    Criteria.where(Dict.TYPE).regex(likeKey, "i")
            ));
        }
        List<String> sort = new ArrayList<String>();
        sort.add(Dict.NAMEEN);
        query.with(new Sort(Sort.Direction.ASC, sort));//排序
        return mongoTemplate.find(query, Env.class);
    }

    @Override
    public Env queryEnv(Map<String, Object> req) throws Exception {
        String nameEn = (String) req.get(Dict.NAMEEN);
        Query query = new Query();
        if (!CommonUtil.isNullOrEmpty(nameEn)) {
            query.addCriteria(new Criteria().and(Dict.NAMEEN).is(nameEn));
        }
        return mongoTemplate.findOne(query, Env.class);
    }

    @Override
    public void saveEnvType(List<EnvType> envTypes) throws Exception {
        mongoTemplate.insert(envTypes, EnvType.class);
    }

    @Override
    public List<EnvType> queryEnvTypeList(Map<String, Object> req) throws Exception {
        return mongoTemplate.find(new Query(), EnvType.class);
    }

}

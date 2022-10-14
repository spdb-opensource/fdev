package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.TotalJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalDat;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xxx
 * @date 2020/7/30 16:54
 */
@Repository
public class TotalJsonDaoImpl implements TotalJsonDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(TotalJson totalJson) {
        mongoTemplate.save(totalJson);
    }

    @Override
    public void updateIsNew(String env) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ENV).is(env).and(Dict.IS_NEW).is(1);
        mongoTemplate.findAndModify(new Query(criteria), Update.update(Dict.IS_NEW, 0), TotalJson.class);
    }

    @Override
    public List<TotalJson> getTotalJsonList(String projectName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.IS_NEW).is(1);
        return mongoTemplate.find(new Query(criteria), TotalJson.class);
    }

    @Override
    public List<TotalJson> queryTotalJsonHistory(String env) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env).and(Dict.IS_NEW).is(0));
        query.with(new Sort(Sort.Direction.DESC, Dict.DAT_TIME));
        return mongoTemplate.find(query, TotalJson.class);
    }

    @Override
    public TotalDat getNewTotalJson(String env) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env).and(Dict.IS_NEW).is(1));
        return mongoTemplate.findOne(query, TotalDat.class);
    }

    @Override
    public List<TotalJson> getNewTwentyTotalDat(String env) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env));
        query.with(new Sort(Sort.Direction.DESC, Dict.DAT_TIME));
        query.skip(0).limit(20);
        return mongoTemplate.find(query, TotalJson.class);
    }

    @Override
    public List<TotalJson> deleteTotalTar(String env, String datTime) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env).and(Dict.DAT_TIME).lt(datTime));
        List<TotalJson> totaljsonList = mongoTemplate.find(query, TotalJson.class);
        mongoTemplate.remove(query, TotalJson.class);
        return totaljsonList;
    }

    @Override
    public TotalJson getTotalDatByProdId(String env, String prodId) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env).and(Dict.PROD_ID).is(prodId));
        return mongoTemplate.findOne(query, TotalJson.class);
    }

    @Override
    public void updateByProdId(TotalJson totalJson) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ENV).is(totalJson.getEnv()).and(Dict.PROD_ID).is(totalJson.getProdId());
        Update update = new Update();
        update.set(Dict.DAT_TIME, totalJson.getDatTime())
                .set(Dict.APP_NUM, totalJson.getAppNum())
                .set(Dict.CENTRAL_JSON, totalJson.getCentralJson())
                .set(Dict.TOTAL_TAR_NAME, totalJson.getTotalTarName())
                .set(Dict.TOTAL_TAR_URL, totalJson.getTotalTarUrl())
                .set(Dict.IS_NEW, totalJson.getIsNew())
                .set(Dict.APP_JSON_URL_MAP, totalJson.getAppJsonUrlMap());
        mongoTemplate.findAndModify(new Query(criteria), update, TotalJson.class);
    }

    @Override
    public List<TotalJson> getTotalJsonListByEnv(String env) {
        Query query = new Query(Criteria.where(Dict.ENV).is(env));
        query.with(new Sort(Sort.Direction.DESC, Dict.DAT_TIME));
        return mongoTemplate.find(query, TotalJson.class);
    }

}

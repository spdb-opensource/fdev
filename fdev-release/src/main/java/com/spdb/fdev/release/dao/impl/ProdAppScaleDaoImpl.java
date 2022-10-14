package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdAppScaleDao;
import com.spdb.fdev.release.entity.ProdAppScale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProdAppScaleDaoImpl implements IProdAppScaleDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ProdAppScale addAppScale(ProdAppScale prodAppScale) {
        try {
            return this.mongoTemplate.save(prodAppScale);
        }catch (DuplicateKeyException e){
          throw new FdevException(ErrorConstants.REPEAT_PROD_APP_SCALE);
        }
    }

    @Override
    public void updateAppScale(String prod_id,String application_id,List<String> deploy_type,List<Map<String,Object>> new_env_scales) {
        Query query = Query.query(Criteria.where(Dict.PROD_ID).is(prod_id)
                .and(Dict.APPLICATION_ID).is(application_id));
        Update update = Update.update(Dict.ENV_SCALES, new_env_scales)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"))
                .set("deploy_type",deploy_type);
        this.mongoTemplate.updateMulti(query, update, ProdAppScale.class);
    }

    @Override
    public void deleteAppScale(Map<String, Object> map) {
        Query query = new Query(
                Criteria.where(Dict.PROD_ID).is(map.get(Dict.PROD_ID))
                        .and(Dict.APPLICATION_ID).is(map.get(Dict.APPLICATION_ID)));
        this.mongoTemplate.remove(query, ProdAppScale.class);
    }

    @Override
    public List<ProdAppScale> queryAppScale(Map<String, Object> map) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(map.get(Dict.PROD_ID)));
        return this.mongoTemplate.find(query, ProdAppScale.class);
    }

    @Override
    public ProdAppScale queryAppScaleById(String prod_id, String application_id) {
        Query query = new Query(
                Criteria.where(Dict.PROD_ID).is(prod_id)
                        .and(Dict.APPLICATION_ID).is(application_id));
        return mongoTemplate.findOne(query, ProdAppScale.class);
    }

}

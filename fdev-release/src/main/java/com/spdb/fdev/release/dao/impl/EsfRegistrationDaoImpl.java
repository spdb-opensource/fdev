package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.IEsfRegistrationDao;
import com.spdb.fdev.release.entity.EsfConfiguration;
import com.spdb.fdev.release.entity.EsfRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RefreshScope
public class EsfRegistrationDaoImpl implements IEsfRegistrationDao {

    private static final Logger logger = LoggerFactory.getLogger(EsfRegistrationDaoImpl.class);
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void batchAdd(List<EsfConfiguration> esfConfigurationList) throws Exception {
        mongoTemplate.insert(esfConfigurationList, EsfConfiguration.class);
    }

    @Override
    public List<EsfConfiguration> queryEsfConfig(String env_name, String[] network) throws Exception {
        Query query;
        // DEV,TEST环境公用4个,故设置env_name为空
        if("DEV".equals(env_name) || "TEST".equals(env_name)){
            query = new Query(Criteria.where(Dict.ENV_NAME).is(""));
        }else{
            query = new Query(Criteria.where(Dict.ENV_NAME).is(env_name).and(Dict.NETWORK).in(network));
        }

        return mongoTemplate.find(query, EsfConfiguration.class);
    }


    @Override
    public EsfRegistration queryEsfRegistById(String id) throws Exception {
        Query query = new Query(Criteria.where("esf_id").is(id));
        return mongoTemplate.findOne(query,EsfRegistration.class);
    }

    @Override
    public EsfRegistration queryEsfRegistByPlatform(String prod_id, String application_id, List<String> platform) throws Exception {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id).and("platform").in(platform).and(Dict.PROD_ID).is(prod_id));
        return mongoTemplate.findOne(query,EsfRegistration.class);
    }

    @Override
    public List<EsfRegistration> queryEsfRegists(String prod_id) throws Exception {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
        return mongoTemplate.find(query,EsfRegistration.class);
    }

    @Override
    public List<EsfRegistration> queryEsfRegistsById(String prod_id,String application_id) throws Exception {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
        return mongoTemplate.find(query,EsfRegistration.class);
    }


    @Override
    public void addEsfRegistration(EsfRegistration esfRegistration) throws Exception {
        mongoTemplate.save(esfRegistration);
    }

    @Override
    public void updateEsfRegistration(String id, String prod_id, String application_id, String caas_network_area, String scc_network_area, String sid, List<String> platform, Map<String, Object> esfInfo) throws Exception {
        Query query = new Query(Criteria.where("esf_id").is(id));
        Update update = Update.update(Dict.PROD_ID, prod_id)
                .set("caas_network_area", caas_network_area)
                .set("scc_network_area", scc_network_area)
                .set("sid", sid)
                .set("platform", platform)
                .set("esf_info", esfInfo);
        EsfRegistration esfRegistration = mongoTemplate.findOne(query,EsfRegistration.class);
        if(!CommonUtils.isNullOrEmpty(esfRegistration)){
            mongoTemplate.findAndModify(query, update, EsfRegistration.class);
        }

    }

    @Override
    public void delEsf(String id) throws Exception {
        Query query = new Query(Criteria.where("esf_id").is(id));
        mongoTemplate.findAndRemove(query, EsfRegistration.class);
    }
}

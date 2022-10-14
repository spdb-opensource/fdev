package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppDeployMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Repository
public class AppDeployMappingDaoImpl implements IAppDeployMappingDao {


    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public AppDeployMapping queryById(Integer gitlabId) {
        Query query = Query.query(Criteria.where(Constants.GITLAB_ID).is(gitlabId));
        return this.mongoTemplate.findOne(query, AppDeployMapping.class);
    }

    @Override
    public AppDeployMapping add(AppDeployMapping deployMapping) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        deployMapping.set_id(objectId);
        deployMapping.setId(id);
        deployMapping.setCtime(String.valueOf(System.currentTimeMillis()));
        return this.mongoTemplate.save(deployMapping, "app-deploy-map");
    }

    @Override
    public void update(AppDeployMapping deployMapping) {
        Query query = new Query(Criteria.where(Dict.GITLABID).is(deployMapping.getGitlabId()));
        Update update = Update.update(Dict.GITLABID, deployMapping.getGitlabId())
                .set(Dict.VARIABLES, deployMapping.getVariables())
                .set(Dict.SCCVARIABLES, deployMapping.getScc_variables());

        if (deployMapping.getConfigGitlabId() != null) {
            update.set(Dict.CONFIG_GITLAB_ID, deployMapping.getConfigGitlabId());
        }
        if (deployMapping.getModelSet() != null) {
            update.set(Dict.MODELSET, deployMapping.getModelSet());
        }
        if (deployMapping.getScc_modeSet() != null) {
            update.set(Dict.SCCMODELSET, deployMapping.getScc_modeSet());
        }

        update.set("utime", String.valueOf(System.currentTimeMillis()));
        this.mongoTemplate.upsert(query, update, AppDeployMapping.class);
    }

    @Override
    public List<AppDeployMapping> getAppDeployMapping(String modelNameEn, String fieldNameEn) {
        Criteria criteria = new Criteria();
        if (StringUtils.isEmpty(fieldNameEn)) {
            // 匹配"modelNameEn."开头的数据
            Pattern pattern = Pattern.compile("^" + modelNameEn + ".", Pattern.CASE_INSENSITIVE);
            criteria.andOperator(Criteria.where(Dict.MODEL_KEY).regex(pattern));
        } else {
            criteria.andOperator(Criteria.where(Dict.MODEL_KEY).is(modelNameEn + "." + fieldNameEn));
        }
        Query query = new Query(Criteria.where(Dict.VARIABLES).elemMatch(criteria));
        query.fields().include(Dict.GITLABID);
        return mongoTemplate.find(query, AppDeployMapping.class);
    }

    @Override
    public Map<String, Object> queryByPage(int page, int per_page) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Long total = mongoTemplate.count(query, AppDeployMapping.class);
        query.skip((page - 1L) * per_page).limit(per_page);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, AppDeployMapping.class));
        return responseMap;
    }

    @Override
    public List<AppDeployMapping> queryByGitlabIds(Set<Integer> pageGitLabIdList) {
        return mongoTemplate.find(new Query(Criteria.where(Dict.GITLABID).in(pageGitLabIdList)), AppDeployMapping.class);
    }

    @Override
    public Map<String, Object> queryByAppIdPage(Integer gitLabId, List<Integer> gitLabIdList, int page, int perPage) {
        Criteria criteria = new Criteria();
        if (gitLabId == null) {
            criteria.and(Dict.GITLABID).in(gitLabIdList);
        } else {
            criteria.and(Dict.GITLABID).is(gitLabId);
        }
        criteria.and(Dict.MODELSET).ne(null);
        Query query = new Query(criteria);
        query.fields().include(Dict.GITLABID).include(Dict.MODELSET);
        Long total = mongoTemplate.count(query, AppDeployMapping.class);
        query.skip(((page - 1L) * perPage)).limit(perPage);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, AppDeployMapping.class));
        return responseMap;
    }

}

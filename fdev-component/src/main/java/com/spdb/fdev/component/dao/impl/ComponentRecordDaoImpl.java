package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.entity.ComponentRecord;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ComponentRecordDaoImpl implements IComponentRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(ComponentRecord componentRecord) throws Exception {
        List<ComponentRecord> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentRecord == null ? "{}" : objectMapper.writeValueAsString(componentRecord);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ComponentRecord> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.COMPONENT_RECORD, ComponentRecord.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public List queryByComponentIdAndVersion(ComponentRecord componentRecord) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(componentRecord.getComponent_id())) {
            c.and(Dict.COMPONENT_ID).is(componentRecord.getComponent_id());
        }
        if (StringUtils.isNotBlank(componentRecord.getVersion())) {
            c.and(Dict.VERSION).is(componentRecord.getVersion());
        }
        Query query = new Query(c);
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public List queryByComponentIdAndRegexVersion(ComponentRecord componentRecord) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(componentRecord.getComponent_id())) {
            c.and(Dict.COMPONENT_ID).is(componentRecord.getComponent_id());
        }
        if (StringUtils.isNotBlank(componentRecord.getVersion())) {
            String version;
            switch (componentRecord.getVersion()) {
                case "snapshot":
                    version = ".*" + Dict._SNAPSHOT;
                    break;
                case "rc":
                    version = ".*" + Dict._RC + ".*";
                    break;
                case "release":
                    version = ".*" + Dict._RELEASE;
                    break;
                default:
                    version = ".*" + Dict._RELEASE;
            }
            c.and(Dict.VERSION).regex("^" + version + "$", "i");
        }
        c.and(Dict.PACKAGETYPE).nin(Constants.PREPACKAGE);
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public ComponentRecord queryByComponentIdAndVersion(String component_id, String version) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id).and(Dict.VERSION).is(version));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public ComponentRecord queryByComponentIdAndType(String component_id, String type) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id).and(Dict.TYPE).is(type));
        return mongoTemplate.findOne(query, ComponentRecord.class);
    }


    @Override
    public ComponentRecord save(ComponentRecord componentRecord) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentRecord.set_id(objectId);
        componentRecord.setId(id);
        return mongoTemplate.save(componentRecord);
    }

    @Override
    public ComponentRecord update(ComponentRecord componentRecord) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(componentRecord.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(componentRecord);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, componentRecord.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, ComponentRecord.class);
        return this.mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public ComponentRecord queryById(ComponentRecord componentRecord) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentRecord.getId()));
        return mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public List<ComponentRecord> queryListByType(String componentid, String type) {
        Query query = new Query(Criteria.where(Dict.TYPE).is(type).and(Dict.COMPONENT_ID).is(componentid));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public ComponentRecord queryByType(String componentid, String type) {
        Query query = new Query(Criteria.where(Dict.TYPE).is(type).and(Dict.COMPONENT_ID).is(componentid));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public long deleteByComponentId(String component_id) {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(component_id));
        return mongoTemplate.remove(query, ComponentRecord.class).getDeletedCount();
    }

    @Override
    public ComponentRecord upsert(ComponentRecord componentRecord) throws Exception {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentRecord.getComponent_id()).and(Dict.VERSION).is(componentRecord.getVersion()));
        List<ComponentRecord> list = mongoTemplate.find(query, ComponentRecord.class);
        if (CommonUtils.isNullOrEmpty(list)) {
            return this.save(componentRecord);
        } else {
            return this.updateByCidAndVersion(componentRecord);
        }

    }

    @Override
    public List queryRecordByComponentIdAndVersion(String componentId, String tagName) {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and(Dict.VERSION).regex("^" + tagName + "$").and(Dict.PACKAGETYPE).nin(Constants.PREPACKAGE));
        return mongoTemplate.find(query, ComponentRecord.class, Dict.COMPONENT_RECORD);
    }

    @Override
    public List queryRecordForDestroy(String componentId, String tagName) {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and(Dict.VERSION).regex("^" + tagName + "$"));
        return mongoTemplate.find(query, ComponentRecord.class, Dict.COMPONENT_RECORD);
    }

    @Override
    public ComponentRecord queryByPipId(String pipid) {
        Query query = new Query(Criteria.where(Dict.PIP_ID).is(pipid));
        return mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public int queryCompoentRecoreds(String componentId) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId));
        List<ComponentRecord> recordList = mongoTemplate.find(query, ComponentRecord.class);
        if (recordList != null)
            return recordList.size();
        return 0;
    }


    public ComponentRecord updateByCidAndVersion(ComponentRecord componentRecord) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentRecord.getComponent_id()).and(Dict.VERSION).is(componentRecord.getVersion()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(componentRecord);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.COMPONENT_ID, componentRecord.getComponent_id());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, ComponentRecord.class);
        return this.mongoTemplate.findOne(query, ComponentRecord.class);
    }

    @Override
    public void delete(ComponentRecord componentRecord) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentRecord.getId()));
        mongoTemplate.findAndRemove(query, ComponentRecord.class);
    }


    /**
     * 根据组件id，版本类型，当前目标版本查询当前组件所有版本
     *
     * @param componentId       组件id
     * @param alphaOrRc         版本类型 alpha rc release
     * @param numVersion        版本匹配
     * @param containPrepackage 是否版本prepackage类型版本
     * @return
     */
    @Override
    public List<ComponentRecord> getAlphaOrRcVersion(String componentId, String alphaOrRc, String numVersion, Boolean containPrepackage) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(componentId)) {
            c.and(Dict.COMPONENT_ID).is(componentId);
        }
        String version;
        //如果传入具体版本，则进行版本匹配，如1.0.1，否则进行全匹配
        if (StringUtils.isNotBlank(numVersion)) {
            version = numVersion;
        } else {
            version = ".*";
        }
        //如果传入类型，如alpha或rc，则进行类型匹配，否则进行全匹配
        if (StringUtils.isNotBlank(alphaOrRc)) {
            switch (alphaOrRc) {
                case "alpha":
                    version = version + Constants._ALPHA + ".*";
                    break;
                case "beta":
                    version = version + Constants._BETA + ".*";
                    break;
                case "rc":
                    version = version + Constants._RCLOWCASE + ".*";
                    break;
                case "release":
                    version = "\\d+\\.\\d+\\.\\d+";
                    break;
                default:
                    break;
            }
        }
        c.and(Dict.VERSION).regex("^" + version + "$", "i");
        //是否包含prepackage的版本
        if (!containPrepackage) {
            c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        }
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public List<ComponentRecord> queryByComponentIdAndBranchs(String component_id, String numVersion, List<String> branchList) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(component_id)) {
            c.and(Dict.COMPONENT_ID).is(component_id);
        }
        String version;
        if (StringUtils.isNotBlank(numVersion)) {
            version = numVersion + ".*";
        } else {
            version = ".*";
        }
        c.and(Dict.VERSION).regex("^" + version + "$", "i");
        //是否包含prepackage的版本
        c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        //分支匹配
        c.and(Dict.BRANCH).in(branchList);
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public List<ComponentRecord> queryByComponentIdAndBranch(String component_id, String numVersion, String feature_branch, String stage) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(component_id)) {
            c.and(Dict.COMPONENT_ID).is(component_id);
        }
        String version;
        //如果传入具体版本，则进行版本匹配，如1.0.1，否则进行全匹配
        if (StringUtils.isNotBlank(numVersion)) {
            version = numVersion + ".*";
        } else {
            version = ".*";
        }
        //如果传入类型，如alpha或beta,rc，则进行类型匹配，否则进行全匹配
        if (StringUtils.isNotBlank(stage)) {
            switch (stage) {
                case "1":
                    version = version + Constants._ALPHA + ".*";
                    break;
                case "2":
                    version = version + Constants._BETA + ".*";
                    break;
                case "3":
                    version = version + Constants._RCLOWCASE + ".*";
                    break;
                default:
                    break;
            }
        }
        c.and(Dict.VERSION).regex("^" + version + "$", "i");
        //是否包含prepackage的版本
        c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        //分支匹配
        if(!CommonUtils.isNullOrEmpty(feature_branch)){
            c.and(Dict.BRANCH).is(feature_branch);
        }
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public Map<String, Object> queryIssueData(String startTime, String endTime) {
        HashMap<String, Object> components = new HashMap<>();
        HashMap<String, Integer> ComponentNum = new HashMap<>();
        HashMap<String, Integer> MpassComponentNum = new HashMap<>();
        Criteria c = new Criteria();
        c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        if (!CommonUtils.isNullOrEmpty(startTime)){
            c.and(Dict.DATE).gte(startTime);
        }
        //查询Component数量
        List<ComponentRecord> alphaNum = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).orOperator(
                        Criteria.where(Dict.VERSION).regex("^.*-SNAPSHOT$", "i"),
                        Criteria.where(Dict.VERSION).regex("^.*-RC.*$", "i")),c
        )), ComponentRecord.class);
        List<ComponentRecord> releaseNum = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).orOperator(
                        Criteria.where(Dict.VERSION).regex("^.*-RELEASE$", "i").and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE)),c
        )), ComponentRecord.class);
        //查询MPassComponent数量
        List<ComponentRecord> mPassComponent_alpha = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).orOperator(
                        Criteria.where(Dict.VERSION).regex("^.*-alpha.*$", "i"),
                        Criteria.where(Dict.VERSION).regex("^.*-beta.*$", "i"),
                        Criteria.where(Dict.VERSION).regex("^.*-rc.*$", "i")
                ),c
        )), ComponentRecord.class);
        List<ComponentRecord> mPassComponent_release = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).and(Dict.VERSION).regex("^\\d+\\.\\d+\\.\\d+$", "i"),c)), ComponentRecord.class);
        ComponentNum.put(Dict.RELEASE.toLowerCase(), releaseNum.size());
        ComponentNum.put(Dict.ALPHA, alphaNum.size());
        ComponentNum.put(Dict.TOTAL, releaseNum.size() + alphaNum.size());
        MpassComponentNum.put(Dict.RELEASE.toLowerCase(), mPassComponent_release.size());
        MpassComponentNum.put(Dict.ALPHA, mPassComponent_alpha.size());
        MpassComponentNum.put(Dict.TOTAL, mPassComponent_release.size() + mPassComponent_alpha.size());
        components.put(Dict.COMPONENTS, ComponentNum);
        components.put(Dict.MPASSCOMPONENTS, MpassComponentNum);
        return components;
    }

    @Override
    public List queryRecordByComponentIdAndVersionandFeature(String componentId, String tagName, String featureBranch) {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and(Dict.VERSION).regex("^" + tagName + "$").and(Dict.PACKAGETYPE).nin(Constants.PREPACKAGE).and(Dict.BRANCH).is(featureBranch));
        return mongoTemplate.find(query, ComponentRecord.class, Dict.COMPONENT_RECORD);
    }

    @Override
    public List<ComponentRecord> queryByIssueId(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ISSUE_ID).is(id));
        return mongoTemplate.find(query, ComponentRecord.class);
    }

    @Override
    public List<ComponentRecord> queryReleaseRecordByComponentId(String componentId,String regex) {
        Query query = Query.query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and(Dict.VERSION).regex(regex).and(Dict.PACKAGETYPE).is(Constants.PIPEPACKAGE));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        query.limit(10);
        return mongoTemplate.find(query, ComponentRecord.class);
    }
}

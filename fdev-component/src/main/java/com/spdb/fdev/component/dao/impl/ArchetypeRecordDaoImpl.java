package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IArchetypeRecordDao;
import com.spdb.fdev.component.entity.ArchetypeRecord;
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
public class ArchetypeRecordDaoImpl implements IArchetypeRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @SuppressWarnings("rawtypes")
    @Override
    public List query(ArchetypeRecord archetypeRecord) throws Exception {
        List<ArchetypeRecord> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = archetypeRecord == null ? "{}" : objectMapper.writeValueAsString(archetypeRecord);

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
        AggregationResults<ArchetypeRecord> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.COMPONENT_RECORD, ArchetypeRecord.class);
        docs.forEach(result::add);
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryByArchetypeIdAndVersion(ArchetypeRecord archetypeRecord) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(archetypeRecord.getArchetype_id())) {
            c.and(Dict.ARCHETYPE_ID).is(archetypeRecord.getArchetype_id());
        }
        if (StringUtils.isNotBlank(archetypeRecord.getVersion())) {
            c.and(Dict.VERSION).is(archetypeRecord.getVersion());
        }
        Query query = new Query(c);
        return mongoTemplate.find(query, ArchetypeRecord.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryByArchetypeIdAndRegexVersion(ArchetypeRecord archetypeRecord) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(archetypeRecord.getArchetype_id())) {
            c.and(Dict.ARCHETYPE_ID).is(archetypeRecord.getArchetype_id());
        }
        if (StringUtils.isNotBlank(archetypeRecord.getVersion())) {
            String version = archetypeRecord.getVersion();
            switch (archetypeRecord.getVersion()) {
                case "snapshot":
                    version = ".*" + Dict._SNAPSHOT;
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
        return mongoTemplate.find(query, ArchetypeRecord.class);
    }

    @Override
    public ArchetypeRecord queryByArchetypeIdAndVersion(String archetype_id, String version) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetype_id).and(Dict.VERSION).is(version));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public ArchetypeRecord queryByArchetypeIdAndType(String archetype_id, String type) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetype_id).and(Dict.TYPE).is(type));
        return mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public ArchetypeRecord save(ArchetypeRecord archetypeRecord) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        archetypeRecord.set_id(objectId);
        archetypeRecord.setId(id);
        return mongoTemplate.save(archetypeRecord);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArchetypeRecord update(ArchetypeRecord archetypeRecord) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(archetypeRecord.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(archetypeRecord);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, archetypeRecord.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, ArchetypeRecord.class);
        return this.mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public ArchetypeRecord queryById(ArchetypeRecord archetypeRecord) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeRecord.getId()));
        return mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public ArchetypeRecord queryByType(String archetypeId, String type) {
        Query query = new Query(Criteria.where(Dict.TYPE).is(type).and(Dict.ARCHETYPE_ID).is(archetypeId));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public long deleteByArchetypeId(String archetypeId) {
        Query query = Query.query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId));
        return mongoTemplate.remove(query, ArchetypeRecord.class).getDeletedCount();
    }

    @Override
    public ArchetypeRecord upsert(ArchetypeRecord archetypeRecord) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeRecord.getArchetype_id()).and(Dict.VERSION).is(archetypeRecord.getVersion()));
        List<ArchetypeRecord> list = mongoTemplate.find(query, ArchetypeRecord.class);
        if (CommonUtils.isNullOrEmpty(list)) {
            return this.save(archetypeRecord);
        } else {
            return this.updateByCidAndVersionAndPipId(archetypeRecord);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryRecordByArchetypeIdAndVersion(String archetypeId, String tagName) {
        Query query = Query.query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and(Dict.VERSION).regex("^" + tagName + "$").and(Dict.PACKAGETYPE).nin(Constants.PREPACKAGE));
        return mongoTemplate.find(query, ArchetypeRecord.class, Dict.ARCHETYPE_RECORD);
    }

    @Override
    public ArchetypeRecord queryByPipId(String pipid) {
        Query query = new Query(Criteria.where(Dict.PIP_ID).is(pipid));
        return mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public int queryArchetypeRecoreds(String archetypeId) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId));
        List<ArchetypeRecord> recordList = mongoTemplate.find(query, ArchetypeRecord.class);
        if (recordList != null) return recordList.size();
        return 0;
    }


    /**
     * 获取最新的非SNAPSHOT的版本
     *
     * @param archetypeId
     * @return
     */
    @Override
    public ArchetypeRecord queryLatestRelease(String archetypeId) {
        Criteria c = new Criteria();
        c.and(Dict.ARCHETYPE_ID).is(archetypeId);
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        List<ArchetypeRecord> recordList = mongoTemplate.find(query, ArchetypeRecord.class);
        if (!CommonUtils.isNullOrEmpty(recordList)) {
            for (int i = 0; i < recordList.size(); i++) {
                ArchetypeRecord record = recordList.get(i);
                if (record.getVersion().contains(Constants.SNAPSHOT) || record.getVersion().contains(Constants.SNAPSHOT_LOWCASE))
                    continue;
                else {
                    return record;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public ArchetypeRecord updateByCidAndVersionAndPipId(ArchetypeRecord archetypeRecord) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeRecord.getArchetype_id()).and(Dict.VERSION).is(archetypeRecord.getVersion()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(archetypeRecord);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ARCHETYPE_ID, archetypeRecord.getArchetype_id());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, ArchetypeRecord.class);
        return this.mongoTemplate.findOne(query, ArchetypeRecord.class);
    }

    @Override
    public void delete(ArchetypeRecord archetypeRecord) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeRecord.getId()));
        mongoTemplate.findAndRemove(query, ArchetypeRecord.class);
    }

    @Override
    public Map<String, Integer> queryIssueData(String startTime, String endTime) {
        Criteria c = new Criteria();
        c.and(Dict.PACKAGETYPE).nin(Constants.PREPACKAGE);
        if (!CommonUtils.isNullOrEmpty(startTime)) {
            c.and(Dict.DATE).gte(startTime);
        }
        HashMap<String, Integer> archetyperecordNum = new HashMap<>();
        List<ArchetypeRecord> archetyperecord_snapshot = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).and(Dict.VERSION).regex("^.*-SNAPSHOT$", "i"), c)), ArchetypeRecord.class);
        List<ArchetypeRecord> ArchetypeRecord_release = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.DATE).lte(endTime).and(Dict.VERSION).regex("^.*-RELEASE$", "i"), c)), ArchetypeRecord.class);
        archetyperecordNum.put(Dict.ALPHA, archetyperecord_snapshot.size());
        archetyperecordNum.put(Dict.RELEASE.toLowerCase(), ArchetypeRecord_release.size());
        archetyperecordNum.put(Dict.TOTAL, archetyperecord_snapshot.size() + ArchetypeRecord_release.size());
        return archetyperecordNum;
    }

    @Override
    public List<ArchetypeRecord> queryReleaseRecordByArchetypeId(String archetypeId) {
        Query query = Query.query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and(Dict.VERSION).regex("^.*" + Dict._RELEASE + "$").and(Dict.PACKAGETYPE).is(Constants.PIPEPACKAGE));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        query.limit(10);
        return mongoTemplate.find(query, ArchetypeRecord.class);
    }
}

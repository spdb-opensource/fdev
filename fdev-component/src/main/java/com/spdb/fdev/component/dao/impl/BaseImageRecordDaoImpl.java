package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IBaseImageRecordDao;
import com.spdb.fdev.component.entity.BaseImageRecord;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class BaseImageRecordDaoImpl implements IBaseImageRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(BaseImageRecord baseImageRecord) throws Exception {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(baseImageRecord.getName())) {
            c.and(Dict.NAME).is(baseImageRecord.getName());
        }
        if (!CommonUtils.isNullOrEmpty(baseImageRecord.getStage())) {
            c.and(Dict.STAGE).is(baseImageRecord.getStage());
        }
        c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        Query query = new Query(c);
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        return mongoTemplate.find(query, BaseImageRecord.class);
    }

    @Override
    public BaseImageRecord queryByNameAndTrialApps(String name, String gitlab_id) {
        //获取带有使用项目的trial版本镜像
        Query queryTrial = new Query(Criteria.where(Dict.TRIAL_APPS).in(gitlab_id).and(Dict.NAME).is(name).and(Dict.STAGE).is(ComponentEnum.ImageStageEnum.TRIAL.getValue()).and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE));
        queryTrial.with(new Sort(Sort.Direction.DESC, Dict.IMAGE_TAG));
        List<BaseImageRecord> recordList = mongoTemplate.find(queryTrial, BaseImageRecord.class);
        //获取正式发布版本镜像
        Query queryRelease = new Query(Criteria.where(Dict.NAME).is(name).and(Dict.STAGE).is(ComponentEnum.ImageStageEnum.RELEASE.getValue()).and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE));
        BaseImageRecord baseImageRecord = mongoTemplate.findOne(queryRelease, BaseImageRecord.class);
        //如果缺少发布版本则抛出异常
        if (baseImageRecord == null && (CommonUtils.isNullOrEmpty(recordList))) {
            throw new FdevException(ErrorConstants.IMAGE_NOT_EXIST, new String[]{name});
        } else if (baseImageRecord == null) {
            return recordList.get(0);
        } else if (CommonUtils.isNullOrEmpty(recordList)) {
            return baseImageRecord;
        } else {
            return recordList.get(0).getImage_tag().compareTo(baseImageRecord.getImage_tag()) > 0 ? recordList.get(0) : baseImageRecord;
        }
    }

    @Override
    public BaseImageRecord save(BaseImageRecord baseImageRecord) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        baseImageRecord.set_id(objectId);
        baseImageRecord.setId(id);
        return mongoTemplate.save(baseImageRecord);
    }

    @Override
    public BaseImageRecord update(BaseImageRecord baseImageRecord) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(baseImageRecord.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(baseImageRecord);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, baseImageRecord.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, BaseImageRecord.class);
        return this.mongoTemplate.findOne(query, BaseImageRecord.class);
    }

    @Override
    public BaseImageRecord queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, BaseImageRecord.class);
    }

    @Override
    public List<BaseImageRecord> queryByNameStage(String name, String stage) {
        Query query = Query.query(Criteria.where(Dict.NAME).is(name)
                .and(Dict.STAGE).is(stage)
        );
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        return mongoTemplate.find(query, BaseImageRecord.class);
    }

    @Override
    public void upsert(BaseImageRecord baseImageRecord) {

    }

    @Override
    public BaseImageRecord queryByPipId(String pipid) {
        Query query = new Query(Criteria.where(Dict.PIP_ID).is(pipid));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, BaseImageRecord.class);
    }

    @Override
    public BaseImageRecord queryByNameAndTag(String name, String tag) {
        Query query = new Query(Criteria.where(Dict.NAME).is(name).and(Dict.IMAGE_TAG).is(tag));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, BaseImageRecord.class);
    }

    @Override
    public List<BaseImageRecord> queryIssueRecord(String name, String branch, String stage) {
        Query query = new Query(Criteria.where(Dict.NAME).is(name).and(Dict.BRANCH).is(branch).and(Dict.STAGE).is(stage).and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        return mongoTemplate.find(query, BaseImageRecord.class);
    }

    @Override
    public void delete(BaseImageRecord baseImageRecord) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(baseImageRecord.getId()));
        mongoTemplate.findAndRemove(query, BaseImageRecord.class);
    }

    @Override
    public Map<String, Integer> queryIssueData(String startTime, String endTime) {
        HashMap<String, Integer> baseImageNum = new HashMap<>();
        Criteria c = new Criteria();
        c.and(Dict.PACKAGETYPE).ne(Constants.PREPACKAGE);
        if (!CommonUtils.isNullOrEmpty(startTime)) {
            c.and(Dict.UPDATE_TIME).gte(startTime);
        }
        List<BaseImageRecord> baseImage_trial = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.UPDATE_TIME).lte(endTime).orOperator(
                        Criteria.where(Dict.STAGE).is(Dict.TRIAL),
                        Criteria.where(Dict.STAGE).is(Dict.DEV)
                ), c
        )), BaseImageRecord.class);
        List<BaseImageRecord> baseImage_release = mongoTemplate.find(new Query(new Criteria().andOperator(
                Criteria.where(Dict.UPDATE_TIME).lte(endTime).orOperator(
                        Criteria.where(Dict.STAGE).is(Constants.RELEASE_LOWCASE),
                        Criteria.where(Dict.STAGE).is(Dict.INVALID)
                ), c
        )), BaseImageRecord.class);
        baseImageNum.put(Dict.ALPHA, baseImage_trial.size());
        baseImageNum.put(Dict.RELEASE.toLowerCase(), baseImage_release.size());
        baseImageNum.put(Dict.TOTAL, baseImage_trial.size() + baseImage_release.size());
        return baseImageNum;
    }

}

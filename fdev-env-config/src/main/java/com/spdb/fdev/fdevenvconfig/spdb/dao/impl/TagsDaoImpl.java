package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.TagsDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AutoConfigTags;
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
public class TagsDaoImpl implements TagsDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveTag(AutoConfigTags autoConfigTags) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        autoConfigTags.set_id(objectId);
        autoConfigTags.setId(id);
        mongoTemplate.save(autoConfigTags);
    }

    @Override
    public AutoConfigTags queryByGitlabId(Integer gitlabId) {
        Query query = Query.query(Criteria.where(Constants.GITLAB_ID).is(gitlabId));
        return mongoTemplate.findOne(query, AutoConfigTags.class);
    }

    @Override
    public void update(Integer gitlabId, List<Map<String, Object>> tagsInfoList) {
        Query query = Query.query(Criteria.where(Constants.GITLAB_ID).is(gitlabId));
        Update update = new Update();
        update.set(Dict.TAGINFO, tagsInfoList);
        mongoTemplate.findAndModify(query, update, AutoConfigTags.class);
    }

    @Override
    public void updateConfigGitlabId(AutoConfigTags autoConfigTags) {
        Query query = new Query(Criteria.where(Dict.GITLABID).is(autoConfigTags.getGitlab_id()));
        Update update = Update.update(Dict.GITLABID, autoConfigTags.getGitlab_id())
                .set(Dict.CONFIG_GITLAB_ID, autoConfigTags.getConfigGitlabId());
        this.mongoTemplate.upsert(query, update, AutoConfigTags.class);
    }
}

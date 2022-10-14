package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.IReleaseRqrmntDao;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReleaseRqrmntDaoImpl implements IReleaseRqrmntDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public ReleaseRqrmnt queryByRqrmntId(String release_node_name, String rqrmnt_id, String type) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.TYPE).is(type));
        if(!CommonUtils.isNullOrEmpty(rqrmnt_id)) {
            query.addCriteria(new Criteria(Dict.RQRMNT_ID_L).is(rqrmnt_id));
        }
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ReleaseRqrmnt.class);
    }

    @Override
    public ReleaseRqrmnt editRqrmntFile(ReleaseRqrmnt releaseRqrmnt) {
        Query query = new Query(Criteria.where(Dict.ID).is(releaseRqrmnt.getId()));
        Update update = Update.update(Dict.RQRMNT_FILE, releaseRqrmnt.getRqrmnt_file())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmnt.class);
        return mongoTemplate.findOne(query, ReleaseRqrmnt.class);
    }

    @Override
    public void saveReleaseRqrmnt(ReleaseRqrmnt releaseRqrmnt) {
        mongoTemplate.save(releaseRqrmnt);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, ReleaseRqrmnt.class);
    }

    @Override
    public List<ReleaseRqrmnt> queryByReleaseNodeName(String release_node_name) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, ReleaseRqrmnt.class);
    }

    @Override
    public ReleaseRqrmnt editTaskMap(ReleaseRqrmnt releaseRqrmnt) {
        Query query = new Query(Criteria.where(Dict.ID).is(releaseRqrmnt.getId()));
        Update update = Update.update(Dict.TASK_MAP, releaseRqrmnt.getTask_map())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmnt.class);
        return mongoTemplate.findOne(query, ReleaseRqrmnt.class);
    }

    @Override
    public void updateNode(String old_release_node_name, String release_node_name) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.updateMulti(query, update, ReleaseRqrmnt.class);
    }

    @Override
    public ReleaseRqrmnt queryByReleaseNodeNameAndTaskId(String release_node_name, String taskId) {
        Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.TASK_MAP+"."+ taskId).exists(true));
        return mongoTemplate.findOne(query, ReleaseRqrmnt.class);
    }
}

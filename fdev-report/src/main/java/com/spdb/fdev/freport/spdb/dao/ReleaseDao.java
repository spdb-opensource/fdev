package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.entity.release.ReleaseNode;
import com.spdb.fdev.freport.spdb.entity.release.ReleaseTask;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ReleaseDao extends BaseDao {

    public List<ReleaseNode> find(ReleaseNode releaseNode, Set<String> nameSet, String startTime, String endTime) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(releaseNode.getOwnerGroupId()))
            query.addCriteria(Criteria.where(EntityDict.OWNER_GROUP_ID).is(releaseNode.getOwnerGroupId()));
        if (!CommonUtils.isNullOrEmpty(releaseNode.getNodeStatus()))
            query.addCriteria(Criteria.where(EntityDict.NODE_STATUS).is(releaseNode.getNodeStatus()));
        if (!CommonUtils.isNullOrEmpty(nameSet))
            query.addCriteria(Criteria.where(EntityDict.RELEASE_NODE_NAME).in(nameSet));
        if (!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime))
            query.addCriteria(new Criteria().andOperator(Criteria.where(EntityDict.RELEASE_DATE).gte(startTime), Criteria.where(EntityDict.RELEASE_DATE).lte(endTime)));
        return getMongoTempate(MongoConstant.RELEASE).find(query, ReleaseNode.class);
    }

    public List<ReleaseTask> findReleaseTaskByTaskId(Set<String> taskIdSet) {
        return getMongoTempate(MongoConstant.RELEASE).find(new Query().addCriteria(Criteria.where(EntityDict.TASK_ID).in(taskIdSet)), ReleaseTask.class);
    }
}

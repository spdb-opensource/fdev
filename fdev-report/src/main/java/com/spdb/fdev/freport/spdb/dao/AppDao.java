package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.entity.app.AppEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AppDao extends BaseDao {

    public List<AppEntity> find(AppEntity appEntity, String startTime, String endTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(appEntity.getGroup()))
            query.addCriteria(Criteria.where(EntityDict.GROUP).is(appEntity.getGroup()));
        if (!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime)) {
            String createTime = "createtime";// TMD这么傻逼的字段老子真的不想在字典类里面维护，太TM丑了
            criteria.orOperator(new Criteria().andOperator(Criteria.where(createTime).gte(startTime), Criteria.where(createTime).lte(endTime)));
        }
        query.addCriteria(Criteria.where(EntityDict.STATUS).ne("0"));//这操作原因详见实体类中status字段的注释
        return getMongoTempate(MongoConstant.APP).find(query, AppEntity.class);
    }

    public AppEntity findByProjectId(Integer projectId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.GITLAB_PROJECT_ID).is(projectId));
        query.addCriteria(Criteria.where(EntityDict.STATUS).ne("0"));
        return getMongoTempate(MongoConstant.APP).findOne(query, AppEntity.class);
    }

    public List<AppEntity> findByGroups(Set<String> groupIdSet) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.GROUP).in(groupIdSet));
        query.addCriteria(Criteria.where(EntityDict.STATUS).ne("0"));//这个字段明显是后期加上的然后又没有做数据清洗，所以这里用ne0
        return getMongoTempate(MongoConstant.APP).find(query, AppEntity.class);
    }

    public List<AppEntity> findByIds(Set<String> appIdSet) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.ID).in(appIdSet));
        query.addCriteria(Criteria.where(EntityDict.STATUS).ne("0"));//这个字段明显是后期加上的然后又没有做数据清洗，所以这里用ne0
        return getMongoTempate(MongoConstant.APP).find(query, AppEntity.class);
    }

    public List<AppEntity> findAllApp() {
        return getMongoTempate(MongoConstant.APP).findAll(AppEntity.class);
    }
}

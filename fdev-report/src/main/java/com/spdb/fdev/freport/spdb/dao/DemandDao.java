package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.DemandEnum;
import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.entity.demand.DemandBaseInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DemandDao extends BaseDao {

    public List<DemandBaseInfo> findByGroupIdWithOutCanceled(DemandBaseInfo demandInfo, String startTime, String endTime) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where(EntityDict.DEMAND_LEADER_GROUP).is(demandInfo.getDemandLeaderGroup()), Criteria.where(EntityDict.RELATE_PART).in(new HashSet<String>() {{
            add(demandInfo.getDemandLeaderGroup());
        }}));
        if (!CommonUtils.isNullOrEmpty(demandInfo.getDemandType())) {
            query.addCriteria(Criteria.where(EntityDict.DEMAND_TYPE).is(demandInfo.getDemandType()));
        }
        if (!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime)) {
            criteria.andOperator(Criteria.where(EntityDict.DEMAND_CREATE_TIME).gte(startTime), Criteria.where(EntityDict.DEMAND_CREATE_TIME).lte(endTime));
        }
        query.addCriteria(Criteria.where(EntityDict.DEMAND_STATUS_NORMAL).ne(DemandEnum.DemandStatusEnum.IS_CANCELED.getValue()));
        query.addCriteria(criteria);
        return getMongoTempate(MongoConstant.DEMAND).find(query, DemandBaseInfo.class);
    }

    /**
     * 特殊定制化dao方法 因为参数特别
     *
     * @param groupIdSet
     * @param startTime
     * @param endTime
     * @return
     */
    public List<DemandBaseInfo> findDemandThroughputStatistics(Set<String> groupIdSet, Boolean showDaily, String startTime, String endTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        query.addCriteria(Criteria.where(EntityDict.DEMAND_LEADER_GROUP).in(groupIdSet));
        query.addCriteria(Criteria.where(EntityDict.DEMAND_STATUS_NORMAL).in(DemandEnum.DemandStatusEnum.PRODUCT.getValue(), DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue()));
        if (!showDaily) {
            query.addCriteria(Criteria.where(EntityDict.DEMAND_TYPE).in(EntityDict.DEMAND_TYPE_BUSINESS, EntityDict.DEMAND_TYPE_TECH));
        }
        if (!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime)) {
            criteria.andOperator(Criteria.where(EntityDict.REAL_PRODUCT_DATE).gte(startTime), Criteria.where(EntityDict.REAL_PRODUCT_DATE).lte(endTime));
        }
        query.addCriteria(criteria);
        return getMongoTempate(MongoConstant.DEMAND).find(query, DemandBaseInfo.class);
    }

    public List<DemandBaseInfo> findDemandByIdSet(Set<String> idSet) {
        return getMongoTempate(MongoConstant.DEMAND).find(new Query().addCriteria(Criteria.where(EntityDict.ID).in(idSet)), DemandBaseInfo.class);
    }

    public List<DemandBaseInfo> findDemandByLeaderGroup(Set<String> leaderGroupIds, String priority) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(priority)) {
            query.addCriteria(Criteria.where(EntityDict.PRIORITY).is(priority));
        }
        query.addCriteria(Criteria.where(EntityDict.DEMAND_LEADER_GROUP).in(leaderGroupIds));
        return getMongoTempate(MongoConstant.DEMAND).find(query, DemandBaseInfo.class);
    }
}

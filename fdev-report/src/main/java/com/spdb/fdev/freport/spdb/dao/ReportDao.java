package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.entity.report.DashBoardUserConfig;
import com.spdb.fdev.freport.spdb.entity.report.DashboardTips;
import com.spdb.fdev.freport.spdb.entity.report.GitlabMergeRecord;
import com.spdb.fdev.freport.spdb.entity.sonar.SonarProjectRank;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ReportDao extends BaseDao {

    public DashBoardUserConfig findUserConfig(DashBoardUserConfig config) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.USER_ID).is(config.getUserId()));
        query.addCriteria(Criteria.where(EntityDict.GROUPID).is(config.getGroupId()));
        return getMongoTempate(MongoConstant.REPORT).findOne(query, DashBoardUserConfig.class);
    }

    public void upsertDashBoardUserConfig(DashBoardUserConfig config) {
        Query query = new Query();
        query.addCriteria(new Criteria().and(EntityDict.USER_ID).is(config.getUserId()));
        query.addCriteria(new Criteria().and(EntityDict.GROUPID).is(config.getGroupId()));
        Update update = new Update();
        update.set(EntityDict.CONFIGS, config.getConfigs());
        update.set(EntityDict.GROUPID, config.getGroupId());
        getMongoTempate(MongoConstant.REPORT).upsert(query, update, DashBoardUserConfig.class);
    }

    public List<GitlabMergeRecord> findGitlabMergeRecord(Set<String> groupIdSet, String startDate, String endDate) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(groupIdSet))
            query.addCriteria(Criteria.where(EntityDict.GROUPID).in(groupIdSet));
        if (!CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate))
            query.addCriteria(new Criteria().andOperator(Criteria.where(EntityDict.CREATE_TIME).gte(startDate), Criteria.where(EntityDict.CREATE_TIME).lte(endDate)));
        return getMongoTempate(MongoConstant.REPORT).find(query, GitlabMergeRecord.class);
    }

    public void insertGitlabMergeRecord(GitlabMergeRecord record) {
        record.init();
        getMongoTempate(MongoConstant.REPORT).insert(record);
    }

    public DashboardTips findDashboardTips(String code) {
        return getMongoTempate(MongoConstant.REPORT).findOne(new Query() {{
            addCriteria(Criteria.where(EntityDict.CODE).is(code));
        }}, DashboardTips.class);
    }

    public SonarProjectRank findSonarProjectRankByCode(String code) {
        return getMongoTempate(MongoConstant.REPORT).findOne(new Query(Criteria.where(EntityDict.CODE).is(code)), SonarProjectRank.class);
    }

    public void upsertSonarProjectRank(SonarProjectRank sonarBranchRank) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.CODE).is(sonarBranchRank.getCode()));
        Update update = new Update();
        update.set(EntityDict.CODE, sonarBranchRank.getCode());
        update.set(EntityDict.DATA, sonarBranchRank.getData());
        getMongoTempate(MongoConstant.REPORT).upsert(query, update, SonarProjectRank.class);
    }
}

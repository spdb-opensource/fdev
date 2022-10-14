package com.spdb.fdev.gitlabwork.dao.impl;

import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * GitlabWorkDaoImpl
 *
 * @blame Android Team
 */
@Component
public class GitlabWorkDaoImpl implements IGitlabWorkDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public GitlabWork save(GitlabWork gitlabWork) {
        return mongoTemplate.save(gitlabWork);
    }

    @Override
    public UpdateResult upsert(GitlabWork gitlabWork) {
        String userName = gitlabWork.getUserName();

        String committedDate = gitlabWork.getCommittedDate();

        Query query = new Query(Criteria.where(Dict.USERNAME).is(userName)

                .and(Dict.COMMITTEDDATE).is(committedDate));

        Update update = Update.update(Dict.COMMITTEDDATE, committedDate)
                .set(Dict.TOTAL, gitlabWork.getTotal())
                .set(Dict.ADDITIONS, gitlabWork.getAdditions())
                .set(Dict.DELETIONS, gitlabWork.getDeletions())
                .set(Dict.DEATIL, gitlabWork.getDeatil())
                .set(Dict.NICKNAME, gitlabWork.getNickName())
                .set(Dict.GROUPID, gitlabWork.getGroupid())
                .set(Dict.GROUPNAME, gitlabWork.getGroupname())
                .set(Dict.COMPANYID, gitlabWork.getCompanyid())
                .set(Dict.COMPANYNAME, gitlabWork.getCompanyname());

        UpdateResult updateResult = mongoTemplate.upsert(query, update, GitlabWork.class);
        return updateResult;
    }

    @Override
    public List<GitlabWork> select() {
        return mongoTemplate.findAll(GitlabWork.class);
    }

    @Override
    public List<GitlabWork> select(String userName, String companyId, String startDate, String endDate) {
        Query query = new Query();
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMPANYID).is(companyId).and(Dict.COMMITTEDDATE).gte(startDate).lte(endDate));
        } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMPANYID).is(companyId).and(Dict.COMMITTEDDATE).gte(startDate));
        } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMPANYID).is(companyId).and(Dict.COMMITTEDDATE).lte(endDate));
        } else {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMPANYID).is(companyId));
        }
        return mongoTemplate.find(query, GitlabWork.class);
    }

    @Override
    public List<GitlabWork> select1(String userName, String startDate, String endDate) {
        Query query = new Query();
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMMITTEDDATE).gte(startDate).lte(endDate));
        } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMMITTEDDATE).gte(startDate));
        } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName).and(Dict.COMMITTEDDATE).lte(endDate));
        } else {
            query.addCriteria(Criteria.where(Dict.USERNAME).is(userName));
        }
        return mongoTemplate.find(query, GitlabWork.class);
    }

    @Override
    public List<GitlabWork> selectByGroupId(String groupId, String startDate, String endDate) {
        Query query = new Query();

        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.COMMITTEDDATE).gte(startDate).lte(endDate));
        } else if (StringUtils.isNotBlank(startDate) && StringUtils.isBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.COMMITTEDDATE).gte(startDate));
        } else if (StringUtils.isBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.COMMITTEDDATE).lte(endDate));
        } else {
            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId));
        }

        return mongoTemplate.find(query, GitlabWork.class);
    }

    @Override
    public GitlabWork selectByUserNameAndCommitDate(String username, String commitDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.USERNAME).is(username).and(Dict.COMMITTEDDATE).is(commitDate));
        return mongoTemplate.findOne(query, GitlabWork.class);
    }
}

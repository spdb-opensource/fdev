package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.dto.PageDto;
import com.spdb.fdev.freport.spdb.entity.git.Commit;
import com.spdb.fdev.freport.spdb.vo.PageVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GitDao extends BaseDao {

    public List<Commit> findByCommitterEmail(Set<String> committerEmails, String startDate, String endDate, Set<Integer> gitIds) {
        return findByCommitterEmail(committerEmails, startDate, endDate, gitIds,null).getData();
    }

    //  New
    public PageVo<Commit> findByCommitterEmail(Set<String> committerEmails, String startDate, String endDate, Set<Integer> gitIds, PageDto pageDto) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        // query.addCriteria(Criteria.where(EntityDict.COMMITTER_EMAIL).in(committerEmails));
        criteria.orOperator(new ArrayList<Criteria>() {{
            for (String committerEmail : committerEmails) {
                add(Criteria.where(EntityDict.COMMITTER_EMAIL).regex(committerEmail, "i"));
            }
        }}.toArray(new Criteria[]{}));
        if (!CommonUtils.isNullOrEmpty(startDate)) startDate += " 00:00:00";
        if (!CommonUtils.isNullOrEmpty(endDate)) endDate += " 23:59:59";
        if (!CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate)) {
            criteria.andOperator(Criteria.where(EntityDict.COMMITTED_DATE).gte(startDate), Criteria.where(EntityDict.COMMITTED_DATE).lte(endDate));
        } else {
            if (!CommonUtils.isNullOrEmpty(startDate))
                query.addCriteria(Criteria.where(EntityDict.COMMITTED_DATE).gte(startDate));
            if (!CommonUtils.isNullOrEmpty(endDate))
                query.addCriteria(Criteria.where(EntityDict.COMMITTED_DATE).lte(endDate));
        }
        if(!CommonUtils.isNullOrEmpty(gitIds)){
            criteria.and(EntityDict.PROJECT_ID).in(gitIds);
        }
        query.addCriteria(criteria);
        return new PageVo<Commit>() {{
            if (pageDto != null) {
                setTotal(getMongoTempate(MongoConstant.GIT).count(query, Commit.class));
                int[] page = pageDto.getPageCondition();
                query.limit(page[1]).skip(page[0]);  //分页
            }
            //通过任务集编号排序
            query.with(new Sort(Sort.Direction.DESC, EntityDict.COMMITTED_DATE));
            setData(getMongoTempate(MongoConstant.GIT).find(query, Commit.class));
        }};
    }

}

package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.dto.PageDto;
import com.spdb.fdev.freport.spdb.entity.user.*;
import com.spdb.fdev.freport.spdb.vo.PageVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserDao extends BaseDao {

    public List<User> find(User user) {
        return find(user, null).getData();
    }

    public PageVo<User> find(User user, PageDto pageDto) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(user.getGroupId()))
            query.addCriteria(Criteria.where(EntityDict.GROUP_ID).is(user.getGroupId()));
        if (!CommonUtils.isNullOrEmpty(user.getGroupIds()))
            query.addCriteria(Criteria.where(EntityDict.GROUP_ID).in(user.getGroupIds()));
        if (!CommonUtils.isNullOrEmpty(user.getRoleId()))
            query.addCriteria(Criteria.where(EntityDict.ROLE_ID).in(user.getRoleId()));
        if (!CommonUtils.isNullOrEmpty(user.getCompanyId()))
            query.addCriteria(Criteria.where(EntityDict.COMPANY_ID).is(user.getCompanyId()));
        if (!CommonUtils.isNullOrEmpty(user.getStatus()))
            query.addCriteria(Criteria.where(EntityDict.STATUS).is(user.getStatus()));
        if (!CommonUtils.isNullOrEmpty(user.getAreaId()))
            query.addCriteria(Criteria.where(EntityDict.AREA_ID).is(user.getAreaId()));
        query.with(new Sort(Sort.Direction.ASC, EntityDict.GROUPID));
        return new PageVo<User>() {{
            if (pageDto != null) {
                setTotal(getMongoTempate(MongoConstant.USER).count(query, User.class));
                int[] page = pageDto.getPageCondition();
                query.limit(page[1]).skip(page[0]);  //分页
            }
            setData(getMongoTempate(MongoConstant.USER).find(query, User.class));
        }};
    }

    public List<Group> findGroup(Group group) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(group.getParentId()))
            query.addCriteria(Criteria.where(EntityDict.PARENT_ID).is(group.getParentId()));
        if (!CommonUtils.isNullOrEmpty(group.getStatus()))
            query.addCriteria(Criteria.where(EntityDict.STATUS).is(group.getStatus()));
        if (!CommonUtils.isNullOrEmpty(group.getSortNum()))
            query.addCriteria(Criteria.where(EntityDict.SORTNUM).regex(group.getSortNum()));
        return getMongoTempate(MongoConstant.USER).find(query, Group.class);
    }

    public List<Group> findGroups(Set<String> groupIds) {
        return findGroups(groupIds, "1");
    }

    public List<Group> findGroups(Set<String> groupIds, String status) {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.ID).in(groupIds)).addCriteria(Criteria.where(EntityDict.STATUS).is(status)), Group.class);
    }

    public List<User> findUsers(Set<String> userIds) {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.ID).in(userIds)), User.class);
    }

    public List<Group> findGroupBySortNum(Set<String> sortNumSet) {
        return findGroupBySortNum(sortNumSet, null);
    }

    public List<Group> findGroupBySortNum(Set<String> sortNumSet, String status) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.orOperator(sortNumSet.stream().map(x -> {
            return Criteria.where(EntityDict.SORTNUM).regex("^" + x + "-");
        }).toArray(Criteria[]::new));
        if (!CommonUtils.isNullOrEmpty(status)) {
            query.addCriteria(criteria).addCriteria(Criteria.where(EntityDict.STATUS).is(status));
        }
        return getMongoTempate(MongoConstant.USER).find(query, Group.class);
    }

    public List<User> findUserByGroupIds(Set<String> groupIdSet) {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.GROUP_ID).in(groupIdSet)).addCriteria(Criteria.where(EntityDict.STATUS).is("0")), User.class);
    }

    public List<Company> findCompany() {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.STATUS).is("1")), Company.class);
    }

    public List<Company> findCompany(Set<String> ids) {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.ID).in(ids)), Company.class);
    }

    public List<Role> findRole(Set<String> ids) {
        return getMongoTempate(MongoConstant.USER).find(new Query().addCriteria(Criteria.where(EntityDict.ID).in(ids)), Role.class);
    }

    public List<Area> findArea() {
        return getMongoTempate(MongoConstant.USER).findAll(Area.class);
    }
}

package com.spdb.fdev.gitlabwork.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitLabUserDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.util.Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitLabUserDaoImpl
 *
 * @blame Android Team
 */
@Component
public class GitLabUserDaoImpl implements IGitLabUserDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户信息
     *
     * @param gitlabUser
     * @return
     */
    @Override
    public GitlabUser save(GitlabUser gitlabUser) {
        return mongoTemplate.save(gitlabUser);
    }

    /**
     * 根据gituser用户名查询用户
     *
     * @param userid
     * @return
     */
    @Override
    public GitlabUser selectByUserId(String userid) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.USER_ID).is(userid)));
        return mongoTemplate.findOne(query, GitlabUser.class);
    }

    /**
     * 根据sign状态查询用户
     *
     * @param sign
     * @return
     */
    @Override
    public List<GitlabUser> selectBySign(int sign) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.SIGN).is(sign)));
        return mongoTemplate.find(query, GitlabUser.class);
    }

    @Override
    public void upsert(GitlabUser gitlabUser) {
        String user_id = gitlabUser.getUser_id();
        Query query = new Query(Criteria.where(Dict.USER_ID).is(user_id));
        Update update = new Update();
        update.set(Dict.NAME, gitlabUser.getName());
        update.set("username", gitlabUser.getUsername());
        update.set(Dict.GROUPID, gitlabUser.getGroupid());
        update.set(Dict.GROUPNAME, gitlabUser.getGroupname());
        update.set(Dict.GITUSER, gitlabUser.getGituser());
        update.set(Dict.COMPANYID, gitlabUser.getCompanyid());
        update.set(Dict.COMPANYNAME, gitlabUser.getCompanyname());
        update.set(Dict.ROLENAME, gitlabUser.getRolename());
        update.set(Dict.SIGN, gitlabUser.getSign());
        update.set(Dict.STATUS, gitlabUser.getStatus());
        if (!Util.isNullOrEmpty(gitlabUser.getAreaname()) && !Util.isNullOrEmpty(gitlabUser.getAreaname())) {
            update.set(Dict.AREAID, gitlabUser.getAreaid());
            update.set(Dict.AREANAME, gitlabUser.getAreaname());
        }
        update.set(Dict.CONFIGNAME, gitlabUser.getConfigname());

        mongoTemplate.upsert(query, update, GitlabUser.class);
    }

    /**
     * 查询用户信息列表
     *
     * @return
     */
    @Override
    public List<GitlabUser> select() {
        return mongoTemplate.findAll(GitlabUser.class);
    }

    /**
     * 删除Document
     */
    @Override
    public void removeGitlabUserInfo() {
        mongoTemplate.dropCollection(GitlabUser.class);

    }

    /**
     * 根据gituser用户名查询用户
     *
     * @param gituser
     * @return
     */
    @Override
    public GitlabUser selectByGitUser(String gituser) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.GITUSER).is(gituser)));
        return mongoTemplate.findOne(query, GitlabUser.class);
    }

    /**
     * 根据groupId查询所有用户
     *
     * @param groupId
     * @return
     */
    @Override
    public List<GitlabUser> selectByGroupId(String groupId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.GROUPID).is(groupId)));
        return mongoTemplate.find(query, GitlabUser.class);
    }

    @Override
    public List<GitlabUser> selectByGroupIdAndCompanyId(String groupId, String companyId) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(groupId)) {
            c.and(Dict.GROUPID).is(groupId);
        }
        if (StringUtils.isNotBlank(companyId)) {
            c.and(Dict.COMPANYID).is(companyId);
        }
        Query query = new Query(c);
//        Query query = new Query();
//        if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(companyId)) {
//            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.COMPANYID).is(companyId));
//        } else if (StringUtils.isNotBlank(groupId) && StringUtils.isBlank(companyId)) {
//            query.addCriteria(Criteria.where(Dict.GROUPID).is(groupId));
//        } else if (StringUtils.isBlank(groupId) && StringUtils.isNotBlank(companyId)) {
//            query.addCriteria(Criteria.where(Dict.COMPANYID).is(companyId));
//        } else {
//            query.addCriteria(new Criteria());
//        }
        return mongoTemplate.find(query, GitlabUser.class);
    }

    @Override
    public List<GitlabUser> selectCompanyIdAndRoleName(String companyId, String roleName, String status) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(companyId)) {
            c.and(Dict.COMPANYID).is(companyId);
        }
        if (StringUtils.isNotBlank(roleName)) {
            c.and(Dict.ROLENAME).regex(roleName);
        }
        if (StringUtils.isNotBlank(status)) {
            c.and(Dict.STATUS).is(status);
        }
        Query query = new Query(c);
//        Query query = new Query();
//        if (StringUtils.isNotBlank(companyId) && StringUtils.isNotBlank(roleName)) {
//            query.addCriteria(Criteria.where(Dict.COMPANYID).is(companyId).and(Dict.ROLENAME).regex(roleName));
//        } else if (StringUtils.isNotBlank(companyId) && StringUtils.isBlank(roleName)) {
//            query.addCriteria(Criteria.where(Dict.COMPANYID).is(companyId));
//        } else if (StringUtils.isBlank(companyId) && StringUtils.isNotBlank(roleName)) {
//            query.addCriteria(Criteria.where(Dict.ROLENAME).regex(roleName));
//        } else {
//            query.addCriteria(new Criteria());
//        }
        return mongoTemplate.find(query, GitlabUser.class);
    }

    @Override
    public Map<String, Object> selectByGroupIdAndCompanyIdRoleName(String groupId, String companyId, String roleName, String status, String area, int page, int per_page) {
        Map<String, Object> result = new HashMap<>();
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(groupId)) {
            c.and(Dict.GROUPID).is(groupId);
        }
        if (StringUtils.isNotBlank(companyId)) {
            c.and(Dict.COMPANYID).is(companyId);
        }
        if (StringUtils.isNotBlank(roleName)) {
            c.and(Dict.ROLENAME).regex(roleName);
        }
        if (StringUtils.isNotBlank(status)) {
            c.and(Dict.STATUS).is(status);
        }
        if (StringUtils.isNotBlank(area)) {
            c.and(Dict.AREANAME).is(area);
        }
        Query query = new Query(c);
        Long total = mongoTemplate.count(query, GitlabUser.class);
        result.put(Dict.TOTAL, total);
        query.skip((page-1)*per_page).limit(per_page);
        List<GitlabUser> userlist =mongoTemplate.find(query, GitlabUser.class);
        result.put("dbs", userlist);
        return result;
    }

    /**
     * 把所有状态值为source的应用，状态修改为target
     *
     * @param source
     * @param target
     */
    @Override
    public void updateSign(int source, int target) {
        Query query = new Query(Criteria.where(Dict.SIGN).is(source));
        Update update = new Update();
        update.set(Dict.SIGN, target);
        mongoTemplate.updateMulti(query, update, GitlabUser.class);
    }

    @Override
    public List<Map> selectArea() {
        List<GitlabUser> UserList = mongoTemplate.findAll(GitlabUser.class);
        List<Map> UserArea = new ArrayList<>();
        StringBuffer arealist = new StringBuffer();
        for (GitlabUser gitlabUser : UserList) {
            if (!Util.isNullOrEmpty(gitlabUser.getAreaname())
                    && !arealist.toString().contains(gitlabUser.getAreaname())) {
                arealist.append(gitlabUser.getAreaname());
                Map map = new HashMap();
                map.put(Dict.AREAID, gitlabUser.getAreaid());
                map.put(Dict.AREANAME, gitlabUser.getAreaname());
                UserArea.add(map);
            }
        }
        return UserArea;
    }

    @Override
    public List<GitlabUser> selectGitlabUserByParams(GitlabUser gitlabUser,List groupIdList) {
        Criteria c = new Criteria();
        if (!Util.isNullOrEmpty(gitlabUser.getUser_id())) {
            c.and(Dict.USER_ID).is(gitlabUser.getUser_id());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getName())) {
            c.and(Dict.NAME).is(gitlabUser.getName());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getUsername())) {
            c.and("username").is(gitlabUser.getUsername());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getGroupid())) {
            c.and(Dict.GROUPID).in(groupIdList);
        }
        if (!Util.isNullOrEmpty(gitlabUser.getGroupname())) {
            c.and(Dict.GROUPNAME).is(gitlabUser.getGroupname());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getGituser())) {
            c.and(Dict.GITUSER).is(gitlabUser.getGituser());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getCompanyid())) {
            c.and(Dict.COMPANYID).is(gitlabUser.getCompanyid());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getCompanyname())) {
            c.and(Dict.COMPANYNAME).is(gitlabUser.getGroupname());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getRolename())) {
            c.and(Dict.ROLENAME).is(gitlabUser.getRolename());
        }
        if (!Util.isNullOrEmpty(gitlabUser.getStatus())) {
            c.and(Dict.STATUS).is(gitlabUser.getStatus());
        }
        Query query = new Query(c);
        return this.mongoTemplate.find(query, GitlabUser.class);
    }


}

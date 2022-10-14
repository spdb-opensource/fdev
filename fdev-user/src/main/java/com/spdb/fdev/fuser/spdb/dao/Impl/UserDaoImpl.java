package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.cache.RedisCache;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.base.utils.EntityUtils;
import com.spdb.fdev.fuser.spdb.dao.LabelDao;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.dto.UserListInGroupPage;
import com.spdb.fdev.fuser.spdb.entity.user.*;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class UserDaoImpl implements UserDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RoleDao roleDao;

    @Resource
    private LabelDao labelDao;

    @Override
    public User addUser(User user) {
        user.initId();
        User rep = mongoTemplate.save(user);
        flushUserCache();
        return rep;
    }

    @Override
    public long delUserByNameEn(String nameEn) {
        flushUserCache();
        Query q = Query.query(Criteria.where(Dict.USER_NAME_EN).is(nameEn));
        return mongoTemplate.remove(q, User.class).getDeletedCount();
    }

    @Override
    public User updateUser(User user) throws Exception {
        flushUserCache();
        Query q = Query.query(Criteria.where(Dict.USER_NAME_EN).is(user.getUser_name_en()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(user);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update up = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.ID.equals(key) || Dict.OBJECTID.equals(key)
                    || Dict.IS_KFAPPROVAL.equals(key) || Dict.IS_VMAPPROVAL.equals(key) || Dict.EMAIL.equals(key)) {
                continue;
            }
            up.set(key, value);
        }
        if (CommonUtils.isNullOrEmpty(user.getLabels())) {
            up.set(Dict.LABELS, new ArrayList<String>());
        }
        if (CommonUtils.isNullOrEmpty(user.getEducation())) {
            up.set(Dict.EDUCATION, "");
        }
        if (CommonUtils.isNullOrEmpty(user.getLeave_date())) {
            up.set(Dict.LEAVE_DATE, "");
        }
        if (CommonUtils.isNullOrEmpty(user.getRemark())) {
            up.set(Dict.REMARK, "");
        }
        if (CommonUtils.isNullOrEmpty(user.getRank_id())) {
            up.set(Dict.RANK_ID, "");
        }
        if (CommonUtils.isNullOrEmpty(user.getVm_user_name())) {
            up.set(Dict.VM_USER_NAME, "");
        }
        if (CommonUtils.isNullOrEmpty(user.getVm_ip())) {
            up.set(Dict.VM_IP, "");
        }
        if (!CommonUtils.isNullOrEmpty(user.getIs_spdb())) {
            if (!user.getIs_spdb()) {
                if (CommonUtils.isNullOrEmpty(user.getVm_name()))
                    up.set(Dict.VM_NAME, "");
            }
        }
        if (CommonUtils.isNullOrEmpty(user.getPhone_mac()))
            up.set(Dict.PHONE_MAC, "");
        if (CommonUtils.isNullOrEmpty(user.getPhone_type()))
            up.set(Dict.PHONE_TYPE, "");

        mongoTemplate.findAndModify(q, up, User.class);
        return mongoTemplate.findOne(q, User.class);
    }

    @Override
    public List<Map> getUser(User user) throws Exception {
        List<Map> result = new ArrayList<>();
        AggregationOperation grouplookup = Aggregation.lookup(Dict.GROUP, Dict.GROUP_ID, Dict.ID, Dict.GROUP);
        AggregationOperation rolelookup = Aggregation.lookup(Dict.ROLE, Dict.ROLE_ID, Dict.ID, Dict.ROLE);
        AggregationOperation companylookup = Aggregation.lookup(Dict.COMPANY, Dict.COMPANY_ID, Dict.ID, Dict.COMPANY);
        AggregationOperation labellookup = Aggregation.lookup(Dict.LABEL, Dict.LABELS, Dict.ID, Dict.USER_LABEL);
        AggregationOperation arealookup = Aggregation.lookup(Dict.AREA, Dict.AREA_ID, Dict.ID, Dict.AREA);
        AggregationOperation functionlookup = Aggregation.lookup(Dict.FUNCTION, Dict.FUNCTION_ID, Dict.ID, Dict.FUNCTION);
        AggregationOperation ranklookup = Aggregation.lookup(Dict.RANK, Dict.RANK_ID, Dict.ID, Dict.RANK);
        LookupOperation sectionLookup = Aggregation.lookup(Dict.SECTION, Dict.SECTION, Dict.ID, Dict.SECTIONINFO);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(user);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) continue;
            if (Dict.LABELS.equals(key)) {
                c.and(key).all(user.getLabels());
            } else if (Dict.ROLE_ID.equals(key)) {
                c.and(key).all(user.getRole_id());
            } else {
                c.and(key).is(value);
            }
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(grouplookup, rolelookup, companylookup, labellookup, arealookup, functionlookup, ranklookup,sectionLookup, match), Dict.USER, Map.class);
        Iterator<Map> iterator = docs.iterator();
        while (iterator.hasNext()) {
            Map next = iterator.next();
            Iterator iterator1 = next.keySet().iterator();
            while (iterator1.hasNext()) {
                String key = (String) iterator1.next();
                if (Dict.COMPANY.equals(key) || Dict.GROUP.equals(key) || Dict.AREA.equals(key) || Dict.FUNCTION.equals(key) || Dict.RANK.equals(key) || Dict.SECTIONINFO.equals(key)) {
                    List<Map> klist = (List<Map>) next.get(key);
                    next.replace(key, (klist == null || klist.size() == 0) ? null : klist.get(0));
                }
            }
            result.add(next);
        }
        return result;
    }

    @Override
    public List<Map> queryAllUser() {
        List<Map> result = new ArrayList<>();
        AggregationOperation grouplookup = Aggregation.lookup(Dict.GROUP, Dict.GROUP_ID, Dict.ID, Dict.GROUP);
        AggregationOperation rolelookup = Aggregation.lookup(Dict.ROLE, Dict.ROLE_ID, Dict.ID, Dict.ROLE);
        AggregationOperation companylookup = Aggregation.lookup(Dict.COMPANY, Dict.COMPANY_ID, Dict.ID, Dict.COMPANY);
        AggregationOperation labellookup = Aggregation.lookup(Dict.LABEL, Dict.LABELS, Dict.ID, Dict.USER_LABEL);
        AggregationOperation arealookup = Aggregation.lookup(Dict.AREA, Dict.AREA_ID, Dict.ID, Dict.AREA);
        AggregationOperation functionlookup = Aggregation.lookup(Dict.FUNCTION, Dict.FUNCTION_ID, Dict.ID, Dict.FUNCTION);
        AggregationOperation ranklookup = Aggregation.lookup(Dict.RANK, Dict.RANK_ID, Dict.ID, Dict.RANK);
        LookupOperation sectionLookup = Aggregation.lookup(Dict.SECTION, Dict.SECTION, Dict.ID, Dict.SECTIONINFO);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(grouplookup, rolelookup, companylookup, labellookup, arealookup, functionlookup, ranklookup, sectionLookup), Dict.USER, Map.class);
        docs.forEach(doc -> {
            if(!CommonUtils.isNullOrEmpty(doc.get("group"))){
                doc.put("group", ((List<Group>) doc.get("group")).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get("company"))){
                doc.put("company", ((List<Company>) doc.get("company")).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get("role")))
                doc.put("role", ((List<Role>) doc.get("role")));
             if(!CommonUtils.isNullOrEmpty(doc.get(Dict.LABEL)))
                doc.put(Dict.LABEL, ((List<Label>) doc.get(Dict.LABEL)));
             //去除Objectid，防止redis获取数据是找不到对应Objectid的属性
            if(!CommonUtils.isNullOrEmpty(doc.get("area")) && ((List) doc.get("area")).size()> 0)
                doc.put("area", EntityUtils.removeObjectid(((List) doc.get("area")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get("function")) && ((List) doc.get("function")).size()> 0)
                doc.put("function", EntityUtils.removeObjectid(((List) doc.get("function")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get("rank")) && ((List) doc.get("rank")).size()> 0)
                doc.put("rank", EntityUtils.removeObjectid(((List) doc.get("rank")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.SECTIONINFO)) && ((List) doc.get(Dict.SECTIONINFO)).size()> 0)
                doc.put(Dict.SECTIONINFO, EntityUtils.removeObjectid(((List) doc.get(Dict.SECTIONINFO)).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.OBJECTID))){
                doc.remove(Dict.OBJECTID);
            }
            result.add(doc);
        });
        return result;
    }

    @Override
    public List<Map> queryAllUserName() {
        List<Map> result = new ArrayList<>();
        AggregationOperation grouplookup = Aggregation.lookup(Dict.GROUP, Dict.GROUP_ID, Dict.ID, Dict.GROUP);
        AggregationOperation rolelookup = Aggregation.lookup(Dict.ROLE, Dict.ROLE_ID, Dict.ID, Dict.ROLE);
        AggregationOperation companylookup = Aggregation.lookup(Dict.COMPANY, Dict.COMPANY_ID, Dict.ID, Dict.COMPANY);
        AggregationOperation labellookup = Aggregation.lookup(Dict.LABEL, Dict.LABELS, Dict.ID, Dict.USER_LABEL);
        AggregationOperation arealookup = Aggregation.lookup(Dict.AREA, Dict.AREA_ID, Dict.ID, Dict.AREA);
        AggregationOperation functionlookup = Aggregation.lookup(Dict.FUNCTION, Dict.FUNCTION_ID, Dict.ID, Dict.FUNCTION);
        AggregationOperation ranklookup = Aggregation.lookup(Dict.RANK, Dict.RANK_ID, Dict.ID, Dict.RANK);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(grouplookup, rolelookup, companylookup, labellookup, arealookup, functionlookup, ranklookup), Dict.USER, Map.class);
        docs.forEach(doc -> {
            if(!CommonUtils.isNullOrEmpty(doc.get("group"))){
                doc.put("group", ((List<Group>) doc.get("group")).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get("company"))) {
                doc.put("company", ((List<Company>) doc.get("company")).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get("role")))
                doc.put("role", ((List<Role>) doc.get("role")));
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.LABEL)))
                doc.put(Dict.LABEL, ((List<Label>) doc.get(Dict.LABEL)));
            //去除Objectid，防止redis获取数据是找不到对应Objectid的属性
            if(!CommonUtils.isNullOrEmpty(doc.get("area")) && ((List) doc.get("area")).size()> 0)
                doc.put("area", EntityUtils.removeObjectid(((List) doc.get("area")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get("function")) && ((List) doc.get("function")).size()> 0)
                doc.put("function", EntityUtils.removeObjectid(((List) doc.get("function")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get("rank")) && ((List) doc.get("rank")).size()> 0)
                doc.put("rank", EntityUtils.removeObjectid(((List) doc.get("rank")).get(0)));
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.OBJECTID))){
                doc.remove(Dict.OBJECTID);
            }
            result.add(doc);
        });
        return result;
    }

    @Override
    public Map updateGitToken(String userNameEn, String gitToken) throws Exception {
        Query query = new Query(Criteria.where(Dict.USER_NAME_EN).is(userNameEn));
        Update update = Update.update(Dict.GIT_TOKEN, gitToken);
        User user = mongoTemplate.findAndModify(query, update, User.class);
        return this.getUser(user).get(0);
    }

    @Override
    public List<User> getUserByRole(String roleId) {
        Query query = new Query(Criteria.where(Dict.STATUS).is("0").and(Dict.ROLE_ID).is(roleId));
        return mongoTemplate.find(query,User.class);
    }

    @Override
    public long queryInJobUserNum(String groupId) {
        Query query = new Query(Criteria.where(Dict.GROUP_ID).is(groupId).and(Dict.STATUS).is("0"));
        return mongoTemplate.count(query,User.class);
    }

    @Override
    public Map getUsersInGroup(List<String> iAndChildGroupIds, int pageSize, int index) {
        Map result = new HashMap();
        AggregationOperation grouplookup = Aggregation.lookup(Dict.GROUP, Dict.GROUP_ID, Dict.ID, Dict.GROUP);
        Criteria criteria = Criteria.where(Dict.GROUP_ID).in(iAndChildGroupIds).and(Dict.STATUS).is("0");
        MatchOperation match = Aggregation.match(criteria);
        //设置返回字段
        ProjectionOperation project = Aggregation.project(Dict.ID, Dict.USER_NAME_EN, Dict.USER_NAME_CN, Dict.EMAIL,"group.name",Dict.GROUP_ID, Dict.TELEPHONE);
        Aggregation aggregation = Aggregation.newAggregation(grouplookup, match,project);
        Query query = new Query(criteria);
        if(!CommonUtils.isNullOrEmpty(pageSize) && pageSize > 0){
            index = index > 0 ? (index - 1)*pageSize : 0;
            query.skip(index).limit(pageSize);
            aggregation = Aggregation.newAggregation(grouplookup,match,project,Aggregation.skip((long)index),Aggregation.limit(pageSize));
        }
        long count = mongoTemplate.count(query, Dict.USER);
        AggregationResults<UserListInGroupPage> docs = mongoTemplate.aggregate(aggregation, Dict.USER, UserListInGroupPage.class);
        result.put("count",count);
        result.put("data",docs.getMappedResults());
        return result;
    }

    @Override
    public List<User> getAllUserInUse() {
        Query query = new Query(Criteria.where(Dict.STATUS).is(Constants.USER_STATUS_IN));
        return mongoTemplate.find(query,User.class);
    }

    /**
     * 批量更新用户信息为离职
     * @param needUpdateUsers
     */
    @Override
    public void updateUsersToLeave(Set<String> needUpdateUsers) {
        Query query = new Query(Criteria.where(Dict.ID).in(needUpdateUsers));
        Update update = Update.update(Dict.STATUS,"1").set(Dict.LEAVE_DATE,CommonUtils.formatDate(CommonUtils.DATE_PARSE));
        mongoTemplate.updateMulti(query,update,User.class);
    }

    @Override
    public long getUserCount(User u) throws JsonProcessingException {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = u==null?"{}":mapper.writeValueAsString(u);
        Query query =new BasicQuery(json);
        return mongoTemplate.count(query,User.class);
    }

    @Override
    public List<User> getUsersInfoByIds(List<String> ids) {
        Query query = Query.query(Criteria.where(Dict.ID).in(ids));
        return mongoTemplate.find(query,User.class);
    }

    @Override
    public List<Map> getAllUserAndRole(Map requestMap) {
        List<Map> result = new ArrayList<>();
        AggregationOperation rolelookup = Aggregation.lookup(Dict.ROLE, Dict.ROLE_ID, Dict.ID, Dict.ROLE);
        ProjectionOperation project = Aggregation.project(Dict.ID, Dict.USER_NAME_EN, Dict.USER_NAME_CN, Dict.STATUS,"role.name");

        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(requestMap.get(Dict.STATUS))){
            criteria.and(Dict.STATUS).is(requestMap.get(Dict.STATUS));
        }
        MatchOperation match = Aggregation.match(criteria);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(rolelookup,match,project), Dict.USER, Map.class);
        docs.forEach(doc -> {
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.NAME))){
                doc.put(Dict.ROLE, doc.get(Dict.NAME));
                doc.remove(Dict.NAME);
            }
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.OBJECTID))){
                doc.remove(Dict.OBJECTID);
            }
            result.add(doc);
        });
        return result;
    }

    @Override
    public List<Map> getUserCoreData(User user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(user);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while(it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(Dict.OBJECTID.equals(key))continue;
            if(Dict.LABELS.equals(key)) {
                c.and(key).all(user.getLabels());
            }else if(Dict.ROLE_ID.equals(key)){
                c.and(key).all(user.getRole_id());
            } else {
                c.and(key).is(value);
            }
        }
        List<Map> result = new ArrayList<>();
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match), Dict.USER, Map.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fuser.allcoreuser")
    public List<Map> getAllUserCoreData() {
        List<Map> userlist = mongoTemplate.findAll(Map.class, Dict.USER);
        //去除Objectid，防止redis获取数据是找不到对应Objectid的属性
        for(Map map : userlist){
            map.remove(Dict.OBJECTID);
        }
        return userlist;
    }


    public  List<Map> getUserByName(String nameCn) throws Exception{
        List<Map> list = new ArrayList<>();
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + nameCn + ".*$", Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where(Dict.USER_NAME_CN).regex(pattern));
        List<User> lists = mongoTemplate.find(query, User.class);
        if (lists != null && lists.size() > 0) {
            for (User user : lists) {
                list.add(getUser(user).get(0));
            }
            return list;
        }
        return null;
    }

    /**
     * 根据 环境+"*"+key+"*" 来清除缓存
     *
     * @param key
     */
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void removeCache(String key) {
    }

    private void flushUserCache() {
        redisCache.removeCache("fuser.alluser");
        redisCache.removeCache("fuser.allcoreuser");
    }

	@Override
	public void updateGitUser(String user_name_en, String gitlabUsername) throws Exception{
		Query query = Query.query(Criteria.where(Dict.USER_NAME_EN).is(user_name_en));
		Update update = Update.update(Dict.GIT_USER, gitlabUsername);
		mongoTemplate.findAndModify(query, update, User.class);
	}

    @Override
    public void updatePassword(String user_name_en, String password) throws Exception {
        flushUserCache();
        Query query = Query.query(Criteria.where(Dict.USER_NAME_EN).is(user_name_en));
        Update update = Update.update(Dict.PASSWORD, password);
        mongoTemplate.findAndModify(query, update, User.class);
    }

    @Override
	public User getIsJobUser(String name_en, String status) throws Exception {
		Query query = Query.query(Criteria.where(Dict.USER_NAME_EN).is(name_en).and(Dict.STATUS).is(status));
		return mongoTemplate.findOne(query, User.class);
	}

    @Override
    public List queryArea(String area_id) throws Exception{
        if(CommonUtils.isNullOrEmpty(area_id)){
            return mongoTemplate.findAll(Map.class, "area");
        }
        Map map = mongoTemplate.findById(area_id, Map.class,"area");
        List result = new ArrayList<>();
        result.add(map);
        return result;
    }

    @Override
    public List queryPost(String post_id) throws Exception{
        if(CommonUtils.isNullOrEmpty(post_id)){
            return mongoTemplate.findAll(Map.class, "post");
        }
        Map map = mongoTemplate.findById(post_id, Map.class,"post");
        List result = new ArrayList<>();
        result.add(map);
        return result;
    }

    @Override
    public List<User> queryUserStatis(List<String> queryRoleids, String s) {
        Query query = Query.query(Criteria.where(Dict.ROLE_ID).in(queryRoleids)
                .and(Dict.STATUS).is(s));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<User> queryRemark(String s, String id, List<String> queryRoleids) {
        Query query = Query.query(new Criteria().orOperator(Criteria.where(Dict.ROLE_ID).nin(queryRoleids),Criteria.where(Dict.LABELS).in(id)));
        query.addCriteria(Criteria.where(Dict.STATUS).is(s));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List queryfunction(String function_id) {
        if(CommonUtils.isNullOrEmpty(function_id)){
            return mongoTemplate.findAll(Map.class, "function");
        }
        Map map = mongoTemplate.findById(function_id, Map.class,"function");
        List result = new ArrayList<>();
        result.add(map);
        return result;
    }

    @Override
    public List queryrank(String rank_id) {
        if(CommonUtils.isNullOrEmpty(rank_id)){
            return mongoTemplate.findAll(Map.class, "rank");
        }
        Map map = mongoTemplate.findById(rank_id, Map.class,"rank");
        List result = new ArrayList<>();
        result.add(map);
        return result;
    }

    @Override
    public  List<User> queryUsersByGroups(List<String> groupids) {
        Query query = Query.query(Criteria.where(Dict.STATUS).is("0").and(Dict.GROUP_ID).in(groupids));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<User> queryUserByCompanyGroup(String status, String company_id, Set<String> child_group_ids) {
        Query query = new Query(Criteria.where(Dict.STATUS).is(status).and(Dict.COMPANY_ID).is(company_id)
                .and(Dict.GROUP_ID).in(child_group_ids));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public Map<String, Object> queryUserBySearch(List<String> search, String companyId, String groupId, String status, int page, int per_page, String labelId,String is_party_member, String  area_id, String function_id, String section) throws Exception {
        Map<String, Object> maplist = new HashMap<>();
        List<Map> result = new ArrayList<>();
        AggregationOperation grouplookup = Aggregation.lookup(Dict.GROUP, Dict.GROUP_ID, Dict.ID, Dict.GROUP);
        AggregationOperation rolelookup = Aggregation.lookup(Dict.ROLE, Dict.ROLE_ID, Dict.ID, Dict.ROLE);
        AggregationOperation companylookup = Aggregation.lookup(Dict.COMPANY, Dict.COMPANY_ID, Dict.ID, Dict.COMPANY);
        AggregationOperation labellookup = Aggregation.lookup(Dict.LABEL, Dict.LABELS, Dict.ID, Dict.USER_LABEL);
        AggregationOperation arealookup = Aggregation.lookup(Dict.AREA, Dict.AREA_ID, Dict.ID, Dict.AREA);
        AggregationOperation functionlookup = Aggregation.lookup(Dict.FUNCTION, Dict.FUNCTION_ID, Dict.ID, Dict.FUNCTION);
        AggregationOperation ranklookup = Aggregation.lookup(Dict.RANK, Dict.RANK_ID, Dict.ID, Dict.RANK);
        AggregationOperation sectionlookup = Aggregation.lookup(Dict.SECTION, Dict.SECTION, Dict.ID, "sectionInfo");
        //条件查询
        Criteria c = new Criteria();
        if(!CommonUtils.isNullOrEmpty(companyId)){
            c.and(Dict.COMPANY_ID).is(companyId);
        }
        if(!CommonUtils.isNullOrEmpty(groupId)){
            c.and(Dict.GROUP_ID).is(groupId);
        }
        if(!CommonUtils.isNullOrEmpty(status)){
            c.and(Dict.STATUS).is(status);
        }
        if(!CommonUtils.isNullOrEmpty(labelId)){
            c.and(Dict.LABELS).in(labelId);
        }
        if(!CommonUtils.isNullOrEmpty(area_id)){
            c.and(Dict.AREA_ID).is(area_id);
        }
        if(!CommonUtils.isNullOrEmpty(is_party_member)){
            c.and(Dict.IS_PARTY_MEMBER).is(is_party_member);
        }
        if(!CommonUtils.isNullOrEmpty(function_id)){
            c.and(Dict.FUNCTION_ID).is(function_id);
        }
        if(!CommonUtils.isNullOrEmpty(section)){
            c.and(Dict.SECTION).is(section);
        }
        //模糊查询
        if(!CommonUtils.isNullOrEmpty(search) && search.size() > 0){
            Role role = new Role();
            role.setStatus("1");
            List<Role> roles = roleDao.queryRole(role);
            List<Criteria> orCriterias = new ArrayList<>();
            List<Criteria> andCriterias = new ArrayList<>();
            ArrayList<String> lableAll = new ArrayList<>();
            for(String str : search){
                List<Label> labelsInfo = labelDao.queryLableByName(str);
                for (Label label : labelsInfo) {
                    String id = label.getId();
                    lableAll.add(id);
                }
                List<String> results =new ArrayList();
                for (Role role1 : roles) {
                    Pattern pattern = Pattern.compile(str);
                    Matcher matcher = pattern.matcher(role1.getName());
                    if (matcher.find()){
                        results.add(role1.getId());
                    }
                }
                andCriterias.add(Criteria.where(Dict.ROLE_ID).in(results));
                orCriterias.add(Criteria.where(Dict.USER_NAME_EN).regex(str));
                orCriterias.add(Criteria.where(Dict.USER_NAME_CN).regex(str));
                orCriterias.add(Criteria.where(Dict.LABELS).in(lableAll));
            }
            Criteria criteria = new Criteria();
            orCriterias.add(criteria.andOperator(andCriterias.toArray(new Criteria[0])));

            c.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        Query query = Query.query(c);
        Long total = mongoTemplate.count(query, User.class);
        maplist.put(Dict.TOTAL, total);

        AggregationOperation match = Aggregation.match(c);
        Aggregation aggregation;
        //limit上送long类型数据，per_page不能为0
        if(per_page == 0){
            aggregation = Aggregation.newAggregation(
                    grouplookup, rolelookup, companylookup, labellookup, arealookup, functionlookup, ranklookup,sectionlookup, match
            );
        } else {
             aggregation = Aggregation.newAggregation(
                    grouplookup, rolelookup, companylookup, labellookup, arealookup, functionlookup, ranklookup,sectionlookup, match ,
                    Aggregation.skip(page>1 ? (page - 1) * per_page : 0),
                    Aggregation.limit(per_page)
            );
        }
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.USER, Map.class);
        docs.forEach(doc -> {
            if (CommonUtils.isNullOrEmpty(doc.get(Dict.AREA)))
                doc.remove(Dict.AREA_ID);
            if (CommonUtils.isNullOrEmpty(doc.get(Dict.SECTIONINFO))){
                doc.remove(Dict.SECTIONINFO);
            }else {
                doc.put(Dict.SECTIONINFO,((List) doc.get(Dict.SECTIONINFO)).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.GROUP))){
                doc.put(Dict.GROUP, ((List) doc.get(Dict.GROUP)).get(0));
            }
            if(!CommonUtils.isNullOrEmpty(doc.get(Dict.COMPANY))){
                doc.put(Dict.COMPANY, ((List) doc.get(Dict.COMPANY)).get(0));
            }
            result.add(doc);
        });
        maplist.put("list", result);
        return maplist;
    }

    @Override
    public List<User> queryProGroup(List<String> groups, List<String> roleIds, String labId) {
        Query query = new Query(Criteria.where(Dict.GROUP_ID).in(groups).and(Dict.STATUS).is("0")
                .and(Dict.LABELS).nin(labId).and(Dict.ROLE_ID).in(roleIds));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<User> queryNoProGroup(List<String> groups, List<String> roleIds, String labId) {
        Query query = new Query(Criteria.where(Dict.GROUP_ID).in(groups).and(Dict.STATUS).is("0"));
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.ROLE_ID).nin(roleIds),
                Criteria.where(Dict.LABELS).in(labId)));
        return mongoTemplate.find(query, User.class);
    }

    public User updateUserFtms(User user) {
        Query query = new Query(Criteria.where(Dict.ID).is(user.getId()));
        List<String> roleId = user.getRole_id();
        String ftmsLevel = user.getFtms_level();
        String mantisToken = user.getMantis_token();
        Update up=Update.update(Dict.ID,user.getId());
        if(!CommonUtils.isNullOrEmpty(roleId)){
            up.set(Dict.ROLE_ID,roleId);
        }
        if(!CommonUtils.isNullOrEmpty(ftmsLevel)){
            up.set(Dict.FTMS_LEVEL,ftmsLevel);
        }
        if(!CommonUtils.isNullOrEmpty(mantisToken)){
            up.set(Dict.MANTIS_TOKEN,mantisToken);
        }
        this.mongoTemplate.findAndModify(query,up,User.class);
        Query query2 = new Query(Criteria.where(Dict.ID).is(user.getId()));
        return mongoTemplate.findOne(query2,User.class);
    }

    @Override
    public Map queryUserNumBygroup(List<String> groupIds, String devId, String testId, String pufaId) {
        Map<String, Object> result = new HashMap<>();
        //用户总人数
        Query query = new Query(Criteria.where(Dict.GROUP_ID).in(groupIds).and(Dict.STATUS).is("0"));
        Long total = mongoTemplate.count(query, User.class);

        Query query1 = new Query(Criteria.where(Dict.GROUP_ID).in(groupIds).and(Dict.ROLE_ID).in(devId).and(Dict.COMPANY_ID).is(pufaId).and(Dict.STATUS).is("0"));
        Long indevNum = mongoTemplate.count(query1, User.class);

        Query query2 = new Query(Criteria.where(Dict.GROUP_ID).in(groupIds).and(Dict.ROLE_ID).in(testId).and(Dict.COMPANY_ID).is(pufaId).and(Dict.STATUS).is("0"));
        Long intestNum = mongoTemplate.count(query2, User.class);

        Query query3 = new Query(Criteria.where(Dict.GROUP_ID).in(groupIds).and(Dict.ROLE_ID).in(devId).and(Dict.COMPANY_ID).ne(pufaId).and(Dict.STATUS).is("0"));
        Long outdevNum = mongoTemplate.count(query3, User.class);

        Query query4 = new Query(Criteria.where(Dict.GROUP_ID).in(groupIds).and(Dict.ROLE_ID).in(testId).and(Dict.COMPANY_ID).ne(pufaId).and(Dict.STATUS).is("0"));
        Long outtestNum = mongoTemplate.count(query4, User.class);

        result.put("userCount",total);
        result.put("inDevNum",indevNum);
        result.put("inTestNum",intestNum);
        result.put("outDevNum",outdevNum);
        result.put("outTestNum",outtestNum);
        return result;
    }

    @Override
    public List<Map> queryEmailByUserIds(List<String> ids) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).in(ids);
        Query query = new Query(criteria);
        List<User> users = this.mongoTemplate.find(query, User.class, Dict.USER);
        List emailList = new ArrayList();
        users.forEach(e -> {
            Map email = new HashMap();
            email.put(e.getId(), e.getEmail());
            emailList.add(email);
        });
        return emailList;
    }
}

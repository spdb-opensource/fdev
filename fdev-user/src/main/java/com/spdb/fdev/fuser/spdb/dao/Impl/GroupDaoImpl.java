package com.spdb.fdev.fuser.spdb.dao.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.GroupDao;
import com.spdb.fdev.fuser.spdb.entity.user.Group;


@Repository
public class GroupDaoImpl implements GroupDao {


	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public Group addGroup(Group group) throws Exception {
		group.initId();
		group.setCount(0);
		group.setStatus("1");
		return mongoTemplate.save(group);
	}

	@Override
	public List<Group> queryGroup(Group group) throws Exception {
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = group==null?"{}":mapper.writeValueAsString(group);
		Query query =new BasicQuery(json);
		return mongoTemplate.find(query,Group.class);
	}

	@Override
	public Group updateGroup(Group group) {
		Query query=Query.query(Criteria.where(Dict.ID).is(group.getId()));
		Update update = Update.update(Dict.NAME, group.getName());
//		if(!CommonUtils.isNullOrEmpty(group.getLevel())){
//			update.set(Dict.LEVEL,group.getLevel());
//		}
//		update.set(Dict.SORTNUM,group.getSortNum());
		mongoTemplate.findAndModify(query, update, Group.class);
		return mongoTemplate.findOne(query, Group.class);
	}

	@Override
	public Group deleteGroup(Group group) {
		Query query = Query.query(Criteria.where(Dict.ID).is(group.getId()));
		Update update = Update.update(Dict.STATUS, "0");
		return mongoTemplate.findAndModify(query, update, Group.class);
	}

	@Override
	public Group queryDetailById(String groupId) {
		Query query = Query.query(Criteria.where(Dict.ID).is(groupId));
		return mongoTemplate.findOne(query, Group.class);
	}

	@Override
	public List<String> queryGroupByNames(String testcentre, String businessdept, String plandept) {
		Query query = Query.query(new Criteria().orOperator(Criteria.where(Dict.NAME).is(testcentre), Criteria.where(Dict.NAME).is(businessdept), Criteria.where(Dict.NAME).is(plandept)));
		List<Group>	grouplist = mongoTemplate.find(query, Group.class);
		List<String> groupids = new ArrayList<>();
		for(Group group : grouplist){
			groupids.add(group.getId());
		}
		return groupids;
	}

    @Override
    public List<Group> queryFirstGroup() {
        Query query = Query.query(new Criteria().orOperator(Criteria.where(Dict.PARENT_ID).exists(false),Criteria.where(Dict.PARENT_ID).is("")));
        return mongoTemplate.find(query, Group.class);
    }

    @Override
    public List<Group> queryGroupByIds(List<String> groupIds, boolean allFlag) {
		Query query = new Query();
		if(allFlag){
			query.addCriteria(Criteria.where(Dict.ID).in(groupIds));
		}else {
			query.addCriteria(Criteria.where(Dict.ID).in(groupIds).and(Dict.STATUS).is("1"));
		}
        return mongoTemplate.find(query, Group.class);
    }

	@Override
	public List queryGroupFullNameByIds(List<String> groupIds) {
		List resultList = new ArrayList();
		for (String groupId : groupIds) {
			Map<String, Object> result = new HashMap<>();
			List<Group> groups = new ArrayList<>();
			groups = loopFindParentGroup(groupId, groups, true);
			result.put("list", groups);
			result.put(Dict.ID, groupId);
			resultList.add(result);
		}
		return resultList;
	}

	@Override
	public Group queryGroupById(String groupId) {
		Query query = Query.query(Criteria.where(Dict.ID).is(groupId));
		return mongoTemplate.findOne(query,Group.class);
	}

	@Override
	public List<Group> queryAllGroup() {
		return mongoTemplate.findAll(Group.class);
	}

	@Override
	public void addGroupUsers(List<String> userIds, String groupId) {
		Query query = new Query(Criteria.where(Dict.ID).in(userIds));
		Update update = Update.update(Dict.GROUP_ID,groupId);
		mongoTemplate.updateMulti(query,update,User.class);
	}

	@Override
	public List<Group> queryGroupBySortNum(String sortNum) {
		Query query = new Query(Criteria.where(Dict.SORTNUM).regex("^"+sortNum).and(Dict.STATUS).is("1"));
		return mongoTemplate.find(query, Group.class);
	}

	/**
	 * 返回所有使用中的一级组
	 * @return
	 */
	@Override
	public List<Group> queryFirstGroupMap(boolean delFlag) {
		Query query;
		if(delFlag){
			query = Query.query(new Criteria().orOperator(Criteria.where(Dict.PARENT_ID).exists(false),Criteria.where(Dict.PARENT_ID).is("")));
		}else {
			query = Query.query(new Criteria().orOperator(Criteria.where(Dict.PARENT_ID).exists(false).and(Dict.STATUS).is("1"),Criteria.where(Dict.PARENT_ID).is("").and(Dict.STATUS).is("1")));
		}
		query.with(Sort.by(Sort.Order.asc(Dict.NAME)));
		return mongoTemplate.find(query, Group.class);
	}

	/**
	 * 根据父组id查询使用中的子组
	 * @param parentId
	 * @return
	 */
	@Override
	public List<Group> queryChild(String parentId, boolean delFlag) {
		Query query;
		if(delFlag){
			query = Query.query(new Criteria().and(Dict.PARENT_ID).is(parentId));
		}else {
			query = Query.query(new Criteria().and(Dict.PARENT_ID).is(parentId).and(Dict.STATUS).is("1"));
		}
		query.with(Sort.by(Sort.Order.asc(Dict.NAME)));
		return mongoTemplate.find(query, Group.class);
	}

	/**
	 * 循环组装fullName
	 *
	 * @param groupId
	 * @param resultOneGroup
	 * @param isFirst
	 * @return
	 */
	private List<Group> loopFindParentGroup(String groupId, List<Group> resultOneGroup, Boolean isFirst) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.ID).is(groupId).and(Dict.STATUS).is(Dict.ONE);
		Query query = new Query(criteria);
		Group group = this.mongoTemplate.findOne(query, Group.class, Dict.GROUP);
		if (!isFirst) {
			//如果不是第一次查，拿当前list的最后一个name加上查出来的组名
			Group lastGroup = resultOneGroup.get(resultOneGroup.size() - 1);
			String lastGroupName = lastGroup.getFullName();
			String currentName = group.getName();
			group.setFullName(currentName + "-" + lastGroupName);
		}else {
			//是第一个
			group.setFullName(group.getName());
		}
		String parentId = group.getParent_id();
		resultOneGroup.add(group);
		if (!CommonUtils.isNullOrEmpty(parentId)) {
			resultOneGroup = loopFindParentGroup(parentId, resultOneGroup, false);
		}
		return resultOneGroup;
	}
}

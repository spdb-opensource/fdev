package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class RoleDaoImpl implements RoleDao {
	
	@Resource
	private MongoTemplate mongoTemplate;
	

	@Override
	public Role addRole(Role role) {
		role.initId();
		role.setStatus("1");
		return mongoTemplate.save(role);
	}

	@Override
	public Role delRoleByID(String id) {
		Query query = Query.query(Criteria.where(Dict.ID).is(id));
		Update update = Update.update(Dict.STATUS, "0");
		return mongoTemplate.findAndModify(query, update,  Role.class);
	}

	@Override
	public List<Role> queryRole(Role role) throws Exception {
		List<Role> result = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = role==null?"{}":objectMapper.writeValueAsString(role);
		Query query =new BasicQuery(json);
		if(!CommonUtils.isNullOrEmpty(role.getName())){
			Pattern pattern = Pattern.compile("^.*" + role.getName() + ".*$");
			query.addCriteria(Criteria.where("name").regex(pattern));
		}
		return mongoTemplate.find(query, Role.class);
	}

	@Override
	public List<Role> queryRole2(Role role) throws Exception {
		List<Role> result = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = role==null?"{}":objectMapper.writeValueAsString(role);
		Query query =new BasicQuery(json);
		return mongoTemplate.find(query, Role.class);
	}

	@Override
	public Role updateRole(Role role) {
		Query query = Query.query(Criteria.where(Dict.ID).is(role.getId()));
		Update update = Update.update(Dict.FUNCTIONS,role.getFunctions()).set(Dict.MENUS,role.getMenus());
		mongoTemplate.findAndModify(query, update,  Role.class);
		return mongoTemplate.findOne(query, Role.class);
	}

	@Override
	public List<String> queryRoleid(List<String> roleNames) {
		Query query = Query.query(Criteria.where(Dict.NAME).in(roleNames));
		List<Role> roleIds = mongoTemplate.find(query, Role.class);
		List<String> ids = new ArrayList<>();
		for(Role role : roleIds){
			ids.add(role.getId());
		}
		return ids;
	}

    @Override
    public Role queryIdByName(String roleName) {
		Query query = Query.query(Criteria.where(Dict.NAME).is(roleName));
        return mongoTemplate.findOne(query, Role.class);
    }

	@Override
	public List<Role> queryRoleByIds(List<String> roleIds) {
		Query query = Query.query(Criteria.where(Dict.ID).in(roleIds));
		return mongoTemplate.find(query,Role.class);
	}

	@Override
	public List<Role> queryRoleByMenuId(List<String> menuIds) {
		Query query = Query.query(Criteria.where("menus").in(menuIds));
		return mongoTemplate.find(query,Role.class);
	}


}

package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.utils.TimeUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IGroupAbbrDao;
import com.spdb.fdev.release.entity.ApplicationImage;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;
@Repository
public class GroupAbbrDaoImpl implements IGroupAbbrDao{
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public GroupAbbr queryGroupAbbr(String group_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.GROUP_ID).is(group_id));
		return mongoTemplate.findOne(query, GroupAbbr.class);
	}

	@Override
	public GroupAbbr updateGroupAbbr(String group_id, String group_abbr) throws Exception {
		Query query = new Query(Criteria.where(Dict.GROUP_ID).is(group_id));
		Update update = Update.update(Dict.GROUP_ABBR, group_abbr).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.upsert(query, update, GroupAbbr.class);
		return mongoTemplate.findOne(query, GroupAbbr.class);
	}

	@Override
	public GroupAbbr updateSystemAbbr(String group_id, String system_abbr) {
		Query query = new Query(Criteria.where(Dict.GROUP_ID).is(group_id));
		Update update = Update.update(Dict.SYSTEM_ABBR, system_abbr).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.upsert(query, update, GroupAbbr.class);
		return mongoTemplate.findOne(query, GroupAbbr.class);
	}

	@Override
	public GroupAbbr queryBySysAbbrNotGroupId(String group_id, String system_abbr) {
		Query query = new Query(Criteria.where(Dict.SYSTEM_ABBR).is(system_abbr)
				.and(Dict.GROUP_ID).ne(group_id));
		return mongoTemplate.findOne(query, GroupAbbr.class);
	}

	@Override
	public List<GroupAbbr> queryAllGroupAbbr() {
		Query query = new Query(Criteria.where(Dict.GROUP_ABBR).ne(null));
		List<GroupAbbr> list = mongoTemplate.find(query, GroupAbbr.class);
		return list;
	}

}

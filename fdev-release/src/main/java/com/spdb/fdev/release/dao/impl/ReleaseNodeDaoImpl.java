package com.spdb.fdev.release.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.spdb.fdev.base.dict.Constants;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IReleaseNodeDao;
import com.spdb.fdev.release.entity.ReleaseNode;


/**
 * 投产窗口数据处理层
 */
@Repository
public class ReleaseNodeDaoImpl extends FreleaseDao implements IReleaseNodeDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public List<ReleaseNode> getReleaseNodeName(String release_date) throws Exception {
		// 查询条件为release_date
		return  this.mongoTemplate.find(Query.query(Criteria.where(Dict.RELEASE_DATE).is(release_date).and(Dict.TYPE).ne(Constants.RELEASE_BIG_NODE_TYPE))
				.with(new Sort(Sort.Direction.DESC, Dict.RELEASE_NODE_NAME)), ReleaseNode.class);
	}

	@Override
	public ReleaseNode delete(ReleaseNode releaseNode) throws Exception {
		Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNode.getRelease_node_name());
		Query query = new Query(criteria);
		// 查询条件为release_node_name 删除此投产窗口
		return this.mongoTemplate.findAndRemove(query, ReleaseNode.class);
	}

	@Override
	public ReleaseNode update(Map<String, Object> map) throws Exception {
		String release_node_name = null;
		if(CommonUtils.isNullOrEmpty(map.get(Dict.OLD_RELEASE_NODE_NAME))) {
			release_node_name = (String) map.get(Dict.RELEASE_NODE_NAME);
		}else {
			release_node_name = (String) map.get(Dict.OLD_RELEASE_NODE_NAME);
		}
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		// 更新条件为release_node_name
		Update up = Update.update(Dict.RELEASE_NODE_NAME, map.get(Dict.RELEASE_NODE_NAME));
		
		if(!CommonUtils.isNullOrEmpty(map.get(Dict.RELEASE_DATE))) {
			up.set(Dict.RELEASE_DATE, map.get(Dict.RELEASE_DATE));
		}
		if (!CommonUtils.isNullOrEmpty(map.get(Dict.RELEASE_MANAGER))) {
			up.set(Dict.RELEASE_MANAGER, map.get(Dict.RELEASE_MANAGER));
		}
		if (!CommonUtils.isNullOrEmpty(map.get(Dict.RELEASE_SPDB_MANAGER))) {
			up.set(Dict.RELEASE_SPDB_MANAGER, map.get(Dict.RELEASE_SPDB_MANAGER));
		}
		if (!CommonUtils.isNullOrEmpty(map.get(Dict.UAT_ENV_NAME))) {
			up.set(Dict.UAT_ENV_NAME, map.get(Dict.UAT_ENV_NAME));
		}
		if (!CommonUtils.isNullOrEmpty(map.get(Dict.TYPE))) {
			up.set(Dict.TYPE, map.get(Dict.TYPE));
		}
		Map<String,Object> owner_group = (Map<String, Object>) map.get(Dict.OWNER_GROUP);
		if(!CommonUtils.isNullOrEmpty(owner_group)){
			if(!CommonUtils.isNullOrEmpty(owner_group.get(Dict.OWNER_GROUPID))) {
				up.set(Dict.OWNER_GROUPID,owner_group.get(Dict.OWNER_GROUPID));
			}
			if(!CommonUtils.isNullOrEmpty(owner_group.get(Dict.OWNER_GROUP_NAME))) {
				up.set(Dict.OWNER_GROUP_NAME,owner_group.get(Dict.OWNER_GROUP_NAME));
			}

		}
		up.set(Dict.UPDATE_TIME,CommonUtils.formatDate("yyyy-MM-dd"));
		this.mongoTemplate.findAndModify(query, up, ReleaseNode.class);
		Query query2 = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(map.get(Dict.RELEASE_NODE_NAME)));
		return mongoTemplate.findOne(query2, ReleaseNode.class);
	}

	@Override
	public List<ReleaseNode> queryReleaseNodes(Map<String, String> map) throws Exception {
		String startDate = map.remove(Dict.START_DATE);
		String endData = map.remove(Dict.END_DATE);
		ReleaseNode releaseNode = new ReleaseNode();
		releaseNode.setRelease_node_name(map.get(Dict.RELEASE_NODE_NAME));
		releaseNode.setRelease_manager(map.get(Dict.RELEASE_MANAGER));
		String json = objectMapper.writeValueAsString(releaseNode);
		Query query = new BasicQuery(json);
		Criteria criteria = new Criteria();
		Criteria dateCriteria = new Criteria(Dict.RELEASE_DATE);
		if (!CommonUtils.isNullOrEmpty(startDate)) {
		   dateCriteria.gte(startDate);
		}
		if (!CommonUtils.isNullOrEmpty(endData)) {
		   dateCriteria.lte(endData);
		}
		if (!CommonUtils.isNullOrEmpty(endData)||!CommonUtils.isNullOrEmpty(startDate)) {
			criteria.andOperator(dateCriteria);
		}
		if(!CommonUtils.isNullOrEmpty(map.get(Dict.OWNER_GROUPID))) {
			criteria.and(Dict.OWNER_GROUPID).in(map.get(Dict.OWNER_GROUPID).split(","));
		}

		if(!CommonUtils.isNullOrEmpty(map.get(Dict.TYPE))) {
			criteria.and(Dict.TYPE).is(map.get(Dict.TYPE));
		}else {
			criteria.and(Dict.TYPE).ne(Constants.RELEASE_BIG_NODE_TYPE);
		}
		query.addCriteria(criteria);
		if(Constants.RELEASE_BIG_NODE_TYPE.equals(map.get(Dict.TYPE))){
			query.with(new Sort(Sort.Direction.DESC, Dict.RELEASE_NODE_NAME));
		}else {
			query.with(new Sort(Sort.Direction.ASC, Dict.RELEASE_NODE_NAME));
		}
		query.fields().exclude(Dict.OBJECTID);
		return  mongoTemplate.find(query, ReleaseNode.class);

	}

	@Override
	public ReleaseNode queryDetail(String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		return this.mongoTemplate.findOne(query, ReleaseNode.class);
	}

	@Override
	public ReleaseNode create(ReleaseNode releaseNode) throws Exception {
		try {
			return this.mongoTemplate.save(releaseNode);
		} catch (DuplicateKeyException e) {
			if (e.getMessage().indexOf(Dict.RELEASE_NODE_NAME) >= 0) {
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "`投产窗口名不能重复!" });
			}
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR);
		}
	}

	@Override
	public List<ReleaseNode> queryReleaseNodesByGroupId(List<Map<String, Object>> group_id) throws Exception {
		List<ReleaseNode> list = new ArrayList<>();
		for (Map<String, Object> map : group_id) {
			Query query = new Query(Criteria.where(Dict.OWNER_GROUPID).is(map.get(Dict.ID)));
			List<ReleaseNode> node_list = mongoTemplate.find(query, ReleaseNode.class);
			list.addAll(node_list);
		}
		return list;
	}

	@Override
	public List<String> archivedNodeByDate(String release_date) throws Exception {
		Query query =  new Query(Criteria.where(Dict.RELEASE_DATE).lt(release_date));
		List<ReleaseNode> releaseNodes = mongoTemplate.find(query, ReleaseNode.class);
		List<String> list =new ArrayList<>();
		for (ReleaseNode releaseNode : releaseNodes) {
			list.add(releaseNode.getRelease_node_name());
		}
		return list;
	}

	@Override
	public List<ReleaseNode> threeDaysAgoNode() throws Exception{
		String archivedDay = CommonUtils.archivedDay(CommonUtils.DATE_PARSE);
		Query query=new Query(Criteria.where(Dict.RELEASE_DATE).lte(archivedDay));
		return mongoTemplate.find(query,ReleaseNode.class);
	}

	@Override
	public List<ReleaseNode> updateReleaseNodeBatch(String start_date, String end_date) throws Exception {
		Criteria criteria = new Criteria();
		if(CommonUtils.isNullOrEmpty(start_date)){
			criteria.and(Dict.RELEASE_DATE).lte(end_date);
		}else {
			//判断开始时间是否大于结束时间
			if(this.valDate(start_date, end_date)){
				throw new FdevException(ErrorConstants.START_DATE_ERROR);
			}
			criteria.and(Dict.RELEASE_DATE).gte(start_date).lte(end_date);
		}
		Query query = new Query(criteria);
		//先得到数据的返回
		List<ReleaseNode> releaseNodes = mongoTemplate.find(query, ReleaseNode.class);
		return releaseNodes;
	}

	@Override
	public ReleaseNode updateBigReleaseNode(ReleaseNode releaseNode, String releaseNodeName) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, releaseNode.getRelease_node_name())
				.set(Dict.RELEASE_DATE,releaseNode.getRelease_date())
				.set(Dict.RELEASE_CONTACT,releaseNode.getRelease_contact())
				.set(Dict.OWNER_GROUPID,releaseNode.getOwner_groupId())
				.set(Dict.UPDATE_TIME,releaseNode.getUpdate_time());
		this.mongoTemplate.findAndModify(query,update,ReleaseNode.class);
		Query query2 = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNode.getRelease_node_name()));
		return mongoTemplate.findOne(query2,ReleaseNode.class);
	}

	@Override
	public List<ReleaseNode> queryReleaseNodesByCondition(String release_date) {
		Query query = new Query();
		List<String> typeList = new ArrayList<>();
		typeList.add("1");
		typeList.add("2");
		query.addCriteria(Criteria.where(Dict.RELEASE_DATE).is(release_date))
				.addCriteria(Criteria.where(Dict.TYPE).in(typeList));
		return mongoTemplate.find(query, ReleaseNode.class);
	}

	@Override
	public List<ReleaseNode> queryReleaseNodesByGroupIdAndDate(String groupId, String release_date) throws Exception {
		List<String> typeList=new ArrayList<>();
		typeList.add("1");
		typeList.add("2");
		Query query = new Query(Criteria.where(Dict.OWNER_GROUPID)
				.is(groupId)
				.and(Dict.RELEASE_DATE)
				.is(release_date)
				.and(Dict.TYPE)
				.in(typeList));
		return mongoTemplate.find(query, ReleaseNode.class);
	}

	@Override
	public List<ReleaseNode> queryFromDate(String date) {
		Query query =  new Query(Criteria.where(Dict.RELEASE_DATE).gte(date).and(Dict.TYPE).in(Arrays.asList("1", "2")));
		List<ReleaseNode> releaseNodes = mongoTemplate.find(query, ReleaseNode.class);
		return releaseNodes;
	}

	@Override
	public String queryDateByName(String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		ReleaseNode releaseNode = mongoTemplate.findOne(query, ReleaseNode.class);
		return releaseNode.getRelease_date();
	}

	@Override
	public String queryNodes(String application_id, String startDate) {
		//将投产管理表和投产模块应用表连接
		AggregationOperation lookup =
				Aggregation.lookup("release_nodes", "release_node_name","release_node_name", "nodes");
		//查找投产管理中小于投产当天，大于等于当前日期的所有投产
		Criteria dateCriteria =	new Criteria().and("nodes.release_date").gte(startDate);//.and("nodes.release_date").lt(endDate);
		// 在投产模块应用中根据应用id查找应用
		Criteria criteria = new Criteria().and("application_id").is(application_id);
		// 投产模块应用表和投产管理表联合查询
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria, dateCriteria));
		// 按照投产窗口的日期倒序排列
		Sort sort = new Sort(Sort.Direction.ASC, "nodes.release_date");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		// 查询条件 取第一个
		AggregationResults<Map> docs =
				mongoTemplate.aggregate(Aggregation.newAggregation(lookup, match, sortOperation), "release_applications", Map.class);
		// 不为空则取出投产窗口
		String release_node_name = "";
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			release_node_name = (String) docs.getMappedResults().get(0).get(Dict.RELEASE_NODE_NAME);
		}
		return release_node_name;
	}

	/**
	 * 判断时间的大小
	 * @param start_date
	 * @param end_date
	 * @return
	 * @throws Exception
	 */
	private boolean valDate(String start_date, String end_date) throws Exception {
		long start = CommonUtils.parseDate(start_date, CommonUtils.DATE_PARSE).getTime();
		long end = CommonUtils.parseDate(end_date, CommonUtils.DATE_PARSE).getTime();
		if((start - end)>0){
			return true;
		}
		return false;
	}

}

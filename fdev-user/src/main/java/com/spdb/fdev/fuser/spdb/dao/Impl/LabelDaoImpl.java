package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.cache.RedisCache;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.LabelDao;
import com.spdb.fdev.fuser.spdb.entity.user.Label;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
public class LabelDaoImpl implements LabelDao {

	private static final Logger logger = LoggerFactory.getLogger(LabelDaoImpl.class);//控制台日志打印
	
	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private RedisCache redisCache;

	@Override
	public Label addLabel(Label label) {
		label.initId();
		return mongoTemplate.save(label);
	}

	@Override
	public Label delLabelByID(String id) {
		//删除用户的标签中使用的该标签
		Query que = new Query();
    	Update up = new Update();
    	up.pull(Dict.LABELS, id);
		redisCache.removeCache("fuser.alluser");
		redisCache.removeCache("fuser.allcoreuser");
    	mongoTemplate.updateMulti(que, up, User.class);
    	//逻辑删除标签,将标签状态置为废弃
    	logger.info("逻辑删除标签,将标签状态置为废弃: " + id);
		Query query = Query.query(Criteria.where(Dict.ID).is(id));
		Update update = Update.update(Dict.STATUS, "0");
		return mongoTemplate.findAndModify(query, update, Label.class);
	}

	@Override
	public List<Label> queryLabel(Label label) throws Exception {
		List<Label> result = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = label==null?"{}":objectMapper.writeValueAsString(label);
		JSONObject pJson = JSONObject.fromObject(json);
		Iterator<Object> it = pJson.keys();
		Criteria c = new Criteria();
		while(it.hasNext()) {
			String key = (String) it.next();
			Object value = pJson.get(key);
			c.and(key).is(value);
		}
		AggregationOperation match = Aggregation.match(c);
		AggregationResults<Label> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match),"label", Label.class);
		docs.forEach(doc->{
			result.add(doc);
		});
		return result;
	}

	@Override
	public Label updateLabel(Label label) throws Exception {
		Query query = Query.query(Criteria.where(Dict.ID).is(label.getId()));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = objectMapper.writeValueAsString(label);
		JSONObject pJson = JSONObject.fromObject(json);
		Iterator<Object> it = pJson.keys();
		Update update = Update.update(Dict.ID, label.getId());
		while(it.hasNext()) {
			String key = (String) it.next();
			Object value = pJson.get(key);
			if(Dict.OBJECTID.equals(key)) {
				continue;
			}
			update.set(key, value);
		}
		mongoTemplate.findAndModify(query, update,  Label.class);
		return mongoTemplate.findOne(query, Label.class);
	}

	@Override
	public List<Label> queryLableByName(String label) {
		Query query = new Query();
		//忽略大小写
		Pattern pattern = Pattern.compile("^.*" + label + ".*$", Pattern.CASE_INSENSITIVE);
		query.addCriteria(Criteria.where(Dict.NAME).regex(pattern).and(Dict.STATUS).is(Dict.ONE));
		return mongoTemplate.find(query,Label.class);
	}
}

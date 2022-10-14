
package com.spdb.fdev.release.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IAssetCatalogDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import com.spdb.fdev.release.service.IReleaseCatalogService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ReleaseCatalogServiceImpl implements IReleaseCatalogService {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private IAssetCatalogDao assetCatalogDao;

	@Override
	public AssetCatalog create(AssetCatalog catalog) throws Exception {
		ObjectId id = new ObjectId();
		catalog.set_id(id);
		catalog.setId(id.toString());
		return mongoTemplate.save(catalog);
	}

	public List<AssetCatalog> query(String id) throws Exception {
		  List<AssetCatalog> result = new ArrayList<>();
		Criteria c = Criteria.where("template_id").is(id);
		AggregationOperation project = Aggregation.project().andExclude("_id");
		AggregationOperation match = Aggregation.match(c);
		AggregationResults<AssetCatalog> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match),
				"asset_catalog", AssetCatalog.class);
		docs.forEach(doc ->result.add(doc));
		return result;
	}

	@Override
	public boolean deleteByTemplateId(String id) throws Exception {
		Criteria criteria = new Criteria();
		criteria.and("template_id").is(id);
		Query query = new Query(criteria);
		mongoTemplate.remove(query, AssetCatalog.class);
		return true;
	}
	
	@Override
	public boolean delete(String id) throws Exception {
		Criteria criteria = new Criteria();
		criteria.and(Dict.ID).is(id);
		Query query = new Query(criteria);
		mongoTemplate.remove(query, AssetCatalog.class);
		return true;
	}
	@Override
	public AssetCatalog update(AssetCatalog catalog) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = objectMapper.writeValueAsString(catalog);
		BasicDBObject catalogJson = BasicDBObject.parse(json);
		Iterator<String> it = catalogJson.keySet().iterator();
		Query query = Query.query(Criteria.where(Dict.ID).is(catalog.getId()));
		Update update = Update.update("id", catalog.getTemplate_id());
		while (it.hasNext()) {
			String key = it.next();
			Object value = catalogJson.get(key);
			if ("_id".equals(key) || "serialVersionUID".equals(key)) {
				continue;
			}
			update.set(key, value);
		}
		mongoTemplate.findAndModify(query, update, AssetCatalog.class);
		return mongoTemplate.findOne(query, AssetCatalog.class);
	}

	@Override
	public AssetCatalog queryAssetCatalogByName(String template_id, String asset_catalog_name) throws Exception {
		AssetCatalog assetCatalog = assetCatalogDao.queryAssetCatalogByName(template_id, asset_catalog_name);
		if (CommonUtils.isNullOrEmpty(assetCatalog)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "介质目录为空" });
		}
		return assetCatalog;

	}


	@Override
	public List<AssetCatalog> queryByName(String name) throws Exception {
		Criteria criteria = new Criteria();
		criteria.and(Dict.CATALOG_NAME).is(name);
		Query query = new Query(criteria);
		return mongoTemplate.find(query, AssetCatalog.class);
	}
}

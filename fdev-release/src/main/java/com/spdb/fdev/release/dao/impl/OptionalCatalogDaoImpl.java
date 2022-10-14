package com.spdb.fdev.release.dao.impl;

import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.IOptionalCatalogDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import com.spdb.fdev.release.entity.OptionalCatalog;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OptionalCatalogDaoImpl implements IOptionalCatalogDao {

    @Resource
    private MongoTemplate mongoTemplate;
	
	@Override
	public List<OptionalCatalog> query(List<String> catalogTypeList) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
		if(!CommonUtils.isNullOrEmpty(catalogTypeList)) {
			query.addCriteria(Criteria.where(Dict.CATALOG_TYPE).nin(catalogTypeList));
		}
        List<OptionalCatalog> list = mongoTemplate.find(query, OptionalCatalog.class);
		return list;
				 
	}

	@Override
	public void save(OptionalCatalog optionalCatalog) throws Exception {
		mongoTemplate.save(optionalCatalog);
	}

	@Override
	public void update(OptionalCatalog optionalCatalog) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(optionalCatalog.getId()));
		Update update = Update.update(Dict.CATALOG_NAME, optionalCatalog.getCatalog_name())
				.set(Dict.CATALOG_TYPE, optionalCatalog.getCatalog_type())
				.set(Dict.DESCRIPTION, optionalCatalog.getDescription());
		mongoTemplate.findAndModify(query, update, OptionalCatalog.class);
	}

	@Override
	public void deleteById(String id) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		mongoTemplate.findAndRemove(query, OptionalCatalog.class);
	}

	@Override
	public List<OptionalCatalog> queryAll() {
		Query query = new Query();
		query.fields().exclude(Dict.OBJECTID).exclude(Dict.ID);
		List<OptionalCatalog> list = mongoTemplate.find(query, OptionalCatalog.class);
		return list;
	}

	@Override
	public Map<String, Object> editTypeByName(String name, String type) {
		Query query = new Query(Criteria.where(Dict.CATALOG_NAME).is(name));
		query.fields().exclude(Dict.OBJECTID);
		Update update = Update.update(Dict.CATALOG_TYPE, type);
        mongoTemplate.findAndModify(query, update, OptionalCatalog.class);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, AssetCatalog.class);
        List<OptionalCatalog> optionalCatalogs = mongoTemplate.find(query, OptionalCatalog.class);
        List<AssetCatalog> assetCatalogs = mongoTemplate.find(query, AssetCatalog.class);
        Map<String, Object> map = new HashMap<>();
        map.put("optionalCatalogs", optionalCatalogs);
        map.put("updateResult", updateResult);
        map.put("assetCatalogs", assetCatalogs);
        return map;
	}

}

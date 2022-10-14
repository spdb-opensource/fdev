package com.spdb.fdev.release.dao.impl;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IAssetCatalogDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import java.util.List;


@Repository
public class AssetCatalogDaoImpl implements IAssetCatalogDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public AssetCatalog queryAssetCatalogById(String asset_catalog_id) {
        Query query = new Query(Criteria.where(Dict.ID).is(asset_catalog_id));
        return mongoTemplate.findOne(query, AssetCatalog.class);
    }

    @Override
    public AssetCatalog queryAssetCatalogByName(String template_id, String asset_catalog_name) {
        Query query = new Query(Criteria.where(Dict.TEMPLATE_ID).is(template_id)
                .and(Dict.CATALOG_NAME).is(asset_catalog_name));
        return mongoTemplate.findOne(query, AssetCatalog.class);
    }


    @Override
	public AssetCatalog findMSAssetCatalog(String template_id) throws Exception {
		Query query =new Query(Criteria.where(Dict.TEMPLATE_ID)
				.is(template_id)
				.and(Dict.CATALOG_TYPE)
				.is(Constants.ASSTETYPE_MS));
		return mongoTemplate.findOne(query, AssetCatalog.class);
	}

    @Override
    public List<AssetCatalog> queryByTemplateIdAndType(String template_id, List<String> catalogTypes) {
        Query query = new Query(Criteria.where("template_id").is(template_id)
                .and(Dict.CATALOG_TYPE).in(catalogTypes));
        return mongoTemplate.find(query, AssetCatalog.class);
    }

}

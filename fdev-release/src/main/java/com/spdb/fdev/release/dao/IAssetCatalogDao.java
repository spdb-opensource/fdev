package com.spdb.fdev.release.dao;


import com.spdb.fdev.release.entity.AssetCatalog;

import java.util.List;

public interface IAssetCatalogDao {

    AssetCatalog queryAssetCatalogById(String asset_catalog_id) ;

    AssetCatalog queryAssetCatalogByName(String template_id, String asset_catalog_name) ;

	AssetCatalog findMSAssetCatalog(String template_id)throws Exception;

    List<AssetCatalog> queryByTemplateIdAndType(String template_id, List<String> catalogTypes);
}

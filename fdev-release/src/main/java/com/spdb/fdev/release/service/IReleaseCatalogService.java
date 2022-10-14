package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.AssetCatalog;

import java.util.List;

public interface IReleaseCatalogService {

	AssetCatalog create(AssetCatalog catalog) throws Exception;
	     
	boolean deleteByTemplateId(String id) throws Exception;

	boolean delete(String id) throws Exception;

	List<AssetCatalog>  query(String id)  throws Exception;

	AssetCatalog update(AssetCatalog catalog) throws Exception;

	AssetCatalog queryAssetCatalogByName(String template_id, String asset_catalog_name)throws Exception;
	
	List<AssetCatalog> queryByName(String name) throws Exception;
}

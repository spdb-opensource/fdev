package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.AssetCatalog;

public interface IAssetCatalogService {
	
	AssetCatalog findMSAssetCatalog(String template_id)throws Exception;
	
	AssetCatalog queryAssetCatalogById(String id) throws Exception;
	

}

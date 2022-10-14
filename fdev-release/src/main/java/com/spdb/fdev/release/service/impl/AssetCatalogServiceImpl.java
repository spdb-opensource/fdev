package com.spdb.fdev.release.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.release.dao.IAssetCatalogDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import com.spdb.fdev.release.service.IAssetCatalogService;
@Service
public class AssetCatalogServiceImpl implements IAssetCatalogService{
	@Autowired
	private IAssetCatalogDao assetCatalogDao;

	@Override
	public AssetCatalog findMSAssetCatalog(String template_id) throws Exception {
		return assetCatalogDao.findMSAssetCatalog(template_id);		
	}

	@Override
	public AssetCatalog queryAssetCatalogById(String id) throws Exception {
		return assetCatalogDao.queryAssetCatalogById(id);
	}

}

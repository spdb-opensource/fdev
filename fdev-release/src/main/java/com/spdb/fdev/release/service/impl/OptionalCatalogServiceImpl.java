package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.IOptionalCatalogDao;
import com.spdb.fdev.release.entity.OptionalCatalog;
import com.spdb.fdev.release.service.IOptionalCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OptionalCatalogServiceImpl implements IOptionalCatalogService {
	  @Autowired
	  private IOptionalCatalogDao optionalCatalogDao;

	@Override
	public List<OptionalCatalog> query(List<String> catalogTypeList) {
	     return optionalCatalogDao.query(catalogTypeList);
	}

	@Override
	public Map<String, OptionalCatalog> queryOptionalCatalogs() {
		List<OptionalCatalog> optionalCatalogs = optionalCatalogDao.query(null);
		Map<String, OptionalCatalog> catalogs = new HashMap<>();
		for (OptionalCatalog optionalCatalog : optionalCatalogs) {
			catalogs.put(optionalCatalog.getCatalog_name(), optionalCatalog);
		}
		return catalogs;
	}

	@Override
	public void save(OptionalCatalog optionalCatalog) throws Exception {
		optionalCatalogDao.save(optionalCatalog);
	}

	@Override
	public void update(OptionalCatalog optionalCatalog) throws Exception {
		optionalCatalogDao.update(optionalCatalog);
	}

	@Override
	public void deleteById(String id) throws Exception {
		optionalCatalogDao.deleteById(id);
	}

	@Override
	public List<OptionalCatalog> queryAll() {
		return optionalCatalogDao.queryAll();
	}

	@Override
	public Map<String, Object> editTypeByName(String name, String type) {
		return optionalCatalogDao.editTypeByName(name, type);
	}
}

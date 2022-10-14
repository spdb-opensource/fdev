package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.OptionalCatalog;

import java.util.List;
import java.util.Map;

public interface IOptionalCatalogService {

	List<OptionalCatalog> query(List<String> catalogTypeList);

	Map<String, OptionalCatalog> queryOptionalCatalogs();

    void save(OptionalCatalog optionalCatalog) throws Exception;

	void update(OptionalCatalog optionalCatalog) throws Exception;

	void deleteById(String id) throws Exception;

	List<OptionalCatalog> queryAll();

	/**
	 * 修改optional_catalog介质目录表 asset_catalog模板与介质目录关联表
	 * @param name 目录
	 * @param type 类型
	 */
	Map<String, Object> editTypeByName(String name, String type);
}

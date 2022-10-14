package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.OptionalCatalog;

import java.util.List;
import java.util.Map;

public interface IOptionalCatalogDao {

	List<OptionalCatalog> query(List<String> catalogTypeList);

    void save(OptionalCatalog optionalCatalog) throws Exception;

    void update(OptionalCatalog optionalCatalog) throws Exception;

    void deleteById(String id) throws Exception;

    List<OptionalCatalog> queryAll();

    Map<String, Object> editTypeByName(String name, String type);
}

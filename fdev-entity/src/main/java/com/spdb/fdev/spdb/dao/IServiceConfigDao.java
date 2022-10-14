package com.spdb.fdev.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.spdb.entity.ServiceConfig;

public interface IServiceConfigDao {

	List<ServiceConfig> queryByServiceGitId(Map<String, Object> paramMap);
	
	List<ServiceConfig> queryServiceConfig(Map<String, Object> requestParam);

	void addServiceConfig(ServiceConfig serviceConfig);

	void updateServiceConfig(ServiceConfig serviceConfig) throws Exception;

	Long queryServiceConfigCount(Map<String, Object> requestParam);

	void deleteServiceConfig(Map<String, Object> requestParam);
}

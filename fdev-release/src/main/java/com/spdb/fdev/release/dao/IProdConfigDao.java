package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ProdConfigApp;
import com.spdb.fdev.release.entity.ReleaseApplication;

import java.util.List;


public interface IProdConfigDao {
	/**
	 * 根据投产点名称查询配置文件变更应用
	 * @param release_node_name
	 * @return
	 * @throws Exception
	 */
	List<ProdConfigApp> queryConfigApplication(String release_node_name) throws Exception;


	ProdConfigApp addConfigApplication(ProdConfigApp prodConfigApp);
	/**
	 * 修改审核状态。
	 * @param release_node_name
	 * @param application_id
	 * @param ischeck
	 * @return
	 */
	ProdConfigApp checkConfigApplication(String release_node_name, String application_id,String ischeck);

	/**
	 * 根据应用id和投产点名称查询配置文件变更应用
	 * @param application_id
	 * @param release_node_name
	 * @return
	 */
	ProdConfigApp queryConfigs(String application_id, String release_node_name);

	/**
	 * 更新配置变更应用表实体属性
	 * @param prodConfigApp
	 * @return
	 */
	ProdConfigApp updateConfig(ProdConfigApp prodConfigApp);

	/**
	 * 通过投产点点名称、应用ID集合查询
	 * @param release_node_name
	 * @param applications
	 * @return
	 */
	List<ProdConfigApp> queryConfigList(String release_node_name, List<String> applications);
	/**
	 * 更新配置变更应用表配置文件生成状态以及配置文件TAG
	 * @param prodConfigApp
	 * @return
	 */
    ProdConfigApp updateStatus(ProdConfigApp prodConfigApp);

	/**
	 * 删除配置文件变更应用
	 * @param release_node_name
	 * @param application_id
	 */
	void deleteConfig(String release_node_name, String application_id);
}

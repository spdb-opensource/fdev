
package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IProdApplicationService {
	/**
	 * 查询变更应用
	 * @param prod_id 变更id
	 * @param application_id 应用id
	 * @return
	 * @throws Exception
	 */
	ProdApplication queryApplication(String prod_id, String application_id)throws Exception;
	/**
	 * 查询变更应用列表
	 * @param prodApplication 包含变更id与变更应用类型
	 * @return
	 * @throws Exception
	 */
	List<Map> queryApplications(ProdApplication prodApplication)throws Exception;

	/**
	 * 添加变更应用
	 * @param prodApplication
	 * @return
	 * @throws Exception
	 */
	ProdApplication addApplication(ProdApplication prodApplication)throws Exception;
	/**
	 * 删除变更应用
	 * @param prod_id 变更id
	 * @param application_id 应用id
	 * @throws Exception
	 */
	void deleteApplication(String prod_id, String application_id)throws Exception;
	/**
	 * 设置变更应用模版
	 * @param prod_id 变更id 
	 * @param application_id 应用id
	 * @param template_id 模版id
	 * @return
	 * @throws Exception
	 */
	ProdApplication setApplicationTemplate(String prod_id, String application_id, String template_id)throws Exception;
	/**
	 * 设置变更应用镜像标签
	 * @param prod_id 变更id
	 * @param application_id 应用id
	 * @param pro_image_uri 镜像标签
	 * @return
	 * @throws Exception
	 */
	ProdApplication setImageUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri,String fake_image_uri)throws Exception;
	/**
	 * 根据投产窗口和应用id删除 变更应用
	 * @param release_node_name 投产窗口名
	 * @param application_id 应用id
	 */
	public void deleteApplicationByNode(String release_node_name, String application_id) throws Exception;
	
	/**
	 * 查询不存在于总介质目录的变更应用
	 * @param prod_assets_version
	 * @return
	 * @throws Exception
	 */
	List<ReleaseApplication> queryAppWithOutSum(String prod_assets_version, String release_node_name) throws Exception;
	
	/**
	 * 查询该总介质目录下已存在的应用列表
	 * @param prod_assets_version
	 * @return
	 * @throws Exception
	 */
	Set<ProdApplication> queryAppBySum(String prod_assets_version) throws Exception;
	/**
	 * 删除变更下的所有变更应用
	 * @param prod_id
	 */
	void deleteAppByProd(String prod_id) throws Exception;

	Map<String, Object> findConfigByGitlab(String gitlabId) throws Exception;

	Map<String,Set<String>> queryVarByLabelAndType(Integer gitlabId) throws Exception;

	String findLatestUri(String application_id, ProdRecord prodRecord, String uriField);

	ProdApplication updateApplication(ProdApplication prodApplication);

	String findLastReleaseUri(String application_id, ProdRecord prodRecord, String deployType);

	List<ProdApplication> findUriByApplicationId(String application_id);

	String findLastTagByApplicationId(String application_id);

	ProdApplication setImageConfigUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
									  boolean fdev_config_changed, String compare_url,
									  String fdev_config_confirm, String fake_image_uri,String tag);

	ProdApplication setImageConfigConfirmUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
											 String fdev_config_confirm, String fake_image_uri);

	List<Map<String, Object>> queryApplicationsNoException(String prod_id) throws Exception;

	String checkEnvConfig(Map<String, Object> configGitlab, String new_tag);

	String queryLastTagByGitlabId(String application_id, String prod_id, String type,String release_node_name);

	List<ProdApplication> queryByProdId(String prod_id);

	List<ProdApplication> queryByApplicationId(String application_id);

	String findLatestTag(String application_id, String deployType);

	/**
	 * 查询上次投产镜像标签
	 * @param application_id
	 * @param prodRecord
	 * @return
	 */
	String findLastReleaseTag(String application_id, ProdRecord prodRecord, String deployType);

	ProdApplication setConfigUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri, boolean fdev_config_changed, String compare_url, String fdev_config_confirm, String new_tag);

	ProdApplication setPackageTag(String prod_id, String application_id, String pro_package_uri);

	void updateProdDir(Map<String, Object> requestParam) throws Exception ;

	void updateProdChange(String prd_id, String application_id, Map<String, Object> changeList);
	
	Map<String, Object> queryDeployTypeByAppId(String applicationId, String prodId) throws Exception;
	
	void updateProdDeploy(String prd_id, String application_id, String release_type, List<String> deploy_type) throws Exception;
	
	List<String> queryOldDeployTypeByAppId(String application_id, ProdRecord prodRecord);
	
	HashSet<String> findAllDeployedType(String application_id, ProdRecord prodRecord);

	/**
	 * 发蓝鲸交易获取副本数
	 * */
	Map<String,Object> queryReplicasnu(Map<String,Object> req) throws Exception;
	/**
	 * 修改副本数
	 * */
	void updateReplicasnu(Map<String,Object> req) throws Exception;

	void getCaasNewConfig(List<String> prodDirList, String application_id, ProdRecord prodRecord, ProdApplication prodApplication, String proImageUri, Map<String, Object> changeMap) throws Exception;

	void getSccNewConfig(List<String> prodDirList, String application_id, ProdRecord prodRecord, ProdApplication prodApplication, String proSccImageUri, Map<String, Object> changeMap) throws Exception;

}

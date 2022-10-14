package com.spdb.fdev.release.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;

public interface IProdApplicationDao {

	ProdApplication queryApplication(String prod_id, String application_id) throws Exception;

	List<ProdApplication> queryApplications(ProdApplication prodApplication) throws Exception;

	ProdApplication addApplication(ProdApplication prodApplication) throws Exception;

	void deleteApplication(String prod_id, String application_id) throws Exception;

	ProdApplication setApplicationTemplate(String prod_id, String application_id, String template_id)
			throws Exception;

	ProdApplication setImageUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri, String fake_image_uri);

	public void deleteApplicationByNode(String release_node_name, String application_id) throws Exception;

	List<ReleaseApplication> queryAppWithOutSum(String prod_assets_version, String release_node_name)throws Exception;

	Set<ProdApplication> queryAppBySum(String prod_assets_version) throws Exception;

	void deleteAppByProd(String prod_id) throws Exception;

	/**
	 * 查询上次投产变更中的镜像标签
	 * @param application_id
	 * @param prodRecord
	 * @return
	 */
	String findLatestUri(String application_id, ProdRecord prodRecord, String deployType);

	ProdApplication updateApplication(ProdApplication prodApplication);

	String findLastReleaseUri(String application_id, ProdRecord prodRecord, String deployType);

	List<ProdApplication> findUriByApplicationId(String application_id);

	String findLastTagByApplicationId(String application_id);

	ProdApplication setImageConfigUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
									  boolean fdev_config_changed, String compare_url,
									  String fdev_config_confirm, String fake_image_uri,String tag);

	ProdApplication setImageConfigConfirmUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
											 String fdev_config_confirm, String fake_image_uri);

    String findImageUriByReleaseNodeName(String application_id, String release_node_name);

    String queryLastTagByGitlabId(String application_id, String prod_id, String type,String release_node_name);

	List<ProdApplication> queryImages(String application_id, List<String> prodIds);

	/**
	 * 根据变更id获取变更应用信息
	 * @param prod_id
	 * @return
	 */
    List<ProdApplication> queryByProdId(String prod_id);

	/**
	 * 根据应用id获取变更应用信息
	 * @param application_id
	 * @return
	 */
	List<ProdApplication> queryByApplicationId(String application_id);

	/**
	 * 查询当前变更应用中变更日期最大的镜像标签
	 * @param application_id
	 * @return
	 */
	String findLatestTag(String application_id ,String deployType);

	/**
	 * 查询上次投产变更中的tag
	 * @param application_id
	 * @param prodRecord
	 * @return
	 */
    String findLastReleaseTag(String application_id, ProdRecord prodRecord,String deployType);

    ProdApplication setConfigUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri, boolean fdev_config_changed, String compare_url, String fdev_config_confirm, String new_tag);

	ProdApplication setPackageTag(String prod_id, String application_id, String pro_package_uri);

	void updateProdDir(String prd_id, String application_id, List<String> prod_dir, List<String> deploy_type);

    ProdApplication queryByTag(String prod_id, String application_id, String tag);

	void updateProdChange(String prd_id, String application_id, Map<String, Object> changeList);
	
	List<String> queryOldDeployTypeByAppId(String application_id, String prod_id, String type);

	HashSet<String> findAllDeployedType(String application_id, ProdRecord prodRecord);

	void updateProdDirs(ProdApplication prodApplication);

	void updateEsfFlag(ProdApplication prodApplication) throws Exception;

    void updateProdStopEnv(String prd_id, String application_id, List<String> caas_stop_env, List<String> scc_stop_env);
}

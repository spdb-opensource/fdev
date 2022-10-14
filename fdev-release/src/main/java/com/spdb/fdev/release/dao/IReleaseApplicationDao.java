package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseNode;

import java.util.List;
/**
 * 投产应用数据库接口层
 *
 */
public interface IReleaseApplicationDao {

	List<ReleaseNode> queryReleaseNodesByAppId(String application_id, String node_status) throws Exception;

	void deleteApplication(String application_id, String release_node_name) throws Exception;

	List<ReleaseApplication> queryApplications(String release_node_name) throws Exception;

	ReleaseApplication queryApplicationDetail(ReleaseApplication releaseApplication) throws Exception;

	ReleaseApplication queryAppByIdAndBranch(String applicationId, String release_branch) throws Exception;

	ReleaseApplication updateReleaseApplication(ReleaseApplication releaseApplication)throws Exception;

	ReleaseApplication findOneReleaseApplication(String appliction_id, String relase_node_name)throws Exception;

	ReleaseApplication saveReleaseApplication(ReleaseApplication releaseApplication) throws Exception;

	ReleaseApplication queryAppByIdAndTag(String applicationId, String product_tag)throws Exception;

	void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id, String release_branch) throws Exception;

	void updateReleaseNodeName(String new_release_node_name, String release_node_name) throws Exception;

	void updateReleaseApplicationConfig(ReleaseApplication releaseApplication);

	ReleaseApplication updateConfigConfirm(ReleaseApplication releaseApplication);

	List<ReleaseApplication> queryOldApplication(String application_id, String release_node_name);

	/**
	 * 根据应用id和投产窗口名称查询应用信息
	 * @param application_id
	 * @param release_node_name
	 * @return
	 */
	ReleaseApplication queryByIdAndNodeName(String application_id, String release_node_name);

    void updateApplicationPath(ReleaseApplication application);

    void updateStaticResource(ReleaseApplication application);
}
package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseNode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ReleaseApplication接口类
 *
 */
public interface IReleaseApplicationService {
	/**
	 * 根据应用id查询所有投产点名称列表
	 * 
	 * @param application_id
	 *            应用id
	 * @return 投产窗口列表
	 * @throws Exception
	 */
	List<ReleaseNode> queryReleaseNodesByAppId(String application_id, String node_status) throws Exception;

	/**
	 * 取消投产应用
	 * 
	 * @param application_id
	 *            应用id
	 * @param release_node_name
	 *            投产点名称
	 * @return 是否删除
	 * @throws Exception
	 */
	public void deleteApplication(String application_id, String release_node_name) throws Exception;


	/**
	 * 查询投产窗口下所有投产应用
	 * 
	 * @param release_node_name
	 *            投产窗口名称
	 * @return 投产应用列表
	 * @throws Exception
	 */
	List<Map<String, Object>> queryApplications(String release_node_name) throws Exception;

	/**
	 * 查询投产应用详情
	 * 
	 * @param releaseApplication
	 *            投产点名称 应用id
	 * @return 投产应用对象
	 * @throws Exception
	 */
	ReleaseApplication queryApplicationDetail(ReleaseApplication releaseApplication) throws Exception;

	/**
	 * 查询投产应用UAT环境
	 * 
	 * @param applicationId
	 *            投产应用id
	 * @param release_branch
	 *            release分支名
	 * @return uat环境地址
	 * @throws Exception
	 */

	public ReleaseApplication queryAppByIdAndBranch(String applicationId, String release_branch) throws Exception;

	/**
	 * 修改投产应用
	 * 
	 * @param releaseApplication
	 * @return
	 * @throws Exception
	 */
	ReleaseApplication updateReleaseApplication(ReleaseApplication releaseApplication) throws Exception;

	/**
	 * 查询唯一的releaseAppliction
	 * 
	 * @param application_id
	 *            应用id
	 * @param release_node_name
	 *            投产窗口名
	 * @return ReleaseApplication
	 * @throws Exception
	 */
	ReleaseApplication findOneReleaseApplication(String application_id, String release_node_name)
			throws Exception;

	/**
	 * 若未传appcation_id 则通过gitlab_projec_id查询
	 * 
	 * @param application_id
	 *            应用id
	 * @param gitlab_project_id
	 *            git项目id
	 * @return application_id
	 * @throws Exception
	 */
	String queryApplicationId(String application_id, Integer gitlab_project_id) throws Exception;

	/**
	 * 插入ReleaseApplication
	 * 
	 * @param releaseApplication
	 * @return ReleaseApplication对象
	 * @throws Exception
	 */
	ReleaseApplication saveReleaseApplication(ReleaseApplication releaseApplication) throws Exception;

	/**
	 * 查询投产应用REL环境
	 * 
	 * @param applicationId 投产应用id
	 * @param product_tag tag分支名
	 * @return uat环境地址
	 * @throws Exception
	 */

	public ReleaseApplication queryAppByIdAndTag(String applicationId, String product_tag) throws Exception;

	
	/**
	 * 查询任务关联项列表
	 * @param ids 任务id列表
	 * @return
	 * @throws Exception
	 */
	List queryTasksReviews(List<String> ids) throws Exception;

	List<String> queryApplicationTags(String release_node_name, String application_id) throws Exception;

	void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id, String release_branch) throws Exception;

	List<Map> queryBatchApplications(String release_node_name) throws Exception;
	
	void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;

	void updateReleaseApplicationConfig(ReleaseApplication releaseApplication);

	ReleaseApplication updateConfigConfirm(ReleaseApplication releaseApplication);

	List<Map<String, Object>> queryApplicationsByReleaseNodeName(String release_node_name, String question_no, List<String> systemIds, List<String> applicationIds) throws Exception;

	void updateApplicationPath(ReleaseApplication application);

	String sourceMapTar(String application_id, String tag, String release_node_name, List<String> path, String rel_env_name);

	void saveApplicationTag(String product_tag, String revertsqlpro,  String revertsqlgray, Integer gitlab_project_id, MultipartFile[] files) throws Exception;

	/**
	 * 查询投产窗口下所有投产组件
	 * @return 投产应用列表
	 * @throws Exception
	 */
	List<Map<String, Object>> queryComponent(String release_node_name,String type) throws Exception;
	
	/**
	 * 查询投产窗口下所有投产组件
	 * @return 投产应用列表
	 * @throws Exception
	 */
	List<String> queryTargetVersion(Map<String, String> requestParam) throws Exception;
}

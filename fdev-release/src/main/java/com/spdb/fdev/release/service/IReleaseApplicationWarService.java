package com.spdb.fdev.release.service;

/**
 * ReleaseApplication接口类
 *
 */
public interface IReleaseApplicationWarService {

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


}

package com.spdb.fdev.fuser.spdb.service;


public interface GitlabService {
	
	/**
     * 查询gitlab中的username
     * @return
	 * @throws Exception 
     */
	String queryGitlabUsername(String git_user_id) throws Exception;

	void removeGitlabUser(String git_user_id, String token) throws Exception;

	Object queryGitlabUserOfGroup(String git_user_id, String token) throws Exception;
}

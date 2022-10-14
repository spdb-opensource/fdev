package com.manager.ftms.service;

public interface FunctionPointService {
	
	public String queryFuncIdByMenu(String funcMenu,String sys_id) throws Exception;

	public String queryFuncIdByNameAndPid(String funcName, String parentId, String sys_id) throws Exception;

}

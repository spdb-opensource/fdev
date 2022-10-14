package com.manager.ftms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manager.ftms.dao.FunctionPointDao;
import com.manager.ftms.service.FunctionPointService;
@Service
public class FunctionPointServiceImpl implements FunctionPointService{
	@Autowired
	private FunctionPointDao functionPointDao;

	@Override
	public String queryFuncIdByMenu(String funcMenu,String sys_id) throws Exception {
		return functionPointDao.queryFuncIdByMenu(funcMenu,sys_id);
	}

	@Override
	public String queryFuncIdByNameAndPid(String funcName, String parentId, String sys_id) throws Exception {
		return functionPointDao.queryFuncIdByNameAndPid(funcName, parentId, sys_id);
	}
	
	
}

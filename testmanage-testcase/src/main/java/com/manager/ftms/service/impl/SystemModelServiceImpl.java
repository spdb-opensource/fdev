package com.manager.ftms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manager.ftms.dao.SystemModelDao;
import com.manager.ftms.service.ISystemModelService;
@Service
public class SystemModelServiceImpl implements ISystemModelService{
	@Autowired
	private SystemModelDao systemModelDao;

	@Override
	public String querySysIdBySysName(String system_name) throws Exception {

		return systemModelDao.querySysIdBySysName(system_name);
	}

}

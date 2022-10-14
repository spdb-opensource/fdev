package com.testmanage.admin.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.testmanagecommon.exception.FtmsException;
import com.testmanage.admin.entity.SystemModel;
import com.testmanage.admin.mapper.SystemModelMapper;
import com.testmanage.admin.service.ISystemModelService;
import com.testmanage.admin.util.ErrorConstants;

@Service
public class SystemModelServiceImpl implements ISystemModelService {

    @Autowired
    private SystemModelMapper systemModelMapper;

    @Override
    public SystemModel query(Integer sys_func_id) throws Exception {
    	try {
    		return systemModelMapper.query(sys_func_id);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
    }

    @Override
    public List<SystemModel> listAll() throws Exception {
    	try {
    		return systemModelMapper.listAll();
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
    }

	@Override
	public SystemModel queryBySystemName(String systemName) throws Exception {
		return systemModelMapper.queryBySystemName(systemName);
	}

	@Override
	public List<Map<String, Object>> listAllToMap() throws Exception {
		try {
			return systemModelMapper.listAllToMap();
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
	}

}

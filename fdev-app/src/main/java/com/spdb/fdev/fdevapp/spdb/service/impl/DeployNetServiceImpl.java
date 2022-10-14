package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.fdevapp.spdb.dao.IDeployNetDao;
import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;
import com.spdb.fdev.fdevapp.spdb.service.IDeployNetService;

@Service
public class DeployNetServiceImpl implements IDeployNetService {

	@Autowired
	private IDeployNetDao deployNetDao;

	@Override
	public DeployNet save(DeployNet deployNet) throws Exception {
		return deployNetDao.save(deployNet);
	}

	@Override
	public List<DeployNet> query(DeployNet deployNet) throws Exception {
		return deployNetDao.query(deployNet);
	}

	@Override
	public DeployNet update(DeployNet deployNet) throws Exception {
		return deployNetDao.update(deployNet);
	}

	@Override
	public DeployNet findById(String id) throws Exception {
		return deployNetDao.findById(id);
	}

}

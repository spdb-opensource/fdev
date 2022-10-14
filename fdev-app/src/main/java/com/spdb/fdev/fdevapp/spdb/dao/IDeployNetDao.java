package com.spdb.fdev.fdevapp.spdb.dao;

import java.util.List;

import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;

public interface IDeployNetDao {
	public DeployNet save(DeployNet appDeployment) throws Exception;

	public List<DeployNet> query(DeployNet appDeployment) throws Exception;

	public DeployNet update(DeployNet appDeployment) throws Exception;

	public DeployNet findById(String id) throws Exception;
}

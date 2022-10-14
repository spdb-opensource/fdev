package com.spdb.fdev.fdevapp.spdb.service;

import java.util.List;

import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;

public interface IDeployNetService {
	 	public DeployNet save(DeployNet deployNet)  throws Exception;

	    public List<DeployNet> query(DeployNet deployNet) throws Exception;

	    public DeployNet update(DeployNet deployNet) throws Exception;

	    public DeployNet findById(String id) throws Exception;

}

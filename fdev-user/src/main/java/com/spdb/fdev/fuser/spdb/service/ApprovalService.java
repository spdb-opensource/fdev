package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.NetApproval;

import java.util.List;
import java.util.Map;

public interface ApprovalService {
	
	public Map queryApprovalList(Map param) throws Exception;

	public void addApprovalByUser(NetApproval netApproval) throws Exception;

	public List<Map> queryApproval(NetApproval netApproval) throws Exception;

	public void updateApprovalStatus(NetApproval netApproval) throws Exception;
	public void updateApproval(Map param) throws Exception;

	public void addOffFlagAndNotify() throws Exception;
	
	public void sendKfOffEmail() throws Exception;
}

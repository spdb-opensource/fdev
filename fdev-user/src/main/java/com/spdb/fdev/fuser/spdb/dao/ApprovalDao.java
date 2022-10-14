package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fuser.spdb.entity.user.NetApproval;

public interface ApprovalDao {
	
	public Map queryApprovalList(Map param);
	
	public List<String> queryUserIdsByCompany(Map param);

	public void addApprovalByUser(NetApproval approval);

	public List<Map> queryApproval(NetApproval approval);

	public void updateApprovalStatus(NetApproval approval) throws Exception;


	public int updateApproval(Map param);

	public List<Map> queryWaitApproveList(Map param);
	
	public int addAllOffFlag();
}

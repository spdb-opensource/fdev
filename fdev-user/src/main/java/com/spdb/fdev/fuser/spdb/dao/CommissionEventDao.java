package com.spdb.fdev.fuser.spdb.dao;


import java.util.List;
import java.util.Map;

import com.spdb.fdev.fuser.spdb.entity.user.UserCommissionEvent;

public interface CommissionEventDao {

	UserCommissionEvent addCommissionEvent(UserCommissionEvent userCommissionEvent)throws Exception;

	UserCommissionEvent updateByTargetIdAndType(UserCommissionEvent userCommissionEvent) throws Exception;
	
	UserCommissionEvent queryDetailBytargetIdAndType(String target_id,String type,String module)throws Exception;
	
	List<UserCommissionEvent> queryCommissionEvent(UserCommissionEvent userCommissionEvent)throws Exception;

	List<UserCommissionEvent> queryCommissionEventByStatus(UserCommissionEvent userCommissionEvent)throws Exception;

	UserCommissionEvent queryEventById(String id)throws Exception;

	UserCommissionEvent updateLabelById(UserCommissionEvent newEvent)throws Exception;

	Long countLabelNum(List<String> userIds, String label)throws Exception;

	Long totalLabelNum(List<String> userIds)throws Exception;

	UserCommissionEvent upsertCommissionEvent(UserCommissionEvent commissionEvent,
			UserCommissionEvent userCommissionEvent)throws Exception;

	Long deleteCommissionEventById(String id);
	
	List<UserCommissionEvent> queryListByStatusOrLabel(UserCommissionEvent userCommissionEvent)throws Exception;
}

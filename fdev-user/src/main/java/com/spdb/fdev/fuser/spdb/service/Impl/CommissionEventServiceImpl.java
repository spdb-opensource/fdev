package com.spdb.fdev.fuser.spdb.service.Impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.spdb.dao.CommissionEventDao;
import com.spdb.fdev.fuser.spdb.entity.user.UserCommissionEvent;
import com.spdb.fdev.fuser.spdb.service.CommissionEventService;

@Service
public class CommissionEventServiceImpl implements CommissionEventService {

	
	@Autowired
	private CommissionEventDao commissionEventDao;

	@Override
	public UserCommissionEvent addCommissionEvent(UserCommissionEvent userCommissionEvent) throws Exception{
		
		return commissionEventDao.addCommissionEvent(userCommissionEvent);
		
	}

	@Override
	public UserCommissionEvent updateByTargetIdAndType(UserCommissionEvent userCommissionEvent) throws Exception{
		return commissionEventDao.updateByTargetIdAndType(userCommissionEvent);
	}

	@Override
	public UserCommissionEvent queryDetailBytargetIdAndType(String target_id,String type,String module) throws Exception {
		return commissionEventDao.queryDetailBytargetIdAndType(target_id,type,module);
	}

	@Override
	public List<UserCommissionEvent> queryCommissionEvent(UserCommissionEvent userCommissionEvent) throws Exception{
		return commissionEventDao.queryCommissionEvent(userCommissionEvent);
	}

	@Override
	public List<UserCommissionEvent> queryCommissionEventByStatus(UserCommissionEvent userCommissionEvent) throws Exception {
		return commissionEventDao.queryCommissionEventByStatus(userCommissionEvent);
	}

	@Override
	public UserCommissionEvent queryEventById(String id)throws Exception {
		return commissionEventDao.queryEventById(id);
	}

	@Override
	public UserCommissionEvent updateLabelById(UserCommissionEvent newEvent)throws Exception {
		return commissionEventDao.updateLabelById(newEvent);
	}

	@Override
	public Long countLabelNum(List<String> userIds, String label)throws Exception {
		return commissionEventDao.countLabelNum(userIds, label);
	}

	@Override
	public Long totalLabelNum(List<String> userIds)throws Exception {
		return commissionEventDao.totalLabelNum(userIds);
	}

	@Override
	public UserCommissionEvent upsertCommissionEvent(UserCommissionEvent commissionEvent,
			UserCommissionEvent userCommissionEvent) throws Exception {
		return commissionEventDao.upsertCommissionEvent(commissionEvent,userCommissionEvent);
	}

	@Override
	public Long deleteCommissionEventById(String id) {
		return commissionEventDao.deleteCommissionEventById(id);
	}

	@Override
	public List<UserCommissionEvent> queryListByStatusOrLabel(UserCommissionEvent userCommissionEvent)
			throws Exception {
		return commissionEventDao.queryListByStatusOrLabel(userCommissionEvent);
	}
}

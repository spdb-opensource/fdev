package com.manager.ftms.service.impl;

import com.manager.ftms.dao.WorkOrderMapper;
import com.manager.ftms.entity.WorkOrder;
import com.manager.ftms.service.IUserService;
import com.manager.ftms.service.WorkOrderService;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkOrderServiceImpl.class);
	@Autowired
	private WorkOrderMapper workOrderMapper;
	@Autowired
	private IUserService userCheckService;

	@Override
	public Map<String, Object> queryTaskNameTestersByNo(String workOrderNo) throws Exception {
		WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
		Map<String, Object> map = new HashMap<>();
		StringBuilder testers = new StringBuilder();
		if(!Util.isNullOrEmpty(workOrder.getTesters())){
			String[] names = workOrder.getTesters().split(",");
			for(String name : names){
				try {
					Map user = userCheckService.queryUserCoreData(name);
				    if(!Util.isNullOrEmpty(user)){
						testers.append(user.get(Dict.USER_NAME_CN)+ "  ");
					}
				} catch (Exception e) {
					throw new FtmsException(ErrorConstants.SELECT_DATA_EXCEPTION);
				}
			}
		}
		map.put(Dict.TESTERS, testers.toString().trim());
		map.put(Dict.WORKORDER, workOrder);
		return map;
	}
}

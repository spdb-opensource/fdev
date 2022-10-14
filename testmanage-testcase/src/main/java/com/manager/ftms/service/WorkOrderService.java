package com.manager.ftms.service;

import java.util.Map;

public interface WorkOrderService {

   Map<String, Object> queryTaskNameTestersByNo(String workOrderNo) throws Exception;

}

package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.List;
import java.util.Map;

public interface InterfaceLazyInitService {

    String getAppIdByName(String name);

    String getAppManagers(String appName);
	/**
     * 获取当前小组的所有应用
     * @param groupId
     */
	List<Map<String, Object>> queryPagination(String groupId);
	/**
     * 获取应用所属小组
     * @param serviceId
     */
	String getAppGroupName(String serviceId);

}

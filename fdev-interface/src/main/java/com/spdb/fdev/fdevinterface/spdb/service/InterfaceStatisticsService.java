package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevinterface.spdb.entity.InterfaceStatistics;

/**
 * 接口统计
 * @author xxx
 *
 */
public interface InterfaceStatisticsService {
	void saveInterfaceStatistics(List<InterfaceStatistics> list);
	
	void deleteInterfaceStatistics(String serviceName);
	/**
	 * 查询fdev接口统计信息
	 * @param params
	 * @return
	 */
	Map queryInterfaceStatistics(Map params);
	
	/**
	 * 扫描fdev内部项目url改动
	 * @param projectName
	 */
	void scanInterfaceStatistics(String projectName);
	
	/**
	 * 初始化接口统计数据
	 * @return
	 */
	void initList();
	
}

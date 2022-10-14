package com.spdb.fdev.fdevinterface.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevinterface.spdb.entity.InterfaceStatistics;

/**
 * 接口统计dao
 * @author xxx
 *
 */
public interface InterfaceStatisticsDao {
	/**
	 * 保存接口统计数据
	 * @param list
	 */
	void saveInterfaceList(List<InterfaceStatistics> list);
	
	/**
	 * 清除数据库所有接口统计数据，批量更新接口统计数据时使用，请勿手动调用
	 */
	void clearInterfaceList(String projectName);
	
	/**
	 * 根据条件查询接口统计数据列表
	 * @param params
	 * @return
	 */
	Map queryInterfaceList(Map params);
}

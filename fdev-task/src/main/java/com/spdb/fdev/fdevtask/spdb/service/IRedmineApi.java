package com.spdb.fdev.fdevtask.spdb.service;

import java.util.Map;

public interface IRedmineApi {

	String getRedmineHost();

	/**根据redmine需求编号，查询redmine信息*/
	public Map queryRedmineInfoByRedmineId(Map param) throws Exception;

	Map getRedmineInfoForExcel(String id);

	Boolean updateRedMineDate(String url);

	Map redMineRest(String url);
}

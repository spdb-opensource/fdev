package com.spdb.fdev.fdevapp.spdb.dao;

import java.util.List;
import java.util.Map;

public interface ICommonDao {
	/**
	     *   通用单表查询
	 * @param parmMap
	 * @param operator 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public <T>List<T> commonQuery(Map<Object,Object> parmMap,String operator,Class<T> clazz) throws Exception;
}

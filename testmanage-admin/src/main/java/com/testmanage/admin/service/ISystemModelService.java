package com.testmanage.admin.service;

import java.util.List;
import java.util.Map;

import com.testmanage.admin.entity.SystemModel;

public interface ISystemModelService {
    /**
     * 根据系统id查询系统信息
     * @param sys_id
     * @return
     * @throws Exception
     */
    SystemModel query(Integer sys_id) throws Exception;

    /**
     * 查询系统列表
     * @return
     * @throws Exception
     */
    List<SystemModel> listAll() throws Exception;

    /**
     * 根据系统名称查询系统详情
     * @param systemName
     * @return
     */
	SystemModel queryBySystemName(String systemName)throws Exception;

	/**
	 * 查询全部系统列表
	 * @return
	 */
	List<Map<String, Object>> listAllToMap()throws Exception;


}

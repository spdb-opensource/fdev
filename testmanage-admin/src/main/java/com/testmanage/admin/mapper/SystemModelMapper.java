package com.testmanage.admin.mapper;

import java.util.List;
import java.util.Map;

import com.testmanage.admin.entity.SystemModel;


public interface SystemModelMapper {

    SystemModel query(Integer sys_func_id) throws Exception;

    List<SystemModel> listAll()throws Exception;

	SystemModel queryBySystemName(String systemName)throws Exception;

	List<Map<String, Object>> listAllToMap()throws Exception;


}

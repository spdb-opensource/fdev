package com.manager.ftms.dao;

import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Repository
public interface FunctionPointDao {

	String queryFuncIdByMenu(@Param("funcMenu")String funcMenu,@Param("sys_id")String sys_id) throws Exception;

	String queryFuncIdByNameAndPid(@Param("funcName")String funcName, @Param("parentId")String parentId,@Param("sys_id")String sys_id) throws Exception;

	Map<String, Object> queryParentByFuncName(@Param("funcId")Integer funcId) throws Exception;
}

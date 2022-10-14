package com.manager.ftms.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemModelDao {

	String querySysIdBySysName(@Param("system_name")String system_name) throws Exception;

	String querySysNameByFuncId(@Param("funcId")String funcId) throws Exception;
}

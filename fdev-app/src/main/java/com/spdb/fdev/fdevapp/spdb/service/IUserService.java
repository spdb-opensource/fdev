package com.spdb.fdev.fdevapp.spdb.service;

import java.util.Map;

public interface IUserService {

	 /**
     * 查询该组第三层级组
     * @param group_id
     * @return
     * @throws Exception
     */
    Map<String, Object> getThreeLevelGroup(String group_id) throws Exception;

   
}

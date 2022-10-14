package com.gotest.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GroupMapper {

    Map<String,String> queryAutoWorkOrder(@Param("groupId") String groupId) throws Exception;

	String queryUatContact(@Param("groupId")String groupId) throws Exception;

}

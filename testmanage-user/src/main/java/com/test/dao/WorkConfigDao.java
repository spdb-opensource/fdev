package com.test.dao;

import com.test.entity.WorkConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WorkConfigDao {

	Integer updateWorkConfig (WorkConfig workConfig) throws Exception;
	
	WorkConfig queryWorkLeader(@Param("group_id") String group_id) throws Exception;
	
	WorkConfig queryGroupLeader (@Param("group_id") String group_id) throws Exception;
	
	Integer insertWorkConfig(@Param("group_id") String group_id,
                             @Param("work_leader") String work_leader,
                             @Param("group_leader") String group_leader,
                             @Param("uatContactId") String uatContactId,
							 @Param("securityLeader") String securityLeader) throws Exception;

	WorkConfig queryCurrentUatContact(@Param("group_id")String group_id) throws Exception;

	void batchWorkLeader() throws Exception;

	void batchUatContact() throws Exception;

	List<Map<String, String>> queryGroupLeaderAll() throws Exception;

	List<Map<String, String>> ftmsUserIdAndCode() throws Exception;

	void setNameEn(@Param("fdevGroupId") String fdevGroupId, @Param("nameEn") String nameEn) throws Exception;

	List<Map<String, String>> queryAlreadyAllocated() throws Exception;

    List<Map<String, String>> getUserRoleLevelMantisToken() throws Exception;
}

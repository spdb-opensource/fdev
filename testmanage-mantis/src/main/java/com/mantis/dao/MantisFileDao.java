package com.mantis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MantisFileDao {

	List<Map<String, Object>> queryIssueFiles(@Param("id")String id) throws Exception;

	void deleteFile(@Param("file_id")String file_id) throws Exception;

	Map<String,Object> queryFileById(@Param("id")String id)throws Exception;

	List<Map<String, Object>> queryIssueFilesExcludeContent(@Param("id")String id);
}

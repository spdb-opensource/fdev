package com.mantis.service;

import java.util.List;
import java.util.Map;

public interface MantisFileService {

	void addFile(Map<String, Object> map) throws Exception;

	void deleteFile(Map<String, String> map) throws Exception;

	List<Map<String,Object>> queryIssueFiles(String id) throws Exception;

}

package com.test.service;

import java.util.List;
import java.util.Map;

import com.test.entity.User;

public interface MantisUserService {

	void addMantisUser(User user) throws Exception;
	
	void branchAddMantisUser() throws Exception;
	
	Map<String,Integer> countReporterSum(String startDate, String endDate, List<String> collect) throws Exception;

}

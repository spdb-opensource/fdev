package com.manager.ftms.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.manager.ftms.entity.Testcase;

public interface BatchAddService {

	void downloadTemplate(HttpServletResponse resp)throws Exception;

	void batchAdd(String planId, String workNo, MultipartFile file,HttpServletResponse resp) throws Exception;
	
	void addTestcase(Testcase testcase) throws Exception;

    void supplementTestcaseExeRecord() throws  Exception;
}

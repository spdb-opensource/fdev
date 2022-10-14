package com.manager.ftms.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ExportExcelService {
	//案例库导出
	void generateCaseExcel(Map map, HttpServletResponse resp) throws Exception;
	
	void generateProdExcel(Map map, HttpServletResponse resp) throws Exception;

	//组装excel并上送指定路径
	String sendCaseExcelToPath(List<Map<String, String>> plans) throws  Exception;
}




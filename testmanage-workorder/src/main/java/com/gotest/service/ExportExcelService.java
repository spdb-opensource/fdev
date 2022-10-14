package com.gotest.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExportExcelService {

	void generateProdExcel(Map dateMap, HttpServletResponse resp) throws Exception;

    void exportUserAllOrder(List<Map> userOrderList, HttpServletResponse resp) throws Exception;

    void qualityReportExport(HttpServletResponse response, List<Map<String, Object>> result) throws IOException;

    void qualityReportNewUnitExport(HttpServletResponse response, Map<String, Object> qualityReportNewUnit, String reportType) throws IOException;
}




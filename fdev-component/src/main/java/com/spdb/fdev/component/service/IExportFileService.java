package com.spdb.fdev.component.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IExportFileService {

    void exportExcelByComponent(List<Map> result, Map<String, String> titleMap, String sheetName, HttpServletResponse resp);

    void exportExcel(List<Map> dataList, Map<Integer, String> titleMap, String sheetName, HttpServletResponse resp,String fileName);
}

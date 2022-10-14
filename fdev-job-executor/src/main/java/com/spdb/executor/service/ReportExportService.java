package com.spdb.executor.service;

import java.util.List;
import java.util.Map;

public interface ReportExportService {

    public String weekUserResourceToPath(String filePath, Map<String, List<Map>> excelDate);

    public String weekGroupResourceToPath(String filePath, Map<String, List<Map>> excelDate);


}

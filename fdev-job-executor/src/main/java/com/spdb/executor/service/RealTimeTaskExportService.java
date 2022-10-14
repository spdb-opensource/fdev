package com.spdb.executor.service;

import java.util.List;
import java.util.Map;

public interface RealTimeTaskExportService {
    String realTimeTaskExport(List<Map> creatorRealTimeTasks, List<Map> testerRealTimeTasks, List<Map> developRealTimeTasks, List<Map> masterRealTimeTasks, List<Map> spdb_managerRealTimeTasks, String filePath);
}

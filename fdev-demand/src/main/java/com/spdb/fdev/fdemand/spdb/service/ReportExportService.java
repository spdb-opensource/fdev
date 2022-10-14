package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;

import java.util.List;
import java.util.Map;

public interface ReportExportService {

    String emailDemandExport(List<DemandBaseInfo> demand, List<Map> queryUser, String flag, String groupName, String filePath);
}

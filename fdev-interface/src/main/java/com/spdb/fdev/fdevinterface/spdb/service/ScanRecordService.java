package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface ScanRecordService {

    void save(ScanRecord scanRecord);

    Map getScanRecord(String serviceId, String branch, String type, Integer page, Integer pageNum, String startTime,
			String endTime, String groupId, String isScanSuccessFlag, String recentlyScanFlag);

	String createExcel(List<ScanRecord> scanRecordList);

	void exportFile(String scanRecordFileName, HttpServletResponse response) throws Exception;

	void timingClearScanRecord();


}

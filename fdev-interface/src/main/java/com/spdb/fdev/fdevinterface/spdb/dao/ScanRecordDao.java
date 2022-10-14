package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ScanRecordDao {

    void save(ScanRecord scanRecord);

    Map getScanRecord(String serviceId, String branch, String type,  String startTime, String endTime, List<String> serviceIdList, String recentlyScanFlag);
    
    int clearScanRecord();

	int timingClearScanRecord();

}

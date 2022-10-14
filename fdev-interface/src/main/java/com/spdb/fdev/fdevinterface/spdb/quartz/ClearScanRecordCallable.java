package com.spdb.fdev.fdevinterface.spdb.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.spdb.fdev.fdevinterface.spdb.dao.ScanRecordDao;

@Component
public class ClearScanRecordCallable implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(ClearScanRecordCallable.class);
	
	@Autowired
	private ScanRecordDao scanRecordDao;
	
	@Override
	public void run() {
		logger.info("===============================开始执行扫描记录清理！");
		long currentTimeMillis = System.currentTimeMillis();
		int clearScanRecord = scanRecordDao.clearScanRecord();
		logger.info("===============================扫描记录清理执行完毕！清理了数据条数：{},耗时：{}",clearScanRecord,System.currentTimeMillis()-currentTimeMillis);
	}

}

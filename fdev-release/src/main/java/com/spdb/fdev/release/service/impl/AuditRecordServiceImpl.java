package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.IAuditRecordDao;
import com.spdb.fdev.release.entity.AuditRecord;
import com.spdb.fdev.release.service.IAuditRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class AuditRecordServiceImpl implements IAuditRecordService{
	@Resource
	private IAuditRecordDao auditRecordDao;

	@Override
	public void auditRecord(AuditRecord auditRecord) throws Exception {
		auditRecordDao.auditRecord(auditRecord);
	}

	@Override
	public String queryRecordOperation(AuditRecord auditRecord) throws Exception {
		return auditRecordDao.queryRecordOperation(auditRecord);
	}

	@Override
	public void updateNode(String old_release_node_name, String release_node_name) {
		auditRecordDao.updateNode(old_release_node_name,release_node_name);
	}

	@Override
	public void save(AuditRecord auditRecord) {
		auditRecordDao.save(auditRecord);
	}

}

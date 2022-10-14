package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.AuditRecord;
/**
 * 审核记录保存
 * 2019年3月28日
 */
public interface IAuditRecordDao {
	/**
	 * 添加审查记录
	 * @param auditRecord
	 * @throws Exception
	 */
	void auditRecord(AuditRecord auditRecord) throws Exception;
	/**
	 * 查询审核记录中的发起人
	 * @param auditRecord taskid release_node_name operation_type
	 * @return operator_name_en
	 * @throws Exception
	 */
	String queryRecordOperation(AuditRecord auditRecord) throws Exception;
	
	void updateNode(String old_release_node_name, String release_node_name);

	void save(AuditRecord auditRecord);
}

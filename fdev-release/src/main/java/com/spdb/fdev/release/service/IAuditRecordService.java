package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.AuditRecord;
/**
 * 添加审查记录
 * 2019年4月1日
 */
public interface IAuditRecordService {
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

	/**
	 * 添加审核记录，dao层不加任何参数
	 * @param auditRecord 审核记录
	 */
	void save(AuditRecord auditRecord);
}

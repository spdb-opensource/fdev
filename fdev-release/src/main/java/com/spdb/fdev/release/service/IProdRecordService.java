
package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdRecord;

import java.util.List;
import java.util.Map;

public interface IProdRecordService {
	/**
	 * 保存变更记录
	 * @param prodRecord
	 * @return
	 * @throws Exception
	 */
	ProdRecord create(ProdRecord prodRecord) throws Exception;
	/**
	 * 查询变更记录
	 * @param release_node_name 投产窗口名
	 * @return
	 * @throws Exception
	 */
	List query(String release_node_name) throws Exception;
	/**
	 * 投产模版id 查询变更记录列表
	 * @param template_id 模版id
	 * @return
	 * @throws Exception
	 */
	List queryByTemplateId(String template_id) throws Exception;
	/**
	 * 查询变更模版详情
	 * @param prod_id 变更id
	 * @return
	 * @throws Exception
	 */
	ProdRecord queryDetail(String prod_id) throws Exception;
	/**
	 * 查询模版精简信息
	 * @param prod_id 变更id
	 * @return
	 * @throws Exception
	 */
	ProdRecord queryTrace(String prod_id) throws Exception;
	/**
	 * 审核变更记录
	 * @param prod_id 变更id
	 * @param audit_type 审核类型
	 * @param reject_reason 拒绝理由
	 * @param templte_properties 模版配置文件
	 * @return
	 * @throws Exception
	 */
	Map audit(String prod_id, String audit_type, String reject_reason, String templte_properties) throws Exception;
	/**
	 * 根据版本号和投产窗口名查询变更记录
	 * @param release_node_name
	 * @param version
	 * @return
	 * @throws Exception
	 */
	ProdRecord queryProdRecordByVersion(String release_node_name, String version) throws Exception;
	/**
	 * 设置变更模版
	 * @param prod_id 变更记录
	 * @param template_id 模版id
	 * @return
	 * @throws Exception
	 */
	void setTemplate(String prod_id, String template_id, String application_id) throws Exception;
	/**
	 * 更新自动化投产日志
	 * @param prod_id
	 * @param auto_release_log
	 * @return
	 * @throws Exception
	 */
	ProdRecord updateAutoReleaseLog(String prod_id, String auto_release_log) throws Exception;
	/**
	 * 更新自动化投产阶段
	 * @param prod_id
	 * @param rel_stage
	 * @return
	 * @throws Exception
	 */
	ProdRecord updateAutoReleaseStage(String prod_id, String rel_stage) throws Exception;
	
	/**
	 * 查询该投产窗口下该变更版本之前的所有镜像标签
	 * @param release_node_name  投产窗口名
	 * @param version 变更版本
	 * @return
	 * @throws Exception
	 */
	Map<String, String> queryBeforePordImages(String release_node_name, String version)throws Exception;
	
	/**
	 * 更新变更记录
	 * @param requestParam
	 * @return
	 */
	void update(Map<String, String> requestParam) throws Exception;
	
	/**
	 * 删除变更
	 * @param prod_id
	 * @throws Exception
	 */
	void delete(String prod_id) throws Exception;
	/**
	 * 根据版本查询变更
	 * @return
	 * @throws Exception
	 */
	ProdRecord queryProdByVersion(String version) throws Exception;
	
	/**
	 * 条件为时间间隔查询变更列表
	 * @param start_date
	 * @param end_date
	 * @return
	 * @throws Exception
	 */
	List<ProdRecord> queryPlan(String start_date, String end_date) throws Exception;
	/**
	 * 根据总介质目录查询相关信息
	 * @param prod_assets_version
	 * @return
	 */
	Map<String, String> queryProdInfo(String prod_assets_version) throws Exception;

	void updateReleaseNodeName(String release_node_name, String old_release_node_name) throws Exception;

	ProdRecord queryEarliestByProdSpdbNo(String prod_spdb_no);

	void updatePordAssetsVersionByProdSpdbNo(String prod_spdb_no, String prod_assets_version);

	void updateProdStatus(String prod_id, String status) throws Exception;

	/**
	 * 判断删除的项目中是否存在已投产的应用。如果没有将new_add_sign新增应用标识改为1
	 * @param
	 */
    String checkApplication(ProdRecord prodRecord) throws Exception;

	Boolean checkAddSign(String application_id, ProdRecord prodRecord) throws Exception;

	void updateAwsAssetGroupId(String prodId, String groupId);
}

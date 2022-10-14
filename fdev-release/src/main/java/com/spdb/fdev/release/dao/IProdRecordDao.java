package com.spdb.fdev.release.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.release.entity.ProdRecord;


public interface IProdRecordDao {

	List queryByTemplateId(String template_id) throws Exception;

	ProdRecord create(ProdRecord releaseRecords) throws Exception;

	List query(String release_node_name) throws Exception;

	ProdRecord queryDetail(String id) throws Exception;

	ProdRecord queryTrace(String prod_id) throws Exception;

	ProdRecord audit(String id, String audit_type, String reject_reason) throws Exception;

	ProdRecord queryProdRecordByVersion(String release_node_name, String version) throws Exception;
	
	ProdRecord setTemplate(String prod_id, String template_id) throws Exception;

	ProdRecord updateAutoReleaseLog(String prod_id, String auto_release_log) throws Exception;

	ProdRecord updateAutoReleaseStage(String prod_id, String auto_release_stage) throws Exception;

	ProdRecord updateStatus(String prod_id, String status) throws Exception;
	
	void auditSetTemplate(String prod_id, String audit_type, String reject_reason, String template_properties)throws Exception;

	void saveHostName(String prod_id, String host_name) throws Exception;

	Map<String, String> queryBeforePordImages(String release_node_name, String version)throws Exception;

	void update(Map<String, String> requestParam)throws Exception;

	void delete(String prod_id) throws Exception;

	ProdRecord queryProdByVersion(String version) throws Exception;

	List<ProdRecord> queryPlan(String start_date, String end_date) throws Exception;

	Map<String, String> queryProdInfo(String prod_assets_version) throws Exception;

	void updateReleaseNodeName(String release_node_name, String old_release_node_name);

	ProdRecord queryEarliestByProdSpdbNo(String prod_spdb_no);

	void updatePordAssetsVersionByProdSpdbNo(String prod_spdb_no, String prod_assets_version);

    List<ProdRecord> queryBeforeProdByReleaseNode(ProdRecord prodRecord);

	ProdRecord queryByProdId(String prod_id);

    List<ProdRecord> queryOldProdList(ProdRecord prodRecord, List<String> ids);

	List<Map<String, Object>> queryRiskProd(String appliction_id, String startDate);

    List<ProdRecord> queryProdListByProdAssetsVersion(String prod_assets_version);

	List<ProdRecord> queryByReleaseNodeName(ProdRecord prodRecord);

    void updateAwsAssetGroupId(String prodId, String groupId);
}

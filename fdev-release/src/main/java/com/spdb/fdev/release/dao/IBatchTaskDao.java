package com.spdb.fdev.release.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.NoteSql;

public interface IBatchTaskDao {

	BatchTaskInfo create(BatchTaskInfo batchTaskInfos) throws Exception;
	
	void updateBatchTaskInfo(String id, String type, String executorId, String transName, String jobGroup, String description, String cronExpression, String misfireInstr, String fireTime,String batchInfo) throws Exception;
	
	void deleteBatchTaskInfo(String id) throws Exception;
	
	List<BatchTaskInfo> queryBatchTypeList(Map<String, Object> requestParam) throws Exception;
	
	void updateBatchTaskProdId(String id,String prod_id,String batchNo) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoList(Map<String, Object> requestParam) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoByProdId(String prod_id) throws Exception;

	BatchTaskInfo queryBatchTaskInfoById(String id) throws Exception;
	
	BatchTaskInfo updateNoteBatchNo(String id, String batch_no) throws Exception;
	
	BatchTaskInfo queryBatchTaskInfoByAppId(String id,String application_id) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoByNoteId(String note_id) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoListByAppId(Map<String, Object> requestParam) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoByReleaseNodeName(String release_node_name) throws Exception;
	
	List<BatchTaskInfo> queryBatchTaskInfoByProdIdAndAppId(String prod_id,String application_id) throws Exception;
}

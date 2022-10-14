package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.entity.ReleaseBindRelation;
import com.spdb.fdev.release.entity.ReleaseTask;

import java.util.List;

/**
 * 投产批次数据库接口层
 *
 */
public interface IReleaseBatchDao {

	ReleaseBatchRecord queryBatchRecordByAppId(String releaseNodeName, String appId) throws Exception;

	ReleaseBindRelation queryBindAppByAppId(String releaseNodeName, String appId) throws Exception;

	ReleaseBindRelation queryBindAppByBatchId(String releaseNodeName, String batchId) throws Exception;

	List<String> queryBindPeople(List<String> appIds, String releaseNodeName) throws Exception;

	ReleaseBatchRecord addBatchRecord(String appId, String releaseNodeName, String batchId, String modifyReason, String userNameEn, String userNameCn, String dateStr) throws Exception;

	void updateBindRelation(String releaseNodeName, String batchId, List<String> bindAppIds) throws  Exception;

	ReleaseBindRelation addBindRelation(ReleaseBindRelation releaseBindRelation) throws Exception;

	List<ReleaseTask> batchReleaseTask(String date) throws Exception;

	void updateReleaseTask(ReleaseTask releaseTask) throws Exception;
}
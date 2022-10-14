package com.spdb.fdev.fdevtask.spdb.dao;

import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecord;
import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecordHistory;

import java.util.List;
import java.util.Map;

public interface ReviewRecordDao {

    ReviewRecord save(String _id, String id, String taskName, String taskId, String appName, String appId, List master, String group, String groupName, String status,String reviewType);

    ReviewRecord update(ReviewRecord reviewRecord);

    Map fuzzySearch(String key, int page, int pageSize, Map param);

    Map queryPageable(Map param, int page, int pageSize);

    List<ReviewRecord> query(ReviewRecord reviewRecord);

    ReviewRecord queryById(String id);

    void deleteReviewRecord(String id);

    List<ReviewRecordHistory> queryReviewRecordHistory(ReviewRecordHistory reviewRecordHistory) ;

    ReviewRecordHistory saveReviewRecord(ReviewRecordHistory reviewRecordHistory);

    void deleteByTaskId(String task_id);

    ReviewRecord queryByTaskId(String taskId);

    void deleteByTask_Id(String task_id);

    ReviewRecord queryTaskReviewByTaskId(String task_id);

    ReviewRecord queryRecordByTaskId(String task_id);

    void updateById(ReviewRecord rup);

    ReviewRecord saveNoCodeReview(ReviewRecord reviewRecord);
}

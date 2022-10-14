package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecord;
import com.spdb.fdev.fdevtask.spdb.entity.ReviewRecordHistory;

import java.util.List;
import java.util.Map;

public interface ReviewRecordService {

    Map fuzzyQuery(String key, int page, int pageSize,Map param);

    void saveSqlReview(FdevTask fdevTask) throws Exception;

    ReviewRecord update(ReviewRecord reviewRecord) throws Exception;

    Map queryPageable(Map param, int page, int pageSize);

    Map queryTaskReview(String id);

    void deleteTaskRecord(FdevTask ntask, FdevTask otask);

    void sendMail(Map mapping,  ReviewRecordHistory reviewRecordHistory);

    List<ReviewRecordHistory> queryReviewRecordHistoryByTaskId(String task_id) ;

    void saveReviewRecord(Map request) throws Exception;

    void sendUpdateMail(Map mapping, ReviewRecord rr);

    void deleteByTaskId(String task_id);

    Map queryReviewBasicMsgById(Map request);

    void deleteByTask_Id(String task_id);

    ReviewRecord queryTaskReviewByTaskId(String task_id);

    void addReviewIdea(Map request)throws Exception;

    void addNoCodeReview(Map params) throws Exception;

}

package com.spdb.fdev.codeReview.spdb.dao;

import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
public interface IProblemDao {
    /**
     * 根据会议id查询问题
     * @param meetingId
     * @return
     */
    List<CodeProblem> queryProblemsByMeetingId(String meetingId);

    CodeProblem queryProblemById(String id);

    void deleteProblemById(String id);

    CodeProblem add(CodeProblem problem) throws Exception;

    void update(CodeProblem problem) throws Exception;

    List<CodeProblem> queryProblemsByMeetingIds(Set<String> meetingIds, String startDate, String endDate);

    Map<String, Object> queryAll(Map param);
}

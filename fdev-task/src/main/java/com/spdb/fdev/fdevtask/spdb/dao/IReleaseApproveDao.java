package com.spdb.fdev.fdevtask.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.fdevtask.spdb.entity.ReleaseApprove;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/12/30
 */
public interface IReleaseApproveDao {
    void save(ReleaseApprove releaseApprove);

    Map<String,Object> queryReleaseApproveList(Map param);

    ReleaseApprove queryById(String id);

    ReleaseApprove update(ReleaseApprove releaseApprove) throws JsonProcessingException;

    long count(List status);

    void deleteByTaskId(String taskId);

    long getCountByTaskId(String taskId);

    List<Map> getCountByTaskIds(Set<String> taskIds);
}

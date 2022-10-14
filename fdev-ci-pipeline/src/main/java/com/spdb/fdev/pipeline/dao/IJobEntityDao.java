package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.JobEntity;

import java.util.List;
import java.util.Map;

public interface IJobEntityDao {

    List<Map> queryAllJobs(long skip, int limit, User user, String searchContent) throws Exception;

    JobEntity queryFullJobsById(String id) throws Exception;

    String add(JobEntity jobEntity) throws Exception;

    JobEntity findOldVersion(String nameId) throws Exception;

    void setStatusClose(String id) throws Exception;

    JobEntity updateVisibleRange(String id, JobEntity jobEntity) throws Exception;

    List<JobEntity> findAllByPluginCode(List<String> pluginCodeList) throws Exception;

    void updateStepsInJobEntity(JobEntity jobEntity) throws Exception;

}

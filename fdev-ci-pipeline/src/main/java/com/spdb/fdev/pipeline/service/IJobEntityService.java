package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.JobEntity;

import java.util.List;
import java.util.Map;

public interface IJobEntityService {

    List<Map> getAllJobs(String pageNum, String pageSize, User user, String searchContent) throws Exception;

    JobEntity getJobTemplateInfo(String id) throws Exception;

    String add(JobEntity jobEntity) throws Exception;

    String update(JobEntity jobEntity) throws Exception;

    void delTemplate(String id) throws Exception;

    JobEntity updateVisibleRange(String id,String visibleRange) throws Exception;

    void updateStepsInJobEntity(String nameId,String type) throws Exception;

}

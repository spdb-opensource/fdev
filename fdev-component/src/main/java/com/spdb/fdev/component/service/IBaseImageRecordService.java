package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.BaseImageRecord;

import java.util.List;
import java.util.Map;

public interface IBaseImageRecordService {
    List query(BaseImageRecord baseImageRecord) throws Exception;

    BaseImageRecord queryByNameAndTrialApps(String name, String gitlab_id);

    BaseImageRecord save(Map map) throws Exception;

    BaseImageRecord update(BaseImageRecord baseImageRecord) throws Exception;

    BaseImageRecord queryById(String id);

    void recoverInvalidRecord(String id) throws Exception;

    BaseImageRecord queryByNameAndTag(String name, String tag);

    List<BaseImageRecord> queryByNameStage(String name, String stage);

    void mergedCallBack(String state, Integer iid, Integer project_id) throws Exception;

    void pipiCallBack(String gitlabId, String rel) throws Exception;

    List<BaseImageRecord> queryIssueRecord(String name, String branch, String stage);

    void relasePackage(String name, String branch) throws Exception;

    void mergedMasterCallBack(String state, Integer iid, Integer project_id) throws Exception;
}

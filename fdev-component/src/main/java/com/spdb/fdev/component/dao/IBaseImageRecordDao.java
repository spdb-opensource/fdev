package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.BaseImageRecord;

import java.util.List;
import java.util.Map;

public interface IBaseImageRecordDao {
    List query(BaseImageRecord baseImageRecord) throws Exception;

    BaseImageRecord queryByNameAndTrialApps(String name, String gitlab_id);

    BaseImageRecord save(BaseImageRecord baseImageRecord) throws Exception;

    BaseImageRecord update(BaseImageRecord baseImageRecord) throws Exception;

    BaseImageRecord queryById(String id);

    List<BaseImageRecord> queryByNameStage(String name, String stage);

    void upsert(BaseImageRecord baseImageRecord);

    BaseImageRecord queryByPipId(String pipid);

    BaseImageRecord queryByNameAndTag(String name, String tag);

    List<BaseImageRecord> queryIssueRecord(String name, String branch, String stage);

    void delete(BaseImageRecord baseImageRecord) throws Exception;

    Map<String,Integer> queryIssueData(String startTime, String endTime);
}

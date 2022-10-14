package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;

import java.util.List;
import java.util.Set;

public interface IReleaseRqrmntInfoDao {
    ReleaseRqrmntInfo queryReleaseRqrmntInfo(String release_date, String rqrmnt_no, String group_id) throws Exception;

    void saveReleaseRqrmntInfo(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception;

    ReleaseRqrmntInfo queryReleaseRqrmntInfoByTaskNo(String task_id) throws Exception;

    List<ReleaseRqrmntInfo> queryReleaseRqrmntInfosByTaskNo(String task_id) throws Exception;

    void updateRqrmntInfo(String relase_date, String package_submit_test, String rel_test) throws Exception;

    void updateRqrmntInfoTaskList(String id, List<String> task_ids) throws Exception;

    void deleteRqrmntInfo(String id) throws Exception;

    void addTaskReleaseRqrmntInfo(String id, String task_id, String new_add_sign,String otherSystem,String commonProfile,String dateBaseAlter,String specialCase) throws Exception;

    void deleteTaskReleaseRqrmntInfo(String id, String task_id) throws Exception;

    List<ReleaseRqrmntInfo> queryRqrmntInfoList(String release_date, String type, Set groupIds) throws  Exception;

    List<ReleaseRqrmntInfo> queryEarilyThanTodayRqrmntInfo(String today) throws Exception;

    void updateRqrmntInfoReview(String id, String otherSystem, String commonProfile, String dateBaseAlter, String specialCase) throws Exception;

    ReleaseRqrmntInfo queryRqrmntById(String id) throws Exception;

    void updateRqrmntInfoPackageSubmitTest(String id, String stage) throws  Exception;

    void updateRqrmntInfoRelTest(String id, String stage) throws  Exception;

    void updateRqrmntInfoTag(String id, String tag)throws Exception;

    void deleteReleaseRqrmntInfoTask(String task_id) throws Exception;

    void updateRqrmntInfoFlag(String id, String flag);
}

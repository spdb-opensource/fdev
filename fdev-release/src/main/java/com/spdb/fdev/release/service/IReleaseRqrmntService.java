package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseRqrmnt;

import java.util.List;
import java.util.Map;

public interface IReleaseRqrmntService {

    ReleaseRqrmnt queryByRqrmntId(String release_node_name, String rqrmnt_id, String type);

    ReleaseRqrmnt editRqrmntFile(ReleaseRqrmnt releaseRqrmnt);

    Map<String, Object> queryRqrmntsInfo(String rqrmnt_id) throws Exception;
    /**
     * 根据研发单元查询研发单元详情
     * */
    String queryFdevImplUnitDetail(String fdevUnitId) throws Exception;

    void saveReleaseRqrmnt(ReleaseRqrmnt releaseRqrmnt);

    void deleteById(String id);

    ReleaseRqrmnt deleteRqrmntTask(String task_id, String release_node_name) throws Exception;

    ReleaseRqrmnt addOrEditRqrmntTask(String task_id, String release_node_name) throws Exception;

    List<ReleaseRqrmnt> queryByReleaseNodeName(String release_node_name);

    void updateNode(String old_release_node_name, String release_node_name);

    ReleaseRqrmnt buildRqrmnt(ReleaseRqrmnt releaseRqrmnt);

    Map<String,String> getConfirmRecord(String taskId,String type) throws Exception;

    void changeRqrmntMsg(String task_id) throws Exception;

    void addOrEditRqrmntTaskAsync(String task_id, String release_node_name) throws Exception;
}

package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseRqrmnt;

import java.util.List;

public interface IReleaseRqrmntDao {

    ReleaseRqrmnt queryByRqrmntId(String release_node_name, String rqrmnt_id, String type);

    ReleaseRqrmnt editRqrmntFile(ReleaseRqrmnt releaseRqrmnt);

    void saveReleaseRqrmnt(ReleaseRqrmnt releaseRqrmnt);

    void deleteById(String id);

    List<ReleaseRqrmnt> queryByReleaseNodeName(String release_node_name);

    ReleaseRqrmnt editTaskMap(ReleaseRqrmnt releaseRqrmnt);

    void updateNode(String old_release_node_name, String release_node_name);

    ReleaseRqrmnt queryByReleaseNodeNameAndTaskId(String release_node_name,String taskId);
}

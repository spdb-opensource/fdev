package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseNode;

import java.util.List;
import java.util.Map;
/**
 * 投产窗口数据库接口层
 *
 */
public interface IReleaseNodeDao {
	List<ReleaseNode> getReleaseNodeName(String release_date) throws Exception;

	ReleaseNode delete(ReleaseNode releaseNode) throws Exception;
	
	ReleaseNode update(Map<String, Object> map)throws Exception;

	List<ReleaseNode> queryReleaseNodes(Map<String, String> map)throws Exception;
	
	ReleaseNode queryDetail(String release_node_name) throws Exception;
	
	ReleaseNode create(ReleaseNode releaseNode) throws Exception;

	List<ReleaseNode> queryReleaseNodesByGroupId(List<Map<String, Object>> group_id) throws Exception;

	List<String> archivedNodeByDate(String release_date) throws Exception;

    List<ReleaseNode> threeDaysAgoNode() throws Exception;

	List<ReleaseNode> updateReleaseNodeBatch(String start_date, String end_date) throws Exception;

	ReleaseNode updateBigReleaseNode(ReleaseNode releaseNode, String releaseNodeName);

	List<ReleaseNode> queryReleaseNodesByCondition(String release_date);

	List<ReleaseNode> queryReleaseNodesByGroupIdAndDate(String groupId, String release_date) throws Exception;

    List<ReleaseNode> queryFromDate(String date);

    String queryDateByName(String release_node_name);

	String queryNodes(String application_id, String startDate);
}

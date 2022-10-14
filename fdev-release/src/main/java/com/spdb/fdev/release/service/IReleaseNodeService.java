package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseNode;

import java.util.List;
import java.util.Map;


public interface IReleaseNodeService {
    /**
     * 通过日期查询对应投产窗口
     * @param release_date 日期
     * @return 投产窗口对象
     * @throws Exception
     */
    List<ReleaseNode> getReleaseNodeName(String release_date) throws Exception;
    /**
     * 删除投产窗口
     * @param releaseNode
     * @return
     * @throws Exception
     */
    ReleaseNode delete(ReleaseNode releaseNode) throws Exception;
    /**
     * 更改投产窗口
     * @param map releaseNode 投产窗口对象
     * @return 投产窗口对象
     * @throws Exception
     */
    ReleaseNode update(Map<String, Object> map) throws Exception;
    /**
     * 通过多个条件查询投产窗口列表（投产日期区间、变更编号、科技负责人、投产负责人、小组、窗口状态）
     * @param map 投产窗口对象
     * @return 投产窗口对象列表
     * @throws Exception
     */
    List<ReleaseNode> queryReleaseNodes(Map<String, String> map) throws Exception;
    /**
     * 根据投产点名称查询投产点详情
     * @param release_node_name 投产点名称
     * @return 投产窗口对象
     * @throws Exception
     */
    ReleaseNode queryDetail(String release_node_name) throws Exception;
    /**
     * 创建投产窗口
     * @param releaseNode 投产窗口对象
     * @return 投产窗口对象
     * @throws Exception
     */
    ReleaseNode create(ReleaseNode releaseNode) throws Exception;
    /**
     * 通过组id查询该组及子组的投产窗口
     * @param group_id 组id
     * @return
     * @throws Exception
     */
    List<ReleaseNode> queryReleaseNodesByGroupId(String group_id) throws Exception;
    /**
     * 将更新变化体现在邮件信息中
     * @param the_old 修改前
     * @param the_new 修改后
     * @return
     * @throws Exception
     */
    Map updateNodeChange(ReleaseNode the_old, ReleaseNode the_new) throws Exception;
    
    /**
     * 补齐投产窗口中user信息的中文名
     * @param releaseNode
     * @return
     * @throws Exception
     */
    ReleaseNode completeNodeUserNameCn(ReleaseNode releaseNode)throws Exception;
    
    /**
     * 使传入日期前的所有投产窗口归档
     * @param release_date
     * @return 归档投产窗口名列表
     * @throws Exception
     */
	List<String> archivedNodeByDate(String release_date) throws Exception;

    /**
     * 查询早于当前日期三天前的投产窗口
     * @return 归档投产窗口名列表
     * @throws Exception
     */
	List<ReleaseNode> threeDaysAgoNode() throws Exception;

    List<ReleaseNode> updateReleaseNodeBatch(String start_date, String end_date) throws Exception;

    ReleaseNode updateBigReleaseNode(ReleaseNode releaseNode, String releaseDate);

    List<Map<String, Object>> queryContactInfo(Map<String, String> requestParam) throws Exception;

    List<String> getReleaseSmallNodeNames(String releaseDate);

    ReleaseNode editReleaseDate(String oldReleaseDate, String releaseDate) throws Exception;

    List<ReleaseNode> queryReleaseNodesByGroupIdAndDate(String groupId, String release_date) throws Exception;

    List<ReleaseNode> queryFromDate(String date);

    /**
     * 根据投产窗口名称获取投产时间
     * @param release_node_name
     * @return
     */
    String queryDateByName(String release_node_name);
    /**
     * 根据投产日期区间查询包含条件应用id的最近的一次投产窗口
     * @param application_id 应用id
     * @param startDate 开始时间
     * @return 投产窗口对象
     * @throws Exception
     */
    String queryNodes(String application_id, String startDate);
    /**
     * 获取当前用户组所属第三层级组及子组
     * */
    Map<String,Object> queryThreeLevelGroups(Map<String, String> req) throws Exception;
}
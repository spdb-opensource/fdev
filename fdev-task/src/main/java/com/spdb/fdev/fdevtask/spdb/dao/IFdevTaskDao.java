package com.spdb.fdev.fdevtask.spdb.dao;

import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.entity.GroupTree;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IFdevTaskDao {

    public FdevTask save(FdevTask task) throws Exception;

    public List<FdevTask> query(FdevTask task) throws Exception;

    List<FdevTask> queryAll(FdevTask task) throws Exception;

    public FdevTask update(FdevTask task) throws Exception;

    /* 根据任务id集合查询任务列表 */
    List<FdevTask> queryAllTasksByIds(ArrayList ids) throws Exception;

    public Map<String, Object> queryUserTask(String userId, String nameKey, Integer pageNum, Integer pageSize, Boolean incloudFile) throws Exception;

    public List<Map> queryTaskByTerm(String taskName, String stage) throws Exception;

    public List<FdevTask> queryTasksByIds(List<String> ids) throws Exception;

    public List<FdevTask> queryTasksByIdsNoAbort(List<String> ids) throws Exception;

    public FdevTask updateTaskView(String scope, String viewId, String name, Boolean audit) throws Exception;

    List<FdevTask> queryByTerms(Set<String> groupIds, ArrayList stage, boolean isIncludeChildren, Integer isDefered) throws Exception;

    public FdevTaskCollection saveFdevTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception;

    public List<Map> queryFdevTaskCollection(ArrayList ids);

    public FdevTaskCollection queryByJobId(FdevTaskCollection fdevTaskCollection) throws Exception;

    public List<Map> queryBySubTask(FdevTaskCollection task) throws Exception;

    List<FdevTaskCollection> queryByTaskId(String task_id) throws Exception;

    public FdevTaskCollection updateTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception;

    public List<FdevTask> queryTaskCByUnitNo(String unitno) throws Exception;

    public void removeTask(String id) throws Exception;

    public int queryTaskNum(String id, String startDate, String endDate, GroupTree groupTree) throws Exception;

    public FdevTaskCollection deleteMainTask(String id) throws Exception;

    public Map<String, Object> queryTaskNum(String groupId, GroupTree groupTree) throws Exception;

    Map<String, Object> queryTaskNumByMember(String id, List roles);

    Map<String, Object> queryTaskNumByApp(String appId);

    Map queryTaskAndRedmineNo() throws Exception;

    List<FdevTask> queryTaskGroupByRqrmnt(String rqrmnt) throws Exception;

    List<FdevTask> queryAllTaskExcludeAbort(String rqrmntNo);

    Map<String, Object> queryPostponeTask(Map<String, Object> params, GroupTree groupTree);

    List<FdevTask> queryTaskNumRQR(String id, List roles);

    List<FdevTask> queryTaskNumByGroupinAll(String group, GroupTree groupTree, List stages);

    List<FdevTask> queryTasksByIdsNotinlineTasks(ArrayList taskIds);

    Map<String, Object> queryOutputTaskbyGroup(GroupTree groupTree, String groupId, String startDate, String endDate) throws Exception;

    Map<String, Object> queryOutputTaskbyUser(List roles, String userId, String startDate, String endDate, List<String> demandTypeList, List<Integer> taskTypeList) throws Exception;

    List<FdevTask> queryByrqrmntIds(List<String> rqrmntIds1);
    List<FdevTask> queryByrqrmntIds(List<String> rqrmntIds1,String stage);

    Map<String, Object> queryByTermsAndPage(Set<String> groupIds, ArrayList stage, boolean isIncludeChildren,  Integer isDefered, int page, int per_page) throws Exception;

    List<FdevTask> queryBySitMergeId(String merge_id, String project_id);

    FdevTask selectTaskById(String task_id);

    long updateTaskByBranchAndWebName(String featureId,String webNameEn,String sonarId,String scanTime);

    long updateDesign(Map<String, List<Map<String, String>>> designMap,String taskId);

    Map<String, Object> queryTaskCardByMember(String id, List roles);

    List<FdevTask> queryTaskForDesign();

    List<FdevTask> queryReviewDetailList(String reviewer, List<String> group, String startDate, String endDate);

    Long deferOrRecoverTask(List<String> ids, Integer taskSpecialStatus);

    void updateByFdevImplementUnitNo(String fdev_implement_unit_no, Map implement_unit_info);

    List<FdevTask> queryTaskDetailByfdevImplementUnitNo(List<String> fdev_implement_unit_no_list);

    /**
     * 获取所有的任务，根据研发单元编号
     *
     * @param unitNo
     * @return
     */
    List<FdevTask> queryAllTaskByUnitNo(String unitNo, Boolean isWaitStatus);

    /**
     * 获取所有的任务，根据需求编号
     *
     * @param demandNo
     * @return
     */
    public List<FdevTask> queryAllTaskByDemandNo(String demandNo, Boolean isWaitStatus);

    List<FdevTask> queryTaskByreadmine(String group);

    List<FdevTask> queryTaskIdsByProjectId(String project_id);

    /**
     * 根据任务id获取任务信息
     * @param taskId
     * @return
     */
    FdevTask queryById(String taskId);

    Integer queryNotDiscarddnum(String fdev_implement_unit_no);

    List<FdevTask> queryTaskByStoryId(String storyId);

    List<FdevTask> queryByGroupId(List groupIds);

    long queryTaskNum(Query query);

    <T> List<T> queryByQuery(Query query, Class<T> clazz);

    /**
     * 查询用户有权限操作的已投产未归档任务
     * @param userId
     * @return
     */
    List<FdevTask> queryUserProductionTask(String userId);

    /**
     * 批量任务归档
     * @param ids
     */
    List<FdevTask> updateTaskStateToFileBatch(List<String> ids);

    /**
     * 获取所有的任务，根据研发单元编号
     *
     * @param unitNo
     * @return
     */
    List<FdevTask> queryAllTaskByUnitNo(String unitNo);

    /**
     * 根据研发单元编号查询不是删除废弃状态的任务
     * @param fdev_implement_unit_no_list
     * @return
     */
    List<FdevTask> queryNotDiscardTask(List<String> fdev_implement_unit_no_list);

    /**
     * 获取所有的任务，根据需求编号
     *
     * @param demandNo
     * @return
     */
    List<FdevTask> queryAllTaskByDemandNo(String demandNo);

    List<FdevTask> queryUITask();

    List<FdevTask> queryTaskByCodeOrderNo(String code_order_no);


    /**
     * 查询未结束的任务
     * @return
     */
    List<FdevTask> queryAllTask();

    void updateApplicationType(String taskId, String typeName);

    void updateInnerTestTime(FdevTask task);

    List<FdevTask> queryTaskInStage(List<String> stageList);
}

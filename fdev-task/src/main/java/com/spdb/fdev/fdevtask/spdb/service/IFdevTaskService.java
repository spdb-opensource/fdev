package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.entity.GroupTree;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public interface IFdevTaskService {

    public FdevTask save(FdevTask task) throws Exception;

    List<Map> queryWithName(FdevTask task) throws Exception;

    public FdevTask update(FdevTask task) throws Exception;

    public List<FdevTask> query(FdevTask task) throws Exception;

    public Map<String, Object>  queryUserTask(String userId, String nameKey, Integer pageNum, Integer pageSize, Boolean incloudFile, Set<String> delayStage) throws Exception;

    public List<Map> queryTaskByTerm(String taskName, String stage) throws Exception;

    public List<FdevTask> queryTasksByIds(ArrayList ids) throws Exception;

    public List<FdevTask> queryTasksByIdsNoAbort(ArrayList ids) throws Exception;

    List<FdevTask> queryAllTasksByIds(ArrayList ids) throws Exception;

    public FdevTask updateTaskView(String scope, String viewId, String name, Boolean audit) throws Exception;

    List<FdevTask> queryByTerms(ArrayList group, ArrayList stage, boolean isIncludeChildren, Integer isDefered) throws Exception;

    public Map queryTaskDetail(FdevTask requestParam) throws Exception;

    public Map reBuildUser(FdevTask li) throws Exception;

    public Map queryTaskDetailForProductions(FdevTask requestParam) throws Exception;

    Map queryTestDetail(FdevTask task,Integer pageNum , Integer pageSize) throws Exception;

    Map queryDocDetail(FdevTask task,Integer pageNum , Integer pageSize) throws Exception;

    FdevTask queryTaskAll(FdevTask requestParam) throws Exception;

    public FdevTaskCollection saveFdevTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception;

    public List<Map> queryFdevTaskCollection(ArrayList ids) ;

    public FdevTaskCollection queryByJobId(FdevTaskCollection fdevTaskCollection) throws Exception;

    List<FdevTaskCollection> queryByTaskId(String task_id) throws Exception;

    public List<Map> queryBySubTask(FdevTaskCollection fdevTaskCollection) throws Exception;

    public FdevTaskCollection updateTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception;

    public List<FdevTask> queryTaskCByUnitNo(String unitno) throws Exception;

    public FdevTask deleteTask(String id) throws Exception;

    public void removeTask(String id) throws Exception;

    Map<String, Map> queryTaskNumByMember(Map members) throws Exception;

    Map<String, Map> queryTaskNumByApp(List<String> appIds) throws Exception;

    public Map<String, Map> queryTaskNum(List<String> id, boolean includeChild) throws Exception;

    public FdevTaskCollection deleteMainTask(String id) throws Exception;

    public Map<String, Map> queryTaskNumByGroup(List<String> ids, boolean includeChild) throws Exception;

    public Map<String, Map> queryTaskNumByGroupDate(List<String> ids, String startDate, String end_date, boolean includeChild) throws Exception;

    Map queryTaskAndRedmineNo() throws Exception;

    List updateBulkTaskForRelease(Map data) throws Exception;

    List queryTaskByRqrmnt(String rqrmnt) throws Exception;

    List queryTaskByRqrmntRQR(List ids, List roles, boolean priority) throws Exception;

    List queryTaskNumByGroupinAll(List groups, boolean priority, boolean includeChild);

    Map<String, Object> queryPostponeTask(Map<String, Object> params) throws Exception;

    List<FdevTask> queryNotinlineTasksByAppId(Map params) throws Exception;

    void bulkAddTask(Map requestParam, User user) throws Exception;

    public Map<String, Map> queryTaskNumByUserIdsDate(List<String> ids, List roles, String startDate, String end_date, List<String> demandTypeList, List<Integer> taskTypeList) throws Exception;

    Map taskNameJudge(Map requestParam) throws Exception;

    Map<String, Object> queryByTermsAndPage(ArrayList group, ArrayList stage, boolean isIncludeChildren, Integer isDefered, int page, int per_page)throws Exception;

    Map queryRqrmntNameAndReinforceMsg(Map requestParam) throws Exception;

    FdevTask createTestRunMerge(Map requestParam)throws Exception;

    List queryGroupRqrmnt(List groups, String priority, boolean isParent);

    String createNoCodeTask (FdevTask task,Map request) throws Exception;

    String addNocodeRelator (FdevTask task,Map request)  throws Exception;

    String deleteNocodeRelator (FdevTask task,Map request) throws Exception;

    String finishNocodeRelator (FdevTask task,Map request)  throws Exception;

    String confirmUpdateNocodeInfo (FdevTask task,Map request) throws Exception;

    List<Map<String, String>> uploadFileByRid(FdevTask task, Map request) throws Exception;

    List<Map<String, String>>  deleteFileByRid (FdevTask task, Map request) throws Exception;

    String nocodeNextStage (FdevTask task) throws Exception;

    List<Map<String, String>> queryFilesByRid(FdevTask task, Map request);

    void updateDesignRemark(FdevTask task) throws Exception;

    Boolean updateConfirmBtn(FdevTask task) throws Exception;

    Boolean updateTestKeyNote(FdevTask task) throws Exception;

    List<Map> listTaskByGroupIdAndProWantWindow(String group, String proWantWindow) throws Exception;

    Map<String, Object> getDocInfoForRqr(String taskId) throws Exception;

    List queryTaskCardByMember(Map requestParams);

    Map group(String group) throws Exception;

    List<Map<String,Object>> queryReviewDetailList(String reviewer,List<String> group,String startDate,String endDate,
                                                   String logicalOperator,List<Map> searchList,String internetChildGroupId) throws Exception;

    Long deferOrRecoverTask(List<String> ids, Integer taskSpecialStatus);

    Map judgeByfdevImplementUnitNo(String demand_id,List<String> ids) throws Exception;

    List<Map> queryTaskDetailByfdevImplementUnitNo(List<String> fdev_implement_unit_no_list) throws Exception;

    List<Map> queryTaskByUnitNos(List<String> fdev_implement_unit_no_list) throws Exception;

    List<FdevTask> queryTaskNumByGroupinAll(String group, GroupTree groupTree, List stages);

    List<FdevTask> queryTaskByreadmine(String group);

    List<Map> getPostponeInfo(FdevTask fdevTask);

    List<String> queryTaskIdsByProjectId(String project_id);

    void checkPlanTime(Map<String,Object> fdevTask,Map<String,Object> implementUnitInfo);

    /**
     * 根据任务id获取任务及审核信息
     * @param taskId
     * @return
     */
    Map queryInfoById(String taskId) throws Exception;

    Integer queryNotDiscarddnum(String fdev_implement_unit_no);

    FdevTask queryTaskById(String taskId);

    FdevTask updateReview(FdevTask fdevTask)throws Exception;

    /**
     * 查询用户可操作的已投产待归档任务列表
     * @return
     */
    List<Map> queryWaitFileTask() throws Exception;

    /**
     * 批量归档任务
     * @param ids
     */
    void updateTaskStateToFile(List<String> ids) throws Exception;

    /**
     * 校验任务的研发单元是否已挂载研发单元
     * @param taskId
     * @return
     */
    Map<String, Boolean> checkMountUnit(String taskId);

    /**
     * 更改任务状态为暂缓或取消
     * @param unitNo
     * @param stage
     * @param spectialStatus
     */
    void updateTaskSpectialStatus(String unitNo, String stage, String spectialStatus) throws Exception;

    /**
     * 校验用户编辑的开始uat测试时间、开始rel测试时间(也是就完成uat测试时间)是否早于意向投产窗口时间
     * @param taskId
     * @return
     */
    Map<String, Boolean> checkUatAndRelTestTime(String taskId) throws Exception;

    /**
     * 查询研发单元下的任务
     * @param fdevUnitNo
     * @return
     */
    List<FdevTask> queryTaskByFdevUnitNo(String fdevUnitNo);

    /**
     * 查询当前需求可以新建任务的类型
     * @param demandId
     * @param unitNo
     * @return
     */
    Map<String, Object> queryAddTaskType(String demandId, String unitNo);

    /**
     * 根据小组id查询子组,默认查互联网条线下的组信息
     * @param id
     * @return
     */
    List<Map> queryGroupById(String id) throws Exception;

    /**
     * 下载UI审核还原数据
     * @param response
     * @param reviewer
     * @param group
     * @param startDate
     * @param endDate
     * @param logicalOperator
     * @param searchList
     * @param columnMap
     * @throws Exception
     */
    void downLoadReviewList(HttpServletResponse response, String reviewer, List<String> group, String startDate, String endDate,
                            String logicalOperator, List<Map> searchList, Map<String,String> columnMap, String internetChildGroupId) throws Exception;

    void updateTaskCodeOrderNo(Map<String, Object> params) throws Exception;

    Map<String,Object> queryTaskByCodeOrderNo(String code_order_no) throws Exception;

    /**
     * 提交安全测试
     * @param taskId
     * @param correlationSystem
     * @param correlationInterface
     * @param interfaceFilePath
     * @param transFilePath
     * @param transList
     * @param remark
     */
    void putSecurityTest(String taskId, String correlationSystem, String correlationInterface, String interfaceFilePath, String transFilePath, List<Map> transList, String remark) throws Exception;

    void syncTaskUIReportUser();

    List<Map<String, Object>> queryTaskSimpleByIds(List<String> ids) throws Exception;

    /**
     * 发起分支合并请求，开发分支到sit分支
     * @param applicationType
     * @param title
     * @param projectId
     * @param description
     * @param sourceBranch
     * @param targetBranch
     * @param versionNum
     * @return
     */
    String createMergeRequest(String applicationType, String title, String projectId, String description, String sourceBranch, String targetBranch, String versionNum, Map createUser) throws Exception;

    void syncTaskApplicationType();

    /**
     * 添加git项目成员
     * @param gitlabProjectId
     * @param members
     * @param appType
     * @param projectId
     */
    void addMemberGit(String gitlabProjectId, HashSet members, String appType, String projectId) throws Exception;

    /**
     * 获取关联项目信息
     * @param projectId
     * @param appType
     * @return
     */
    Map<String, Object> getProjectInfo(String projectId, String appType) throws Exception;

    /**
     * 查询合并请求信息
     * @param gitlabProjectId
     * @param mergeId
     * @return
     */
    Map<String, String> queryMergeInfo(String gitlabProjectId, String mergeId);

    /**
     * 修改任务信息
     * @param task
     */
    void updateTaskInfo(FdevTask task) throws Exception;

    /**
     * 批量查询任务基础信息
     * @param ids 任务id集合
     * @param fields 需要组装的字段集合
     * @param responseFields
     * @return
     */
    List queryTaskBaseInfoByIds(List<String> ids, List<String> fields, List<String> responseFields) throws Exception;

    /**
     * 修改任务实际提交用户测试时间并反推给研发单元
     * @param id
     * @param startUatTime
     */
    void updateStartUatTime(String id, String startUatTime) throws Exception;

    /**
     * 创建release合并请求
     * @param queryTask
     * @param user
     * @param releaseApp
     * @return
     * @throws Exception
     */
    FdevTask createReleaseMergeRequest(FdevTask queryTask, Map user, Map releaseApp) throws Exception;

    /**
     * 判断任务是否要走release合并审核流程，卡点条件为：
     * 1、不是首次合并release；2、任务所属小组的三级组为武汉研发A1-A6
     * @param fdevTask
     * @return
     */
    boolean releaseMergeFlag(FdevTask fdevTask) throws Exception;

    /**
     * 查询单个任务信息
     * @param id
     * @param fields
     * @return
     */
    Object queryTaskInfoById(String id, List<String> fields) throws Exception;
    /**
     * 简单需求任务跳过功能测试
     */
    void skipInnerTest(Map<String,Object> param) throws Exception;

    /**
     * 导出应用下任务
     * @param response
     */
    void exportAppTask(HttpServletResponse response) throws IOException;
}
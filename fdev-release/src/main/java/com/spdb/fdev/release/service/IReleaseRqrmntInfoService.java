package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.entity.ReleaseTask;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IReleaseRqrmntInfoService {
    /**
     * 根据日期和需求编号查询需求信息
     * @param release_date
     * @param rqrmnt_no
     * @return
     * @throws Exception
     */
    ReleaseRqrmntInfo queryReleaseRqrmntInfo(String release_date, String rqrmnt_no, String group_id) throws  Exception;

    /**
     * 保存投产窗口需求信息
     * @param releaseRqrmntInfo
     * @throws Exception
     */
    void saveReleaseRqrmntInfo(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception;

    /**
     * 根据任务编号查询需求信息
     * @return
     * @throws Exception
     */
    ReleaseRqrmntInfo queryReleaseRqrmntInfoByTaskNo(String task_id) throws  Exception;


    /**
     * 根据任务编号,日期和需求编号查询需求信息
     * @param relase_date
     * @return
     * @throws Exception
     */
    void updateRqrmntInfo(String relase_date, String type, List<String> groupIds, HttpServletResponse resp) throws  Exception;

    /**
     * 修改需求信息任务列表
     * @param id
     * @param task_ids
     * @return
     * @throws Exception
     */
    void updateRqrmntInfoTaskList(String id, List<String> task_ids) throws  Exception;


    /**
     * 根据任务编号,日期和需求编号查询需求信息
     * @param id
     * @return
     * @throws Exception
     */
    void deleteRqrmntInfo(String id) throws  Exception;

    /**
     * 将任务id加入需求信息
     * @param task_id
     * @throws Exception
     */
    void addTaskReleaseRqrmntInfo(String id, String task_id, String new_add_sign,String otherSystem,String commonProfile,String dateBaseAlter,String specialCase) throws Exception;

    /**
     * 将任务从需求信息列表中删除，若再无任务则删除此需求
     * @param id
     * @param task_id
     * @throws Exception
     */
    void deleteTaskReleaseRqrmntInfo(String id, String task_id) throws Exception;

    /**
     * 新增任务需求信息，若存在需求则加入任务列表，否则新增需求信息
     * @param rqrmntNo
     * @param groupId
     * @param releaseDate
     * @param taskId
     * @param type
     * @throws Exception
     */
    void addReleaseRqrmntInfoOrTask(String rqrmntNo, String rqrmntId, String rqrmntName, String groupId,
                                    String releaseDate, String taskId, String type, String new_add_sign,
                                    String otherSystem, String commonProfile, String dateBaseAlter, String specialCase
    ) throws Exception;

    /**
     * 查询投产需求列表
     * @param release_date
     * @return
     * @throws Exception
     */
    List queryRqrmntInfoList(String release_date, String type, List<String> groupIds, boolean isParent) throws  Exception;

    /**
     * 按类型查询提测重点列表 或安全测试需求列表
     * @param release_date
     * @param type
     * @return
     * @throws Exception
     */
    List queryRqrmntInfoListByType(String release_date, String type, List<String> groupIds, boolean isParent) throws  Exception;

    /**
     * 同步需求信息标签
     * @throws Exception
     */
    void asyncRqrmntInfoTag() throws Exception;
    /**
     * 复核需求信息标签
     * @throws Exception
     */
    void confirmRqrmntInfoTag(String confirmFileDate, String task_id) throws Exception;

    /**
     * 同步需求信息标签-业务需求不予投产
     * @throws Exception
     */
    void asyncRqrmntInfoTagNotAllow() throws  Exception;

    /**
     * 更新需求列表关联项
     * @param task_id
     */
    void updateRqrmntInfoReview(String task_id) throws Exception;

    /**
     * 更新需求列表关联项
     * @param id
     */
    String queryRqrmntInfoPackageSubmitTest(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> tasks) throws Exception;

    /**
     * 更新需求列表关联项
     * @param id
     */
    String queryRqrmntInfoRelTest(String id, Map<String, Map> tasks) throws Exception;

    void batchAddRqrmntInfo() throws  Exception;

    void deleteRqrmntInfoTask(String task_id) throws Exception;

    List<Map> queryRqrmntInfoTasks(String id) throws Exception;

    void auditAddRqrmntInfo(ReleaseTask releaseTask, String uat_testobject) throws Exception;

    void batchDeleteRqrmntInfo() throws Exception;

    void batchUpdateRqrmntInfo() throws Exception;

    String queryTaskRqrmntAlert(String task_id) throws Exception;

    String queryRqrmntFileUri(String release_date) throws Exception;

    void changeReleaseNode(ReleaseTask releaseTask, ReleaseNode releaseNode) throws Exception;

    void cancelRelease(ReleaseTask releaseTask) throws Exception;

    void addReleaseCycleT8() throws Exception;

    void deleteReleaseRqrmntInfoTask(String task_id) throws Exception;

    void updateRqrmntInfoFlag(String id, String s);
    
    List<Map<String, Object>> addRedLineScanReport(Map<String, Object> requestParam) throws Exception;
}

package com.gotest.dao;

import com.gotest.domain.TaskList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskListMapper {
    int queryTaskListCountByWorkNo(@Param("workNo")String workNo, @Param("taskName")String taskName);
    List<TaskList> queryTaskListByWorkNo(@Param("workNo")String workNo, @Param("taskName")String taskName, @Param("start")Integer start, @Param("pageSize")Integer pageSize);

    Integer addTaskList(TaskList taskList);

    Integer dropTaskListByTaskNo(@Param("taskNo")String taskNo);

    Integer queryTaskExist(@Param("taskNo")String taskNo, @Param("workOrderNo")String workOrderNo);

    List<TaskList> queryTaskByNo(@Param("workNo")String workNo) throws Exception;

    List<String> queryOrderByTaskName(@Param("taskName")String taskName) throws Exception;

    TaskList queryTaskByTaskNo(@Param("taskNo") String taskNo);

    Integer updateWorkNoAndUnitNoByTaskNo(@Param("taskNo") String taskNo,@Param("workNo") String workNo,@Param("unitNo") String unitNo);

    List<TaskList> queryTaskListByWorkNoAndTaskNo(@Param("workNo")String workNo,@Param("taskNo")String taskNo);

    Integer updateWorkNoByTaskNo(@Param("taskNo") String taskNo,@Param("workNo") String workNo);

    Integer queryNewOrOldOrder(@Param("workNo") String workNo) throws Exception;

    List<String> queryTaskNoByOrder(@Param("workOrderNo") String workOrderNo);

    Integer updateWorkNoByTaskNos(@Param("taskIds")List<String> taskIds,@Param("newWorkNo")String newWorkNo);

    List<Map> queryTaskNoByOrders(@Param("workNoList") List<String> workNoList);
}

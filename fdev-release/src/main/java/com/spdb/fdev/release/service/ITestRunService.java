package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.TestRunApplication;
import com.spdb.fdev.release.entity.TestRunTask;

import java.util.List;

public interface ITestRunService {

    /**
     * 根据应用id与投产窗口名称查询试运行应用列表
     * @param testRunApplication application_id, release_node_name
     * @return
     */
    List<TestRunApplication> findByAppNode(TestRunApplication testRunApplication);

    void save(TestRunApplication testRunApplication);

    /**
     * 根据试运行id查询试运行详情
     * @param id testrun_id
     * @return
     */
    TestRunApplication findById(String id);

    void saveTask(TestRunTask testRunTask);

    /**
     * 根据试运行任务id修改合并状态
     * @param testRunTask id， status
     */
    void updateTaskStatusById(TestRunTask testRunTask);

    /**
     * 根据试运行任务id删除任务
     * @param testRunTask id
     */
    void removeTaskById(TestRunTask testRunTask);

    /**
     * 根据gitlab_project_id与merge_request_id查询单个试运行合并任务
     * @param testRunTask gitlab_project_id， merge_request_id
     * @return
     */
    TestRunTask findTaskByGitMergeId(TestRunTask testRunTask);

    /**
     * 根据试运行id查询任务列表
     * @param testRunTask testrun_id
     * @return
     */
    List<TestRunTask> findTaskByTestRunId(TestRunTask testRunTask);

    /**
     * 根据gitlab_project_id与试运行分支名称插入试运行包地址
     * @param testRunApplication gitlab_project_id, testrun_branch
     */
    void setTestRunUrlByGitBranch(TestRunApplication testRunApplication);
}

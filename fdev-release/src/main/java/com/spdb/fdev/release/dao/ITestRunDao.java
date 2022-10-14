package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.TestRunApplication;
import com.spdb.fdev.release.entity.TestRunTask;

import java.util.List;

public interface ITestRunDao {

    List<TestRunApplication> findByAppNode(TestRunApplication testRunApplication);

    void save(TestRunApplication testRunApplication);

    TestRunApplication findById(String id);

    void saveTask(TestRunTask testRunTask);

    void updateTaskStatusById(TestRunTask testRunTask);

    void removeTaskById(TestRunTask testRunTask);

    TestRunTask findTaskByGitMergeId(TestRunTask testRunTask);

    List<TestRunTask> findTaskByTestRunId(TestRunTask testRunTask);

    void setTestRunUrlByGitBranch(TestRunApplication testRunApplication);
}

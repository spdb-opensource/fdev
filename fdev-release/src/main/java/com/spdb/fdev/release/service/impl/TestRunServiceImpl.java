package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.ITestRunDao;
import com.spdb.fdev.release.entity.TestRunApplication;
import com.spdb.fdev.release.entity.TestRunTask;
import com.spdb.fdev.release.service.ITestRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestRunServiceImpl implements ITestRunService {

    @Autowired
    private ITestRunDao testRunDao;

    @Override
    public List<TestRunApplication> findByAppNode(TestRunApplication testRunApplication) {
        return testRunDao.findByAppNode(testRunApplication);
    }

    @Override
    public void save(TestRunApplication testRunApplication) {
        testRunDao.save(testRunApplication);
    }

    @Override
    public TestRunApplication findById(String id) {
        return testRunDao.findById(id);
    }

    @Override
    public void saveTask(TestRunTask testRunTask) {
        testRunDao.saveTask(testRunTask);
    }

    @Override
    public void updateTaskStatusById(TestRunTask testRunTask) {
        testRunDao.updateTaskStatusById(testRunTask);
    }

    @Override
    public void removeTaskById(TestRunTask testRunTask) {
        testRunDao.removeTaskById(testRunTask);
    }

    @Override
    public TestRunTask findTaskByGitMergeId(TestRunTask testRunTask) {
        return testRunDao.findTaskByGitMergeId(testRunTask);
    }

    @Override
    public List<TestRunTask> findTaskByTestRunId(TestRunTask testRunTask) {
        return testRunDao.findTaskByTestRunId(testRunTask);
    }

    @Override
    public void setTestRunUrlByGitBranch(TestRunApplication testRunApplication) {
        testRunDao.setTestRunUrlByGitBranch(testRunApplication);
    }
}

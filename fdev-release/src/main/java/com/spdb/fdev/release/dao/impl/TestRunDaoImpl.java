package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.ITestRunDao;
import com.spdb.fdev.release.entity.TestRunApplication;
import com.spdb.fdev.release.entity.TestRunTask;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TestRunDaoImpl implements ITestRunDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<TestRunApplication> findByAppNode(TestRunApplication testRunApplication) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(testRunApplication.getApplication_id())
                .and(Dict.RELEASE_NODE_NAME).is(testRunApplication.getRelease_node_name()));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, TestRunApplication.class);
    }

    @Override
    public void save(TestRunApplication testRunApplication) {
        testRunApplication.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.save(testRunApplication);
    }

    @Override
    public TestRunApplication findById(String id) {
        return mongoTemplate.findById(id, TestRunApplication.class);
    }

    @Override
    public void saveTask(TestRunTask testRunTask) {
        testRunTask.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.save(testRunTask);
    }

    @Override
    public void updateTaskStatusById(TestRunTask testRunTask) {
        Query query = new Query(Criteria.where(Dict.ID).is(testRunTask.getId()));
        Update update = Update.update(Dict.STATUS, testRunTask.getStatus())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, TestRunTask.class);
    }

    @Override
    public void removeTaskById(TestRunTask testRunTask) {
        Query query = new Query(Criteria.where(Dict.ID).is(testRunTask.getId()));
        mongoTemplate.findAndRemove(query, TestRunTask.class);
    }

    @Override
    public TestRunTask findTaskByGitMergeId(TestRunTask testRunTask) {
        Query query = new Query(Criteria.where(Dict.GITLAB_PROJECT_ID).is(testRunTask.getGitlab_project_id())
                .and(Dict.MERGE_REQUEST_ID).is(testRunTask.getMerge_request_id()));
        return mongoTemplate.findOne(query, TestRunTask.class);
    }

    @Override
    public List<TestRunTask> findTaskByTestRunId(TestRunTask testRunTask) {
        Query query = new Query(Criteria.where(Dict.TESTRUN_ID).is(testRunTask.getTestrun_id()));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, TestRunTask.class);
    }

    @Override
    public void setTestRunUrlByGitBranch(TestRunApplication testRunApplication) {
        Query query = new Query(Criteria.where(Dict.GITLAB_PROJECT_ID).is(testRunApplication.getGitlab_project_id())
                .and(Dict.TESTRUN_BRANCH).is(testRunApplication.getTestrun_branch()));
        Update update = Update.update(Dict.TESTRUN_URL, testRunApplication.getTestrun_url())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, TestRunApplication.class);
    }
}

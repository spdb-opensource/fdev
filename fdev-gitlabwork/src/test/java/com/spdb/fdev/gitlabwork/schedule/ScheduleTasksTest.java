package com.spdb.fdev.gitlabwork.schedule;

import com.spdb.fdev.gitlabwork.schedule.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ScheduleTasksTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UpdateGitlabUserTask updateGitlabUserTask;

//    @Autowired
//    private UpdateGitlabGroupTask updateGitlabGroupTask;

    @Autowired
    private UpdateGitlabProjectTask updateGitlabProjectTask;

    @Autowired
    private UpdateGitlabCommitTask updateGitlabCommitTask;

    @Autowired
    private UpdateGitlabWorkTask updateGitlabWorkTask;

    /**
     * 刷新人员，角色和公司
     */
    @Test
    public void updateUser() {
        updateGitlabUserTask.updateUser();
    }

    /**
     * 刷新小组
     */
//    @Test
//    public void updateGroup() {
//        updateGitlabGroupTask.updateGroup();
//    }

    /**
     * 刷新应用信息
     */
    @Test
    public void updateProject() {
        updateGitlabProjectTask.updateProject();
    }

    /**
     * 刷新Commit信息
     */
    @Test
    public void updateCommit() {
        updateGitlabCommitTask.updateCommit();
    }

    /**
     * 刷新汇总信息
     */
    @Test
    public void updateWork() {
        updateGitlabWorkTask.updateWorkInfo();
    }

    /**
     * 定时任务刷新全部信息
     */
    @Test
    public void scheduleTest() {
        logger.info(">>>>>> Start to update GitlabUser");
        updateGitlabUserTask.updateUser();
        logger.info(">>>>>> End check update GitlabUser");

//        logger.info(">>>>>> Start to update GitlabGroup");
//        updateGitlabGroupTask.updateGroup();
//        logger.info(">>>>>> End check update GitlabGroup");

        logger.info(">>>>>> Start to update GitlabProject");
        updateGitlabProjectTask.updateProject();
        logger.info(">>>>>> End check update GitlabProject");

        logger.info(">>>>>> Start to update GitlabCommit");
        updateGitlabCommitTask.updateCommit();
        logger.info(">>>>>> End check update GitlabCommit");

        logger.info(">>>>>> Start to update updateWorkInfo");
        updateGitlabWorkTask.updateWorkInfo();
        logger.info(">>>>>> End check update updateWorkInfo");
    }

}
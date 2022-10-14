package com.spdb.fdev.gitlabwork.schedule;

import com.spdb.fdev.gitlabwork.dto.CommitUpdate;
import com.spdb.fdev.gitlabwork.schedule.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ScheduleTasks
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class ScheduleTasks {

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

    @Value("${schedule.updateGitlabUser.flag}")
    private boolean updateGitlabUserTaskFlag;

    /**
     * 刷新人员、公司、角色信息
     * 刷新小组信息
     * 刷新项目信息
     * 刷新Commit提交记录
     * 刷新汇总信息
     * 每天凌晨执行一次代码
     * cron = 秒 分 时 日 月 星期中的日期
     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    通过提供接口迁移到批量模块
    public void updateGitlabUser() {
        if (updateGitlabUserTaskFlag) {

            logger.info(">>>>>> Start to update GitlabUser");
            updateGitlabUserTask.updateUser();
            logger.info(">>>>>> End check update GitlabUser");

//            logger.info(">>>>>> Start to update GitlabGroup");
//            updateGitlabGroupTask.updateGroup();
//            logger.info(">>>>>> End check update GitlabGroup");

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

    public void commitStatistics(CommitUpdate commitUpdate){
        updateGitlabCommitTask.updateCommit(commitUpdate);
    }

    public void commitStats(){
        updateGitlabCommitTask.updateCommitStats();
    }

}

package com.spdb.fdev.gitlabwork.schedule.service;

import com.spdb.fdev.gitlabwork.dto.CommitUpdate;
import com.spdb.fdev.gitlabwork.service.CommitService;
import com.spdb.fdev.gitlabwork.service.IGitlabCommitService;
import com.spdb.fdev.gitlabwork.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * UpdateGitlabCommitTask
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class UpdateGitlabCommitTask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IGitlabCommitService gitlabCommitService;

    @Autowired
    private CommitService commitService;

    @Value("${schedule.since}")
    private String since;

    public void updateCommit() {

        String until = Util.simpleDateFormat("yyyy-MM-dd").format(new Date());

        logger.info(">>>>>UpdateGitlabCommitTask.addCommitInfoList拉取commit记录开始时间" + since + "和结束时间" + until);
        try {
            gitlabCommitService.addCommitInfoList(since, until);
        } catch (Exception e) {
            logger.error("更新commit信息失败", e.getMessage());
        }
    }

    /**
     * 上者是旧版代码提交同步
     * 此处是新版代码提交同步
     */
    public void updateCommit(CommitUpdate commitUpdate) {
        new Thread(() -> {
            try {
                commitService.updateCommit(commitUpdate);
            } catch (Exception e) {
                logger.error("代码提交同步异常", e);
            }
        }).start();
    }

    public void updateCommitStats() {
        new Thread(() -> {
            try {
                commitService.updateCommitStats();
            } catch (Exception e) {
                logger.error("提交详情同步异常", e);
            }
        }).start();
    }

}
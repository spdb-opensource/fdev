package com.spdb.fdev.gitlabwork.schedule.service;

import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * UpdateGitlabUserTask
 *
 * @blame Android Team
 */
@Component
public class UpdateGitlabUserTask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IGitLabUserService gitLabUserService;

    public void updateUser() {
        try {
            gitLabUserService.selectUserAndSave();
        } catch (Exception e) {
            logger.error("更新人员信息失败", e.getMessage());
        }
    }
}

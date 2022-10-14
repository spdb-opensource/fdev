package com.spdb.fdev.gitlabwork.schedule.service;

import com.spdb.fdev.gitlabwork.service.IGitlabProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UpdateGitlabProjectTask
 *
 * @blame Android Team
 */
@Component
public class UpdateGitlabProjectTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IGitlabProjectService gitlabProjectService;

    public void updateProject() {
        try {
            gitlabProjectService.selectProjectAndSave();
        } catch (Exception e) {
            logger.error("更新应用信息失败", e.getMessage());
        }
    }
}

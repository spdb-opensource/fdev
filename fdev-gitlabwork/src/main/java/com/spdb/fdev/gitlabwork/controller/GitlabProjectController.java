package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.service.IGitlabProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GitlabProjectController
 *
 * @blame Android Team
 */
@RestController
@RequestMapping("/api/project")
public class GitlabProjectController {

    @Autowired
    IGitlabProjectService gitlabProjectService;

    /**
     * 查询项目信息
     *
     * @return
     */
    @PostMapping("/query")
    public List<GitlabProject> query() {
        return gitlabProjectService.select();
    }

}

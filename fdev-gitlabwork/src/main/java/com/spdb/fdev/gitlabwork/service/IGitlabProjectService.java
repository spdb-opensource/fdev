package com.spdb.fdev.gitlabwork.service;

import java.util.List;

import com.spdb.fdev.gitlabwork.entiy.GitlabProject;

/**
 * IGitlabProjectService
 *
 * @blame Android Team
 */
public interface IGitlabProjectService {

    List<GitlabProject> selectProjectAndSave();

    List<GitlabProject> select();

    GitlabProject selectByName(String projectName);
}

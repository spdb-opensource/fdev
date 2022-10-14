package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.GitlabProject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * IGitlabProjectDao
 *
 * @blame Android Team
 */
public interface IGitlabProjectDao {

    GitlabProject save(GitlabProject gitlabProject);

    void upsert(GitlabProject gitlabProject);

    List<GitlabProject> select();

    GitlabProject selectByProjectId(String projectId);

    GitlabProject selectByName(String projectName);

    List<GitlabProject> selectBySign(int sign);

    List<Map<String, Object>> findBrancheListById(int projectId) throws IOException;

    void updateSign(int source,int target);
}

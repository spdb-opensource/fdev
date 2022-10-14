package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * IGitlabCommitDao
 *
 * @blame Android Team
 */
public interface IGitlabCommitDao {

    GitlabCommit save(GitlabCommit gitlabCommit);

    void upsert(GitlabCommit gitlabCommit);

    List<GitlabCommit> selectGitlabCommitInfo(String nickName, String userName, String gituser, String configname, String committed_date);

    List<GitlabCommit> selectGitlabCommitInfo(List nameList, String committed_date);

    List<GitlabCommit> selectGitlabCommitInfo(String nickName, String userName, String gituser, String configname, String committed_date, List projectlist);

    List<GitlabCommit> selectGitlabCommitInfo(List nameList, String committed_date, List projectlist);

    List<Map<String, Object>> findOneBranchCommitList(int projectId, String ref_name, String since, String until) throws IOException;

    Map<String, Object> findCommitDetail(int projectId, String hashVersion) throws IOException;

    List<Map<String, Object>> findCommitDiff(int projectId, String hashVersion) throws IOException;
}

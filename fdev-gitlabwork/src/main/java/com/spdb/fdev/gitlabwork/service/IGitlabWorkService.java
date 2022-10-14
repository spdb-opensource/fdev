package com.spdb.fdev.gitlabwork.service;

import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * IGitlabWorkService
 *
 * @blame Android Team
 */
public interface IGitlabWorkService {
    GitlabWork initGitlabUser(GitlabUser user);

    GitlabWork newGitlabWork(GitlabWork work1, GitlabWork work2) throws ParseException;

    void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, String userName, String nickName, String groupid, String groupname,String projectName,String companyid,String companyname);

    void addWorkInfoFromMap(List<Map<String, Object>> resultMapList);

    void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, String projectname, GitlabUser gitlabUser);

    void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, GitlabUser gitlabUser);

    void updateWorkInfoFromMap(List<Map<String, Object>> resultMapList);

    void insertShortId(List<GitlabWork> gitlabWorks);
}

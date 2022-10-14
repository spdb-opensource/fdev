package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import com.mongodb.client.result.UpdateResult;

import java.util.List;

/**
 * IGitlabWorkDao
 *
 * @blame Android Team
 */
public interface IGitlabWorkDao {
    GitlabWork save(GitlabWork gitlabWork);

    UpdateResult upsert(GitlabWork gitlabWork);

    List<GitlabWork> select();

    List<GitlabWork> select(String userName, String companyId, String startDate, String endDate);

    List<GitlabWork> select1(String userName, String startDate, String endDate);

    List<GitlabWork> selectByGroupId(String groupId, String startDate, String endDate);

    GitlabWork selectByUserNameAndCommitDate(String userName, String committedDate);
}

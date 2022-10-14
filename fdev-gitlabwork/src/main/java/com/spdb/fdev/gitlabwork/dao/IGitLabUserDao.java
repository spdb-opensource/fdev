package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.GitlabUser;

import java.util.List;
import java.util.Map;

/**
 * IGitLabUserDao
 *
 * @blame Android Team
 */
public interface IGitLabUserDao {
    GitlabUser save(GitlabUser gitlabUser);

    GitlabUser selectByUserId(String userid);

    void upsert(GitlabUser gitlabUser);

    List<GitlabUser> select();

    void removeGitlabUserInfo();

    GitlabUser selectByGitUser(String gituser);

    List<GitlabUser> selectBySign(int sign);

    List<GitlabUser> selectByGroupId(String groupId);

    List<GitlabUser> selectByGroupIdAndCompanyId(String groupId, String companyId);

    List<GitlabUser> selectCompanyIdAndRoleName(String companyId, String roleName, String status);

    Map<String, Object> selectByGroupIdAndCompanyIdRoleName(String groupId, String companyId, String roleName, String status, String area, int page, int per_page);

    void updateSign(int source, int target);

    List<Map> selectArea();

    List<GitlabUser> selectGitlabUserByParams(GitlabUser gitlabUser, List groupIdList);
}

package com.spdb.fdev.gitlabwork.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;

/**
 * IGitLabUserService
 *
 * @blame Android Team
 */
public interface IGitLabUserService {

    List<GitlabUser> select();

    List<GitlabUser> selectUserAndSave();

    JSONArray selectGroup();

    JSONArray selectGroupId(Map<String, String> map);

    GitlabUser selectByGitUser(String gituser);

    List<GitlabUser> selectByGroupIdAndCompanyId(String groupId, String companyId);

    List<GitlabUser> selectCompanyIdAndRoleName(String companyId, String roleName, String status);

    Map<String, Object> selectByGroupIdAndCompanyIdRoleName(String groupId, String companyId, String rolename, String status, String area, int page, int per_page);

    List<Map> selectArea();

    List<GitlabUser> selectGitlabUserByParams(GitlabUser gitlabUser);

}

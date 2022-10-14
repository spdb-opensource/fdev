package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.Author;

import java.util.List;
import java.util.Map;

public interface IUserService {

    String getUserGitToken(String gitUsername) throws Exception;

    User getUserFromRedis() throws Exception;

    Author getAuthor() throws Exception;

//    List<String> getLineIdsByGroupId(String groupId);

//    List<Map> getSectionChildByGroupId(String groupId);

    List<Map> getChildGroupByGroupId(String groupId);

    List<String> getChildGroupIdsByGroupId(String groupId);

    Map queryUserById(String id);

    Map queryUserByIdComm(String id) throws Exception;

    Map querySectionUser(String groupId);
}

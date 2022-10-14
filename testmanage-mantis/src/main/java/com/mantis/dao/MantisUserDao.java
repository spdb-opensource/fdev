package com.mantis.dao;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MantisUserDao {

    void changeProjectAccessLevel(String userName, String projectId, String level) throws Exception;

    List<String> queryAllMantisUser() throws Exception;

    String queryProjectUserLevel(String projectId, String userName) throws  Exception;

    void addProjectAccessLevel(String userName, String projectId, String level) throws Exception;

    Map<String, String> queryUserById(String handlerId);
}

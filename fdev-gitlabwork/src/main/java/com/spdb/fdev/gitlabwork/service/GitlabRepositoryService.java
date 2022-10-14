package com.spdb.fdev.gitlabwork.service;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.GitlabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GitlabRepositoryService
 *
 * @blame Android Team
 */
@Service
public class GitlabRepositoryService {

    @Autowired
    GitlabRepository gitlabRepository;
    /**
     * 获取组信息
     *
     * @param groupName
     * @param workInfoMap
     * @return
     * @throws IOException
     */
    public Map<String, Object> getGroup(String groupName, Map<String, Object> workInfoMap) throws IOException {
        Map<String, Object> groupMap = gitlabRepository.getGroup(groupName);
        workInfoMap.put(Dict.GROUPNAME, groupName);
        return groupMap;
    }

    public Map queryProjectUrlById(String projectId) throws Exception {
        return gitlabRepository.queryProjectUrlById(projectId);
    }
}

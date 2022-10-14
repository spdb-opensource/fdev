package com.spdb.fdev.gitlabwork.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.gitlabwork.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitlabRepository
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class GitlabRepository {
    private static final Logger logger = LoggerFactory.getLogger(GitlabRepository.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.token}")
    private  String gitlabToken;

    /**
     * 获取项目仓库列表
     *
     * @return
     * @throws IOException
     */
    public List<String> getAllRepositoryList() throws IOException {
        List<String> repositoryList = new ArrayList<>();
        int page = 10;
        for (int i = 0; i < page; i++) {
            String url = gitlabApiUrl+"projects?page=" + i + "&per_page=100&owned=true";
            String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
            /* repositoryList = Util.stringToList(body);*/
            repositoryList.add(body);
            logger.info("getRepositoryList:" + repositoryList.toString());
        }

        return repositoryList;
    }

    /**
     * 根据组别获取项目仓库列表
     *
     * @param groupId
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> getRepositoryList(int groupId) throws IOException {
        String url = gitlabApiUrl+"groups/" + groupId + "/projects?page=1&per_page=1000";
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        List<Map<String, Object>> repositoryList = Util.stringToList(body);
        logger.info("getRepositoryList:" + repositoryList.toString());
        return repositoryList;
    }

    /**
     * 获取组信息
     *
     * @return
     * @throws IOException
     */
    public Map<String, Object> getGroup(String groupName) throws IOException {
        String url = gitlabApiUrl+"groups?search=" + groupName;
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        Map<String, Object> groupMap = Util.stringToMap(body);
        logger.info("getGroup:" + groupMap.toString());
        return groupMap;
    }

    /**
     * 根据项目id查询Members
     *
     * @param groupsId
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> getMembersbyId(int groupsId) throws IOException {
        String url = gitlabApiUrl+"groups/" + groupsId + "/members/all?per_page=10000";
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        List<Map<String, Object>> membersList = Util.stringToList(body);
        logger.info("getMembersbyId:" + membersList.toString());
        return membersList;
    }

    /**
     * 根据项目id查询项目gitlab信息
     * @param projectId
     * @return
     * @throws Exception
     */
    public Map queryProjectUrlById(String projectId) throws Exception {
        String url = gitlabApiUrl+"projects/" + projectId;
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> ProjectInfoList = objectMapper.readValue(body, Map.class);
        logger.info("getProjectInfo:" + ProjectInfoList.toString());
        return ProjectInfoList;
    }

}

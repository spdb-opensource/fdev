package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.spdb.dao.GitlabDao;
import com.fdev.database.util.GitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;


@Repository
@RefreshScope
public class GitlabDaoImpl implements GitlabDao {

    @Autowired
    RestTemplate restTemplate;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.token}")
    private  String gitlabToken;

    /**
     * 根据项目ID、比较分支和目标分支获取commit记录
     * @throws IOException
     */
    @Override
    public Map<String, Object> compareBranch(Integer projectId, String sourceBranch, String mergeBranch) throws Exception {
        String url = gitlabApiUrl+"projects/" + projectId + "/repository/compare/?from=" + sourceBranch + "&to="+mergeBranch;
        String body = GitUtils.httpMethodGetExchange(url, restTemplate, gitlabToken);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> commitCompareList = objectMapper.readValue(body, Map.class);
        return commitCompareList;
    }

    /**
     * 根据项目ID和commit版本获取该commit修改内容
     */
    @Override
    public List<Map<String, Object>> findCommitDiff(Integer projectId, String commitId) throws Exception {
        String url = gitlabApiUrl+"projects/" + projectId +  "/repository/commits/" + commitId + "/diff";
        String body = GitUtils.httpMethodGetExchange(url, restTemplate, gitlabToken);
        List<Map<String, Object>> commitDiffList = GitUtils.stringToList(body);
        //logger.info("getCommitDiff:" + commitDiffList.toString());
        return commitDiffList;
    }
}

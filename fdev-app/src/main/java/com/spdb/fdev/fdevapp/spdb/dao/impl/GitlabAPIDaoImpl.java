package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.spdb.dao.IGitlabAPIDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RefreshScope
@Repository
public class GitlabAPIDaoImpl implements IGitlabAPIDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private GitlabTransport gitlabTransport;
    
    @Value("${gitlab.token}")
    private String token;
    @Value("${gitlib.path}")
    private String url;


    @Override
    public List<Map> getProjectsBranch(String id) throws Exception {
        List<Map> repositoryList = new ArrayList<>();
        // GitLab page默认为1，per_page默认为20，最大为100
        for (int i = 1; i <= 10; i++) {
            List<Map> repoList = getProjectsBranchByPage(id, i);
            if (CommonUtils.isNullOrEmpty(repoList)) {
                break;
            } else {
                repositoryList.addAll(repoList);
                if (repoList.size() == 100) {
                    continue;
                } else {
                    break;
                }
            }
        }
        return repositoryList;
    }

    @Override
    public List<Map> getProjectsUser(String id, String gitlab_token) throws Exception {
        String userUrl = CommonUtils.projectUrl(url) + "/" + id + "/members" ;
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        try {
            Object object = gitlabTransport.submitGet(userUrl, header);
            List<Map> userList = JSONArray.parseArray((String) object, Map.class);
            return userList;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR,new String[]{e.getMessage()});
        }
    }

    /**
     * 获取项目所有分支
     *
     * @param id
     * @param page
     * @return
     */
    public List<Map> getProjectsBranchByPage(String id, Integer page) {
        List<Map> repositoryList = new ArrayList<>();
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + id + "/repository/branches?" + "&page=" + page + "&per_page=100";
        MultiValueMap header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            String submitGet = (String) gitlabTransport.submitGet(gitLabUrl, header);
            repositoryList = JSONArray.parseArray(submitGet, Map.class);
        } catch (Exception e) {
            logger.info("获取项目{}的分支列表失败：{}", id, e.getMessage());
        }
        return repositoryList;
    }
}

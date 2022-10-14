package com.spdb.fdev.gitlabwork.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.util.Util;
import com.spdb.fdev.gitlabwork.dao.IGitlabProjectDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitlabProjectDaoImpl
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class GitlabProjectDaoImpl implements IGitlabProjectDao {

    private static final Logger logger = LoggerFactory.getLogger(GitlabProjectDaoImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    RestTemplate restTemplate;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.token}")
    private  String gitlabToken;

    /**
     * 保存项目信息
     *
     * @param gitlabProject
     * @return
     */
    @Override
    public GitlabProject save(GitlabProject gitlabProject) {
        return mongoTemplate.save(gitlabProject);
    }

    /**
     * 根据projectid判断项目是否存在，存在则更新
     *
     * @param gitlabProject
     */
    @Override
    public void upsert(GitlabProject gitlabProject) {
        String projectId = gitlabProject.getProject_id();
        Query query = new Query(Criteria.where(Dict.PROJECT_ID).is(projectId));
        Update update = new Update();
        update.set(Dict.NAME_EN, gitlabProject.getNameEn());
        update.set(Dict.NAME_CH, gitlabProject.getNameCh());
        update.set(Dict.GROUP, gitlabProject.getGroup());
        update.set(Dict.GIT, gitlabProject.getGit());
        update.set(Dict.WEB_URL, gitlabProject.getWeb_url());
        update.set(Dict.SIGN, gitlabProject.getSign());
        mongoTemplate.upsert(query, update, GitlabProject.class);
    }

    /**
     * 查询项目列表
     *
     * @return
     */
    @Override
    public List<GitlabProject> select() {
        return mongoTemplate.findAll(GitlabProject.class);
    }

    /**
     * 根据项目Id查询
     *
     * @param projectId
     * @return
     */
    @Override
    public GitlabProject selectByProjectId(String projectId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.PROJECT_ID).is(projectId)));
        return mongoTemplate.findOne(query, GitlabProject.class);
    }

    @Override
    public GitlabProject selectByName(String projectName) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.NAME_EN).is(projectName));
        return mongoTemplate.findOne(query, GitlabProject.class);
    }

    @Override
    public List<GitlabProject> selectBySign(int sign) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.SIGN).is(sign)));
        return mongoTemplate.find(query, GitlabProject.class);
    }


    /**
     * 根据项目ID获取该项目分支列表
     *
     * @param projectId
     * @return
     * @throws IOException
     */
    @Override
    public List<Map<String, Object>> findBrancheListById(int projectId) throws IOException {
        boolean flag = true;
        List<Map<String, Object>> result = new ArrayList<>();
        int i = 1;
        while (flag) {
            String url = gitlabApiUrl+"projects/" + projectId + "/repository/branches?page=" + i + "&per_page=100";
            String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> branchList = Util.stringToList(body);
                if (branchList != null && branchList.size() > 0) {
                    result.addAll(branchList);
                    i++;
                } else {
                    flag = false;
                }
            } else if (body == null)
                flag = false;
        }
        logger.info(">>项目：" + projectId + ">>分支总数为：" + result.size());
        return result;
    }

    /**
     * 把所有状态值为source的应用，状态修改为target
     *
     * @param source
     * @param target
     */
    @Override
    public void updateSign(int source, int target) {
        Query query = new Query(Criteria.where(Dict.SIGN).is(source));
        Update update = new Update();
        update.set(Dict.SIGN, target);
        mongoTemplate.updateMulti(query, update, GitlabProject.class);
    }
}

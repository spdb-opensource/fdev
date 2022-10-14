package com.spdb.fdev.gitlabwork.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.util.Util;
import com.spdb.fdev.gitlabwork.dao.IGitlabCommitDao;
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
 * GitlabCommitDaoImpl
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class GitlabCommitDaoImpl implements IGitlabCommitDao {

    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitDaoImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.token}")
    private  String gitlabToken;

    /**
     * 保存commit信息
     *
     * @param gitlabCommit
     * @return
     */
    @Override
    public GitlabCommit save(GitlabCommit gitlabCommit) {
        return mongoTemplate.save(gitlabCommit);
    }

    /**
     * 保存commit信息，如果没有新增，有的话根据shortid和committed_date进行修改
     *
     * @param gitlabCommit
     */
    @Override
    public void upsert(GitlabCommit gitlabCommit) {

        String short_id = gitlabCommit.getShort_id();
        String committed_date = gitlabCommit.getCommitted_date();
        Query query = new Query(Criteria.where(Dict.SHORT_ID).is(short_id)
                .and(Dict.COMMITTED_DATE).is(committed_date));

        Update update = new Update();
        update.set(Dict.SHORT_ID, short_id);
        update.set(Dict.COMMITTED_DATE, committed_date);
        update.set(Dict.COMMITTER_EMAIL, gitlabCommit.getCommitter_email());
        update.set(Dict.STATS, gitlabCommit.getStats());
        update.set(Dict.BRANCHNAME, gitlabCommit.getBranchName());
        update.set(Dict.PROJECTNAME, gitlabCommit.getProjectName());
        update.set(Dict.COMMITTER_NAME, gitlabCommit.getCommitter_name());
        update.set(Dict.FILENAMELIST, gitlabCommit.getFileNameList());
        update.set(Dict.FILEDIFFURL, gitlabCommit.getFileDiffUrl());

        mongoTemplate.upsert(query, update, GitlabCommit.class);


    }

    /**
     * 根据姓名和日期查询提交记录
     *
     * @param nickName
     * @param userName
     * @param gituser
     * @param committed_date
     * @return
     */
    @Override
    public List<GitlabCommit> selectGitlabCommitInfo(String nickName, String userName, String gituser, String configname, String committed_date) {
        Query query = new Query();
        if (StringUtils.isNotBlank(configname)) {
            query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(userName), "i").and(Dict.COMMITTED_DATE).is(committed_date), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(nickName), "i").and(Dict.COMMITTED_DATE).is(committed_date), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(gituser), "i").and(Dict.COMMITTED_DATE).is(committed_date), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(configname)).and(Dict.COMMITTED_DATE).is(committed_date)));
        } else {
            query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(userName), "i").and(Dict.COMMITTED_DATE).is(committed_date), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(nickName), "i").and(Dict.COMMITTED_DATE).is(committed_date), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(gituser), "i").and(Dict.COMMITTED_DATE).is(committed_date)));
        }
        return mongoTemplate.find(query, GitlabCommit.class);
    }

    /**
     * 根据姓名列表，日期查询提交记录
     *
     * @param nameList
     * @param committed_date
     * @return
     */
    @Override
    public List<GitlabCommit> selectGitlabCommitInfo(List nameList, String committed_date) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).in(nameList).and(Dict.COMMITTED_DATE).is(committed_date)));
        return mongoTemplate.find(query, GitlabCommit.class);
    }

    /**
     * 根据姓名，日期和项目列表查询提交记录
     *
     * @param nickName
     * @param userName
     * @param gituser
     * @param committed_date
     * @param projectlist
     * @return
     */
    @Override
    public List<GitlabCommit> selectGitlabCommitInfo(String nickName, String userName, String gituser, String configname, String committed_date, List projectlist) {
        Query query = new Query();
        if (StringUtils.isNotBlank(configname)) {
            query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(userName), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(nickName), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(gituser), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(configname), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist)));
        } else {
            query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(userName), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(nickName), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).regex(transToFullPattern(gituser), "i").and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist)));
        }
        //query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).is(userName).and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).is(nickName).and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).is(gituser).and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist), Criteria.where(Dict.COMMITTER_NAME).is(configname).and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist)));
        return mongoTemplate.find(query, GitlabCommit.class);
    }

    /**
     * 根据姓名列表，日期和项目列表查询提交记录
     *
     * @param nameList
     * @param committed_date
     * @param projectlist
     * @return
     */
    @Override
    public List<GitlabCommit> selectGitlabCommitInfo(List nameList, String committed_date, List projectlist) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(Dict.COMMITTER_NAME).in(nameList).and(Dict.COMMITTED_DATE).is(committed_date).and(Dict.PROJECTNAME).in(projectlist)));
        return mongoTemplate.find(query, GitlabCommit.class);
    }

    /**
     * 根据项目ID和分支名称获取该分支commit列表
     *
     * @param projectId 项目ID
     * @param ref_name  分支名称
     * @param since     开始时间
     * @param until     结束时间
     * @return
     * @throws IOException
     */
    @Override
    public List<Map<String, Object>> findOneBranchCommitList(int projectId, String ref_name, String since, String until/*, int page, int per_page*/) throws IOException {
        boolean flag = true;
        int i = 1;
        List<Map<String, Object>> result = new ArrayList<>();
        while (flag) {
            String url = gitlabApiUrl+"projects/" + projectId + "/repository/commits?ref_name=" + ref_name + "&page=" + i + "&per_page=100&since=" + since + "&until=" + until;
            String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> oneBranchCommitList = Util.stringToList(body);
                if (oneBranchCommitList != null && oneBranchCommitList.size() > 0) {
                    result.addAll(oneBranchCommitList);
                    i++;
                } else
                    flag = false;
            } else if (body == null)
                flag = false;
        }
        logger.info(">>项目：" + projectId + ">>ref_name：" + ref_name + ">>项目提交次数：" + result.size());
        return result;
    }

    /**
     * 根绝项目ID和commit版本获取该commit详情
     *
     * @param projectId
     * @param hashVersion
     * @return
     */
    @Override
    public Map<String, Object> findCommitDetail(int projectId, String hashVersion) throws IOException {
        String url = gitlabApiUrl+"projects/" + projectId + "/repository/commits/" + hashVersion;
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> commitDetailMap = objectMapper.readValue(body, Map.class);
        //logger.info("getCommitDetail:" + commitDetailMap.toString());
        return commitDetailMap;
    }

    /**
     * 根据项目ID和commit版本获取该commit修改内容
     *
     * @param projectId
     * @param hashVersion
     * @return
     * @throws IOException
     */
    @Override
    public List<Map<String, Object>> findCommitDiff(int projectId, String hashVersion) throws IOException {
        String url = gitlabApiUrl+"projects/" + projectId + "/repository/commits/" + hashVersion + "/diff";
        String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
        List<Map<String, Object>> commitDiffList = Util.stringToList(body);
        //logger.info("getCommitDiff:" + commitDiffList.toString());
        return commitDiffList;
    }

    /**
     * 对查询时忽略大小写进行完全匹配
     */
    private String transToFullPattern(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("^").append(name).append("$");
        return stringBuilder.toString();
    }
}

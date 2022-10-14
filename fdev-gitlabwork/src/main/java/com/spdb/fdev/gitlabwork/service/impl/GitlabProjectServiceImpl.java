package com.spdb.fdev.gitlabwork.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.gitlabwork.dao.IGitlabProjectDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.service.IGitlabProjectService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitlabProjectServiceImpl
 *
 * @blame Android Team
 */
@Service
@RefreshScope
public class GitlabProjectServiceImpl implements IGitlabProjectService {

    @Autowired
    IGitlabProjectDao gitLabProjectDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTransport restTransport;

    /**
     * 从Fedv查询所有应用，更新gitlabPeoject信息
     */
    @Override
    public List<GitlabProject> selectProjectAndSave() {

        Map<String, Object> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryApp");
        List<Map<String, Object>> appList;
        try {
            appList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调应用模块接口(/api/app/query)出错：" + e.getMessage()});
        }
        for (Map<String, Object> map : appList) {
            JSONObject jObject = (JSONObject) JSONObject.toJSON(map);
            if (jObject != null) {
                this.setGitlabProject(jObject);// 新增
            }
        }
        return gitLabProjectDao.select();
    }

    /**
     * 从数据库查询应用信息
     */
    @Override
    public List<GitlabProject> select() {
        return gitLabProjectDao.select();
    }

    @Override
    public GitlabProject selectByName(String projectName) {
        return gitLabProjectDao.selectByName(projectName);
    }

    /**
     * 根据Fdev的返回结果初始化项目表信息
     *
     * @param jObject
     */
    public void setGitlabProject(JSONObject jObject) {
        GitlabProject gitlabProject = new GitlabProject();
        gitlabProject.setProject_id(jObject.getString(Dict.GITLAB_PROJECT_ID).trim());// 应用Id
        gitlabProject.setNameEn(jObject.getString(Dict.NAME_EN).trim());// 应用英文名
        gitlabProject.setNameCh(jObject.getString(Dict.NAME_ZH).trim());// 应用中文名
        gitlabProject.setGroup(jObject.getString(Dict.GROUP).trim());// 组
        String git = jObject.getString(Dict.GIT).trim();
        gitlabProject.setGit(git);// git地址
        if (StringUtils.isNotBlank(git)) {
            gitlabProject.setWeb_url(git.substring(0, git.length() - 4));
        }
        //如果是新增的项目，状态值改为0
        gitlabProject.setSign(0);
        GitlabProject project = gitLabProjectDao.selectByProjectId(gitlabProject.getProject_id());
        if (project == null)
            gitLabProjectDao.save(gitlabProject);
        else {
            gitlabProject.setSign(project.getSign());
            gitLabProjectDao.upsert(gitlabProject);
        }
    }
}

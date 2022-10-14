package com.spdb.fdev.gitlabwork.schedule.service;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.gitlabwork.dao.IGitLabUserDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabCommitDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabProjectDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.entiy.StatisticCodeLine;
import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import com.spdb.fdev.gitlabwork.service.IGitlabWorkService;
import com.spdb.fdev.gitlabwork.service.IStatisticCodeLineService;
import com.spdb.fdev.gitlabwork.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

@Component
@RefreshScope
public class UpdateGitlabWorkTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IGitlabCommitDao gitlabCommitDao;
    @Autowired
    IGitlabProjectDao gitlabProjectDao;
    @Autowired
    IGitLabUserDao gitLabUserDao;
    @Autowired
    IGitlabWorkService gitlabWorkService;
    @Autowired
    IGitLabUserService gitLabUserService;
    @Autowired
    private IGitlabWorkDao gitlabWorkDao;
    @Autowired
    private IStatisticCodeLineService statisticsService;

    @Value("${schedule.since}")
    private String startDate;

    public void updateWorkInfo() {
        try {
            this.updateNewUser();
            this.updateExistUserAndProject();
            this.updateExistUserAndNewProject();
            gitLabUserDao.updateSign(0, 1);
            gitlabProjectDao.updateSign(1, 2);
        } catch (Exception e) {
            logger.error(">>>>>UpdateGitlabWorkTask.updateWorkInfo更新汇总信息失败", e.getMessage());
        }
    }

    /**
     * 新增的用户，汇总所有项目的commit信息
     */
    public void updateNewUser() throws ParseException {
        String endDate = Util.simpleDateFormat("yyyy-MM-dd").format(new Date());
        logger.info(">>>>>新增的用户汇总所有项目的commit信息开始" + startDate + "和结束时间" + endDate);
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        List<GitlabUser> newUserList = gitLabUserDao.selectBySign(0);
        for (int i = 0; i < newUserList.size(); i++) {
            GitlabUser gitlabUser = newUserList.get(i);
            logger.info("新增用户汇总至第" + (i + 1) + "个，共" + newUserList.size() + "个");
            //如果是新增的用户，汇总开始时间到当天的所有应用的提交记录
            List<String> dateList = Util.getBetweenDates(startDate, endDate);
            for (String committed_date : dateList) {
                Map<String, Object> resultMap = new HashMap<>();
                List<Map<String, Object>> resultDeatilMapList = new ArrayList<>();
                List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getName(), gitlabUser.getUsername(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date);
                if (gitlabCommitList.size() > 0) {
                    gitlabWorkService.addCommitInfoToMap(gitlabCommitList, resultMap, resultDeatilMapList, gitlabUser);
                }
                if (resultMap.size() > 0)
                    resultMapList.add(resultMap);
            }
        }
        gitlabWorkService.addWorkInfoFromMap(resultMapList);
    }

    /**
     * 已存在的用户和已汇总项目，汇总前一天的记录
     */
    public void updateExistUserAndProject() {
        logger.info(">>>>>已存在的用户和已汇总项目，汇总前一天的记录开始" + Util.getDateBefore());
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        List<GitlabUser> existUserList = gitLabUserDao.selectBySign(1);
        for (int i = 0; i < existUserList.size(); i++) {
            GitlabUser gitlabUser = existUserList.get(i);
            logger.info("存在的用户和已汇总项目汇总至第" + (i + 1) + "个，共" + existUserList.size() + "个");
            List<GitlabProject> gitlabProjectList = gitlabProjectDao.selectBySign(2);
            List<String> projectList = new ArrayList();
            for (GitlabProject gitlabProject : gitlabProjectList) {
                String projectName = gitlabProject.getNameEn();
                projectList.add(projectName);
            }
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultDeatilMapList = new ArrayList<>();
            String committed_date = Util.getDateBefore();
            List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getName(), gitlabUser.getUsername(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date, projectList);
            if (gitlabCommitList.size() > 0) {
                gitlabWorkService.addCommitInfoToMap(gitlabCommitList, resultMap, resultDeatilMapList, gitlabUser);
            }
            if (resultMap.size() > 0)
                resultMapList.add(resultMap);
        }
        gitlabWorkService.addWorkInfoFromMap(resultMapList);

    }

    /**
     * 已存在的用户和新增的应用，汇总前一周的提交记录，并且完成对历史数据的更新
     */
    public void updateExistUserAndNewProject() throws ParseException {
        String startDate = Util.getWeekBefore();
        String endDate = Util.simpleDateFormat("yyyy-MM-dd").format(new Date());
        logger.info(">>>>>已存在的用户和新增的应用，汇总所有的提交记录开始" + startDate + "和结束时间" + endDate);
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        List<GitlabUser> existUserList = gitLabUserDao.selectBySign(1);
        List<GitlabProject> gitlabProjectList = gitlabProjectDao.selectBySign(1);
        if (null != gitlabProjectList && gitlabProjectList.size() > 0) {
            logger.info(">>>>新增应用共" + gitlabProjectList.size() + "条");
            for (int i = 0; i < existUserList.size(); i++) {
                GitlabUser gitlabUser = existUserList.get(i);
                logger.info("已存在的用户和新增的应用汇总至第" + (i + 1) + "个，共" + existUserList.size() + "个");
                List<String> projectList = new ArrayList();
                for (GitlabProject gitlabProject : gitlabProjectList) {
                    String projectName = gitlabProject.getNameEn();
                    projectList.add(projectName);
                }
                List<String> dateList = Util.getBetweenDates(startDate, endDate);
                for (String committed_date : dateList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    List<Map<String, Object>> resultDeatilMapList = new ArrayList<>();
                    List<GitlabCommit> gitlabCommitList = gitlabCommitDao.selectGitlabCommitInfo(gitlabUser.getName(), gitlabUser.getUsername(), gitlabUser.getGituser(), gitlabUser.getConfigname(), committed_date, projectList);
                    if (gitlabCommitList.size() > 0) {
                        gitlabWorkService.addCommitInfoToMap(gitlabCommitList, resultMap, resultDeatilMapList, gitlabUser);
                    }
                    if (resultMap.size() > 0)
                        resultMapList.add(resultMap);
                }
            }
            gitlabWorkService.updateWorkInfoFromMap(resultMapList);//如果在之前日期有汇总记录，对于新增的应用，对两条数据合并
        }

    }

    public List<String> getNameList(GitlabUser gitlabUser) {
        List<String> nameList = new ArrayList<>();
        nameList.add(gitlabUser.getUsername());
        nameList.add(gitlabUser.getName());
        nameList.add(gitlabUser.getGituser());
        nameList.add(gitlabUser.getConfigname());
        return nameList;
    }


}

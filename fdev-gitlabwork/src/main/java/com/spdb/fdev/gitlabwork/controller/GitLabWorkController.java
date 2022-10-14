package com.spdb.fdev.gitlabwork.controller;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.gitlabwork.dao.IGitLabUserDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabCommitDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import com.spdb.fdev.gitlabwork.service.GitlabRepositoryService;
import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import com.spdb.fdev.gitlabwork.service.IGitlabProjectService;
import com.spdb.fdev.gitlabwork.service.IGitlabWorkService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * GitLabWorkController
 *
 * @blame Android Team
 */
@RestController
@RefreshScope
@RequestMapping("/api/work")
public class GitLabWorkController {
    private static final Logger logger = LoggerFactory.getLogger(GitLabWorkController.class);

    @Autowired
    private IGitlabCommitDao gitlabCommitDao;

    @Autowired
    private IGitLabUserDao gitLabUserDao;

    @Autowired
    private IGitlabWorkDao gitlabWorkDao;

    @Autowired
    private IGitLabUserService gitLabUserService;

    @Autowired
    private IGitlabWorkService gitlabWorkService;

    @Autowired
    IGitlabProjectService gitlabProjectService;

    @Autowired
    GitlabRepositoryService gitlabRepositoryService;

    @Value("${schedule.since}")
    private String startDate;

    /**
     * 查询commit汇总信息
     * 查询条件组id，公司id，角色名，开始时间，结束时间
     *
     * @return
     */
    @PostMapping("/queryGitlabCommitInfoByGroupUser")
    public JsonResult queryGitlabCommitInfoByGroupUser(@RequestBody Map<String, Object> param) throws ParseException {
        String groupId = (String) param.get("groupId");
        String companyId = (String) param.get("companyId");
        String roleName = (String) param.get("roleName");
        String startDate = (String) param.get(Dict.STARTDATE);
        String endDate = (String) param.get(Dict.ENDDATE);
        String status = (String) param.get(Dict.STATUS);
        String area = (String) param.get(Dict.AREA);
        int page = (int) param.get(Dict.PAGE);
        int per_page = (int) param.get(Dict.PER_PAGE);
        Map<String, Object> userMap = gitLabUserService.selectByGroupIdAndCompanyIdRoleName(groupId, companyId, roleName, status, area, page, per_page);
        List<GitlabUser> users = (List<GitlabUser>) userMap.get("dbs");
        Integer total = Integer.parseInt(String.valueOf(userMap.get(Dict.TOTAL)));
        List<GitlabWork> result = new ArrayList<>();
        for (GitlabUser user : users) {
            String userName = user.getGituser();
            String companyId1 = user.getCompanyid();
            List<GitlabWork> gitlabWorkList = gitlabWorkDao.select(userName, companyId1, startDate, endDate);
            if (gitlabWorkList == null || gitlabWorkList.size() == 0) {
                gitlabWorkList = new ArrayList<>();
                gitlabWorkList.add(gitlabWorkService.initGitlabUser(user));
            }
            result.addAll(gitlabWorkList);
        }
        Map<String, GitlabWork> map = new HashMap();
        //合并所有姓名相同的提交数据（sit存在不同用户使用同一gituserName的情况，导致数据统计异常）
        for (int i = 0; i < result.size(); i++) {
            GitlabWork gitlabWork = result.get(i);
            GitlabWork work = map.get(gitlabWork.getUserName());
            if (work == null) {
                gitlabWork.setStartDate(gitlabWork.getCommittedDate());
                gitlabWork.setEndDate(gitlabWork.getCommittedDate());
                map.put(gitlabWork.getUserName(), gitlabWork);
            } else {
                map.put(gitlabWork.getUserName(), gitlabWorkService.newGitlabWork(work, gitlabWork));
            }

        }
        result = new ArrayList<>(map.values());
        for (GitlabWork gitlabWork : result) {
            GitlabUser user = gitLabUserService.selectByGitUser(gitlabWork.getUserName());
            if (user != null) {
                gitlabWork.setRolename(user.getRolename());
                gitlabWork.setUserId(user.getUser_id());
                gitlabWork.setGroupid(user.getGroupid());
                gitlabWork.setGroupname(user.getGroupname());
            }
        }
        Map mapResult = new HashMap();
        mapResult.put(Dict.TOTAL, total);
        mapResult.put("result", result);
        return JsonResultUtil.buildSuccess(mapResult);
    }

    /**
     * 根据人名，开始和结束日期查询详情
     *
     * @param param
     * @return
     */
    @PostMapping("/queryDetailInfo")
    public JsonResult queryDetailInfo(@RequestBody Map<String, String> param) {
        String name = param.get(Dict.USERNAME);
        String startDate = param.get(Dict.STARTDATE);
        String endDate = param.get(Dict.ENDDATE);
        List<GitlabWork> gitlabWorkList = gitlabWorkDao.select1(name, startDate, endDate);
        List<Map> detail = new ArrayList();
        for (int i = 0; i < gitlabWorkList.size(); i++) {
            GitlabWork gitlabWork = gitlabWorkList.get(i);
            detail.addAll(gitlabWork.getDeatil());
        }
        return JsonResultUtil.buildSuccess(detail);
    }

    @PostMapping("/getProjectUrl")
    public JsonResult getProjectUrl(@RequestBody Map<String, String> param) {
        String projectName = param.get(Dict.PROJECTNAME);
        String short_id = param.get(Dict.SHORT_ID);
        String projectUrl = "";
        try {
            GitlabProject project = gitlabProjectService.selectByName(projectName);
            //根据项目id获取从git上获取项目跳转路径
            Map gitInfo = gitlabRepositoryService.queryProjectUrlById(project.getProject_id());
            projectUrl = gitInfo.get(Dict.WEB_URL) + "/commit/" + short_id;
        } catch (Exception e) {
            logger.info("从gitlab获取项目"+projectName+"差异链接失败！");
        }
        return JsonResultUtil.buildSuccess(projectUrl);
    }

    @ApiOperation(value = "项目commit详情批量添加commit短id")
    @PostMapping("/insertShortId")
    public JsonResult insertShortId() {
        List<GitlabWork> gitlabWorks = gitlabWorkDao.select();
        for (GitlabWork gitlabWork : gitlabWorks) {
            List<Map> deatils = gitlabWork.getDeatil();
            for (Map map : deatils) {
                String fileDiffUrl = (String) map.get(Dict.FILEDIFFURL);
                String[] strs = fileDiffUrl.split("/");
                String shortId = strs[strs.length - 1];
                map.put(Dict.SHORT_ID, shortId);
            }
        }
        gitlabWorkService.insertShortId(gitlabWorks);
        return JsonResultUtil.buildSuccess();
    }

}

package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import com.spdb.fdev.gitlabwork.service.IGitlabWorkService;
import com.spdb.fdev.gitlabwork.service.impl.ExportExcelGitlabWork;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * WorkInfoExportController
 *
 * @blame Android Team
 */
@RestController
@RequestMapping("/api/excel")
public class WorkInfoExportController {

    private static final Logger logger = LoggerFactory.getLogger(WorkInfoExportController.class);

    @Autowired
    private IGitlabWorkDao gitlabWorkDao;

    @Autowired
    private IGitLabUserService gitLabUserService;

    @Autowired
    private IGitlabWorkService gitlabWorkService;

    /**
     * 将汇总信息导出为Excel
     *
     * @return
     */
    @GetMapping("/excelExport")
    public void excelExport(String roleName, String companyId, String startDate, String endDate, String status, String groupId, HttpServletResponse response) throws ParseException {
        if ("companyall".equals(companyId))
            companyId = null;
        if ("--全部--".equals(roleName))
            roleName = null;
        if ("--全部--".equals(status))
            status = null;
        if (StringUtils.isEmpty(groupId.trim()))
            groupId = null;
        GitlabUser params = new GitlabUser();
        params.setCompanyid(companyId);
        params.setRolename(roleName);
        params.setStatus(status);
        params.setGroupid(groupId);
        List<GitlabUser> users = gitLabUserService.selectGitlabUserByParams(params);
        List<GitlabWork> result = new ArrayList<>();
        for (GitlabUser user : users) {
            String userName = user.getGituser();
            List<GitlabWork> gitlabWorkList = gitlabWorkDao.select1(userName, startDate, endDate);
            if (gitlabWorkList == null || gitlabWorkList.size() == 0) {
                gitlabWorkList = new ArrayList<>();
                gitlabWorkList.add(gitlabWorkService.initGitlabUser(user));
            }
            result.addAll(gitlabWorkList);
        }
        Map<String, GitlabWork> map = new HashMap();
        //合并所有姓名相同的提交数据
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
        //暂时改为从用户表中查询角色，后续考虑是否在汇总时加入到汇总表中，避免多次遍历List
        for (GitlabWork gitlabWork : result) {
            GitlabUser user = gitLabUserService.selectByGitUser(gitlabWork.getUserName());
            if (user != null) {
                gitlabWork.setRolename(user.getRolename());
            }
        }
        //对导出的数据按照公司和小组进行排序
        if (result.size() > 0) {
            Collections.sort(result, (o1, o2) -> {
                if (o1.getCompanyname() != null && o2.getCompanyname() != null) {
                    int flag = o2.getCompanyname().compareTo(o1.getCompanyname());
                    if (flag == 0) {
                        if (o1.getGroupname() != null && o2.getGroupname() != null)
                            return o2.getGroupname().compareTo(o1.getGroupname());
                        else
                            return 0;
                    } else
                        return flag;
                }
                return 0;
            });
        }
        Map<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("nicename", "姓名");
        titleMap.put(Dict.COMPANYNAME, "所属公司");
        titleMap.put(Dict.GROUPNAME, "组名");
        titleMap.put(Dict.ROLE, "角色");
        titleMap.put(Dict.TOTAL, "总行数");
        titleMap.put(Dict.ADDITIONS, "添加行数");
        titleMap.put(Dict.DELETIONS, "删除行数");
        titleMap.put(Dict.STARTDATE, "开始日期");
        titleMap.put(Dict.ENDDATE, "结束日期");
        String sheetName = "Gitlab提交汇总";
        logger.info("start导出");
        long start = System.currentTimeMillis();
        ExportExcelGitlabWork.excelExport(startDate, endDate, result, titleMap, sheetName, response);
        long end = System.currentTimeMillis();
        logger.info("end导出");
        logger.info("耗时：" + (end - start) + "ms");
    }
}

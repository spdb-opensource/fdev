package com.spdb.fdev.gitlabwork.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitlabCommitDao;
import com.spdb.fdev.gitlabwork.dao.IGitlabProjectDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.entiy.GitlabProject;
import com.spdb.fdev.gitlabwork.service.IGitlabCommitService;
import com.spdb.fdev.gitlabwork.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * GitlabCommitServiceImpl
 *
 * @blame Android Team
 */
@Service
public class GitlabCommitServiceImpl implements IGitlabCommitService {
    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitServiceImpl.class);

    @Autowired
    IGitlabProjectDao gitlabProjectDao;

    @Autowired
    IGitlabCommitDao gitlabCommitDao;

    /**
     * 添加commit信息，判断是否是新增应用
     * 对于新增应用，同步从配置文件开始日期到前一天的所有记录
     * 对于已存在应用，只同步前一天的commit记录
     * 0 新增 1 已拉取commit记录 2 已汇总
     *
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> addCommitInfoList(String since, String until) throws Exception {
        try {
            List<GitlabProject> projectList = gitlabProjectDao.select();
            for (int i = 0; i < projectList.size(); i++) {
                List<Map<String, Object>> list = new ArrayList<>();
                Map<String, Object> workInfoMap = new HashMap<>();
                logger.info("addCommitInfoList拉取" + projectList.get(i).getNameEn() + "提交记录，第" + (i + 1) + "个项目");
                String sinceDate = since;
                //如果不是新增的项目，就只拉取前一天的commit记录
                GitlabProject gitlabProject = projectList.get(i);
                if (0 != gitlabProject.getSign())
                    sinceDate = Util.getDateBefore();
                workInfoMap.put(Dict.PROJECTNAME, gitlabProject.getNameEn());
                Integer projectId = Integer.valueOf(gitlabProject.getProject_id());
                List<Map<String, Object>> branchList = gitlabProjectDao.findBrancheListById(projectId);
                if (branchList != null && branchList.size() > 0) {
                    findOneBranchCommitList(projectId, branchList, workInfoMap, list, sinceDate, until);
                }
                if (list.size() > 0) {
                    for (int j = 0; j < list.size(); j++) {
                        GitlabCommit gitlabCommit = Util.mapToBean(list.get(j), GitlabCommit.class);
                        gitlabCommitDao.upsert(gitlabCommit);
                    }
                }
                //把已经同步完成的新增应用状态值改为1
                if (0 == gitlabProject.getSign()) {
                    gitlabProject.setSign(1);
                    gitlabProjectDao.upsert(gitlabProject);
                }

            }
        } catch (IOException e) {
            logger.error("GitlabCommitServiceImpl addCommitInfoList添加commit信息错误", e.getMessage());
        }
        //gitlabProjectDao.updateSign(0, 1);//把已经同步完成的新增应用状态值改为1
        return new HashMap<>();
    }


    /**
     * 获取单个分支commit列表
     *
     * @param projectId
     * @param branchList
     * @param workInfoMap
     * @param list
     * @throws IOException
     */
    @Override
    public void findOneBranchCommitList(int projectId, List<Map<String, Object>> branchList, Map<String, Object> workInfoMap, List<Map<String, Object>> list, String since, String until) throws IOException {
        if (branchList.size() > 0) {
            for (int i = 0; i < branchList.size(); i++) {
                Map<String, Object> branchMap = branchList.get(i);
                String branchName = (String) branchMap.get(Dict.NAME);
                workInfoMap.put(Dict.BRANCHNAME, branchName);
                List<Map<String, Object>> oneBranchCommitList = gitlabCommitDao.findOneBranchCommitList(projectId, branchName, since, until);
                findCommitInfo(projectId, oneBranchCommitList, list, workInfoMap);
            }
        }
    }

    /**
     * 获取commit 信息
     *
     * @param projectId
     * @param oneBranchCommitList
     * @param list
     * @param workInfoMap
     * @throws IOException
     */
    @Override
    public void findCommitInfo(int projectId, List<Map<String, Object>> oneBranchCommitList, List<Map<String, Object>> list, Map<String, Object> workInfoMap) throws IOException {
        if (oneBranchCommitList.size() > 0) {
            for (int i = 0; i < oneBranchCommitList.size(); i++) {
                Map<String, Object> oneBranchCommitMap = oneBranchCommitList.get(i);
                //若为合并请求(Merge)，便不记录
                if (((String) oneBranchCommitMap.get("title")).contains("Merge branch"))
                    continue;
                String short_id = (String) oneBranchCommitMap.get(Dict.SHORT_ID);
                Map<String, Object> commitDetailMap = gitlabCommitDao.findCommitDetail(projectId, short_id);
                String committer_name = (String) commitDetailMap.get(Dict.COMMITTER_NAME);
                String committer_email = (String) commitDetailMap.get(Dict.COMMITTER_EMAIL);
                String committed_date = (String) commitDetailMap.get(Dict.COMMITTED_DATE);
                String[] git_date = committed_date.split("T");
                committed_date = git_date[0];
                LinkedHashMap stats = (LinkedHashMap) commitDetailMap.get(Dict.STATS);
                //单个commit提交diff详情
                List<Map<String, Object>> commitDiffList = gitlabCommitDao.findCommitDiff(projectId, short_id);
                List fileNameList = new ArrayList();
                if (commitDiffList.size() > 0) {
                    for (int j = 0; j < commitDiffList.size(); j++) {
                        Map<String, Object> commitDiffMap = commitDiffList.get(j);
                        String fileName = (String) commitDiffMap.get(Dict.OLD_PATH);
                        String fileDiff = (String) commitDiffMap.get(Dict.DIFF);
                        fileNameList.add(fileName);
                    }
                }
                GitlabProject gitlabProject = gitlabProjectDao.selectByName((String) workInfoMap.get(Dict.PROJECTNAME));
                System.out.println("pname:" + gitlabProject.getNameEn());
                String web_url = gitlabProject.getWeb_url();
                String fileDiffUrl = web_url + "/commit/" + short_id;
                Map<String, Object> map = new HashMap<>();
                map.put(Dict.PROJECTNAME, workInfoMap.get(Dict.PROJECTNAME));
                map.put(Dict.BRANCHNAME, workInfoMap.get(Dict.BRANCHNAME));
                map.put(Dict.SHORT_ID, short_id);
                map.put(Dict.COMMITTER_NAME, committer_name);
                map.put(Dict.COMMITTER_EMAIL, committer_email);
                map.put(Dict.COMMITTED_DATE, committed_date);
                map.put(Dict.STATS, stats);
                map.put(Dict.FILENAMELIST, fileNameList);
                map.put(Dict.FILEDIFFURL, fileDiffUrl);
                list.add(map);
            }
        }
    }
}

package com.spdb.fdev.gitlabwork.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabCommit;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import com.spdb.fdev.gitlabwork.service.IGitlabWorkService;
import com.spdb.fdev.gitlabwork.util.Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * GitlabWorkServiceImpl
 *
 * @blame Android Team
 */
@Service
public class GitlabWorkServiceImpl implements IGitlabWorkService {

    @Autowired
    IGitlabWorkDao gitlabWorkDao;


    /**
     * 汇总时初始化没有提交记录的用户
     */
    @Override
    public GitlabWork initGitlabUser(GitlabUser user) {
        GitlabWork gitlabWork = new GitlabWork();
        gitlabWork.setTotal(0);
        gitlabWork.setAdditions(0);
        gitlabWork.setDeletions(0);
        gitlabWork.setDeatil(null);
        gitlabWork.setCommittedDate(null);
        gitlabWork.setUserName(user.getGituser());
        gitlabWork.setNickName(user.getName());
        gitlabWork.setGroupid(user.getGroupid());
        gitlabWork.setGroupname(user.getGroupname());
        gitlabWork.setCompanyname(user.getCompanyname());
        gitlabWork.setCompanyid(user.getCompanyid());
        return gitlabWork;
    }


    /**
     * 合并两个用户名相同的GitlabWork
     *
     * @param work1
     * @param work2
     * @return
     */
    @Override
    public GitlabWork newGitlabWork(GitlabWork work1, GitlabWork work2) throws ParseException {
        work1.setAdditions(work1.getAdditions() + work2.getAdditions());
        work1.setTotal(work1.getTotal() + work2.getTotal());
        Date dateWork1 = null;
        Date dateWork2 = null;
        if (StringUtils.isNotBlank(work1.getCommittedDate()))
            dateWork1 = Util.simpleDateFormat("yyyy-MM-dd").parse(work1.getCommittedDate());
        if (StringUtils.isNotBlank(work2.getCommittedDate()))
            dateWork2 = Util.simpleDateFormat("yyyy-MM-dd").parse(work2.getCommittedDate());
        String startDate = null;
        String endDate = null;
        if (dateWork1 != null && dateWork2 != null) {
            if (dateWork1.compareTo(dateWork2) <= 0) {
                startDate = work1.getCommittedDate();
                endDate = work2.getCommittedDate();
            } else {
                startDate = work2.getCommittedDate();
                endDate = work1.getCommittedDate();
            }
        }

        if (work1.getStartDate() != null) {
            Date date1 = Util.simpleDateFormat("yyyy-MM-dd").parse(work1.getStartDate());
            Date date2 = Util.simpleDateFormat("yyyy-MM-dd").parse(startDate);
            if (date1.compareTo(date2) >= 0) {
                work1.setStartDate(startDate);
            }
        } else {
            work1.setStartDate(startDate);
        }

        if (work1.getEndDate() != null) {
            Date date1 = Util.simpleDateFormat("yyyy-MM-dd").parse(work1.getEndDate());
            Date date2 = Util.simpleDateFormat("yyyy-MM-dd").parse(endDate);
            if (date1.compareTo(date2) <= 0) {
                work1.setEndDate(endDate);
            }
        } else {
            work1.setEndDate(endDate);
        }
        work1.setDeletions(work1.getDeletions() + work2.getDeletions());
        return work1;
    }

    /**
     * 把gitlab查询的Commit提交记录和详情保存到Map中
     *
     * @param gitlabCommitList
     * @param resultMap
     * @param resultDeatilMapList
     */
    @Override
    public void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, String userName, String nickName, String groupid, String groupname, String projectName, String companyid, String companyname) {
        for (int k = 0; k < gitlabCommitList.size(); k++) {
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            int total = (int) status.get(Dict.TOTAL);
            int additions = (int) status.get(Dict.ADDITIONS);
            int deletions = (int) status.get(Dict.DELETIONS);

            if (resultMap.size() <= 0) {
                resultMap.put(Dict.USERNAME, userName);
                resultMap.put(Dict.NICKNAME, nickName);
                resultMap.put(Dict.COMMITTEDDATE, committedDate);
                resultMap.put(Dict.TOTAL, total);
                resultMap.put(Dict.ADDITIONS, additions);
                resultMap.put(Dict.DELETIONS, deletions);
                resultMap.put(Dict.GROUPID, groupid);
                resultMap.put(Dict.GROUPNAME, groupname);
                resultMap.put(Dict.COMPANYID, companyid);
                resultMap.put(Dict.COMPANYNAME, companyname);
            } else {
                resultMap.put(Dict.TOTAL, (int) resultMap.get(Dict.TOTAL) + total);
                resultMap.put(Dict.ADDITIONS, (int) resultMap.get(Dict.ADDITIONS) + additions);
                resultMap.put(Dict.DELETIONS, (int) resultMap.get(Dict.DELETIONS) + deletions);
            }
        }

        for (int k = 0; k < gitlabCommitList.size(); k++) {
            Map<String, Object> resultDeatilMap = new HashMap<>();
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String commitName = gitlabCommit.getCommitter_name();
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            String fileDiffUrl = gitlabCommit.getFileDiffUrl();
            List fileNameList = gitlabCommit.getFileNameList();
            resultDeatilMap.put(Dict.PROJECTNAME, projectName);
            resultDeatilMap.put(Dict.COMMITNAME, commitName);
            resultDeatilMap.put(Dict.NICKNAME, nickName);
            resultDeatilMap.put(Dict.COMMITTEDDATE, committedDate);
            resultDeatilMap.put(Dict.FILENAMELIST, fileNameList);
            resultDeatilMap.put(Dict.STATUS, status);
            resultDeatilMap.put(Dict.FILEDIFFURL, fileDiffUrl);
            resultDeatilMapList.add(resultDeatilMap);
        }
        resultMap.put(Dict.DEATIL, resultDeatilMapList);
    }

    /**
     * 把gitlab查询的Commit提交记录和详情保存到Map中
     *
     * @param gitlabCommitList
     * @param resultMap
     * @param resultDeatilMapList
     */
    @Override
    public void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, String projectName, GitlabUser gitlabUser) {
        for (int k = 0; k < gitlabCommitList.size(); k++) {
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            int total = (int) status.get(Dict.TOTAL);
            int additions = (int) status.get(Dict.ADDITIONS);
            int deletions = (int) status.get(Dict.DELETIONS);

            if (resultMap.size() <= 0) {
                resultMap.put(Dict.COMMITTEDDATE, committedDate);
                resultMap.put(Dict.TOTAL, total);
                resultMap.put(Dict.ADDITIONS, additions);
                resultMap.put(Dict.DELETIONS, deletions);
                resultMap.put(Dict.USERNAME, gitlabUser.getGituser());
                resultMap.put(Dict.NICKNAME, gitlabUser.getName());
                resultMap.put(Dict.GROUPID, gitlabUser.getGroupid());
                resultMap.put(Dict.GROUPNAME, gitlabUser.getGroupname());
                resultMap.put(Dict.COMPANYID, gitlabUser.getCompanyid());
                resultMap.put(Dict.COMPANYNAME, gitlabUser.getCompanyname());
            } else {
                resultMap.put(Dict.TOTAL, (int) resultMap.get(Dict.TOTAL) + total);
                resultMap.put(Dict.ADDITIONS, (int) resultMap.get(Dict.ADDITIONS) + additions);
                resultMap.put(Dict.DELETIONS, (int) resultMap.get(Dict.DELETIONS) + deletions);
            }
        }

        for (int k = 0; k < gitlabCommitList.size(); k++) {
            Map<String, Object> resultDeatilMap = new HashMap<>();
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String commitName = gitlabCommit.getCommitter_name();
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            String fileDiffUrl = gitlabCommit.getFileDiffUrl();
            List fileNameList = gitlabCommit.getFileNameList();
            resultDeatilMap.put(Dict.PROJECTNAME, projectName);
            resultDeatilMap.put(Dict.COMMITNAME, commitName);
            resultDeatilMap.put(Dict.NICKNAME, gitlabUser.getName());
            resultDeatilMap.put(Dict.COMMITTEDDATE, committedDate);
            resultDeatilMap.put(Dict.FILENAMELIST, fileNameList);
            resultDeatilMap.put(Dict.STATUS, status);
            resultDeatilMap.put(Dict.FILEDIFFURL, fileDiffUrl);
            resultDeatilMapList.add(resultDeatilMap);
        }
        resultMap.put(Dict.DEATIL, resultDeatilMapList);
    }

    /**
     * 把gitlab查询的Commit提交记录和详情保存到Map中
     *
     * @param gitlabCommitList
     * @param resultMap
     * @param resultDeatilMapList
     */
    @Override
    public void addCommitInfoToMap(List<GitlabCommit> gitlabCommitList, Map<String, Object> resultMap, List<Map<String, Object>> resultDeatilMapList, GitlabUser gitlabUser) {
        for (int k = 0; k < gitlabCommitList.size(); k++) {
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            int total = (int) status.get(Dict.TOTAL);
            int additions = (int) status.get(Dict.ADDITIONS);
            int deletions = (int) status.get(Dict.DELETIONS);

            if (resultMap.size() <= 0) {
                resultMap.put(Dict.COMMITTEDDATE, committedDate);
                resultMap.put(Dict.TOTAL, total);
                resultMap.put(Dict.ADDITIONS, additions);
                resultMap.put(Dict.DELETIONS, deletions);
                resultMap.put(Dict.USERNAME, gitlabUser.getGituser());
                resultMap.put(Dict.NICKNAME, gitlabUser.getName());
                resultMap.put(Dict.GROUPID, gitlabUser.getGroupid());
                resultMap.put(Dict.GROUPNAME, gitlabUser.getGroupname());
                resultMap.put(Dict.COMPANYID, gitlabUser.getCompanyid());
                resultMap.put(Dict.COMPANYNAME, gitlabUser.getCompanyname());
            } else {
                resultMap.put(Dict.TOTAL, (int) resultMap.get(Dict.TOTAL) + total);
                resultMap.put(Dict.ADDITIONS, (int) resultMap.get(Dict.ADDITIONS) + additions);
                resultMap.put(Dict.DELETIONS, (int) resultMap.get(Dict.DELETIONS) + deletions);
            }
        }

        for (int k = 0; k < gitlabCommitList.size(); k++) {
            Map<String, Object> resultDeatilMap = new HashMap<>();
            GitlabCommit gitlabCommit = gitlabCommitList.get(k);
            String commitName = gitlabCommit.getCommitter_name();
            String committedDate = gitlabCommit.getCommitted_date();
            Map status = gitlabCommit.getStats();
            String fileDiffUrl = gitlabCommit.getFileDiffUrl();
            String shortId = gitlabCommit.getShort_id();
            List fileNameList = gitlabCommit.getFileNameList();
            resultDeatilMap.put(Dict.PROJECTNAME, gitlabCommit.getProjectName());
            resultDeatilMap.put(Dict.COMMITNAME, commitName);
            resultDeatilMap.put(Dict.NICKNAME, gitlabUser.getName());
            resultDeatilMap.put(Dict.COMMITTEDDATE, committedDate);
            resultDeatilMap.put(Dict.FILENAMELIST, fileNameList);
            resultDeatilMap.put(Dict.STATUS, status);
            resultDeatilMap.put(Dict.FILEDIFFURL, fileDiffUrl);
            resultDeatilMap.put(Dict.SHORT_ID, shortId);
            resultDeatilMapList.add(resultDeatilMap);
        }
        resultMap.put(Dict.DEATIL, resultDeatilMapList);
    }


    @Override
    public void addWorkInfoFromMap(List<Map<String, Object>> resultMapList) {
        for (int i = 0; i < resultMapList.size(); i++) {
            Map<String, Object> map = resultMapList.get(i);
            GitlabWork gitlabWork = new GitlabWork();
            gitlabWork.setTotal((Integer) map.get(Dict.TOTAL));
            gitlabWork.setAdditions((Integer) map.get(Dict.ADDITIONS));
            gitlabWork.setDeletions((Integer) map.get(Dict.DELETIONS));
            gitlabWork.setDeatil((List) map.get(Dict.DEATIL));
            gitlabWork.setCommittedDate((String) map.get(Dict.COMMITTEDDATE));
            gitlabWork.setUserName((String) map.get(Dict.USERNAME));
            gitlabWork.setNickName((String) map.get(Dict.NICKNAME));
            gitlabWork.setGroupid((String) map.get(Dict.GROUPID));
            gitlabWork.setGroupname((String) map.get(Dict.GROUPNAME));
            gitlabWork.setCompanyid((String) map.get(Dict.COMPANYID));
            gitlabWork.setCompanyname((String) map.get(Dict.COMPANYNAME));
            gitlabWorkDao.upsert(gitlabWork);
        }
    }

    @Override
    public void updateWorkInfoFromMap(List<Map<String, Object>> resultMapList) {
        for (int i = 0; i < resultMapList.size(); i++) {
            Map<String, Object> map = resultMapList.get(i);
            GitlabWork gitlabWork = new GitlabWork();
            gitlabWork.setTotal((Integer) map.get(Dict.TOTAL));
            gitlabWork.setAdditions((Integer) map.get(Dict.ADDITIONS));
            gitlabWork.setDeletions((Integer) map.get(Dict.DELETIONS));
            gitlabWork.setDeatil((List) map.get(Dict.DEATIL));
            gitlabWork.setCommittedDate((String) map.get(Dict.COMMITTEDDATE));
            gitlabWork.setUserName((String) map.get(Dict.USERNAME));
            gitlabWork.setNickName((String) map.get(Dict.NICKNAME));
            gitlabWork.setGroupid((String) map.get(Dict.GROUPID));
            gitlabWork.setGroupname((String) map.get(Dict.GROUPNAME));
            gitlabWork.setCompanyid((String) map.get(Dict.COMPANYID));
            gitlabWork.setCompanyname((String) map.get(Dict.COMPANYNAME));
            GitlabWork work = gitlabWorkDao.selectByUserNameAndCommitDate(gitlabWork.getUserName(), gitlabWork.getCommittedDate());
            if (work == null)
                gitlabWorkDao.save(gitlabWork);
            else {
                gitlabWork.setTotal(work.getTotal() + gitlabWork.getTotal());
                gitlabWork.setAdditions(work.getAdditions() + gitlabWork.getAdditions());
                gitlabWork.setDeletions(work.getDeletions() + gitlabWork.getDeletions());
                List detail = new ArrayList();
                detail.addAll(work.getDeatil());
                detail.addAll(gitlabWork.getDeatil());
                gitlabWork.setDeatil(detail);
                gitlabWorkDao.upsert(gitlabWork);
            }

        }
    }

    @Override
    public void insertShortId(List<GitlabWork> gitlabWorks) {
        for(GitlabWork gitlabWork : gitlabWorks){
            gitlabWorkDao.upsert(gitlabWork);
        }
    }
}

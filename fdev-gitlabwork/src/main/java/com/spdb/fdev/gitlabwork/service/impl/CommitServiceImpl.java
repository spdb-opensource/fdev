package com.spdb.fdev.gitlabwork.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.config.GitlabApiConfig;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.gitlabwork.dao.CommitDao;
import com.spdb.fdev.gitlabwork.dto.CommitUpdate;
import com.spdb.fdev.gitlabwork.entiy.Commit;
import com.spdb.fdev.gitlabwork.service.CommitService;
import com.spdb.fdev.gitlabwork.util.Util;
import com.spdb.fdev.transport.RestTransport;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CommitServiceImpl implements CommitService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GitlabApiConfig gitlabApiConfig;

    @Autowired
    private CommitDao commitDao;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void updateCommit(CommitUpdate commitUpdate) throws Exception {
        log.info("------------------------开始：同步gitlab代码提交------------------------");
        log.info("------------------------开始：获取gitlab代码提交------------------------");
        GitLabApi gitLabApi = gitlabApiConfig.getGitLabApi();
        Date until = commitUpdate.getUntil() == null ? TimeUtils.getYesterdayMax(new Date()) : TimeUtils.getDayMax(TimeUtils.FORMAT_DATESTAMP.parse(commitUpdate.getUntil()));
        Date since = commitUpdate.getSince() == null ? TimeUtils.getDayMin(until) : TimeUtils.getDayMin(TimeUtils.FORMAT_DATESTAMP.parse(commitUpdate.getSince()));
        //汇总需要同步的gitId
        Set<Integer> projectIds = getSelectProjectId();
        //Set<Commit> record = new HashSet<>();
        //Git服务太脆弱了不能用太多线程请求
        Set<Commit> record = Collections.synchronizedSet(new HashSet<>());
        //准备线程池 - 根据projectId查commit的线程池
        ExecutorService projectCommitEs = Executors.newFixedThreadPool(8);//目标服务器扛不住啊
        //projectIds.add(2163);
        for (Integer projectId : projectIds) {
            //if (2163 != projectId) continue;
            try {
                Project project = gitLabApi.getProjectApi().getProject(projectId);
                projectCommitEs.submit(() -> {
                    List<org.gitlab4j.api.models.Commit> commits = new ArrayList<>();
                    // 以下两种方法都可以达到目的，经过尝试选择方法1(方法2调用大量接口不说而且还非常慢)
                    //1.Commit
                    try {
                        commits.addAll(gitLabApi.getCommitsApi().getCommitStream(projectId).filter(commit -> {
                            return since.compareTo(commit.getCommittedDate()) <= 0 && until.compareTo(commit.getCommittedDate()) >= 0 //时间过滤条件
                                    && !commit.getTitle().contains("Merge branch") && !commit.getTitle().contains("Merge remote-tracking branch");//排除合并请求
                        }).collect(Collectors.toList()));
                    } catch (GitLabApiException e) {
                        log.info(e.getMessage() + ":projectId=" + projectId);
                    }
                    // 2.Branch
//            List<Branch> branches = null;
//            try {
//                branches = gitLabApi.getRepositoryApi().getBranches(projectId);
//            } catch (GitLabApiException e) {//我真的是快烦死了，一天到晚都要考虑这种脏数据
//                log.info(e.getMessage() + ":projectId=" + projectId);
//                continue;//查不到就跳过，这里日志记录一下
//            }
//            for (Branch branch : branches) {//这里我有点无语，查提交记录必须查需分支，不过也好，我想这样应该更加提高api效率
//                commits.addAll(gitLabApi.getCommitsApi().getCommits(projectId, branch.getName(), since, until));
//            }
                    //---------------------------------------------------------------------------
                    for (org.gitlab4j.api.models.Commit commit : commits) {
                        Commit myCommit = new Commit();
                        BeanUtils.copyProperties(commit, myCommit);
                        myCommit.setProjectId(projectId);
                        myCommit.setProjectName(project.getName());
                        myCommit.setCommittedDate(TimeUtils.FORMAT_TIMESTAMP.format(commit.getCommittedDate()));
                        record.add(myCommit);
                    }
                });
            } catch (GitLabApiException e) {//我真的是快烦死了，一天到晚都要考虑这种脏数据
                log.info(e.getMessage() + ":projectId=" + projectId);
            }
        }
        projectCommitEs.shutdown();
        projectCommitEs.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        log.info("------------------------结束：获取gitlab代码提交------------------------");
        setCommitStats(record);
        commitDao.upert(record);
        log.info("------------------------结束：同步gitlab代码提交------------------------");
    }

    /**
     * 获取需要同步的项目的gitId
     * @return
     */
    private Set<Integer> getSelectProjectId() throws Exception {
        //同步应用的项目
        List<Map<String, Object>> apps = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryApp");
        }});
        //同步组件的项目
        List<Map<String, Object>> components = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryComponents");
        }});
        List<Map<String, Object>> mpassComponents = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryMpassComponents");
        }});
        //同步镜像的项目
        List<Map<String, Object>> baseImages = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryBaseImage");
        }});
        //同步骨架的项目
        List<Map<String, Object>> archetypes = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryArchetypes");
        }});
        List<Map<String, Object>> mpassArchetypes = (List<Map<String, Object>>) this.restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "queryMpassArchetypes");
        }});
        //汇总需要同步的gitId
        Set<Integer> projectIds = apps.stream().map(x -> (Integer) x.get(Dict.GITLAB_PROJECT_ID)).collect(Collectors.toSet());
        Set<Integer> componentProjectIds = components.stream().filter(x -> !CommonUtil.isNullOrEmpty(x.get(Dict.GITLAB_ID))).map(x -> Integer.parseInt(x.get(Dict.GITLAB_ID).toString())).collect(Collectors.toSet());
        Set<Integer> mpassComponentProjectIds = mpassComponents.stream().filter(x -> !CommonUtil.isNullOrEmpty(x.get(Dict.GITLAB_ID))).map(x -> Integer.parseInt(x.get(Dict.GITLAB_ID).toString())).collect(Collectors.toSet());
        Set<Integer> baseImageProjectIds = baseImages.stream().filter(x -> !CommonUtil.isNullOrEmpty(x.get(Dict.GITLAB_ID))).map(x -> Integer.parseInt(x.get(Dict.GITLAB_ID).toString())).collect(Collectors.toSet());
        Set<Integer> archetypeProjectIds = archetypes.stream().filter(x -> !CommonUtil.isNullOrEmpty(x.get(Dict.GITLAB_ID))).map(x -> Integer.parseInt(x.get(Dict.GITLAB_ID).toString())).collect(Collectors.toSet());
        Set<Integer> mpassArcheProjectIds = mpassArchetypes.stream().filter(x -> !CommonUtil.isNullOrEmpty(x.get(Dict.GITLAB_ID))).map(x -> Integer.parseInt(x.get(Dict.GITLAB_ID).toString())).collect(Collectors.toSet());
        projectIds.addAll(componentProjectIds);
        projectIds.addAll(mpassComponentProjectIds);
        projectIds.addAll(baseImageProjectIds);
        projectIds.addAll(archetypeProjectIds);
        projectIds.addAll(mpassArcheProjectIds);
        return projectIds;
    }

    @Override
    public void updateCommitStats() throws Exception {//数据量过大同步用这个吧，反正接口IO也慢不需要批量插入了
        List<Commit> unStats = commitDao.findUnStats();
        setCommitStats(unStats, true);
//        update();//测试用
    }

    @Override
    public List<String> queryCommitDiff(Integer projectId, String shortId) throws Exception {
        JSONArray objects = JSONObject.parseArray(Util.httpMethodGetExchangeWithOutTryCatch(gitlabApiConfig.getApiUrl() + "projects/" + projectId + "/repository/commits/" + shortId + "/diff", restTemplate, gitlabApiConfig.getToken()));
        return objects.stream().map(object -> ((JSONObject) object).getString("new_path")).collect(Collectors.toList());
    }

    private void setCommitStats(Collection<Commit> commits) throws InterruptedException {
        setCommitStats(commits, false);
    }

    private void setCommitStats(Collection<Commit> commits, Boolean isUpdateStats) throws InterruptedException {
        log.info("------------------------开始：获取gitlab提交详情------------------------");
        log.info("------------------------" + commits.size() + "------------------------");
        GitLabApi gitLabApi = gitlabApiConfig.getGitLabApi();
        ExecutorService commitEs = Executors.newFixedThreadPool(8);//目标服务器扛不住啊
        for (Commit commit : commits) {
            commitEs.submit(() -> {
                try {
                    org.gitlab4j.api.models.Commit object = gitLabApi.getCommitsApi().getCommit(commit.getProjectId(), commit.getShortId());//这里要吐槽一下，包无法升级导致Api数据有限，只能使用http
                    commit.setStats(object.getStats());
                    commit.setWebUrl(object.getWebUrl());
                    if (isUpdateStats) commitDao.updateStats(commit);
                } catch (Exception e) {
                    log.info(e.getMessage() + ":projectId=" + commit.getProjectId() + "&commitShortId=" + commit.getShortId());
                }
            });
        }
        commitEs.shutdown();
        commitEs.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        log.info("------------------------结束：获取gitlab提交详情------------------------");
    }

    private void update() throws Exception {
        List<Commit> all = commitDao.findAll();
        all.removeIf(commit -> {
            try {
                TimeUtils.FORMAT_TIMESTAMP.parse(commit.getCommittedDate());
                return true;
            } catch (ParseException e) {
                // Thu Oct 29 16:09:10 GMT+08:00 2020
                String[] s = commit.getCommittedDate().split(" ");
                String year = s[5];
                String month = getMonth(s[1]);
                String day = s[2];
                String time = s[3];
                commit.setCommittedDate(year + "-" + month + "-" + day + " " + time);
                return false;
            } catch (NullPointerException e){
                return true;
            }
        });
        commitDao.upertCommittedTitle(all);
    }

    private String getMonth(String year) {
        switch (year) {
            case "Jan":
                return "01";
            case "Feb":
                return "02";
            case "Mar":
                return "03";
            case "Apr":
                return "04";
            case "May":
                return "05";
            case "Jun":
                return "06";
            case "Jul":
                return "07";
            case "Aug":
                return "08";
            case "Sep":
                return "09";
            case "Oct":
                return "10";
            case "Nov":
                return "11";
            case "Dec":
                return "12";
            default:
                return null;
        }
    }

}

package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.DealGitApiUnit;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitHttpServer;
import com.spdb.fdev.fdevtask.base.utils.GitlabTransport;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.GitApiService;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RefreshScope
public class GitApiServiceImpl implements GitApiService {

    @Resource
    private IAppApi iAppApi;

    @Autowired
    private IReleaseTaskApi iReleaseTaskApi;

    @Resource
    private DealGitApiUnit dealGitApiUnit;

    @Resource
    private GitlabTransport gitlabTransport;

    @Autowired
    private GitHttpServer gitHttpServer;

    @Autowired
    private IFdevTaskService fdevTaskService;

    private Logger log = LoggerFactory.getLogger(GitApiServiceImpl.class);

    @Value("${gitlib.path}")
    private String url;//http://xxx/api/v4/

    @Value("${git.token}")
    private String token;

    private static String suffixConfict = "projects/{$id}/repository/compare?from=master&to={$branch}";
    private static String suffixCommit = "projects/{$id}/repository/compare?from={$branch}&to=master";
    private static String mergeRequest = "projects/{$id}/merge_requests?state=merged&target_branch={$SIT}&source_branch={$branch}";

    @Override
    public Tuple.Tuple3 getAfterCommits(String gitlabId, String branch) throws Exception {
        // 构建请求 1.url
        ExecutorService es = Executors.newFixedThreadPool(2);
        String commits = suffixCommit.replace("{$id}", gitlabId).replace("{$branch}", branch);
        Future<Map> commit = es.submit(() -> {
            //获取落后commit
            log.info("获取冲突请求的url:{}", url + commits);
            return dealGitApiUnit.sendReqToGitServer(url + commits);
        });
        String replace = suffixConfict.replace("{$id}", gitlabId).replace("{$branch}", branch);
        Future<Map> conflit = es.submit(() -> {
            //获取冲突
            log.info("获取冲突请求的url:{}", url + replace);
            return dealGitApiUnit.sendReqToGitServer(url + replace);
        });
        //等待结果
        Map mapCommit = commit.get();
        Map mapconflit = conflit.get();
        es.shutdown();
        //生产上存在git偶尔404问题
        if (CommonUtils.isNullOrEmpty(mapCommit)) {
            log.info("git又不通了");
            return Tuple.tuple(0, 0, Collections.emptyList());
        }
        //替换
        mapCommit.put("diffs", mapconflit.get("diffs"));
        // 处理请求结果,获取落后版本数、文件清单等
        if (!CommonUtils.isNullOrEmpty(mapCommit)) {
            return dealGitApiUnit.getListAndCommits(mapCommit);
        }
        // 判断是否发送通知
        return Tuple.tuple(0, 0, Collections.emptyList());

    }

    @Override
    public String getConflictInfo(String branch) {
        return null;
    }

    @Override
    public boolean judgeMergeSit(String branchName, String girLabId, String flag) throws Exception {
        //构建请求的url
        String mergReq = mergeRequest.replace("{$id}", girLabId).replace("{$branch}", branchName).replace("{$SIT}",flag);
        //处理请求结果
        JSONArray array = (JSONArray) dealGitApiUnit.sendReqToGitServer(url + mergReq, JSONArray.class);
        //判断是否成功合并
        log.info("请求git的url:{}size{}",url + mergReq,array.size());
        return array.size() >= 1;
    }


    @Override
    public String getLatestCommitSha(Integer gitProjectId, String branchName) {
        String gitlab_url = CommonUtils.projectUrl(url) + "/" + gitProjectId + "/repository/commits?ref_name=" + branchName ;
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, token);
        String result = (String) gitlabTransport.submitGet2(gitlab_url, header);
        JSONArray resultArray = JSON.parseArray(result);
        return CommonUtils.isNullOrEmpty(resultArray)?null:(String) ((JSONObject) resultArray.get(0)).get(Dict.ID);
    }

    @Override
    public Map<String, Object> queryBranch(String gitlabProjectId, String featureBranch) {
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabProjectId + "/repository/branches/"+featureBranch;
        MultiValueMap<String,String> header = new LinkedMultiValueMap<String,String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            String result = (String) gitlabTransport.submitGet2(gitLabUrl, header);
            return JSONObject.parseObject(result, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public List<Map> searchTags(String gitlabId, String tagName) {
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabId + "/repository/tags?search=" + tagName;
        MultiValueMap<String,String> header = new LinkedMultiValueMap<String,String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            String result = (String) gitlabTransport.submitGet2(gitLabUrl, header);
            return JSONArray.parseArray(result, Map.class);
        } catch (Exception e) {
            log.error(">>>>>搜索项目下tag异常,url:{}", gitLabUrl);
            return new ArrayList<>();
        }
    }

    @Override
    public void createTag(String gitlabId, String createFrom, String tagName) {
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabId + "/repository/tags";
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.REF, createFrom);
        param.put(Dict.TAG_NAME, tagName);
        try {
            gitlabTransport.submitPost(gitLabUrl, param);
        } catch (Exception e) {
            log.error(">>>>>>>创建tag包失败,url:{}", gitLabUrl);
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"创建tag包失败"});
        }
    }

    @Override
    public void createBranch(String gitlabId, String branchName, String createFrom) {
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabId + "/repository/branches";
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.REF, createFrom);
        param.put(Dict.BRANCH, branchName);
        try {
            gitlabTransport.submitPost(gitLabUrl, param);
        } catch (Exception e) {
            log.error(">>>>>>>创建分支失败,url:{}", gitLabUrl);
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"创建分支失败"});
        }
    }

    @Override
    public void addMember(String gitlabProjectId, int accessLevel, String gitlabUserId) {
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabProjectId + "/members";
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.USER_ID, gitlabUserId);
        param.put(Dict.ACCESS_LEVEL, accessLevel);
        try {
            gitlabTransport.submitPost(gitLabUrl, param);
        } catch (Exception e) {
            log.info(">>>>>>>添加权限失败,url:{},gitlabId:{}", gitLabUrl, gitlabUserId);
        }
    }

    @Override
    public Map<String, Object> queryMergeInfo(String gitlabProjectId, String mergeId) {
        String gitlabUrl = CommonUtils.projectUrl(url) + "/" + gitlabProjectId + "/merge_requests/" + mergeId;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        //查询merge_request信息
        Object result = gitlabTransport.submitGet2(gitlabUrl, header);
        if(CommonUtils.isNullOrEmpty(result)) {
            return new HashMap<>();
        }
        Map<String, Object> mergeInfo = JSONObject.parseObject((String) result, Map.class);
        String sha = (String) mergeInfo.get("merge_commit_sha");
        if(!CommonUtils.isNullOrEmpty(sha)) {
            //使用sha值查询与之对应的piplines信息
            String pipines_url = CommonUtils.projectUrl(url) + "/" + gitlabProjectId + "/pipelines?sha=" + sha;
            Object pipeline = gitlabTransport.submitGet2(pipines_url, header);
            if(CommonUtils.isNullOrEmpty(pipeline)) {
                return mergeInfo;
            }
            JSONArray parseArray = JSONObject.parseArray((String) pipeline);
            if (CommonUtils.isNullOrEmpty(parseArray)) {
                return mergeInfo;
            }
            Map pipinesInfo = (Map) parseArray.get(0);
            mergeInfo.put(Dict.PIPELINE, pipinesInfo.get(Dict.STATUS));
            return mergeInfo;
        }
        return mergeInfo;
    }

    @Override
    public String createMergeRequest(String gitLabId, String sourceBranch, String targetBranch, String desc, String title, String token) throws Exception {
        String mergeRequestUrl = CommonUtils.projectUrl(url) + "/" + gitLabId + "/merge_requests";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.SOURCE_BRANCH, sourceBranch);
        param.put(Dict.TARGET_BRANCH, targetBranch);
        param.put(Dict.TITLE, title);
        param.put(Dict.DESCRIPTION, desc);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        String resultStr = (String) gitlabTransport.submitPost(mergeRequestUrl, param);
        Map<String, Object> mergeInfo = JSONObject.parseObject(resultStr, Map.class);
        return String.valueOf(mergeInfo.get(Dict.IID));
    }

    @Override
    public List<String> queryProjectBranchList(String gitLabId, String applicationType) {
        List<Map> repositoryList = new ArrayList<>();
        // GitLab page默认为1，per_page默认为20，最大为100
        for (int i = 1; i <= 10; i++) {
            List<Map> repoList = getProjectsBranchByPage(gitLabId, i);
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
        //如果应用类型为镜像，则只返回dev开头的分支
        List<String> result = new ArrayList<>();
        if("image".equals(applicationType)){
            for(Map map : repositoryList){
                String name = (String) map.get("name");
                if(name.startsWith("dev") && !(boolean)map.get("protected")){
                    result.add(name);
                }
            }
        }else {
            for(Map map : repositoryList){
                String name = (String) map.get("name");
                if(!(boolean)map.get("protected")){
                    result.add(name);
                }
            }
        }
        return result;
    }

    @Override
    public Map getMergeRequestInfo(Map requestParam) throws Exception {
        Map mergeStatus = new HashMap();
        if (null == requestParam.get(Dict.ID) || "".equals(requestParam.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "任务id为空"});
        }
        if (null == requestParam.get(Dict.TYPE) || "".equals(requestParam.get(Dict.TYPE))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TYPE + "环境类型信息为空"});
        }
        String task_id = (String) requestParam.get(Dict.ID);
        FdevTask taskParam = null;
        List<FdevTask> list = fdevTaskService.query(new FdevTask.FdevTaskBuilder().id(task_id).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务查询为空"});
        }
        taskParam = list.get(0);// 引用ID
        String projectId = taskParam.getProject_id();
        Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(projectId, taskParam.getApplicationType());
        String gitlabId = (String) projectInfo.get(Dict.GITLAB_ID);
        String type = (String) requestParam.get(Dict.TYPE);
        //获取应用类型
        String flag = "SIT";
        String feature_branch = taskParam.getFeature_branch();
        if (Constants.APPLICATION_IOS.equals(taskParam.getApplicationType()) || Constants.APPLICATION_ANDROID.equals(taskParam.getApplicationType())){
            flag = feature_branch.replaceFirst("feature", "dev");
        }
        //查询合并信息
        boolean merged = judgeMergeSit(taskParam.getFeature_branch(), gitlabId, flag);
        String mergeId = "";
        switch (type.toLowerCase()) {
            case Dict.SIT:
                mergeId = taskParam.getSit_merge_id();
                break;
            case Dict.UAT:
                mergeId = taskParam.getUat_merge_id();
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"环境变量参数异常"});
        }
        if (CommonUtils.isNullOrEmpty(mergeId)) {
            mergeStatus.put(Dict.STATUS_CODE, "0");
            mergeStatus.put(Dict.STATUS_MEAN, "未发起合并");
            mergeStatus.put("merged",merged);
            return mergeStatus;
        }
        mergeStatus = fdevTaskService.queryMergeInfo(gitlabId, mergeId);
        mergeStatus.put("merged",merged);
        if (!CommonUtils.isNullOrEmpty(mergeStatus) && "5".equals(mergeStatus.get("status_code"))) {
            mergeStatus.putAll(queryMerged(requestParam, gitlabId, taskParam.getApplicationType()));
        }
        return mergeStatus;
    }

    @Override
    public Map queryMerged(Map param, String gitlabId, String appType) {
        String taskId = (String) param.get(Dict.ID);
        String stage = (String) param.get(Dict.TYPE);
        String targeBranch = "";
        String sourceBranch = "";
        try {
            FdevTask task = fdevTaskService.queryTaskAll(new FdevTask.FdevTaskBuilder().id(taskId).init());
            if (CommonUtils.isNullOrEmpty(task))
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.TASK});
            switch (stage.toLowerCase()) {
                case Dict.TASK_STAGE_SIT:
                    if(appType.startsWith("app")) {
                        Map appInfo = iAppApi.queryAppById(new HashMap() {{
                            put(Dict.ID, task.getProject_id());
                        }});
                        if (CommonUtils.isNullOrEmpty(appInfo))
                            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.APP});
                        HashSet sit = new HashSet((ArrayList) appInfo.get(Dict.SIT));
                        if (null == sit || sit.size() <= 0) {
                            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->APP 查询该任务SIT分支返回为空"});
                        }
                        List brnames = new ArrayList();
                        sit.forEach(n -> brnames.add(((Map) n).get(Dict.NAME)));
                        targeBranch = (String) brnames.get(0);

                    }else {
                        targeBranch = Constants.BRANCH_SIT;
                    }
                    sourceBranch = task.getFeature_branch();
                    break;
                case Dict.TASK_STAGE_UAT:
                    Map releaseInfo = iReleaseTaskApi.queryDetailByTaskId(taskId, CommonUtils.getReleaseType(appType));
                    if (CommonUtils.isNullOrEmpty(releaseInfo) && stage.equals(Dict.TASK_STAGE_UAT)) {
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.RELEASE_APPLICATION});
                    }
                    Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
                    targeBranch = (String) releaseApp.get(Dict.RELEASE_BRANCH);
                    sourceBranch = task.getFeature_branch();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (e instanceof FdevException)
                throw (FdevException) e;
            else {
                log.error("查询merge信息异常--" + e.getMessage());
            }
        }
        Map gitParam = null;
        boolean merged = false;
        Map tmp = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(gitlabId, sourceBranch, targeBranch)) {
            gitParam = new HashMap();
            gitParam.put(Dict.GITLAB_PROJECT_ID, gitlabId);
            gitParam.put(Dict.STATE, "merged");
            gitParam.put(Dict.SOURCE_BRANCH, sourceBranch);
            gitParam.put(Dict.TARGET_BRANCH, targeBranch);
            merged = !CommonUtils.isNullOrEmpty(gitHttpServer.getMergeInfo(gitParam));
            tmp.put(Dict.STATUS, merged);
        }
        return tmp;
    }

    /**
     * 获取项目所有分支
     *
     * @param gitlabId
     * @param page
     * @return
     */
    public List<Map> getProjectsBranchByPage(String gitlabId, Integer page) {
        List<Map> repositoryList = new ArrayList<>();
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + gitlabId + "/repository/branches?" + "&page=" + page + "&per_page=100";
        MultiValueMap header = new LinkedMultiValueMap();
        header.add("PRIVATE-TOKEN", token);
        try {
            Object submitGet = gitlabTransport.submitGet(gitLabUrl, header);
            String s = JSONArray.toJSONString(submitGet);
            repositoryList = JSONArray.parseArray(s, Map.class);
        } catch (Exception e) {
            log.info("获取项目{}的分支列表失败：{}", gitlabId, e.getMessage());
        }
        return repositoryList;
    }

    @Override
    public Map<String, Object> getMergeInfo(String projectId, String iid) {
        String gitlabUrl = CommonUtils.projectUrl(url) + "/" + projectId + "/merge_requests/" + iid;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        //查询merge_request信息
        Object result = gitlabTransport.submitGet2(gitlabUrl, header);
        if(CommonUtils.isNullOrEmpty(result)) {
            return new HashMap<>();
        }
        return JSONObject.parseObject((String) result, Map.class);
    }
}

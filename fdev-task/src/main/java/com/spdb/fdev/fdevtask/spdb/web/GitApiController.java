package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitHttpServer;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.GitApiService;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "GIT接口")
@RequestMapping("/api/git")
@RestController
@RefreshScope
public class GitApiController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IFdevTaskService fdevTaskService;

    @Autowired
    private IReleaseTaskApi iReleaseTaskApi;

    @Autowired
    private IAppApi iAppApi;

    @Autowired
    private GitHttpServer gitHttpServer;

    @Autowired
    private IAppApi appApi;

    @Autowired
    private GitApiService gitApiService;
    @Value("${git.token}")
    private String token;

    /**
     * @param requestParam
     * @return 0 - 未发起合并 1 - 待合并 2 - 已合并，pipline进行中 3 - 已合并，pipline构建成功 4 -
     * 已合并，pipline构建失败 5 - 合并请求被关闭，可重新发起合并请求
     * @throws Exception 2019年4月1日
     * @Desc
     */

    @ApiOperation(value = "根据任务，查询merge请求的信息")
    @ResponseBody
    @RequestMapping(value = "/queryMergeInfo", method = RequestMethod.POST)
    public JsonResult getMergeRequestInfo(@RequestBody @ApiParam(name = "参数", value = "示例{\"id\":\"5c877a02e43ba200067c8c14\",\"type\":\"sit\" }") Map requestParam) throws Exception {
        Map mergeStatus = gitApiService.getMergeRequestInfo(requestParam);
        return JsonResultUtil.buildSuccess(mergeStatus);
    }

    @ApiOperation(value = "根据任务，查询试运行merge请求的信息")
    @ResponseBody
    @RequestMapping(value = "/queryTestMergeInfo", method = RequestMethod.POST)
    public JsonResult queryTestMergeInfo(@RequestBody Map requestParam) throws Exception {
        Map mergeStatus = new HashMap();
        if (null == requestParam.get(Dict.ID) || "".equals(requestParam.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "任务id为空"});
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
        String mergeId = taskParam.getTest_merge_id();
        if (CommonUtils.isNullOrEmpty(mergeId)) {
            mergeStatus.put(Dict.STATUS_CODE, "0");
            mergeStatus.put(Dict.STATUS_MEAN, "未发起合并");
            return JsonResultUtil.buildSuccess(mergeStatus);
        }
        mergeStatus = fdevTaskService.queryMergeInfo(gitlabId, mergeId);
        if (!CommonUtils.isNullOrEmpty(mergeStatus) && "3".equals(mergeStatus.get(Dict.STATUS_CODE))) {
            mergeStatus.putAll(gitApiService.queryMerged(requestParam, gitlabId, taskParam.getApplicationType()));
        }
        return JsonResultUtil.buildSuccess(mergeStatus);
    }

    @ApiOperation(value = "获取当前分支和master分支信息", notes = "落后commits数")
    @RequestMapping(value = "/queryCommitTips", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKID})
    public JsonResult queryCommitInfo(@RequestBody Map<Object, String> requestParam) throws Exception{
        String taskId = requestParam.get(Dict.TASKID);
        FdevTask task = fdevTaskService.queryTaskById(taskId);
        Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(task.getProject_id(), task.getApplicationType());
        HashMap<String,Object> result = new HashMap();
        try {
            Tuple.Tuple3 afterCommits = gitApiService.getAfterCommits((String) projectInfo.get(Dict.GITLAB_ID), task.getFeature_branch());
            result.put("commits",afterCommits.first());
            result.put("conflict",afterCommits.second());
            result.put("fileList",afterCommits.third());
        }catch (Exception e) {
            logger.info(">>>>>>调用gitlab服务异常");
            result.put("commits", 0);
            result.put("conflict", 0);
            result.put("fileList", Collections.emptyList());
        }
        return JsonResultUtil.buildSuccess(result);
    }

    private Map queryTestMerged(Map param) {
        String taskId = (String) param.get(Dict.ID);
        String targeBranch = "";
        String sourceBranch = "";
        String gitPid = "";
        try {
            FdevTask task = fdevTaskService.queryTaskAll(new FdevTask.FdevTaskBuilder().id(taskId).init());
            Map appInfo = iAppApi.queryAppById(new HashMap() {{
                put(Dict.ID, task.getProject_id());
            }});
            if (CommonUtils.isNullOrEmpty(task))
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.TASK});
            if (CommonUtils.isNullOrEmpty(appInfo))
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.APP});
            gitPid = String.valueOf(appInfo.get(Dict.GITLAB_PROJECT_ID));
            Map releaseInfo = iReleaseTaskApi.queryDetailByTaskId(taskId, "3");
            if (CommonUtils.isNullOrEmpty(releaseInfo)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.RELEASE_APPLICATION});
            }
            Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
            targeBranch = (String) releaseApp.get(Dict.RELEASE_BRANCH);
            sourceBranch = task.getFeature_branch().replaceFirst("feature", "dev");
        } catch (Exception e) {
            if (e instanceof FdevException)
                throw (FdevException) e;
            else {
                logger.error("查询merge信息异常--" + e.getMessage());
            }
        }
        Map gitParam = null;
        boolean merged = false;
        Map tmp = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(gitPid, sourceBranch, targeBranch)) {
            gitParam = new HashMap();
            gitParam.put(Dict.GITLAB_PROJECT_ID, gitPid);
            gitParam.put(Dict.STATE, "merged");
            gitParam.put(Dict.SOURCE_BRANCH, sourceBranch);
            gitParam.put(Dict.TARGET_BRANCH, targeBranch);
            merged = !CommonUtils.isNullOrEmpty(gitHttpServer.getMergeInfo(gitParam));
            tmp.put(Dict.STATUS, merged);
        }
        return tmp;
    }

}

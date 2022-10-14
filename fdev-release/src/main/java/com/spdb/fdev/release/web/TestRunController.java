package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.TestRunApplication;
import com.spdb.fdev.release.entity.TestRunTask;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IGitlabService;
import com.spdb.fdev.release.service.IRoleService;
import com.spdb.fdev.release.service.ITestRunService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "客户端试运行管理接口")
@RequestMapping("/api/testrun")
@RestController
public class TestRunController {

    private Logger logger = LoggerFactory.getLogger(TestRunController.class);

    @Autowired
    private ITestRunService testRunService;
    @Autowired
    private IAppService appService;
    @Autowired
    private IGitlabService gitlabService;
    @Autowired
    private IRoleService roleService;

    @OperateRecord(operateDiscribe="投产模块-试运行发布")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/createTestrunBranch")
    public JsonResult createTestrunBranch(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        if (!roleService.isApplicationManager(application_id)
                && !roleService.isAppSpdbManager(application_id)) {//判断当前用户为该任务的应用负责人
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        List<TestRunApplication> list = testRunService.findByAppNode(new TestRunApplication(application_id, release_node_name));
        String transition_branch = "transition-" + release_node_name + "-" + transIntToString(list.size());
        String testrun_branch = "testrun-" + release_node_name + "-" + transIntToString(list.size());
        TestRunApplication testRunApplication = new TestRunApplication();
        testRunApplication.setApplication_id(application_id);
        testRunApplication.setRelease_node_name(release_node_name);
        testRunApplication.setTestrun_branch(testrun_branch);
        testRunApplication.setTransition_branch(transition_branch);
        return JsonResultUtil.buildSuccess(testRunApplication);
    }

    private String transIntToString(int num) {
        String numToString;
        num = num + 1;
        if(num >= 0 && num < 10) {
            numToString = "00" + num;
        } else if(num >=10 && num < 100) {
            numToString = "0" + num;
        } else {
            numToString = "" + num;
        }
        return numToString;
    }

    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME, Dict.TESTRUN_BRANCH, Dict.TRANSITION_BRANCH, Dict.TESTRUN_TASK})
    @PostMapping(value = "/mergeTaskBranch")
    public JsonResult mergeTaskBranch(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        if (!roleService.isApplicationManager(application_id)
                && !roleService.isAppSpdbManager(application_id)) {//判断当前用户为该任务的应用负责人
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        String testrun_branch = (String) requestParam.get(Dict.TESTRUN_BRANCH);
        String transition_branch = (String) requestParam.get(Dict.TRANSITION_BRANCH);

        // 根据应用id查询gitlab对应id
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        Integer gitlab_project_id = (Integer) application.get(Dict.GITLAB_PROJECT_ID);

        // 拉取过渡分支
        gitlabService.createBranch(gitlab_project_id, transition_branch, Dict.MASTER);
        // 拉取试运行分支
        gitlabService.createBranch(gitlab_project_id, testrun_branch, Dict.MASTER);

        TestRunApplication testRunApplication = new TestRunApplication();
        ObjectId id = new ObjectId();
        testRunApplication.set_id(id);
        testRunApplication.setId(id.toString());
        testRunApplication.setGitlab_project_id(gitlab_project_id);
        testRunApplication.setApplication_id(application_id);
        testRunApplication.setRelease_node_name(release_node_name);
        testRunApplication.setTestrun_branch(testrun_branch);
        testRunApplication.setTransition_branch(transition_branch);
        testRunService.save(testRunApplication);

        List<Map<String, String>> testrun_task = (List<Map<String, String>>) requestParam.get(Dict.TESTRUN_TASK);
        for(Map<String, String> map : testrun_task) {
            String task_id = map.get(Dict.TASK_ID);
            String task_name = map.get(Dict.TASK_NAME);
            String task_branch = map.get(Dict.TASK_BRANCH);
            Map<String, Object> result = (Map<String, Object>) gitlabService.createMergeRequest(testRunApplication.getGitlab_project_id(), task_branch,
                    testRunApplication.getTransition_branch(), task_name, null);
            TestRunTask testRunTask = new TestRunTask();
            ObjectId testRunTaskId = new ObjectId();
            testRunTask.set_id(testRunTaskId);
            testRunTask.setId(testRunTaskId.toString());
            testRunTask.setTask_id(task_id);
            testRunTask.setTask_name(task_name);
            testRunTask.setTask_branch(task_branch);
            testRunTask.setMerge_request_id(String.valueOf(result.get(Dict.IID)));
            testRunTask.setStatus("0");
            testRunTask.setTestrun_id(testRunApplication.getId());
            testRunTask.setGitlab_project_id(testRunApplication.getGitlab_project_id());
            testRunService.saveTask(testRunTask);
        }
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestValidate(NotEmptyFields = {Dict.TESTRUN_ID})
    @PostMapping(value = "/queryTaskByTestRunId")
    public JsonResult queryTaskByTestRunId(@RequestBody @ApiParam Map<String, String> requestParam) {
        String testrun_id = requestParam.get(Dict.TESTRUN_ID);
        List<TestRunTask> list = testRunService.findTaskByTestRunId(new TestRunTask(testrun_id));
        return JsonResultUtil.buildSuccess(list);
    }

}

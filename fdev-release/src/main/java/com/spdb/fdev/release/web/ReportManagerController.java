package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName ReportManagerController
 * @Description
 * @Author xuxuanrui
 * @Date 2020/8/10 18:28
 * @Version 1.0
 **/
@Api(tags = "投产发布按钮的接口")
@RequestMapping("/api/application")
@RestController
public class ReportManagerController {

    @Autowired
    private IGitlabService gitlab;

    @Autowired
    private IRelDevopsRecordService relDevopsRecordService;

    @Autowired
    private IReleaseApplicationService releaseApplicationService;

    @Autowired
    private IAppService appService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IReleaseTaskService releaseTask;

    @Autowired
    private ITaskService taskService;

    private Logger logger = LoggerFactory.getLogger(ReportManagerController.class);

    @OperateRecord(operateDiscribe="投产模块-准生产发布、灰度发布、appstore发布、试运行发布")
    @RequestValidate(NotEmptyFields = {Dict.IS_REINFORCE,Dict.ENV,Dict.ID,Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/release")
    public JsonResult release(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String is_reinforce = requestParam.get(Dict.IS_REINFORCE);
        String env = requestParam.get(Dict.ENV);
        String id = requestParam.get(Dict.ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        if (!roleService.isApplicationManager(id)
                && !roleService.isAppSpdbManager(id)) {//判断当前用户为该任务的应用负责人
            logger.error("the user must be this app manager!");
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        List<ReleaseTask> releaseTasks = releaseTask.queryReleaseTaskByAppid(id, release_node_name);
        List<String> ids = new ArrayList<>();
        for (ReleaseTask releaseTask : releaseTasks) {
            ids.add(releaseTask.getTask_id());
        }
        String type = requestParam.get(Dict.TYPE);
        String release_date=release_node_name.substring(0, release_node_name.indexOf("_"));
        User user = CommonUtils.getSessionUser();
        String username = user.getGit_user();
        String token = user.getGit_token();
        String ref="";
        Map<String, Object> application = appService.queryAPPbyid(id);
        if(CommonUtils.isNullOrEmpty(application)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Integer git_id = (Integer) application.get(Dict.GITLAB_PROJECT_ID);
        if(CommonUtils.isNullOrEmpty(git_id)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(id, release_node_name);
        if(Dict.RELEASE.equals(type) || "".equals(type)){
            if(Dict.APPSTORE.equals(env)){
                List<String> tags = relDevopsRecordService.queryNormalTags(release_node_name, id);
                if(CommonUtils.isNullOrEmpty(tags)){
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"请先点击生产发布"});
                }
                //AppStore发布
                ref=tags.get(0);
            }else if(Dict.JRCS.equals(env) || Dict.AUTO.equals(env) || Dict.YACE.equals(env) || Dict.RELEASE.equals(env) || Dict.GRAY.equals(env)){
                //准生产发布和灰度发布
                if(CommonUtils.isNullOrEmpty(releaseApplication)){
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
                }
                ref = releaseApplication.getRelease_branch();
            }
        }else if(Dict.TESTRUN.equals(type)){
            //试运行发布
            if(CommonUtils.isNullOrEmpty(releaseApplication)){
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            if(Dict.JRCS.equals(env) || Dict.AUTO.equals(env) || Dict.YACE.equals(env) || Dict.TESTRUN.equals(env)) {
                ref = releaseApplication.getRelease_branch();
            }
        }
        if(CommonUtils.isNullOrEmpty(ref)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        createPipelineMethod(ref,release_date,username,is_reinforce,git_id.toString(),env,token);
        return JsonResultUtil.buildSuccess(null);
    }
    //处理并调用createPipeline方法
    private void createPipelineMethod(String ref, String date, String username, String is_reinforce, String git_id,String env,String token) throws Exception {
        List<Map<String, String>> variables=new ArrayList<>();
        Map<String,String> param1=new HashMap<>(2);
        Map<String,String> param2=new HashMap<>(2);
        Map<String,String> param3=new HashMap<>(2);
        Map<String,String> param4=new HashMap<>(2);
        Map<String,String> param5=new HashMap<>(2);
        param1.put("key","FDEV_BRANCH_NAME");
        param1.put("value",ref);
        param2.put("key","FDEV_DESC");
        param2.put("value",date);
        param3.put("key","FDEV_USERNAME");
        param3.put("value",username);
        param4.put("key","FDEV_IS_REINFORCE");
        param4.put("value",is_reinforce);
        param5.put("key","FDEV_ENVS");
        param5.put("value",env);
        variables.add(param1);
        variables.add(param2);
        variables.add(param3);
        variables.add(param4);
        variables.add(param5);
        gitlab.createPipeline(git_id,ref,variables,token);
    }
}

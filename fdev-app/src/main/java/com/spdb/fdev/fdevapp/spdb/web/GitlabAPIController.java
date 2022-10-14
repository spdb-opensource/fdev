package com.spdb.fdev.fdevapp.spdb.web;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Api(tags = "gitlab api")
@RequestMapping("/api/gitlabapi")
@RestController
@RefreshScope
public class GitlabAPIController {

    @Autowired
    private IGitlabAPIService gitlab;
    @Autowired
    private IAppEntityDao appEntityDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Value("${gitlab.token}")
    private String gitlab_token;
    @Value("${interface.api}")
    private String interfaceApi;
    @Value("${task.api}")
    private String taskApi;
    @Autowired
    private IGitlabAPIService gitlabAPIService;

    private static final int MAXLENGTH = 7;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryProject", method = RequestMethod.POST)
    public JsonResult queryProject(@RequestBody Map requestParam) throws Exception {
        String id = ((String) requestParam.get(Dict.ID)).trim();
        if (id.toCharArray().length < MAXLENGTH) {
            return JsonResultUtil.buildSuccess(this.gitlab.getProjectInfo(id, this.gitlab_token));
        }
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.queryProjectById(projectid, this.gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.USER_ID, Dict.TOKEN})
    @RequestMapping(value = "/queryProjectByUser", method = RequestMethod.POST)
    public JsonResult queryProjectByUser(@RequestBody Map requestParam) throws Exception {
        String user_id = (String) requestParam.get(Dict.USER_ID);
        String token = (String) requestParam.get(Dict.TOKEN);
        Object result = gitlab.quertProjectByUser(user_id, token);
        return JsonResultUtil.buildSuccess(JSONObject.parse((String) result));
    }

    @RequestValidate(NotEmptyFields = {Dict.PATH, Dict.PROJECT_NAME})
    @RequestMapping(value = "/createProject", method = RequestMethod.POST)
    public JsonResult createProject(@RequestBody Map requestParam) throws Exception {
        String path = (String) requestParam.get(Dict.PATH);
        String project_name = (String) requestParam.get(Dict.PROJECT_NAME);
        Object result = gitlab.createProject(path, project_name, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.TOKEN})
    @RequestMapping(value = "/queryProjectMembers", method = RequestMethod.POST)
    public JsonResult queryProjectMembers(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String token = (String) requestParam.get(Dict.TOKEN);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.queryProjectMembers(projectid, token);
        return JsonResultUtil.buildSuccess(JSONObject.parse((String) result));
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/addProjectHook", method = RequestMethod.POST)
    public JsonResult addProjectHook(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String merge_request = (String) requestParam.get(Dict.MERGE_REQUEST);
        String pipeline = (String) requestParam.get(Dict.PIPELINE);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.addProjectHook(projectid, gitlab_token, merge_request, pipeline);
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "应用模块-应用详情\"自定义部署\"")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.REF})
    @RequestMapping(value = "/createPipeline", method = RequestMethod.POST)
    public JsonResult createPipeline(@RequestBody Map<String, Object> requestParam) throws Exception {
        //请求头中添加source=back跳过权限验证
        String back = (String) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.SOURCE);
        String id = (String) requestParam.get(Dict.ID);
        if (!Dict.BACK.equals(back)) {
            //权限控制，当前行内负责人和应用负责人才能更新应用
            Boolean authorityManager = false;
            //从数据库拿到应用实体queryappEntity
            AppEntity queryappEntity = appEntityDao.findById(id);
            User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getSession().getAttribute(Dict._USER);
            if (null == user) {
                throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
            }
            //判断用户是否是当前应用的行内项目负责人
            for (Object object : queryappEntity.getSpdb_managers()) {
                Map spdb_managers = (Map) JSONObject.toJSON(object);
                if (user.getUser_name_en().equals(spdb_managers.get(Dict.USER_NAME_EN))) {
                    authorityManager = true;
                    break;
                }
            }
            //判断用户是否是当前应用的厂商项目负责人
            for (Object object1 : queryappEntity.getDev_managers()) {
                Map dev_managers = (Map) JSONObject.toJSON(object1);
                if (user.getUser_name_en().equals(dev_managers.get(Dict.USER_NAME_EN))) {
                    authorityManager = true;
                    break;
                }
            }
            //判断用户是否是卡点管理员
            if (userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                authorityManager = true;
            }
            if (authorityManager == false) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
            }
        }

        String ref = (String) requestParam.get(Dict.REF);
        List<Map<String, String>> variables = (List<Map<String, String>>) requestParam.get(Dict.VARIABLES);
        if(!CommonUtils.isNullOrEmpty(variables)){
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("key", "definedDeploy");
            map.put("value", "true");
            variables.add(map);
        }
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.createPipeline(projectid, ref, variables, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/webHook", method = RequestMethod.POST)
    public JsonResult webHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.ID, Dict.REF})
    @RequestMapping(value = "/createBranch", method = RequestMethod.POST)
    public JsonResult createBranch(@RequestBody Map requestParam) throws Exception {
        String name = (String) requestParam.get(Dict.NAME);
        String id = (String) requestParam.get(Dict.ID);
        String ref = (String) requestParam.get(Dict.REF);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.createBranch(projectid, name, ref, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.SOURCE_BRANCH, Dict.TARGET_BRANCH, Dict.TITLE, Dict.TOKEN})
    @RequestMapping(value = "/createMergeRequest", method = RequestMethod.POST)
    public JsonResult createMergeRequest(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String token = (String) requestParam.get(Dict.TOKEN);
        String source_branch = (String) requestParam.get(Dict.SOURCE_BRANCH);
        String target_branch = (String) requestParam.get(Dict.TARGET_BRANCH);
        String title = (String) requestParam.get(Dict.TITLE);
        String desc = (String) requestParam.get(Dict.DESCRIPTION);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.createMergeRequest(projectid, source_branch,
                target_branch, title, desc, token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.IID})
    @RequestMapping(value = "/getMergeRequestInfo", method = RequestMethod.POST)
    public JsonResult getMergeRequestInfo(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String iid = (String) requestParam.get(Dict.IID);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.getMergeRequestInfo(projectid, iid, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.PID})
    @RequestMapping(value = "/queryPipelineByPid", method = RequestMethod.POST)
    public JsonResult queryPipelineByPid(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String pid = (String) requestParam.get(Dict.PID);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.queryPipelineByPid(projectid, pid, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.MID})
    @RequestMapping(value = "/queryPipelineByMid", method = RequestMethod.POST)
    public JsonResult queryPipelineByMid(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String mid = (String) requestParam.get(Dict.MID);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Map<String, Object> result = gitlab.queryPipelineByMid(projectid, mid, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/getProjectBranchList", method = RequestMethod.POST)
    public JsonResult getProjectBranchList(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String projectid = gitlab.getProjectIDByApp(id);
        String order_by = (String) requestParam.get(Dict.ORDER_BY);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.getProjectBranchList(projectid, gitlab_token);
        List<Map<String, Object>> tagsList = gitlab.getProjectTagsList(projectid, gitlab_token, order_by);
        ArrayList branchList = (ArrayList) result;
        branchList.addAll(tagsList);
        return JsonResultUtil.buildSuccess(branchList);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH})
    @RequestMapping(value = "/deleteBranch", method = RequestMethod.POST)
    public JsonResult deleteBranch(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String branch = (String) requestParam.get(Dict.BRANCH);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.deleteBranch(projectid, branch, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.NAME, Dict.REF})
    @RequestMapping(value = "/createTags", method = RequestMethod.POST)
    public JsonResult createTags(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String name = (String) requestParam.get(Dict.NAME);
        String ref = (String) requestParam.get(Dict.REF);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlab.createTages(projectid, name, ref, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/getProjectTagsList", method = RequestMethod.POST)
    public JsonResult getProjectTagsList(@RequestBody Map<String, String> requestParam) throws Exception {
        String id = requestParam.get(Dict.ID);
        String order_by = requestParam.get(Dict.ORDER_BY);
        String projectid = gitlab.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        List<Map<String, Object>> projectTagsList = gitlab.getProjectTagsList(projectid, gitlab_token, order_by);
        return JsonResultUtil.buildSuccess(projectTagsList);
    }

    /**
     * status 0
     * 查询 gitlab Group 信息
     * /ebank/devops/  /ebank/devops
     * status 1
     * 新增 用户
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/getGroup")
    @RequestValidate(NotEmptyFields = {Dict.GROUP, Dict.STATUS})
    public JsonResult getGroup(@RequestBody Map<String, String> requestParam) throws Exception {
        boolean result;
        String group = requestParam.get(Dict.GROUP);
        String status = requestParam.get(Dict.STATUS);
        if ("0".equals(status)) {
            // 查询 组
            result = this.gitlab.getGroupInfo(group, this.gitlab_token);
            return JsonResultUtil.buildSuccess(result);
        }
        if ("1".equals(status)) {
            // 新增 组
            result = this.gitlab.createGroup(group, this.gitlab_token);
            return JsonResultUtil.buildSuccess(result);
        } else {
            result = false;
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数解析失败，不存在status"});
        }
    }

    /**
     * *   查询gitlab按项目pipeine
     *
     * @param requestParam
     * @return pipeline数组
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {"project_id"})
    @RequestMapping(value = "/queryPipelines", method = RequestMethod.POST)
    public JsonResult queryPipelines(@RequestBody Map<String, String> requestParam) throws Exception {
        requestParam.put(Dict.GITLAB_TOKEN, this.gitlab_token);
        Object result = this.gitlab.queryPipelines(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * *查询pipeline下的所有jobs
     *
     * @param requestParam
     * @return job数组
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID, Dict.PIPELINE_ID})
    @RequestMapping(value = "/queryJobs", method = RequestMethod.POST)
    public JsonResult queryJobs(@RequestBody Map<String, String> requestParam) throws Exception {
        requestParam.put(Dict.GITLAB_TOKEN, this.gitlab_token);
        Object result = this.gitlab.queryJobs(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * *    查询gitlab项目pipeine并组装对应jobs
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID})
    @RequestMapping(value = "/queryPipelinesWithJobs", method = RequestMethod.POST)
    public JsonResult queryPipelinesWithJobs(@RequestBody Map<String, String> requestParam) throws Exception {
        requestParam.put(Dict.GITLAB_TOKEN, this.gitlab_token);
        Object result = this.gitlab.queryPipelinesWithJobs(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * *    查询gitlab项目pipeine并组装对应jobs
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID})
    @RequestMapping(value = "/queryPipelinesWithJobsPage", method = RequestMethod.POST)
    public JsonResult queryPipelinesWithJobsPage(@RequestBody Map<String, Object> requestParam) throws Exception {
        requestParam.put(Dict.GITLAB_TOKEN, this.gitlab_token);
        Object result = this.gitlab.queryPipelinesWithJobsPage(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * *    查询gitlab下job的json日志
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID, Dict.JOB_ID})
    @RequestMapping(value = "/queryTraces", method = RequestMethod.POST)
    public JsonResult queryTrace(@RequestBody Map<String, String> requestParam) throws Exception {
        requestParam.put(Dict.GITLAB_TOKEN, this.gitlab_token);
        Object result = this.gitlab.queryTraces(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 通过id获取所有分支
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID})
    @PostMapping("/queryAllBranch")
    public JsonResult queryAllBranch(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.GITLAB_PROJECT_ID);
        Map projectInfo = gitlab.getProjectInfo(id, gitlab_token);
        if (CommonUtils.isNullOrEmpty(projectInfo)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        //获取yaml所有配置中only下的配置
        String filePath = ".gitlab-ci.yml";
        String content = this.gitlabAPIService.getContent(id, filePath, "master");
        Yaml yaml = new Yaml();
        Map<String, Object> yamlContent = yaml.loadAs(content, Map.class);
        List<String> stages = (List<String>) yamlContent.get("stages");

        HashSet<String> onlySet = new HashSet();
        Iterator keys = yamlContent.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if (yamlContent.get(key) instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) yamlContent.get(key);
                if (map.containsKey("stage") && stages.contains((String) map.get("stage"))) {
                    List<String> onlys = new ArrayList<>();
                    if (map.containsKey("only")) {
                        onlys = (List<String>) map.get("only");
                        if(CommonUtils.isNullOrEmpty(onlys)){
                            onlySet = new HashSet<>();
                            break;
                        }
                        onlySet.addAll(onlys);
                    } else {
                        onlySet = new HashSet<>();
                        break;
                    }
                }
            }
        }
        if(onlySet.contains("api")){
            onlySet = new HashSet<>();
        }
        List<String> branchList = this.gitlab.getAllBranch(id, gitlab_token, onlySet);
        return JsonResultUtil.buildSuccess(branchList);
    }

    /**
     * 通过gitlab project Id校验项目英文名
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID})
    @PostMapping("/queryAppNameEnByGitlabProjectId")
    public JsonResult queryAppNameEnByGitlabProjectId(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.GITLAB_PROJECT_ID);
        String type_name = (String) requestParam.get(Dict.TYPE_NAME);
        String isInternetSystem = (String) requestParam.get("isInternetSystem");
        if (StringUtils.isBlank(type_name)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"应用类型"});
        }
        String name_en = gitlab.getFileContent(type_name, id, isInternetSystem);
        return JsonResultUtil.buildSuccess(name_en);
    }

    //获取gitlab的组全量信息
    @PostMapping("/getGroupGit")
    public JsonResult getGroupGit(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(this.gitlabAPIService.getFullGroupGit());
    }

}

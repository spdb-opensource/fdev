package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.schedule.ScheduleTaskService;
import com.spdb.fdev.pipeline.service.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    @Autowired
    IPipelineService pipelineService;
    @Autowired
    IPipelineTemplateService pipelineTemplateService;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    IPluginService pluginService;
    @Autowired
    IAppService appService;
    @Autowired
    IFileService iFileService;
    @Value("${gitlab.manager.token}")
    private String userGitToken;
    @Value("${mioBuket}")
    private String mioBuket;
    @Autowired
    private IUserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private ScheduleTaskService scheduleTaskService;

    @Value("${temPoraryRelativeRootUrl}")
    private String temPoraryRelativeRootUrl;

    @Value("${gitlab.token}")
    private String superToken;

    private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

    @PostMapping("/triggerPipeline")
    public JsonResult triggerPipeline(@RequestBody Map<String, Object> param) throws Exception{
        String pipelineId = (String) param.get(Dict.PIPELINEID);
        String branch = (String) param.get(Dict.BRANCH);
        List<Map> runVariables = (List<Map>)param.get(Dict.RUNVARIABLES);
        String userId = (String) param.get(Dict.USERID);
        if (!CommonUtils.isNullOrEmpty(runVariables)){
            for (Map map : runVariables){
                Iterator iterator = map.keySet().iterator();
                while (iterator.hasNext()){
                    Object next = iterator.next();
                    if (CommonUtils.isNullOrEmpty(map.get(next))){
                        iterator.remove();
                    }
                }
            }
        }
        Iterator<Map> iterator = runVariables.iterator();
        while (iterator.hasNext()){
            Object next = iterator.next();
            if (CommonUtils.isNullOrEmpty(next)){
                iterator.remove();
            }
        }
        List<Map> exeSkippedSteps = (List<Map>)param.get(Dict.EXESKIPPEDSTEPS);
        Boolean tagFlag = (Boolean) param.get(Dict.TAGFLAG);
        String pipelineExeId = pipelineService.triggerPipeline(pipelineId, branch, tagFlag, Dict.MANUAL,runVariables,exeSkippedSteps, userId);
        return JsonResultUtil.buildSuccess(pipelineExeId);
    }


    @PostMapping("/webhookPipeline")
    public JsonResult webhookPipeline(@RequestBody Map<String, Object> param) throws Exception{
        pipelineService.webhookPipeline(param);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping("/retryPipeline")
    public JsonResult retryPipeline(@RequestBody Map<String, String> param) throws Exception{
        String pipelineExeId = param.get(Dict.PIPELINEEXEID);
        pipelineService.retryPipeline(pipelineExeId);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping("/retryJob")
    public JsonResult retryJob(@RequestBody Map<String, Object> param) throws Exception{
        String pipelineExeId = (String)param.get(Dict.PIPELINEEXEID);
        Integer stageIndex = (Integer) param.get(Dict.STAGEINDEX);
        Integer jobIndex = (Integer) param.get(Dict.JOBINDEX);
        pipelineService.retryPipeline(pipelineExeId, stageIndex, jobIndex);
        return JsonResultUtil.buildSuccess(null);
    }

    /*
    新增保存流水线
     */
    @PostMapping("/add")
    @NoNull(require = {Dict.NAME, Dict.STAGES,  Dict.BINDPROJECT, Dict.GITLABPROJECTIDGITLABPROJECTID,
            Dict.BINDPROJECTPROJECTID}, parameter = Pipeline.class)
    public JsonResult add(@RequestBody Pipeline pipeline) throws Exception{
        return JsonResultUtil.buildSuccess(pipelineService.add(pipeline));
    }

    @PostMapping("/addByTemplateNameIdAndBindProject")
    @RequestValidate(NotEmptyFields = {Dict.NAMEID,Dict.BINDPROJECT})
    public JsonResult addByTemplateNameIdAndBindProject(@RequestBody Map request) throws Exception{
        String nameId = (String) request.get(Dict.NAMEID);
        Map bindProjectMap = (Map) request.get(Dict.BINDPROJECT);
        if (CommonUtils.isNullOrEmpty(bindProjectMap)){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"【bindProject应用不能为空】"});
        }
        if (CommonUtils.isNullOrEmpty(bindProjectMap.get(Dict.GITLABPROJECTID))
                || CommonUtils.isNullOrEmpty(bindProjectMap.get(Dict.PROJECTID))){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"【应用的gitlabProjectId或projectId不能为空】"});
        }
        String userId = (String) request.get(Dict.USERID);
        BindProject bindProject = null;
        if (!CommonUtils.isNullOrEmpty(bindProjectMap)){
            bindProject = CommonUtils.map2Object(bindProjectMap, BindProject.class);
        }
        Map resultMap = pipelineService.addByTemplateNameIdAndBindProject(nameId, bindProject, userId);
        return JsonResultUtil.buildSuccess(resultMap);
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.NAME, Dict.ID, Dict.NAMEID, Dict.STAGES, Dict.BINDPROJECT,Dict.GITLABPROJECTIDGITLABPROJECTID,
            Dict.BINDPROJECTPROJECTID}, parameter = Pipeline.class)
    public JsonResult update(@RequestBody Pipeline pipeline) throws Exception{
        return JsonResultUtil.buildSuccess(pipelineService.update(pipeline));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult del(@RequestBody  Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.PIPELINEID);
        pipelineService.delete(id);
        return JsonResultUtil.buildSuccess(id);
    }

    /**
     * 根据id或者nameI的查询流水线详情
     * nameId的情况取生效的流水线返回，两个都有取id查询
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/queryById")
    public JsonResult queryById(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(pipelineService.queryById(request));
    }

    @RequestMapping(value = "/updateFollowStatus", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult updateFollowStatus(@RequestBody  Map requestParam) throws Exception {
        String pipelineId = (String) requestParam.get(Dict.PIPELINEID);
        String result = pipelineService.updateFollowStatus(pipelineId);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/queryAllPipelineList", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryAllPipelineList(@RequestBody  Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        User user = userService.getUserFromRedis();
        Map<String, Object> map = pipelineService.queryAllPipelineList(pageNum, pageSize, user, searchContent);
        return JsonResultUtil.buildSuccess(map);
    }

    @RequestMapping(value = "/queryAppPipelineList", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE, Dict.APPLICATION_ID})
    public JsonResult queryAppPipelineList(@RequestBody  Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String applicationId = requestParam.get(Dict.APPLICATION_ID);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        User user = userService.getUserFromRedis();
        Map<String, Object> map = pipelineService.queryAppPipelineList(pageNum, pageSize, user.getId(), applicationId, searchContent);
        return JsonResultUtil.buildSuccess(map);
    }

    @RequestMapping(value = "/queryCollectionPipelineList", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryCollectionPipelineList(@RequestBody  Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        User user = userService.getUserFromRedis();
        Map<String, Object> map = pipelineService.queryCollectionPipelineList(pageNum, pageSize, user.getId(), searchContent);
        return JsonResultUtil.buildSuccess(map);
    }

    @RequestMapping(value = "/queryMinePipelineList", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryMinePipelineList(@RequestBody  Map<String, String> requestParam) throws Exception {
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        String searchContent = requestParam.get("searchContent");
        User user = userService.getUserFromRedis();
        Map<String, Object> map = pipelineService.queryMinePipelineList(pageNum, pageSize, user.getId(), searchContent);
        return JsonResultUtil.buildSuccess(map);
    }

    @PostMapping("saveDraft")
    public JsonResult saveDraft(@RequestBody PipelineDraft draft) throws Exception{
        return JsonResultUtil.buildSuccess(pipelineService.saveDraft(draft));
    }

    @PostMapping("readDraft")
    public JsonResult readDraft() throws Exception{
        return JsonResultUtil.buildSuccess(pipelineService.readDraft());
    }

    @PostMapping("queryImageList")
    public JsonResult queryImageList() throws Exception {
        return JsonResultUtil.buildSuccess(pipelineService.queryAllImages());
    }

    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    @PostMapping("queryBranchesByPipelineId")
    public JsonResult queryBranchesByPipelineId(@RequestBody Map<String, String> requestParam) throws Exception {
        String pipelineId = requestParam.get(Dict.PIPELINEID);
        Pipeline pipeline = pipelineService.querySimpleObjectById(pipelineId);
        if (CommonUtils.isNullOrEmpty(pipeline)){

            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Map<String, Object> map = null;
        List<String> normalBranches = null;
        List<String> tags = null;
        //map = appService.queryAppDetailById(pipeline.getBindProject().getProjectId());
        normalBranches = gitlabService.queryBranches(pipeline.getBindProject().getGitlabProjectId(), superToken);
        tags = gitlabService.queryTags(pipeline.getBindProject().getGitlabProjectId(), superToken);
        Map<String, Object> branches = new HashMap<>();
        branches.put(Dict.BRANCHES, normalBranches);
        branches.put(Dict.TAGS, tags);
        return JsonResultUtil.buildSuccess(branches);
    }


    /**
     * 查询流水线历史记录
     */
    @RequestMapping(value = "/queryPipelineHistory",method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.NAMEID,Dict.PAGE_NUM, Dict.PAGE_SIZE})
    public JsonResult queryPipelineHistory(@RequestBody Map<String, String> requestParam) throws Exception {
        String nameId = requestParam.get(Dict.NAMEID);
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        Map<String, Object> resultMap = pipelineService.queryPipelineHistory(nameId,pageNum,pageSize);
        return JsonResultUtil.buildSuccess(resultMap);
    }

    /**
     * 复制流水线
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/copy")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult copy(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(pipelineService.copy(id));
    }

    @PostMapping("/saveAsPipelineTemplate")
    public JsonResult saveAsPipelineTemplate(@RequestBody Map request) throws Exception{
        String id = (String) request.get(Dict.ID);
        String pipelineTemplateId = pipelineTemplateService.saveFromPipeline(id);
        return JsonResultUtil.buildSuccess(pipelineTemplateId);
    }

    /**
     *
     * 停止pipeline执行
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/stopPipeline")
    public JsonResult stopPipeline(@RequestBody Map<String, Object> param) throws Exception{
        String pipelineExeId = (String) param.get(Dict.PIPELINEEXEID);
        //true标识为人工审核插件调用，不校验用户信息停止流水线
        Boolean skipManualReview = (Boolean) param.get("skipManualReview");
        pipelineService.stopPipeline(pipelineExeId, skipManualReview);
        return JsonResultUtil.buildSuccess(null);
    }

    /**
     * 停止单个job
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/stopJob")
    public JsonResult stopJob(@RequestBody Map<String, Object> param) throws Exception{
        String pipelineExeId = (String)param.get(Dict.PIPELINEEXEID);
        Integer stageIndex = (Integer) param.get(Dict.STAGEINDEX);
        Integer jobIndex = (Integer) param.get(Dict.JOBINDEX);
        this.pipelineService.stopJob(pipelineExeId, stageIndex, jobIndex);
        return JsonResultUtil.buildSuccess(null);
    }

    /**
     * 从gitlab查询git-ci下面的文件内容
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTypeAndContent")
    @RequestValidate(NotEmptyFields = {Dict.BRANCHNAME,Dict.GITLABPROJECTID,Dict.FILEPATH})
    public JsonResult queryTypeAndContent(@RequestBody Map<String, Object> param) throws Exception{
        String branchName = (String)param.get(Dict.BRANCHNAME);
        Integer gitlabProjectId = (Integer) param.get(Dict.GITLABPROJECTID);
        String filePath = (String) param.get(Dict.FILEPATH);
        //gitlabService.queryTypeAndContent(1641,"xxx","gitlab-ci/selector.yaml","master");
        Map map = gitlabService.queryTypeAndContent(gitlabProjectId, userGitToken, filePath, branchName);
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 保存源文件到minio
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/saveToMinio")
    public JsonResult saveMinio(@RequestBody Map<String, Object> param) throws Exception{
        String msg = (String) param.get("msg");
        if(CommonUtils.isNullOrEmpty(msg)){
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        String id = new ObjectId().toString();
        String filePath = id+".sh";
        byte[] bytes = msg.getBytes();
        //文件类型转换
        MockMultipartFile multipartFile = new MockMultipartFile(Dict.FILE, filePath, "text/plain", bytes);
        Map<String,String> resMap = new HashMap<>();
        User user = userService.getUserFromRedis();
        String pathMinio = Constants.SCRIPT_PREFIX+ user.getUser_name_en() +"/"+ id + ".sh";
        resMap.put(Dict.FILEPATH,pathMinio);
        iFileService.minioUploadFiles(mioBuket,pathMinio,multipartFile);
        resMap.put(Dict.TYPE,Dict.MINIO);
        return JsonResultUtil.buildSuccess(resMap);
    }

    /**
     * 从fdev获取源文件或导入模板
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/getMinioInfo")
    public JsonResult getMinioInfo(@RequestBody Map<String, Object> param) throws Exception{
        String mioPath = (String) param.get(Dict.MIOPATH);
        String msg = null;
        Map resMap = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(mioPath)){
            msg = iFileService.downloadDocumentFile(mioBuket, mioPath);
            resMap.put(Dict.FILEPATH,mioPath);
        }else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.MIOPATH+ mioPath});
        }
        resMap.put(Dict.TYPE,Dict.MINIO);
        resMap.put(Dict.MSG,msg);
        return JsonResultUtil.buildSuccess(resMap);
    }

    /**
     * 定时执行流水线
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/pipelineScheduleExecute")
    public JsonResult pipelineScheduleExecute(@RequestBody Map<String, Object> param) throws Exception{
        this.scheduleTaskService.scheduleExecutePipeline();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询触发规则
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTriggerRules")
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult queryTriggerRules(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.PIPELINEID);
        Map triggerRules = pipelineService.queryTriggerRules(id);
        return JsonResultUtil.buildSuccess(triggerRules);
    }

    /**
     * 修改触发规则
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/updateTriggerRules")
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult updateTriggerRules(@RequestBody Map request) throws Exception {
        String id = (String) request.get(Dict.PIPELINEID);
        Map triggerRuleMap = (Map) request.get(Dict.TRIGGERRULES);
        TriggerRules triggerRules = null;
        if (!CommonUtils.isNullOrEmpty(triggerRuleMap)){
            triggerRules = CommonUtils.map2Object(triggerRuleMap, TriggerRules.class);
        }
        String result = pipelineService.updateTriggerRules(id, triggerRules);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 上传文件到nas
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/fileUpload2Nas", consumes = {"multipart/form-data"})
    public JsonResult fileUpload2Nas(@RequestParam(value = "file",required = true) MultipartFile file) throws Exception {
        if (file.isEmpty()){
            logger.error("**************The uploadFile is empty！");
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"上传文件内容不允许为空！！！"});
        }
        String originalFilename = file.getOriginalFilename();
        pluginService.checkUploadFileSuffix(originalFilename);

        String id = new ObjectId().toString();
        File nasUrlFile = new File(temPoraryRelativeRootUrl+id);
        if (!nasUrlFile.exists()){
            nasUrlFile.mkdirs();
        }
        String saveUrl = temPoraryRelativeRootUrl + id + "/" + originalFilename;
        CommonUtils.write2FileByChar(file,null,saveUrl);
        Map urlMap = new HashMap<>();
        urlMap.put(Dict.FILEPATH,saveUrl);
        return JsonResultUtil.buildSuccess(urlMap);
    }

    @PostMapping("/queryByNameId")
    @RequestValidate(NotEmptyFields = {Dict.NAMEID})
    public JsonResult queryByNameId(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(pipelineService.queryByNameId(request));
    }

    @GetMapping(value = "/downLoadArtifacts")
   // @RequestValidate(NotEmptyFields ={Dict.EXEID})
    public JsonResult downLoadArtifacts(@RequestParam("objectName") String  objectName,HttpServletResponse response) throws Exception {
        pipelineService.downLoadArtifacts(objectName, response);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 新增后台接口给环境配置模块使用：查询实体被哪些流水线使用到，上送实体id，返回流水线ID，nameID，名称，绑定应用信息，作者信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getPipelineByEntityId")
    @RequestValidate(NotEmptyFields = {Dict.ENTITYIDS})
    public JsonResult getPipelineByEntityId(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(pipelineService.getPipelineByEntityId(request));
    }

    //    在流水线列表增加一列历史版本，点击弹框展示历史版本信息，
    //    选择进入历史详情，在详情页只能选择“回退到该版本”和“返回”，
    //    支持查看流水线历史版本以及回退。

    @PostMapping("/getPipelineHistoryVersion")
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult getPipelineHistoryVersion(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(this.pipelineService.getPipelineHistoryVersion(request));
    }

    @PostMapping("/setPipelineRollBack")
    @RequestValidate(NotEmptyFields = {Dict.PIPELINEID})
    public JsonResult setPipelineRollBack(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(this.pipelineService.setPipelineRollBack(request));
    }

    /**
     * 流水线断点续跑，从失败的第一个任务开始执行
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/continueRunPipeline")
    public JsonResult continueRunPipeline(@RequestBody Map request) throws Exception {
        pipelineService.continueRunPipeline(request);
        return JsonResultUtil.buildSuccess(null);
    }


    /**
     * 定时停止流水线（每小时调一次，执行超过一小时的job取消掉）
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/cronStopPipeline")
    public JsonResult cronStopPipeline(@RequestBody Map request) throws Exception {
        pipelineService.cronStopPipeline(request);
        return JsonResultUtil.buildSuccess(null);
    }


    /**
     * 统计所有流水线的可视化展示数据
     *
     *  exeTotal 流水线构建总数，successExeTotal 成功构建总数，errorExeTotal 失败构建总数，successRate 成功率，aveErrorTime 失败平均修复时长
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/countPipelineDigitalRate")
    public JsonResult countPipelineDigitalRate(@RequestBody Map request) throws Exception {
        pipelineService.countPipDigital();
        return JsonResultUtil.buildSuccess();
    }

}

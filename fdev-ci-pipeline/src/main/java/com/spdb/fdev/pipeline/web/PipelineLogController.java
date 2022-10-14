package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.VaildateUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.dao.IJobExeDao;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RequestMapping("api/pipelineLog")
@RestController
public class PipelineLogController {

    @Autowired
    IAppService appService;
    @Autowired
    IPipelineExeService pipelineExeService;
    @Autowired
    IJobExeService jobExeService;
    @Autowired
    IFileService fileService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    VaildateUtil vaildateUtil;
    @Autowired
    IJobExeService iJobExeService;
    @Autowired
    private IUserService userService;

    @Autowired
    IPluginService iPluginService;

    @Autowired
    IJobExeDao iJobExeDao;

    @Autowired
    IPluginDao iPluginDao;

    @Autowired
    private IPipelineService pipelineService;

    private static final Logger logger = LoggerFactory.getLogger(PipelineLogController.class);

    @RequestValidate(NotEmptyFields = {Dict.PAGE_NUM, Dict.PAGE_SIZE})
    @PostMapping(value = "/queryFdevciLogList")
    public JsonResult queryFdevciLogList(@RequestBody Map<String, String> requestParam) throws Exception {
        String pipelineId = requestParam.get(Dict.PIPELINEID);
        String branch = requestParam.get(Dict.BRANCH);
        String commitId = requestParam.get(Dict.COMMITID);
        String searchContent = requestParam.get(Dict.SEARCHCONTENT);
        String pageNum = requestParam.get(Dict.PAGE_NUM);
        String pageSize = requestParam.get(Dict.PAGE_SIZE);
        User user = userService.getUserFromRedis();
        Map<String, Object> map;
        if (!CommonUtils.isNullOrEmpty(pipelineId)) {
            /*根据流水线ID查日志列表*/
            map = pipelineExeService.queryListByPipelineIdSort(pipelineId, pageNum, pageSize);
        } else {
            /*根据commitid branch serchContent 查日志列表*/
            map = pipelineExeService.queryListRegexSort(commitId, branch, searchContent, pageNum, pageSize);
        }
        map = preparePipelineExe(map);
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 处理pipelineExe,根据权限添加重试字段
     */
    private Map<String, Object> preparePipelineExe(Map<String, Object> map) throws Exception {
        User user = userService.getUserFromRedis();
        if (!CommonUtils.isNullOrEmpty(map)) {
            Object pipelineExeList = map.get(Dict.PIPELINEEXELIST);
            if (!CommonUtils.isNullOrEmpty(pipelineExeList)) {
                List<Map<String,Object>> list = (List<Map<String,Object>>) pipelineExeList;
                if (!CommonUtils.isNullOrEmpty(list)) {
                    boolean flag = vaildateUtil.projectCondition(String.valueOf(
                            ((Map) list.get(0)
                                    .get("bindProject"))
                                    .get(Dict.PROJECTID)));
                    for (Map<String,Object> pipelineExe : list) {
                        // 判断是否为创建人或应用负责人
                        if (user.getUser_name_en().equals(((Map) pipelineExe.get("user")).get("nameEn")) || flag) {
                            pipelineExe.put("retry", Constants.ONE);
                        } else {
                            pipelineExe.put("retry", Constants.ZERO);
                        }
                    }
                }
            }
        }
        return map;
    }

    @RequestValidate(NotEmptyFields = {Dict.PIPELINEEXEID})
    @PostMapping(value = "/queryPipelineDetail")
    public JsonResult queryPipelineDetail(@RequestBody Map<String, String> requestParam) throws Exception {
        String pipelineExeId = requestParam.get(Dict.PIPELINEEXEID);
        PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(pipelineExeId);
        if (pipelineExe == null){
            logger.error("********************queryPipelineDetail  can not find pipelineExe;pipelineExeId:"+pipelineExeId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        User user = userService.getUserFromRedis();
        Map bindProject = pipelineExe.getBindProject();
        String projectID= (String) bindProject.get(Dict.PROJECTID);
        String retry = getPipelineRetryFlag(pipelineExe, user, projectID);
        //添加展示下载按钮的标识
        List<Map> artifacts = pipelineExe.getArtifacts();
        Map<String,Map> artifactsMap = new HashMap<>();
        if (!CommonUtils.isNullOrEmpty(artifacts)){
            //用索引值做key放入新map
            for (Map map : artifacts){
                if(!CommonUtils.isNullOrEmpty(map)){
                    String key = new StringBuffer().append(map.get(Dict.STAGE_INDEX)).append("_")
                            .append(map.get(Dict.JOB_INDEX)).append("_")
                            .append(map.get(Dict.PLUGIN_INDEX)).toString();
                    artifactsMap.put(key,map);
                }
            }
        }
        pipelineExe.setRetry(retry);
        //添加是否需要展示输出结果的标识
        List<Map<String, Object>> stages = pipelineExe.getStages();
        for (int i = 0; i < stages.size(); i++) {
            Map<String, Object> stage = stages.get(i);
            List<Map> jobs = (List<Map>) stage.get(Dict.JOBS);
            for (int n = 0; n < jobs.size(); n++) {
                List<Map> stepsResultInfo = new ArrayList<>();
                List<Map> jobExes = (List<Map>) jobs.get(n).get(Dict.JOBEXES);
                Map lastJobMap = jobExes.get(jobExes.size() - 1);
                String jobExeId = (String) lastJobMap.get(Dict.JOBEXEID);
                JobExe jobExe = iJobExeDao.queryJobExeByExeId(jobExeId);
                if(CommonUtils.isNullOrEmpty(jobExe)) {
                    //这种情况是trigger异步生产的数据框的时候没有jobExe，此时不用组装
                    continue;
                }
                List<Map> steps = jobExe.getSteps();
                for (int j=0 ; j< steps.size(); j++){
                    Map step = steps.get(j);
                    Map stepResultMap = new HashMap<>();
                    Object output = step.get(Dict.OUTPUT);
                    Map pluginInfo = (Map)step.get(Dict.PLUGININFO);
                    String pluginCode = (String) pluginInfo.get(Dict.PLUGINCODE);
                    stepResultMap.put(Dict.PLUGINCODE,pluginCode);
                    Plugin plugin = iPluginDao.queryPluginDetail(pluginCode);
                    if (CommonUtils.isNullOrEmpty(plugin) || plugin == null){
                        logger.error("**********plugin not exist, pluginId="+pluginCode);
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"plugin not exist"+pluginCode});
                    }else {
                        stepResultMap.put(Dict.PLUGINNAME,plugin.getPluginName());
                        stepResultMap.put(Dict.CATEGORYNAME,plugin.getCategory().getCategoryName());
                        stepResultMap.put(Dict.NAMEID,plugin.getNameId());
                        stepResultMap.put(Dict.STEPINDEX,j);
                        //是否包含构建物
                        String key = new StringBuffer().append(i).append("_")
                                .append(n).append("_")
                                .append((j)).toString();
                        if (artifactsMap.containsKey(key)){
                            Map map = artifactsMap.get(key);
                            String obName = (String) map.get(Dict.OBJECT_NAME);
                            if (obName.startsWith("fdevtest-generic-local") || obName.startsWith("fdev-generic-local")){
                                stepResultMap.put(Dict.ARTIFACTSFLAG, true);
                            }else {
                                stepResultMap.put(Dict.ARTIFACTSFLAG, false);
                            }
                        }else {
                            stepResultMap.put(Dict.ARTIFACTSFLAG, false);
                        }
                        //是否有结果展示
                        if (plugin.getResultDisplayFlag()  && !CommonUtils.isNullOrEmpty(output)){
                            stepResultMap.put(Dict.RESULTDISPLAYFLAG,true);
                        }else {
                            stepResultMap.put(Dict.RESULTDISPLAYFLAG,false);
                        }
                    }
                    stepsResultInfo.add(stepResultMap);
                }
                jobs.get(n).put(Dict.STEPSRESULTINFO,stepsResultInfo);
            }
        }
        return JsonResultUtil.buildSuccess(pipelineExe);
    }

    /**
     * 判断流水线实例是否可以重试
     * @param pipelineExe
     * @param user 登录用户
     * @param projectID
     * @return 0-不可以  1-可以
     * @throws Exception
     */
    private String getPipelineRetryFlag(PipelineExe pipelineExe, User user, String projectID) throws Exception {
        String retry = Constants.ONE; //可以重试
        boolean appManagerFlag = vaildateUtil.projectCondition(projectID);
        // 判断是否为构建人或应用负责人
        if(user.getUser_name_en().equals(pipelineExe.getUser().getNameEn()) || appManagerFlag) {
        } else {
            retry = Constants.ZERO;
        }
        //不是已经完成的，不可以重试
        if(!Dict.ERROR.equals(pipelineExe.getStatus()) && !Dict.SUCCESS.equals(pipelineExe.getStatus())){
            retry = Constants.ZERO;
        }
        return retry;
    }

    @RequestValidate(NotEmptyFields = {Dict.JOBEXEID})
    @PostMapping(value = "/queryLogDetailById")
    public JsonResult queryLogDetailById(@RequestBody Map<String, String> requestParam) throws Exception {
        String jobExeId = requestParam.get(Dict.JOBEXEID);
        JobExe jobExe = jobExeService.queryJobExeByExeId(jobExeId);
        PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(jobExe.getPipelineExeId());
        String status = jobExe.getStatus();
        String logContent = "";
        if (!CommonUtils.isNullOrEmpty(jobExe.getMinioLogUrl())) {
            if(Dict.SUCCESS.equals(status) || Dict.ERROR.equals(status)) {
                String logPath = jobExe.getMinioLogUrl();
                String[] pathArray = logPath.split("/minio/");
                if(pathArray.length > 1) {
                    String[] minioArr = pathArray[1].split("/");
                    String bucket = minioArr[0];
                    StringBuilder minioPath = new StringBuilder();
                    for(int i = 1;i < minioArr.length;i ++) {
                        minioPath.append("/").append(minioArr[i]);
                    }
                    try {
                        logContent = fileService.downloadDocumentFile(bucket, minioPath.toString());
                    } catch (Exception e) {
                        logContent = getRedisLog(jobExe);
                    }
                } else {
                    logger.error("*************minio address  error", logPath);
                    logContent = getRedisLog(jobExe);
                }
            }
        }else if(Dict.RUNNING.equals(status)) {
            logContent = getRedisLog(jobExe);
        }else if (!Dict.RUNNING.equals(status)) {
                logContent = "当前job未执行";
        }
        Map<String, Object> returnMap = new HashMap<>();
        Map info = jobExe.getInfo();
        String executorId =null;
        if (!CommonUtils.isNullOrEmpty(info)){
            executorId = (String) info.get(Dict.EXECUTOR);
        }
        returnMap.put(Dict.EXECUTORID,executorId);
        returnMap.put(Dict.LOG, logContent);
        returnMap.put(Dict.JOBEXEID, jobExe.getExeId());
        returnMap.put(Dict.JOBNUMBER, jobExe.getJobNumber());
        returnMap.put(Dict.STATUS, jobExe.getStatus());
        returnMap.put(Dict.JOBNAME, jobExe.getJobName());
        returnMap.put(Dict.JOBSTARTTIME, jobExe.getJobStartTime());
        returnMap.put(Dict.JOBENDTIME, jobExe.getJobEndTime());
        returnMap.put(Dict.JOBCOSTTIME, jobExe.getJobCostTime());
        returnMap.put(Dict.STAGEINDEX,jobExe.getStageIndex());
        returnMap.put(Dict.JOBINDEX,jobExe.getJobIndex());
        returnMap.put(Dict.USER, pipelineExe.getUser().getNameEn());
        returnMap.put(Dict.BRANCH, pipelineExe.getBranch());
        returnMap.put(Dict.STAGE, pipelineExe.getStages());
        //判断是否可以重试
        User user = userService.getUserFromRedis();
        Map bindProject = pipelineExe.getBindProject();
        String projectID= String.valueOf(bindProject.get(Dict.PROJECTID));
        String retry = getPipelineRetryFlag(pipelineExe, user, projectID);
        returnMap.put(Dict.STAGE_RETRYJOB, retry);
        return JsonResultUtil.buildSuccess(returnMap);
    }

    private String getRedisLog(JobExe jobExe) {
        String cacheKey = Constants.PIPELINE_JOE_EXE_LOG_KEY_PROFIX + jobExe.getExeId();
        Map<String, Object> logMap = redisTemplate.opsForHash().entries(cacheKey);
        List<String> keys = new ArrayList<>(logMap.keySet());
        Collections.sort(keys, (o1, o2) -> {
            // key值格式为1-49，截取第一个数字
            Integer a = Integer.valueOf(o1.split("-")[0]);
            Integer b = Integer.valueOf(o2.split("-")[0]);
            if(a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            } else {
                return 0;
            }
        });
        StringBuilder sb = new StringBuilder();
        for(String key : keys) {
            sb.append(logMap.get(key));
        }
        return sb.toString();
    }
    //根据commitId查询流水线实例中的代码扫描等插件结果，如不传入插件英文名，默认返回该流水线中代码扫描插件和蓝鲸插件返回的结果信息。
    @RequestMapping(value = "/getCodeScanOutput",method = RequestMethod.POST)
//    @RequestValidate(NotEmptyFields = {Dict.PROJECTID,Dict.COMMITID})
    public JsonResult getCodeScanOutput(@RequestBody Map requestParam) throws Exception{
        String pipleNumber = (String) requestParam.get(Dict.PIPELINENUMBER);
        String commitId = (String) requestParam.get(Dict.COMMITID);
        String nameEn = (String) requestParam.get(Dict.NAMEEN);
        if (CommonUtils.isNullOrEmpty(pipleNumber)&&CommonUtils.isNullOrEmpty(commitId))
        {
            logger.error("**********pelease input pipelineNumber or  commitId");
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"pipelineNumber or  commitId empty"});
        }
        List<PipelineExe> pipelineExeList = pipelineExeService.queryExeByPipeLineNumberOrCommitId(pipleNumber, commitId);
        List<Map> resultList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(pipelineExeList)) {
//            PipelineExe pipelineExe = pipelineExeList.get(0);
            for (PipelineExe pipelineExe : pipelineExeList) {
                //获取最新执行的流水线的实例,因为它是时间降序排序的，所以要取的时第一个
                Map result_map = new LinkedHashMap();
                List outputList = new ArrayList();
                //获取流水线实例的一些信息，并进行数据封装
                String exeId = pipelineExe.getExeId();
                String status = pipelineExe.getStatus();
                String branch = pipelineExe.getBranch();
                String commitTitle = pipelineExe.getCommitTitle();
                Author author = pipelineExe.getUser();
                String startTIme = pipelineExe.getStartTime();
                result_map.put(Dict.EXEID, exeId);
                result_map.put(Dict.BRANCH, branch);
                result_map.put(Dict.STARTTIME, startTIme);
                result_map.put(Dict.COMMITTITLE, commitTitle);
                result_map.put(Dict.AUTHOR, author);
                result_map.put(Dict.STATUS, status);
                List<Map<String, Object>> stages = pipelineExe.getStages();
                //流水线实例的id
                List<String> jobExeIds = new ArrayList<>();
                for (Map stage : stages) {
                    List<Map> Jobs = (List) stage.get(Dict.JOBS);

                    for (Map job : Jobs) {
                        List<Map> JobExes = (List) job.get(Dict.JOBEXES);
                        //获取最后一个实例，最后一个实例是最新执行的
                        Map jobexe = JobExes.get(JobExes.size() - 1);
                        String jobExeId = (String) jobexe.get(Dict.JOBEXEID);
                        //获取jobexes实例的详情信息
                        JobExe jobExeDetail = jobExeService.queryJobExeByExeId(jobExeId);
                        //获取索引
                        Integer stageIndex = jobExeDetail.getStageIndex();
                        Integer jobIndex = jobExeDetail.getJobIndex();

                        List<Map> steps = jobExeDetail.getSteps();
                        int stepLen = steps.size();
                        for (int stepIndex = 0;stepIndex < stepLen; ++stepIndex) {
                            Map step = steps.get(stepIndex);
//                System.out.println(step);
                            Map pluginInfo = (Map) step.get(Dict.PLUGININFO);
                            //插件的编号
                            String pluginCode = (String) pluginInfo.get(Dict.PLUGINCODE);
                            //插件的名称
                            String name = (String) pluginInfo.get(Dict.NAME);
                            //获取插件的详情信息
                            Plugin plugin = iPluginService.queryPluginDetail(pluginCode);
                            //获取插件分类的id
                            String category_id = plugin.getCategory().getCategoryId();
                            //如果默认指定了插件的英文名称就指定的插件，否则就取找其他的两类插件
                            boolean setData = false;
                            if (!CommonUtils.isNullOrEmpty(nameEn)) {
                                if (nameEn.equals(plugin.getPluginNameEn())||nameEn.equals(Dict.ALL)) {
                                    setData = true;
                                }
                            } else {
                                //如果属于蓝鲸和sonar扫描插件就直接把这个插件添加进去,暂时只能写死成这样
                                for (String str : Constants.pluginCategoryList) {
                                    if (str.equals(category_id)) {
                                        setData = true;
                                        break;
                                    }
                                }
                            }
                            if (setData) {
                                //插件输出的信息
                                Map output = (Map) step.get(Dict.OUTPUT);
                                Map map = new LinkedHashMap();
                                map.put(Dict.PLUGINNAME, name);
                                map.put(Dict.STAGEINDEX,stageIndex);
                                map.put(Dict.JOBINDEX,jobIndex);
                                map.put(Dict.STEPINDEX,stepIndex);
                                map.put(Dict.OUTPUT, output);
//                System.out.println(name+":"+pluginCode+":"+output);
                                outputList.add(map);
                            }
                        }
                    }
                }
                //如果插件返回的信息为空就不把他加进容器里面去
                if (!CommonUtils.isNullOrEmpty(outputList))
                {
                    result_map.put(Dict.OUTPUTLIST, outputList);
                    resultList.add(result_map);
                }
            }
        }
        return JsonResultUtil.buildSuccess(resultList);
    }

    @RequestValidate(NotEmptyFields = {Dict.JOBEXEID})
    @PostMapping(value = "/downLoadLog")
    public void downLoadLog(@RequestParam("jobExeId") String jobExeId,HttpServletResponse response) throws Exception {
        JobExe jobExe = jobExeService.queryJobExeByExeId(jobExeId);
        User currentLoginUser = null;
        try {
            //获取用户的条线
            currentLoginUser = this.userService.getUserFromRedis();
        } catch (Exception e) {
            logger.info(" 当前交易没有用户信息 ");
        }
        if (currentLoginUser != null) {
//            List<String> lineIds = this.userService.getLineIdsByGroupId(currentLoginUser.getGroup_id());
            //获取流水线的条线
            PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(jobExe.getPipelineExeId());
            Map param = new HashMap();
            param.put(Dict.ID, pipelineExe.getPipelineId());
            Pipeline pipeline = this.pipelineService.queryById(param);
            //当前pipeline的条线
//            String groupLineId = pipeline.getGroupLineId();
//            if (!lineIds.contains(groupLineId)) {
//                //当流水线的条线与用户的条线不一样时，不让下载
//                logger.info(" 当流水线的条线与用户的条线不一样 pipelineId" + pipeline.getId());
//                throw new FdevException(ErrorConstants.USER_IS_NOT_AUTH, new String[]{currentLoginUser.getUser_name_en()});
//            }
        }
        String status = jobExe.getStatus();
        String logContent = "";
        if (!CommonUtils.isNullOrEmpty(jobExe.getMinioLogUrl())) {
            if (Dict.SUCCESS.equals(status) || Dict.ERROR.equals(status)) {
                String logPath = jobExe.getMinioLogUrl();
                String[] pathArray = logPath.split("/minio/");
                if (pathArray.length > 1) {
                    String[] minioArr = pathArray[1].split("/");
                    String bucket = minioArr[0];
                    StringBuilder minioPath = new StringBuilder();
                    for (int i = 1; i < minioArr.length; i++) {
                        minioPath.append("/").append(minioArr[i]);
                    }
                    try {
                        logContent = fileService.downloadDocumentFile(bucket, minioPath.toString());
                    } catch (Exception e) {
                        logContent = getRedisLog(jobExe);
                    }
                } else {
                    logger.error("*************minio address  error", logPath);
                    logContent = getRedisLog(jobExe);
                }
            }
        } else if (Dict.RUNNING.equals(status)) {
            logContent = getRedisLog(jobExe);
        } else if (!Dict.RUNNING.equals(status)) {
            logContent = "当前job未执行";
        }
        byte[] bytes = logContent.getBytes();
        response.reset();
        response.setContentType("application/form-data");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                "log" + ".txt");
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"下载失败"});
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
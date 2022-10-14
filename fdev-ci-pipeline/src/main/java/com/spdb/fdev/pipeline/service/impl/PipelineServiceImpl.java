package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.listener.SaveMongoEventListener;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.DiffUtils;
import com.spdb.fdev.base.utils.ObjectUtil;
import com.spdb.fdev.base.utils.VaildateUtil;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.*;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.*;
import org.bson.types.ObjectId;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class PipelineServiceImpl implements IPipelineService {

    @Autowired
    private IPluginService pluginService;
    @Autowired
    private IImageDao imageDao;
    @Autowired
    private IPluginDao pluginDao;

    @Autowired
    private IJobExeService jobExeService;

    @Autowired
    private IAppService appService;

    @Autowired
    private IPipelineExeService pipelineExeService;

    @Autowired
    private IPipelineDao pipelineDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IPipelineExeDao pipelineExeDao;

    @Value("${gitlab.manager.username}")
    private String gitUserName;

    @Value("${gitlab.manager.password}")
    private String gitPassword;

    @Autowired
    private VaildateUtil vaildateUtil;

    @Autowired
    private SaveMongoEventListener mongoEventListener;

    @Autowired
    private IGitlabService gitlabService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IJobExeDao jobExeDao;
    @Autowired
    private IAppService iAppService;

    @Autowired
    private IPluginService iPluginService;

    @Value("${mioBuket}")
    private String mioBuket;

    @Value("${minio.endpoint}")
    private String minioEndPoint;

    @Value("${minio.firstKey}")
    private String minioAccessKey;

    @Value("${minio.secondKey}")
    private String minioSecretKey;

    @Value("${gitlab.api.url}")
    private String gitLabApiUrl;

    @Value("${gitlab.manager.token}")
    private String userGitToken;

    @Value("${gitlab.token}")
    private String gitlabToken;

    @Value("${fdev.domain}")
    private String domain;

    @Value("${envUrl}")
    private String envUrl;

    @Value("${relPluginRelativeRootUrl}")
    private String relPluginRelativeRootUrl;

    @Autowired
    IFileService fileService;

    @Autowired
    IPipelineTemplateDao pipelineTemplateDao;

    @Autowired
    private IRunnerClusterService runnerClusterService;

    @Autowired
    private IPipelineUpdateDiffDao pipelineUpdateDiffDao;

    @Value("${jfrog.username}")
    private String username;

    @Value("${jfrog.password}")
    private String password;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;

    @Autowired
    private IDigitalService digitalService;

    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    @Value("#{${env.plugin.map:}}")
    private Map<String, List<String>> envPluginMap;

    private static final Logger logger = LoggerFactory.getLogger(PipelineServiceImpl.class);

    @Override
    public String triggerPipeline(String pipelineId, String branch, Boolean tagFlag, String triggerType, List<Map> runVariables, List<Map> exeSkippedSteps, String userId) throws Exception {
        Pipeline orgPipeline = pipelineDao.queryById(pipelineId);
        if (CommonUtils.isNullOrEmpty(orgPipeline)) {
            logger.error("**********pipeline not exist, pipelineId=" + pipelineId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId" + pipelineId});
        }
        Pipeline resPipeline = CommonUtils.copyEntityObj(orgPipeline, Pipeline.class);
        //可跳过参数不为空
        if (!CommonUtils.isNullOrEmpty(exeSkippedSteps)) {
            Map skipMap = new HashMap();
            for (Map skipInput : exeSkippedSteps) {
                //获取所有要跳过的所有步骤到skipMap，去重
                if (CommonUtils.isNullOrEmpty(skipInput.get(Dict.STAGEINDEX)) ||
                        CommonUtils.isNullOrEmpty(skipInput.get(Dict.JOBINDEX)) ||
                        CommonUtils.isNullOrEmpty(skipInput.get(Dict.STEPINDEX))
                ) {
                    continue;
                    // throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{"跳过不执行的步骤下标不能为空"});
                } else {
                    int stageIndexParam = (int) skipInput.get(Dict.STAGEINDEX);
                    int jobIndexParam = (int) skipInput.get(Dict.JOBINDEX);
                    int stepIndexParam = (int) skipInput.get(Dict.STEPINDEX);
                    String skipIndex = new StringBuffer().append(stageIndexParam).append("_")
                            .append(jobIndexParam).append("_").append(stepIndexParam).toString();
                    skipMap.put(skipIndex, null);
                }
            }
            logger.info("******triggerPipeline[" + resPipeline.getId() + "] input skipped step index :" + skipMap);

            if (!skipMap.isEmpty()) {
                //准备待执行的流水线
                List<Stage> stages = orgPipeline.getStages();
                for (int i = 0; i < stages.size(); i++) {
                    Stage stage = stages.get(i);
                    List<Job> jobs = stage.getJobs();
                    for (int j = 0; j < jobs.size(); j++) {
                        int removeJobNum = 0;
                        Job job = jobs.get(j);
                        List<Step> steps = job.getSteps();
                        //找到对应的步骤列表，只获取要执行的步骤
                        List<Step> resSteps = resPipeline.getStages().get(i).getJobs().get(j).getSteps();
                        resSteps.clear();
                        for (int k = 0; k < steps.size(); k++) {
                            String currentIndex = new StringBuffer().append(i).append("_")
                                    .append(j).append("_").append(k).toString();
                            if (!skipMap.containsKey(currentIndex)) {
                                //没有跳过该步骤，该步骤放到结果步骤里
                                resSteps.add(steps.get(k));
                            }
                        }
                        logger.info("******triggerPipeline[" + resPipeline.getId() + "] stage [" + i + "]" + "job [" + j + "] steps info:"
                                + JSONObject.toJSONString(resSteps));
                    }

                }

                //如果step为空,删除job; job为空,删除stage，stage为空报错。
                List<Stage> resultStages = resPipeline.getStages();
                List<Stage> resultStages1 = new ArrayList<Stage>();
                for (int i = 0; i < resultStages.size(); i++) {
                    List<Job> resJobs = resultStages.get(i).getJobs();
                    List<Job> resJobs1 = new ArrayList<Job>();
                    for (int j = 0; j < resJobs.size(); j++) {
                        if (!resJobs.get(j).getSteps().isEmpty()) {
                            resJobs1.add(resJobs.get(j));
                        } else {
                            logger.info("******triggerPipeline[" + resPipeline.getId() + "] stage [" + i + "]"
                                    + "job [" + j + "] removed !");
                        }
                    }
                    resultStages.get(i).setJobs(resJobs1);
                    if (!resultStages.get(i).getJobs().isEmpty()) {
                        resultStages1.add(resultStages.get(i));
                    } else {
                        logger.info("******triggerPipeline[" + resPipeline.getId() + "] stage [" + i + "]"
                                + " removed !");
                    }
                }
                if (resultStages1.isEmpty()) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"跳过了全部步骤，无法触发流水线"});
                } else {
                    resPipeline.setStages(resultStages1);
                }
                logger.info("******after skip steps, triggerPipeline[" + resPipeline.getId() + "] info:" + JSONObject.toJSONString(resPipeline));
            }
        }
        BindProject bindProject = resPipeline.getBindProject();
        //该用户对象存入pipelineExe
        Author author = null;
        if (triggerType.equals(Dict.SCHEDULE)) {
            author = new Author();
            author.setId(Dict.SYSTEM);
            author.setNameCn(Dict.SYSTEM);
            author.setNameEn(Dict.SYSTEM);
        } else if (CommonUtils.isNullOrEmpty(userId)) {
            author = userService.getAuthor();
        } else if (!CommonUtils.isNullOrEmpty(userId)) {
            //有userId查询用户信息
            Map userInfo = userService.queryUserById(userId);
            author = new Author();
            author.setId((String) userInfo.get(Dict.ID));
            author.setNameCn((String) userInfo.get(Dict.USER_NAME_CN));
            author.setNameEn((String) userInfo.get(Dict.USER_NAME_EN));
        }
        //新增tagFlag参数来标识当前跑pipeline的分支是否为tag
        resPipeline.setRunVariables(runVariables);
        return executePipeline(resPipeline, triggerType, author, branch, bindProject.getGitlabProjectId(), tagFlag);
    }

    @Override
    public String add(Pipeline pipeline) throws Exception {
        boolean isAppManager = vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId());
        if (!isAppManager) {
            throw new FdevException(ErrorConstants.USER_NOT_APPMANAGER);
        }
        //条线id
        Map project = appService.queryAppDetailById(pipeline.getBindProject().getProjectId());
        if (CommonUtils.isNullOrEmpty(project)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"无法查询到应用信息"});
        }
//        String groupLineId = vaildateUtil.getProjectSectionId(project);
//        if (CommonUtils.isNullOrEmpty(groupLineId)) {
//            //不存在条线信息
//            Map group = (Map) project.get(Dict.GROUP);
//            String groupId = (String) group.get(Dict.ID);
//            logger.error("******cannot find project section groupId ,projectNameEn:" + pipeline.getBindProject().getNameEn()
//                    + ", projectGroupId:" + groupId);
////            throw new FdevException(ErrorConstants.USER_IS_NOT_EXISTS_LINEID);
//        } else
//            pipeline.setGroupLineId(groupLineId);
        CommonUtils.checkCronExpression(pipeline);
        CommonUtils.stagesCheck(pipeline.getStages());
        //删除草稿
        pipelineDao.deleteDraft(userService.getUserFromRedis().getId());
        pipeline.setNameId(null);
        //将plugin中信息取出放入到pipeline
        preparePipeline(pipeline);
        pipeline.setVersion(Constants.UP_CHANGE_VERSION);
        pipeline.setAuthor(userService.getAuthor());
        String pipelineId = pipelineDao.add(pipeline);
        updateUseCountWhenAdd(pipelineId);
        return pipelineId;
    }

    @Override
    public Map addByTemplateNameIdAndBindProject(String nameId, BindProject bindProject, String userId) throws Exception {
        boolean isAppManager = vaildateUtil.projectConditionNoAuth(bindProject.getProjectId(), userId);
        if (!isAppManager) {
            throw new FdevException(ErrorConstants.USER_NOT_APPMANAGER);
        }
        PipelineTemplate pipelineTemplate = pipelineTemplateDao.queryByNameId(nameId);
        if (CommonUtils.isNullOrEmpty(pipelineTemplate) || pipelineTemplate == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"流水线模版nameId" + nameId});
        }
        if (CommonUtils.isNullOrEmpty(bindProject)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"应用信息"});
        }
        Pipeline pipeline = new Pipeline();
//        String groupLineId = checkGetGroupLineId(userId, pipeline);
//        if (groupLineId == null) {
//            //即为当前用户的条线不存在 or 发老的模块不存在条线信息
//            logger.debug(">>>>>>>>>> current user is not exists group line <<<<<<<<<<<<<");
////            throw new FdevException(ErrorConstants.USER_IS_NOT_EXISTS_LINEID);
//        } else
//            pipeline.setGroupLineId(groupLineId);
        pipeline.setName(pipelineTemplate.getName());
        pipeline.setDesc(pipelineTemplate.getDesc());
        pipeline.setBindProject(bindProject);
        pipeline.setPipelineTemplateId(pipelineTemplate.getId());
        pipeline.setPipelineTemplateNameId(pipelineTemplate.getNameId());
        pipeline.setFixedModeFlag(pipelineTemplate.getFixedModeFlag());
        pipeline.setStages(pipelineTemplate.getStages());
        //没有设置有image
        /*List<Stage> stages = pipeline.getStages();
        for (Stage stage : stages) {
            for (Job job : stage.getJobs()) {
                Images image = job.getImage();
                if (CommonUtils.isNullOrEmpty(image)) {
                    job.setImage(pipelineDao.findDefaultImage());
                }
            }
        }*/
        pipeline.setVersion(Constants.UP_CHANGE_VERSION);
        if (CommonUtils.isNullOrEmpty(userId)) {
            //如果userId为空时，用当前登录用户
            //如果userId不为空时，用该id发送用户模块查询用户信息（因为该接口会给其他哟个sourceback调用）
            pipeline.setAuthor(userService.getAuthor());
        } else {
            Author author = new Author();
            author.setId(userId);
            Map userInfo = this.userService.queryUserById(userId);
            String userNameEn = (String) userInfo.get("user_name_en");
            String userNameCn = (String) userInfo.get("user_name_cn");
            author.setNameEn(userNameEn);
            author.setNameCn(userNameCn);
            pipeline.setAuthor(author);
        }
        String pipelineId = pipelineDao.add(pipeline);
        //增加插件统计次数
        updateUseCountWhenAdd(pipelineId);
        Map resultMap = new HashMap<>();
        resultMap.put(Dict.ID, pipelineId);
        resultMap.put(Dict.NAMEID, pipelineId);
        resultMap.put(Dict.PIPELINENAME, pipelineTemplate.getName());
        return resultMap;
    }

    public Pipeline preparePipeline(Pipeline pipeline) throws Exception {
        for (Stage stage : pipeline.getStages()) {
            for (Job job : stage.getJobs()) {
                //1 如果runner集群为空报错
                if (CommonUtils.isNullOrEmpty(job.getRunnerClusterId())) {
                    logger.error("runnerCluster cannot be empty !pipelineName: " + pipeline.getName() + " jobName: " + job.getName());
                    throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"任务[" + job.getName() + "]的构建集群"});
                }
                String runnerClusterID = job.getRunnerClusterId();
                RunnerCluster runnerCluster = runnerClusterService.getRunnerClusterById(runnerClusterID);
                if (runnerCluster == null) {
                    logger.error("runnerCluster not exist! runnerClusterID: " + runnerClusterID + " pipelineName: " + pipeline.getName() + " jobName: " + job.getName());
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务[" + job.getName() + "]的构建集群不存在或已经关闭"});
                }
                //2 根据集群的id查找平台，如果是linux,镜像不能为空。否则镜像为空
                String runnerPlatform = runnerCluster.getPlatform();
                if (runnerPlatform.equals(Constants.PLATFORM_LINUX)) {
                    if (job.getImage() == null || CommonUtils.isNullOrEmpty(job.getImage().getId())) {
                        logger.error("image cannot be empty !pipelineName: " + pipeline.getName() + " jobName: " + job.getName());
//                        throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"任务[" + job.getName() + "]的工作镜像"});
                    }
                } else {
                    job.setImage(null);
                }
                //3 根据集群id，检查插件运行平台，如果不匹配，报错
                for (Step step : job.getSteps()) {
                    PluginInfo inputPlugin = step.getPluginInfo();
                    Plugin plugin = pluginDao.queryPluginDetail(inputPlugin.getPluginCode());
                    if (plugin == null) {
                        logger.error("**********plugin not exist, pluginId=" + inputPlugin.getPluginCode());
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"步骤[" + step.getName() + "]的插件"});
                    }
                    if (CommonUtils.isNullOrEmpty(plugin.getPlatform()) || !plugin.getPlatform().contains(runnerPlatform)) {
                        logger.error("platform not match !"
                                + " runnerPlatform: " + runnerPlatform
                                + " pluginplatform: " + plugin.getPlatform()
                                + " plugin: " + plugin.getPlatform());
                        throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"插件[" + plugin.getPluginName() + "]需要在" + runnerPlatform + "平台上运行！请重新选择构建集群"});
                    }
                    //准备插件参数信息
                    prepareStep(step);
                }
            }
        }
        return pipeline;
    }

    /**
     * 编辑任务模板中的步骤信息，主要是带了插件默认的属性参数，以及
     *
     * @param step
     * @return
     * @throws Exception
     */
    @Override
    public Step prepareJobTemplateStep(Step step) throws Exception {
        //1 查询插件详情
        PluginInfo inputPlugin = step.getPluginInfo();

        Plugin plugin = pluginDao.queryPluginDetail(inputPlugin.getPluginCode());
        if (plugin == null) {
            logger.error("**********plugin not exist, pluginId=" + inputPlugin.getPluginCode());
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"plugin not exist"});
        }
        //插件过期了改成最新的插件
        if (plugin.getStatus().equals(Constants.STATUS_CLOSE)) {
            List<Plugin> pluginList = pluginDao.queryPluginDetailByNameId(plugin.getNameId());
            plugin = pluginDao.queryPluginDetail(pluginList.get(0).getPluginCode());
        }

        inputPlugin.setName(plugin.getPluginName());
        step.setName(plugin.getPluginName());
        inputPlugin.setDesc(plugin.getPluginDesc());
        inputPlugin.setPluginCode(plugin.getPluginCode());

        //2 如果插件需要有构建物，增加用户填的值，否则和插件保持一致
        preparePluginArtifacts(inputPlugin, plugin);
        //3 如果插件没有脚本，script为空
        preparePluginScript(inputPlugin, plugin);
        //4 如果插件没有入参，和插件一致
        List<Map<String, Object>> params = new ArrayList<>();
        List<Map<String, Object>> pluginParams = plugin.getParams();
        if (!CommonUtils.isNullOrEmpty(pluginParams)) {
            //如果插件有参
            List inputParamList = inputPlugin.getParams();
            Map<String, Map> inputParamMaps = CommonUtils.listToMap(inputParamList, Dict.KEY);
            //遍历插件参数，插件没有的参数不要了。
            for (Map pluginParam : pluginParams) {
                Map resPluginParam = new HashMap();
                //一个参数有以下属性：DEFAULT,DEFAULTARR,HINT,LABEL,REQUIRED,HIDDEN,ITEMVALUE,
                resPluginParam.putAll(pluginParam);

                String key = (String) pluginParam.get(Dict.KEY);
                String type = (String) pluginParam.get(Dict.TYPE);
                if (inputParamMaps.containsKey(key)) {
                    //前端上送了这个参数信息
                    Map inputParam = inputParamMaps.get(key);
                    resPluginParam.put(Dict.VALUE, inputParam.get(Dict.VALUE));

                    //参数一共有以下类型：input、multipleInput、password、select, multipleSelect, fileEdit,fileUpload,entityType
                    //如果是上传文件类型，将文件目录从temporary转移到upload
                    if (Objects.equals(type, Dict.FILEUPLOAD)) {
                        String value = (String) inputParam.get(Dict.VALUE);
                        value = renameUploadFile(value);
                        resPluginParam.put(Dict.VALUE, value);
                    } else if (type.equals(Dict.ENTITYTYPE)) {
                        //插件中的实体类型，包含id 中英文名
                        List entityTemplateList = (List) pluginParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        List<Map> inputEntityTempParamList = (List<Map>) inputParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        List list = prepareEntityTemplateParam(entityTemplateList, inputEntityTempParamList);
                        //设置实体类型参数列表（可以支持一个实体类型多个实体参数），
                        resPluginParam.put(Dict.ENTITYTEMPLATEPARAMS, list);
                    } else if (type.equals(Dict.MULTIPLESELECT)) {
                        resPluginParam.put(Dict.VALUEARRAY, inputParam.get(Dict.VALUEARRAY));
                    }
                } else {
                    //前端没有送这个参数
                    resPluginParam.put(Dict.VALUE, pluginParam.get(Dict.DEFAULT));
                    if (type.equals(Dict.ENTITYTYPE)) {
                        List entityTemplateList = (List) pluginParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        if (!CommonUtils.isNullOrEmpty(entityTemplateList)) {
                            Map entityMap = new HashMap();
                            Map entityTemplateMap = (Map) entityTemplateList.get(0);
                            entityMap.put(Dict.ID, entityTemplateMap.get(Dict.ID));
                            entityMap.put(Dict.NAMEEN, entityTemplateMap.get(Dict.NAMEEN));
                            entityMap.put(Dict.NAMECN, entityTemplateMap.get(Dict.NAMECN));
                            entityMap.put(Dict.ENTITY, null);
                            entityMap.put(Dict.ENV, null);
                            //根据实体模板查询获取属性字段填入
                            List entityParamsList = prepareEntityParams((String) entityMap.get(Dict.ID), null);
                            entityMap.put(Dict.ENTITYPARAMS, entityParamsList);
                            List list = new ArrayList();
                            list.add(entityMap);
                            resPluginParam.put(Dict.ENTITYTEMPLATEPARAMS, list);
                        }
                    } else if (type.equals(Dict.MULTIPLESELECT)) {
                        resPluginParam.put(Dict.VALUEARRAY, pluginParam.get(Dict.DEFAULTARR));
                    }
                }
                params.add(resPluginParam);
            }
        }
        inputPlugin.setParams(params);
        return step;
    }

    /**
     * 编辑流水线和流水线中的步骤信息
     *
     * @param step
     * @return
     * @throws Exception
     */
    @Override
    public Step prepareStep(Step step) throws Exception {
        //1 查询插件详情
        PluginInfo inputPlugin = step.getPluginInfo();
        Plugin plugin = pluginDao.queryPluginDetail(inputPlugin.getPluginCode());
        if (plugin == null) {
            logger.error("**********plugin not exist, pluginId=" + inputPlugin.getPluginCode());
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"plugin not exist"});
        }

        inputPlugin.setName(plugin.getPluginName());
        inputPlugin.setDesc(plugin.getPluginDesc());
        inputPlugin.setPluginCode(plugin.getPluginCode());

        //2 如果插件需要有构建物，增加用户填的值，否则和插件保持一致
        preparePluginArtifacts(inputPlugin, plugin);
        //3 如果插件没有脚本，script为空
        preparePluginScript(inputPlugin, plugin);
        //4 如果插件没有入参，和插件一致
        List<Map<String, Object>> params = new ArrayList<>();
        List<Map<String, Object>> pluginParams = plugin.getParams();
        if (!CommonUtils.isNullOrEmpty(pluginParams)) {
            //如果插件有参
            List inputParamList = inputPlugin.getParams();
            Map<String, Map> inputParamMaps = CommonUtils.listToMap(inputParamList, Dict.KEY);
            //遍历插件参数，插件没有的参数不要了。
            for (Map pluginParam : pluginParams) {
                String key = (String) pluginParam.get(Dict.KEY);
                String type = (String) pluginParam.get(Dict.TYPE);
                Map resPluginParam = new HashMap();
                resPluginParam.put(Dict.KEY, pluginParam.get(Dict.KEY));
                resPluginParam.put(Dict.TYPE, pluginParam.get(Dict.TYPE));
                if (inputParamMaps.containsKey(key)) {
                    //前端上送了这个参数信息
                    Map inputParam = inputParamMaps.get(key);
                    resPluginParam.put(Dict.VALUE, inputParam.get(Dict.VALUE));
                    resPluginParam.put(Dict.VALUEARRAY, inputParam.get(Dict.VALUEARRAY));

                    //input、multipleInput、password、select, multipleSelect, fileEdit,fileUpload,entityType
                    //如果是上传文件类型，将文件目录从temporary转移到upload
                    if (Objects.equals(type, Dict.FILEUPLOAD)) {
                        String value = (String) inputParam.get(Dict.VALUE);
                        value = renameUploadFile(value);
                        resPluginParam.put(Dict.VALUE, value);
                    } else if (type.equals(Dict.ENTITYTYPE)) {
                        //插件中的实体类型，包含id 中英文名
                        List entityTemplateList = (List) pluginParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        List<Map> inputEntityTempParamList = (List<Map>) inputParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        List list = prepareEntityTemplateParam(entityTemplateList, inputEntityTempParamList);
                        //设置实体类型参数列表（可以支持一个实体类型多个实体参数），
                        resPluginParam.put(Dict.ENTITYTEMPLATEPARAMS, list);
                    }
                } else {
                    //前端没有送这个参数
                    resPluginParam.put(Dict.VALUE, pluginParam.get(Dict.DEFAULT));
                    if (type.equals(Dict.ENTITYTYPE)) {
                        List entityTemplateList = (List) pluginParam.get(Dict.ENTITYTEMPLATEPARAMS);
                        if (!CommonUtils.isNullOrEmpty(entityTemplateList)) {
                            Map entityMap = new HashMap();
                            Map entityTemplateMap = (Map) entityTemplateList.get(0);
                            entityMap.put(Dict.ID, entityTemplateMap.get(Dict.ID));
                            entityMap.put(Dict.NAMEEN, entityTemplateMap.get(Dict.NAMEEN));
                            entityMap.put(Dict.NAMECN, entityTemplateMap.get(Dict.NAMECN));
                            entityMap.put(Dict.ENTITY, null);
                            entityMap.put(Dict.ENV, null);
                            //根据实体模板查询获取属性字段填入
                            List entityParamsList = prepareEntityParams((String) entityMap.get(Dict.ID), null);
                            entityMap.put(Dict.ENTITYPARAMS, entityParamsList);
                            List list = new ArrayList();
                            list.add(entityMap);
                            resPluginParam.put(Dict.ENTITYTEMPLATEPARAMS, list);
                        }
                    } else if (type.equals(Dict.MULTIPLESELECT)) {
                        resPluginParam.put(Dict.VALUEARRAY, pluginParam.get(Dict.DEFAULTARR));
                    }
                }
                params.add(resPluginParam);
            }
        }
        inputPlugin.setParams(params);
        return step;
    }

    /**
     * 准备步骤中的脚本，脚本没改，和上送的一样；改了，重新上传；如果插件没有脚本，script为空
     *
     * @param inputPlugin
     * @param plugin
     * @throws Exception
     */
    private void preparePluginScript(PluginInfo inputPlugin, Plugin plugin) throws Exception {
        if (CommonUtils.isNullOrEmpty(plugin.getScript())) {
            inputPlugin.setScript(null);
        } else {
            logger.info("====test1:" + inputPlugin.getScriptCmd());
            logger.info("====test11:" + inputPlugin.getScriptUpdateFlag());
            //否则判断是否改过脚本内容，如果改过，上传用户脚本，没改过，取前端送的
            String scriptCmd = inputPlugin.getScriptCmd();
            //boolean scriptUpdateFlag = inputPlugin.getScriptUpdateFlag();
            if (!CommonUtils.isNullOrEmpty(inputPlugin.getScriptUpdateFlag()) && inputPlugin.getScriptUpdateFlag()) {
                Map script = uploadScript(scriptCmd);
                inputPlugin.setScript(script);
            }
        }
    }

    /**
     * 如果插件需要有构建物，增加用户填的值，否则和插件保持一致
     *
     * @param inputPlugin
     * @param plugin
     */
    private void preparePluginArtifacts(PluginInfo inputPlugin, Plugin plugin) {
        Map<String, Object> pluginArtifactsInfo = plugin.getArtifacts();
        if (!CommonUtils.isNullOrEmpty(pluginArtifactsInfo)) {
            if (!CommonUtils.isNullOrEmpty(inputPlugin.getArtifacts())) {
                pluginArtifactsInfo.put(Dict.VALUE, inputPlugin.getArtifacts().get(Dict.VALUE));
            } else {
                pluginArtifactsInfo.put(Dict.VALUE, pluginArtifactsInfo.get(Dict.DEFAULT));
            }
            pluginArtifactsInfo.remove(Dict.DEFAULT);
        }
        inputPlugin.setArtifacts(pluginArtifactsInfo);
    }

    /**
     * 插件中的脚本内容有更新，重新上传minio
     *
     * @param scriptCmd
     * @throws Exception
     */
    private Map uploadScript(String scriptCmd) throws Exception {
        if (CommonUtils.isNullOrEmpty(scriptCmd)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"scriptCmd"});
        }
        Map script = new HashMap();
        String id = new ObjectId().toString();
        String filePath = id + ".sh";
        byte[] bytes = scriptCmd.getBytes();
        //文件转换
        MockMultipartFile multipartFile = new MockMultipartFile(Dict.FILE, filePath, "text/plain", bytes);
        //上传minio
        Map<String, String> restMap = iPluginService.uploadFile(multipartFile, id);
        //存放mio名称
        script.put(Dict.MINIO_OBJECT_NAME, restMap.get(Dict.PACKAGE_PATH));
        logger.info("**********the script hava changed，new Address：" + script);
        return script;
    }

    /**
     * 如果是上传文件类型，将文件目录从temporary转移到upload
     *
     * @param sourceFileName
     * @return
     * @throws FileNotFoundException
     */
    private String renameUploadFile(String sourceFileName) throws FdevException {
        if (!CommonUtils.isNullOrEmpty(sourceFileName)) {
            String[] splitLeftSlash = sourceFileName.split("/");
            int i = splitLeftSlash.length - 1;
            int j = splitLeftSlash.length - 2;
            String fileName = splitLeftSlash[i];
            String idDirectory = splitLeftSlash[j];
            File nasRelUrlFile = new File(relPluginRelativeRootUrl + idDirectory);
            if (!nasRelUrlFile.exists()) {
                nasRelUrlFile.mkdirs();
            }
            String saveUrl = relPluginRelativeRootUrl + idDirectory + "/" + fileName;
            String temporary = splitLeftSlash[2];
            if (Dict.TEMPORARY.equals(temporary)) {
                try {
                    File sourceFile = new File(sourceFileName);
                    File outFile = new File(saveUrl);
                    sourceFile.renameTo(outFile);
                } catch (Exception e) {
                    logger.error("rename file failed, " + sourceFileName);
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"上传文件重命名失败"});
                }
                sourceFileName = saveUrl;
            }
        }
        return sourceFileName;
    }

    /**
     * 准备实体模板输入参数
     *
     * @param pluginEntityTemplateList 插件param中的一个实体模板
     * @param inputEntityTempParamList 前端上送的实体模板参数
     * @return 组装好流水线中的该参数集合
     * @throws Exception
     */
    public List prepareEntityTemplateParam(List pluginEntityTemplateList, List<Map> inputEntityTempParamList) throws Exception {
        List resEntityParamList = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(pluginEntityTemplateList)) {
            Map pluginEntityTemplate = (Map) pluginEntityTemplateList.get(0);
            if (!CommonUtils.isNullOrEmpty(inputEntityTempParamList)) {
                for (int i = 0; i < inputEntityTempParamList.size(); i++) {
                    //现在只支持一个模板选一个实体,本段代码支持一个模板选多个实体或者手动输入多套
                    Map inputParamMap = inputEntityTempParamList.get(i);
                    if (CommonUtils.isNullOrEmpty(inputParamMap.get(Dict.ID)) || !((String) pluginEntityTemplate.get(Dict.ID)).equals((String) inputParamMap.get(Dict.ID))) {
                        //校验输入的实体模板ID与插件实体模板id配套
                        continue;
                    }
                    Map resEntityMap = new HashMap();
                    resEntityMap.putAll(pluginEntityTemplate);
                    //用户输入了这个实体模板，判断是否选了实体
                    Map entity = null;
                    if (!CommonUtils.isNullOrEmpty(inputParamMap.get(Dict.ENTITY))) {
                        entity = (Map) inputParamMap.get(Dict.ENTITY);
                    }

                    if (!CommonUtils.isNullOrEmpty(entity) && entity.containsKey(Dict.ID)) {
                        //选了实体
                        resEntityMap.put(Dict.ENTITY, inputParamMap.get(Dict.ENTITY));
                        resEntityMap.put(Dict.ENV, inputParamMap.get(Dict.ENV));
                        resEntityMap.put(Dict.ENTITYPARAMS, null);
                    } else {
                        //没选实体
                        resEntityMap.put(Dict.ENTITY, null);
                        resEntityMap.put(Dict.ENV, null);
                        //准备实体模板的手动输入参数列表
                        List<Map> inputEntityParamList = (List<Map>) inputParamMap.get(Dict.ENTITYPARAMS);
                        List entityParamsList = prepareEntityParams((String) pluginEntityTemplate.get(Dict.ID), inputEntityParamList);
                        resEntityMap.put(Dict.ENTITYPARAMS, entityParamsList);
                    }
                    resEntityParamList.add(resEntityMap);
                }
            }
        }

        return resEntityParamList;
    }

    /**
     * 准备实体模板的手动输入参数列表
     *
     * @param entityTemplateId  实体模板idist
     * @param inputEntityParams 这个实体模下，用户输入的entityParams  可以为空，
     * @return
     * @throws Exception
     */
    public List prepareEntityParams(String entityTemplateId, List<Map> inputEntityParams) throws Exception {
        List entityParamsList = new ArrayList();
        /*Map inputEntityParamMap = null;
        if (!CommonUtils.isNullOrEmpty(inputEntityParams)) {
            inputEntityParamMap = CommonUtils.listToMap(inputEntityParams, Dict.KEY);
        }
        List<Map> tempParamList = iAppService.queryModelTemplateDetailInfo(entityTemplateId);
        if (!CommonUtils.isNullOrEmpty(tempParamList)) {
            for (Map tempParam : tempParamList) {
                Map entityParam = new HashMap();
                entityParam.put(Dict.KEY, tempParam.get(Dict.NAMEEN));
                entityParam.put(Dict.LABEL, tempParam.get(Dict.NAMECN));
                entityParam.put(Dict.REQUIRED, tempParam.get(Dict.REQUIRED));
                //如果用户填了值，获取value
                String value = null;
                if (!CommonUtils.isNullOrEmpty(inputEntityParamMap)) {
                    Map inputEntityParam = (Map) inputEntityParamMap.get(tempParam.get(Dict.NAMEEN));
                    if (!CommonUtils.isNullOrEmpty(inputEntityParam)) {
                        value = (String) inputEntityParam.get(Dict.VALUE);
                    }
                }
                entityParam.put(Dict.VALUE, value);
                entityParamsList.add(entityParam);
            }
        }*/
        /**
         * 此处自定义输入参数值的时候会先根据plugin表中配置的实体ID查询实体模版参数key限制，然后根据模块中的参数从传入进来
         * 的参数中获取值保存进Pipeline表的entityParams中去，所有不在模版中的字段都舍弃了
         */
        if (CommonUtils.isNullOrEmpty(inputEntityParams)) {
            return entityParamsList;
        }
        for (Map inputMapItem : inputEntityParams) {
            Map entityParam = new HashMap();
            entityParam.put(Dict.KEY, inputMapItem.get(Dict.KEY));
            entityParam.put(Dict.LABEL, inputMapItem.get(Dict.LABEL));
            entityParam.put(Dict.REQUIRED, Boolean.TRUE);
            //如果用户填了值，获取value
            String value = null;
            if (!CommonUtils.isNullOrEmpty(inputMapItem)) {
                value = (String) inputMapItem.get(Dict.VALUE);
            }
            entityParam.put(Dict.VALUE, value);
            entityParamsList.add(entityParam);
        }

        return entityParamsList;
    }

    @Override
    public String update(Pipeline pipeline) throws Exception {
        boolean isAppManager = vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId());
        if (!isAppManager) {
            throw new FdevException(ErrorConstants.USER_NOT_APPMANAGER);
        }
        CommonUtils.checkCronExpression(pipeline);
        CommonUtils.stagesCheck(pipeline.getStages());
        String nameId = pipeline.getNameId();
        String oldPipelineId = pipeline.getId();
        //1. 旧版本status均设为0
        Pipeline orgPipeline = pipelineDao.findActiveVersion(pipeline.getNameId());
        if (CommonUtils.isNullOrEmpty(orgPipeline)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"nameId = " + nameId});
        }
        Pipeline oldPipeline = this.pipelineDao.queryById(orgPipeline.getId());
        pipeline.setAuthor(userService.getAuthor());
        preparePipeline(pipeline);
        /*用JSONCompare比较流水线是否有改动，过滤流水线部分非持久化字段，不进行比较*/
        ValueFilter valueFilter = (o, s, o1) -> {
            if (s.contains("validFlag") || s.contains("scriptCmd") || s.contains("collectedOrNot") || s.contains("updateRight")) {
                return null;
            }
            if (s.contains("scriptUpdateFlag") && !Objects.equals(o1, true)) {
                return null;
            }
            return o1;
        };
        String oldPipelineString = JSON.toJSONString(oldPipeline, valueFilter);
        String pipelineString = JSON.toJSONString(pipeline, valueFilter);
        JSONCompareResult jsonCompareResult = JSONCompare.compareJSON(oldPipelineString, pipelineString, JSONCompareMode.STRICT);
        if (jsonCompareResult.passed()) {
            /*流水线比较无变化返回原来流水线id*/
            return pipeline.getId();
        }
        /*流水线比较完成有变化*/
        //2. 新版本的版本号自动新增
        Integer version = Integer.valueOf(orgPipeline.getVersion().split("\\.")[0]);
        Integer newVersion = version + 1;
        pipeline.setVersion(String.valueOf(newVersion));
        String newPipelineId = pipelineDao.add(pipeline);
        pipelineDao.updateStatusClose(orgPipeline.getId(), userService.getUserFromRedis());
        PipelineUpdateDiff diffEntity = DiffUtils.getHandlerPipelineDiff(oldPipeline, pipeline);
        this.pipelineUpdateDiffDao.saveDiff(diffEntity);
        updateUseCountWhenUpdate(oldPipelineId, newPipelineId);
        return newPipelineId;
    }

    /**
     * 更新流水线时，更新插件统计次数
     *
     * @param oldPipelineId 更新前的ID
     * @param newPipelineId 更新后的ID
     * @throws Exception
     */
    private void updateUseCountWhenUpdate(String oldPipelineId, String newPipelineId) throws Exception {
        //1.减去之前应用使用插件的次数
        updateUseCountWhenDelPipeline(oldPipelineId);
        //2.新增现在应用使用插件的次数
        updateUseCountWhenAdd(newPipelineId);

        /*
        if (!CommonUtils.isNullOrEmpty(newPipelineId)) {
            //先统计一下修改之前被使用的情况
            List<PluginUseCount> beforeUpdatePluginUseCounts = pluginDao.statisticsPluginUseCount(oldPipelineId);
            //再统计一下修改之后被使用的情况
            List<PluginUseCount> afterUpdatePluginUseCounts = pluginDao.statisticsPluginUseCount(newPipelineId);
            if (!CommonUtils.isNullOrEmpty(beforeUpdatePluginUseCounts) && !CommonUtils.isNullOrEmpty(afterUpdatePluginUseCounts)) {
                //如果为true，则表示该流水线没有更换应用，否则就更换了
                if (beforeUpdatePluginUseCounts.get(0).getBindProject().getProjectId().equals(afterUpdatePluginUseCounts.get(0).getBindProject().getProjectId())) {
                    //没有更换应用
                    Map<String,PluginUseCount> beforeUpdatePluginUseCountsMap = new HashMap<>();
                    for (PluginUseCount beforeUpdatePluginUseCount : beforeUpdatePluginUseCounts) {
                        beforeUpdatePluginUseCountsMap.put(beforeUpdatePluginUseCount.getPluginCode(),beforeUpdatePluginUseCount);
                    }
                    for (PluginUseCount afterUpdatePluginUseCount : afterUpdatePluginUseCounts) {
                        String pluginCode = afterUpdatePluginUseCount.getPluginCode();
                        String bindProjectId = afterUpdatePluginUseCount.getBindProject().getProjectId();
                        String useCount = afterUpdatePluginUseCount.getUseCount();
                        PluginUseCount remove = beforeUpdatePluginUseCountsMap.remove(pluginCode);
                        PluginUseCount temp = pluginDao.queryPluginUseCountByPluginCodeAndBindProjectId(pluginCode,bindProjectId);
                        //如果为空，则表示这个插件是此应用新增使用的
                        if (CommonUtils.isNullOrEmpty(remove)) {
                            //看数据库中有该应用使用该插件的记录没
                            if (!CommonUtils.isNullOrEmpty(temp)) {
                                temp.setUseCount(String.valueOf(Integer.parseInt(temp.getUseCount()) + Integer.parseInt(useCount)));
                                pluginDao.savePluginUseCount(temp);
                            } else {
                                BindProject bindProject = pipelineDao.queryOneByProjectId(bindProjectId).getBindProject();
                                afterUpdatePluginUseCount.setBindProject(bindProject);
                                pluginDao.savePluginUseCount(afterUpdatePluginUseCount);
                            }
                        } else {
                            int resultCount = Integer.parseInt(useCount) - Integer.parseInt(remove.getUseCount());
                            //如果为0，则此应用使用该插件的次数不变，如果不为0，则需要修改次数
                            if (resultCount != 0) {
                                temp.setUseCount(String.valueOf(Integer.parseInt(temp.getUseCount()) + resultCount));
                                pluginDao.savePluginUseCount(temp);
                            }
                        }
                    }
                    //如果不为空，则表示此流水线更新之后不再使用某些之前的插件
                    if (!CommonUtils.isNullOrEmpty(beforeUpdatePluginUseCountsMap)) {
                        for (PluginUseCount pluginUseCount : beforeUpdatePluginUseCountsMap.values()) {
                            String pluginCode = pluginUseCount.getPluginCode();
                            String bindProjectId = pluginUseCount.getBindProject().getProjectId();
                            String useCount = pluginUseCount.getUseCount();
                            PluginUseCount temp = pluginDao.queryPluginUseCountByPluginCodeAndBindProjectId(pluginCode, bindProjectId);
                            //修改数据库的次数，如果为0则需要在数据库中删除记录
                            int resultCount = Integer.parseInt(temp.getUseCount()) - Integer.parseInt(useCount);
                            temp.setUseCount(String.valueOf(resultCount));
                            pluginDao.savePluginUseCount(temp);
                        }
                    }
                } else {
                    //1.减去之前应用使用插件的次数
                    updateUseCountWhenDelPipeline(oldPipelineId);
                    //2.新增现在应用使用插件的次数
                    updateUseCountWhenAdd(newPipelineId);
                }
            }
        }*/
    }

    @Override
    public Pipeline queryById(Map request) throws Exception {
        //1. 查询库中的pipeline
        Pipeline pipeline = pipelineDao.queryPipelineByIdOrNameId(request);
        if (CommonUtils.isNullOrEmpty(pipeline))
            return null;
        pipeline = preparePipelineDetailInfo(pipeline);
        return pipeline;
    }

    @Override
    public void delete(String id) throws Exception {
        //删除仅把状态置为失效
        User user = userService.getUserFromRedis();
        Pipeline pipeline = pipelineDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(pipeline)) {
            logger.error("**********pipeline not exist, pipelineId:" + id);
            /*throw new Exception("找不到该流水线");*/
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId = " + id});
        }
        pipelineDao.updateStatusClose(id, user);
        iPluginService.closeYamlConfigInStages(pipeline.getStages());
        updateUseCountWhenDelPipeline(id);
    }

    /**
     * 删除pipeline时更新插件使用次数
     *
     * @param id
     * @throws Exception
     */
    private void updateUseCountWhenDelPipeline(String id) throws Exception {
        List<PluginUseCount> pluginUseCountList = pluginDao.statisticsPluginUseCount(id);
        if (!CommonUtils.isNullOrEmpty(pluginUseCountList)) {
            for (PluginUseCount pluginUseCount : pluginUseCountList) {
                String pluginCode = pluginUseCount.getPluginCode();
                String bindProjectId = pluginUseCount.getBindProject().getProjectId();
                String useCount = pluginUseCount.getUseCount();
                PluginUseCount temp = pluginDao.queryPluginUseCountByPluginCodeAndBindProjectId(pluginCode, bindProjectId);
                if (!CommonUtils.isNullOrEmpty(temp)) {
                    //避免插件查询统计没有记录出错
                    int resultCount = Integer.parseInt(temp.getUseCount()) - Integer.parseInt(useCount);
                    temp.setUseCount(String.valueOf(resultCount));
                    pluginDao.savePluginUseCount(temp);
                }
            }
        }
    }

    /**
     * 新增流水线时更新插件使用次数
     *
     * @param pipelineId
     * @throws Exception
     */
    private void updateUseCountWhenAdd(String pipelineId) throws Exception {
        if (!CommonUtils.isNullOrEmpty(pipelineId)) {
            List<PluginUseCount> pluginUseCountList = pluginDao.statisticsPluginUseCount(pipelineId);
            if (!CommonUtils.isNullOrEmpty(pluginUseCountList)) {
                for (PluginUseCount pluginUseCount : pluginUseCountList) {
                    String pluginCode = pluginUseCount.getPluginCode();
                    String bindProjectId = pluginUseCount.getBindProject().getProjectId();
                    String useCount = pluginUseCount.getUseCount();
                    PluginUseCount temp = pluginDao.queryPluginUseCountByPluginCodeAndBindProjectId(pluginCode, bindProjectId);
                    //如果不为空，则数据库中有记录，修改useCount
                    if (!CommonUtils.isNullOrEmpty(temp)) {
                        temp.setUseCount(String.valueOf(Integer.parseInt(temp.getUseCount()) + Integer.parseInt(useCount)));
                        pluginDao.savePluginUseCount(temp);
                    } else {
                        pluginUseCount.setBindProject(pipelineDao.queryOneByProjectId(bindProjectId).getBindProject());
                        pluginDao.savePluginUseCount(pluginUseCount);
                    }
                }
            }
        }
    }

    @Override
    public String updateFollowStatus(String pipelineId) throws Exception {
        User user = userService.getUserFromRedis();
        long row = pipelineDao.updateFollowStatus(pipelineId, user);
        if (row > 0) {
            logger.info("*********UPDATE SUCCESS，Effect rows number:{}", row);
            return "SUCCESS";
        }
        logger.info("*********UPDATE FAILED，pipelineId：{}，nameId：{}，state：{},user:{}", pipelineId, user);
        throw new FdevException(ErrorConstants.UPDATE_FOLLOW_STATUS_FAIL, new String[]{"pipelineId = " + pipelineId});
    }

    @Override
    public Map<String, Object> queryAllPipelineList(String pageNum, String pageSize, User user, String searchContent) throws Exception {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> map = pipelineDao.queryPipelineList(skip, limit, null, null, null, user, searchContent);
        handlePipeline((List<Map>) map.get(Dict.PIPELINELIST), user.getId());
        return map;
    }

    @Override
    public Map<String, Object> queryAppPipelineList(String pageNum, String pageSize, String userId, String applicationId, String searchContent) {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> map = pipelineDao.queryPipelineList(skip, limit, applicationId, null, null, null, searchContent);
        handlePipeline((List<Map>) map.get(Dict.PIPELINELIST), userId);
        return map;
    }

    /**
     * 我收藏的流水线
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param searchContent
     * @return
     */
    @Override
    public Map<String, Object> queryCollectionPipelineList(String pageNum, String pageSize, String userId, String searchContent) {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> map = pipelineDao.queryPipelineList(skip, limit, null, userId, null, null, searchContent);
        handlePipeline((List<Map>) map.get(Dict.PIPELINELIST), userId);
        return map;
    }

    /**
     * 查询我的流水线，我负责的流水线列表
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param searchContent
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryMinePipelineList(String pageNum, String pageSize, String userId, String searchContent) throws Exception {
        List<Map<String, Object>> applist = appService.queryMyApps(userId);
        /*取出各应用的管理用户id列表*/
        Map<String, List<String>> id_appMap = applist.stream()
                .collect(Collectors.toMap(app -> (String) app.get(Dict.ID), app -> {
                            List<String> managers = new ArrayList<>();
                            Optional.ofNullable((List<Map<String, String>>) app.get("spdb_managers"))
                                    .map(arr -> arr.stream().map(m -> m.get("id")))
                                    .ifPresent(s -> s.collect(Collectors.toCollection(() -> managers)));
                            Optional.ofNullable((List<Map<String, String>>) app.get("dev_managers"))
                                    .map(arr -> arr.stream().map(m -> m.get("id")))
                                    .ifPresent(s -> s.collect(Collectors.toCollection(() -> managers)));
                            return managers;
                        }
                ));
        if (CommonUtils.isNullOrEmpty(applist)) {
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.TOTAL, "0");
            map.put(Dict.PIPELINELIST, new ArrayList<>());
            return map;
        }
        List<String> apps = applist.stream().map(app -> (String) app.get(Dict.ID)).collect(Collectors.toList());
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> map = pipelineDao.queryPipelineList(skip, limit, null, null, apps, null, searchContent);
        List<Map> pipelineList = (List<Map>) map.get(Dict.PIPELINELIST);
        /*根据当前用户id是否在管理员id列表中，赋值更新权限*/
        pipelineList.forEach(pipeline -> {
            pipeline.remove(Dict._ID);
            pipeline.put("collectedOrNot", !CommonUtils.isNullOrEmpty(pipeline.get(Dict.COLLECTED)) && ((List) pipeline.get(Dict.COLLECTED)).contains(userId));
            String appId = (String) ((Map) pipeline.get(Dict.BINDPROJECT)).get(Dict.PROJECTID);
            pipeline.put("updateRight", (id_appMap.get(appId).contains(userId)));
        });
        return map;
    }

    private void handlePipeline(List<Map> list, String userId) {
        for (Map pipeline : list) {
            if (!CommonUtils.isNullOrEmpty(pipeline.get(Dict._ID))) {
                pipeline.remove(Dict._ID);
            }
            if (!CommonUtils.isNullOrEmpty(pipeline.get(Dict.COLLECTED)) && ((List) pipeline.get(Dict.COLLECTED)).contains(userId)) {
                pipeline.put("collectedOrNot", true);
            } else {
                pipeline.put("collectedOrNot", false);
            }
            boolean flag = false;
            try {
                flag = vaildateUtil.projectCondition((String) ((Map) pipeline.get(Dict.BINDPROJECT)).get(Dict.PROJECTID));
            } catch (Exception e) {
                logger.error("**********the bindProject not belong to the user , userId=" + userId + ";projectId=" + (String) ((Map) pipeline.get(Dict.BINDPROJECT)).get(Dict.PROJECTID));
            }
            pipeline.put("updateRight", flag);
//            PipelineExe exe = pipelineExeDao.queryOneByPipelineIdSort((String) pipeline.get(Dict.ID));
//            if(!CommonUtils.isNullOrEmpty(exe)) {
//                pipeline.put(Dict.USER, exe.getUser().getNameEn());
//            }
        }
    }

    @Override
    public String saveDraft(PipelineDraft draft) throws Exception {
        return pipelineDao.saveDraft(draft);
    }

    @Override
    public PipelineDraft readDraft() throws Exception {
        //1. 查询当前用户的草稿
        String authorID = userService.getUserFromRedis().getId();
        PipelineDraft draft = pipelineDao.readDraftByAuthor(authorID);
        if (CommonUtils.isNullOrEmpty(draft)) {
            return null;
        }
        return draft;
    }

    @Override
    public void retryPipeline(String pipelineExeId) throws Exception {
        Author author = userService.getAuthor();
        PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(pipelineExeId);
        String pipelineId = pipelineExe.getPipelineId();
        Pipeline pipeline = this.pipelineDao.queryById(pipelineId);
        User user = userService.getUserFromRedis();
        if (!vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId())
                && !pipelineExe.getUser().equals(user.getUser_name_en())) {
            logger.error("**********user has not permission to retry");
            throw new FdevException(ErrorConstants.RETRY_FAILED, new String[]{"current user illegal permission"});
        }
        checkRetryStatus(pipelineExe);
        //重试，修改为整条流水线重跑
        logger.info("*********** retry Pipeline， pipelineExeId：" + pipelineExeId);
        List pendingJobs = new ArrayList();
        List<Map<String, Object>> stages = pipelineExe.getStages();
        for (int i = 0; i < stages.size(); i++) {
            //只有第一个stage是pending
            String jobStatus = Dict.WAITING;
            if (i == 0) {
                jobStatus = Dict.PENDING;
            }
            Map<String, Object> stage = stages.get(i);
            stage.put(Dict.STATUS, jobStatus);
            Stage pipStage = pipeline.getStages().get(i);
            List<Map> jobs = (List<Map>) stage.get(Dict.JOBS);

            for (int j = 0; j < jobs.size(); j++) {
                Map job = jobs.get(j);
                List<Map> stageJobExes = (List<Map>) job.get(Dict.JOBEXES);
                //获取最后一个jobexeMap
                Map map = stageJobExes.get(stageJobExes.size() - 1);
                String jobExeId = (String) map.get(Dict.JOBEXEID);
                String runnerClusterId = (String) job.get(Dict.RUNNERCLUSTERID);
                if (CommonUtils.isNullOrEmpty(runnerClusterId)) {
                    throw new FdevException(ErrorConstants.RETRY_FAILED, new String[]{"，或者当前数据存在问题，请重新执行流水线!"});
                }
                //添加新的jobexe
                JobExe jobExe = new JobExe();
                Long number = jobExe.getJobNumber();
                ObjectId objId = new ObjectId();
                JobExe orgJobExe = jobExeService.queryJobExeByExeId(jobExeId);
                BeanUtils.copyProperties(orgJobExe, jobExe);
                jobExe.setJobNumber(number);
                jobExe.setExeId(objId.toString());
                jobExe.setUser(author);
                jobExe.setStatus(jobStatus);
                jobExe.setJobStartTime("");
                jobExe.setJobCostTime("");
                jobExe.setJobEndTime("");
                jobExe.setMinioLogUrl("");
                jobExe.setInfo(null);
                jobExe.setToken("");
                //准备jobExe中的steps
                List<Map> jobExeSteps = jobExe.getSteps();
                for (Map jobExeStep : jobExeSteps) {
                    jobExeStep.remove(Dict.OUTPUT);
                    jobExeStep.remove(Dict.STEPSTARTTIME);
                    jobExeStep.remove(Dict.STEPENDTIME);
                    jobExeStep.remove(Dict.STEPCOSTTIME);
                    jobExeStep.remove(Dict.STATUS);
                }
                jobExeService.saveJobExe(jobExe);

                if (Dict.PENDING.equals(jobStatus)) {
//                    Job pipJob = pipStage.getJobs().get(j);
                    Map jobMap = new HashMap();
                    jobMap.put(Dict.JOB_EXE, jobExe);
                    //集群也存放在pipelineExe中
                    jobMap.put(Dict.RUNNERCLUSTERID, runnerClusterId);
                    pendingJobs.add(jobMap);
                }

                //添加stage-jobs-jobexes中的job实例
                String jobNumber = String.valueOf(jobExe.getJobNumber());
                Map exeMap = new HashMap();
                exeMap.put(Dict.JOBEXEID, objId.toString());
                exeMap.put(Dict.JOBNUMBER, jobNumber);
                exeMap.put(Dict.JOBEXESTATUS, jobStatus);
                stageJobExes.add(exeMap);
            }

        }
        //更新pipelineExe
        pipelineExe.setStatus(Dict.PENDING);
        pipelineExe.setStartTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        pipelineExe.setCostTime("");
        pipelineExe.setEndTime("");
        pipelineExe.setUser(author);
        logger.info("********** retryPipeline will update the pipelineExe info:" + JSONObject.toJSON(pipelineExe));
        pipelineExeService.updateStagesAndStatusAndUser(pipelineExe);
        //更新pipeline构建时间和修改时间
        pipeline.setBuildTime(pipelineExe.getStartTime());
        pipeline.setUpdateTime(pipelineExe.getStartTime());
        pipelineDao.updateBuildTime(pipeline);
        for (Map pendingJob : (List<Map>) pendingJobs) {
            //只把pending的放入队列
            JobExe jobExe = (JobExe) pendingJob.get(Dict.JOB_EXE);
            String runnerClusterId = (String) pendingJob.get(Dict.RUNNERCLUSTERID);
            addJobExeQueue(jobExe, runnerClusterId);
        }
    }

    /**
     * 校验是否可以重试的前提条件，流水线必须是成功/失败/取消。
     *
     * @param pipelineExe
     * @throws Exception 校验失败会报错，校验通过返回true
     */
    private void checkRetryStatus(PipelineExe pipelineExe) throws Exception {
        /*Map bindProject = pipelineExe.getBindProject();
        String projectId = (String)bindProject.get(Dict.PROJECTID);
        //若用户为应用的负责人或该流水线的触发人，则可重试
        if(!vaildateUtil.projectCondition(projectId) && !pipelineExe.getUser().equals(user.getUser_name_en())){
            logger.error("**********user has not role to retry");
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }*/
        if (!Dict.ERROR.equals(pipelineExe.getStatus()) && !Dict.SUCCESS.equals(pipelineExe.getStatus()) && !Dict.CANCEL.equals(pipelineExe.getStatus())) {
            logger.error("********** pipelineExe status cannot retry , pipelineExeId: " + pipelineExe.getExeId() + ", status: " + pipelineExe.getStatus());
            throw new FdevException(ErrorConstants.PIPELINE_IS_RUNNING);
        }
    }

    @Override
    public List<Images> queryAllImages() throws Exception {
        Query query = new Query();
        User user = userService.getUserFromRedis();
        Criteria statusCriteria = Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN);
        query.addCriteria(statusCriteria);
        //非管理员用户
        if (!Dict.ADMIN.equals(user.getUser_name_en()) && !whiteList.contains(user.getUser_name_en())) {
//            Criteria groupsCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.GROUP);
            Criteria visibleRangeCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.PUBLIC);
            Criteria userCriteria = Criteria.where(Dict.AUTHOR__ID).is(user.getId());
//            String groupId = user.getGroup_id();
            //根据组id查询当前组和其所有子组
            //List<String> groupIds = appService.queryCurrentAndChildGroup(groupId);
//            List<String> groupIds = userService.getChildGroupIdsByGroupId(groupId);
//            if (!CommonUtils.isNullOrEmpty(groupIds)) {
//                groupsCriteria.and(Dict.GROUPID).in(groupIds);
//            }
            query.addCriteria(new Criteria().orOperator(/*groupsCriteria,*/visibleRangeCriteria, userCriteria));
        }
        return imageDao.queryImages(query);
    }

    @Override
    public Pipeline querySimpleObjectById(String pipelineId) {
        return pipelineDao.queryById(pipelineId);
    }

    @Override
    public Map<String, Object> queryPipelineByPluginCode(String pluginCode) throws Exception {
        //当没有使用该插件的记录时，可以删除插件，当存在使用该插件的记录时，返回记录数+第一条的数据
        Map<String, Object> resMap = new HashMap<>();
        List<Pipeline> pipelines = pipelineDao.queryByPluginId(pluginCode);

        if (pipelines.size() >= 1) {
            resMap.put(Dict.LIST, pipelines.get(0));
            resMap.put(Dict.TOTAL, pipelines.size());
        } else {
            //没有pipeline使用记录的时候可以删除插件
            logger.info("********* delete plugin;  pluginCode:" + pluginCode);
            pluginService.delPlugin(pluginCode);
            resMap.put(Dict.PLUGINCODE, pluginCode);
        }
        return resMap;
    }

    private List<Map> setVariables(String project_id, String exe_id, String branch, Boolean tagFlag, String userId) throws Exception {
        List<Map> variables = new ArrayList<>();
        Map<String, Object> project = appService.queryAppDetailById(project_id);
        if (CommonUtils.isNullOrEmpty(project)) {
            logger.error("*************fdev project not exist, projectId = " + project_id);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Map<String, Object> gitProject = appService.queryGitProjectDetail(project_id);
        if (CommonUtils.isNullOrEmpty(gitProject)) {
            logger.error("**************gitlab project not exist, projectId = " + project_id);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Map<String, String> namespace = (Map<String, String>) gitProject.get(Dict.NAMESPACE);
        String fullPath = namespace.get(Dict.FULL_PATH);
        setVariableMap(variables, Dict.CI_WORKSPACE, "/workspace/" + exe_id);
        setVariableMap(variables, Dict.CI_PROJECT_NAMESPACE, fullPath);
        setVariableMap(variables, Dict.CI_PROJECT_NAME, (String) project.get(Dict.NAME_EN));
        String gitUrl = (String) project.get(Dict.GIT);
        String[] split = gitUrl.split("http://");
        String projectUrl = "http://" + gitUserName + ":" + gitPassword + "@" + split[1];
        setVariableMap(variables, Dict.CI_PROJECT_URL, projectUrl);
        setVariableMap(variables, Dict.CI_PROJECT_BRANCH, branch);
        setVariableMap(variables, Dict.CI_COMMIT_REF_NAME, branch);
        StringBuilder dir = new StringBuilder("/workspace/" + exe_id);
        dir.append("/").append(fullPath).append("/").append(project.get(Dict.NAME_EN));
        setVariableMap(variables, Dict.CI_PROJECT_DIR, dir.toString());
        //增加CI_PROJECT_ID、CI_PIPELINE_NUMBER、CI_TAG_FLAG
        setVariableMap(variables, Dict.CI_PROJECT_ID, String.valueOf(gitProject.get(Dict.ID)));
        Long pipelineNum = mongoEventListener.getPipelineNum();
        setVariableMap(variables, Dict.CI_PIPELINE_NUMBER, String.valueOf(pipelineNum + 1));
        //是否为tag分支
        setVariableMap(variables, Dict.CI_TAG_FLAG, String.valueOf(tagFlag));
        //2021年6月11日 14:14:49  增加环境变量userId
        setVariableMap(variables, Dict.CI_USERID, userId);
        return variables;
    }

    private void setVariableMap(List<Map> variables, String key, Object value) {
        Map item = new HashMap();
        item.put(Dict.KEY, key);
        item.put(Dict.VALUE, value);
        variables.add(item);
    }

    private List setVolumes() throws Exception {
        String jsonStr = CommonUtils.readJsonFile("volumes.json");
        Map volumes = JSONObject.parseObject(jsonStr, Map.class);
        return (List) volumes.get(Dict.VOLUMES);
    }

    @Override
    public void webhookPipeline(Map<String, Object> param) throws Exception {
        logger.info("*******************WebhookPipeline info; param :" + JSONObject.toJSONString(param));
        Integer gitlabProjectId = (Integer) param.get(Dict.PROJECT_ID);
        String userName = (String) param.get(Dict.USER_USERNAME);
        //根据用户名去获取用户信息
        Author author = appService.queryUserByNameEn(userName);
        if (CommonUtils.isNullOrEmpty(author.getNameEn())) {
            author.setNameEn(userName);
        }
        String ref = (String) param.get(Dict.REF);
        String[] split = ref.split("/");
        String branch = split[split.length - 1];
        //根据gitlabProjectId 查询应用信息
        Map projectMap = appService.queryAppDetailByGitId(gitlabProjectId);
        if (CommonUtils.isNullOrEmpty(projectMap)) {
            logger.error("******** can not find this project: " + gitlabProjectId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        String applicationId = (String) projectMap.get(Dict.ID);
        List<Pipeline> pipelines = queryDetailByProjectId(applicationId);
        if (CommonUtils.isNullOrEmpty(pipelines)) {
            logger.info("*********WebhookPipeline, cannot find pipelines!, applicationId: " + applicationId);
            //throw new Exception("该应用没有配置fdev-ci流水线");
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{"该应用没有配置fdev-ci流水线"});
        }
        BindProject bindProject = pipelines.get(0).getBindProject();
        String projectId = bindProject.getProjectId();
        checkProjectFdevCI(projectId);

        //获取提交信息 可能有多条 获取最后一条
        String commitTitle = null;
        String commitId = null;
        if (!CommonUtils.isNullOrEmpty(param.get(Dict.COMMITS))) {
            List<Map> commits = (List<Map>) param.get(Dict.COMMITS);
            Map lastCommitMap = commits.get(commits.size() - 1);
            commitTitle = String.valueOf(lastCommitMap.get(Dict.TITLE));
            commitId = String.valueOf(lastCommitMap.get(Dict.ID));
        } else {
            logger.info("*********WebhookPipeline, 没有commit记录，不触发流水线: " + applicationId);
            return;
        }
        for (Pipeline pipeline : pipelines) {
            TriggerRules triggerRules = pipeline.getTriggerRules();
            List<String> branchs = new ArrayList<>();
            //List<PushParams> pushParams = triggerRules.getPush().getPushParams();
            List<Map> pushParams = (List<Map>) triggerRules.getPush().getPushParams();
            if (!CommonUtils.isNullOrEmpty(pushParams)) {
                for (Map params : pushParams) {
                    String branchName = (String) params.get("branchName");
                    branchs.add(branchName);
                }
            }
            //符合分支过滤规则
            if (isTrigger(branchs, branch)) {
                //通过gitlab用户名调用fuser模块查询用户的gitlab token
//                String userGitToken = userService.getUserGitToken(userName);
                //使用该token来查询该分支是否为tag分支
                List<String> tags = gitlabService.queryTags(gitlabProjectId, userGitToken);
                Boolean tagFlag = false;
                if (tags.contains(branch))
                    tagFlag = true;
                //新增tagFlag参数来标识当前跑pipeline的分支是否为tag
                logger.info("*******************WebhookPipeline  executePipeline info ; pipelineId为:" + pipeline.getId() + "；触发的分支为********" + branch);
                pipeline.setCommitTitle(commitTitle);
                pipeline.setCommitId(commitId);
                executePipeline(pipeline, Dict.PUSH, author, branch, gitlabProjectId, tagFlag);
            }
        }
    }

    /**
     * 校验项目是否设置了fdev-ci集成方式
     *
     * @param projectId 项目ID，不是gitlabID
     * @return
     * @throws Exception
     */
    private boolean checkProjectFdevCI(String projectId) throws Exception {
        Map<String, Object> project = appService.queryAppDetailById(projectId);
        if (CommonUtils.isNullOrEmpty(project)) {
            logger.error("******** can not find this project " + projectId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        String appCiType = (String) project.get(Dict.APPCITYPE);
        if (CommonUtils.isNullOrEmpty(appCiType) || !appCiType.equals(Dict.FDEV_CI)) {
            throw new FdevException(ErrorConstants.APP_NOT_CHOOSE_FDEV_CI);
        }
        return true;
    }

    @Override
    public String executePipeline(Pipeline pipeline, String type, Author user, String branch, Integer gitlabId, Boolean tagFlag) throws Exception{
        logger.info("当前线程为：" + Thread.currentThread().getName());
        String pipelineExeId = new ObjectId().toString();
        asyncBuildPipelineExePending(pipeline, type, user, branch, gitlabId, tagFlag, pipelineExeId);
        return pipelineExeId;
    }

    /**
     * 异步生成pending的pipelineExe
     * 生产一个空的pipelineExe模版给前端，后面继续用异步去构建pipelineExe和JobExe 填入完整的信息，并放入队列
     *
     * @param pipeline
     * @param type
     * @param user
     * @param branch
     * @param gitlabId
     * @param tagFlag
     * @param pipelineExeId
     */
    @Async
    public void asyncBuildPipelineExePending(Pipeline pipeline, String type, Author user, String branch, Integer gitlabId, Boolean tagFlag, String pipelineExeId) throws Exception {
        logger.info("当前线程为：" + Thread.currentThread().getName());
        logger.info(">>>>>>>>>>>>>>>>>>> 执行异步构建pending pipelineExe 方法 <<<<<<<<<<<<<<<<<<<<<<");
        BindProject bindProject = pipeline.getBindProject();
        PipelineExe pipelineExe = new PipelineExe();
        pipelineExe.setExeId(pipelineExeId);
        pipelineExe.setPipelineId(pipeline.getId());
        pipelineExe.setPipelineNameId(pipeline.getNameId());
        pipelineExe.setCommitTitle(pipeline.getCommitTitle());
        pipelineExe.setCommitId(pipeline.getCommitId());
        Map bindProjectMap = new HashMap();
        bindProjectMap.put(Dict.GITLABPROJECTID, gitlabId);
        bindProjectMap.put(Dict.PROJECTID, bindProject.getProjectId());
        bindProjectMap.put(Dict.NAMEEN, bindProject.getNameEn());
        bindProjectMap.put(Dict.NAMECN, bindProject.getNameCn());
        pipelineExe.setBindProject(bindProjectMap);
        pipelineExe.setUser(user);
        List<Map> variables = setVariables(bindProject.getProjectId(), pipelineExe.getExeId(), branch, tagFlag, user.getId());
        pipelineExe.setTriggerMode(type);//触发方式为手动/自动
        if (!CommonUtils.isNullOrEmpty(pipeline.getRunVariables()) && pipeline.getRunVariables().size() > 0) {
            variables.addAll(pipeline.getRunVariables());
        }
        pipelineExe.setVariables(variables);
        List volumesList = setVolumes();
        for (Object map : volumesList) {
            Map volumesMap = (Map) map;
            if (Objects.equals(volumesMap.get(Dict.NAME), Dict.FILE_UPLOAD)) {
                String host_path = volumesMap.get(Dict.HOST_PATH) + envUrl;
                volumesMap.put(Dict.HOST_PATH, host_path);
            }
        }
        pipelineExe.setVolumes(volumesList);
        pipelineExe.setBranch(branch);
        pipelineExe.setStatus(Dict.PENDING);
        List<Stage> stages = pipeline.getStages();
        Integer stage_index = 0;
        List stageExes = new ArrayList();
        for (Stage stage : stages) {
            List<Job> jobs = stage.getJobs();
            Integer job_index = 0;
            List jobIds = new ArrayList();
            for (Job job : jobs) {
                Map jobInfo = new HashMap();
                String runnerClusterId = job.getRunnerClusterId();
                if (CommonUtils.isNullOrEmpty(runnerClusterId)) {
                    //如果前端送的集群是空的，给集群默认值
                    runnerClusterId = Dict.RUNNERCLUSTERID_FDEV;
                }
                jobInfo.put(Dict.RUNNERCLUSTERID, runnerClusterId);
                //3 准备pipelineEXE中的job实例
                List jobExes = new ArrayList();
                Map jobExeInfo = new HashMap();
                jobExeInfo.put(Dict.JOBNUMBER, "");
                jobExeInfo.put(Dict.JOBEXEID, new ObjectId().toString());
                if (stage_index == 0) {
                    jobExeInfo.put(Dict.JOBEXESTATUS, Dict.PENDING);
                }else {
                    jobExeInfo.put(Dict.JOBEXESTATUS, Dict.WAITING);
                }
                jobExes.add(jobExeInfo);

                jobInfo.put(Dict.NAME, job.getName());
                jobInfo.put(Dict.JOBEXES, jobExes);
                List<Step> steps = job.getSteps();
                List<String> stepNames = new ArrayList<>();
                for (Step step : steps) {
                    stepNames.add(step.getName());
                }
                jobInfo.put(Dict.STEPS, stepNames);
                jobIds.add(jobInfo);
                job_index++;
            }

            Map stageExe = new HashMap();
            stageExe.put(Dict.NAME, stage.getName());
            stageExe.put(Dict.JOBS, jobIds);
            if (stage_index == 0) {
                stageExe.put(Dict.STATUS, Dict.PENDING);
            } else {
                stageExe.put(Dict.STATUS, Dict.WAITING);
            }
            stageExes.add(stageExe);
            stage_index++;
        }
        pipelineExe.setPipelineName(pipeline.getName());
        pipelineExe.setStartTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        pipelineExe.setStages(stageExes);
        logger.info("************* executePipeline will save pipelineExe, pipelineExe info:" + JSONObject.toJSON(pipelineExe));
        //将job实例列表加到pipeline实例列表中
        pipelineExeService.save(pipelineExe);
        //4 流水线表中设置构建时间,同时更新updateTime
        pipeline.setBuildTime(pipelineExe.getStartTime());
        pipeline.setUpdateTime(pipelineExe.getStartTime());
        pipelineDao.updateBuildTime(pipeline);


        logger.info(">>>>>>>>>>>>>>>>>>> 执行异步构建pending pipelineExe 方法完成 <<<<<<<<<<<<<<<<<<<<<<");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncBuildJobExe(pipeline, pipelineExeId, branch, gitlabId, user);
                } catch (Exception e) {
                    logger.error(">>>>>>>>>>>>>>>>>>>  执行 asyncBuildJobExe 失败! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                    logger.error(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 异步构建jobExe,填入完整的pipelineExe参数，以及填入jobExe参数 放入队列
     *
     * @param pipeline
     * @param pipelineExeId
     * @param branch
     * @param gitlabId
     * @param user
     * @throws Exception
     */
    @Async
    private void asyncBuildJobExe(Pipeline pipeline, String pipelineExeId, String branch, Integer gitlabId, Author user) throws Exception {
        logger.info("当前线程为：" + Thread.currentThread().getName());
        logger.info(">>>>>>>>>>>>>>>>>>> 执行异步构建pending jobExe 方法 <<<<<<<<<<<<<<<<<<<<<<");
        //需要放入队列的job列表
        List<Map> jobQueueList = new ArrayList();
        PipelineExe pipelineExe = this.pipelineExeDao.queryPipelineExeByExeId(pipelineExeId);
        List<Map<String, Object>> exeStages = pipelineExe.getStages();
        List<Stage> stages = pipeline.getStages();
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            List<Job> jobs = stage.getJobs();
            for (int j = 0; j < jobs.size(); j++) {
                Job job = jobs.get(j);
                //1创建jobExe
                JobExe jobExe = new JobExe();
                List<Map> exeJobs = (List<Map>) exeStages.get(i).get(Dict.JOBS);
                List<Map> jobExesListMap = (List<Map>) exeJobs.get(j).get(Dict.JOBEXES);
                String jobExeId = (String) jobExesListMap.get(jobExesListMap.size() - 1).get(Dict.JOBEXEID);
//                ObjectId id = new ObjectId();
                jobExe.setExeId(jobExeId);
                jobExe.setPipelineId(pipeline.getId());
                jobExe.setPipelineExeId(pipelineExeId);
                jobExe.setStageIndex(i);
                jobExe.setStageName(stage.getName());
                jobExe.setJobIndex(j);
                jobExe.setJobName(job.getName());
                Map imageMap = new HashMap();
                Images image = job.getImage();
                if(image != null && !CommonUtils.isNullOrEmpty(image.getId())){
                    Images jobImage = pipelineDao.findImageById(image.getId());
                    if (!CommonUtils.isNullOrEmpty(jobImage)) {
                        imageMap.put(Dict.PATH, jobImage.getPath());
                        imageMap.put(Dict.IMAGEID, jobImage.getId());
                        imageMap.put(Dict.NAME, jobImage.getName());
                    }
                }
                jobExe.setImage(imageMap);
                //准备steps
                jobExe.setSteps(prepareSteps(job,branch,gitlabId));
                jobExe.setUser(user);

                String runnerClusterId = job.getRunnerClusterId();
                if(CommonUtils.isNullOrEmpty(runnerClusterId)) {
                    //如果前端送的集群是空的，给集群默认值
                    runnerClusterId = Dict.RUNNERCLUSTERID_FDEV;
                }
                //将第一个stage中的job状态改为pending
                String jobStatus = Dict.WAITING;
                if(i == 0) {
                    jobStatus = Dict.PENDING;
                }
                jobExe.setStatus(jobStatus);
                //默认加默认的info参数
//                jobExe.setInfo();
                jobExeService.saveJobExe(jobExe);

                //将pending的jobExe放入队列
                if (jobExe.getStatus().equals(Dict.PENDING)) {
                    Map jobMap = new HashMap();
                    jobMap.put(Dict.JOB_EXE, jobExe);
                    jobMap.put(Dict.RUNNERCLUSTERID, runnerClusterId);
                    jobQueueList.add(jobMap);
                }

                //给pipelineExe中的JobExe更新
                List jobExes = new ArrayList();
                Map jobExeInfo = new HashMap();
                jobExeInfo.put(Dict.JOBNUMBER, jobExe.getJobNumber());
                jobExeInfo.put(Dict.JOBEXEID, jobExe.getExeId());
                jobExeInfo.put(Dict.JOBEXESTATUS, jobExe.getStatus());
                jobExes.add(jobExeInfo);
                exeJobs.get(j).put(Dict.JOBEXES, jobExes);
            }
        }

        logger.info("************* executePipeline will save pipelineExe, pipelineExe info:" + JSONObject.toJSON(pipelineExe));
        //将job实例列表加到pipeline实例列表中
        pipelineExeService.updateStagesAndStatus(pipelineExe);

        //5将jobexe放入队列
        for (Map jobMap : jobQueueList) {
            JobExe jobQueExe = (JobExe) jobMap.get(Dict.JOB_EXE);
            String runnerClusterId = (String) jobMap.get(Dict.RUNNERCLUSTERID);
            //根据集群区分
            addJobExeQueue(jobQueExe, runnerClusterId);
        }

        logger.info(">>>>>>>>>>>>>>>>>>> 执行异步构建asyncBuildJobExe 方法完成 <<<<<<<<<<<<<<<<<<<<<<");
    }

    /**
     * 准备jobexe中的steps
     *
     * @param job
     * @return
     * @throws Exception
     */
    private List<Map> prepareSteps(Job job, String branch, Integer gitlabId) throws Exception {
        List<Map> jobExeSteps = new ArrayList<>();
        //准备jobExe中的steps
        List<Step> steps = job.getSteps();
        for (Step step : steps) {
            Map jobExeStep = new HashMap();
            jobExeStep.put(Dict.STEPNAME, step.getName());
            //封装pluginInfo的信息
            PluginInfo pluginInfo = step.getPluginInfo();
            Map jobExePlugin = preparePluginInfo(pluginInfo);
            jobExeStep.put(Dict.PLUGININFO, jobExePlugin);
            //封装input
            List<Map> input = preparePluginInput(pluginInfo, branch, gitlabId);
            jobExeStep.put(Dict.INPUT, input);
            //jobExeStep.put(Dict.STEPSTARTTIME, CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
            //准备完成
            jobExeSteps.add(jobExeStep);
        }
        return jobExeSteps;
    }

    /**
     * 准备jobexe中的plugininfo
     *
     * @param pluginInfo
     * @return
     * @throws Exception
     */
    private Map preparePluginInfo(PluginInfo pluginInfo) throws Exception {
        String pluginCode = pluginInfo.getPluginCode();
        Map jobExePlugin = new HashMap();
        //组装plugininfo基础信息
        jobExePlugin.put(Dict.PLUGINCODE, pluginCode);
        jobExePlugin.put(Dict.NAME, pluginInfo.getName());
        jobExePlugin.put(Dict.DESC, pluginInfo.getDesc());
        //封装execution
        Plugin plugin = pluginService.queryPluginDetail(pluginCode);
        if (!CommonUtils.isNullOrEmpty(plugin)) {
            jobExePlugin.put(Dict.EXECUTION, plugin.getExecution());
        }
        //封装artifacts
        Map<String, Object> artifacts = pluginInfo.getArtifacts();
        if (!CommonUtils.isNullOrEmpty(artifacts)) {
            Object value = artifacts.get(Dict.VALUE);
            if (!CommonUtils.isNullOrEmpty(value)) {
                jobExePlugin.put(Dict.ARTIFACTS, value);
            }
        }
        return jobExePlugin;
    }

    /**
     * 组装该plugin运行时所需的input参数
     *
     * @param pluginInfo
     * @param branch
     * @param gitlabId
     * @return
     * @throws Exception
     */
    private List<Map> preparePluginInput(PluginInfo pluginInfo, String branch, Integer gitlabId) throws Exception {
        //封装input
        List<Map> input = new ArrayList<>();
        //放入分支提交的commits最近一次的短id
        String shortId = gitlabService.queryShortId(gitlabId, userGitToken, branch);
        Map gitLabUrlMap = inputValue(Dict.CI_API_V4_URL, gitLabApiUrl);
        input.add(gitLabUrlMap);
        Map shortIdMap = inputValue(Dict.CI_COMMIT_SHORT_SHA, shortId);
        input.add(shortIdMap);
        //input放入gitlab的token
        Map fdevToken = inputValue(Dict.CI_FDEV_TOKEN, gitlabToken);
        input.add(fdevToken);
        //放入人工审核待办插件的入参，默认false为 停止流水线，当调用断点续跑的时候会送true
        Map skipManualReview = inputValue(Dict.SKIP_MANUAL_REVIEW, Dict.PARAM_FALSE);
        input.add(skipManualReview);
        //input放入script  存量的数据去执行
        Map<String, String> script = pluginInfo.getScript();
        if (!CommonUtils.isNullOrEmpty(script)) {
            //放入minio信息
            Map inputScriptMap = inputValue(Dict.PACKAGE_PATH, script.get(Dict.MINIO_OBJECT_NAME));
            input.add(inputScriptMap);
            Map inputScriptMinioObjectName = inputValue(Dict.MINIO_OBJECT_NAME, script.get(Dict.MINIO_OBJECT_NAME));
            input.add(inputScriptMinioObjectName);
            List<Map> mapMinioAccessInfo = minioAccessInfo();
            input.addAll(mapMinioAccessInfo);
        }
        //存量的实体模板列表,已经废弃
        /*List<Map> entityTemplateParams = pluginInfo.getEntityTemplateParams();
        if (!CommonUtils.isNullOrEmpty(entityTemplateParams)){
            List<Map> entityTemplateInputInfo = entityTypeInputInfo(entityTemplateParams);
            input.addAll(entityTemplateInputInfo);
        }*/

        //新增为镜像构建这个插件
        Plugin plugin = this.pluginDao.queryPluginDetailById(pluginInfo.getPluginCode());
        //插件英文名
        String pluginNameEn = plugin.getPluginNameEn();
        //input放入八种类型的param
        List<Map<String, Object>> params = pluginInfo.getParams();
        if (!CommonUtils.isNullOrEmpty(params)) {
            for (Map<String, Object> paramMap : params) {
                //input、multipleInput、password	、select
                String type = (String) paramMap.get(Dict.TYPE);
                if (Dict.INPUT.equals(type) || Dict.MULTIPLEINPUT.equals(type)
                        || Dict.PASSWORD.equals(type) || Dict.SELECT.equals(type)|| Dict.HTTP_SELECT.equals(type) || Dict.FILEEDIT.equals(type) || Dict.FILEUPLOAD.equals(type)) {
                    Map inputParamMap = inputValue((String) paramMap.get(Dict.KEY), paramMap.get(Dict.VALUE));
                    input.add(inputParamMap);
                } else if (Dict.MULTIPLESELECT.equals(type)) {
                    //多选框
                    Map multipleParamMap = inputValue((String) paramMap.get(Dict.KEY), paramMap.get(Dict.VALUEARRAY));
                    input.add(multipleParamMap);
                } else if (Dict.ENTITYTYPE.equals(type)) {
                    //实体模板类型
                    if (!CommonUtils.isNullOrEmpty(paramMap.get(Dict.ENTITYTEMPLATEPARAMS))) {
                        List<Map> templateMaps = (List<Map>) paramMap.get(Dict.ENTITYTEMPLATEPARAMS);
                        List<Map> entityTemplateInputInfo = entityTypeInputInfo(templateMaps);
                        input.addAll(entityTemplateInputInfo);
                        if (!CommonUtils.isNullOrEmpty(pluginNameEn)) {
                            if (envPluginMap.containsKey(pluginNameEn)) {
                                List<String> envParamKeys = envPluginMap.get(pluginNameEn);
                                for (int i = 0; i < templateMaps.size(); i++) {
                                    Map templateMap = templateMaps.get(i);
                                    String paramKey = (String) templateMap.get(Dict.NAMEEN);
                                    if (envParamKeys.contains(paramKey)) {
                                        Map innerEnv = (Map) templateMap.get(Dict.ENV);
                                        //获取环境放入
                                        if (!CommonUtils.isNullOrEmpty(innerEnv)) {
                                            String envNameEn = (String) innerEnv.get(Dict.NAMEEN);
                                            Map imageEnvMap = inputValue(Dict.IMAGE_ENV,envNameEn);
                                            input.add(imageEnvMap);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        //将插件中的构建参数放入到input中
        List<Map> addParams = pluginInfo.getAddParams();
        if (!CommonUtils.isNullOrEmpty(addParams)) {
            for (Map addParam : addParams) {
                if (Dict.CUSTOMVARIABLETYPE.equals(addParam.get(Dict.TYPE))) {
                    Object customParams = addParam.get(Dict.CUSTOMPARAMS);
                    if (!CommonUtils.isNullOrEmpty(customParams)) {
                        Map customParamsMap = (Map) customParams;
                        Map customVariableTypeMap = inputValue((String) customParamsMap.get(Dict.KEY), customParamsMap.get(Dict.VALUE));
                        input.add(customVariableTypeMap);
                    }
                } else if (Dict.ENTITYTEMPLATETYPE.equals(addParam.get(Dict.TYPE))) {
                    List<Map> templateMaps = new ArrayList<>();
                    templateMaps.add(addParam);
                    List<Map> entityTemplateMap = entityTypeInputInfo(templateMaps);
                    input.addAll(entityTemplateMap);
                } else if (Dict.ENTITYTYPE.equals(addParam.get(Dict.TYPE))) {
                    List<Map> oldTemplateMaps = new ArrayList<>();
                    oldTemplateMaps.add(addParam);
                    List<Map> oldEntityTemplateMap = entityTypeInputInfo(oldTemplateMaps);
                    input.addAll(oldEntityTemplateMap);
                }
            }
        }
        return input;
    }


    /**
     * 封装连接桶信息
     *
     * @return
     */
    public List<Map> minioAccessInfo() {
        List<Map> input = new ArrayList<>();
        String[] splitInfo = minioEndPoint.split("//");
        String endPoint = splitInfo[1];
        Map minioUrlMap = inputValue(Dict.MINIO_URL, endPoint);
        input.add(minioUrlMap);
        Map minioBucketMap = inputValue(Dict.MINIO_BUCKET, mioBuket);
        input.add(minioBucketMap);
        Map minioAccessMap = inputValue(Dict.MINIO_ACCESS_KEY, minioAccessKey);
        input.add(minioAccessMap);
        Map minioScretMap = inputValue(Dict.MINIO_SECRET_KEY, minioSecretKey);
        input.add(minioScretMap);
        return input;
    }

    /**
     * 实体模板信息放入input
     *
     * @param templateMaps
     * @return
     */
    public List<Map> entityTypeInputInfo(List<Map> templateMaps) throws Exception {
        List<Map> input = new ArrayList<>();
        for (Map templateMap : templateMaps) {
            if (!CommonUtils.isNullOrEmpty(templateMap.get(Dict.ENTITY)) && !CommonUtils.isNullOrEmpty(templateMap.get(Dict.ENV))) {
                Map entity = (Map) templateMap.get(Dict.ENTITY);
                Map env = (Map) templateMap.get(Dict.ENV);
                String[] entityIds = new String[1];
                entityIds[0] = (String) entity.get(Dict.ID);
                String envName = (String) env.get(Dict.NAMEEN);
                if (!CommonUtils.isNullOrEmpty(entityIds) && !CommonUtils.isNullOrEmpty(envName)) {
                    /*List<Map> variablesList = appService.queryEntityVariables(entityName, envName);
                    if (!CommonUtils.isNullOrEmpty(variablesList)){
                        for (Map variables : variablesList) {
                            Map<String, String> inputEntityEnvMap = new HashMap<>();
                            inputEntityEnvMap.put(Dict.KEY, String.valueOf(variables.get(Dict.NAME_EN)));
                            inputEntityEnvMap.put(Dict.VALUE, String.valueOf(variables.get(Dict.VALUE)));
                            input.add(inputEntityEnvMap);
                        }
                    }*/
                    Map resultMap = appService.queryEntityMapping(entityIds, envName);
                    if (!CommonUtils.isNullOrEmpty(resultMap)) {
                        for (Object key : resultMap.keySet()) {
                            Map inputEntityEnvMap = new HashMap<>();
                            inputEntityEnvMap.put(Dict.KEY, key);
                            inputEntityEnvMap.put(Dict.VALUE, resultMap.get(key));
                            input.add(inputEntityEnvMap);
                        }
                    }

                }
            } else {
                if (!CommonUtils.isNullOrEmpty(templateMap.get(Dict.ENTITYPARAMS))) {
                    List<Map> map = (List<Map>) templateMap.get(Dict.ENTITYPARAMS);
                    for (Map paramMap : map) {
                        Map inputTemplateEntityParamMap = inputValue((String) paramMap.get(Dict.KEY), paramMap.get(Dict.VALUE));
                        input.add(inputTemplateEntityParamMap);
                    }
                }
            }
        }
        return input;
    }

    public Map inputValue(String firstValue, Object secondValue) {
        Map map = new HashMap<>();
        map.put(Dict.KEY, firstValue);
        map.put(Dict.VALUE, secondValue);
        return map;
    }

    private boolean isTrigger(List<String> branch, String ref) {
        //判断当前分支是否符合触发规则\
        if (!CommonUtils.isNullOrEmpty(branch)) {
            for (String reg : branch) {
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(ref);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addJobExeQueue(JobExe jobExe, String runnerClusterId) throws Exception {
        ByteArrayOutputStream bio = ObjectUtil.object2Bytes(jobExe);
        logger.info("**************** save redis data :" + bio.toByteArray());
        redisTemplate.opsForList().rightPush(Constants.JOB_EXE_QUEUE_REDIS_KEY_PROFIX + runnerClusterId, bio.toByteArray());
    }


    @Override
    public List<Pipeline> queryDetailByProjectId(String id) throws Exception {
        return pipelineDao.queryDetailByProjectId(id);
    }

    /**
     * 单个job重试
     *
     * @param pipelineExeId
     * @param stageIndex
     * @param jobIndex
     * @throws Exception
     */
    @Override
    public void retryPipeline(String pipelineExeId, Integer stageIndex, Integer jobIndex) throws Exception {
        PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(pipelineExeId);
        String pipelineId = pipelineExe.getPipelineId();
        Pipeline pipeline = this.pipelineDao.queryById(pipelineId);
        User user = userService.getUserFromRedis();
        if (!vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId())
                && !pipelineExe.getUser().equals(user.getUser_name_en())) {
            logger.error("**********user has not permission to retry");
            throw new FdevException(ErrorConstants.RETRY_FAILED, new String[]{"current user illegal permission"});
        }
        checkRetryStatus(pipelineExe);
        //单个job重试，要求前面阶段都成功
        List<Map<String, Object>> stagesList = pipelineExe.getStages();
        if (stageIndex > 0) {
            for (int i = 0; i < stageIndex; i++) {
                String stageStatus = (String) stagesList.get(i).get(Dict.STATUS);
                if (!Dict.SUCCESS.equals(stageStatus)) {
                    logger.error("*********** previous stage：" + i + " status：" + stageStatus + "，current stage：" + stageIndex
                            + " cannot retry, pipelineExeId: " + pipelineExeId);
                    //throw new Exception("前面的阶段有未成功的任务，无法重试该任务");
                    throw new FdevException(ErrorConstants.PREVIOUS_TASK_FAIL);
                }
            }
        }
        Author author = userService.getAuthor();
        JobExe orgJobExe = jobExeDao.queryJobExeByIndex(pipelineExeId, stageIndex, jobIndex);
        //1 添加新的jobexe
        JobExe jobExe = new JobExe();
        Long number = jobExe.getJobNumber();
        ObjectId objId = new ObjectId();
        BeanUtils.copyProperties(orgJobExe, jobExe);
        jobExe.setJobNumber(number);
        jobExe.setExeId(objId.toString());
        jobExe.setUser(author);
        jobExe.setStatus(Dict.PENDING);
        jobExe.setJobStartTime("");
        jobExe.setJobCostTime("");
        jobExe.setJobEndTime("");
        jobExe.setMinioLogUrl("");
        jobExe.setInfo(null);
        jobExe.setToken("");
        //准备jobExe中的steps
        List<Map> jobExeSteps = jobExe.getSteps();
        for (Map jobExeStep : jobExeSteps) {
            jobExeStep.remove(Dict.OUTPUT);
            jobExeStep.remove(Dict.STEPSTARTTIME);
            jobExeStep.remove(Dict.STEPENDTIME);
            jobExeStep.remove(Dict.STEPCOSTTIME);
            jobExeStep.remove(Dict.STATUS);
        }

        jobExeService.saveJobExe(jobExe);

        //2 修改pipelineExe jobs中的jobExes新增一个jobExe
        Map<String, Object> stage = pipelineExe.getStages().get(stageIndex);
        stage.put(Dict.STATUS, Dict.PENDING);
        List<Map> stageJobs = (List<Map>) stage.get(Dict.JOBS);
        List<Map> jobExes = (List<Map>) stageJobs.get(jobIndex).get(Dict.JOBEXES);
        //添加stage-jobs-jobexes中的job实例
        String jobNumber = String.valueOf(jobExe.getJobNumber());
        Map exeMap = new HashMap();
        exeMap.put(Dict.JOBEXEID, objId.toString());
        exeMap.put(Dict.JOBNUMBER, jobNumber);
        exeMap.put(Dict.JOBEXESTATUS, Dict.PENDING);
        jobExes.add(exeMap);
        pipelineExe.setStatus(Dict.PENDING);
        logger.info("***************** retryPipeline will update pipelineExe; pipelineExe info:" + JSONObject.toJSON(pipelineExe));
        pipelineExeService.updateStagesAndStatus(pipelineExe);

        //3 同步更新流水线表中构建时间，同时更新修改时间
        pipeline.setBuildTime(pipelineExe.getStartTime());
        pipeline.setUpdateTime(pipelineExe.getStartTime());
        pipelineDao.updateBuildTime(pipeline);

        //4 只把pending的放入队列
        Job job = pipeline.getStages().get(stageIndex).getJobs().get(jobIndex);
        String runnerClusterId = job.getRunnerClusterId();
        addJobExeQueue(jobExe, runnerClusterId);
    }

    /**
     * 流水线断点续跑，从失败的第一个任务开始往下执行，供其他模块调用，后台接口。
     *
     * @param request
     * @throws Exception
     */
    @Override
    public void continueRunPipeline(Map request) throws Exception {
        String pipelineExeId = (String) request.get(Dict.PIPELINEEXEID);
        String pipelineNumber = (String) request.get(Dict.PIPELINENUMBER);
        if (CommonUtils.isNullOrEmpty(pipelineExeId) && CommonUtils.isNullOrEmpty(pipelineNumber)) {
            logger.error("****** continueRunPipeline pipelineExeId and pipelineNumber cannot all be empty!");
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_BOTH_EMPTY, new String[]{"pipelineExeId and pipelineNumber"});
        }
        //1、查找流水线实例，判断是否状态结束，没有结束则报错。
        PipelineExe pipelineExe = pipelineExeService.queryPipelineExeByExeId(pipelineExeId);
        if (pipelineExe == null) {
            logger.error("******pipelineExe not exist, pipelineExeId: " + pipelineExeId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        checkRetryStatus(pipelineExe);
        String pipelineId = pipelineExe.getPipelineId();
        Pipeline pipeline = this.pipelineDao.queryById(pipelineId);
        //2、获取第一个失败的stage，将里面失败的job以及后续的job放进待jobexe中，如果没有则报错。
        logger.info("*********** continueRunPipeline， pipelineExeId：" + pipelineExeId);
        List pendingJobs = new ArrayList();
        List<Map<String, Object>> stages = pipelineExe.getStages();
        Author author = pipelineExe.getUser();
        boolean firstStage = true;
        for (int i = 0; i < stages.size(); i++) {
            Map<String, Object> stage = stages.get(i);
            String stageStatus = (String) stage.get(Dict.STATUS);
            if (Dict.SUCCESS.equals(stageStatus)) {
                continue;
            }
            //只有第一个stage是pending
            String jobStatus = Dict.WAITING;
            if (firstStage) {
                firstStage = false;
                jobStatus = Dict.PENDING;
            }
            stage.put(Dict.STATUS, jobStatus);
            Stage pipStage = pipeline.getStages().get(i);
            List<Map> jobs = (List<Map>) stage.get(Dict.JOBS);

            for (int j = 0; j < jobs.size(); j++) {
                Map job = jobs.get(j);
                List<Map> stageJobExes = (List<Map>) job.get(Dict.JOBEXES);
                //获取最后一个jobexeMap
                Map map = stageJobExes.get(stageJobExes.size() - 1);
                String jobExeId = (String) map.get(Dict.JOBEXEID);
                //添加新的jobexe
                JobExe jobExe = new JobExe();
                Long number = jobExe.getJobNumber();
                ObjectId objId = new ObjectId();
                JobExe orgJobExe = jobExeService.queryJobExeByExeId(jobExeId);
                if (firstStage && orgJobExe.getStatus().equals(Dict.SUCCESS)) {
                    //如果是第一个失败的阶段，已经成功的job不重跑，失败的job才重跑，后面的stage全部job重跑
                    continue;
                }
                BeanUtils.copyProperties(orgJobExe, jobExe);
                jobExe.setJobNumber(number);
                jobExe.setExeId(objId.toString());
                jobExe.setUser(author);
                jobExe.setStatus(jobStatus);
                jobExe.setJobStartTime("");
                jobExe.setJobCostTime("");
                jobExe.setJobEndTime("");
                jobExe.setMinioLogUrl("");
                jobExe.setInfo(null);
                jobExe.setToken("");
                //准备jobExe中的steps
                List<Map> jobExeSteps = jobExe.getSteps();
                for (Map jobExeStep : jobExeSteps) {
                    jobExeStep.remove(Dict.OUTPUT);
                    jobExeStep.remove(Dict.STEPSTARTTIME);
                    jobExeStep.remove(Dict.STEPENDTIME);
                    jobExeStep.remove(Dict.STEPCOSTTIME);
                    jobExeStep.remove(Dict.STATUS);
                }

                for (Map step : jobExe.getSteps()) {
                    List<Map> input = (List<Map>) step.get(Dict.INPUT);
                    for (Map inputMap : input) {
                        String skipKey = (String) inputMap.get(Dict.KEY);
                        if (Dict.SKIP_MANUAL_REVIEW.equals(skipKey)) {
                            logger.info(" current stuck point running key :" + skipKey + " ,value:" + inputMap.get(Dict.VALUE));
                            inputMap.put(Dict.VALUE, Dict.PARAM_TRUE);
                        }
                    }
//                    Map paramMap = new HashMap();
//                    paramMap.put(Dict.KEY, "SKIP_MANUAL_REVIEW");
//                    paramMap.put(Dict.VALUE, "true");
//                    input.add(paramMap);
                }
                jobExeService.saveJobExe(jobExe);
                if (Dict.PENDING.equals(jobStatus)) {
                    Job pipJob = pipStage.getJobs().get(j);
                    Map jobMap = new HashMap();
                    jobMap.put(Dict.JOB_EXE, jobExe);
                    jobMap.put(Dict.RUNNERCLUSTERID, pipJob.getRunnerClusterId());
                    pendingJobs.add(jobMap);
                }

                //添加stage-jobs-jobexes中的job实例
                String jobNumber = String.valueOf(jobExe.getJobNumber());
                Map exeMap = new HashMap();
                exeMap.put(Dict.JOBEXEID, objId.toString());
                exeMap.put(Dict.JOBNUMBER, jobNumber);
                exeMap.put(Dict.JOBEXESTATUS, jobStatus);
                stageJobExes.add(exeMap);
            }

        }
        //更新pipelineExe
        pipelineExe.setStatus(Dict.PENDING);
        //pipelineExe.setStartTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        pipelineExe.setCostTime("");
        pipelineExe.setEndTime("");
        //pipelineExe.setUser(author);
        logger.info("********** continueRunPipeline will update the pipelineExe info:" + JSONObject.toJSON(pipelineExe));
        pipelineExeService.updateStagesAndStatusAndUser(pipelineExe);
        //更新pipeline构建时间和修改时间
        pipeline.setBuildTime(pipelineExe.getStartTime());
        pipeline.setUpdateTime(pipelineExe.getStartTime());
        pipelineDao.updateBuildTime(pipeline);
        for (Map pendingJob : (List<Map>) pendingJobs) {
            //只把pending的放入队列
            JobExe jobExe = (JobExe) pendingJob.get(Dict.JOB_EXE);
            String runnerClusterId = (String) pendingJob.get(Dict.RUNNERCLUSTERID);
            addJobExeQueue(jobExe, runnerClusterId);
        }

    }

    @Override
    public Map<String, Object> queryPipelineHistory(String nameId, String pageNum, String pageSize) throws Exception {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        return pipelineDao.findHistoryPipelineList(skip, limit, nameId);
    }

    /**
     * 封装详细的流水线信息
     *
     * @param pipeline
     * @return
     * @throws Exception
     */
    public Pipeline preparePipelineDetailInfo(Pipeline pipeline) throws Exception {
        if (CommonUtils.isNullOrEmpty(pipeline))
            return null;
        //2. 当前用户能否编辑
        boolean isAppManager = vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId());
        pipeline.setUpdateRight(isAppManager);
        //3、获取最新image信息
        for (Stage stage : pipeline.getStages()) {
            for (Job job : stage.getJobs()) {
                //替换image
                Images image = job.getImage();
                if (image != null && !CommonUtils.isNullOrEmpty(image.getId())) {
                    image = pipelineDao.findImageById(image.getId());
                    job.setImage(image);
                }
                for (Step step : job.getSteps()) {
                    PluginInfo pluginInfo = step.getPluginInfo();
                    String scriptCmd = null;
                    Map<String, String> script = pluginInfo.getScript();
                    if (!CommonUtils.isNullOrEmpty(script)) {
                        String mioPath = script.get(Dict.MINIO_OBJECT_NAME);
                        if (!CommonUtils.isNullOrEmpty(mioPath)) {
                            scriptCmd = fileService.downloadDocumentFile(mioBuket, mioPath);
                        }
                    }
                    pluginInfo.setScriptCmd(scriptCmd);
                    prePareResultShowPluginInfo(pluginInfo);
                }
            }
        }
        //4、是否收藏
        String userId = userService.getUserFromRedis().getId();
        if (!CommonUtils.isNullOrEmpty(pipeline.getCollected())) {
            if (pipeline.getCollected().contains(userId)) {
                pipeline.setCollectedOrNot(true);
            } else {
                pipeline.setCollectedOrNot(false);
            }
        } else {
            pipeline.setCollectedOrNot(false);
        }
        return pipeline;
    }

    /**
     * 查询流水线和流水线模版返回插件信息
     *
     * @param pluginInfo
     * @return
     * @throws Exception
     */
    @Override
    public PluginInfo prePareResultShowPluginInfo(PluginInfo pluginInfo) throws Exception {
        Plugin plugin = pluginDao.queryPluginDetail(pluginInfo.getPluginCode());
        //这里查询的plugin  默认存量script和实体模版参数已经放入params中
        if (plugin == null) {
            logger.error("**********plugin not exist, pluginId=" + pluginInfo.getPluginCode());
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"plugin not exist"});
        }
        List<Map<String, Object>> params = pluginInfo.getParams();
        //存量流水线的实体模版列表展示,看还有没有这样的数据，没有的话可以删掉这段代码
        List<Map> inputEntityTempParamList = pluginInfo.getEntityTemplateParams();
        if (!CommonUtils.isNullOrEmpty(inputEntityTempParamList)) {
            for (Map map : inputEntityTempParamList) {
                Map entityTemplateParam = new HashMap();
                entityTemplateParam.put(Dict.KEY, map.get(Dict.NAMEEN));
                entityTemplateParam.put(Dict.VALUE, null);
                entityTemplateParam.put(Dict.TYPE, Dict.ENTITYTYPE);
                //实体模板类型  只有一个实体模板
                List<Map> entityTemplateParamList = new ArrayList<>();
                entityTemplateParamList.add(map);
                entityTemplateParam.put(Dict.ENTITYTEMPLATEPARAMS, entityTemplateParamList);
                params.add(entityTemplateParam);
            }
        }

        //获取label hint default等值封装
        if (!CommonUtils.isNullOrEmpty(params)) {
            List<Map<String, Object>> resultParam = prepareShowParam(params, plugin);
            pluginInfo.setParams(resultParam);
        }
        pluginInfo.setEntityTemplateParams(null);
        //添加插件是否有效判断
        Boolean validFlag = Constants.STATUS_OPEN.equals(plugin.getStatus()) ? true : false;
        pluginInfo.setValidFlag(validFlag);
        return pluginInfo;
    }

    @Override
    public Images findImageById(String id) {
        Images images = pipelineDao.findImageById(id);
        //不需要返回作者的信息
        if (!CommonUtils.isNullOrEmpty(images)) {
            images.setAuthor(null);
        }
        return images;
    }

    /**
     * 获取回显的 param值
     *
     * @param params
     * @param plugin
     * @return
     */
    public List<Map<String, Object>> prepareShowParam(List<Map<String, Object>> params, Plugin plugin) {
        //数据库插件的param
        List<Map<String, Object>> pluginParams = plugin.getParams();
        if (CommonUtils.isNullOrEmpty(pluginParams)) {
            params = null;
        } else {
            //带存量数据的param中key可能为null
            for (Map resultMap : params) {
                if (!CommonUtils.isNullOrEmpty(resultMap.get(Dict.KEY))) {
                    //与plugin插件中key值匹配
                    for (Map pluginMap : pluginParams) {
                        if (String.valueOf(resultMap.get(Dict.KEY)).equals(String.valueOf(pluginMap.get(Dict.KEY)))) {
                            pluginMap.remove(Dict.VALUE);
                            pluginMap.remove(Dict.VALUEARRAY);
                            pluginMap.remove(Dict.ENTITYTEMPLATEPARAMS);
                            resultMap.putAll(pluginMap);
                            //获取hidden label hint default defaultArr
                            /*resultMap.put(Dict.HIDDEN,pluginMap.get(Dict.HIDDEN));
                            resultMap.put(Dict.LABEL,pluginMap.get(Dict.LABEL));
                            resultMap.put(Dict.HINT,pluginMap.get(Dict.HINT));
                            resultMap.put(Dict.DEFAULT,pluginMap.get(Dict.DEFAULT));
                            resultMap.put(Dict.DEFAULTARR,pluginMap.get(Dict.DEFAULTARR));
                            resultMap.put(Dict.REQUIRED,pluginMap.get(Dict.REQUIRED));
                            resultMap.put(Dict.ITEMVALUE,pluginMap.get(Dict.ITEMVALUE));*/
                            break;
                        }
                    }
                }
            }
        }
        return params;
    }

    @Override
    public String copy(String id) throws Exception {
        Pipeline pipeline = pipelineDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(pipeline)) {
            logger.error("**************The pipeline not exist***************");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId" + id});
        }
        //入库的pipeline
        Pipeline newPipeline = CommonUtils.map2Object(CommonUtils.obj2Map(pipeline), Pipeline.class);
//        BeanUtils.copyProperties(pipeline,newPipeline);
        newPipeline.setName(newPipeline.getName() + Dict._COPY);
        Author author = userService.getAuthor();
        newPipeline.setAuthor(author);
        //复制收藏
        List<String> user = new ArrayList<>();
        String userId = author.getId();
        if (!CommonUtils.isNullOrEmpty(newPipeline.getCollected())) {
            if (newPipeline.getCollected().contains(userId)) {
                user.add(userId);
            }
        }
        newPipeline.setNameId(null);
        newPipeline.setId(null);
        newPipeline.setCollected(user);
        newPipeline.setVersion(Constants.UP_CHANGE_VERSION);
        iPluginService.copyYamlConfigInStages(newPipeline.getStages());
        String pipelineId = pipelineDao.add(newPipeline);
        updateUseCountWhenAdd(pipelineId);
        return pipelineId;
    }

    /**
     * 停止pipeline
     *
     * @param pipelineExeId
     * @throws Exception
     */
    @Override
    public PipelineExe stopPipeline(String pipelineExeId, Boolean skipManualReview) throws Exception {
        PipelineExe pipelineExe = this.pipelineExeDao.queryPipelineExeByExeId(pipelineExeId);
        //1 校验是否可以停止
        if (CommonUtils.isNullOrEmpty(skipManualReview) || !skipManualReview)
            checkCancelCondition(userService.getUserFromRedis(), pipelineExe);
        logger.info("**********begin to stopPipeline, pipelineExeId: " + pipelineExeId + ", status: " + pipelineExe.getStatus());
        List<Map<String, Object>> stages = pipelineExe.getStages();
        for (int i = 0; i < stages.size(); i++) {
            Map<String, Object> stage = stages.get(i);
            List<Map> jobs = (List<Map>) stage.get(Dict.JOBS);
            for (Map job : jobs) {
                List<Map> jobExes = (List<Map>) job.get(Dict.JOBEXES);
                Map jobExeMap = jobExes.get(jobExes.size() - 1);
                String jobExeId = (String) jobExeMap.get(Dict.JOBEXEID);
                String jobExeStatus = (String) jobExeMap.get(Dict.JOBEXESTATUS);
                JobExe jobExe = this.jobExeDao.queryJobExeByExeId(jobExeId);
                if (Dict.RUNNING.equals(jobExeStatus) || Dict.PENDING.equals(jobExeStatus)) {
                    //2 更新jobexe
                    //如果job还没开始，没有开始时间
                    if (Dict.PENDING.equals(jobExe.getStatus())) {
                        jobExe.setJobStartTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
                    }
                    jobExe.setJobEndTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
                    jobExe.setJobCostTime(CommonUtils.getCostDate(jobExe.getJobStartTime(), jobExe.getJobEndTime(), CommonUtils.STANDARDDATEPATTERN));
                    jobExe.setStatus(Dict.CANCEL);
                    jobExe.setMinioLogUrl("");

                    //3 更新pipelineExe的running或者pending的jobExeMap
                    jobExeMap.put(Dict.JOBEXESTATUS, jobExe.getStatus());
                    jobExeMap.put(Dict.JOBSTARTTIME, jobExe.getJobStartTime());
                    jobExeMap.put(Dict.JOBENDTIME, jobExe.getJobEndTime());
                    jobExeMap.put(Dict.JOBCOSTTIME, jobExe.getJobCostTime());
                    stage.put(Dict.STATUS, Dict.CANCEL);
                }
                // 更新后续stage为cancel
                if (Dict.WAITING.equals(jobExeStatus)) {
                    jobExe.setStatus(Dict.CANCEL);
                    jobExe.setMinioLogUrl("");
                    jobExeMap.put(Dict.JOBEXESTATUS, jobExe.getStatus());
                    stage.put(Dict.STATUS, Dict.CANCEL);
                }
                this.jobExeDao.updateJobFinish(jobExe);
            }
        }

        //更新pipelineExe的状态为cancel
        pipelineExe.setStatus(Dict.CANCEL);
        pipelineExe.setStages(stages);
        pipelineExe.setEndTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        pipelineExe.setCostTime(pipelineExeService.calculatePipelineExeCostTime(pipelineExe));
        pipelineExeDao.updateStagesAndStatus(pipelineExe);
        return pipelineExe;
    }

    /**
     * 校验是否可以停止/取消的前提条件，流水线必须是成功/失败，且用户是应用负责人/构建人。
     *
     * @param user        登录用户
     * @param pipelineExe
     * @throws Exception 校验失败会报错，校验通过返回true
     */
    private void checkCancelCondition(User user, PipelineExe pipelineExe) throws Exception {
        Map bindProject = pipelineExe.getBindProject();
        String projectId = (String) bindProject.get(Dict.PROJECTID);
        //若用户为应用的负责人或该流水线的触发人，则可停止
        if (!vaildateUtil.projectCondition(projectId) && !pipelineExe.getUser().equals(user.getUser_name_en())) {
            logger.error("**********user has not role to cancel");
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        if (!Dict.RUNNING.equals(pipelineExe.getStatus()) && !Dict.PENDING.equals(pipelineExe.getStatus())) {
            logger.error("********** pipelineExe status cannot cancel , pipelineExeId: " + pipelineExe.getExeId() + ", status: " + pipelineExe.getStatus());
            throw new FdevException(ErrorConstants.PIPELINE_IS_FINISHED);
        }
    }

    /**
     * 更新jobExe 为停止状态，仅更新，后面这个jobExe也还有可能走job/request、plugin/request、plugin/input、plugin/output、artifacts/webhook、artifacts/request  内都一定对stop状态的判断逻辑
     *
     * @param pipelineExeId
     * @param stageIndex
     * @param jobIndex
     * @throws Exception
     */
    @Override
    public JobExe stopJob(String pipelineExeId, Integer stageIndex, Integer jobIndex) throws Exception {
        PipelineExe pipelineExe = this.pipelineExeDao.queryPipelineExeByExeId(pipelineExeId);
        //1 校验是否可以停止
        checkCancelCondition(userService.getUserFromRedis(), pipelineExe);
        //2 更新jobExe的状态
        JobExe jobExe = this.jobExeDao.queryJobExeByIndex(pipelineExeId, stageIndex, jobIndex);
        if (!Dict.RUNNING.equals(jobExe.getStatus()) && !Dict.PENDING.equals(jobExe.getStatus())) {
            logger.error("********** jobExe status cannot cancel , jobExeID: " + jobExe.getExeId() + ", status: " + pipelineExe.getStatus());
            throw new FdevException(ErrorConstants.JOB_CANNOT_CANCEL);
        }
        logger.info("**********begin to stopJob, jobExeId: " + jobExe.getExeId() + ", status: " + jobExe.getStatus());
        //如果job还没开始，没有开始时间
        if (Dict.PENDING.equals(jobExe.getStatus())) {
            jobExe.setJobStartTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        }
        jobExe.setJobEndTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        jobExe.setJobCostTime(CommonUtils.getCostDate(jobExe.getJobStartTime(), jobExe.getJobEndTime(), CommonUtils.STANDARDDATEPATTERN));
        jobExe.setStatus(Dict.CANCEL);
        jobExe.setMinioLogUrl("");
        this.jobExeDao.updateJobFinish(jobExe);
        //3 更新pipelineExe的jobExe状态
        List<Map<String, Object>> stages = pipelineExe.getStages();
        Map<String, Object> stageMap = stages.get(stageIndex);
        List<Map> jobs = (List<Map>) stageMap.get(Dict.JOBS);
        Map job = jobs.get(jobIndex);
        List<Map> jobExes = (List<Map>) job.get(Dict.JOBEXES);
        Map jobExeMap = jobExes.get(jobExes.size() - 1);
        jobExeMap.put(Dict.JOBEXESTATUS, jobExe.getStatus());
        jobExeMap.put(Dict.JOBSTARTTIME, jobExe.getJobStartTime());
        jobExeMap.put(Dict.JOBENDTIME, jobExe.getJobEndTime());
        jobExeMap.put(Dict.JOBCOSTTIME, jobExe.getJobCostTime());
        //4 判断如果当前stage已经完成，更新stage和pipeline，如果没有完成不更新。
        String stageStatus = pipelineExeService.getStageFinalStatus(pipelineExe, stageIndex);
        if (Dict.CANCEL.equals(stageStatus)) {
            stageMap.put(Dict.STATUS, stageStatus);
            pipelineExe.setStatus(stageStatus);
        }
        // 更新后续stage
        for (int i = stageIndex + 1; i < stages.size(); i++) {
            Map<String, Object> stage = stages.get(i);
            List<Map> jobsAfter = (List<Map>) stage.get(Dict.JOBS);
            for (Map jobAfter : jobsAfter) {
                List<Map> jobExesSkip = (List<Map>) jobAfter.get(Dict.JOBEXES);
                Map jobExeMapAfter = jobExesSkip.get(jobExesSkip.size() - 1);
                String jobExeId = (String) jobExeMapAfter.get(Dict.JOBEXEID);
                String jobExeStatus = (String) jobExeMapAfter.get(Dict.JOBEXESTATUS);
                if (Dict.WAITING.equals(jobExeStatus)) {
                    //2 更新jobexe
                    JobExe jobExeSkip = this.jobExeDao.queryJobExeByExeId(jobExeId);
                    jobExeSkip.setStatus(Dict.SKIP);
                    jobExeSkip.setMinioLogUrl("");
                    this.jobExeDao.updateJobFinish(jobExeSkip);

                    //3 更新pipelineExe的running或者pending的jobExeMap
                    jobExeMapAfter.put(Dict.JOBEXESTATUS, jobExeSkip.getStatus());
                }
            }
            stage.put(Dict.STATUS, Dict.SKIP);
        }
        pipelineExe.setStages(stages);
        this.pipelineExeDao.updateStagesAndStatus(pipelineExe);
        return jobExe;
    }

    @Override
    public List<Pipeline> getSchedulePipelines() {
        return this.pipelineDao.querySchedulePipelines();
    }

    @Override
    public Map queryTriggerRules(String id) throws Exception {
        Pipeline pipeline = pipelineDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(pipeline)) {
            logger.error("**************The pipeline not exist***************");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId" + id});
        }
        TriggerRules triggerRules = pipeline.getTriggerRules();
        if (CommonUtils.isNullOrEmpty(triggerRules)) {
            triggerRules = new TriggerRules();
            Push push = new Push();
            push.setSwitchFlag(false);
            triggerRules.setPush(push);
            Schedule schedule = new Schedule();
            schedule.setSwitchFlag(false);
            triggerRules.setPush(push);
            triggerRules.setSchedule(schedule);
        }
        //对于没有schedule/push字段,方便前端展示
        if (!CommonUtils.isNullOrEmpty(triggerRules)) {
            if (CommonUtils.isNullOrEmpty(triggerRules.getSchedule())) {
                Schedule schedule = new Schedule();
                schedule.setSwitchFlag(false);
                triggerRules.setSchedule(schedule);
            }
            if (CommonUtils.isNullOrEmpty(triggerRules.getPush())) {
                Push push = new Push();
                push.setSwitchFlag(false);
                triggerRules.setPush(push);
            }
        }
        Map resultNap = new HashMap<>();
        resultNap.put(Dict.TRIGGERRULES, triggerRules);
        return resultNap;
    }

    @Override
    public String updateTriggerRules(String pipelineId, TriggerRules triggerRules) throws Exception {
        Pipeline pipeline = pipelineDao.queryById(pipelineId);
        if (CommonUtils.isNullOrEmpty(pipeline)) {
            logger.error("**************The pipeline not exist***************");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId" + pipelineId});
        }
        boolean isAppManager = vaildateUtil.projectCondition(pipeline.getBindProject().getProjectId());
        if (!isAppManager) {
            throw new FdevException(ErrorConstants.USER_NOT_APPMANAGER);
        }
        String result = pipelineDao.updateTriggerRules(pipelineId, triggerRules);
        return result;
    }


    /**
     * 不校验用户信息，根据id和nameId查询流水线
     *
     * @param request
     * @return
     */
    @Override
    public Pipeline queryByNameId(Map request) {
        //1. 查询库中的pipeline
        Pipeline pipeline = pipelineDao.queryPipelineByIdOrNameId(request);
        return pipeline;
    }

    /**
     * 下载制品
     *
     * @param
     * @return
     */
    @Override
    public void downLoadArtifacts(String name, HttpServletResponse response) throws Exception {
        User currentLoginUser = null;
        try {
            currentLoginUser = this.userService.getUserFromRedis();
        } catch (Exception e) {
            logger.info(" 当前交易不带有用户信息 " + e.getMessage());
        }
        if (currentLoginUser != null) {
            /*List<String> lineIds = this.userService.getLineIdsByGroupId(currentLoginUser.getGroup_id());
            //获取用户的条线id
            if (CommonUtils.isNullOrEmpty(lineIds)) {
                logger.info(" current user lineId is null, groupId:" + currentLoginUser.getGroup_id());
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{currentLoginUser.getGroup_id()});
            }*/
            String exeId = "";
            String[] strings = name.split("/");
            for (String string : strings) {
                if (!string.contains("-")) {
                    if (!string.contains(".")) {
                        exeId = string;
                        break;
                    }
                }
            }
            if (CommonUtils.isNullOrEmpty(exeId)) {
                logger.info(" exeId is null, exeId:" + exeId);
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{exeId});
            }
        }
        InputStream in = null;
        ServletOutputStream out = null;
        String path = name;
        try {
            String input = username + ":" + password;
            if (path.indexOf("/") == 0) {
                path = path.substring(1, path.length());
            }
            URL url = new URL("http://arti.spdb.com:80/artifactory/" + path);
            // URL url = new URL( "http://arti.spdb.com:80/artifactory/fdevtest-generic-local/golang-plugin-cmd.md");
            URLConnection connection = url.openConnection();
            BASE64Encoder base = new BASE64Encoder();
            String encode = base.encode(input.getBytes("UTF-8"));
            connection.setRequestProperty("Authorization", "Basic " + encode);
            in = connection.getInputStream();
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            String filename = path.substring(path.lastIndexOf("/") + 1);
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            out = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024 * 9];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"下载失败"});
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    public List getPipelineByEntityId(Map request) {
        return pipelineDao.queryByEntityId(request);
    }

    @Override
    public List<Map> getPipelineHistoryVersion(Map request) {
        String pipelineId = (String) request.get(Dict.PIPELINEID);
        if (!CommonUtils.isNullOrEmpty(pipelineId)) {
            //查询出来pipeline的历史版本
            Pipeline pipeline = this.pipelineDao.queryById(pipelineId);
            String nameId = pipeline.getNameId();
            List<Pipeline> pipelines = this.pipelineDao.queryPipelinesByNameId(nameId);
            List resultList = new ArrayList();
            for (Pipeline resultPip : pipelines) {
                //过滤了正常状态的流水线
                if (Constants.ONE.equals(resultPip.getStatus())) {
                    continue;
                }
                Map resultMap = CommonUtils.obj2Map(resultPip);
                //当前流水线的pipelineId
                String sourceId = resultPip.getId();
                //根据目标流水线id查询diff信息，目标id可以作为唯一标识
                PipelineUpdateDiff diffEntity = pipelineUpdateDiffDao.queryDiffBySourceId(sourceId);

                resultMap.put("diffEntity", diffEntity);
                resultList.add(resultMap);
            }
            return resultList;
        } else {
            throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"id is null!!"});
        }
    }

    /**
     * 校验dataGroupId 是否在用户User的组或子组中，如果是admin，返回true
     *
     * @param dataGroupId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean checkGroupidInUserGroup(String dataGroupId) throws Exception {
        User user = userService.getUserFromRedis();
        if (pipelineTemplateDao.checkAdminRole(user.getUser_name_en()) || user.getRole_id().contains(groupRoleAdminId)) {
            return true;
        } /*else {
            List<String> groupIds = userService.getChildGroupIdsByGroupId(user.getGroup_id());
            if (!CommonUtils.isNullOrEmpty(dataGroupId)) {
                if (user.getRole_id().contains(groupRoleAdminId) && groupIds.contains(dataGroupId)) {
                    return true;
                }
            }
        }*/
        return false;
    }


    /**
     * 回退流水线版本
     *
     * @param request
     * @return
     */
    @Override
    public String setPipelineRollBack(Map request) throws Exception {
        String id = (String) request.get(Dict.PIPELINEID);
        Pipeline pipeline = this.pipelineDao.queryById(id);
        if (Constants.ONE.equals(pipeline.getStatus())) {
            logger.error(" current pipeline status is using(1)，can't rollback ");
            throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{" current pipeline status is using(1)，can't rollback "});
        }
        String nameId = pipeline.getNameId();
        Pipeline currentUsingPip = this.pipelineDao.findActiveVersion(nameId);
        if (CommonUtils.isNullOrEmpty(currentUsingPip)) {
            logger.error(" current using pipeline status is using(1)，can't rollback ");
            throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{" current using pipeline status is using(1)，can't rollback "});
        }
        //升版本
        pipeline.setId(new ObjectId().toString());
        String version = currentUsingPip.getVersion();
        Integer versionInt = Integer.valueOf(version) + 1;
        pipeline.setVersion(String.valueOf(versionInt));

        User user = userService.getUserFromRedis();
        Author author = new Author();
        author.setId(user.getId());
        author.setNameCn(user.getUser_name_cn());
        author.setNameEn(user.getUser_name_en());

        pipeline.setAuthor(author);
        pipeline.setUpdateTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));

        //把原来的状态置0
        this.pipelineDao.updateStatusClose(currentUsingPip.getId(), user);
        //新增一条
        String addedId = this.pipelineDao.add(pipeline);
        //保存diff
        PipelineUpdateDiff diffEntity = DiffUtils.getHandlerPipelineDiff(currentUsingPip, pipeline);
        this.pipelineUpdateDiffDao.saveDiff(diffEntity);
        return addedId;
    }

    /**
     * 定时停止pipeline
     *
     * @param request
     * @throws Exception
     */
    @Override
    public void cronStopPipeline(Map request) throws Exception {
        List<PipelineExe> runningJobExes = this.pipelineExeDao.findRunningJobPipeline();
        for (PipelineExe pipelineExe : runningJobExes) {
            for (Map<String, Object> stage : pipelineExe.getStages()) {
                List<Map> jobs = (List<Map>) stage.get(Dict.JOBS);
                for (Map job : jobs) {
                    List<Map> jobExes = (List<Map>) job.get(Dict.JOBEXES);
                    Map jobExe = jobExes.get(jobExes.size() - 1);
                    String status = (String) jobExe.get(Dict.JOBEXESTATUS);
                    if (Dict.RUNNING.equals(status)) {
                        String jobStartTime = (String) jobExe.get(Dict.JOBSTARTTIME);
                        if (CommonUtils.calcTimesTarCurrent(jobStartTime, CommonUtils.STANDARDDATEPATTERN)) {
                            logger.info(" 当前需要停止的流水线为 stop pipeline exe : " + pipelineExe.getExeId());
                            //running的时候手动调用停止接口
                            this.stopPipeline(pipelineExe.getExeId(), true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void countPipDigital() throws Exception {
        List<Map> pipelines = this.pipelineDao.queryPipLookDigital();
        for (Map pipeline : pipelines) {
            List<Map> pipelineExes = (List) pipeline.get("exeInfo");
            if (pipelineExes.size() == 0)
                continue;
            digitalService.calculateDigital((String) pipeline.get(Dict.NAMEID));
        }
    }
}

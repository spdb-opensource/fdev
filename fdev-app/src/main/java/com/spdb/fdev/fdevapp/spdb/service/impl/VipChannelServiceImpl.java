package com.spdb.fdev.fdevapp.spdb.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.dao.IVipChannelDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IKafkaService;
import com.spdb.fdev.fdevapp.spdb.service.IVipChannelService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
@RefreshScope
public class VipChannelServiceImpl implements IVipChannelService {

    private static Logger logger = LoggerFactory.getLogger(VipChannelServiceImpl.class);// 控制台日志打印


    @Autowired
    private IVipChannelDao IVipChannelDao;

    @Autowired
    private IGitlabAPIService gitlabAPIService;

    @Autowired
    private IAppEntityDao appEntityDao;

    @Autowired
    private IKafkaService kafkaService;

    @Value("${gitlib.path}")
    private String gitlabUrl;

    @Value("${minio.accessKey}")
    private String accessKey;       //Minio

    @Value("${minio.secretKey}")
    private String secretKey;       //JCJG1234!

    private static String jsonString;

    static {
        jsonString = CommonUtils.getTemplateJson("kafka-template/VipChannel.json");
    }


    /**
     * 检验解析获取得到channel的信息的参数
     *
     * @param params
     */
    private void checkAnalysisInfoParams(Map params) {
        if (CommonUtils.isNullOrEmpty(params.get(Dict.ID)))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用id"});
        if (CommonUtils.isNullOrEmpty(params.get(Dict.REF)))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"分支ref"});
    }

    /**
     * 通过应用id来获取得到fdev应用的信息
     *
     * @param params
     */
    private void getAppInfoById(Map params) throws Exception {
        AppEntity appEntity = this.appEntityDao.findById((String) params.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(appEntity))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"按id来查找应用信息不存在"});
        Map app = new HashMap();
        app.put(Dict.APP_NAME, appEntity.getName_en());
        app.put(Dict.APP_ID, params.get(Dict.ID));
        app.put(Dict.PROJECT_URL, appEntity.getGit());
        app.put(Dict.PROJECT_GITLAB_ID, String.valueOf(appEntity.getGitlab_project_id()));
        params.put(Dict.APP, app);
    }

    /**
     * 创建一个TriggerTime时间戳
     *
     * @return
     */
    private Long createTriggerTime() {
        Date date = new Date();
        return date.getTime();
    }

    /**
     * 组装发送kafka消息
     *
     * @param variables 前端加入的环境变量
     * @param image     yaml的image镜像信息
     * @param timestamp 用来作为主键查询记录
     * @param stageName 用来作为当前执行的job标识
     */
    public void sendKafka(Map variables, String image, Long timestamp, String stageName) {
        AppVipChannel queryBean = new AppVipChannel();
        queryBean.setTrigger_time(timestamp);
        //查询当前的记录
        AppVipChannel appVipChannel = this.getVipChannel(queryBean).get(0);
        String projectId = (String) appVipChannel.getApp().get(Dict.PROJECT_GITLAB_ID);
        //发送gitlab接口获取得到namespace和name_with_namespace
        Map namespaceMap = this.gitlabAPIService.getChannelNamespace(projectId);
        String namespace = (String) namespaceMap.get(Dict.NAMESPACE);
        //String name_with_namespace = (String) namespaceMap.get(Dict.NAME_WITH_NAMESPACE);
        String projectName = (String) appVipChannel.getApp().get(Dict.APP_NAME);
        String projectUrl = (String) appVipChannel.getApp().get(Dict.PROJECT_URL);
        Map commit = appVipChannel.getCommit();
        //获取最后一次commit的短id
        String short_id = (String) commit.get(Dict.SHORT_ID);
        //分支，作为环境变量发给kafka
        String projectBranch = appVipChannel.getRef();
        long jobStartTime = new Date().getTime();       //job的开始时间
        List command = null;
        String buildState = null;
        //记录更新的job在jobs的下标
        List<Map<String, Object>> jobs = appVipChannel.getJobs();
        Boolean isExistCreated = false;
        for (int i = 0; i < jobs.size(); i++) {
            //传进来的stageName和stage相同，且status为created的时候
            if (stageName.equals(jobs.get(i).get(Dict.STAGES)) && Dict.CREATED.equals(jobs.get(i).get(Dict.STATUS))) {
                command = (List) jobs.get(i).get(Dict.COMMAND);
                buildState = (String) jobs.get(i).get(Dict.STAGES);
                isExistCreated = true;
                break;
            }
        }
        //如果不存在有created状态的job，说明发送有误，直接return
        if (!isExistCreated)
            return;
        String commandStr = "";
        for (String com : (List<String>) command) {
            commandStr = commandStr + " && " + com;
        }
        commandStr = commandStr.substring(4).replace("\"", "\\\"");
        logger.info("Command:" + commandStr);
        String kafkaData = jsonString.replace(Constant.VC_NAME, projectName + "-" + buildState)
                .replace(Constant.VC_TIMESTAMP, String.valueOf(timestamp))
                .replace(Constant.VC_COMMAND, commandStr)
                .replace(Constant.VC_IMAGE, image)
                .replace(Constant.VC_PROJECT_NAME, projectName)
                .replace(Constant.VC_PROJECT_BRANCH, projectBranch)
                .replace(Constant.VC_PROJECT_URL, projectUrl)
                .replace(Constant.VC_BUILD_STATE, buildState)
                .replace(Constant.VC_PROJECTS_NAMESPACE, namespace)
                .replace(Constant.VC_PROJECT_ID, projectId);
        //给metadata添加环境变量variables
        Map parse = (Map) JSONObject.parse(kafkaData);
        Map metadata = (Map) parse.get(Constant.VC_METADATA);
        if (!CommonUtils.isNullOrEmpty(variables))
            metadata.putAll(variables);
        //将job的开始时间装进去
        metadata.put(Constant.VC_TRIGGER_TIME, timestamp);
        metadata.put(Constant.CI_COMMIT_REF_NAME, projectBranch);
        //pro开头的需传这个字段，其他不传
        if (projectBranch.toLowerCase().startsWith("pro"))
            metadata.put(Constant.CI_COMMIT_TAG, projectBranch);
        //metadata.put(Constant.CI_PROJECT_DIR, name_with_namespace);
        metadata.put(Constant.CI_PIPELINE_ID, 0);
        metadata.put(Constant.CI_API_V4_URL, gitlabUrl);
        metadata.put(Constant.CI_COMMIT_SHORT_SHA, short_id);
        //更新当前job的开始时间
        this.IVipChannelDao.updateJobTime(jobStartTime, null, stageName, timestamp);
        //更新当前job的状态从created->pending
        this.IVipChannelDao.updateJobStatus(Dict.PENDING, timestamp, stageName);
        //发送kafka消息
        this.kafkaService.sendData((String) appVipChannel.getTriggerer().get(Dict.USER_NAME_EN), projectName, parse.toString(), "");
    }

    /**
     * 更新单个job的开始或结束时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param stage     标识那一个job的stage
     * @param timeStamp 标识那一条channel记录
     */
    @Override
    public void updateJobTime(Long startTime, Long endTime, String stage, Long timeStamp) {
        this.IVipChannelDao.updateJobTime(startTime, endTime, stage, timeStamp);
    }

    /**
     * 取消当前跑pipeline的操作
     *
     * @param id
     * @return
     */
    @Override
    public AppVipChannel cancelPipelines(String id) {
        return this.IVipChannelDao.updateStatusForCancel(id);
    }

    /**
     * 分页获取channel
     *
     * @param params
     * @return
     */
    @Override
    public Map getChannelListByPagination(Map params) {
        return this.IVipChannelDao.queryChannelListByPagination(params);
    }

    /**
     * 根据条件查询获取得到minio_url对应的log，并获取log的content返回
     *
     * @param id
     * @param stage
     * @return
     */
    @Override
    public Map getChannelLogContent(String id, String stage) {
        //获取得到minio_url的数据
        String minioUrl = this.IVipChannelDao.queryMinioUrl(id, stage);
        Map result = new HashMap();
        String content = "Loading....";
        if (!CommonUtils.isNullOrEmpty(minioUrl))
            content = analysisMinio(minioUrl);
        //获取当前pipeline的status和job的status
        AppVipChannel queryEntity = new AppVipChannel();
        queryEntity.setId(id);
        List<AppVipChannel> appVipChannels = this.IVipChannelDao.queryVipChannelByParams(queryEntity);
        AppVipChannel vipChannel = appVipChannels.get(0);
        String pipeline_status = vipChannel.getStatus();
        result.put(Dict.PIPELINE_STATUS, pipeline_status);
        //获取当前stage的job的status
        List<Map<String, Object>> jobs = vipChannel.getJobs();
        for (Map job : jobs) {
            if (!CommonUtils.isNullOrEmpty(job.get(Dict.STAGES)) && job.get(Dict.STAGES).equals(stage)) {
                String status = (String) job.get(Dict.STATUS);
                result.put(Dict.STATUS, status);
                //如果当前状态为created，修改返回的content内容
                if (status.equals(Dict.CREATED))
                    content = "当前job还未被触发,需在上一个job成功后才可被启动！";
                else if (status.equals(Dict.SKIPPED))
                    content = "当前job已被跳过！";
                else if (status.equals(Dict.CANCELED))
                    content = "当前job已被取消！";
                break;
            }
        }
        result.put(Dict.TRACE, content);
        return result;
    }

    @Override
    public List<AppVipChannel> getPipelineByTowStatus(String status1, String status2) {
        return this.IVipChannelDao.queryPipelineByTowStatus(status1, status2);
    }

    @Override
    public void updateJobMinioUrl(String minioUrl, Long timestamp, String stage) {
        this.IVipChannelDao.updateMinioUrl(minioUrl, timestamp, stage);
    }

    @Override
    public Boolean checkedStatus(String status, Long timestamp, String stage) {
        return this.IVipChannelDao.checkedStatus(status, timestamp, stage);
    }

    @Override
    public AppVipChannel getPipelineById(String id) {
        return this.IVipChannelDao.queryPipelineById(id);
    }

    /**
     * 解析minio，获取content
     *
     * @param url
     * @return
     */
    private String analysisMinio(String url) {
        //[xxx:9000, minio, super-runner-test, 2020-09-18, super-runner-mspmk-web-fund-parent-1594893564951.log]
        String[] urlArr = url.split("/");
        String fileName = "";
        //fileName = urlArr[urlArr.length - 1];
        for (int i = 3; i < urlArr.length; i++) {
            fileName = fileName + "/" + urlArr[i];
        }
        fileName = fileName.substring(1);
        InputStream inputStream = null;
        try {
            MinioClient minioClient = MinioClient.builder().endpoint("http://" + urlArr[0]).credentials(accessKey, secretKey).build();
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(urlArr[2]).object(fileName).build());
            byte[] buf = new byte[1024 * 1000 * 5];
            int byteRead;
            String content = "";
            while ((byteRead = inputStream.read(buf, 0, buf.length)) >= 0) {
                content = content + new String(buf, 0, byteRead, StandardCharsets.UTF_8);
            }
            inputStream.close();
            return content;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"解析vipChannel日志" + fileName + "出错，" + e.getMessage()});
        }
    }


    /**
     * 获取得到jobs信息，通过直接从应用的持续集成文件获取
     */
    private Map getJobsByReadFile(Map params) throws Exception {
        Map app = (Map) params.get(Dict.APP);
        String projectId = (String) app.get(Dict.PROJECT_GITLAB_ID);
        String filePath = ".gitlab-ci.yml";
        String content = this.gitlabAPIService.getContent(projectId, filePath, (String) params.get(Dict.REF));
        Yaml yaml = new Yaml();
        Map<String, Object> yamlContent = yaml.loadAs(content, Map.class);
        //kafka的环境参数
        Map variables = (Map) yamlContent.get(Dict.VARIABLES);
        //修改没有variables的时候报错
        if (CommonUtils.isNullOrEmpty(variables)) {
            variables = new HashMap();
        }
        //将请求中的variables放入kafka的环境参数中
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.VARIABLES))) {
            //解析将list的variables转换成map
            Map reqVariables = analysisListToMap((List) params.get(Dict.VARIABLES));
            variables.putAll(reqVariables);
            params.put(Dict.VARIABLES, variables);
        }
        //获取遍历stages
        List<String> stages = (List) yamlContent.get(Dict.STAGES);
        //获取全局的image，当job的内容没有image用这个
        String image = (String) yamlContent.get(Dict.IMAGE);
        //存入数据的库的jobs
        List jobs = new ArrayList();
        for (int i = 0; i < stages.size(); i++) {
            for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> stageMap = (Map<String, Object>) entry.getValue();
                    String stageName = (String) stageMap.get(Dict.STAGE);
                    if (stageName == null) {
                        continue;
                    }
                    if (stageName.equals(stages.get(i))) {
                        Map<String, Object> job = new HashMap();
                        job.put(Dict.STAGES, stageName);
                        job.put(Dict.PRIORITY, i + 1);
                        job.put(Dict.STATUS, Dict.CREATED);
                        job.put(Dict.MINIO_URL, "");
                        job.put(Dict.COMMAND, stageMap.get(Dict.SCRIPT));
                        //加入单个job的开始触发时间和完成时间
                        job.put(Dict.TRIGGER_TIME, Long.valueOf(0L));
                        job.put(Dict.FINISHED_AT, Long.valueOf(0L));
                        //当image存在的时候，用自己的image，不存在的时候，用全局的image
                        if (CommonUtils.isNullOrEmpty(stageMap.get(Dict.IMAGE)))
                            job.put(Dict.IMAGE, image);
                        else
                            job.put(Dict.IMAGE, stageMap.get(Dict.IMAGE));
                        jobs.add(job);
                        break;
                    }
                }
            }
        }
        params.put(Dict.JOBS, jobs);
        params.put(Dict.DURATION, Long.valueOf(0L));
        params.put(Dict.FINISHED_AT, Long.valueOf(0L));
        //删除ID，前端传进来的应用id
        params.remove(Dict.ID);
        //保存信息进数据库
        this.IVipChannelDao.saveVipChannel(CommonUtils.map2Object(params, AppVipChannel.class));
        params.put(Dict.IMAGE, image);
        return params;
    }

    /**
     * 解析variables  list变成map
     *
     * @param variables
     * @return
     */
    private Map analysisListToMap(List variables) {
        if (CommonUtils.isNullOrEmpty(variables))
            return null;
        Map variablesMap = new HashMap();
        for (Map variable : (List<Map>) variables) {
            //获取前端传过来的key
            String key = (String) variable.get(Dict.KEY);
            //获取前端传过来的value
            String value = (String) variable.get(Dict.VALUE);
            //若值为数字，则转为数字
            if (value.matches("[0-9]+"))
                variablesMap.put(key, Integer.valueOf(value));
            else variablesMap.put(key, value);
        }
        return variablesMap;
    }

    /**
     * 获取当前triggerer用户，并存取相应的信息
     *
     * @param params
     */
    private void getTriggererInfo(Map params) {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if (null == user)
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        Map triggerer = new HashMap();
        triggerer.put(Dict.USER_NAME_CN, user.getUser_name_cn());
        triggerer.put(Dict.TRIGGERERID, user.getId());
        params.put(Dict.TRIGGERER, triggerer);
    }

    /**
     * 解析获取得到channel的信息
     *
     * @param request
     */
    @Override
    public void analysisInfo(Map request) throws Exception {
        //检验属性是否有不存在的
        this.checkAnalysisInfoParams(request);
        //获得app信息
        this.getAppInfoById(request);
        //创建时间戳triggerTime
        Long triggerTime = this.createTriggerTime();
        request.put(Dict.TRIGGER_TIME, triggerTime);
        //获取当前triggerer的fdev用户信息
        this.getTriggererInfo(request);
        //获取commit的最后一次信息
        Map app = (Map) request.get(Dict.APP);
        Map commitInfo = this.gitlabAPIService.getChannelCommitInfo((String) app.get(Dict.PROJECT_GITLAB_ID), (String) request.get(Dict.REF));
        request.put(Dict.COMMIT, commitInfo);
        //设置status为pending
        request.put(Dict.STATUS, Dict.PENDING);
        //读取文件获取jobs
        Map entity = this.getJobsByReadFile(request);
        //用来作为当前记录的唯一标识查询，时间戳
        Long timestamp = (Long) entity.get(Dict.TRIGGER_TIME);
        String firstJob = "";
        List jobs = (List) entity.get(Dict.JOBS);
        for (Map job : (List<Map>) jobs) {
            Integer priority = (Integer) job.get(Dict.PRIORITY);
            if (priority == 1)
                firstJob = (String) job.get(Dict.STAGES);
        }
        //发送kafka
        this.sendKafka((Map) entity.get(Dict.VARIABLES), (String) entity.get(Dict.IMAGE), timestamp, firstJob);
    }

    /**
     * 根据timestamp唯一标识来查询channel记录，或者根据其他条件来查询记录
     *
     * @param appVipChannel
     * @return
     */
    @Override
    public List<AppVipChannel> getVipChannel(AppVipChannel appVipChannel) {
        return this.IVipChannelDao.queryVipChannelByParams(appVipChannel);
    }

    /**
     * 更新channel记录中job的状态，以id作为标识
     *
     * @param status
     * @param timestamp
     */
    @Override
    public void updateJobStatus(String status, Long timestamp, String stage) {
        this.IVipChannelDao.updateJobStatus(status, timestamp, stage);
    }

    /**
     * 更新整条vipChannel记录的status
     *
     * @param status
     * @param timestamp
     */
    @Override
    public void updateStatus(String status, Long timestamp) {
        this.IVipChannelDao.updateStatus(status, timestamp);
    }
}

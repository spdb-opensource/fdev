package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IReleaseApplicationDao;
import com.spdb.fdev.release.dao.IReleaseBatchDao;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 投产应用业务层 2019年3月26日
 */
@Service
public class ReleaseApplicationServiceImpl implements IReleaseApplicationService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseApplicationServiceImpl.class);

    @Value("${scripts.path}")
    String scripts_path;

    @Value("${prod.record.minio.url}")
    String recordAssetsUrl;

    @Value("${aws.bucketName}")
    String bucketName;

    @Autowired
    private IAppService appService;
    @Autowired
    private IGitlabService gitlabService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ITaskService taskService;
    @Autowired
    private IFileService fileService;
    @Autowired
	private IReleaseTaskDao releaseTaskDao;

    @Autowired
    private IReleaseApplicationDao releaseApplicationDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IAsyncService asyncService;
    @Autowired
    private IRelDevopsRecordService relDevopsRecordService;
    @Autowired
    private ITestRunService testRunService;
    @Autowired
    private IProdApplicationDao prodApplicationDao;
    @Autowired
    private IReleaseBatchDao iReleaseBatchDao;
    @Autowired
    private IComponenService componenService;

    @Override
    public List<ReleaseNode> queryReleaseNodesByAppId(String application_id,String node_status) throws Exception {
        return releaseApplicationDao.queryReleaseNodesByAppId(application_id,node_status);
    }

    @Override
    public void deleteApplication(String application_id, String release_node_name) throws Exception {
         releaseApplicationDao.deleteApplication(application_id, release_node_name);
    }

    @Override
    public List<Map<String, Object>> queryApplications(String release_node_name) throws Exception {
        List<Map<String, Object>> listResult = new ArrayList<>();
        List<ReleaseApplication> applist = releaseApplicationDao.queryApplications(release_node_name);
        // 条件查询获取应用列表
        //若应用的merge_requst_id存在,且devops状态为1-已发起，待合并
        asyncService.updateApplicationMergeState(applist);
        for (ReleaseApplication app : applist) {// 遍历应用列表
            Map<String, Object> result = new HashMap<>();
            // 发app模块获取应用名称
            Map<String, Object> application = appService.queryAPPbyid(app.getApplication_id());
            result.put(Dict.GIT, application.get(Dict.GIT));
            result.put(Dict.SYSTEM, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.SYSTEM));
            result.put(Dict.NETWORK,
            		CommonUtils.isNullOrEmpty(application)?"":application.get(Dict.NETWORK));
            result.put(Dict.LABEL,
                    CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.LABEL));
            result.put(Dict.APP_NAME_EN,
                    CommonUtils.isNullOrEmpty(application)?"":application.get(Dict.NAME_EN));
            result.put(Dict.APP_NAME_ZH,
                    CommonUtils.isNullOrEmpty(application)?"":application.get(Dict.NAME_ZH));
            // 应用类型
            result.put(Dict.TYPE_NAME,
                    CommonUtils.isNullOrEmpty(application)?"":application.get(Dict.TYPE_NAME));
            // 是否新增应用
            result.put(Dict.NEW_ADD_SIGN,
                    CommonUtils.isNullOrEmpty(application)?"":application.get(Dict.NEW_ADD_SIGN));
            List<Map<String, String>> appSpdbManager = userService.queryAppSpdbManager(app.getApplication_id());
            result.put(Dict.APP_SPDB_MANAGERS,appSpdbManager);
            List<Map<String, String>> appDevManager = userService.queryAppDevManager(app.getApplication_id());
            result.put(Dict.APP_DEV_MANAGERS,appDevManager);
            RelDevopsRecord relDevopsRecord =new RelDevopsRecord();
            relDevopsRecord.setApplication_id(app.getApplication_id());
            relDevopsRecord.setRelease_node_name(app.getRelease_node_name());
            //查询投产应用投产记录信息
            List<String> tagList = relDevopsRecordService.queryTagList(relDevopsRecord);
            Map<String,Object> uriMap = relDevopsRecordService.queryImageList(relDevopsRecord);
            result.put(Dict.PRO_IMAGE_URI, uriMap.get(Dict.CAAS));
            result.put(Dict.PRO_SCC_IMAGE_URI, uriMap.get(Dict.SCC));
            result.put(Dict.APPLICATION_ID, app.getApplication_id());
            result.put(Dict.RELEASE_NODE_NAME, app.getRelease_node_name());
            result.put(Dict.RELEASE_BRANCH, app.getRelease_branch());
            result.put(Dict.FAKE_FLAG, app.getFake_flag());
            result.put(Dict.FAKE_IMAGE_NAME, app.getFake_image_name());
            result.put(Dict.FAKE_IMAGE_VERSION, app.getFake_image_version());
            result.put(Dict.PRODUCT_TAG, tagList);
            String uatEnvName = app.getUat_env_name();
            result.put(Dict.UAT_ENV_NAME, CommonUtils.isNullOrEmpty(uatEnvName)?"":uatEnvName);
            String relEnvName = app.getRel_env_name();
            result.put(Dict.REL_ENV_NAME, CommonUtils.isNullOrEmpty(relEnvName)?"":relEnvName);

            // 配置文件变化与审核状态
            if(!CommonUtils.isNullOrEmpty(app.isFdev_config_changed())) {
                result.put(Dict.FDEV_CONFIG_CONFIRM, app.getFdev_config_confirm());
                result.put(Dict.FDEV_CONFIG_CHANGED, app.isFdev_config_changed());
                result.put(Dict.COMPARE_URL, app.getCompare_url());
                result.put(Dict.LAST_RELEASE_TAG, app.getLast_release_tag());
                result.put(Dict.DEVOPS_TAG, app.getDevops_tag());
            }

            List<TestRunApplication> list = testRunService.findByAppNode(new TestRunApplication(app.getApplication_id(), release_node_name));
            result.put(Dict.TESTRUN_APPLICATIONS, list);
            //查询应用批次
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(release_node_name, app.getApplication_id());
            String batchId = "";
            if(!CommonUtils.isNullOrEmpty(releaseBatchRecord)){
                batchId = releaseBatchRecord.getBatch_id();
            }
            result.put(Dict.BATCH_ID, batchId);
            // TODO 发应用模块查询应用详情+发任务模块查询任务集合是否关联项均检查完毕
            listResult.add(result);
        }
        return listResult;
    }

    @Override
    public ReleaseApplication queryApplicationDetail(ReleaseApplication releaseApplication) throws Exception {
        return releaseApplicationDao.queryApplicationDetail(releaseApplication);
    }

    @Override
    public ReleaseApplication queryAppByIdAndBranch(String applicationId, String releaseBranch) throws Exception {
        return releaseApplicationDao.queryAppByIdAndBranch(applicationId, releaseBranch);
    }

    @Override
    public ReleaseApplication updateReleaseApplication(ReleaseApplication releaseApplication) throws Exception {
        return releaseApplicationDao.updateReleaseApplication(releaseApplication);
    }

    @Override
    public ReleaseApplication findOneReleaseApplication(String appliction_id, String relase_node_name) throws Exception {
        return releaseApplicationDao.findOneReleaseApplication(appliction_id, relase_node_name);
    }

    @Override
    public String queryApplicationId(String application_id, Integer gitlab_project_id) throws Exception {
        if (CommonUtils.isNullOrEmpty(application_id)) {
            // application_id 和 gitlab_project_id不能同时送空
            if (CommonUtils.isNullOrEmpty(gitlab_project_id)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR);
            } else {
                Map<String, Object> send = new HashMap<>();
                send.put(Dict.ID, gitlab_project_id);
                send.put(Dict.REST_CODE,"getAppByGitId");
                Map result = (Map) restTransport.submit( send);
                if (CommonUtils.isNullOrEmpty(result)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[] {"通过gitid查询应用信息不存在"});
                }
                return (String) result.get(Dict.ID);
            }
        }
        return application_id;
    }

    public Map queryApplicationById(String application_id, Integer gitlab_project_id) throws Exception {
        if (CommonUtils.isNullOrEmpty(application_id)) {
            // application_id 和 gitlab_project_id不能同时送空
            if (CommonUtils.isNullOrEmpty(gitlab_project_id)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR);
            } else {
                Map<String, Object> send = new HashMap<>();
                send.put(Dict.ID, gitlab_project_id);
                send.put(Dict.REST_CODE,"getAppByGitId");
                Map result = (Map) restTransport.submit( send);
                if (CommonUtils.isNullOrEmpty(result)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[] {"通过gitid查询应用信息不存在"});
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public ReleaseApplication saveReleaseApplication(ReleaseApplication releaseApplication) throws Exception {
        return releaseApplicationDao.saveReleaseApplication(releaseApplication);
    }

    @Override
    public ReleaseApplication queryAppByIdAndTag(String applicationId, String product_tag) throws Exception {
        return releaseApplicationDao.queryAppByIdAndTag(applicationId, product_tag);
    }

    @Override
    public List queryTasksReviews(List<String> ids) throws Exception {
        Map TaskReviews = taskService.queryTaskReview(ids);
        List task_list=new ArrayList();
        List taskList = (List) TaskReviews.get("taskList");
        if(!CommonUtils.isNullOrEmpty(TaskReviews)) {
            boolean auditFlag = (boolean) TaskReviews.get(Dict.ALLAUDITRESULT);
            if (auditFlag) {
                return task_list;
            }else {
                for(int i=0;i<taskList.size();i++) {
                    Map taskmap = (Map) taskList.get(i);
                    Map<String, Object> review_tasks=new HashMap<String, Object>();
                    Boolean taskaudit = (Boolean) taskmap.get(Dict.AUDITRESULT);
                    if(!taskaudit) {
                        String task_id = (String) taskmap.get(Dict.TASK_ID);
                        Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
                        List<Map> spdb_managers = (List<Map>) taskInfo.get(Dict.SPDB_MASTER);
                        List<Map> task_managers = (List<Map>) taskInfo.get(Dict.MASTER);
                        review_tasks.put(Dict.BANK_MASTER, spdb_managers);
                        review_tasks.put(Dict.DEV_MANAGERS, task_managers);
                        review_tasks.put(Dict.NAME, taskInfo.get(Dict.NAME));
                        review_tasks.put(Dict.TASK_ID, task_id);
                        task_list.add(review_tasks);
                    }
                }
            }
        }
        return task_list;
    }

	@Override
	public List<String> queryApplicationTags(String release_node_name, String application_id) throws Exception {
        RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
        relDevopsRecord.setApplication_id(application_id);
        relDevopsRecord.setRelease_node_name(release_node_name);
        return relDevopsRecordService.queryTagList(relDevopsRecord);
	 }

	@Override
	public void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id,String release_branch)
			throws Exception {
		releaseApplicationDao.changeReleaseNodeName(release_node_name,release_node_name_new,application_id,release_branch);
	}

    @Override
    public List<Map> queryBatchApplications(String release_node_name) throws Exception {
        List<ReleaseApplication> applist = releaseApplicationDao.queryApplications(release_node_name);
        List<Map> result = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(applist)) {
            for (ReleaseApplication releaseApplication : applist) {
                Map beanMap = CommonUtils.beanToMap(releaseApplication);
                Map<String, Object> application = appService.queryAPPbyid(releaseApplication.getApplication_id());
                RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
                relDevopsRecord.setApplication_id(releaseApplication.getApplication_id());
                relDevopsRecord.setRelease_node_name(releaseApplication.getRelease_node_name());
                List<String> tagList = relDevopsRecordService.queryTagList(relDevopsRecord);
                beanMap.put(Dict.GITLAB_PROJECT_ID, CommonUtils.isNullOrEmpty(application) ? null : application.get(Dict.GITLAB_PROJECT_ID));
                beanMap.put(Dict.PRODUCT_TAG, tagList);
                result.add(beanMap);
            }
        }
        return result;
    }

	@Override
	public void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		releaseApplicationDao.updateReleaseNodeName(old_release_node_name , release_node_name);
	}

    @Override
    public void updateReleaseApplicationConfig(ReleaseApplication releaseApplication) {
        releaseApplicationDao.updateReleaseApplicationConfig(releaseApplication);
    }

    @Override
    public ReleaseApplication updateConfigConfirm(ReleaseApplication releaseApplication) {
        return releaseApplicationDao.updateConfigConfirm(releaseApplication);
    }

    @Override
    public List<Map<String, Object>> queryApplicationsByReleaseNodeName(String release_node_name, String question_no, List<String> systemIds, List<String> applicationIds) throws Exception {
        List<ReleaseApplication> list = releaseApplicationDao.queryApplications(release_node_name);
        List<Map<String, Object>> mapList = new ArrayList<>();
        for(ReleaseApplication releaseApplication : list) {
            if(applicationIds.contains(releaseApplication.getApplication_id())){
                Map<String, Object> application = appService.queryAPPbyid(releaseApplication.getApplication_id());
                if(systemIds.contains(((Map<String, String>)application.get(Dict.SYSTEM)).get(Dict.ID))) {
                    // 根据投产窗口及应用id查询投产任务表从而获取任务id
                    Map<String,String> req = new HashMap<>();
                    req.put(Dict.RELEASE_NODE_NAME, release_node_name);
                    req.put(Dict.APPLICATION_ID, releaseApplication.getApplication_id());
                    List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(req);
                    String taskId = releaseTasks.get(0).getTask_id();
                    Set<String> hs = new HashSet<String>();
                    hs.add(taskId);
                    Map<String, Object> allTaskMap = taskService.queryTasksByIds(hs);
                    Map<String, Object> taskMap = (Map<String, Object>) allTaskMap.get(taskId);
                    List<Map> task_manager_list = (List<Map>) taskMap.get(Dict.MASTER);
                    Map<String,Object> master_map = task_manager_list.get(0);
                    // 根据任务id获取任务负责人
                    Map<String,Object> applay_map = new HashMap<>();
                    /*String name = ((List<Map<String, String>>)application.get(Dict.SPDB_MANAGERS)).get(0).get(Dict.USER_NAME_CN);
                    String name_en = ((List<Map<String, String>>)application.get(Dict.SPDB_MANAGERS)).get(0).get(Dict.USER_NAME_EN);*/
                    applay_map.put(Dict.NAME_CN, master_map.get(Dict.USER_NAME_CN));
                    applay_map.put(Dict.NAME_EN, master_map.get(Dict.USER_NAME_EN));
                    String tag = prodApplicationDao.findImageUriByReleaseNodeName(releaseApplication.getApplication_id(), release_node_name);
                    if(!CommonUtils.isNullOrEmpty(tag)) {
                        Map<String, Object> differs;
                        try {
                            differs = gitlabService.compareBranches(
                                    String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)), tag, releaseApplication.getRelease_branch());
                        } catch (Exception e) {
                            throw new FdevException(ErrorConstants.BRANCH_NOT_EXIST, new String[]{(String) application.get(Dict.NAME_EN), releaseApplication.getRelease_branch() + "、" + tag});
                        }
                        if(!CommonUtils.isNullOrEmpty(differs.get(Dict.DIFFS))) {
                            List<Map<String, Object>> diffs = (List<Map<String, Object>>) differs.get(Dict.DIFFS);
                            for(Map<String, Object> diff : diffs) {
                                String path = (String) diff.get(Dict.OLD_PATH);
                                Map<String, Object> map = new HashMap<>();
                                map.put(Dict.QUESTION_NO, question_no);
                                map.put(Dict.PATH, path);
                                map.put(Dict.NAME, applay_map);
                                mapList.add(map);
                            }
                        }
                    } else {
                        Set<String> set;
                        try {
                            set = gitlabService.queryResourceFiles(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)), releaseApplication.getRelease_branch());
                        } catch (Exception e) {
                            throw new FdevException(ErrorConstants.BRANCH_NOT_EXIST, new String[]{(String) application.get(Dict.NAME_EN), releaseApplication.getRelease_branch()});
                        }
                        for(String path : set) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(Dict.QUESTION_NO, question_no);
                            map.put(Dict.PATH, path);
                            map.put(Dict.NAME, applay_map);
                            mapList.add(map);
                        }
                    }
                }
            }
        }
        return mapList;
    }

    @Override
    public void updateApplicationPath(ReleaseApplication application) {
        releaseApplicationDao.updateApplicationPath(application);
    }

    @Override
    public String sourceMapTar(String application_id, String tag, String release_node_name, List<String> paths, String rel_env_name) {
        Map<String, Object> queryAPP;
        try {
            queryAPP = appService.queryAPPbyid(application_id);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用：" + application_id + "查询异常"});
        }
        if(CommonUtils.isNullOrEmpty(queryAPP)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用：" + application_id + "不存在"});
        }
        String ip;
        String port;
        String userName;
        String pwd;
        try {
            Map<String, Object> scpMap = appService.queryVariablesMapping((Integer) queryAPP.get(Dict.GITLAB_PROJECT_ID), rel_env_name);
            if(!CommonUtils.isNullOrEmpty(scpMap) && !CommonUtils.isNullOrEmpty(scpMap.get("FDEV_SCP_IP"))
                    && !CommonUtils.isNullOrEmpty(scpMap.get("FDEV_SCP_USER"))
                    && !CommonUtils.isNullOrEmpty(scpMap.get("FDEV_SCP_PWD"))) {
                ip = (String) scpMap.get("FDEV_SCP_IP");
                port = CommonUtils.isNullOrEmpty(scpMap.get("FDEV_SCP_PORT")) ? "22" : (String) scpMap.get("FDEV_SCP_PORT");
                userName = (String) scpMap.get("FDEV_SCP_USER");
                pwd = (String) scpMap.get("FDEV_SCP_PWD");
            } else {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用" + queryAPP.get(Dict.GITLAB_PROJECT_ID) +"在" + rel_env_name + "环境获取source map文件所属服务器失败"});
        }
        String application_name_en = (String) queryAPP.get(Dict.NAME_EN);
        logger.info("开始遍历文件");
        String pre_local_path = "/fdev/collapse/" + release_node_name + "/" + System.currentTimeMillis() + "/";
        // 将文件从nas盘复制到pod内
        int fileExistsNo = 0;
        for(String path : paths) {
            logger.info("开始获取文件{}", path);
            String[] splits = path.split("/");
            String fileName = splits[splits.length - 1];
            String local_path = pre_local_path + application_name_en + "/";
            CommonUtils.createDirectory(local_path);
            String local_file = local_path + fileName;
            try {
                CommonUtils.runPythonArray(scripts_path + "copy_collapse_file.py", new String[]{application_name_en, ip, port, userName, pwd, path, local_path});
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{path + "文件地址不正确或服务器" + ip + "用户名密码不正确"});
            }
            if(new File(local_file).exists() && new File(local_file).isFile()) {
                logger.info("获取文件{}成功", local_file);
                fileExistsNo ++;
            }
            logger.info("应用{}对应tag{}共有{}文件，实际获取文件数量{}", application_name_en, tag, paths.size(), fileExistsNo);
        }
        String tarName = "sourceMap-" + application_name_en + ".tar";
        String cmd_tar = "cd " + pre_local_path + " && tar -cvf " + tarName + " " + application_name_en;
        CommonUtils.runCmd(cmd_tar);
        String tar_file = pre_local_path + tarName;
        MultipartFile f;
        try {
            f = new MockMultipartFile(tarName, tarName, ContentType.APPLICATION_OCTET_STREAM.toString(),
                    new FileInputStream(new File(tar_file)));
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{tarName + "文件生成失败"});
        }
        String minioPath = recordAssetsUrl + release_node_name + "/" + application_name_en + "/" + tag + "/" + tarName;
        try {
            fileService.uploadFiles(minioPath, f, tarName, "fdev-release");
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{tarName + "文件上传minio失败"});
        }
        try {
            CommonUtils.deleteDirectory(new File(pre_local_path), pre_local_path);
        } catch (Exception e) {
            logger.error("本地文件夹{}删除异常", pre_local_path);
        }
        return minioPath;
    }

    @Override
    public void saveApplicationTag(String product_tag,  String revertsqlpro, String revertsqlgray, Integer gitlab_project_id, MultipartFile[] files) throws Exception {
        //通过gitlab_project_id查询应用id
        Map<String,Object> application = queryApplicationById("", gitlab_project_id);
        //根据应用id和tag名称查询投产窗口
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid((String) application.get(Dict.ID), product_tag);
        if(CommonUtils.isNullOrEmpty(relDevopsRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{product_tag + "不是fdev拉取，不做处理"});
        }
        ReleaseApplication releaseApplication = findOneReleaseApplication((String) application.get(Dict.ID), relDevopsRecord.getRelease_node_name());
        // /testassets-sit/20211031_003/mspmk-cli-makejar
        String groupFullPath = recordAssetsUrl + relDevopsRecord.getRelease_node_name() + Constants.SPLIT_STRING + application.get(Dict.NAME_EN) + Constants.SPLIT_STRING + product_tag;
            // /testassets-sit/20211031_003/mspmk-cli-makejar/AWS/DEV/mobper-staticresource/
        List<String> envList = Arrays.asList("DEV","PROC");
        List<String> typeList = Arrays.asList("gray","prd");
        String git_url = "";
        for (String env: envList){
            for(String type: typeList){
                String path = groupFullPath + Constants.SPLIT_STRING + "AWS_STATIC" + Constants.SPLIT_STRING + env + Constants.SPLIT_STRING + bucketName + Constants.SPLIT_STRING + type + Constants.SPLIT_STRING;
                for (MultipartFile file : files) {
                    String filename = file.getOriginalFilename();
                    path += filename;
                    fileService.uploadFiles(path, file, filename, "fdev-release");
                }
                git_url = git_url +  path + ";";
            }
        }
        if(!CommonUtils.isNullOrEmpty(releaseApplication) && !CommonUtils.isNullOrEmpty(git_url)) {
            if(!CommonUtils.isNullOrEmpty(releaseApplication.getStatic_resource())) {
                releaseApplication.getStatic_resource().put(product_tag, git_url);
            } else {
                Map<String,String> map = new HashMap<>();
                map.put(product_tag, git_url);
                releaseApplication.setStatic_resource(map);
            }
            // 以tag为维度，每个tag下对应4条sql,
            Map<String,Object> sql_map = new HashMap<>();
//            sql_map.put("InsertSqlPro",insertsqlpro);
            sql_map.put("RevertSqlPro",revertsqlpro);
//            sql_map.put("InsertSqlGray",insertsqlgray);
            sql_map.put("RevertSqlGray",revertsqlgray);
            if(!CommonUtils.isNullOrEmpty(releaseApplication.getApp_sql())) {
                releaseApplication.getApp_sql().put(product_tag,sql_map);
            } else {
                Map<String,Object> map = new HashMap<>();
                map.put(product_tag,sql_map);
                releaseApplication.setApp_sql(map);
            }
        }
        // 保存生成的tar包路径到投产应用表中
        releaseApplicationDao.updateStaticResource(releaseApplication);

    }

	@Override
	public List<Map<String, Object>> queryComponent(String release_node_name, String type) throws Exception {
        List<Map<String, Object>> listResult = new ArrayList<>();
        List<ReleaseApplication> applist = releaseApplicationDao.queryApplications(release_node_name);
        // 条件查询获取应用列表
        //若应用的merge_requst_id存在,且devops状态为1-已发起，待合并
        asyncService.updateApplicationMergeState(applist);
        for (ReleaseApplication app : applist) {// 遍历应用列表
        	List<ReleaseTask> tasks = releaseTaskDao.querySameAppTask(app.getApplication_id(),release_node_name);
        	String taskId = tasks.get(0).getTask_id();
        	String applicationType = (String) taskService.queryTaskDetail(taskId).get("applicationType");
            Map<String, Object> result = new HashMap<>();
            if ("4".equals(type)) {
            	Map<String, Object> component = new HashMap<String, Object>();
    			//查询骨架
            	if ("componentWeb".equals(applicationType)) {
            		component = componenService.queryMpassComponentDetail(app.getApplication_id());
    			} else {
    				component = componenService.queryComponenbyid(app.getApplication_id());
    			}
            	if (!CommonUtils.isNullOrEmpty(component)) {
            		result.put("component_id", component.get(Dict.ID));
                	result.put("module_name_en", component.get("name_en"));
                	result.put("module_name_zh", component.get("name_cn"));
                	result.put("module_type", component.get("component_type"));
                	if ("componentWeb".equals(applicationType)) {
                		result.put("module_spdb_managers", component.get("manager"));
					} else {
						result.put("module_spdb_managers", component.get("manager_id"));
					}
                	List<String> tagList = componenService.queryTagList(app.getRelease_node_name(),app.getApplication_id());
                    result.put(Dict.PRODUCT_TAG, tagList);
                    result.put(Dict.RELEASE_BRANCH, app.getRelease_branch());
				}
			} else if ("5".equals(type)) {
				Map<String, Object> archetype = new HashMap<String, Object>();
				//查询骨架
	        	if ("archetypeWeb".equals(applicationType)) {
	        		archetype = componenService.queryMpassArchetypeDetail(app.getApplication_id());
				} else {
					archetype = componenService.queryArchetypeDetail(app.getApplication_id());
				}
				if (!CommonUtils.isNullOrEmpty(archetype)) {
					result.put("component_id", archetype.get(Dict.ID));
	            	result.put("module_name_en", archetype.get("name_en"));
	            	result.put("module_name_zh", archetype.get("name_cn"));
	            	result.put("module_type", archetype.get("archetype_type"));
	            	if ("archetypeWeb".equals(applicationType)) {
	            		result.put("module_spdb_managers", archetype.get("manager"));
					} else {
						result.put("module_spdb_managers", archetype.get("manager_id"));
					}
	            	List<String> tagList = componenService.queryTagList(app.getRelease_node_name(),app.getApplication_id());
	                result.put(Dict.PRODUCT_TAG, tagList);
	                result.put(Dict.RELEASE_BRANCH, app.getRelease_branch());
				}
			} else if ("6".equals(type)) {
				Map<String, Object> image = componenService.queryBaseImageDetail(app.getApplication_id());
				if (!CommonUtils.isNullOrEmpty(image)) {
					result.put("component_id", image.get(Dict.ID));
	            	result.put("module_name_en", image.get(Dict.NAME));
	            	result.put("module_name_zh", image.get("name_cn"));
	            	result.put("module_type", image.get("image_type"));
	            	result.put("module_spdb_managers", image.get("manager"));
	            	List<String> tagList = componenService.queryTagList(app.getRelease_node_name(),app.getApplication_id());
	                result.put(Dict.PRODUCT_TAG, tagList);
	                result.put(Dict.RELEASE_BRANCH, app.getRelease_branch());
				}
			}
            //查询应用批次
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(release_node_name, app.getApplication_id());
            String batchId = "";
            if(!CommonUtils.isNullOrEmpty(releaseBatchRecord)){
                batchId = releaseBatchRecord.getBatch_id();
            }
            result.put(Dict.BATCH_ID, batchId);
            // TODO 发应用模块查询应用详情+发任务模块查询任务集合是否关联项均检查完毕
            listResult.add(result);
        }
        return listResult;
	}

	@Override
	public List<String> queryTargetVersion(Map<String, String> requestParam) throws Exception {
		List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(requestParam);
		List<String> list = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		for (ReleaseTask task : releaseTasks) {// 遍历任务列表
			String task_id = task.getTask_id();
			ids.add(task_id);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.IDS, ids);// 发task模块获取task详细信息
		map.put(Dict.REST_CODE, "queryTaskDetailByIds");
		try {
			Map<String, Object> allTaskMap = (Map<String, Object>) restTransport.submit(map);
			for (ReleaseTask task : releaseTasks) {// 遍历任务列表
				Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(task.getTask_id());
				if (CommonUtils.isNullOrEmpty(taskmap)) {
					continue;
				}
				String target_version = (String) taskmap.get("versionNum");
				list.add(target_version);
			}
		} catch (Exception e) {
			logger.error("queryTaskDetail error with:" + e);
		}
		return list;
	}

}
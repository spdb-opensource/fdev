package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.csii.pe.redis.util.Util;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.AsyncService;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.TimeUtils;
import com.spdb.fdev.fdevapp.base.utils.validate.ValidateApp;
import com.spdb.fdev.fdevapp.base.utils.validate.ValidateUtils;
import com.spdb.fdev.fdevapp.spdb.cache.IAppEntityCache;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.dao.IAppTypeDao;
import com.spdb.fdev.fdevapp.spdb.entity.*;
import com.spdb.fdev.fdevapp.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class AppEntityServiceImpl implements IAppEntityService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
    @Autowired
    private IArchetypeService archetypeService;
    @Autowired
    private IGitlabAPIService gitlabAPIService;
    @Autowired
    private IGitlabCIService gitlabCIService;
    @Autowired
    private IGitlabVariablesService gitlabVariablesService;
    @Autowired
    private IAppEntityDao appEntityDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IGitlabUserService gitlabUserService;
    @Autowired
    private IAppEntityCache appEntityCache;
    @Autowired
    private AuthorityManagers authorityManagers;
    @Autowired
    private IAppTypeDao appTypeDao;
    @Autowired
    @Lazy
    private AsyncService asyncService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Autowired
    private ISystemService systemService;

    @Value("${gitlab.token}")
    private String token;
    @Value("${gitlab.name}")
    private String name;
    @Value("${gitlab.password}")
    private String password;
    @Value("${task.api}")
    private String taskApi;
    @Value("${gitlab.groupPath}")
    private String groupPath;
    @Value("${user.api}")
    private String userApi;
    @Value("${envconfig.api}")
    private String envconfigApi;
    @Value("${spring.profiles.active}")
    private String asctive;
    @Value("${addapp.step}")
    private String addAppStep;
    @Value("${archetypeGitlabciMapping}")
    private String ciMapping;
    @Value("${continuous.ignore}")
    private String continuous_ignore;
    @Value("${userStuckPoint.RoleId}")
    public String stuckPointRoleId;
    @Value("${userStuckPoint.url}")
    public String stuckPointUrl;

    @Value("${fdev.user.groups}")
	private String groups;
    /**
     * 新增应用时候建立websocket发送进度
     *
     * @param sendMap
     * @throws Exception
     */
    void sendNotify(Map sendMap) throws Exception {
        String SocketName = (String) sendMap.get("user_name");
        String content = JSONObject.toJSONString(sendMap);
        Map param = new HashMap();
        param.put(Dict.TARGET, SocketName);
        param.put(Dict.CONTENT, content);
        param.put(Dict.ACTION, "add application");
        param.put(Dict.MODULE, "fapp");
        param.put(Dict.REST_CODE, "sendStageData");
        restTransport.submit(param);
    }

    /**
     * 新增应用总控制方法
     *
     * @param appEntity
     * @throws Exception
     */
    public void addNewAppHandler(AppEntity appEntity, Map sendMap) throws Exception {
        logger.info("method addNewAppHandler start====");
        Map<String, Object> initParm = initParm(appEntity, sendMap);
        logger.info("addNewAppHandler initParm end");
        if (initParm == null)
            return;
        Queue<String> executeQuenue = (LinkedBlockingQueue<String>) initParm.get("executeQuenue");
        int quenueSize = executeQuenue.size();
        BigDecimal b = BigDecimal.valueOf((double) 1 / quenueSize);
        String oneProgess = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        try {
            for (int i = 1; i <= quenueSize; i++) {
                String methodName = executeQuenue.poll();
                initParm.put("stepNum", i);
                CommonUtils.reflectByMethod(this, methodName, new Class[]{Map.class}, new Object[]{initParm});
                Stack rollBackStack = (Stack) initParm.get("rollBackStack");
                rollBackStack.push("rollBack" + CommonUtils.transformMethodName(methodName));//将执行成功步骤的对应回滚步骤添加到回滚栈中
                initParm.put("rollBackStack", rollBackStack);
                //修改的进度
                if (i < quenueSize) {
                    b = BigDecimal.valueOf(Double.valueOf(oneProgess) * (Integer) (initParm.get("stepNum")));
                    String currProgress = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    sendMap.put("rate_progress", currProgress);
                } else {
                    sendMap.put("rate_progress", "1");
                    sendMap.put("status", "0");
                    sendMap.put("app_id", (String) initParm.get("appId"));
                }
                sendMap.put("cur_msg", (String) initParm.get("curMsg"));
                try {
                    sendNotify(sendMap);
                } catch (RuntimeException e) {
                    logger.error("发送消息模块失败，失败原因:" + e.getMessage());
                }
            }
        } catch (FdevException e) {
            //将新增应用中的stauts设置为失败
            sendMap.put("cur_msg", (String) e.getArgs()[0]);
            sendMap.put("status", "2");
            sendNotify(sendMap);
            //执行回滚计划
            try {
                doRollBack((Stack) initParm.get("rollBackStack"), initParm);
            } catch (FdevException e2) {
                //回滚失败后，应该通知到相关人员执行手动回滚 考虑以邮件方式通知

            }
        }

    }

    /**
     * 新增应用初始化参数
     *
     * @param appEntity
     * @return
     * @author xxx
     */
    public Map<String, Object> initParm(AppEntity appEntity, Map sendMap)  throws Exception{
        try {
            logger.info("appEntity:" + JSONObject.toJSONString(appEntity));
            Map<String, Object> paraMap = CommonUtils.object2Map(appEntity);
            logger.info("paraMap:" + JSONObject.toJSONString(paraMap));
            
            Map archetype = this.archetypeService.queryArchetype((String) paraMap.get(Dict.ARCHETYPE_ID));
            logger.info("archetype:" + JSONObject.toJSONString(archetype));
            paraMap.put("archetype", archetype);
            String projectName = (String) paraMap.get(Dict.NAME_EN);
            String type = (String) archetype.get(Dict.TYPE);
            if (StringUtils.isNotBlank(type) && type.contains(Constant.MAVEN_MULTI_MODULE)) {
                projectName += "-parent";
            }
            paraMap.put("projectName", projectName);
            String git_group = (String) paraMap.get(Dict.GIT);
            if (git_group.endsWith("/"))
                git_group = git_group.substring(0, git_group.length() - 1);
            paraMap.put(Dict.GIT_GROUP, git_group);
            String yamlName = continuous_ignore;
            if (StringUtils.isNotBlank(type) && type.contains(Constant.VUE)) {
                yamlName = Constant.MOBCLI_VUE;
            }
            if (CommonUtils.isNullOrEmpty(yamlName))
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"持续集成模板不存在"});
            paraMap.put(Dict.YAML_NAME, yamlName);
            paraMap.put("rollBackStack", new Stack<String>());
            LinkedBlockingQueue<String> executeQuenue = CommonUtils.str2Queue(addAppStep, new LinkedBlockingQueue<String>());
            paraMap.put("executeQuenue", executeQuenue);
            logger.info("return paraMap:" + JSONObject.toJSONString(paraMap));
            return paraMap;
        } catch (Exception e) {
            logger.info("initParm:error");
            logger.info(e.getClass().getName());
            if (e instanceof FdevException) {
                sendMap.put("cur_msg", ((FdevException) e).getArgs()[0]);
            } else {
                sendMap.put("cur_msg", (String) e.getMessage() == null ? "新增应用初始化参数失败！" : (String) e.getMessage());
            }
            sendMap.put("status", "2");
            try {
                logger.info("sendNotify sendMap:" + JSONObject.toJSONString(sendMap));
                sendNotify(sendMap);
            } catch (Exception e1) {
                logger.error("新增应用初始化参数失败！\n" + e.getMessage());
            }
        }
        logger.info("initParm return null");
        return null;
    }

    /**
     * 通过gitlab API创建项目
     *
     * @param paraMap
     */
    public void createProjectByGitLabApi(Map<String, Object> paraMap) {
        try {
            Object result = this.gitlabAPIService.createProject((String) paraMap.get(Dict.GIT_GROUP), (String) paraMap.get("projectName"), this.token);
            Map map = (Map) JSON.parse((String) result);
            //将一些信息存入paraMap以供后续步骤使用
            paraMap.put("projectId", map.get(Dict.ID));
            paraMap.put(Dict.HTTP_URL_TO_REPO, map.get(Dict.HTTP_URL_TO_REPO));
            paraMap.put("curMsg", "在gitlab上创建项目成功");
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 在gitlab上创建项目成功");
        } catch (Exception e) {
            logger.error("在gitlab上创建项目失败\n" + e.getMessage());
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"在gitlab上创建项目失败!"});
        }
    }

    /**
     * 回退在gitlab创建的项目暨根据gitlab_project_id删除
     *
     * @param parmMap
     * @author xxx
     */
    public void rollBackCreateProjectByGitLabApi(Map<String, Object> parmMap) {
        try {
            this.gitlabAPIService.deleteProjectById(parmMap.get("projectId").toString());
        } catch (Exception e) {
            logger.error("在gitlab上删除项目失败!\n" + e.getMessage());
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"在gitlab上删除项目失败!"});
        }
    }

    /**
     * 给行内负责人和应用负责人添加项目的Maintainer权限
     *
     * @param parmMap 应用表参数实例
     * @author xxx
     */
    public void addSpdbAndDevMangersForApp(Map<String, Object> parmMap) {
        String projectId = parmMap.get("projectId").toString();
        try {
            HashSet<Map<String, String>> allMangerSet = new HashSet<Map<String, String>>();
            allMangerSet.addAll((HashSet) parmMap.get(Dict.SPDB_MANAGERS));
            allMangerSet.addAll((HashSet) parmMap.get(Dict.DEV_MANAGERS));
            this.gitlabUserService.addMembers(addMembersForApp(allMangerSet, projectId, Dict.MAINTAINER));
            parmMap.put("curMsg", "给项目负责人添加Maintainer权限成功");
            logger.info("@@@@@@ step:" + parmMap.get("stepNum") + " 给行内负责人和应用负责人添加" + projectId + "项目的 Maintainer 权限 成功");
        } catch (Exception e) {
            logger.error("给行内负责人和应用负责人添加" + projectId + "项目的Maintainer权限失败\n" + e.getMessage());
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给行内负责人和应用负责人添加" + projectId + "项目的 Maintainer 权限 失败!"});
        }
    }

    /**
     * 给项目添加骨架
     *
     * @param paraMap
     * @author xxx
     */
    public void createArchetypeForProject(Map<String, Object> paraMap) {
        String projectId = paraMap.get("projectId").toString();
        try {
            gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
            boolean bo = archetypeService.createArchetype((Map) paraMap.get("archetype"), (String) paraMap.get(Dict.HTTP_URL_TO_REPO), this.groupPath, this.name, this.password, (String) paraMap.get(Dict.NAME_EN), this.token);
            if (!bo)
                throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"生成骨架代码出错"});
            paraMap.put("curMsg", "给项目添加骨架成功");
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 给项目添加骨架成功");
        } catch (Exception e) {
            logger.error("给项目添加骨架失败!\n" + e.getMessage());
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给项目添加骨架失败!"});
        } finally {
            try {
                gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
            } catch (Exception e) {
                logger.error("给项目添加骨架回收权限失败");
            }
        }
    }


    /**
     * 给项目添加持续集成
     *
     * @param paraMap
     * @author xxx
     */
    public void addContinuousIntergrationForProject(Map<String, Object> paraMap) {
        Map archetype = (Map) paraMap.get("archetype");
        try {
            boolean bo = gitlabCIService.addDevops(paraMap.get("projectId").toString(), this.token, (String) paraMap.get(Dict.YAML_NAME), (String) paraMap.get(Dict.NAME_EN), (String) paraMap.get(Dict.HTTP_URL_TO_REPO), archetype, (String) paraMap.get("fileEncoding"));
            if (!bo)
                throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"添加持续集成出错"});
            paraMap.put("curMsg", "给项目添加持续集成成功");
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 给项目添加持续集成成功");
        } catch (Exception e) {
            logger.error("@@@@@@ 给项目添加持续集成失败!\n" + e.getMessage());
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给项目添加持续集成失败!"});
        }
    }


    /**
     * 给项目在gitlab上拉取sit分支
     *
     * @param paraMap
     * @author xxx
     */
    public void createSITBranch(Map<String, Object> paraMap) {
        try {
            Object branch = this.gitlabAPIService.createBranch(String.valueOf(paraMap.get("projectId")), Dict.SIT_UP, Dict.MASTER, this.token);
            ValidateUtils.validateObj(branch, "拉取SIT失败");
            paraMap.put("curMsg", "给项目项目拉取SIT成功");
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 给" + paraMap.get("projectId") + "项目拉取SIT成功");
        } catch (Exception e) {
            logger.error("给" + paraMap.get("projectId") + "项目拉取SIT失败!");
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给" + paraMap.get("projectId") + "项目拉取SIT失败!"});
        }
    }


    /**
     * 给项目添加wehook
     *
     * @param paraMap
     * @author xxx
     */
    public void addWeHookForProject(Map<String, Object> paraMap) {
        try {
            // 添加过webhook的不做添加
            if (this.gitlabAPIService.validateProjectHook(String.valueOf(paraMap.get("projectId")), this.token)) {
                this.gitlabAPIService.addProjectHook(String.valueOf(paraMap.get("projectId")), this.token);
            }
            paraMap.put("curMsg", "给项目添加wehook成功");
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 给" + paraMap.get("projectId") + "项目添加wehook成功");
        } catch (Exception e) {
            logger.error("给" + paraMap.get("projectId") + "项目拉取SIT失败!");
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给" + paraMap.get("projectId") + "项目拉取SIT失败!"});
        }
    }

    /**
     * 存入数据库
     *
     * @param paraMap
     * @author xxx
     */
    public void addAppEntityToDB(Map<String, Object> paraMap) {
        try {
            paraMap.put(Dict.GITLAB_PROJECT_ID, Integer.parseInt(String.valueOf(paraMap.get("projectId"))));
            paraMap.put(Dict.GIT, (String) paraMap.get(Dict.HTTP_URL_TO_REPO));
            AppEntity appEntity = CommonUtils.map2Object(paraMap, AppEntity.class);
            //关闭自动部署开关
            appEntity.setPipeline_schedule_switch("0");
            AppEntity save = this.appEntityDao.save(this.addLabel(appEntity));
            paraMap.put("curMsg", "新增项目应用成功");
            paraMap.put("appId", save.getId());
            logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 新增" + paraMap.get("projectId") + "项目应用成功");
        } catch (Exception e) {
            logger.error("新增" + paraMap.get("projectId") + "项目项目应用失败!");
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"新增" + paraMap.get("projectId") + "项目项目应用失败!"});
        }
    }

    /**
     * 执行回滚操作
     *
     * @param rollBackStack 存放需要回滚的方法名
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void doRollBack(Stack<String> rollBackStack, Map paraMap) {
        while (!rollBackStack.empty()) {
            String methodName = rollBackStack.pop();
            try {
                CommonUtils.reflectByMethod(this, methodName, new Class[]{Map.class}, new Object[]{paraMap});
            } catch (Exception e) {
                throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"回滚失败!"});
            }
        }
    }


    /**
     * 获得当前登录用户的角色
     *
     * @return roleList
     * @throws Exception
     */
    public ArrayList<String> getManagerAuthority() throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String creator = user.getId();
        Map param = new HashMap();
        param.put(Dict.ID, creator);
        param.put(Dict.REST_CODE, Dict.QUERYUSER);
        Object userResult = restTransport.submit(param);
        ArrayList jsonArray = (ArrayList) userResult;
        Map usermap = (Map) jsonArray.get(0);
        ArrayList roleList = (ArrayList) usermap.get(Dict.ROLE_ID);
        return roleList;
    }

    @Override
    public Map findById(String id) throws Exception {
        AppEntity appEntity = this.appEntityDao.findById(id);
        if (null == appEntity) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        Map<String, Object> app = CommonUtils.object2Map(appEntity);
        // 根据组id获取组信息
        Map<String, Object> group = appEntityCache.queryByGroupId((String) app.get(Dict.GROUP));
        if (!CommonUtils.isNullOrEmpty(group)) {
            app.put(Dict.GROUP, group);
        }
        //组装类型名称
        AppType appType = appTypeDao.findById((String) app.get(Dict.TYPE_ID));
        if (appType != null) {
            app.put(Dict.TYPE_NAME, appType.getName());
        }
        //组装system信息
        if (null != appEntity.getSystem()) {
            try {
                AppSystem paramEntity = new AppSystem();
                paramEntity.setId(appEntity.getSystem());
                AppSystem appSystem = this.systemService.getSystem(paramEntity).get(0);
                app.put("system", appSystem);
            } catch (Exception e) {
                app.put("system", null);
                logger.error(ErrorConstants.DATA_NOT_EXIST, new String[]{"system对应的数据不存在"});
            }
        }
        return app;
    }

    /**
     * 更新 应用的时候
     * 需求是
     * 直接添加新权限
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public AppEntity update(Map map) throws Exception {
        String requestData = JSON.toJSONString(map);
        AppEntity appEntity = JSON.parseObject(requestData, AppEntity.class);
        Set<Map<String, String>> spdbManagers = new LinkedHashSet<Map<String, String>>();
        Set<Map<String, String>> devManagers = new LinkedHashSet<Map<String, String>>();
        List<Map<String, String>> spdbManagersList = (List<Map<String, String>>) map.get("spdb_managers");
        List<Map<String, String>> devManagersList = (List<Map<String, String>>) map.get("dev_managers");
        for (Map map1 : spdbManagersList) {
            spdbManagers.add(map1);
        }
        for (Map map2 : devManagersList) {
            devManagers.add(map2);
        }
        appEntity.setSpdb_managers(spdbManagers);
        appEntity.setDev_managers(devManagers);
        appEntity.setId((String) map.get("id"));
        //对废弃应用不做处理
        AppEntity entity1 = appEntityDao.findById(appEntity.getId());
        if (null != entity1.getStatus() && entity1.getStatus().equals("0")) {
            return appEntity;
        }
        //添加source back跳过权限校验
        String back = (String) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.SOURCE);
        if (!Dict.BACK.equals(back)) {
            //权限控制，当前行内负责人和应用负责人才能更新应用
            Boolean authorityManager = false;
            String appId = appEntity.getId();
            //从数据库拿到应用实体queryappEntity
            AppEntity queryappEntity = appEntityDao.findById(appId);
            User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
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

        this.gitlabUserService.addMembers(addMembersForApp(appEntity.getSpdb_managers(), appEntity.getGitlab_project_id().toString(), Dict.MAINTAINER));
        logger.info("@@@@@@ 给行内负责人添加 Maintainer 权限 成功");
        this.gitlabUserService.addMembers(addMembersForApp(appEntity.getDev_managers(), appEntity.getGitlab_project_id().toString(), Dict.MAINTAINER));
        logger.info("@@@@@@ 给应用负责人添加 Maintainer 权限 成功");
        logger.info("appUpdateBefore:" + appEntity);
        AppEntity entity = this.appEntityDao.update(appEntity);
        logger.info("appUpdateAfter:" + entity);

        //同步修改数据库模块行内应用负责人
        if (!CommonUtils.isEqualCollection(entity1.getSpdb_managers(), entity.getSpdb_managers())) {    //行内负责人变动
            appEntityCache.updateManager(entity.getId(), entity.getSpdb_managers());
        }

        String id = entity.getId();
        String nameEn = entity.getName_en();
        //根据应用ID进行缓存清理.
        this.appEntityCache.resetAppEntityByNameEN(id);
        this.appEntityCache.resetAppEntityByNameEN(nameEn);
        return entity;
    }

    @Override
    public List<Map> query(AppEntity appEntity) throws Exception {
        List<AppEntity> query = this.appEntityDao.query(appEntity);
        HashSet<Map<String, String>> spdb_managers = new HashSet<>();
        HashSet<Map<String, String>> dev_managers = new HashSet<>();
        List<Map> appMap = new ArrayList<>();
        String idkey = "";
        String userNameENKey = "";
        String key = "";
        String keys = "";
        for (AppEntity entity : query) {
            List<String> ids = new ArrayList<>();
            List<String> labelName = new ArrayList<>();
            List<String> userNameENs = new ArrayList<>();
            Map<String, List<String>> param = new HashMap<String, List<String>>();
            spdb_managers = (HashSet<Map<String, String>>) entity.getSpdb_managers();
            List<Object> list = valdate(spdb_managers, ids, idkey, userNameENs, userNameENKey);
            dev_managers = (HashSet<Map<String, String>>) entity.getDev_managers();
            List<Object> lists = valdate(dev_managers, (List) list.get(0), (String) list.get(1), (List) list.get(2), (String) list.get(3));
            ids = (List) lists.get(0);
            userNameENs = (List) lists.get(2);
            if (ids.size() == 0) {
                keys = (String) lists.get(3);
                key = "usernameEns";
                param.put(key, userNameENs);
            } else {
                keys = (String) lists.get(1);
                key = Dict.IDS;
                param.put(key, ids);
            }
            Map<String, Map> params = this.appEntityCache.getUsersByIdsOrUserNameEN(keys, param);
            spdb_managers = updateManagers(spdb_managers, key, params);
            entity.setSpdb_managers(spdb_managers);
            dev_managers = updateManagers(dev_managers, key, params);
            entity.setDev_managers(dev_managers);
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装组信息
            String group_id = (String) map.get(Dict.GROUP);
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo);
            }
            //组装类型名称
            AppType appType = appTypeDao.findById((String) map.get(Dict.TYPE_ID));
            if (appType != null) {
                map.put(Dict.TYPE_NAME, appType.getName());
            }
            appMap.add(map);
            idkey = "";
            userNameENKey = "";
            key = "";
        }
        return appMap;
    }

    @Override
    public List<Map<String, String>> queryForSelect() throws Exception {
        return this.appEntityDao.query(null).stream().map(app -> new HashMap<String, String>() {{
            put(Dict.ID, app.getId());
            put(Dict.NAME_EN, app.getName_en());
            put(Dict.NAME_ZH, app.getName_zh());
        }}).collect(Collectors.toList());
    }

    /**
     * 根据中文名称模糊查询应用信息
     *
     * @param appnamezh
     * @return
     * @throws Exception
     */
    @Override
    public List<AppEntity> queryByAppNameZh(String appnamezh) {
        return this.appEntityDao.queryByAppNameZh(appnamezh);
    }

    /**
     * 根据英文名称模糊查询应用信息
     *
     * @param appnameen
     * @return
     * @throws Exception
     */
    @Override
    public List<AppEntity> queryByAppNameEn(String appnameen) {
        return this.appEntityDao.queryByAppNameEn(appnameen);
    }


    /**
     * 根据英文信息精准查询应用信息
     *
     * @param appNameEn
     * @return
     */
    @Override
    public List<AppEntity> queryByAppName(String appNameEn) {
        return this.appEntityDao.queryByAppName(appNameEn);
    }


    /**
     * 根据小组集合查询各组近6周的应用数量
     *
     * @param ids
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Map> queryAppNum(List<String> ids, boolean flag) throws Exception {
        List<Date> dates = CommonUtils.getLastSixWeek(new Date());
        Map<String, Map> result = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        for (String groupid : ids) {
            int num = 0;
            Map<String, Integer> nums = new HashedMap();
            for (int i = 0; i <= 5; i++) {
                if (flag) {
                    List<String> groupList = appEntityCache.getAllGroup(groupid);
                    num = appEntityDao.queryAppNum(groupList, "", CommonUtils.getDateStr(dates.get(i)));
                } else {
                    num = appEntityDao.queryAppNum(groupid, "", CommonUtils.getDateStr(dates.get(i)));
                }
                nums.put(dateFormat.format(dates.get(i)), num);
            }
            result.put(groupid, nums);
        }
        return result;
    }

    /**
     * 通过应用 id 分支名 自动false或者定时true状态 获取 env_name
     *
     * @param ci_project_id
     * @param ci_commit_ref_name
     * @param ci_schedule
     * @return
     */
    @Override
    public List<Map<String, String>> getSitSlug(Integer ci_project_id, String ci_commit_ref_name, Boolean ci_schedule) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        AppEntity appEntity = this.getAppByGitlabId(ci_project_id);
        if (null == appEntity) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        HashSet<EnvBranch> sit = appEntity.getSit();
        if (sit != null && sit.size() > 0) {
            for (EnvBranch envBranch : sit) {
                if (ci_schedule) {
                    // 定时
                    if (envBranch.getName().equals(ci_commit_ref_name)) {
                        String schedule_env_name = envBranch.getSchedule_env_name();
                        if (CommonUtils.isNullOrEmpty(schedule_env_name)) {
                            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"环境信息不存在"});
                        }
                        map.put(Dict.ENV_NAME, schedule_env_name);
                        list.add(map);
                    }
                } else {
                    String auto_env_name = envBranch.getAuto_env_name();
                    if (CommonUtils.isNullOrEmpty(auto_env_name)) {
                        throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"环境信息不存在"});
                    }
                    map.put(Dict.ENV_NAME, auto_env_name);
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 根据gitlab id 获取 应用信息
     *
     * @param id
     * @return
     */
    @Override
    public AppEntity getAppByGitlabId(Integer id) {
        return this.appEntityDao.getAppByGitlabId(id);
    }

    /**
     * 根据应用id 去获取分支名
     *
     * @param id
     * @return
     */
    @Override
    public List<String> getBranchNameByAppId(String id) throws Exception {
        List<String> list = new ArrayList<>();
        AppEntity appEntity = this.appEntityDao.findById(id);
        if (null == appEntity) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"未查找到相关应用  应用id:" + id});
        }

        HashSet<EnvBranch> sit = appEntity.getSit();
        if (sit != null && sit.size() > 0) {
            sit.forEach(envBranch -> list.add(envBranch.getName()));
        }
        return list;
    }

    /**
     * 录入已有的应用信息
     *
     * @param
     * @return
     */
    @Override
    public AppEntity add(Map map) throws Exception {
        // 权限控制，只有行内负责人和厂商负责人有权限添加
        // 比对分支名
        // 1.添加持续集成模板
        // 2.拉取 sit分支 (先判断有没有当前分支 如果有 通知 其 删除原来分支 在进行)
        // 3.创建应用的定时触发持续集成（默认值）
        // 4.添加webhook
        //  自动扫描

        String requestData = JSON.toJSONString(map);
        AppEntity appEntity = JSON.parseObject(requestData, AppEntity.class);
        Set<Map<String, String>> spdbManagers = new LinkedHashSet<Map<String, String>>();
        Set<Map<String, String>> devManagers = new LinkedHashSet<Map<String, String>>();
        List<Map<String, String>> spdbManagersList = (List<Map<String, String>>) map.get("spdb_managers");
        List<Map<String, String>> devManagersList = (List<Map<String, String>>) map.get("dev_managers");
        for (Map map1 : spdbManagersList) {
            spdbManagers.add(map1);
        }
        for (Map map2 : devManagersList) {
            devManagers.add(map2);
        }
        appEntity.setSpdb_managers(spdbManagers);
        appEntity.setDev_managers(devManagers);


        //获取当前用户权限
        ArrayList roleList = getManagerAuthority();
        //校验当前用户是否是卡点管理员
        boolean operateManager = true;
        if (!ValidateApp.validateUserStuckPoint(roleList, authorityManagers.getStuckPointRoleId())) {
            operateManager = false;
        }
        boolean authorityManager = true;
        //校验用户权限 厂商负责人 行内负责人 拥有权限
        if (!ValidateApp.validateUserAuthority(roleList, authorityManagers.getAuthManagers())) {
            authorityManager = false;
        }
        if (authorityManager == false && operateManager == false) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }
        // 设置新增应用标识,设置为"2",为新录入的已有应用
        appEntity.setNew_add_sign("2");
        //判断应用英文名只能为小写英文字母或-
        AppType appType = appTypeDao.findById(appEntity.getType_id());
        //判断是否进行应用名校验
        boolean isValid = true;
        //判断为IOS应用或Andoid应用
        if (appType != null) {
            if ((appType.getName().toUpperCase().contains(GitlabAPIServiceImpl.AppTypeEnum.IOS.toString())
            ) || (appType.getName().toUpperCase().contains(GitlabAPIServiceImpl.AppTypeEnum.ANDROID.toString()))
                    || (appType.getName().toUpperCase().contains(Constant.CONTAINER_PROJECT))
                    || (appType.getName().toUpperCase().contains(Constant.OLD_VERSION_SERVICE))) {
                isValid = false;
            }
        }
        //是否是互联网条线，该值来判断是否
        String isInternetSystem = appEntity.getIsInternetSystem();
        //是否校验三段式
        Boolean internetValid = true;
        if (!CommonUtils.isNullOrEmpty(isInternetSystem))
            internetValid = isInternetSystem.equals("1") ? true : false;
        //不是互联网条线不需要校验三段式
        if (isValid && !internetValid && !ValidateApp.validateAppEnNameNoThreeStage(appEntity.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范！"});
        }
        //为互联网条线需要校验三段式
        if (isValid && internetValid && !ValidateApp.validateAppEnName(appEntity.getName_en()))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范！"});
        String project_id = String.valueOf(appEntity.getGitlab_project_id());
        Integer id = appEntity.getGitlab_project_id();

        Map projectMap = this.gitlabAPIService.getProjectInfo(project_id, this.token);
        ValidateUtils.validateObj(projectMap, "该项目不存在");

        String http_url_to_repo = (String) projectMap.get(Dict.HTTP_URL_TO_REPO);
        appEntity.setGit(http_url_to_repo);
        // 控制 中文名 英文 重复
        List<AppEntity> query = this.appEntityDao.query(new AppEntity());
        ValidateApp.checkAppNameEnAndNameZh(query, appEntity);

        //控制 gitlab project id 重复
        ValidateApp.checkAppGitlaProjectId(query, appEntity);

        // step 1. 比对分支名
        Object branchList = this.gitlabAPIService.getProjectBranchList(project_id, this.token);
        ValidateUtils.compireBranch(branchList);
        // step 2 添加成员
        this.gitlabUserService.addMembers(addMembersForApp((HashSet<Map<String, String>>) appEntity.getSpdb_managers(), project_id, Dict.MAINTAINER));
        logger.info("@@@@@@ 给行内负责人添加 Maintainer 权限 成功");
        this.gitlabUserService.addMembers(addMembersForApp((HashSet<Map<String, String>>) appEntity.getDev_managers(), project_id, Dict.MAINTAINER));
        logger.info("@@@@@@ 给应用负责人添加 Maintainer 权限 成功");
        // 查询 gitlabCi 模板
        GitlabCI gitlabCI = this.gitlabCIService.findById(appEntity.getGitlabci_id());
        ValidateUtils.validateObj(gitlabCI, "查询集成模板为空");

        // Step 3. 给项目添加持续集成
        boolean bo = gitlabCIService.addDevops(project_id, this.token, gitlabCI.getYaml_name(), appEntity.getName_en(), http_url_to_repo, appEntity.getFileEncoding());
        if (!bo) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"给项目添加持续集成失败"});
        }

        // Step 4. 给项目拉取 SIT 分支,设置 SIT 对应环境
        Object branch = this.gitlabAPIService.createBranch(project_id, Dict.SIT_UP, Dict.MASTER, this.token);
        ValidateUtils.validateObj(branch, "拉取SIT失败");
        logger.info("@@@@@@ 给项目拉取SIT成功");

        // step 6 添加webhook
        // 添加过webhook的不做添加
        if (this.gitlabAPIService.validateProjectHook(project_id, this.token)) {
            this.gitlabAPIService.addProjectHook(project_id, this.token);
        }
        logger.info("@@@@@@ 给项目添加webhook成功");

        // step 7 数据入库
        AppEntity entity = this.appEntityDao.save(this.addLabel(appEntity));

        //自动扫描
        asyncService.autoScan(appEntity);

        return entity;

    }

    /**
     * 封装 给项目 添加 成员所需要 的信息
     *
     * @param managers
     * @return
     */
    private Map addMembersForApp(Set<Map<String, String>> managers, String id, String role) throws Exception {
        Map<String, Object> spdbMap = new HashMap<>();
        spdbMap.put(Dict.ROLE, role);
        spdbMap.put(Dict.ID, id);
        List<String> objects = new ArrayList<>();
        for (Map<String, String> map : managers) {
            Map<String, String> param = new HashMap<>();
            String name_en = map.get(Dict.USER_NAME_EN);
            String url = this.userApi + "/user/query";
            param.put(Dict.USER_NAME_EN, name_en);
            param.put(Dict.REST_CODE, Dict.QUERYUSER);
            Object submit = this.restTransport.submit(param);
            if (!CommonUtils.isNullOrEmpty(submit)) {
                List userList = net.sf.json.JSONArray.fromObject(submit);
                if (userList.size() > 0) {
                    Map parse = (Map) JSONObject.parse(userList.get(0).toString());
                    objects.add((String) parse.get(Dict.GIT_USER_ID));
                }
            }
        }
        spdbMap.put(Dict.GIT_USER_ID, objects);
        return spdbMap;
    }

    /***
     * 将应用的行内负责人和厂商负责人合并,并做好懒加载的key
     * @param param 应用的行内负责人和厂商负责人数据
     * @param listIds Ids集合
     * @param idkey   Idkey
     * @param listNames userNameEN集合
     * @param userNameENKey userNameENKey
     * @return
     */
    private List<Object> valdate(HashSet<Map<String, String>> param,
                                 List<String> listIds, String idkey, List<String> listNames, String userNameENKey) {
        for (Map<String, String> map : param) {
            if (map.containsKey(Dict.ID)) {
                String Id = map.get(Dict.ID);
                if (!listIds.contains(Id)) {
                    listIds.add(Id);
                    if (idkey.length() == 0) {
                        idkey = idkey + Id;
                    } else {
                        idkey = idkey + "-" + Id;
                    }
                }
            } else if (map.containsKey(Dict.USER_NAME_EN)) {
                String name_en = map.get(Dict.USER_NAME_EN);
                if (!listNames.contains(name_en)) {
                    listNames.add(name_en);
                    if (userNameENKey.length() == 0) {
                        userNameENKey = userNameENKey + name_en;
                    } else {
                        userNameENKey = userNameENKey + "-" + name_en;
                    }
                }
            }
        }
        List<Object> list = new ArrayList<>();
        list.add(listIds);
        list.add(idkey);
        list.add(listNames);
        list.add(userNameENKey);
        return list;
    }

    /*    *//**
     * 给项目设置定时触发持续集成环境
     *
     * @param paraMap
     * @author xxx
     *//*
    public void createPipelineScheduleForProject(Map<String, Object> paraMap) {
        try {
            if ("pro".equals(asctive)) {
                this.gitlabVariablesService.createPipelineSchedule(String.valueOf(paraMap.get("projectId")), "定时触发SIT编译", Dict.SIT_UP, "0 4 * * *", Dict.CI_SCHEDULE, Dict.TRUE, this.token);
                paraMap.put("curMsg", "给项目设置定时触发持续集成环境成功");
                logger.info("@@@@@@ step:" + paraMap.get("stepNum") + " 给" + paraMap.get("projectId") + "项目设置定时触发持续集成环境成功");
            }
        } catch (Exception e) {
            logger.error("给" + paraMap.get("projectId") + "项目设置定时触发持续集成环境失败!");
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"给" + paraMap.get("projectId") + "项目设置定时触发持续集成环境失败!"});
        }

    }*/

    /***
     * 更新应用中的行内负责人/厂商负责人信息
     * @param Mapdata 行内负责人/厂商负责人信息原信息
     * @param key     Dict.IDS/usernameEns
     * @param params   实时用户信息
     * @return 更新的用户信息
     */
    private HashSet<Map<String, String>> updateManagers(HashSet<Map<String, String>> Mapdata, String
            key, Map<String, Map> params) {
        if (Dict.IDS.equals(key)) {
            for (Map<String, String> map : Mapdata) {
                if (map.containsKey(Dict.ID)) {
                    String Id = map.get(Dict.ID);
                    Object ob = params.get(Id);
                    if (ob instanceof Map) {
                        Map maps = (Map) ob;
                        String userName_CN = (String) maps.get(Dict.USER_NAME_CN);
                        map.replace(Dict.USER_NAME_CN, userName_CN);
                    }
                }
            }
        } else if ("usernameEns".equals(key)) {
            for (Map<String, String> map : Mapdata) {
                String name_en = map.get(Dict.USER_NAME_EN);
                Object ob = params.get(name_en);
                if (ob instanceof Map) {
                    Map maps = (Map) ob;
                    String userName_CN = (String) maps.get(Dict.USER_NAME_CN);
                    map.replace(Dict.USER_NAME_CN, userName_CN);
                }
            }
        }
        return Mapdata;
    }

    @Override
    public Map<String, Object> saveByAsync(Map map) throws Exception {

        String requestData = JSON.toJSONString(map);
        AppEntity appEntity = JSON.parseObject(requestData, AppEntity.class);
        Set<Map<String, String>> spdbManagers = new LinkedHashSet<Map<String, String>>();
        Set<Map<String, String>> devManagers = new LinkedHashSet<Map<String, String>>();
        List<Map<String, String>> spdbManagersList = (List<Map<String, String>>) map.get("spdb_managers");
        List<Map<String, String>> devManagersList = (List<Map<String, String>>) map.get("dev_managers");
        for (Map map1 : spdbManagersList) {
            spdbManagers.add(map1);
        }
        for (Map map2 : devManagersList) {
            devManagers.add(map2);
        }
        appEntity.setSpdb_managers(spdbManagers);
        appEntity.setDev_managers(devManagers);

        //获取当前用户角色
        ArrayList roleList = getManagerAuthority();
        //校验用户是否行内或厂商负责人
        boolean authorityManager = true;
        //校验用户权限 厂商负责人 行内负责人 拥有权限
        if (!ValidateApp.validateUserAuthority(roleList, authorityManagers.getAuthManagers())) {
            authorityManager = false;
        }

        // 校验卡点管理员
        boolean operateManager = true;
        if (!ValidateApp.validateUserStuckPoint(roleList, authorityManagers.getStuckPointRoleId())) {
            operateManager = false;
        }
        if (authorityManager == false && operateManager == false) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }
        // 新增时，是否新增标记自动标记为"0",即是
        appEntity.setNew_add_sign("0");
        logger.info("@@@@@@ 创建应用 入参 [{}]", appEntity.toString());
        //1代表需要校验三段式，0代表不需要校验三段式
        String isInternetSystem = appEntity.getIsInternetSystem();
        Boolean isValidate = true;
        if (!CommonUtils.isNullOrEmpty(isInternetSystem)) {
            isValidate = isInternetSystem.equals("1") ? true : false;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            //判断应用英文名只能为小写英文字母或- 以及三段式
            if (isValidate && !ValidateApp.validateAppEnName(appEntity.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范（需要校验三段式）！"});
            }
            //仅校验英文名
            if (!isValidate && !ValidateApp.validateAppEnNameNoThreeStage(appEntity.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范（不需要校验三段式）！"});
            }
            List<AppEntity> query = this.appEntityDao.query(new AppEntity());
            ValidateApp.checkAppNameEnAndNameZh(query, appEntity);
            //生成流水号,记录用户id，记录创建时间
            ObjectId objectId = new ObjectId();
            String serinalNo = objectId.toString();
            User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
            String user_en_name = user.getUser_name_en();
            Map sendmap = new HashMap();
            sendmap.put("id", serinalNo);
            sendmap.put("status", "1");
            sendmap.put("ctime", TimeUtils.getFormat(Dict.FORMAT_DATE));
            sendmap.put("user_name", user_en_name);
            result.put(Dict.ID, serinalNo);

            //异步执行新增步骤
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("====addNewAppHandler异步执行新增步骤开始====");
                        addNewAppHandler(appEntity, sendmap);
                    } catch (Exception e) {
                        logger.info("====addNewAppHandler异步执行新增步骤异常====");
                        e.printStackTrace();
                        logger.info(e.getClass().getName());
                        // logger.error(e.getMessage());
                    }
                }
            }).start();

        } catch (Exception e) {
            logger.error("新增应用失败!\n" + e.getMessage());
            throw new FdevException(Constant.I_PARAM_ERROR, new String[]{"新增应用失败!"});
        }
        return result;
    }

    /**
     * 自动部署
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @Override
    public String createPipelineSchedule(Map requestParam) throws Exception {
        String appId = (String) requestParam.get(Dict.ID);
        AppEntity queryappEntity = appEntityDao.findById(appId);
        if (CommonUtils.isNullOrEmpty(queryappEntity)) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        //权限控制，当前行内负责人和应用负责人
        Boolean authorityManager = false;
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
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

        //自动部署 "true"打开 "false"关闭（即删除定时任务）
        if (requestParam.get(Dict.AUTOSWITCH).equals(Dict.TRUE)) {
            queryappEntity.setPipeline_schedule_switch("1");
        } else if (requestParam.get(Dict.AUTOSWITCH).equals(Dict.FALSE)) {
            queryappEntity.setPipeline_schedule_switch("0");
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        appEntityDao.update(queryappEntity);
        return "设置成功";
    }

    @Override
    public String customDeplayment(String id) throws Exception {
        AppEntity appEntity = appEntityDao.findById(id);
        if (null == appEntity) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        String gitlabId = String.valueOf(appEntity.getGitlab_project_id());
        String url = ".gitlab-ci.yml";
        String content = gitlabAPIService.getContent(gitlabId, url, Dict.MASTER);
        if (content.contains(Dict.EBANK_HELPER)) {
            return "1";// "1" 新的持续集成
        }
        return "0";// "0" 旧的持续集成
    }

    @Override
    public AppEntity deleteAppById(Map requestParam) throws Exception {
        String appId = (String) requestParam.get(Dict.ID);
        Boolean authorityManager = false;
        //从数据库拿到应用实体queryappEntity
        AppEntity queryappEntity = appEntityDao.findById(appId);
        if (null == queryappEntity) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        if (null != queryappEntity.getStatus() && queryappEntity.getStatus().equals("0")) {
            throw new FdevException(ErrorConstants.APP_HAS_DELETED);
        }
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //判断用户是否是当前应用的行 内项目负责人
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

        List<String> tasks = queryAppTasks(appId);
        if (null != tasks && tasks.size() > 0) {
            throw new FdevException(ErrorConstants.EXIST_TASK_NOT_OPTIONS, new String[]{""});
        }
        queryappEntity.setStatus("0");// 设置应用状态为"0" 已废弃
        AppEntity entity = appEntityDao.update(queryappEntity);
        //异步删除接口相关信息接口模块
        asyncService.deleteDataforApp(queryappEntity.getName_en());
        logger.info("deleteApp :" + entity);
        String id = entity.getId();
        String nameEn = entity.getName_en();
        //异步发送kafka进行废弃通知
        Integer project_id = entity.getGitlab_project_id();
        String nameZh = entity.getName_zh();
        Map param = new HashMap();
        param.put(Dict.ID, id);
        param.put(Dict.NAME_EN, nameEn);
        param.put(Dict.GITLAB_PROJECT_ID, project_id);
        param.put(Dict.NAME_ZH, nameZh);
        param.put(Dict.USER_NAME_EN, user.getUser_name_en());
        String deleteTime = CommonUtils.getDateStr(new Date());
        param.put(Dict.DELETE_TIME, deleteTime);
        this.asyncService.sendKafkaMessage(param);
        //根据应用ID进行缓存清理.
        this.appEntityCache.resetAppEntityByNameEN(id);
        this.appEntityCache.resetAppEntityByNameEN(nameEn);
        return entity;
    }

    @Override
    public List<Map> queryAbandonApp(AppEntity appEntity) throws Exception {
        List<AppEntity> query = this.appEntityDao.queryAbandonApp(appEntity);
        List<Map> appMap = new ArrayList<>();
        for (AppEntity entity : query) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装组信息
            String group_id = (String) map.get(Dict.GROUP);
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo);
            }
            //组装类型名称
            AppType appType = appTypeDao.findById((String) map.get(Dict.TYPE_ID));
            if (appType != null) {
                map.put(Dict.TYPE_NAME, appType.getName());
            }
            appMap.add(map);
        }
        return appMap;
    }

    @Override
    public void scheduleJob() throws Exception {
        List<AppEntity> appEntityList = appEntityDao.queryByAppSchedule();
        if (!CommonUtils.isNullOrEmpty(appEntityList)) {
            appEntityList.forEach(appEntity -> {
                try {
                    asyncService.scheduleJob(appEntity);
                } catch (Exception e) {
                    logger.info("scheduleJob error appName=" + appEntity.getName_en());
                }
            });
        }
    }

    @Override
    public List<AppEntity> queryAppByLabel(AppEntity appEntity) throws Exception {
        List<AppEntity> appEntityList = this.appEntityDao.queryAppByLabel(appEntity.getLabel());
        return appEntityList;
    }

    @Override
    public List<Map> getAppByIdsOrGitlabIds(Map requestMap) throws Exception {
        List params = (List) requestMap.get(Dict.IDS);
        String type = (String) requestMap.get(Dict.TYPE);
        if (CommonUtils.isNullOrEmpty(params) || CommonUtils.isNullOrEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        List<AppEntity> query = appEntityDao.getAppByIdsOrGitlabIds(params, type);
        List<Map> appMap = new ArrayList<>();
        for (AppEntity entity : query) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装组信息
            String group_id = (String) map.get(Dict.GROUP);
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo);
            }
            appMap.add(map);
        }
        return appMap;
    }

    @Override
    public List<String> getAppServiceSystem(String name_en) throws Exception {
        List<String> returnList = new ArrayList<>();
        //获取应用所属系统中文名
        List<ServiceSystem> querySystem = appEntityCache.querySystem();
        querySystem.forEach(system -> {
            if (name_en.split("-")[0].equals((String) system.getName_en())) {
                returnList.add((String) system.getName_cn());
            }
        });
        return returnList;
    }

    @Override
    public List<Map> queryAppsByUserId(String user_id) throws Exception {
        List<AppEntity> query = this.appEntityDao.queryAppsByUserId(user_id);
        List<Map> appMap = new ArrayList<>();
        for (AppEntity entity : query) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装组信息
            String group_id = (String) map.get(Dict.GROUP);
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo);
            }
            //组装类型名称
            AppType appType = appTypeDao.findById((String) map.get(Dict.TYPE_ID));
            if (appType != null) {
                map.put(Dict.TYPE_NAME, appType.getName());
            }
            appMap.add(map);
        }
        return appMap;
    }

    @Override
    public List<AppEntity> queryApps(AppEntity appEntity) throws Exception {
        List<AppEntity> list = this.appEntityDao.query(appEntity);

        for (AppEntity app : list) {
            //组装类型名称
            AppType appType = appTypeDao.findById(app.getType_id());
            if (appType != null) {
                app.setType_name(appType.getName());
            }
        }
        return list;
    }

    private String generateCIByArchetype(String archetype) {
        String ci = "";
        try {
            Map mapping = CommonUtils.str2Map(ciMapping);
            if (CommonUtils.isNullOrEmpty(mapping)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"骨架映射持续集成模板失败"});
            }
            for (Object key : mapping.keySet()) {
                String regex = (String) key;
                if (archetype.contains(regex)) {
                    ci = (String) mapping.get(key);
                }
                if (!CommonUtils.isNullOrEmpty(ci)) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"骨架映射持续集成模板失败"});
        }
        if (CommonUtils.isNullOrEmpty(ci)) {
            logger.error("没有匹配的持续集成模板");
        }
        return ci;
    }

    /**
     * 将应用所属系统中文名以标签的形式添加到应用标签
     *
     * @param appEntity
     * @return
     * @throws Exception
     */
    public AppEntity addLabel(AppEntity appEntity) throws Exception {
        List<String> appServiceSystem = this.getAppServiceSystem(appEntity.getName_en());
        HashSet<String> label = appEntity.getLabel();
        HashSet<String> serviceSystem = new HashSet<>(appServiceSystem);
        if (CommonUtils.isNullOrEmpty(appServiceSystem)) {
            serviceSystem.add("自定义系统");
        }
        label.addAll(serviceSystem);
        appEntity.setLabel(label);
        return appEntity;
    }

    /**
     * 应用列表查询
     */
    @Override
    public Map queryPagination(Map<String, Object> requestMap) throws Exception {
        Integer pageSize = (Integer) requestMap.get("size");//页面大小
        Integer currentPage = (Integer) requestMap.get("index");//当前页
        String keyword = (String) requestMap.get("keyword");       //检索框
        String status = (String) requestMap.get("status");       //状态
        String group = (String) requestMap.get("groupId");       //组id
        String typeId = (String) requestMap.get("typeId");   //应用类型id
        List<String> label = (List<String>) requestMap.get("label");  //应用标签
        String userId = (String) requestMap.get("user_id");  //行内应用负责人id/应用负责人id
        String system = (String) requestMap.get("system");  //系统id
        List<String> groupIds = null;
        List<Map> groupInfos = null;
        //查询父子组
        if (!CommonUtils.isNullOrEmpty(group)) {
            groupInfos = this.appEntityCache.queryChildGroupById(group);
        }
        if (!CommonUtils.isNullOrEmpty(groupInfos)) {
            groupIds = new ArrayList<String>();
            for (Map map : groupInfos) {
                groupIds.add((String) map.get("id"));
            }
        }
        List<AppEntity> query = new ArrayList<>();
        Integer start = pageSize * (currentPage - 1);   //起始
        query = this.appEntityDao.queryPagination(start, pageSize, keyword, status, groupIds, typeId, label, userId, system);    //分页查询
        HashSet<Map<String, String>> spdb_managers = new HashSet<>();
        HashSet<Map<String, String>> dev_managers = new HashSet<>();
        List<Map> appMap = new ArrayList<>();
        String idkey = "";
        String userNameENKey = "";
        String key = "";
        String keys = "";
        for (AppEntity entity : query) {
            List<String> ids = new ArrayList<>();
            List<String> labelName = new ArrayList<>();
            List<String> userNameENs = new ArrayList<>();
            Map<String, List<String>> param = new HashMap<String, List<String>>();
            spdb_managers = (HashSet<Map<String, String>>) entity.getSpdb_managers();
            List<Object> list = valdate(spdb_managers, ids, idkey, userNameENs, userNameENKey);
            dev_managers = (HashSet<Map<String, String>>) entity.getDev_managers();
            List<Object> lists = valdate(dev_managers, (List) list.get(0), (String) list.get(1), (List) list.get(2), (String) list.get(3));
            ids = (List) lists.get(0);
            userNameENs = (List) lists.get(2);
            if (ids.size() == 0) {
                keys = (String) lists.get(3);
                key = "usernameEns";
                param.put(key, userNameENs);
            } else {
                keys = (String) lists.get(1);
                key = Dict.IDS;
                param.put(key, ids);
            }
            Map<String, Map> params = this.appEntityCache.getUsersByIdsOrUserNameEN(keys, param);
            spdb_managers = updateManagers(spdb_managers, key, params);
            entity.setSpdb_managers(spdb_managers);
            dev_managers = updateManagers(dev_managers, key, params);
            entity.setDev_managers(dev_managers);
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装组信息
            String group_id = (String) map.get(Dict.GROUP);
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo);
            }
            //组装类型名称
            AppType appType = appTypeDao.findById((String) map.get(Dict.TYPE_ID));
            if (appType != null) {
                map.put(Dict.TYPE_NAME, appType.getName());
            }
            //组装系统名
            if (!CommonUtils.isNullOrEmpty(entity.getSystem())) {
                AppSystem appSystem = new AppSystem();
                appSystem.setId(entity.getSystem());
                List<AppSystem> appSystemlist = systemService.getSystem(appSystem);
                if (!CommonUtils.isNullOrEmpty(appSystemlist)) {
                    map.put("systemName_cn", appSystemlist.get(0).getName());
                }
            }
            appMap.add(map);
            idkey = "";
            userNameENKey = "";
            key = "";
        }
        Long count;
        count = this.appEntityDao.findAppListCount(keyword, status, groupIds, typeId, label, userId, system);
        Map map = new HashMap();
        map.put("appEntity", appMap);
        map.put("count", count);
        return map;
    }

    @Override
    public AppEntity updateForEnv(AppEntity appEntity) throws Exception {
        //对废弃应用不做处理
        AppEntity entity1 = appEntityDao.findById(appEntity.getId());
        if (null != entity1.getStatus() && entity1.getStatus().equals("0")) {
            return appEntity;
        }
        AppEntity entity = this.appEntityDao.updateForEnv(appEntity);
        logger.info("appUpdateAfter:" + entity);
        String id = entity.getId();
        String nameEn = entity.getName_en();
        //根据应用ID进行缓存清理.
        this.appEntityCache.resetAppEntityByNameEN(id);
        this.appEntityCache.resetAppEntityByNameEN(nameEn);
        return entity;
    }

    @Override
    public List<AppEntity> getNoInvolveEnvApp() {
        return this.appEntityDao.queryNoInvolveEnvApp();
    }

    /**
     * 给应用绑定实体
     *
     * @param systemId
     * @param appId
     * @return
     */
    @Override
    public void bindAppSystem(String systemId, String appId) throws Exception {
        this.appEntityDao.updateSystem(systemId, appId);
        //根据应用ID进行缓存清理.
        this.appEntityCache.resetAppEntityByNameEN(appId);
    }

    @Override
    public List<String> queryAppTasks(String appId) throws Exception {
        // 调用任务模块接口查询是否存在任务
        Map paramReq = new HashMap();
        paramReq.put(Dict.PROJECT_ID, appId);
        paramReq.put(Dict.REST_CODE, "queryTaskIdsByProjectId");
        Object responseData = this.restTransport.submit(paramReq);
        List<String> taskIds = new ArrayList<>();
        if (null != responseData) {
            taskIds = (List<String>) responseData;
        }
        return taskIds;
    }

    /**
     * 获取应用isTest属性
     *
     * @param appId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getTestFlag(String appId) throws Exception {
        AppEntity app = this.appEntityDao.findById(appId);
        Map<String, Object> result = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(app.getIsTest()))
            app.setIsTest("1");
        result.put("isTest", app.getIsTest());
        return result;
    }

    /**
     * 根据gitlab project id 来查询应用，并根据ciType来返回返回值
     *
     * @param projectId
     * @return
     */
    @Override
    public String getResultCodeForCiType(Integer projectId) {
        AppEntity app = this.appEntityDao.getAppByGitlabId(projectId);
        if (CommonUtils.isNullOrEmpty(app)) {
            logger.error("该projectId查询的应用不存在");
            return "201";
        }
        String appCiType = app.getAppCiType();
        if (!CommonUtils.isNullOrEmpty(appCiType) && appCiType.equals("fdev-ci")) {
            return "204";
        } else
            return "201";
    }

    @Override
    public Map<String, Object> queryDutyAppBaseInfo() throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(Dict._USER);
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map<String, Object> result = this.appEntityDao.queryduty(0, 0, user.getId(), "", "");
        return result;
    }

    @Override
    public Map getAppByNameEns(Map requestParam) throws Exception {
        List params = (List) requestParam.get("nameEns");
        List<AppEntity> query = appEntityDao.getAppByNameEns(params);
        Map<String, Object> appMap = new HashMap<String, Object>();
        for (AppEntity entity : query) {

            Map<String, Object> map = new HashMap<String, Object>();
            //组装组信息
            String group_id = entity.getGroup();
            Map<String, Object> groupInfo = appEntityCache.queryByGroupId(group_id);
            if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                map.put(Dict.GROUP, groupInfo.get("name"));
            }
            map.put("dev_managers", entity.getDev_managers());
            map.put("spdb_managers", entity.getSpdb_managers());
            appMap.put(entity.getName_en(), map);
        }
        return appMap;
    }

	@Override
	public void autoDeploy() throws Exception {
		List<Map> groupsList = new ArrayList<Map>(); 
		String [] group = groups.split(",");
		if(!Util.isNullOrEmpty(group)) {
			for(String id : group) {
				List<Map> groupList = appEntityCache.queryChildGroupById(id);
				groupsList.addAll(groupList);
			}
		}
		
		List<AppEntity> appEntityList = new ArrayList<AppEntity>();
		if(!Util.isNullOrEmpty(groupsList)) {
			for (Iterator iterator = groupsList.iterator(); iterator.hasNext();) {
				Map groupMap = (Map) iterator.next();
				String groupId = (String) groupMap.get("id");
				List<AppEntity> groupApps = appEntityDao.queryByGroupId(groupId);
				appEntityList.addAll(groupApps);
			}
		}
		
        if (!Util.isNullOrEmpty(appEntityList)) {
            appEntityList.forEach(appEntity -> {
                try {
                    asyncService.scheduleJob(appEntity);
                } catch (Exception e) {
                    logger.info("scheduleJob error appName=" + appEntity.getName_en());
                }
            });
        }
	}


}

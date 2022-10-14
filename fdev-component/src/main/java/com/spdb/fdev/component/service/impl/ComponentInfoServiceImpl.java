package com.spdb.fdev.component.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.dict.MpassComEnum;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.base.utils.validate.ValidateComponent;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class ComponentInfoServiceImpl implements IComponentInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ComponentInfoServiceImpl.class);

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${python.maven.path}")
    private String maven_path;

    @Value("${gitlab.component.group}")
    private String gitlab_group;

    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IComponentIssueDao componentIssueDao;

    @Autowired
    private IMpassReleaseIssueDao mpassReleaseIssueDao;

    @Autowired
    private ITagRecordDao tagRecordDao;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IComponentRecordHistoryLogDao componentRecordHistoryLogDao;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IGitlabUserService gitlabUserService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private ICommonInfoService commonInfoService;

    @Autowired
    private IMpassComponentDao iMpassComponentDao;

    @Autowired
    private IArchetypeInfoDao iArchetypeInfoDao;

    @Autowired
    private IMpassArchetypeDao iMpassArchetypeDao;

    @Autowired
    private IBaseImageInfoDao iBaseImageInfoDao;

    @Autowired
    private ComponentIssueServiceImpl componentIssueService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    @Autowired
    private IMpassArchetypeService mpassArchetypeService;

    @Autowired
    private IBaseImageInfoService baseImageInfoService;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IArchetypeRecordService archetypeRecordService;

    @Autowired
    private IMpassRelaseIssueService iMpassRelaseIssueService;

    @Override
    public List query(ComponentInfo componentInfo) throws Exception {
        return componentInfoDao.query(componentInfo);
    }


    /**
     * 录入组件信息
     * 根据groupId和artifactId搜索maven仓库中所有组件版本，记录入库
     * 定制组件持续集成模版，录入组件时间，同步加入.gitlabci文件
     * 新增组件时异步扫描应用使用组件情况，新增组件应用关系表
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public ComponentInfo save(Map<String, Object> map) throws Exception {
        HashSet<Map<String, String>> manager_id = new HashSet<>((ArrayList<HashMap<String, String>>) map.get("manager_id"));
        map.remove(Dict.MANAGER_ID);
        ComponentInfo componentInfo = CommonUtils.map2Object(map, ComponentInfo.class);
        componentInfo.setManager_id(manager_id);
//        ComponentInfo componentInfo = CommonUtils.map2Object(map, ComponentInfo.class);
        //校验用户权限 必须为基础架构负责人
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人"});
        }
        //判断当前组件中文名和英文名是否存在
        String root_dir = componentInfo.getRoot_dir();
        if (!CommonUtils.isNullOrEmpty(root_dir)) {
            if (root_dir.startsWith("/")) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{root_dir, "root_dir字段不能以/开头"});

            }
        }
        List<ComponentInfo> query = this.query(new ComponentInfo());
        ValidateComponent.checkAppNameEnAndNameZh(query, componentInfo);
        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, componentInfo.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{componentInfo.getName_en(), "当前组件英文名不符合规范！"});
        }
        //判断当前组件的groupId和Artifactid是否已存在
        ComponentInfo info = componentInfoDao.queryByGroupIdAndArtifact(componentInfo.getGroupId(), componentInfo.getArtifactId());
        if (info != null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"groupId Artifactid", "当前组件groupId和Artifactid已存在"});
        }
        //对gitlaburl进行校验，不需要.git
        String gitlabUrl = componentInfo.getGitlab_url();
        if (StringUtils.isNotBlank(gitlabUrl) && gitlabUrl.endsWith(".git") && gitlabUrl.length() > 4) {
            componentInfo.setGitlab_url(gitlabUrl.substring(0, gitlabUrl.length() - 4));
        }
        //组内维护组件
        if (Constants.INNER_SOURCE.equals(componentInfo.getSource())) {
            //判断当前组件是否在gitlab上存在
            String gitlabId = gitlabSerice.queryProjectIdByUrl(componentInfo.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{componentInfo.getGitlab_url()});
            }
            componentInfo.setGitlab_id(gitlabId);

            //若组件类型为多模块组件，则需加mpass组件
            if (componentInfo.getType().equals(Constants.MULTI_COMPONENT)) {
                gitlabSerice.addProjectHook(gitlabId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_MPASS_COMPONENT);
            } else {
                gitlabSerice.addProjectHook(gitlabId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_COMPONENT);
                commonInfoService.continueIntegration(gitlabId, componentInfo.getName_en(), Constants.COMPONENT_COMPONENT);//持续集成
            }
            //给组件负责人添加mainter权限
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(componentInfo.getManager_id(), gitlabId, Dict.MAINTAINER));
        }
        componentInfo.setCreate_date(Util.simdateFormat(new Date()));//设置创建时间
        componentInfo.setComponent_type("back");
        ComponentInfo result = componentInfoDao.save(componentInfo);

        //第三方组件
        if (Constants.THIRD_PARTY_SOURCE.equals(componentInfo.getSource())) {
            String recommond_version = (String) map.get("recommond_version");
            if (StringUtils.isNotBlank(recommond_version)) {
                String componentId = result.getId();
                User user = CommonUtils.getSessionUser();
                ComponentRecord componentRecord = new ComponentRecord(componentId, recommond_version, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), user.getId(), Constants.PIPEPACKAGE, Constants.RECORD_RECOMMEND_TYPE);
                componentRecord.setJdk_version(componentInfo.getJdk_version());
                componentRecordService.save(componentRecord);
                ComponentRecordHistoryLog componentRecordHistoryLog = new ComponentRecordHistoryLog(componentId, recommond_version, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME)
                        , user.getUser_name_cn(), "新增推荐版本" + recommond_version, "0");
                addComponentRecordHistoryLog(componentRecordHistoryLog);
            }
        }
        componentScanService.initComponentHistory(componentInfo);//调用maven接口获取组件历史版本并入库
        componentScanService.initComponentApplication(componentInfo);//添加组件应用关系
        return result;
    }

    private void addComponentRecordHistoryLog(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception {
        componentRecordHistoryLogDao.save(componentRecordHistoryLog);
    }

    /**
     * 新增组件
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @Override
    public ComponentInfo create(ComponentInfo componentInfo) throws Exception {
        //校验用户权限 必须为基础架构负责人
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人"});
        }
        //判断当前组件中文名和英文名是否存在
        List<ComponentInfo> query = this.query(new ComponentInfo());
        ValidateComponent.checkAppNameEnAndNameZh(query, componentInfo);
        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, componentInfo.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{componentInfo.getName_en(), "当前组件英文名不符合规范！"});
        }
        //判断当前组件的groupId和Artifactid是否已存在
        ComponentInfo info = componentInfoDao.queryByGroupIdAndArtifact(componentInfo.getGroupId(), componentInfo.getArtifactId());
        if (info != null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"groupId Artifactid", "当前组件groupId和Artifactid已存在"});
        }
        // Step 1. 调用 gitlab api 创建项目
        String projectName = componentInfo.getName_en();
        String group = gitlab_group;
        logger.info("project Name is {}", projectName);
        Object project = gitlabSerice.createProject(group, projectName, this.token);
        if (project == null) {
            throw new FdevException(ErrorConstants.GITLAB_CREATE_ERROR, new String[]{projectName});
        }
        Map map = (Map) JSON.parse((String) project);
        String projectId = map.get(Dict.ID).toString();
        String http_url_to_repo = map.get(Dict.HTTP_URL_TO_REPO).toString();
        if (StringUtils.isNotBlank(http_url_to_repo)) {
            componentInfo.setGitlab_url(http_url_to_repo.substring(0, http_url_to_repo.length() - 4));
        }
        logger.info("@@@@@@ 创建 gitlabProject 成功 projectId: {}", projectId);
        componentInfo.setGitlab_id(projectId);
        componentInfo.setType(Constants.SINGLE_COMPONENT);//新增默认单模块组件
        componentInfo.setSource(Constants.INNER_SOURCE);//新增默认组内来源
        componentInfo.setCreate_date(Util.simdateFormat(new Date()));//设置创建时间
        ComponentInfo result = componentInfoDao.save(componentInfo);
        gitlabSerice.addProjectHook(projectId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_COMPONENT);
        //给组件负责人添加mainter权限
        gitlabUserService.addMembers(commonInfoService.addMembersForApp(componentInfo.getManager_id(), projectId, Dict.MAINTAINER));
        commonInfoService.continueIntegration(projectId, componentInfo.getName_en(), Constants.COMPONENT_COMPONENT);//持续集成
        //调用python脚本生成maven项目，并推送到仓库，设置为异步操作
        componentScanService.initMavenArcheType(maven_path, result.getGroupId(), result.getArtifactId(), result.getName_en(), http_url_to_repo);

        return result;
    }

    @Override
    public void delete(ComponentInfo componentInfo) throws Exception {
        //校验用户权限 必须为基础架构负责人或组件管理员
        ComponentInfo info = componentInfoDao.queryById(componentInfo.getId());
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(info.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足,必须为基础架构负责人或组件管理员"});
            }
        }
        componentInfoDao.delete(componentInfo);
        //删除组件时删除历史信息
        componentRecordDao.deleteByComponentId(componentInfo.getId());
    }

    @Override
    public ComponentInfo queryById(ComponentInfo componentInfo) throws Exception {
        return componentInfoDao.queryById(componentInfo);
    }

    @Override
    public ComponentInfo queryById(String id) throws Exception {
        return componentInfoDao.queryById(id);
    }

    @Override
    public ComponentInfo queryByNameEn(String name_en) throws Exception {
        return componentInfoDao.queryByNameEn(name_en);
    }

    @Override
    public List<ComponentInfo> queryByType(String type) throws Exception {
        return componentInfoDao.queryByType(type);
    }


    @Override
    public ComponentInfo update(Map<String, Object> map) throws Exception {

        HashSet<Map<String, String>> manager_id = new HashSet<>((ArrayList<HashMap<String, String>>) map.get("manager_id"));
        map.remove(Dict.MANAGER_ID);
        ComponentInfo componentInfo = CommonUtils.map2Object(map, ComponentInfo.class);
        componentInfo.setManager_id(manager_id);

        ComponentInfo info = componentInfoDao.queryById(componentInfo.getId());
        //校验用户权限 必须为基础架构负责人或组件负责人
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(info.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或组件管理员"});
            }
        }
        //给组件负责人添加mainter权限
        if (StringUtils.isNotBlank(componentInfo.getGitlab_url())) {
            String gitlabId = gitlabSerice.queryProjectIdByUrl(componentInfo.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{componentInfo.getName_en()});
            }
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(componentInfo.getManager_id(), gitlabId, Dict.MAINTAINER));
        }
        componentInfo.setComponent_type("back");

        List<ComponentInfo> query = this.query(new ComponentInfo());
        List queryList = query.stream().filter(item -> !item.getId().equals(componentInfo.getId())).collect(Collectors.toList());
        ValidateComponent.checkAppNameEnAndNameZh(queryList, componentInfo);

        //第三方组件
        if (Constants.THIRD_PARTY_SOURCE.equals(componentInfo.getSource())) {
            String recommond_version = (String) map.get("recommond_version");
            if (StringUtils.isNotBlank(recommond_version)) {

                ComponentRecord componentRecord = componentRecordDao.queryByComponentIdAndVersion(componentInfo.getId(), recommond_version);
                if (CommonUtils.isNullOrEmpty(componentRecord)) {
                    ComponentRecord componentRecord1 = componentRecordDao.queryByType(componentInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
                    String content = "新增推荐版本" + recommond_version;
                    String type = "0";
                    if (componentRecord1 != null) {
                        componentRecord1.setType(Constants.RECORD_OFFICIAL_TYPE);
                        componentRecordDao.update(componentRecord1);
                        content = "更新推荐版本从" + componentRecord1.getVersion() + "至最新版本" + recommond_version;
                        type = "1";
                    }

                    String componentId = componentInfo.getId();
                    User user = CommonUtils.getSessionUser();
                    componentRecord1 = new ComponentRecord(componentId, recommond_version, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), user.getId(), Constants.PIPEPACKAGE, Constants.RECORD_RECOMMEND_TYPE);
                    componentRecord1.setJdk_version(componentInfo.getJdk_version());
                    componentRecordDao.save(componentRecord1);
                    ComponentRecordHistoryLog componentRecordHistoryLog = new ComponentRecordHistoryLog(componentId, recommond_version
                                        , TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), user.getUser_name_cn(), content, type);
                    addComponentRecordHistoryLog(componentRecordHistoryLog);
                }
            }
        }

        return componentInfoDao.update(componentInfo);
    }

    @Override
    public ComponentInfo queryByWebUrl(String web_url) {
        return componentInfoDao.queryByWebUrl(web_url);
    }

    @Override
    public List<ComponentInfo> queryByUserId(String user_id) throws Exception {
        return componentInfoDao.queryByUserId(user_id);
    }

    //查询近6周组件、骨架、镜像新增趋势
    @Override
    public Map<String, Object> queryDataByType(ComponentInfo componentInfo) throws ParseException {
        HashMap<String, Object> allDate = new HashMap<>();
        HashMap<String, Integer> componentNum = new HashMap<>();
        HashMap<String, Integer> iMpassComponenNum = new HashMap<>();
        HashMap<String, Integer> iArchetypeNum = new HashMap<>();
        HashMap<String, Integer> iMpassArchetypeNum = new HashMap<>();
        HashMap<String, Integer> iBaseImageNum = new HashMap<>();
        Date date = new Date();
        List<String> lastSixWeek = CommonUtils.getLastSixWeek(date);
        for (int i = 0; i <= 5; i++) {
            Integer component = componentInfoDao.queryDataByType("", lastSixWeek.get(i));
            Integer iMpassComponen = iMpassComponentDao.queryDataByType("", lastSixWeek.get(i));
            Integer iArchetype = iArchetypeInfoDao.queryDataByType("", lastSixWeek.get(i));
            Integer MpassArchetype = iMpassArchetypeDao.queryDataByType("", lastSixWeek.get(i));
            Integer iBaseImage = iBaseImageInfoDao.queryDataByType("", lastSixWeek.get(i));
            String time = lastSixWeek.get(i);
            if (i == 0) {
                time = CommonUtils.getPreviousTime(time);
            }
            componentNum.put(time, component);
            iMpassComponenNum.put(time, iMpassComponen);
            iArchetypeNum.put(time, iArchetype);
            iMpassArchetypeNum.put(time, MpassArchetype);
            iBaseImageNum.put(time, iBaseImage);
        }
        allDate.put(Dict.COMPONENTS, componentNum);
        allDate.put(Dict.MPASSCOMPONENTS, iMpassComponenNum);
        allDate.put(Dict.ARCHETYPES, iArchetypeNum);
        allDate.put(Dict.MPASSARCHETYPES, iMpassArchetypeNum);
        allDate.put(Dict.BASEIMAGE, iBaseImageNum);
        return allDate;
    }

    @Override
    public List<Object> queryAllIssue(Map requestParm) throws Exception {
        ArrayList<Object> list = new ArrayList<>();
        ComponentInfo componentInfo = new ComponentInfo();
        MpassComponent mpassComponent = new MpassComponent();
        ArchetypeInfo archetypeInfo = new ArchetypeInfo();
        MpassArchetype mpassArchetypeInfo = new MpassArchetype();
        BaseImageInfo baseImageInfo = new BaseImageInfo();
        if (Dict.ZERO.equals(requestParm.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParm.get(Dict.NAME))) {
            List componentInfos = this.query(componentInfo);
            for (int i = 0; i < componentInfos.size(); i++) {
                ComponentInfo component = (ComponentInfo) componentInfos.get(i);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put(Dict.ID, component.getId());
                hashMap.put(Dict.NAME_EN, component.getName_en());
                hashMap.put(Dict.NAME_CN, component.getName_cn());
                hashMap.put(Dict.TYPE, Constants.COMPONENT_TYPE);
                list.add(hashMap);
            }
        }
        if (Dict.ONE.equals(requestParm.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParm.get(Dict.NAME))) {
            List mpassComponents = mpassComponentService.query(mpassComponent);
            for (int i = 0; i < mpassComponents.size(); i++) {
                MpassComponent component = (MpassComponent) mpassComponents.get(i);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put(Dict.ID, component.getId());
                hashMap.put(Dict.NAME_EN, component.getName_en());
                hashMap.put(Dict.NAME_CN, component.getName_cn());
                hashMap.put(Dict.TYPE, Constants.MPASS_COMPONENT_TYPE);
                list.add(hashMap);
            }
        }
        if (Dict.TWO.equals(requestParm.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParm.get(Dict.NAME))) {
            List archetypeInfos = archetypeInfoService.query(archetypeInfo);
            for (int i = 0; i < archetypeInfos.size(); i++) {
                ArchetypeInfo archetype = (ArchetypeInfo) archetypeInfos.get(i);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put(Dict.ID, archetype.getId());
                hashMap.put(Dict.NAME_EN, archetype.getName_en());
                hashMap.put(Dict.NAME_CN, archetype.getName_cn());
                hashMap.put(Dict.TYPE, Constants.ARCHETYPE_TYPE);
                list.add(hashMap);
            }
        }
        if (Dict.THREE.equals(requestParm.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParm.get(Dict.NAME))) {
            List mpassArchetypes = mpassArchetypeService.query(mpassArchetypeInfo);
            for (int i = 0; i < mpassArchetypes.size(); i++) {
                MpassArchetype mpassArchetype = (MpassArchetype) mpassArchetypes.get(i);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put(Dict.ID, mpassArchetype.getId());
                hashMap.put(Dict.NAME_EN, mpassArchetype.getName_en());
                hashMap.put(Dict.NAME_CN, mpassArchetype.getName_cn());
                hashMap.put(Dict.TYPE, Constants.MPASS_ARCHETYPE_TYPE);
                list.add(hashMap);
            }
        }
        if (Constants.FOUR.equals(requestParm.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParm.get(Dict.NAME))) {
            List baseImages = baseImageInfoService.query(baseImageInfo);
            for (int i = 0; i < baseImages.size(); i++) {
                BaseImageInfo baseImage = (BaseImageInfo) baseImages.get(i);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put(Dict.ID, baseImage.getId());
                hashMap.put(Dict.NAME_EN, baseImage.getName());
                hashMap.put(Dict.NAME_CN, baseImage.getName_cn());
                hashMap.put(Dict.TYPE, Constants.IMAGE_TYPE);
                list.add(hashMap);
            }
        }
        return list;
    }

    @Override
    public Map defaultBranchAndVersion(String component_id, String issue_type) throws Exception {
        ComponentInfo componentInfo = queryById(component_id);
        if (componentInfo == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
        }
        String featureBranch;
        String predict_version = "";
        String version = commonScanService.getLatestVersion(componentInfo.getGroupId(), componentInfo.getArtifactId());
        if (CommonUtils.isNullOrEmpty(version)) {
            return null;
        }
        String[] split = version.split("-");
        String[] versionArray = split[0].split("\\.");
        //优化需求流程
        if (MpassComEnum.IssueTypeEnum.OPTIMIZE.getValue().equals(issue_type)) {
            if (versionArray.length == 3) {
                versionArray[1] = String.valueOf(Integer.parseInt(versionArray[1]) + 1);
                versionArray[2] = "0";
                predict_version = String.join(".", versionArray);
            }
            featureBranch = Constants.RELEASE_LOWCASE + predict_version;
        }
        //bug修复流程
        else if (MpassComEnum.IssueTypeEnum.BUGFIX.getValue().equals(issue_type)) {
            if (versionArray.length == 3) {
                versionArray[2] = String.valueOf(Integer.parseInt(versionArray[2]) + 1);
                predict_version = String.join(".", versionArray);
            }
            featureBranch = Constants.RELEASE_LOWCASE + predict_version + "_fix";
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{issue_type, "优化需求类型不存在"});
        }
        Map result = new HashMap();
        result.put(Dict.FEATURE_BRANCH, featureBranch);
        result.put(Dict.PREDICT_VERSION, predict_version);
        return result;
    }

    @Override
    public void saveMergeRequest(TagRecord tagRecord) throws Exception {
        tagRecordDao.save(tagRecord);
    }


    @Override
    public void relDevops(Map<String, String> map) throws Exception {

        String type = map.get(Dict.TYPE);
        String componentId = map.get(Dict.COMPONENT_ID);
        String branch = map.get(Dict.BRANCH);
        String target_version = map.get(Dict.TARGET_VERSION);
        String predict_version = map.get(Dict.PREDICT_VERSION);
        String desc = map.get(Dict.DESCRIPTION);
        String releaseNodeName = map.get("release_node_name");
        String gitlabId = "";
        String tag = "";
        ArrayList<Map<String, String>> variables = new ArrayList<>();

        if ("back".equals(type)) {

            ComponentInfo component = componentInfoDao.queryById(componentId);
            gitlabId = component.getGitlab_id();

            List bs = gitlabSerice.queryBranches(gitlabId);
            if (!bs.contains(branch)) {
                throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
            }

            String targetVersion = StringUtils.isNotBlank(target_version) ? target_version : predict_version;


            if ("0".equals(component.getType())) {
                tag = targetVersion + Dict._RELEASE;
            } else {
                tag = component.getName_en() + "-" + targetVersion + Dict._RELEASE;
            }
            boolean existTag = gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, tag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + tag + "已存在  "});
            }


            String root_dir = component.getRoot_dir();
            //判断root_dir是否为空。不为空放到list
            if (!CommonUtils.isNullOrEmpty(root_dir)) {
                String shellDir = "cd " + root_dir;
                HashMap<String, String> dirMap = new HashMap<>();
                dirMap.put(Dict.KEY, Dict.CDDIR);
                dirMap.put(Dict.VALUE, shellDir);
                variables.add(dirMap);
            }

            //将jdkVersion放到variables
            HashMap<String, String> jdkMap = new HashMap<>();
            jdkMap.put(Dict.KEY, Dict.JDKVERSION);
            jdkMap.put(Dict.VALUE, "3-jdk-8");
            variables.add(jdkMap);

            //将version放到variables
            String packetVersion = targetVersion + Dict._RELEASE;

            HashMap<String, String> version = new HashMap<>();
            version.put(Dict.KEY, Dict.VERSION);
            version.put(Dict.VALUE, packetVersion);
            variables.add(version);
        } else if ("mpass".equals(type)) {

            MpassComponent mpassComponent = mpassComponentService.queryById(componentId);
            gitlabId = mpassComponent.getGitlab_id();
            List bs = gitlabSerice.queryBranches(gitlabId);
            if (!bs.contains(branch)) {
                throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
            }

            tag = StringUtils.isNotBlank(target_version) ? target_version : predict_version;
            String vTag = Constants.VERSION_TAG + tag;
            //判断Tag是否已存在
            boolean existTag = gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, vTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + vTag + "已存在  "});
            }
            //检查npm版本是否存在
            List<String> versions = iMpassRelaseIssueService.getVersion(mpassComponent.getNpm_name(), mpassComponent.getNpm_group());
            if (!CommonUtils.isNullOrEmpty(versions) && versions.contains(tag)) {
                throw new FdevException(ErrorConstants.VERSION_EXISTS, new String[]{"版本: " + tag + "Npm仓库已存在  "});
            }
            // 在release分支下执行npm --no-git-tag-version version，修改版本和changelog
            gitlabSerice.updateReleaseChangeLog(mpassComponent, branch, tag);

        }

        //目标版本号不为空时，保存入库
        if (StringUtils.isNotBlank(target_version)) {
            Map<String, String> map1 = new HashMap<>();
            map1.put("component_id", componentId);
            map1.put("target_version", target_version);
            map1.put("type", type);
            map1.put("release_node_name", releaseNodeName);
            componentIssueService.saveTargetVersion(map);
        }


        //发起release分支到master的合并请求
        Object result = gitlabSerice.createMergeRequest(gitlabId, branch, Dict.MASTER, desc + "-提交准生产测试", desc, token);

        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord();
        tagRecord.setGitlab_id(gitlabId);
        tagRecord.setMerge_request_id(mergeId);
        tagRecord.setComponent_id(componentId);
        tagRecord.setTarget_version(tag);
        tagRecord.setVariables(variables);
        tagRecord.setRelease_node_name(releaseNodeName);
        tagRecordDao.save(tagRecord);
    }

    @Override
    public List<Map> getDetailByIds(Map requestMap) throws Exception {
        List params = (List) requestMap.get(Dict.IDS);
        String type = (String) requestMap.get(Dict.TYPE);
        if (CommonUtils.isNullOrEmpty(params) || CommonUtils.isNullOrEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }

        List<Map> appMap = new ArrayList<>();
        if ("componentWeb".equals(type)) {
            List<MpassComponent> list = iMpassComponentDao.getMpassComponentByIds(params);
            for (MpassComponent info : list) {
                Map map = CommonUtils.object2Map(info);
                //查询历史版本表中的推荐版本
                ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
                if (record != null) {
                    map.put(Dict.RECOMMOND_VERSION, record.getVersion());
                }
                //返回组名
                if (StringUtils.isNotBlank(info.getGroup())) {
                    Map groupMap = roleService.queryByGroupId(info.getGroup());
                    if (groupMap != null) {
                        map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                    }
                }
            }

        } else if ("componentServer".equals(type)) {
            List<ComponentInfo> list = componentInfoDao.getComponentByIds(params);
            for (ComponentInfo info : list) {
                Map map = CommonUtils.object2Map(info);
                //查询历史版本表中的推荐版本
                ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
                if (record != null) {
                    map.put(Dict.RECOMMOND_VERSION, record.getVersion());
                }
                //查询是否有父组件
                ComponentInfo parentInfo = componentInfoService.queryById(info.getParentId());
                if (parentInfo != null) {
                    map.put(Dict.PARENT_NAMEEN, parentInfo.getName_en());
                }
                //返回组名
                if (StringUtils.isNotBlank(info.getGroup())) {
                    Map groupMap = roleService.queryByGroupId(info.getGroup());
                    if (groupMap != null) {
                        map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                    }
                }
                appMap.add(map);
            }

        } else if ("archetypeWeb".equals(type)) {
            List<MpassArchetype> list = iMpassArchetypeDao.getMpassArchetypeByIds(params);
            for (MpassArchetype info : list) {
                Map map = CommonUtils.object2Map(info);
                //返回组名
                if (StringUtils.isNotBlank(info.getGroup())) {
                    Map groupMap = roleService.queryByGroupId(info.getGroup());
                    if (groupMap != null) {
                        map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                    }
                }
                appMap.add(map);
            }

        } else if ("archetypeServer".equals(type)) {
            List<ArchetypeInfo> list = iArchetypeInfoDao.getArchetypeByIds(params);
            for (ArchetypeInfo info : list) {
                Map map = CommonUtils.object2Map(info);
                //查询历史版本表中的推荐版本
                ArchetypeRecord record = archetypeRecordService.queryByArchetypeIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
                if (record != null) {
                    map.put(Dict.RECOMMOND_VERSION, record.getVersion());
                }
                //返回组名
                if (StringUtils.isNotBlank(info.getGroup())) {
                    Map groupMap = roleService.queryByGroupId(info.getGroup());
                    if (groupMap != null) {
                        map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                    }
                }
                appMap.add(map);
            }
        } else if ("image".equals(type)) {
            List<BaseImageInfo> list = iBaseImageInfoDao.getBaseImageByIds(params);
            for (BaseImageInfo info : list) {
                Map map = CommonUtils.object2Map(info);
                appMap.add(map);
            }
        }
        return appMap;


    }

    @Override
    public String queryLatestVersion(Map<String, String> map) throws Exception {
        String type = map.get("type");
        String componentId = map.get("component_id");
        String releaseNodeName = map.get("release_node_name");
        String latestVersion = "";
        if ("mpass".equals(type)) {

            MpassComponent component = mpassComponentService.queryById(componentId);
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
            }
            List<MpassReleaseIssue> issueList = mpassReleaseIssueDao.queryLatestVersion(componentId, releaseNodeName);
            if (null != issueList && issueList.size() > 0) {
                latestVersion = issueList.get(0).getPredict_version();
            }


        } else if ("back".equals(type)) {

            //根据component_id从表中查出componentInfo信息
            ComponentInfo component = componentInfoDao.queryById(componentId);
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
            }
            List<ComponentIssue> issueList = componentIssueDao.queryLatestVersion(componentId, releaseNodeName);
            if (null != issueList && issueList.size() > 0) {
                latestVersion = issueList.get(0).getTarget_version();
            }

        }
        return latestVersion;
    }

    @Override
    public List<String> queryReleaseVersion(Map<String, String> map) throws Exception {
        String type = map.get("type");
        String componentId = map.get("component_id");
        List<String> releaseVersionList = new ArrayList<String>();
        List<ComponentRecord> list = new ArrayList<ComponentRecord>();

        String regex = "";
        if ("mpass".equals(type)) {

            MpassComponent component = mpassComponentService.queryById(componentId);
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
            }

            regex = "^.*" + Constant.PATTERN + "$";
            list = componentRecordDao.queryReleaseRecordByComponentId(componentId, regex);

        } else if ("back".equals(type)) {

            //根据component_id从表中查出componentInfo信息
            ComponentInfo component = componentInfoDao.queryById(componentId);
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
            }
            regex = "^.*" + Dict._RELEASE + "$";
            list = componentRecordDao.queryReleaseRecordByComponentId(componentId, regex);

        }

        if (!CommonUtils.isNullOrEmpty(list)) {
            for (ComponentRecord componentRecord : list) {
                releaseVersionList.add(componentRecord.getVersion());
            }
        }
        return releaseVersionList;

    }

}

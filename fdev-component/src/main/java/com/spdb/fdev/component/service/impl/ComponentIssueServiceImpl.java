package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class ComponentIssueServiceImpl implements IComponentIssueService {

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${nexus.search}")
    private String nexus_search;

    private IAppService appService;

    private IComponentInfoDao componentInfoDao;

    private IComponentIssueDao componentIssueDao;

    private IGitlabSerice gitlabSerice;

    private RestTransport restTransport;

    private ITagRecordDao tagRecordDao;

    private IComponentRecordDao componentRecordDao;

    private IRoleService roleService;

    private RestTemplate restTemplate;

    private RedisTemplate redisTemplate;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;

    private ICommonScanService commonScanService;

    @Autowired
    private IGitlabSerice gitlabService;

    @Autowired
    private ComponentIssueServiceImpl componentIssueService;

    @Autowired
    private IMpassReleaseIssueDao mpassReleaseIssueDao;


    @Autowired
    private IComponentIssueDao iComponentIssueDao;

    @Autowired
    private IMpassRelaseIssueService iMpassRelaseIssueService;

    @Autowired
    private IArchetypeInfoService iArchetypeInfoService;

    @Autowired
    private IMpassArchetypeIssueService iMpassArchetypeIssueService;

    @Autowired
    private IBaseImageIssueService iBaseImageIssueService;

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;

    @Autowired
    private IArchetypeIssueService iArchetypeIssueService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    //changestage时判断是否可以修改状态
    private final List<String> stages = Arrays.asList("0", "1", "2", "3");
    //新增优化需求时判断是否有开发状态需求
    private final List<String> devstages = Arrays.asList("0", "1", "2");


    @Override
    public List query(ComponentIssue componentIssue) throws Exception {
        return componentIssueDao.query(componentIssue);
    }

    @Override
    public ComponentIssue save(Map<String, String> map) throws Exception {
        String version = map.get(Dict.TARGET_VERSION);
        if (!Pattern.matches(Constant.POM_PATTERN, version)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "版本号必须为10.10.10此种格式，长度不限"});
        }
        //将需求状态置为0
        map.put("stage", "0");
        ComponentIssue componentIssue = CommonUtils.map2Object(map, ComponentIssue.class);
        String feature_branch = componentIssue.getFeature_branch();
        //根据component_id从表中查出componentInfo信息
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setId(componentIssue.getComponent_id());
        ComponentInfo component = componentInfoDao.queryById(componentInfo);
        //如果查不到 抛错
        if (component == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
        }
        //进行版本比较，要比当前版本大
        List<ComponentIssue> issueList = this.queryAllVersion(componentIssue.getComponent_id());
        String latestVersion = null;
        if (null != issueList && issueList.size() > 0) {
            latestVersion = issueList.get(0).getTarget_version();
        }
        if (commonInfoService.isJoinCompare(latestVersion)) {
            String[] split = latestVersion.split("-");
            String[] versionList = split[0].split("\\.");
            Boolean flag = commonInfoService.compareVersion(versionList, version.split("\\."));
            if (!flag) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "目标版本号必须比" + split[0] + "大"});
            }
        }
        //根据gitlabUrl查到gitlab上的id
        String gitlabId = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        List list = gitlabSerice.queryBranches(gitlabId);
        //如果是多模块子项目  检查分支名称是否合规
        if ("2".equals(component.getType())) {
            if (!feature_branch.startsWith(component.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{feature_branch, "多模块子项目拉取的分支名应为 子项目名+版本号+-SNAPSHOT "});

            }
        }
        if (!feature_branch.endsWith(Dict._SNAPSHOT)) {
            throw new FdevException(ErrorConstants.BRANCHNAMEERROR, new String[]{feature_branch});
        }
        if (list.contains(feature_branch)) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + feature_branch + "已存在  "});
        }
        //创建分支
        gitlabSerice.createBranch(gitlabId, componentIssue.getFeature_branch(), Dict.MASTER, token);
        //给开发人员分配Developer权限
        this.gitlabUserService.addMembers(commonInfoService.addMembersForApp(componentIssue.getAssignee(), gitlabId, Dict.DEVELOPER));
        //入库
        componentIssue.setCreate_date(Util.simdateFormat(new Date()));
        return componentIssueDao.save(componentIssue);
    }

    @Override
    public void delete(ComponentIssue componentIssue) throws Exception {
        componentIssueDao.delete(componentIssue);
    }

    @Override
    public ComponentIssue queryById(ComponentIssue componentIssue) throws Exception {
        return componentIssueDao.queryById(componentIssue);
    }

    @Override
    public ComponentIssue update(ComponentIssue componentIssue) throws Exception {
        return componentIssueDao.update(componentIssue);
    }

    @Override
    public void packageTag(Map map) throws Exception {

        String component_id = (String) map.get(Dict.COMPONENT_ID);
        String jdk = (String) map.get(Dict.JDK_VERSION);
        String stage = (String) map.get(Dict.STAGE);
        String target_version = (String) map.get(Dict.TARGET_VERSION);
        String feature_branch = (String) map.get(Dict.FEATURE_BRANCH);
        String release_log = (String) map.get(Dict.RELEASE_LOG);
        String update_user = (String) map.get(Dict.UPDATE_USER);
        //判断当前是否最小开发版本，否则不可发布Rc和Release版本
        if (!"1".equals(stage)) {
            List<ComponentIssue> issueList = this.queryFirstVersion(component_id);
            if (null != issueList && issueList.size() > 0) {
                String firstVersion = issueList.get(issueList.size() - 1).getTarget_version();
                if (!target_version.equals(firstVersion)) {
                    throw new FdevException(ErrorConstants.OPTIMIZE_COMPONENT_LIMIT_ERROR, new String[]{firstVersion});
                }
            }
        }

        //根据gitlabUrl查询gitlab上的id
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setId(component_id);
        ComponentInfo component = componentInfoDao.queryById(componentInfo);
        //校验用户权限 必须为基础架构负责人或组件负责人
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(component.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或组件管理员"});
            }
        }
        String gitlabId = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        List bs = gitlabSerice.queryBranches(gitlabId);
        if (!bs.contains(feature_branch)) {
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{feature_branch});
        }
        //判断分支是否以_SNAPSHOT结尾
        if (!feature_branch.endsWith(Dict._SNAPSHOT)) {
            throw new FdevException(ErrorConstants.BRANCHNAMEERROR, new String[]{feature_branch});
        }


        ArrayList<Map<String, String>> variables = new ArrayList<>();
        String root_dir = component.getRoot_dir();
        //判断root_dir是否为空。不为空放到list
        if (!CommonUtils.isNullOrEmpty(root_dir)) {
            String shellDir = "cd " + root_dir;
            HashMap<String, String> dirMap = new HashMap<>();
            dirMap.put(Dict.KEY, Dict.CDDIR);
            dirMap.put(Dict.VALUE, shellDir);
            variables.add(dirMap);
        }

        //将jdk版本放到variables
        String jdkVersion = "";
        if ("1.8".equals(jdk)) {
            jdkVersion = "3-jdk-8";
        } else if ("1.7".equals(jdk)) {
            jdkVersion = "3-jdk7_79";
        } else {
            throw new FdevException(ErrorConstants.JDKERROR, new String[]{"jdk版本传值错误"});
        }
        HashMap<String, String> jdkMap = new HashMap<>();
        jdkMap.put(Dict.KEY, Dict.JDKVERSION);
        jdkMap.put(Dict.VALUE, jdkVersion);
        variables.add(jdkMap);
        HashMap<String, String> version = new HashMap<>();


        String tag_name = "";
        String ref = "";
        String packetVersion = "";

        String[] split = feature_branch.split(Dict._SNAPSHOT);
        if ("0".equals(stage)) {
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"开发状态不进行拉Tag打包"});
        }

        //如果状态是1，则直接在开发分支上打包
        if ("1".equals(stage)) {
            packetVersion = target_version + Dict._SNAPSHOT;
            if ("1.7".equals(jdk)) {
                packetVersion = target_version + "-jdk7" + Dict._SNAPSHOT;
            }
            ref = feature_branch;
            version.put(Dict.KEY, Dict.VERSION);
            version.put(Dict.VALUE, packetVersion);
            variables.add(version);
            Object result = gitlabSerice.createPipeline(gitlabId, ref, variables, token);
            String pipid = CommonUtils.getPipId(result);
            ComponentRecord componentRecord = new ComponentRecord(component_id, packetVersion, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), update_user, release_log, jdk, Constants.PREPACKAGE);
            componentRecord.setType(Constants.RECORD_TEST_TYPE);//如果是snapshot打的包，设置为测试版本
            componentRecord.setPipid(pipid);
            componentRecordDao.upsert(componentRecord);
            //如果状态为3，进行拉tag打包
        } else if ("3".equals(stage)) {
            packetVersion = target_version + Dict._RELEASE;
            if ("1.7".equals(jdk)) {
                packetVersion = target_version + "-jdk7" + Dict._RELEASE;
            }
            List<Map<String, Object>> projectTagsList = gitlabSerice.getProjectTagsList(gitlabId, token);
            tag_name = split[0] + Dict._RELEASE;
            version.put(Dict.KEY, Dict.VERSION);
            version.put(Dict.VALUE, packetVersion);
            variables.add(version);
            for (Map<String, Object> tagMap : projectTagsList) {
                String name = (String) tagMap.get(Dict.NAME);
                if (tag_name.equals(name)) {
                    throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + name + "已存在  "});
                }
            }
            gitlabSerice.createTags(gitlabId, tag_name, Dict.MASTER, token);
            Object result = gitlabSerice.createPipeline(gitlabId, tag_name, variables, token);
            String pipid = CommonUtils.getPipId(result);
            ComponentRecord componentRecord = new ComponentRecord(component_id, packetVersion, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), update_user, release_log, jdk, Constants.PREPACKAGE);
            componentRecord.setType(Constants.RECORD_OFFICIAL_TYPE);//如果是release打的包，设置为正式版本
            componentRecord.setPipid(pipid);
            ComponentRecord record = componentRecordDao.upsert(componentRecord);
            //存储信息到redis
            String email_content = (String) map.get(Dict.EMAIL_CONTENT);
            redisTemplate.opsForValue().set(record.getId(), email_content);
            //如果状态为2，发起开发分支合并到master分支的请求
        } else if ("2".equals(stage)) {
            Object result = gitlabSerice.createMergeRequest(gitlabId, feature_branch, Dict.MASTER, feature_branch, "RC发布阶段，开发分支合并到master", token);
            JSONObject jsonObject = JSONObject.fromObject(result);
            ObjectMapper objectMapper = new ObjectMapper();
            Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
            String mergeId = String.valueOf(merge.get(Dict.IID));
            TagRecord tagRecord = new TagRecord(component_id, gitlabId, mergeId, target_version, variables, split[0], release_log, update_user, jdk);
            tagRecordDao.save(tagRecord);

        }
    }

    @Override
    public List<String> queryBranches(Map map) throws Exception {
        String gitlabId = gitlabSerice.queryProjectIdByUrl((String) map.get(Dict.GITLAB_URL));
        return gitlabSerice.queryBranches(gitlabId);
    }

    @Override
    public void changeStage(Map<String, String> map) throws Exception {
        ComponentIssue issue = componentIssueDao.queryById(map.get(Dict.ID));
        ComponentInfo componentInfo = componentInfoDao.queryById(issue.getComponent_id());
        //校验用户权限 必须为基础架构负责人或组件管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(componentInfo.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或组件管理员"});
            }
        }
        String sta = map.get(Dict.STAGE);
        if (!stages.contains(sta)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{sta, "所传stage不在0，1，2当中"});
        }
        Integer stage = Integer.valueOf(issue.getStage());
        stage++;
        issue.setStage(String.valueOf(stage));
        componentIssueDao.update(issue);


    }

    @Override
    public Object queryIssueRecord(Map<String, String> map) throws Exception {
        String componentId = map.get(Dict.COMPONENT_ID);
        String targetVersion = map.get(Dict.TARGET_VERSION);
        String stage = map.get(Dict.STAGE);

        String tagName = "";
        switch (stage) {
            case "1":
                tagName = targetVersion + ".*" + Dict._SNAPSHOT;
                break;
            case "2":
                tagName = targetVersion + ".*" + Dict._RC + ".*";
                break;
            case "3":
                tagName = targetVersion + ".*" + Dict._RELEASE;
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage, "状态stage值异常"});
        }

        List list = componentRecordDao.queryRecordByComponentIdAndVersion(componentId, tagName);
        return list;
    }

    public ComponentRecord queryReleaseLog(String componentId, String targetVersion) {
        ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(componentId, targetVersion + Dict._SNAPSHOT);
        if (record == null) {
            return componentRecordDao.queryByComponentIdAndVersion(componentId, targetVersion + "-jdk7" + Dict._SNAPSHOT);
        } else {
            return record;
        }
    }

    @Override
    public List<ComponentIssue> queryFirstVersion(String component_id) {
        return componentIssueDao.queryDevIssues(component_id);
    }


    @Override
    public void destroyComponentIssue(String id) throws Exception {
        ComponentIssue componentIssue = componentIssueDao.queryById(id);
        if (ComponentEnum.ComponentIssueStageEnum.PASSED.getValue().equals(componentIssue.getStage())) {
            //如果属于已完成阶段，则不能删除当前issue
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"已完成优化需求不能删除"});
        }
        String targetVersion = componentIssue.getTarget_version();
        String feature_branch = componentIssue.getFeature_branch();
        ComponentInfo componentInfo = componentInfoDao.queryById(componentIssue.getComponent_id());
        String gitlabId = gitlabSerice.queryProjectIdByUrl(componentInfo.getGitlab_url());
        if (gitlabSerice.checkBranchOrTag(token, gitlabId, Dict.BRANCH, feature_branch)) {
            //将原有开发分支拉取备份分支
            gitlabSerice.createBranch(gitlabId, feature_branch + "-" + Util.getTimeStamp(new Date()) + "-destroyed", feature_branch, token);
            //删除原有分支
            gitlabSerice.deleteBranch(gitlabId, feature_branch, token);
        }
        //删除内测阶段数据，只有一条
        String snapShot = targetVersion + ".*" + Dict._SNAPSHOT;
        List snapShotList = componentRecordDao.queryRecordForDestroy(componentIssue.getComponent_id(), snapShot);
        if (!CommonUtils.listIsNullOrEmpty(snapShotList)) {
            ComponentRecord record = (ComponentRecord) snapShotList.get(0);
            //删除历史记录
            componentRecordDao.delete(record);
        }
        //删除候选发布阶段数据
        String rc = targetVersion + ".*" + Dict._RC + ".*";
        List rcList = componentRecordDao.queryRecordForDestroy(componentIssue.getComponent_id(), rc);
        if (!CommonUtils.listIsNullOrEmpty(rcList)) {
            for (int i = 0; i < rcList.size(); i++) {
                ComponentRecord record = (ComponentRecord) rcList.get(i);
                //需要删除的tag
                String[] split = feature_branch.split(Dict._SNAPSHOT);
                String[] rcSplit = record.getVersion().split("-");
                String tagName = split[0] + "-" + rcSplit[rcSplit.length - 1];
                if (gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, tagName)) {
                    gitlabSerice.deleteTag(gitlabId, tagName, token);
                }
                //删除历史记录
                componentRecordDao.delete(record);
            }
        }
        //删除当前issue
        componentIssueDao.delete(componentIssue);
    }

    @Override
    public List<HashMap<String, Object>> queryQrmntsData(Map requestParam) throws Exception {
        List user_ids = (List) requestParam.get(Dict.USER_IDS);
        List<Map> results = null;
        List<HashMap<String, Object>> componentResult = new ArrayList<>();
        for (Object userId : user_ids) {
            //获取开发人员信息
            Map<String, Object> userName = roleService.queryUserbyid((String) userId);
            String user_name_cn = null;
            HashMap<String, Object> developerInfo = new HashMap<>();
            if (userName != null) {
                user_name_cn = (String) userName.get(Dict.USER_NAME_CN);
            }
            developerInfo.put(Dict.ID, userId);
            developerInfo.put(Dict.USER_NAME_CN, user_name_cn);
            //组装组件、骨架、镜像信息
            results = iComponentIssueDao.queryQrmntsData((String) userId);
            List<HashMap<String, Object>> iMpassRelaseIssue = iMpassRelaseIssueService.queryQrmntsData(developerInfo);
            List<HashMap<String, Object>> iArchetypeInfos = iArchetypeInfoService.queryQrmntsData(developerInfo);
            List<HashMap<String, Object>> iMpassArchetypeIssueInfos = iMpassArchetypeIssueService.queryQrmntsData(developerInfo);
            List<HashMap<String, Object>> iBaseImageIssueInfos = iBaseImageIssueService.queryQrmntsData(developerInfo);
            for (Map doc : results) {
                HashMap<String, Object> componentMap = new HashMap<>();
                ComponentInfo componentInfoList = (ComponentInfo) (doc.get(Dict.COMPONENT_INFO));
                String name_en = componentInfoList.getName_en();
                HashSet<Map<String, String>> manager_id = componentInfoList.getManager_id();
                switch ((String) doc.get(Dict.STAGE)) {
                    case Dict.ZERO:
                        componentMap.put(Dict.STAGE, Constants.DEV);
                        break;
                    case Dict.ONE:
                        componentMap.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.ALPHA.getName());
                        break;
                    case Dict.TWO:
                        componentMap.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RC.getName());
                        break;
                    case Dict.THREE:
                        componentMap.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RELEASE.getName());
                        break;
                    case Constants.FOUR:
                        componentMap.put(Dict.STAGE, Constants.DEV_END);
                        break;
                    default:
                        continue;
                }
                componentMap.put(Dict.PREDICT_VERSION, doc.get(Dict.PREDICT_VERSION));
                componentMap.put(Dict.FEATURE_BRANCH, doc.get(Dict.FEATURE_BRANCH));
                componentMap.put(Dict.RECOMMOND_VERSION, doc.get(Dict.TARGET_VERSION));
                componentMap.put(Dict.ID, componentInfoList.getId());
                componentMap.put(Dict.RQRMNTS_ID, doc.get(Dict.ID));
                componentMap.put(Dict.NAME, name_en);
                componentMap.put(Dict.RQRMNTS_ADMIN, manager_id);
                componentMap.put(Dict.RQRMNTS_NAME, doc.get(Dict.TITLE));
                componentMap.put(Dict.DEVELOP, developerInfo);
                componentMap.put(Dict.DUE_DATE, doc.get(Dict.DUE_DATE));
                componentMap.put(Dict.DESC, doc.get(Dict.DESC));
                componentMap.put(Dict.TYPE, Constants.COMPONENT_TYPE);
                componentResult.add(componentMap);
            }
            for (HashMap<String, Object> stringObjectHashMap : iMpassRelaseIssue) {
                componentResult.add(stringObjectHashMap);
            }
            for (HashMap<String, Object> iAchetypeInfo : iArchetypeInfos) {
                componentResult.add(iAchetypeInfo);
            }
            for (HashMap<String, Object> iMpassArchetypeIssueInfo : iMpassArchetypeIssueInfos) {
                componentResult.add(iMpassArchetypeIssueInfo);
            }
            for (HashMap<String, Object> iBaseImageIssueInfo : iBaseImageIssueInfos) {
                componentResult.add(iBaseImageIssueInfo);
            }
        }
        return componentResult;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) throws Exception {
        List<Map> issueDelayInfo = new ArrayList<>();
        if (Constants.MPASS_COMPONENT_TYPE.equals(requestParam.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParam.get(Dict.NAME))) {
            List<Map> mpassRelaseIssue = mpassRelaseIssueService.queryIssueDelay(requestParam);
            issueDelayInfo.addAll(mpassRelaseIssue);
        }
        if (Constants.ARCHETYPE_TYPE.equals(requestParam.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParam.get(Dict.NAME))) {
            List<Map> iArchetypeIssue = iArchetypeIssueService.queryIssueDelay(requestParam);
            issueDelayInfo.addAll(iArchetypeIssue);
        }
        if (Constants.IMAGE_TYPE.equals(requestParam.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParam.get(Dict.NAME))) {
            List<Map> iBaseImageIssue = iBaseImageIssueService.queryIssueDelay(requestParam);
            issueDelayInfo.addAll(iBaseImageIssue);
        }
        if (Constants.COMPONENT_TYPE.equals(requestParam.get(Dict.NAME)) || CommonUtils.isNullOrEmpty(requestParam.get(Dict.NAME))) {
            List<Map> issueDelay = iComponentIssueDao.queryIssueDelay(requestParam);
            for (Map issueInfo : issueDelay) {
                String targetVersion = (String) issueInfo.get(Dict.TARGET_VERSION);
                String stage = (String) issueInfo.get(Dict.STAGE);//当前状态
                String componentId = (String) issueInfo.get(Dict.COMPONENT_ID);
                String tagName = "";
                switch (stage) {
                    case Dict.ZERO:
                        break;
                    case Dict.ONE:
                        tagName = targetVersion + ".*" + Dict._SNAPSHOT;
                        break;
                    case Dict.TWO:
                        tagName = targetVersion + ".*" + Dict._RC + ".*";
                        break;
                    case Dict.THREE:
                        tagName = targetVersion + ".*" + Dict._RELEASE;
                        break;
                    default:
                        continue;
                }
                List list = componentRecordDao.queryRecordByComponentIdAndVersion(componentId, tagName);
                ComponentRecord componentRecord = null;
                String date = null;
                for (int i = 0; i < list.size(); i++) {
                    componentRecord = (ComponentRecord) list.get(list.size() - 1);
                    date = componentRecord.getDate();
                }
                Map delayDays = new LinkedHashMap();
                String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PARSE);
                switch (stage) {
                    // alpha延期
                    case Dict.ZERO:
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                        delayDays.put(Dict.STAGE, Constants.DEV);
                        break;
                    //RC延期
                    case Dict.ONE:
                        //未发包
                        if (componentRecord == null) {
                            delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                            delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.ALPHA.getName());
                            break;
                        }
                        //已发包
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), date));
                        delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.ALPHA.getName());
                        break;
                    //release延期
                    case Dict.TWO:
                        if (componentRecord == null) {
                            delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                            delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RC.getName());
                            break;
                        }
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), date));
                        delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RC.getName());
                        break;
                    //进入release未发布延期
                    case Dict.THREE:
                        if (componentRecord == null) {
                            delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                            delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RELEASE.getName());
                            break;
                        }
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), date));
                        delayDays.put(Dict.STAGE, ComponentEnum.ComponentIssueStageEnum.RELEASE.getName());
                        break;
                    default:
                        continue;
                }
                //获取开发人员信息
                String devloperId = (String) issueInfo.get(Dict.ASSIGNEE);
                Map<String, Object> userName = roleService.queryUserbyid(devloperId);
                HashMap<String, Object> developerInfo = new HashMap<>();
                if (userName != null) {
                    developerInfo.put(Dict.ID, userName.get(Dict.ID));
                    developerInfo.put(Dict.USER_NAME_CN, userName.get(Dict.USER_NAME_CN));
                }
                ComponentInfo componentInfo = (ComponentInfo) issueInfo.get(Dict.COMPONENT_INFO);
                delayDays.put(Dict.NAME, componentInfo.getName_cn());
                delayDays.put(Dict.ID, componentInfo.getId());
                delayDays.put(Dict.RQRMNTS_ADMIN, componentInfo.getManager_id());
                delayDays.put(Dict.RQRMNTS_ID, issueInfo.get(Dict.ID));
                delayDays.put(Dict.RQRMNTS_NAME, issueInfo.get(Dict.TITLE));
                delayDays.put(Dict.DEVELOP, developerInfo);
                delayDays.put(Dict.FEATURE_BRANCH, issueInfo.get(Dict.FEATURE_BRANCH));
                delayDays.put(Dict.DUE_DATE, issueInfo.get(Dict.DUE_DATE));
                delayDays.put(Dict.TYPE, Constants.COMPONENT_TYPE);
                issueDelayInfo.add(delayDays);
            }
        }
        for (int i = 0; i < issueDelayInfo.size(); i++) {
            Map<String, Object> issueMap = issueDelayInfo.get(i);
            if (CommonUtils.isNullOrEmpty(issueMap.get(Dict.DELAY_DATE))) {
                issueDelayInfo.remove(i);
                i--;
            }
        }
        return issueDelayInfo;
    }


    @Override
    public void judgeTargetVersion(Map<String, String> map) throws Exception {
        String type = map.get(Dict.TYPE);
        String version = map.get(Dict.TARGET_VERSION);
        String componentId = map.get(Dict.COMPONENT_ID);
        if ("back".equals(type)) {

            if (!Pattern.matches(Constant.PATTERN, version)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "正确格式:x.x.x, 只能输入非0开头的数字,如:10.12.1"});
            }
            //根据component_id从表中查出componentInfo信息
            ComponentInfo component = componentInfoDao.queryById(componentId);
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
            }

            //进行版本比较，要比当前版本大
            List<ComponentIssue> issueList = this.queryAllVersion(componentId);
            String latestVersion = null;
            if (null != issueList && issueList.size() > 0) {
                latestVersion = issueList.get(0).getTarget_version();
            }

            if (commonInfoService.isJoinCompare(latestVersion)) {
                String[] split = latestVersion.split("-");
                String[] versionList = split[0].split("\\.");
                Boolean flag = commonInfoService.compareVersion(versionList, version.split("\\."));
                if (!flag) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "目标版本号必须比" + split[0] + "大"});
                }
            }
        } else if ("mpass".equals(type)) {

            if (!Pattern.matches(Constant.PATTERN, version)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "正确格式:x.x.x, 只能输入非0开头的数字,如:10.12.1"});
            }

            MpassComponent component = mpassComponentService.queryById(componentId);
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
            }
            //检查npm版本是否存在
            List<String> versions = iMpassRelaseIssueService.getVersion(component.getNpm_name(), component.getNpm_group());
            if (!CommonUtils.isNullOrEmpty(versions) && versions.contains(version)) {
                throw new FdevException(ErrorConstants.VERSION_EXISTS, new String[]{"版本: " + version + "Npm仓库已存在  "});
            }

            List<MpassReleaseIssue> releaseIssueList = mpassReleaseIssueDao.queryAllReleaseIssues(componentId);

            Set versionSet = new HashSet();
            if (!CommonUtils.isNullOrEmpty(releaseIssueList)) {
                for (MpassReleaseIssue issue : releaseIssueList) {
                    versionSet.add(issue.getPredict_version());
                }
            }

            if (!CommonUtils.isNullOrEmpty(versionSet)) {
                if (versionSet.contains(version)) {
                    throw new FdevException(ErrorConstants.VERSION_EXISTS, new String[]{"版本: " + version + "已存在  "});
                }

            }
        }
    }


    @Override
    public List<ComponentIssue> queryAllVersion(String component_id) {
        return componentIssueDao.queryAllDevIssues(component_id);
    }


    /**
     * 出测试包
     *
     * @param map
     * @throws Exception
     */
    public void testPackageTag(Map<String, String> map) throws Exception {
        String componentType = map.get(Dict.TYPE);
        String predict_version = map.get(Dict.PREDICT_VERSION);
        String componentId = map.get(Dict.COMPONENT_ID);
        String branch = map.get(Dict.BRANCH);
        String packge_type = map.get("packageType");
        String target_version = map.get(Dict.TARGET_VERSION);

        ArrayList<Map<String, String>> variables = new ArrayList<>();
        String gitlabId = "";
        String gitlabUrl = "";
        if ("back".equals(componentType)) {

            //根据component_id从表中查出componentInfo信息
            ComponentInfo component = componentInfoDao.queryById(componentId);
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
            }

            gitlabId = component.getGitlab_id();
            gitlabUrl = component.getGitlab_url();

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

        } else if ("mpass".equals(componentType)) {
            if (!"SIT".equals(branch) && !branch.startsWith("feature")) {
                throw new FdevException(ErrorConstants.BRANCHNAMEERROR, new String[]{"前端组件目前只支持在feature开头的开发分支上出测试包"});
            }

            MpassComponent mpassComponent = mpassComponentService.queryById(componentId);
            if (mpassComponent == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
            }
            gitlabId = mpassComponent.getGitlab_id();
            gitlabUrl = mpassComponent.getGitlab_url();
        }

        List bs = gitlabSerice.queryBranches(gitlabId);
        if (!bs.contains(branch)) {
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
        }
        String version = StringUtils.isNotBlank(target_version) ? target_version : predict_version;

        createPipeline(packge_type, gitlabId, gitlabUrl, version, variables, componentType, branch);
        //目标版本号不为空时，保存入库
        if (StringUtils.isNotBlank(target_version)) {
            Map<String, String> map1 = new HashMap<>();
            map1.put("component_id", componentId);
            map1.put("target_version", target_version);
            map1.put("type", componentType);
            componentIssueService.saveTargetVersion(map);
        }

    }

    @Override
    public void saveTargetVersion(Map<String, String> map) throws Exception {

        String type = map.get("type");
        if ("back".equals(type)) {
            String version = map.get(Dict.TARGET_VERSION);
            map.put("stage", "4");
            if (!Pattern.matches(Constant.PATTERN, version)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "正确格式:x.x.x, 只能输入非0开头的数字,如:10.12.1"});
            }
            ComponentIssue componentIssue = CommonUtils.map2Object(map, ComponentIssue.class);

            //根据component_id从表中查出componentInfo信息
            ComponentInfo component = componentInfoDao.queryById(componentIssue.getComponent_id());
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过component_id查询组件信息不存在"});
            }
            componentIssue.setCreate_date(Util.simdateFormat(new Date()));
            componentIssueDao.save(componentIssue);

        } else if ("mpass".equals(type)) {

            map.put("predict_version", map.get("target_version"));
            map.remove("target_version");
            MpassReleaseIssue mpassReleaseIssue = CommonUtils.map2Object(map, MpassReleaseIssue.class);
            MpassComponent component = mpassComponentService.queryById(mpassReleaseIssue.getComponent_id());
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
            }
            mpassReleaseIssue.setCreate_date(Util.simdateFormat(new Date()));
            mpassReleaseIssueDao.save(mpassReleaseIssue);
        }
    }


    private void createPipeline(String packge_type, String gitlabId, String gitlabUrl, String version, List<Map<String, String>> variables, String componentType, String branch) throws Exception {


        if ("mpass".equals(componentType)) {

            HashMap<String, String> versionMap = new HashMap<>();
            versionMap.put(Dict.KEY, Dict.VERSION);
            versionMap.put(Dict.VALUE, version);
            variables.add(versionMap);

            HashMap<String, String> packgeType = new HashMap<>();
            packgeType.put(Dict.KEY, Dict.TYPE);
            packgeType.put(Dict.VALUE, packge_type);
            variables.add(packgeType);

            Object result = gitlabService.createPipeline(gitlabId, branch, variables, token);

        } else if ("back".equals(componentType)) {

            Map<String, String> map = new HashMap<String, String>();
            map.put("gitlab_url", gitlabUrl);
            map.put("type", packge_type);
            map.put("version", version);
            map.put("component_type", "back");
            String alphaorBetaVersion = mpassRelaseIssueService.getNextAlphaorBetaVersion(map);

            HashMap<String, String> versionMap = new HashMap<>();
            versionMap.put(Dict.KEY, Dict.VERSION);
            versionMap.put(Dict.VALUE, alphaorBetaVersion);
            variables.add(versionMap);

            Object result = gitlabService.createPipeline(gitlabId, branch, variables, token);
            Map<String, String> map1 = new HashMap<String, String>();
            map1.put("gitlab_url", gitlabUrl);
            map1.put("version", alphaorBetaVersion);
            map1.put("component_type", "back");
            map1.put("release_log", "组件出测试包");
            mpassRelaseIssueService.addComponentRecord(map1);
        }

    }


    @Autowired
    public void setAppService(IAppService appService) {
        this.appService = appService;
    }

    @Autowired
    public void setComponentInfoDao(IComponentInfoDao componentInfoDao) {
        this.componentInfoDao = componentInfoDao;
    }

    @Autowired
    public void setComponentIssueDao(IComponentIssueDao componentIssueDao) {
        this.componentIssueDao = componentIssueDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setRestTransport(RestTransport restTransport) {
        this.restTransport = restTransport;
    }

    @Autowired
    public void setTagRecordDao(ITagRecordDao tagRecordDao) {
        this.tagRecordDao = tagRecordDao;
    }

    @Autowired
    public void setComponentRecordDao(IComponentRecordDao componentRecordDao) {
        this.componentRecordDao = componentRecordDao;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }

    @Autowired
    public void setCommonScanService(ICommonScanService commonScanService) {
        this.commonScanService = commonScanService;
    }


}

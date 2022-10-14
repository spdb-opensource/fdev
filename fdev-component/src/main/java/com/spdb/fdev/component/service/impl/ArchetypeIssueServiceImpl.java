package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IArchetypeInfoDao;
import com.spdb.fdev.component.dao.IArchetypeIssueDao;
import com.spdb.fdev.component.dao.IArchetypeRecordDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class ArchetypeIssueServiceImpl implements IArchetypeIssueService {

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${nexus.search}")
    private String nexus_search;

    private IGitlabSerice gitlabSerice;

    //changestage时判断是否可以修改状态
    private final List<String> stages = Arrays.asList("0", "1", "2");

    //请求打包时的状态 1:SNAPSHOT打包   2：tag打包
    private final List<String> packageStages = Arrays.asList(Constants.ARCHETYPE_STAGE_ALPHA, Constants.ARCHETYPE_STAGE_RELEASE);

    private IRoleService roleService;

    private IGitlabSerice gitlabService;

    private IArchetypeIssueDao archetypeIssueDao;

    private IArchetypeInfoDao archetypeInfoDao;

    private ITagRecordDao tagRecordDao;

    private RestTemplate restTemplate;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;

    private ICommonScanService commonScanService;

    private IArchetypeRecordDao archetypeRecordDao;

    @SuppressWarnings("rawtypes")
    @Override
    public List query(ArchetypeIssue archetypeIssue) throws Exception {
        return archetypeIssueDao.query(archetypeIssue);
    }

    @Override
    public ArchetypeIssue save(Map<String, String> map) throws Exception {
        String version = map.get(Dict.TARGET_VERSION);
        if (!Pattern.matches(Constant.POM_PATTERN, version)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "版本号必须为10.10.10此种格式，长度不限"});
        }
        //将需求状态置为0
        map.put(Dict.STAGE, Constants.ARCHETYPE_STAGE_SNAPSHOT);
        ArchetypeIssue archetypeIssue = CommonUtils.map2Object(map, ArchetypeIssue.class);
        String feature_branch = archetypeIssue.getFeature_branch();
        //根据archetypeInfo_id从表中查出componentInfo信息
        ArchetypeInfo archetypeInfo = new ArchetypeInfo();
        archetypeInfo.setId(archetypeIssue.getArchetype_id());
        ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeInfo);
        //如果查不到 抛错
        if (archetype == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过archetype_id查询组件信息不存在"});
        }

        //如果maven仓库中或已经新建的优化需求，进行版本比较，要比当前版本大
        List<ArchetypeIssue> issueList = this.queryAllVersion(archetypeIssue.getArchetype_id());
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
//        String gitlabId = gitlabService.queryProjectIdByUrl(archetype.getGitlab_url());
        String gitlabId = archetype.getGitlab_id();
        List list = gitlabService.queryBranches(gitlabId);
        //检查分支名称是否以-DEV结束
        if (!feature_branch.endsWith(Dict._DEV)) {
            throw new FdevException(ErrorConstants.BRANCHNAMEERROR, new String[]{feature_branch});
        }
        if (list.contains(feature_branch)) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + feature_branch + "已存在  "});
        }
        //创建分支
        gitlabService.createBranch(gitlabId, archetypeIssue.getFeature_branch(), Dict.MASTER, token);
        //给开发人员分配Developer权限
        this.gitlabUserService.addMembers(commonInfoService.addMembersForApp(archetypeIssue.getAssignee(), gitlabId, Dict.DEVELOPER));
        //入库
        archetypeIssue.setCreate_date(Util.simdateFormat(new Date()));
        return archetypeIssueDao.save(archetypeIssue);
    }

    @Override
    public void changeStage(Map<String, String> map) throws Exception {
        ArchetypeIssue issue = archetypeIssueDao.queryById(map.get(Dict.ID));
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(issue.getArchetype_id());
        //校验用户权限 必须为基础架构负责人或骨架管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(archetypeInfo.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或骨架管理员"});
            }
        }
        String sta = map.get(Dict.STAGE);
        if (!stages.contains(sta)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{sta, "所传stage不在0，1，2当中"});
        }
        Integer stage = Integer.valueOf(issue.getStage());
        stage++;
        issue.setStage(String.valueOf(stage));
        archetypeIssueDao.update(issue);
    }

    @Override
    public void packageTag(Map map) throws Exception {
        String archetype_id = (String) map.get(Dict.ARCHETYPE_ID);
        String stage = (String) map.get(Dict.STAGE);
        String target_version = (String) map.get(Dict.TARGET_VERSION);
        String feature_branch = (String) map.get(Dict.FEATURE_BRANCH);
        String release_log = (String) map.get(Dict.RELEASE_LOG);
        String update_user = (String) map.get(Dict.UPDATE_USER);
        //判断当前是否最小开发版本，否则不可发布Rc和Release版本
        if (!"1".equals(stage)) {
            List<ArchetypeIssue> issueList = this.queryFirstVersion(archetype_id);
            if (null != issueList && issueList.size() > 0) {
                String firstVersion = issueList.get(issueList.size() - 1).getTarget_version();
                if (!target_version.equals(firstVersion)) {
                    throw new FdevException(ErrorConstants.OPTIMIZE_ARCHETYPE_LIMIT_ERROR, new String[]{firstVersion});
                }
            }
        }
        //根据gitlabUrl查询gitlab上的id
        ArchetypeInfo archetypeInfo = new ArchetypeInfo();
        archetypeInfo.setId(archetype_id);
        ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeInfo);
        //校验用户权限 必须为基础架构负责人或骨架管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(archetype.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或骨架管理员"});
            }
        }
        if (!packageStages.contains(stage)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage, "所传stage不在1，2当中"});
        }
        String gitlabId = gitlabService.queryProjectIdByUrl(archetype.getGitlab_url());
        List bs = gitlabService.queryBranches(gitlabId);
        //检查开发分支是否存在
        if (!bs.contains(feature_branch)) {
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{feature_branch});
        }
        //持续集成参数传递，groupId
        ArrayList<Map<String, String>> variables = new ArrayList<>();
        Map<String, String> groupId = new HashMap<>();
        groupId.put(Dict.KEY, Dict.GROUPID);
        groupId.put(Dict.VALUE, archetype.getGroupId());
        variables.add(groupId);
        //持续集成参数传递，artifactId
        Map<String, String> artifactId = new HashMap<>();
        artifactId.put(Dict.KEY, Dict.ARTIFACTID);
        artifactId.put(Dict.VALUE, archetype.getArtifactId());
        variables.add(artifactId);

        //持续集成参数传递，encoding
        Map<String, String> encoding = new HashMap<>();
        encoding.put(Dict.KEY, Dict.ENCODING);
        encoding.put(Dict.VALUE, archetype.getEncoding());
        variables.add(encoding);
        //SNAPSHOT分支名，开发阶段创建
        String target_branch = target_version + Dict._SNAPSHOT;
        Object result = null;
        String branch = "";
        if (Constants.ARCHETYPE_STAGE_ALPHA.equals(stage)) {
            //持续集成参数传递，version
            Map<String, String> version = new HashMap<>();
            version.put(Dict.KEY, Dict.VERSION);
            version.put(Dict.VALUE, target_version + Dict._SNAPSHOT);
            variables.add(version);
            branch = target_branch;
            //从主分支拉取新分支SNAPSHOT
            if (!bs.contains(target_branch)) {
                gitlabService.createBranch(gitlabId, target_branch, Dict.MASTER, token);
            }
            //把开发分支的代码合并到新分支 保存请求记录
            result = gitlabService.createMergeRequest(gitlabId, feature_branch, target_branch, "骨架开发分支合并", release_log, token);

        } else if (Constants.ARCHETYPE_STAGE_RELEASE.equals(stage)) {
            //持续集成参数传递，version
            Map<String, String> version = new HashMap<>();
            version.put(Dict.KEY, Dict.VERSION);
            version.put(Dict.VALUE, target_version + Dict._RELEASE);
            variables.add(version);
            //检查SNAPSHOT分支是否存在
            if (!bs.contains(target_branch)) {
                throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{target_branch});
            }
            branch = target_version + Dict._RELEASE;
            result = gitlabService.createMergeRequest(gitlabId, target_branch, Dict.MASTER, target_branch, release_log, token);
        }
        JSONObject jsonObject = JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord(archetype_id, gitlabId, mergeId, target_version, variables, branch, release_log, update_user);
        tagRecordDao.save(tagRecord);
    }

    @Override
    public void delete(ArchetypeIssue archetypeIssue) throws Exception {
        archetypeIssueDao.delete(archetypeIssue);
    }

    @Override
    public ArchetypeIssue queryById(ArchetypeIssue archetypeIssue) throws Exception {
        return archetypeIssueDao.queryById(archetypeIssue);
    }

    @Override
    public ArchetypeIssue queryByArchetypeIdAndVersion(String archetype_id, String version) {
        return archetypeIssueDao.queryByArchetypeIdAndVersion(archetype_id, version);
    }

    @Override
    public ArchetypeIssue queryById(String id) throws Exception {
        return archetypeIssueDao.queryById(id);
    }

    @Override
    public ArchetypeIssue update(ArchetypeIssue archetypeIssue) throws Exception {
        return archetypeIssueDao.update(archetypeIssue);
    }

    @Override
    public Object queryIssueRecord(Map<String, String> map) throws Exception {
        String archetype_id = map.get(Dict.ARCHETYPE_ID);
        String targetVersion = map.get(Dict.TARGET_VERSION);
        String stage = map.get(Dict.STAGE);

        String tagName;
        switch (stage) {
            case "1":
                tagName = targetVersion + ".*" + Dict._SNAPSHOT;
                break;
            case "2":
                tagName = targetVersion + ".*" + Dict._RELEASE;
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage, "状态stage值异常"});
        }

        List list = archetypeRecordDao.queryRecordByArchetypeIdAndVersion(archetype_id, tagName);
        return list;
    }

    @Override
    public ArchetypeRecord queryReleaseLog(String archetypeId, String targetVersion) {
        return archetypeRecordDao.queryByArchetypeIdAndVersion(archetypeId, targetVersion + Dict._SNAPSHOT);
    }

    @Override
    public List<ArchetypeIssue> queryFirstVersion(String archetypeId) {
        return archetypeIssueDao.queryDevIssues(archetypeId);
    }

    @Override
    public void destroyArchetypeIssue(String id) throws Exception {
        ArchetypeIssue archetypeIssue = archetypeIssueDao.queryById(id);
        if (ComponentEnum.ArchetypeIssueStageEnum.PASSED.getValue().equals(archetypeIssue.getStage())) {
            //如果属于已完成阶段，则不能删除当前issue
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"已完成优化需求不能删除"});
        }
        String targetVersion = archetypeIssue.getTarget_version();
        String feature_branch = archetypeIssue.getFeature_branch();
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetypeIssue.getArchetype_id());
        String gitlabId = gitlabSerice.queryProjectIdByUrl(archetypeInfo.getGitlab_url());
        //拉取开发分支备份，删除原开发分支
        if (gitlabSerice.checkBranchOrTag(token, gitlabId, Dict.BRANCH, feature_branch)) {
            gitlabSerice.createBranch(gitlabId, feature_branch + "-" + Util.getTimeStamp(new Date()) + "-destroyed", feature_branch, token);
            gitlabSerice.deleteBranch(gitlabId, feature_branch, token);
        }
        //删除内测阶段数据
        String snapShot = targetVersion + Dict._SNAPSHOT;
        ArchetypeRecord snapRecord = archetypeRecordDao.queryByArchetypeIdAndVersion(archetypeIssue.getArchetype_id(), snapShot);
        if (snapRecord != null) {
            //删除SNAPSHOT分支
            if (gitlabSerice.checkBranchOrTag(token, gitlabId, Dict.BRANCH, snapShot)) {
                gitlabSerice.deleteBranch(gitlabId, snapShot, token);
            }
            //删除历史记录
            archetypeRecordDao.delete(snapRecord);
        }
        //删除release阶段数据
        String release = targetVersion + Dict._RELEASE;
        ArchetypeRecord releaseRecord = archetypeRecordDao.queryByArchetypeIdAndVersion(archetypeIssue.getArchetype_id(), release);
        if (releaseRecord != null && Constants.PREPACKAGE.equals(releaseRecord.getPackagetype())) {
            //删除Release tag
            if (gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, release)) {
                gitlabSerice.deleteTag(gitlabId, release, token);
            }
            //删除历史记录
            archetypeRecordDao.delete(releaseRecord);
        }
        archetypeIssueDao.delete(archetypeIssue);
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) throws Exception {
        List<Map> issueDelayInfo = new ArrayList<>();
        List<Map> archetypeIssueInfo = archetypeIssueDao.queryIssueDelay(requestParam);
        for (Map issueInfo : archetypeIssueInfo) {
            String targetVersion = (String) issueInfo.get(Dict.TARGET_VERSION);
            String stage = (String) issueInfo.get(Dict.STAGE);
            String archetypeId = (String) issueInfo.get(Dict.ARCHETYPE_ID);
            String tagName = "";
            switch (stage) {
                case Dict.ZERO:
                    break;
                case Dict.ONE:
                    tagName = targetVersion + ".*" + Dict._SNAPSHOT;
                    break;
                case Dict.TWO:
                    tagName = targetVersion + ".*" + Dict._RELEASE;
                    break;
                default:
                    continue;
            }
            List list = archetypeRecordDao.queryRecordByArchetypeIdAndVersion(archetypeId, tagName);
            ArchetypeRecord archetypeRecord = null;
            String date = null;
            for (int i = 0; i < list.size(); i++) {
                archetypeRecord = (ArchetypeRecord) list.get(0);
                date = archetypeRecord.getDate();
            }
            Map delayDays = new LinkedHashMap();
            String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PARSE);
            switch (stage) {
                // alpha延期
                case Dict.ZERO:
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                    delayDays.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.CREATE.getName());
                    break;
                //RC延期
                case Dict.ONE:
                    //未发包
                    if (archetypeRecord == null) {
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                        delayDays.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.ALPHA.getName());
                        break;
                    }
                    //已发包
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), date));
                    delayDays.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.ALPHA.getName());
                    break;
                //release延期
                case Dict.TWO:
                    if (archetypeRecord == null) {
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                        delayDays.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.RELEASE.getName());
                        break;
                    }
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), date));
                    delayDays.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.RELEASE.getName());
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
            Object archetype_Info = issueInfo.get(Dict.ARCHETYPEINFO);
            if (CommonUtils.isNullOrEmpty(archetype_Info)) continue;
            ArchetypeInfo archetypeInfo = (ArchetypeInfo) archetype_Info;
            delayDays.put(Dict.NAME, archetypeInfo.getName_cn());
            delayDays.put(Dict.ID, archetypeInfo.getId());
            delayDays.put(Dict.RQRMNTS_ADMIN, archetypeInfo.getManager_id());
            delayDays.put(Dict.RQRMNTS_ID, issueInfo.get(Dict.ID));
            delayDays.put(Dict.RQRMNTS_NAME, issueInfo.get(Dict.TITLE));
            delayDays.put(Dict.DEVELOP, developerInfo);
            delayDays.put(Dict.FEATURE_BRANCH, issueInfo.get(Dict.FEATURE_BRANCH));
            delayDays.put(Dict.DUE_DATE, issueInfo.get(Dict.DUE_DATE));
            delayDays.put(Dict.TYPE, Constants.ARCHETYPE_TYPE);
            issueDelayInfo.add(delayDays);
        }
        return issueDelayInfo;
    }

    @Override
    public void judgeTargetVersion(Map<String, String> map) throws Exception {
        String version = map.get(Dict.TARGET_VERSION);
        String archetypeId = map.get(Dict.ARCHETYPE_ID);
        if (!Pattern.matches(Constant.PATTERN, version)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "正确格式:x.x.x, 只能输入非0开头的数字,如:10.12.1"});
        }

        //根据archetypeInfo_id从表中查出componentInfo信息
        ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeId);
        //如果查不到 抛错
        if (archetype == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过archetype_id查询组件信息不存在"});
        }

        //如果maven仓库中或已经新建的优化需求，进行版本比较，要比当前版本大
        List<ArchetypeIssue> issueList = this.queryAllVersion(archetypeId);
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
    }


    @Override
    public List<ArchetypeIssue> queryAllVersion(String archetypeId) {
        return archetypeIssueDao.queryAllDevIssues(archetypeId);
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setGitlabService(IGitlabSerice gitlabService) {
        this.gitlabService = gitlabService;
    }

    @Autowired
    public void setArchetypeIssueDao(IArchetypeIssueDao archetypeIssueDao) {
        this.archetypeIssueDao = archetypeIssueDao;
    }

    @Autowired
    public void setArchetypeInfoDao(IArchetypeInfoDao archetypeInfoDao) {
        this.archetypeInfoDao = archetypeInfoDao;
    }

    @Autowired
    public void setTagRecordDao(ITagRecordDao tagRecordDao) {
        this.tagRecordDao = tagRecordDao;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    @Autowired
    public void setArchetypeRecordDao(IArchetypeRecordDao archetypeRecordDao) {
        this.archetypeRecordDao = archetypeRecordDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }
}

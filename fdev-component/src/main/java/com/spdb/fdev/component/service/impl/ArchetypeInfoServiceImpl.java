package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.base.utils.validate.ValidateComponent;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IArchetypeInfoDao;
import com.spdb.fdev.component.dao.IArchetypeIssueDao;
import com.spdb.fdev.component.dao.IArchetypeRecordDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class ArchetypeInfoServiceImpl implements IArchetypeInfoService {

    private IArchetypeInfoDao archetypeInfoDao;

    private IRoleService roleService;

    private IGitlabSerice gitlabSerice;

    private IArchetypeScanService archetypeScanService;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;

    @Autowired
    private IArchetypeRecordDao archetypeRecordDao;

    @Autowired
    private IArchetypeIssueDao archetypeIssueDao;

    @Autowired
    private IMpassArchetypeService mpassArchetypeService;

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    private RestTransport restTransport;

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${gitlab.component.group}")
    private String gitlab_group;

    @Value("${python.maven.path}")
    private String maven_path;

    @Autowired
    private ITagRecordDao tagRecordDao;


    @Override
    public List query(ArchetypeInfo archetypeInfo) throws Exception {
        return archetypeInfoDao.query(archetypeInfo);
    }

    @Override
    public ArchetypeInfo save(ArchetypeInfo archetypeInfo) throws Exception {
        //校验用户权限 必须为基础架构负责人
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人"});
        }
        //验证中英文名是否重复
        List<ArchetypeInfo> query = this.query(new ArchetypeInfo());
        ValidateComponent.checkAppNameEnAndNameZh(query, archetypeInfo);

        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, archetypeInfo.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{archetypeInfo.getName_en(), "当前骨架英文名不符合规范！"});
        }
        //判断当前骨架的groupId和Artifactid是否已存在
        ArchetypeInfo info = archetypeInfoDao.queryByGroupIdAndArtifact(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId());
        if (info != null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"groupId Artifactid", "当前骨架groupId和Artifactid已存在"});
        }
        //对gitlaburl进行校验，不需要.git
        String gitlabUrl = archetypeInfo.getGitlab_url();
        if (StringUtils.isNotBlank(gitlabUrl) && gitlabUrl.endsWith(".git") && gitlabUrl.length() > 4) {
            archetypeInfo.setGitlab_url(gitlabUrl.substring(0, gitlabUrl.length() - 4));
        }
        String gitlabId = gitlabSerice.queryProjectIdByUrl(archetypeInfo.getGitlab_url());
        if (StringUtils.isBlank(gitlabId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{archetypeInfo.getGitlab_url()});
        }
        archetypeInfo.setGitlab_id(gitlabId);

        archetypeInfo.setCreate_date(Util.simdateFormat(new Date()));//设置创建时间
        archetypeInfo.setArchetype_type("back");
        gitlabSerice.addProjectHook(gitlabId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_ARCHETYPE);
        //给组件负责人添加mainter权限
        gitlabUserService.addMembers(commonInfoService.addMembersForApp(archetypeInfo.getManager_id(), gitlabId, Dict.MAINTAINER));
        commonInfoService.continueIntegration(gitlabId, archetypeInfo.getName_en(), Constants.COMPONENT_ARCHETYPE);//持续集成
        //异步扫描Maven仓库，获取所有历史版本
        archetypeScanService.initArchetypeHistory(archetypeInfo);
        //异步扫描骨架和应用关联关系
        archetypeScanService.initArchetypeApplication(archetypeInfo);
        ArchetypeInfo result = archetypeInfoDao.save(archetypeInfo);
        return result;
    }

    @Override
    public List<ArchetypeInfo> queryByUserId(String user_id) {
        return archetypeInfoDao.queryByUserId(user_id);
    }

    @Override
    public Object saveConfigTemplate(Map hashMap) throws Exception {
        String content = (String) hashMap.get(Dict.CONTENT);//文件内容
        String branch = (String) hashMap.get(Dict.BRANCH);//项目分支
        String archetype_id = (String) hashMap.get(Dict.ARCHETYPE_ID);
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetype_id);
        String gitlabId = gitlabSerice.queryProjectIdByUrl(archetypeInfo.getGitlab_url());
        if (StringUtils.isBlank(gitlabId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{archetypeInfo.getName_en()});
        }
        try {
            Map<String, Object> send_map = new HashMap<>();
            send_map.put(Dict.REST_CODE, Dict.SAVECONFIGTEMPLATE);
            send_map.put(Dict.CONTENT, content);// 发环境配置模块获取appliaction详细信息
            send_map.put(Dict.FEATURE_BRANCH, branch);
            send_map.put(Dict.GITLABID, gitlabId);
            return restTransport.submit(send_map);
        } catch (FdevException e) {
            throw new FdevException(ErrorConstants.MODULE_REQUEST_ERROR, new String[]{"环境配置", "保存配置模版(saveConfigTemplate)", e.getCode() + ":" + e.getMessage()});
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.MODULE_REQUEST_ERROR, new String[]{"环境配置", "保存配置模版(saveConfigTemplate)", e.getMessage()});
        }

    }

    @Override
    public String queryConfigTemplate(Map hashMap) throws Exception {
        String branch = (String) hashMap.get(Dict.BRANCH);
        String archetype_id = (String) hashMap.get(Dict.ARCHETYPE_ID);
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetype_id);
        String gitlabId = gitlabSerice.queryProjectIdByUrl(archetypeInfo.getGitlab_url());
        if (StringUtils.isBlank(gitlabId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{archetypeInfo.getName_en()});
        }
        if (StringUtils.isBlank(archetypeInfo.getApplication_path())) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"骨架环境配置文件路径未配置"});
        }
        // 判断分支
        gitlabSerice.checkBranch(token, gitlabId, branch);
        // 获取gitlab文件内容
        String applicationFile = CommonUtils.replateGitKey(archetypeInfo.getApplication_path());
        return gitlabSerice.getFileContent(this.token, gitlabId, branch, applicationFile);
    }

    @Override
    public void delete(ArchetypeInfo archetypeInfo) throws Exception {
        archetypeInfoDao.delete(archetypeInfo);
    }

    @Override
    public ArchetypeInfo queryById(ArchetypeInfo archetypeInfo) throws Exception {
        return archetypeInfoDao.queryById(archetypeInfo);
    }

    @Override
    public ArchetypeInfo queryById(String id) throws Exception {
        return archetypeInfoDao.queryById(id);
    }

    @Override
    public ArchetypeInfo queryByNameEn(String name_en) throws Exception {
        return archetypeInfoDao.queryByNameEn(name_en);
    }

    @Override
    public ArchetypeInfo queryByGroupIdAndArtifact(String groupId, String artifactId) {
        return archetypeInfoDao.queryByGroupIdAndArtifact(groupId, artifactId);
    }

    @Override
    public ArchetypeInfo queryByWebUrl(String web_url) {
        return archetypeInfoDao.queryByWebUrl(web_url);
    }

    @Override
    public List<ArchetypeInfo> queryByType(String type) throws Exception {
        return archetypeInfoDao.queryByType(type);
    }

    @Override
    public ArchetypeInfo update(ArchetypeInfo archetypeInfo) throws Exception {
        ArchetypeInfo info = archetypeInfoDao.queryById(archetypeInfo.getId());
        //校验用户权限 必须为基础架构负责人或组件负责人
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(info.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人"});
            }
        }
        //给骨架负责人添加mainter权限
        String gitlabId = archetypeInfo.getGitlab_id();
        if (StringUtils.isNotBlank(gitlabId)) {
            Map projectMap = gitlabSerice.getProjectInfo(archetypeInfo.getGitlab_id(), token);
            if (!archetypeInfo.getGitlab_url().equals(projectMap.get("web_url"))) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{archetypeInfo.getName_en()});
            }
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(archetypeInfo.getManager_id(), gitlabId, Dict.MAINTAINER));
        }
        archetypeInfo.setArchetype_type("back");
        return archetypeInfoDao.update(archetypeInfo);
    }

    @Autowired
    public void setRestTransport(RestTransport restTransport) {
        this.restTransport = restTransport;
    }

    @Autowired
    public void setArchetypeInfoDao(IArchetypeInfoDao archetypeInfoDao) {
        this.archetypeInfoDao = archetypeInfoDao;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setArchetypeScanService(IArchetypeScanService archetypeScanService) {
        this.archetypeScanService = archetypeScanService;
    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }

    @Override
    public List<HashMap<String, Object>> queryQrmntsData(Map requestParam) throws Exception {
        String user_id = (String) requestParam.get(Dict.ID);
        List<Map> results = archetypeInfoDao.queryQrmntsData(user_id);
        List<HashMap<String, Object>> archetypeResult = new ArrayList<>();
        for (Map doc : results) {
            HashMap<String, Object> archetypeMap = new HashMap<>();
            switch ((String) doc.get(Dict.STAGE)) {
                case Dict.ZERO:
                    archetypeMap.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.CREATE.getName());
                    break;
                case Dict.ONE:
                    archetypeMap.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.ALPHA.getName());
                    break;
                case Dict.TWO:
                    archetypeMap.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.RELEASE.getName());
                    break;
                case Dict.THREE:
                    archetypeMap.put(Dict.STAGE, ComponentEnum.ArchetypeIssueStageEnum.PASSED.getName());
                    break;
                default:
                    continue;
            }
            ArchetypeInfo ArchetypeInfo = (ArchetypeInfo) (doc.get(Dict.ARCHETYPEINFO));
            HashSet<Map<String, String>> manager_id = ArchetypeInfo.getManager_id();
            archetypeMap.put(Dict.PREDICT_VERSION, doc.get(Dict.PREDICT_VERSION));
            archetypeMap.put(Dict.FEATURE_BRANCH, doc.get(Dict.FEATURE_BRANCH));
            archetypeMap.put(Dict.RECOMMOND_VERSION, doc.get(Dict.TARGET_VERSION));
            archetypeMap.put(Dict.ID, ArchetypeInfo.getId());
            archetypeMap.put(Dict.RQRMNTS_ID, doc.get(Dict.ID));
            archetypeMap.put(Dict.NAME, ArchetypeInfo.getName_en());
            archetypeMap.put(Dict.RQRMNTS_ADMIN, manager_id);
            archetypeMap.put(Dict.RQRMNTS_NAME, doc.get(Dict.TITLE));
            archetypeMap.put(Dict.DEVELOP, requestParam);
            archetypeMap.put(Dict.DUE_DATE, doc.get(Dict.DUE_DATE));
            archetypeMap.put(Dict.DESC, doc.get(Dict.DESC));
            archetypeMap.put(Dict.TYPE, Constants.ARCHETYPE_TYPE);
            archetypeResult.add(archetypeMap);
        }
        return archetypeResult;
    }

    @Override
    public void relDevops(Map<String, String> map) throws Exception {
        String type = map.get(Dict.TYPE);
        String archetypeId = map.get(Dict.ARCHETYPE_ID);
        String branch = map.get(Dict.BRANCH);
        String predict_version = map.get(Dict.PREDICT_VERSION);
        String target_version = map.get(Dict.TARGET_VERSION);
        String releaseNodeName = map.get("release_node_name");
        String desc = map.get(Dict.DESCRIPTION);
        String gitlabId = "";
        ArrayList<Map<String, String>> variables = new ArrayList<>();
        Object result = null;
        String targetVersion="";

        if ("back".equals(type)) {

            ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeId);
            if (null == archetype) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前骨架不存在"});
            }

            gitlabId = archetype.getGitlab_id();
            List bs = gitlabSerice.queryBranches(gitlabId);
            if (!bs.contains(branch)) {
                throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
            }


            targetVersion = StringUtils.isNotBlank(target_version) ? target_version : predict_version;

            String newTag = targetVersion + Dict._RELEASE;
            boolean existTag = gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, newTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + newTag + "已存在  "});
            }
            //发起release分支到master的合并请求
             result = gitlabSerice.createMergeRequest(gitlabId, branch, Dict.MASTER, desc + "-提交准生产测试", desc, token);

            //目标版本号不为空时，保存入库
            if (StringUtils.isNotBlank(target_version)) {
                Map<String, String> map1 = new HashMap<>();
                map1.put("archetype_id", archetypeId);
                map1.put("target_version", target_version);
                map1.put("release_node_name",releaseNodeName);
                archetypeInfoService.saveTargetVersion(map);
            }
        } else if ("mpass".equals(type)) {

            MpassArchetype mpassArchetype = mpassArchetypeService.queryById(archetypeId);
            gitlabId = mpassArchetype.getGitlab_id();
            List bs = gitlabSerice.queryBranches(gitlabId);
            if (!bs.contains(branch)) {
                throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
            }
            if (null == mpassArchetype) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前骨架不存在"});
            }
            targetVersion = StringUtils.isNotBlank(target_version) ? target_version : branch + "-" + Util.getTimeStamp(new Date());
            String newTag = targetVersion;
            boolean existTag = gitlabSerice.checkBranchOrTag(token, mpassArchetype.getGitlab_id(), Constants.TAG, newTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + newTag + "已存在  "});
            }
             result = gitlabSerice.createMergeRequest(gitlabId, branch, Dict.MASTER, desc + "-提交准生产测试", desc, token);
        }

        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord();
        tagRecord.setGitlab_id(gitlabId);
        tagRecord.setMerge_request_id(mergeId);
        tagRecord.setArchetype_id(archetypeId);
        tagRecord.setTarget_version(targetVersion);
        tagRecord.setVariables(variables);
        tagRecord.setRelease_node_name(releaseNodeName);
        tagRecordDao.save(tagRecord);

    }

    @Override
    public void saveTargetVersion(Map<String, String> map) throws Exception {

        String version = map.get(Dict.TARGET_VERSION);
        if (!Pattern.matches(Constant.PATTERN, version)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{version, "正确格式:x.x.x, 只能输入非0开头的数字,如:10.12.1"});
        }

        //将需求状态置为3
        map.put(Dict.STAGE, "3");
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
        archetypeIssue.setCreate_date(Util.simdateFormat(new Date()));
        archetypeIssueDao.save(archetypeIssue);
    }

    @Override
    public String queryLatestVersion(Map<String, String> map) throws Exception {
        String archetypeId = map.get("archetype_id");
        String releaseNodeName = map.get("release_node_name");
        String latestVersion = "";

        //根据archetypeInfo_id从表中查出componentInfo信息
        ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeId);
        //如果查不到 抛错
        if (archetype == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过archetype_id查询组件信息不存在"});
        }

        List<ArchetypeIssue> issueList = archetypeIssueDao.queryLatestVersion(archetypeId, releaseNodeName);
        if (null != issueList && issueList.size() > 0) {
            latestVersion = issueList.get(0).getTarget_version();
        }

        return latestVersion;
    }

    @Override
    public List<String> queryReleaseVersion(Map<String, String> map) throws Exception {
        String archetypeId = map.get("archetype_id");
        List<String> releaseVersionList = new ArrayList<String>();
        List<ArchetypeRecord> list = new ArrayList<ArchetypeRecord>();

        //根据archetypeInfo_id从表中查出componentInfo信息
        ArchetypeInfo archetype = archetypeInfoDao.queryById(archetypeId);
        //如果查不到 抛错
        if (archetype == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过archetype_id查询组件信息不存在"});
        }
        list = archetypeRecordDao.queryReleaseRecordByArchetypeId(archetypeId);

        if (!CommonUtils.isNullOrEmpty(list)) {
            for (ArchetypeRecord archetypeRecord : list) {
                releaseVersionList.add(archetypeRecord.getVersion());
            }
        }

        return  releaseVersionList;

    }

}

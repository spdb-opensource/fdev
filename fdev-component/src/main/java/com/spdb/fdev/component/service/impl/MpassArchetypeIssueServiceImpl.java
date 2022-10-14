package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.dict.MpassArcEnum;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IArchetypeIssueDao;
import com.spdb.fdev.component.dao.IArchetypeIssueTagDao;
import com.spdb.fdev.component.dao.IMpassArchetypeDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;
import com.spdb.fdev.component.entity.MpassArchetype;
import com.spdb.fdev.component.entity.TagRecord;
import com.spdb.fdev.component.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class MpassArchetypeIssueServiceImpl implements IMpassArchetypeIssueService {

    private IArchetypeIssueDao archetypeIssueDao;

    private IMpassArchetypeService mpassArchetypeService;

    @Autowired
    private IGitlabSerice gitlabService;
    @Autowired
    ReleaseNodeServiceImpl  iReleaseNodeService;

    @Autowired
    private ITagRecordDao tagRecordDao;

    private IGitlabSerice gitlabSerice;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;

    private IArchetypeIssueTagDao archetypeIssueTagDao;

    private IRoleService roleService;

    @Autowired
    private IMpassArchetypeDao iMpassArchetypeDao;

    @Value("${gitlab.manager.token}")
    private String token;

    @Override
    public List<ArchetypeIssue> query(ArchetypeIssue archetypeIssue) throws Exception {
        return archetypeIssueDao.query(archetypeIssue);
    }

    /**
     * 新增一条骨架优化需求，复用后端骨架优化需求表，在Service层和后端骨架区分
     *
     * @param archetypeIssue
     * @return
     * @throws Exception
     */
    @Override
    public ArchetypeIssue save(ArchetypeIssue archetypeIssue) throws Exception {
        MpassArchetype archetype = mpassArchetypeService.queryById(archetypeIssue.getArchetype_id());
        if (archetype == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前骨架不存在"});
        }
        //校验用户权限 必须为基础架构管理员或骨架管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(archetype.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员或骨架管理员"});
            }
        }
        List list = gitlabSerice.queryBranches(archetype.getGitlab_id());
        //检查分支是否存在
        if (list.contains(archetypeIssue.getFeature_branch())) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + archetypeIssue.getFeature_branch() + "已存在  "});
        }
        //检查已有优化需求标题是否存在
        ArchetypeIssue query = new ArchetypeIssue();
        query.setArchetype_id(archetypeIssue.getArchetype_id());
        List<ArchetypeIssue> issueList = archetypeIssueDao.query(query);
        if (!CommonUtils.isNullOrEmpty(issueList)) {
            for (ArchetypeIssue issue : issueList) {
                if (archetypeIssue.getTitle().equals(issue.getTitle())) {
                    throw new FdevException(ErrorConstants.TITLE_EXISTS, new String[]{"优化需求标题: " + issue.getTitle() + "已存在  "});
                }
            }
        }
        // 设置创建时间
        archetypeIssue.setCreate_date(Util.simdateFormat(new Date()));
        // 设置当前阶段，为新建阶段
        archetypeIssue.setStage(MpassArcEnum.IssueStageEnum.CREATE.getValue());
        //创建分支
        gitlabSerice.createBranch(archetype.getGitlab_id(), archetypeIssue.getFeature_branch(), Dict.MASTER, token);
        //给开发人员分配Devoloper权限
        this.gitlabUserService.addMembers(commonInfoService.addMembersForApp(archetypeIssue.getAssignee(), archetype.getGitlab_id(), Dict.DEVELOPER));
        //入库
        ArchetypeIssue result = archetypeIssueDao.save(archetypeIssue);
        return result;
    }

    @Override
    public void delete(ArchetypeIssue archetypeIssue) throws Exception {
        archetypeIssueDao.delete(archetypeIssue);
    }

    @Override
    public void packageTag(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);//优化需求id
        String stage = (String) param.get(Dict.STAGE);//当前阶段
        String updateUser = (String) param.get(Dict.UPDATE_USER);//更新人员
        String releaseLog = (String) param.get(Dict.RELEASE_LOG);//发布日志
        String tagName = (String) param.get(Dict.TAG_NAME);//拉取tag名称
        ArchetypeIssue archetypeIssue = archetypeIssueDao.queryById(id);
        if (null == archetypeIssue) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前骨架优化需求不存在"});
        }
        MpassArchetype mpassArchetype = mpassArchetypeService.queryById(archetypeIssue.getArchetype_id());
        if (null == mpassArchetype) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前骨架不存在"});
        }
        //进行分支合并
        if (MpassArcEnum.IssueStageEnum.ALPHA.getValue().equals(stage)) {
            gitlabSerice.createMergeRequest(mpassArchetype.getGitlab_id(), archetypeIssue.getFeature_branch(), Dict.MASTER, archetypeIssue.getFeature_branch(), "骨架优化需求合并开发分支", token);
        }
        //拉取备份tag，如果传入tag为空，tag名为开发分支和时间戳
        else if (MpassArcEnum.IssueStageEnum.RELEASE.getValue().equals(stage)) {
            String newTag = StringUtils.isNotBlank(tagName) ? tagName : archetypeIssue.getFeature_branch() + "-" + Util.getTimeStamp(new Date());
            boolean existTag = gitlabSerice.checkBranchOrTag(token, mpassArchetype.getGitlab_id(), Constants.TAG, newTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + newTag + "已存在  "});
            }
            gitlabSerice.createTags(mpassArchetype.getGitlab_id(), newTag, Dict.MASTER, token);
            //存入一条当前版本的tag记录
            ArchetypeIssueTag archetypeIssueTag = new ArchetypeIssueTag();
            archetypeIssueTag.setArchetype_id(mpassArchetype.getId());
            archetypeIssueTag.setIssue_id(archetypeIssue.getId());
            archetypeIssueTag.setDate(Util.simdateFormat(new Date()));
            archetypeIssueTag.setTag(newTag);
            archetypeIssueTag.setUpdate_user(updateUser);
            archetypeIssueTag.setRelease_log(releaseLog);
            archetypeIssueTagDao.save(archetypeIssueTag);
        }
        // 当前阶段不允许发布
        else
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"当前阶段不支持发布"});

    }

    @Override
    public ArchetypeIssue queryById(String id) throws Exception {
        return archetypeIssueDao.queryById(id);
    }

    @Override
    public List<ArchetypeIssueTag> queryIssueTag(Map param) {
        String issue_id = (String) param.get(Dict.ISSUE_ID);
        return archetypeIssueTagDao.queryByIssueId(issue_id);
    }

    @Override
    public void changeStage(Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);//优化开发需求id
        ArchetypeIssue archetypeIssue = archetypeIssueDao.queryById(id);
        if (null == archetypeIssue) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前优化开发需求不存在"});
        }
        String stage = map.get(Dict.STAGE);//当前阶段
        if (!Arrays.asList("0", "1").contains(stage)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage, "所传stage不在0，1当中"});
        }
        Integer devStage = Integer.valueOf(archetypeIssue.getStage());
        devStage++;
        archetypeIssue.setStage(String.valueOf(devStage));
        archetypeIssueDao.update(archetypeIssue);
    }

    @Override
    public void destroyIssue(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        ArchetypeIssue archetypeIssue = archetypeIssueDao.queryById(id);
        //校验用户权限 必须为基础架构管理员或骨架管理员
        MpassArchetype mpassArchetype = mpassArchetypeService.queryById(archetypeIssue.getArchetype_id());
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(mpassArchetype.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员或骨架管理员"});
            }
        }
        List<ArchetypeIssueTag> tagList = archetypeIssueTagDao.queryByIssueId(id);
        if (!CommonUtils.isNullOrEmpty(tagList)) {
            throw new FdevException(ErrorConstants.ISSUE_DESTORY_FORBID, new String[]{"当前已有tag分支记录"});
        }
        archetypeIssueDao.delete(archetypeIssue);
    }

    @Override
    public List<ArchetypeIssueTag> queryMpassArchetypeHistory(Map param) {
        String archetype_id = (String) param.get(Dict.ARCHETYPE_ID);
        return archetypeIssueTagDao.queryByArchetypeId(archetype_id);
    }

    @Autowired
    public void setArchetypeIssueDao(IArchetypeIssueDao archetypeIssueDao) {
        this.archetypeIssueDao = archetypeIssueDao;
    }

    @Autowired
    public void setMpassArchetypeService(IMpassArchetypeService mpassArchetypeService) {
        this.mpassArchetypeService = mpassArchetypeService;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
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
    public void setArchetypeIssueTagDao(IArchetypeIssueTagDao archetypeIssueTagDao) {
        this.archetypeIssueTagDao = archetypeIssueTagDao;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public List<HashMap<String, Object>> queryQrmntsData(Map requestParam) {
        String user_id = (String) requestParam.get(Dict.ID);
        List<Map> results = iMpassArchetypeDao.queryQrmntsData(user_id);
        List<HashMap<String, Object>>  mpassArchetypeResult = new ArrayList<>();
        for (Map doc : results) {
            HashMap<String, Object> iMpassArchetypeMap = new HashMap<>();
            switch ((String)doc.get(Dict.STAGE)){
                case Dict.ZERO:
                    iMpassArchetypeMap.put(Dict.STAGE,MpassArcEnum.IssueStageEnum.CREATE.getName());
                    break;
                case Dict.ONE:
                    iMpassArchetypeMap.put(Dict.STAGE,Constants.RELEASE_LOWCASE);
                    break;
                case Dict.TWO:
                    iMpassArchetypeMap.put(Dict.STAGE, Constants.PUSH_TAG);
                    break;
                case Dict.THREE:
                    iMpassArchetypeMap.put(Dict.STAGE, Constants.DEV_END);
                    break;
                default:
                    continue;
            }
            MpassArchetype MpassArchetypeInfo = (MpassArchetype) (doc.get(Dict.MPASS_ARCHETYPE));
            HashSet<Map<String, String>> manager_id = MpassArchetypeInfo.getManager();
            iMpassArchetypeMap.put(Dict.PREDICT_VERSION, doc.get(Dict.PREDICT_VERSION));
            iMpassArchetypeMap.put(Dict.FEATURE_BRANCH, doc.get(Dict.FEATURE_BRANCH));
            iMpassArchetypeMap.put(Dict.RECOMMOND_VERSION, doc.get(Dict.TARGET_VERSION));
            iMpassArchetypeMap.put(Dict.ID, MpassArchetypeInfo.getId());
            iMpassArchetypeMap.put(Dict.RQRMNTS_ID, doc.get(Dict.ID));
            iMpassArchetypeMap.put(Dict.NAME, MpassArchetypeInfo.getName_en());
            iMpassArchetypeMap.put(Dict.RQRMNTS_ADMIN, manager_id);
            iMpassArchetypeMap.put(Dict.RQRMNTS_NAME, doc.get(Dict.TITLE));
            iMpassArchetypeMap.put(Dict.DEVELOP, requestParam);
            iMpassArchetypeMap.put(Dict.DUE_DATE, doc.get(Dict.DUE_DATE));
            iMpassArchetypeMap.put(Dict.DESC, doc.get(Dict.DESC));
            iMpassArchetypeMap.put(Dict.TYPE,Constants.MPASS_ARCHETYPE_TYPE);
            mpassArchetypeResult.add(iMpassArchetypeMap);
        }
        return mpassArchetypeResult;
    }

    @Override
    public Map<String, Integer> queryIssueData(String startTime, String endTime) {
        Map<String, Integer> iMpassArchetype = iMpassArchetypeDao.queryIssueData(startTime, endTime);
        return iMpassArchetype;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) throws Exception{
        List<Map> issueDelayInfo = new ArrayList<>();
        List<Map> archetypeIssueInfos = archetypeIssueDao.queryIssueDelay(requestParam);
        for (Map issueInfo : archetypeIssueInfos) {
            String stage = (String) issueInfo.get(Dict.STAGE);
            String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PARSE);
            Map delayDays = new LinkedHashMap();
            switch (stage){
                case Dict.ZERO:
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                    delayDays.put(Dict.STAGE, Constants.DEV);
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
            MpassArchetype archetypeInfo = (MpassArchetype) issueInfo.get(Dict.MPASS_ARCHETYPE);
            if(CommonUtils.isNullOrEmpty(archetypeInfo))continue;
            delayDays.put(Dict.NAME, archetypeInfo.getName_cn());
            delayDays.put(Dict.ID, archetypeInfo.getId());
            delayDays.put(Dict.RQRMNTS_ADMIN, archetypeInfo.getManager());
            delayDays.put(Dict.RQRMNTS_ID, issueInfo.get(Dict.ID));
            delayDays.put(Dict.RQRMNTS_NAME, issueInfo.get(Dict.TITLE));
            delayDays.put(Dict.DEVELOP, developerInfo);
            delayDays.put(Dict.FEATURE_BRANCH, issueInfo.get(Dict.FEATURE_BRANCH));
            delayDays.put(Dict.DUE_DATE, issueInfo.get(Dict.DUE_DATE));
            delayDays.put(Dict.TYPE, Constants.MPASS_ARCHETYPE_TYPE);
            issueDelayInfo.add(delayDays);
        }
        return issueDelayInfo;
    }

    @Override
    public void mergedCallBack(String state, Integer iid, Integer projectId) throws Exception {
        String gitlabId = String.valueOf(projectId);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);

        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {
                gitlabService.createTags(gitlabId, tagRecord.getTarget_version(), Dict.MASTER, token);

                //保存tag
                Map<String, String> map = new HashMap<String, String>();
                map.put("app_id", tagRecord.getArchetype_id());
                map.put("release_node_name", tagRecord.getRelease_node_name());
                map.put("tag", tagRecord.getTarget_version());
                iReleaseNodeService.saveTag(map);
            }
        }
    }
}

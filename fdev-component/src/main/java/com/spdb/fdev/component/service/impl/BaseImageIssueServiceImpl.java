package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IBaseImageInfoDao;
import com.spdb.fdev.component.dao.IBaseImageIssueDao;
import com.spdb.fdev.component.dao.IBaseImageRecordDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class BaseImageIssueServiceImpl implements IBaseImageIssueService {

    private IGitlabUserService gitlabUserService;

    private IBaseImageIssueDao baseImageIssueDao;

    private IBaseImageRecordDao baseImageRecordDao;

    private IBaseImageInfoDao baseImageInfoDao;

    private IGitlabSerice gitlabSerice;

    private ICommonInfoService commonInfoService;

    private IRoleService roleService;

    private ITagRecordDao tagRecordDao;

    @Autowired
    private IGitlabSerice gitlabService;

    @Autowired
    private IBaseImageIssueDao iBaseImageIssueDao;

    @Autowired
    private IBaseImageRecordDao iBaseImageRecordDao;

    @Value("${gitlab.manager.token}")
    private String token;

    /**
     * changestage时判断是否可以修改状态
     */
    private final List<String> stages = Arrays.asList(ComponentEnum.IssueStageEnum.CREATE.getValue(), ComponentEnum.IssueStageEnum.DEV.getValue());

    /**
     * 新增优化需求时判断是否有开发状态需求
     */
    private final List<String> devStages = Arrays.asList(ComponentEnum.IssueStageEnum.CREATE.getValue(), ComponentEnum.IssueStageEnum.DEV.getValue(), ComponentEnum.IssueStageEnum.TRIAL.getValue());


    @Override
    public List query(BaseImageIssue baseImageIssue) throws Exception {
        return baseImageIssueDao.query(baseImageIssue);
    }

    @Override
    public BaseImageIssue save(BaseImageIssue baseImageIssue) throws Exception {
        //控制优化需求数目，开发中只能有一个
        BaseImageIssue issue = new BaseImageIssue();
        issue.setName(baseImageIssue.getName());
        List<BaseImageIssue> issueList = baseImageIssueDao.query(issue);
        if (issueList != null && issueList.size() > 0) {
            for (BaseImageIssue baseImageIssue1 : issueList) {
                if (devStages.contains(baseImageIssue1.getStage())) {
                    throw new FdevException(ErrorConstants.IMAGE_ISSUE_LIMIT, new String[]{});
                }
            }
        }

        BaseImageInfo imageInfo = baseImageInfoDao.queryByName(baseImageIssue.getName());
        if (imageInfo == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"通过镜像名称查询镜像信息不存在"});
        }
        List list = gitlabSerice.queryBranches(imageInfo.getGitlab_id());
        //检查分支是否存在
        if (list.contains(baseImageIssue.getBranch())) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + baseImageIssue.getBranch() + "已存在  "});
        }
        // 设置需求状态，初始为0
        baseImageIssue.setStage(ComponentEnum.IssueStageEnum.CREATE.getValue());
        // 设置创建时间
        baseImageIssue.setCreate_date(Util.simdateFormat(new Date()));
        //创建分支
        gitlabSerice.createBranch(imageInfo.getGitlab_id(), baseImageIssue.getBranch(), Dict.MASTER, token);
        //给开发人员分配Developer权限
        this.gitlabUserService.addMembers(commonInfoService.addMembersForApp(baseImageIssue.getAssignee(), imageInfo.getGitlab_id(), Dict.DEVELOPER));
        //入库
        BaseImageIssue result = baseImageIssueDao.save(baseImageIssue);
        return result;
    }

    @Override
    public BaseImageIssue update(BaseImageIssue baseImageIssue) throws Exception {
        return baseImageIssueDao.update(baseImageIssue);
    }

    @Override
    public BaseImageIssue queryById(String id) {
        return baseImageIssueDao.queryById(id);
    }

    @Override
    public void packageTag(BaseImageRecord record) throws Exception {
        String release_log = record.getRelease_log();//发布日志
        HashMap<String, String> meta_data = record.getMeta_data();//元数据
        HashSet<String> trial_apps = record.getTrial_apps();//试用列表
        String stage = record.getStage();//当前阶段
        String branch = record.getBranch();//分支
        String name = record.getName();//镜像名称
        String update_user = record.getUpdate_user();//更新人员
        //检查基础镜像表
        BaseImageInfo baseImage = baseImageInfoDao.queryByName(name);
        if (baseImage == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{name + "镜像"});
        }
        //检查用户权限 必须为基础架构负责人或镜像管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(baseImage.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或镜像管理员"});
            }
        }
        //检查stage，当前需为trial阶段才能进行发布
        if (!ComponentEnum.IssueStageEnum.TRIAL.getValue().equals(stage)) {
            throw new FdevException(ErrorConstants.IMAGE_PACKAGE_STAGE, new String[]{});
        }
        //检验开发分支是否存在
        List bs = gitlabSerice.queryBranches(baseImage.getGitlab_id());
        if (!bs.contains(branch)) {
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{branch});
        }
        //持续集成参数传递，imageTag，{target_env}-{yyMMddHHmm}
        String targetTag = baseImage.getTarget_env() + "-" + Util.getTimeStamp(new Date());
        ArrayList<Map<String, String>> variables = new ArrayList<>();
        Map<String, String> groupId = new HashMap<>();
        groupId.put(Dict.KEY, Dict.IMAGE_TAG);
        groupId.put(Dict.VALUE, targetTag);
        variables.add(groupId);
        Object result = gitlabSerice.createMergeRequest(baseImage.getGitlab_id(), branch, Dict.MASTER, "镜像开发分支合并", release_log, token);
        JSONObject jsonObject = JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord(baseImage.getName(), baseImage.getGitlab_id(), mergeId, variables, branch, targetTag, release_log, meta_data, trial_apps, update_user);
        tagRecordDao.save(tagRecord);
    }

    @Override
    public void changeStage(Map<String, String> map) throws Exception {
        BaseImageIssue issue = baseImageIssueDao.queryById(map.get(Dict.ID));
        BaseImageInfo baseImageInfo = baseImageInfoDao.queryByName(issue.getName());
        //校验用户权限 必须为基础架构负责人或镜像管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(baseImageInfo.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或镜像管理员"});
            }
        }
        String sta = map.get(Dict.STAGE);
        if (!stages.contains(sta)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{sta, "所传stage不在0，1，2当中"});
        }
        Integer stage = Integer.valueOf(issue.getStage());
        stage++;
        issue.setStage(String.valueOf(stage));
        baseImageIssueDao.update(issue);
    }

    @Override
    public void destroyBaseImageIssue(String id) throws Exception {
        BaseImageIssue baseImageIssue = baseImageIssueDao.queryById(id);
        if (ComponentEnum.IssueStageEnum.RELEASE.getValue().equals(baseImageIssue.getStage())) {
            //如果属于已完成阶段，则不能删除当前issue
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"已完成优化需求不能删除"});
        }
        String branch = baseImageIssue.getBranch();
        BaseImageInfo baseImageInfo = baseImageInfoDao.queryByName(baseImageIssue.getName());
        String gitlabId = baseImageInfo.getGitlab_id();
        //拉取开发分支备份，删除原开发分支
        if (gitlabSerice.checkBranchOrTag(token, gitlabId, Dict.BRANCH, branch)) {
            gitlabSerice.createBranch(gitlabId, Util.getTimeStamp(new Date()) + "-destroyed-" + branch, branch, token);
            gitlabSerice.deleteBranch(gitlabId, branch, token);
        }
        //删除内测阶段数据
        List<BaseImageRecord> devList = baseImageRecordDao.queryByNameStage(baseImageIssue.getName(), ComponentEnum.ImageStageEnum.DEV.getValue());
        if (!CommonUtils.listIsNullOrEmpty(devList)) {
            for (BaseImageRecord record : devList) {
                baseImageRecordDao.delete(record);
            }
        }
        //删除试用阶段数据
        List<BaseImageRecord> trialList = baseImageRecordDao.queryByNameStage(baseImageIssue.getName(), ComponentEnum.ImageStageEnum.TRIAL.getValue());
        if (!CommonUtils.listIsNullOrEmpty(trialList)) {
            for (BaseImageRecord record : trialList) {
                if (gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, record.getImage_tag())) {
                    gitlabSerice.deleteTag(gitlabId, record.getImage_tag(), token);
                }
                baseImageRecordDao.delete(record);
            }
        }
        baseImageIssueDao.delete(baseImageIssue);

    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }

    @Autowired
    public void setBaseImageIssueDao(IBaseImageIssueDao baseImageIssueDao) {
        this.baseImageIssueDao = baseImageIssueDao;
    }

    @Autowired
    public void setBaseImageInfoDao(IBaseImageInfoDao baseImageInfoDao) {
        this.baseImageInfoDao = baseImageInfoDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setTagRecordDao(ITagRecordDao tagRecordDao) {
        this.tagRecordDao = tagRecordDao;
    }

    @Autowired
    public void setBaseImageRecordDao(IBaseImageRecordDao baseImageRecordDao) {
        this.baseImageRecordDao = baseImageRecordDao;
    }

    @Override
    public List<HashMap<String, Object>> queryQrmntsData(Map requestParam) throws Exception {
        String user_id = (String) requestParam.get(Dict.ID);
        List<Map> results = iBaseImageIssueDao.queryQrmntsData(user_id);
        List<HashMap<String, Object>> iBaseImageIssueResult = new ArrayList<>();
        for (Map doc : results) {
            HashMap<String, Object> iBaseImageIssueMap = new HashMap<>();
            switch ((String) doc.get(Dict.STAGE)) {
                case Dict.ZERO:
                    iBaseImageIssueMap.put(Dict.STAGE, ComponentEnum.IssueStageEnum.CREATE.getName());
                    break;
                case Dict.ONE:
                    iBaseImageIssueMap.put(Dict.STAGE, ComponentEnum.IssueStageEnum.DEV.getName());
                    break;
                case Dict.TWO:
                    iBaseImageIssueMap.put(Dict.STAGE, ComponentEnum.IssueStageEnum.TRIAL.getName());
                    break;
                case Dict.THREE:
                    iBaseImageIssueMap.put(Dict.STAGE, ComponentEnum.IssueStageEnum.RELEASE.getName());
                    break;
                case Constants.FOUR:
                    iBaseImageIssueMap.put(Dict.STAGE, Constants.DEV_END);
                    break;
                default:
                    continue;
            }
            BaseImageInfo BaseImageInfo = (BaseImageInfo) (doc.get(Dict.BASEIMAGE_INFO));
            HashSet<Map<String, String>> manager_id = BaseImageInfo.getManager();
            iBaseImageIssueMap.put(Dict.PREDICT_VERSION, doc.get(Dict.PREDICT_VERSION));
            iBaseImageIssueMap.put(Dict.FEATURE_BRANCH, doc.get(Dict.BRANCH));
            iBaseImageIssueMap.put(Dict.RECOMMOND_VERSION, doc.get(Dict.TARGET_VERSION));
            iBaseImageIssueMap.put(Dict.ID, BaseImageInfo.getId());
            iBaseImageIssueMap.put(Dict.RQRMNTS_ID, doc.get(Dict.ID));
            iBaseImageIssueMap.put(Dict.NAME, BaseImageInfo.getName());
            iBaseImageIssueMap.put(Dict.RQRMNTS_ADMIN, manager_id);
            iBaseImageIssueMap.put(Dict.RQRMNTS_NAME, doc.get(Dict.TITLE));
            iBaseImageIssueMap.put(Dict.DEVELOP, requestParam);
            iBaseImageIssueMap.put(Dict.DUE_DATE, doc.get(Dict.DUE_DATE));
            iBaseImageIssueMap.put(Dict.DESC, doc.get(Dict.DESC));
            iBaseImageIssueMap.put(Dict.TYPE, Constants.IMAGE_TYPE);
            iBaseImageIssueResult.add(iBaseImageIssueMap);
        }
        return iBaseImageIssueResult;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) throws Exception {
        List<Map> issueDelayInfo = new ArrayList<>();
        List<Map> baseImageIssueInfos = iBaseImageIssueDao.queryIssueDelay(requestParam);
        for (Map issueInfo : baseImageIssueInfos) {
            if (CommonUtils.isNullOrEmpty(issueInfo.get(Dict.BASEIMAGE_INFO))) continue;
            String name = (String) issueInfo.get(Dict.NAME);
            String branch = (String) issueInfo.get(Dict.BRANCH);
            String stage = (String) issueInfo.get(Dict.STAGE);
            if (Dict.ONE.equals(stage)) {
                stage = Dict.DEV;
            }
            //获取发包数据
            List<BaseImageRecord> baseImageRecords = iBaseImageRecordDao.queryIssueRecord(name, branch, stage);
            BaseImageRecord baseImageRecord = null;
            String update_time = null;
                for (int i = 0; i < baseImageRecords.size(); i++) {
                    baseImageRecord = baseImageRecords.get(0);
                    update_time = baseImageRecord.getUpdate_time();
                }
            String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PARSE);
            Map delayDays = new LinkedHashMap();
            switch ((String) issueInfo.get(Dict.STAGE)) {
                // alpha延期
                case Dict.ZERO:
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                    delayDays.put(Dict.STAGE, ComponentEnum.IssueStageEnum.CREATE.getName());
                    break;
                //RC延期
                case Dict.ONE:
                    //未发包
                    if (baseImageRecord == null) {
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                        delayDays.put(Dict.STAGE, ComponentEnum.IssueStageEnum.DEV.getName());
                        break;
                    }
                    //已发包
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), update_time));
                    delayDays.put(Dict.STAGE, ComponentEnum.IssueStageEnum.DEV.getName());
                    break;
                case Dict.TWO:
                    //未发包
                    if (baseImageRecord == null) {
                        delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), now));
                        delayDays.put(Dict.STAGE, ComponentEnum.IssueStageEnum.TRIAL.getName());
                        break;
                    }
                    //已发包
                    delayDays.put(Dict.DELAY_DATE, CommonUtils.dateCompare((String) issueInfo.get(Dict.DUE_DATE), update_time));
                    delayDays.put(Dict.STAGE, ComponentEnum.IssueStageEnum.TRIAL.getName());
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
            BaseImageInfo baseImageInfos = (BaseImageInfo) issueInfo.get(Dict.BASEIMAGE_INFO);
            delayDays.put(Dict.NAME, baseImageInfos.getName_cn());
            delayDays.put(Dict.ID, baseImageInfos.getId());
            delayDays.put(Dict.RQRMNTS_ADMIN, baseImageInfos.getManager());
            delayDays.put(Dict.RQRMNTS_ID, issueInfo.get(Dict.ID));
            delayDays.put(Dict.RQRMNTS_NAME, issueInfo.get(Dict.TITLE));
            delayDays.put(Dict.DEVELOP, developerInfo);
            delayDays.put(Dict.FEATURE_BRANCH, issueInfo.get(Dict.BRANCH));
            delayDays.put(Dict.DUE_DATE, issueInfo.get(Dict.DUE_DATE));
            delayDays.put(Dict.TYPE, Constants.IMAGE_TYPE);
            issueDelayInfo.add(delayDays);
        }
        return issueDelayInfo;
    }


}

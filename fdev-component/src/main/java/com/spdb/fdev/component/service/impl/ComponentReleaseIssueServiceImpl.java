package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.dict.MpassComEnum;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IMpassDevIssueDao;
import com.spdb.fdev.component.dao.IMpassReleaseIssueDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ComponentReleaseIssueServiceImpl implements IComponentReleaseIssueService {
    @Autowired
    private IComponentInfoService componentInfoService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IGitlabSerice gitlabSerice;
    @Autowired
    private ICommonScanService commonScanService;
    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;
    @Value("${gitlab.manager.token}")
    private String token;
    @Autowired
    private IGitlabUserService gitlabUserService;
    @Autowired
    private ICommonInfoService commonInfoService;
    @Autowired
    private IMpassReleaseIssueDao mpassReleaseIssueDao;
    @Autowired
    private IMpassDevIssueDao mpassDevIssueDao;
    @Autowired
    private IComponentRecordService componentRecordService;
    @Autowired
    private ITagRecordDao tagRecordDao;

    @Override
    public MpassReleaseIssue save(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        ComponentInfo component = componentInfoService.queryById(mpassReleaseIssue.getComponent_id());
        if (component == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
        }
        //校验用户权限 必须为组件管理员
        if (!roleService.isComponentManager(component.getManager_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为组件管理员"});
        }
        String project_id = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        List list = gitlabSerice.queryBranches(project_id);
        //检查分支是否存在
        if (list.contains(mpassReleaseIssue.getFeature_branch())) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + mpassReleaseIssue.getFeature_branch() + "已存在  "});
        }
        JSONArray jsonArray = commonScanService.getNexus(component.getGroupId(), component.getArtifactId());
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject job = jsonArray.getJSONObject(i);
                String version = job.getString(Dict.VERSION);
                if(version.equals(mpassReleaseIssue.getPredict_version())){
                    throw new FdevException(ErrorConstants.VERSION_EXISTS, new String[]{"版本: " + mpassReleaseIssue.getPredict_version() + "Nexus仓库已存在  "});
                }
            }
        }
        //检查已有优化需求版本是否存在
        MpassReleaseIssue releaseIssue = new MpassReleaseIssue();
        releaseIssue.setComponent_id(mpassReleaseIssue.getComponent_id());
        List<MpassReleaseIssue> releaseIssueList = mpassRelaseIssueService.query(releaseIssue);
        if (!CommonUtils.isNullOrEmpty(releaseIssueList)) {
            for (MpassReleaseIssue issue : releaseIssueList) {
                if (mpassReleaseIssue.getPredict_version().equals(issue.getPredict_version())
                        || mpassReleaseIssue.getFeature_branch().equals(issue.getFeature_branch())) {
                    throw new FdevException(ErrorConstants.RELEASE_WINDOW_EXISTS, new String[]{"窗口: " + issue.getFeature_branch() + "已存在  "});
                }
            }
        }
        // 设置创建时间
        mpassReleaseIssue.setCreate_date(Util.simdateFormat(new Date()));
        //创建分支
        gitlabSerice.createBranch(project_id, mpassReleaseIssue.getFeature_branch(), Dict.MASTER, token);
        //设置保护分支
        gitlabSerice.setProtectedBranches(project_id, mpassReleaseIssue.getFeature_branch());
        //给开发人员分配Maintainer权限
        gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassReleaseIssue.getManager(), project_id, Dict.MAINTAINER));
        //入库
        MpassReleaseIssue result = mpassReleaseIssueDao.save(mpassReleaseIssue);
        return result;
    }

    @Override
    public MpassReleaseIssue update(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        MpassReleaseIssue issue = mpassReleaseIssueDao.queryById(mpassReleaseIssue.getId());
        ComponentInfo component = componentInfoService.queryById(issue.getComponent_id());
        //校验用户权限 必须为组件管理员
        if (!roleService.isComponentManager(component.getManager_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为组件管理员"});
        }
        return mpassReleaseIssueDao.update(mpassReleaseIssue);
    }

    @Override
    public MpassDevIssue save(MpassDevIssue mpassDevIssue) throws Exception {
        MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(mpassDevIssue.getIssue_id());
        if (mpassReleaseIssue == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件优化需求不存在"});
        }
        ComponentInfo component = componentInfoService.queryById(mpassReleaseIssue.getComponent_id());
        if (component == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件不存在"});
        }
        //校验用户权限 必须为版本管理员
        if (!roleService.isComponentManager(mpassReleaseIssue.getManager())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为版本管理员"});
        }
        String project_id = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        List list = gitlabSerice.queryBranches(project_id);
        //检查分支是否存在
        if (list.contains(mpassDevIssue.getFeature_branch())) {
            throw new FdevException(ErrorConstants.BRANCH_EXISTS, new String[]{"分支: " + mpassReleaseIssue.getFeature_branch() + "已存在  "});
        }
        //检查优化需求是否存在
        List<MpassDevIssue> devIssueList = mpassDevIssueDao.queryByIssueId(mpassReleaseIssue.getId());
        if (!CommonUtils.isNullOrEmpty(devIssueList)) {
            for (MpassDevIssue devIssue : devIssueList) {
                if (mpassDevIssue.getFeature_branch().equals(devIssue.getFeature_branch())) {
                    throw new FdevException(ErrorConstants.DEV_ISSUE_EXIST, new String[]{devIssue.getFeature_branch()});
                }
            }
        }
        // 设置创建时间
        mpassDevIssue.setCreate_date(Util.simdateFormat(new Date()));
        //设置关联组件
        mpassDevIssue.setComponent_id(component.getId());
        // 设置当前stage
        mpassDevIssue.setStage(MpassComEnum.DevIssueStageEnum.CREATE.getValue());
        //创建分支
        gitlabSerice.createBranch(project_id, mpassDevIssue.getFeature_branch(), Dict.MASTER, token);
        //给开发人员分配Developer权限
        this.gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassDevIssue.getAssignee(), project_id, Dict.DEVELOPER));
        //入库
        MpassDevIssue result = mpassDevIssueDao.save(mpassDevIssue);
        //发送提醒邮件
        mpassRelaseIssueService.sendDevNotify(component.getName_cn(), mpassReleaseIssue, mpassDevIssue);
        return result;
    }

    @Override
    public boolean canPackage(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        MpassDevIssue mpassDevIssue = mpassDevIssueDao.queryById(id);
        MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(mpassDevIssue.getIssue_id());
        if (null == mpassReleaseIssue) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件优化需求不存在"});
        }
        ComponentInfo component = componentInfoService.queryById(mpassDevIssue.getComponent_id());
        String project_id = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        //查询是否有open状态的merge请求
        Object merge = gitlabSerice.queryMergeList(project_id, mpassReleaseIssue.getFeature_branch(), Constant.MERGE_STATE_OPENED);
        com.alibaba.fastjson.JSONArray mergelist = com.alibaba.fastjson.JSONObject.parseArray((String) merge);
        if (null != mergelist && mergelist.size() > 0) {
            throw new FdevException(ErrorConstants.MERGE_EXISTS, new String[]{mpassReleaseIssue.getFeature_branch()});
        }
        //查询是否有running状态的pipeline请求
        Object pipeRuning = gitlabSerice.queryPipelineList(project_id, mpassReleaseIssue.getFeature_branch(), Constant.PIPELINE_STATUS_RUNNING);
        com.alibaba.fastjson.JSONArray runningList = com.alibaba.fastjson.JSONObject.parseArray((String) pipeRuning);
        if (null != runningList && runningList.size() > 0) {
            throw new FdevException(ErrorConstants.PIPELINE_EXISTS, new String[]{mpassReleaseIssue.getFeature_branch()});
        }
        //查询是否有pending状态的pipeline请求
        Object pipePending = gitlabSerice.queryPipelineList(project_id, mpassReleaseIssue.getFeature_branch(), Constant.PIPELINE_STATUS_PENDING);
        com.alibaba.fastjson.JSONArray pendinglist = com.alibaba.fastjson.JSONObject.parseArray((String) pipePending);
        if (null != pendinglist && pendinglist.size() > 0) {
            throw new FdevException(ErrorConstants.PIPELINE_EXISTS, new String[]{mpassReleaseIssue.getFeature_branch()});
        }
        return true;
    }

    @Override
    public void devPackage(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        String stage = (String) param.get(Dict.STAGE);
        String updateUser = (String) param.get(Dict.UPDATE_USER);//更新人员
        String releaseLog = (String) param.get(Dict.RELEASE_LOG);//发布日志
        MpassDevIssue mpassDevIssue = mpassDevIssueDao.queryById(id);
        if (null == mpassDevIssue) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前优化开发需求不存在"});
        }
        MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(mpassDevIssue.getIssue_id());
        if (null == mpassReleaseIssue) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前组件优化需求不存在"});
        }
        ComponentInfo component = componentInfoService.queryById(mpassDevIssue.getComponent_id());
        String project_id = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        //内测阶段进行分支合并操作
        if (MpassComEnum.DevIssueStageEnum.ALPHA.getValue().equals(stage)) {
            gitlabSerice.createMergeRequest(project_id, mpassDevIssue.getFeature_branch(), mpassReleaseIssue.getFeature_branch(), mpassDevIssue.getFeature_branch(), "组件开发需求，合并到release分支", token);
        }
        //beta阶段拉取tag
        else if (MpassComEnum.DevIssueStageEnum.BETA.getValue().equals(stage)) {
            String version = mpassReleaseIssue.getPredict_version()+"-"+Constants.BETA;
            String tag = "";
            List list = gitlabSerice.queryTag(project_id, version);
            if(CommonUtils.isNullOrEmpty(list)){
                tag = version+".0";
            }else{
                tag = version+"."+list.size();
            }
            String vTag = Constants.VERSION_TAG + tag + Dict._SNAPSHOT;
            //判断Tag是否已存在
            boolean existTag = gitlabSerice.checkBranchOrTag(token, project_id, Constants.TAG, vTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + vTag + "已存在  "});
            }
            gitlabSerice.createTags(project_id, vTag, mpassReleaseIssue.getFeature_branch(), token);
            ComponentRecord componentRecord = new ComponentRecord();
            componentRecord.setComponent_id(mpassDevIssue.getComponent_id());
            componentRecord.setVersion(tag + Dict._SNAPSHOT);
            componentRecord.setUpdate_user(updateUser);
            componentRecord.setBranch(mpassDevIssue.getFeature_branch());
            componentRecord.setRelease_log(releaseLog);
            componentRecord.setPackagetype(Constants.PREPACKAGE);
            componentRecord.setDate(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            componentRecord.setType(Constants.RECORD_TEST_TYPE);//beta阶段打包设置为测试版本
            componentRecord.setIssue_id(mpassDevIssue.getIssue_id());//关联Release优化需求窗口id
            componentRecordService.save(componentRecord);
        }
        //rc阶段拉取rc的tag
        else if (MpassComEnum.DevIssueStageEnum.RC.getValue().equals(stage)) {
            String version = mpassReleaseIssue.getPredict_version()+"-"+ Constants.RC;
            String tag = "";
            List list = gitlabSerice.queryTag(project_id, version);
            //生成rc tag
            if(CommonUtils.isNullOrEmpty(list)){
                tag = version+"1";
            }else{
                tag = version + (list.size() + 1);
            }
            String vTag = Constants.VERSION_TAG + tag;
            //判断Tag是否已存在
            boolean existTag = gitlabSerice.checkBranchOrTag(token, project_id, Constants.TAG, vTag);
            if (existTag) {
                throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + vTag + "已存在  "});
            }
            gitlabSerice.createTags(project_id, vTag, mpassReleaseIssue.getFeature_branch(), token);
            ComponentRecord componentRecord = new ComponentRecord();
            componentRecord.setComponent_id(mpassDevIssue.getComponent_id());
            componentRecord.setVersion(tag);
            componentRecord.setUpdate_user(updateUser);
            componentRecord.setBranch(mpassDevIssue.getFeature_branch());
            componentRecord.setRelease_log(releaseLog);
            componentRecord.setPackagetype(Constants.PREPACKAGE);
            componentRecord.setDate(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            componentRecord.setType(Constants.RECORD_TEST_TYPE);//rc阶段打包设置为测试版本
            componentRecord.setIssue_id(mpassDevIssue.getIssue_id());//关联Release优化需求窗口id
            componentRecordService.save(componentRecord);
        } else {
            throw new FdevException(ErrorConstants.STAGE_ERROR, new String[]{"当前阶段不支持发布"});
        }
    }

    @Override
    public void destroyIssue(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);//优化需求或开发需求的id
        String type = (String) param.get(Dict.TYPE);//属于优化需求还是开发需求
        if (Constants.MPASS_RELEASE.equals(type)) {
            //如果属于优化需求，查询release下所有开发需求
            MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(id);
            //查询Mpass组件
            ComponentInfo component = componentInfoService.queryById(mpassReleaseIssue.getComponent_id());
            //校验用户权限 必须为组件管理员
            if (!roleService.isComponentManager(component.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为组件管理员"});
            }
            if (null != mpassReleaseIssue) {
                //如果有开发优化需求，则不能删除此release
                List<MpassDevIssue> devIssueList = mpassDevIssueDao.queryByIssueId(mpassReleaseIssue.getId());
                if (!CommonUtils.isNullOrEmpty(devIssueList)) {
                    throw new FdevException(ErrorConstants.RELEASE_WINDOW_DESTORY_FORBID, new String[]{"当前窗口已有开发分支"});
                }
                mpassReleaseIssueDao.delete(mpassReleaseIssue);
            }
        } else if (Constants.MPASS_DEV.equals(type)) {
            //如果是优化开发需求，只查询属于此开发分支合并打出的包
            MpassDevIssue mpassDevIssue = mpassDevIssueDao.queryById(id);
            //校验用户权限 必须为版本管理员
            MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(mpassDevIssue.getIssue_id());
            if (!roleService.isComponentManager(mpassReleaseIssue.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为版本管理员"});
            }
            //如果已有正式发布版本，则不允许废弃
            if (null != mpassDevIssue) {
                List<ComponentRecord> recordList = componentRecordService.queryByComponentIdAndBranch(mpassDevIssue.getComponent_id(), "", mpassReleaseIssue.getFeature_branch(), "");
                if (!CommonUtils.isNullOrEmpty(recordList)) {
                    throw new FdevException(ErrorConstants.DEV_DESTORY_FORBID, new String[]{"当前已有正式发布记录"});
                }
                List<ComponentRecord> deleteList = componentRecordService.queryByComponentIdAndBranch(mpassDevIssue.getComponent_id(), mpassReleaseIssue.getPredict_version(), mpassDevIssue.getFeature_branch(), "");
                //删除已经发布的alpha、beta、rc的tag
                if (!CommonUtils.isNullOrEmpty(deleteList)) {
                    for (ComponentRecord record : deleteList) {
                        componentRecordService.delete(record);
                    }
                }
                mpassDevIssueDao.delete(mpassDevIssue);
            }
        }
    }

    @Override
    public void releasePackage(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        String updateUser = (String) param.get(Dict.UPDATE_USER);
        String releaseLog = (String) param.get(Dict.RELEASE_LOG);
        String tag_name = (String) param.get(Dict.TAG_NAME);
        MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(id);
        if (mpassReleaseIssue == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前优化需求不存在"});
        }
        ComponentInfo component = componentInfoService.queryById(mpassReleaseIssue.getComponent_id());
        //release发布权限仅限于组件管理员、版本管理员
        if (StringUtils.isBlank(updateUser))
            updateUser = CommonUtils.getSessionUser().getId();
        if (!CommonUtils.checkUser(updateUser, component.getManager_id()) && !CommonUtils.checkUser(updateUser, mpassReleaseIssue.getManager())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为组件管理员或版本管理员"});
        }
        //release发包前需要有beta包
        List<ComponentRecord> recordList = componentRecordService.getAlphaOrRcVersion(mpassReleaseIssue.getComponent_id(), Constants.BETA, mpassReleaseIssue.getPredict_version(), false);
        if (CommonUtils.isNullOrEmpty(recordList)) {
            throw new FdevException(ErrorConstants.RELEASE_PACKAGE_LIMIT);
        }
        //获取正式发布的版本，如果传入版本为空，选择预设版本；会拉取一个分支，自动触发持续集成
        String tag = StringUtils.isNotBlank(tag_name) ? tag_name : mpassReleaseIssue.getPredict_version();
        String vTag = Constants.VERSION_TAG + tag+ Constants._RELEASE;
        //判断Tag是否已存在
        String project_id = gitlabSerice.queryProjectIdByUrl(component.getGitlab_url());
        boolean existTag = gitlabSerice.checkBranchOrTag(token, project_id, Constants.TAG, vTag);
        if (existTag) {
            throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + vTag + "已存在  "});
        }
        //检查nexus版本是否存在
        JSONArray jsonArray = commonScanService.getNexus(component.getGroupId(), component.getArtifactId());
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject job = jsonArray.getJSONObject(i);
                String version = job.getString(Dict.VERSION);
                if(version.equals(mpassReleaseIssue.getPredict_version())){
                    throw new FdevException(ErrorConstants.VERSION_EXISTS, new String[]{"版本: " + mpassReleaseIssue.getPredict_version() + "Nexus仓库已存在  "});
                }
            }
        }
        // 在release分支下执行npm --no-git-tag-version version，修改版本和changelog
        //gitlabSerice.updateReleaseChangeLog(component, mpassReleaseIssue.getFeature_branch(), tag);
        //发起release分支到master的合并请求
        Object result = gitlabSerice.createMergeRequest(project_id, mpassReleaseIssue.getFeature_branch(), Dict.MASTER, mpassReleaseIssue.getFeature_branch(), "正式发布阶段，release分支合并到master", token);
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord();
        tagRecord.setGitlab_id(project_id);
        tagRecord.setMerge_request_id(mergeId);
        tagRecord.setComponent_id(mpassReleaseIssue.getComponent_id());
        tagRecord.setTarget_version(tag + Constants._RELEASE);
        tagRecord.setUpdate_user(updateUser);
        tagRecord.setBranch(mpassReleaseIssue.getFeature_branch());
        tagRecord.setRelease_log(releaseLog);
        tagRecord.setIssue_id(mpassReleaseIssue.getId());
        tagRecordDao.save(tagRecord);
    }

    @Override
    public List<ComponentRecord> queryMpassIssueRecord(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);//优化需求或开发需求的id
        String type = (String) param.get(Dict.TYPE);//属于优化需求还是开发需求
        String stage = (String) param.get(Dict.STAGE);//当前开发需求阶段
        List<ComponentRecord> result = null;
        if (Constants.MPASS_RELEASE.equals(type)) {
            //如果属于优化需求，查询release打出的包和属于此release下的开发分支合并打出的包
            MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(id);
            if (null != mpassReleaseIssue) {
                //查询Release打包版本
                result = componentRecordService.queryByIssueId(mpassReleaseIssue.getId());
            }
        } else if (Constants.MPASS_DEV.equals(type)) {
            //如果是优化开发需求，只查询属于此开发分支合并打出的包
            MpassDevIssue mpassDevIssue = mpassDevIssueDao.queryById(id);
            MpassReleaseIssue mpassReleaseIssue = mpassReleaseIssueDao.queryById(mpassDevIssue.getIssue_id());
            if (null != mpassDevIssue) {
                result = componentRecordService.queryByComponentIdAndBranch(mpassDevIssue.getComponent_id(), mpassReleaseIssue.getPredict_version(),"", stage);
            }
        }
        return result;
    }

}

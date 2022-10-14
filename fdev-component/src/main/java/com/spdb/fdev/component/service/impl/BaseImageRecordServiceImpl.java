package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IBaseImageInfoDao;
import com.spdb.fdev.component.dao.IBaseImageIssueDao;
import com.spdb.fdev.component.dao.IBaseImageRecordDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.IBaseImageRecordService;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.component.service.IRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class BaseImageRecordServiceImpl implements IBaseImageRecordService {

    private IBaseImageInfoDao baseImageInfoDao;

    private IBaseImageRecordDao baseImageRecordDao;

    private ITagRecordDao tagRecordDao;

    private IGitlabSerice gitlabSerice;

    private IBaseImageIssueDao baseImageIssueDao;

    private IRoleService roleService;

    @Autowired
    ReleaseNodeServiceImpl  iReleaseNodeService;

    @Value("${gitlab.manager.token}")
    private String token;

    @Override
    public List query(BaseImageRecord baseImageRecord) throws Exception {
        return baseImageRecordDao.query(baseImageRecord);
    }

    @Override
    public BaseImageRecord queryByNameAndTrialApps(String name, String gitlab_id) {
        return baseImageRecordDao.queryByNameAndTrialApps(name, gitlab_id);
    }

    @Override
    public BaseImageRecord save(Map hashMap) throws Exception {
        BaseImageRecord baseImageRecord = new BaseImageRecord();
        String name = (String) hashMap.get(Dict.NAME);
        String branch = (String) hashMap.get(Dict.BRANCH);
        String image_tag = (String) hashMap.get(Dict.IMAGE_TAG);
        String stage = (String) hashMap.get(Dict.STAGE);
        String release_log = (String) hashMap.get(Dict.RELEASE_LOG);
        String gitlab_user_id = (String) hashMap.get(Dict.GIT_USER_ID);
        Map<String, Object> userMap = roleService.queryUserbyGitId(gitlab_user_id);
        baseImageRecord.setName(name);
        baseImageRecord.setBranch(branch);
        baseImageRecord.setImage_tag(image_tag);
        baseImageRecord.setStage(stage);
        baseImageRecord.setRelease_log(release_log);
        if (userMap != null) {
            baseImageRecord.setUpdate_user((String) userMap.get(Dict.ID));
        }
        //image_tag为时间戳（到分钟），如果在同一分钟触发两次持续集成，认为一个，仅更新时间
        baseImageRecord.setUpdate_time(Util.simdateFormat(new Date()));
        BaseImageRecord record = this.queryByNameAndTag(baseImageRecord.getName(), baseImageRecord.getImage_tag());
        if (record == null) {
            return baseImageRecordDao.save(baseImageRecord);
        } else {
            record.setUpdate_time(baseImageRecord.getUpdate_time());
            return baseImageRecordDao.update(record);
        }

    }

    @Override
    public BaseImageRecord update(BaseImageRecord baseImageRecord) throws Exception {
        //如果当前版本设置为正式版本，将原有正式（release）版本设置为失效版本
        if (StringUtils.isNotBlank(baseImageRecord.getStage()) && ComponentEnum.ImageStageEnum.RELEASE.getValue().equals(baseImageRecord.getStage())) {
            //将原有Release版本设置为invalid版本
            List<BaseImageRecord> releaseList = this.queryByNameStage(baseImageRecord.getName(), ComponentEnum.ImageStageEnum.RELEASE.getValue());
            if (!CommonUtils.isNullOrEmpty(releaseList)) {
                for (BaseImageRecord info : releaseList) {
                    info.setStage(ComponentEnum.ImageStageEnum.INVALID.getValue());
                    baseImageRecordDao.update(info);
                }
            }
        }
        return baseImageRecordDao.update(baseImageRecord);
    }

    @Override
    public BaseImageRecord queryById(String id) {
        return baseImageRecordDao.queryById(id);
    }

    @Override
    public void recoverInvalidRecord(String id) throws Exception {
        BaseImageRecord record = baseImageRecordDao.queryById(id);
        // 判断是否为invalid版本
        if (!ComponentEnum.ImageStageEnum.INVALID.getValue().equals(record.getStage())) {
            throw new FdevException(ErrorConstants.IS_NOT_INVALID, new String[]{});
        }
        record.setStage(ComponentEnum.ImageStageEnum.RELEASE.getValue());
        //将原有Release版本设置为invalid版本
        List<BaseImageRecord> releaseList = this.queryByNameStage(record.getName(), ComponentEnum.ImageStageEnum.RELEASE.getValue());
        if (!CommonUtils.isNullOrEmpty(releaseList)) {
            for (BaseImageRecord info : releaseList) {
                info.setStage(ComponentEnum.ImageStageEnum.INVALID.getValue());
                baseImageRecordDao.update(info);
            }
        }
        baseImageRecordDao.update(record);
    }

    @Override
    public BaseImageRecord queryByNameAndTag(String name, String tag) {
        return baseImageRecordDao.queryByNameAndTag(name, tag);
    }

    @Override
    public List<BaseImageRecord> queryByNameStage(String name, String stage) {
        return baseImageRecordDao.queryByNameStage(name, stage);
    }

    @Override
    public void mergedCallBack(String state, Integer iid, Integer project_id) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);
        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {
                gitlabSerice.createTags(gitlabId, tagRecord.getBranch(), Dict.MASTER, token);
                BaseImageRecord baseImageRecord = new BaseImageRecord(
                        tagRecord.getName(),
                        tagRecord.getDevBranch(),
                        tagRecord.getBranch(),
                        ComponentEnum.ImageStageEnum.TRIAL.getValue(),
                        tagRecord.getTrial_apps(),
                        tagRecord.getRelease_log(),
                        tagRecord.getUpdate_user(),
                        TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME),
                        Constants.PREPACKAGE, //未打包
                        tagRecord.getMeta_data());
                baseImageRecordDao.save(baseImageRecord);
            }
        }
    }

    @Override
    public void pipiCallBack(String gitlabId, String ref) throws Exception {
        BaseImageInfo baseImageInfo = baseImageInfoDao.queryByGitlabId(gitlabId);
        if (baseImageInfo == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前镜像gitlabId:" + gitlabId + "不存在"});
        }
        BaseImageRecord baseImageRecord = baseImageRecordDao.queryByNameAndTag(baseImageInfo.getName(), ref);
        if (baseImageRecord != null) {
            baseImageRecord.setPackagetype(Constants.PIPEPACKAGE);
            baseImageRecordDao.update(baseImageRecord);
        }
    }

    @Override
    public List<BaseImageRecord> queryIssueRecord(String name, String branch, String stage) {
        return baseImageRecordDao.queryIssueRecord(name, branch, stage);
    }

    @Override
    public void relasePackage(String name, String branch) throws Exception {
        List<BaseImageRecord> trialList =
                baseImageRecordDao.queryIssueRecord(name, branch, ComponentEnum.ImageStageEnum.TRIAL.getValue());
        List<BaseImageRecord> releaseList =
                baseImageRecordDao.queryByNameStage(name, ComponentEnum.ImageStageEnum.RELEASE.getValue());
        if (!CommonUtils.isNullOrEmpty(trialList)) {
            //将原有release版本设置为invalid
            if (!CommonUtils.isNullOrEmpty(releaseList)) {
                for (BaseImageRecord baseImageRecord : releaseList) {
                    baseImageRecord.setStage(ComponentEnum.ImageStageEnum.INVALID.getValue());
                    baseImageRecordDao.update(baseImageRecord);
                }
            }
            //将最新trial设置为release
            BaseImageRecord record = trialList.get(0);
            record.setStage(ComponentEnum.ImageStageEnum.RELEASE.getValue());
            baseImageRecordDao.update(record);
            //更新issue为正式发布
            BaseImageIssue imageIssue = baseImageIssueDao.queryByNameAndBranch(name, branch);
            imageIssue.setStage(ComponentEnum.IssueStageEnum.RELEASE.getValue());
            baseImageIssueDao.update(imageIssue);

        }

    }

    @Override
    public void mergedMasterCallBack(String state, Integer iid, Integer project_id) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);
        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {
                gitlabSerice.createTags(gitlabId, tagRecord.getBranch(), Dict.MASTER, token);
                //保存tag
                Map<String, String> map = new HashMap<String, String>();
                map.put("app_id", tagRecord.getImage_id());
                map.put("release_node_name", tagRecord.getRelease_node_name());
                map.put("tag", tagRecord.getBranch());
                iReleaseNodeService.saveTag(map);
            }
        }
    }


    @Autowired
    public void setBaseImageRecordDao(IBaseImageRecordDao baseImageRecordDao) {
        this.baseImageRecordDao = baseImageRecordDao;
    }

    @Autowired
    public void setBaseImageInfoDao(IBaseImageInfoDao baseImageInfoDao) {
        this.baseImageInfoDao = baseImageInfoDao;
    }

    @Autowired
    public void setTagRecordDao(ITagRecordDao tagRecordDao) {
        this.tagRecordDao = tagRecordDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setBaseImageIssueDao(IBaseImageIssueDao baseImageIssueDao) {
        this.baseImageIssueDao = baseImageIssueDao;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }
}

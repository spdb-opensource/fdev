package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IArchetypeInfoDao;
import com.spdb.fdev.component.dao.IArchetypeIssueDao;
import com.spdb.fdev.component.dao.IArchetypeRecordDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.IArchetypeRecordService;
import com.spdb.fdev.component.service.IArchetypeScanService;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.component.service.IRoleService;
import com.spdb.fdev.component.strategy.webhook.impl.ArchetypeMergeEventStrategyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class ArchetypeRecordServiceImpl implements IArchetypeRecordService {

    @Value("${gitlab.manager.token}")
    private String token;

    @Autowired
    private IGitlabSerice gitlabService;

    @Autowired
    private IArchetypeRecordDao archetypeRecordDao;

    @Autowired
    private IArchetypeInfoDao archetypeInfoDao;

    @Autowired
    private ITagRecordDao tagRecordDao;


    @Autowired
    private IRoleService roleService;

    @Autowired
    private IArchetypeIssueDao archetypeIssueDao;

    @Autowired
    private IArchetypeScanService archetypeScanService;

    @Autowired
    ReleaseNodeServiceImpl  iReleaseNodeService;

    @Autowired
    private IGitlabSerice gitlabSerice;


    @SuppressWarnings("rawtypes")
    @Override
    public List query(ArchetypeRecord archetypeRecord) throws Exception {
        return archetypeRecordDao.query(archetypeRecord);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryByArchetypeIdAndVersion(ArchetypeRecord archetypeRecord) {
        return archetypeRecordDao.queryByArchetypeIdAndVersion(archetypeRecord);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryByArchetypeIdAndRegexVersion(ArchetypeRecord archetypeRecord) {
        return archetypeRecordDao.queryByArchetypeIdAndRegexVersion(archetypeRecord);
    }

    @Override
    public ArchetypeRecord queryByArchetypeIdAndVersion(String archetype_id, String version) {
        return archetypeRecordDao.queryByArchetypeIdAndVersion(archetype_id, version);
    }

    @Override
    public ArchetypeRecord queryByArchetypeIdAndType(String archetype_id, String type) {
        return archetypeRecordDao.queryByArchetypeIdAndType(archetype_id, type);
    }

    @Override
    public ArchetypeRecord save(ArchetypeRecord archetypeRecord) throws Exception {
        return archetypeRecordDao.save(archetypeRecord);
    }

    @Override
    public ArchetypeRecord update(ArchetypeRecord archetypeRecord) throws Exception {
        //校验用户权限 必须为基础架构负责人或骨架管理员
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetypeRecord.getArchetype_id());
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(archetypeInfo.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人或骨架管理员"});
            }
        }
        //SNAPSHOT只能设置为测试版本
        if (isSnapshot(archetypeRecord.getVersion())
                && !Constants.RECORD_TEST_TYPE.equals(archetypeRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_COMPONENTRECORD_ERROR, new String[]{});
        }
        //RELEASE版本可以修改为推荐，正式和废弃，如果修改为推荐版本，需要将之前推荐版本设置为正式版本
        if ((!isSnapshot(archetypeRecord.getVersion()))
                && Constants.RECORD_TEST_TYPE.equals(archetypeRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_COMPONENTRECORD_ERROR, new String[]{});
        }
        //如果设置为推荐版本，要将之前的推荐版本设置为正式版本
        if ((!isSnapshot(archetypeRecord.getVersion())) && Constants.RECORD_RECOMMEND_TYPE.equals(archetypeRecord.getType())) {
            ArchetypeRecord record = archetypeRecordDao.queryByType(archetypeRecord.getArchetype_id(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                record.setType(Constants.RECORD_OFFICIAL_TYPE);
                archetypeRecordDao.update(record);
            }
        }
        return archetypeRecordDao.update(archetypeRecord);
    }

    /**
     * 判断是否SNAPSHOT或Rc版本
     *
     * @param version
     * @return
     */
    boolean isSnapshot(String version) {
        if (version.endsWith(Constants.SNAPSHOT)
                || version.endsWith(Constants.SNAPSHOT_LOWCASE)) {
            return true;
        }
        return false;
    }

    @Override
    public ArchetypeRecord queryById(ArchetypeRecord archetypeRecord) throws Exception {
        return archetypeRecordDao.queryById(archetypeRecord);
    }

    @Override
    public ArchetypeRecord queryByType(String archetypeId, String type) {
        return archetypeRecordDao.queryByType(archetypeId, type);
    }

    @Override
    public long deleteByArchetypeId(String archetypeId) {
        return archetypeRecordDao.deleteByArchetypeId(archetypeId);
    }

    @Override
    public ArchetypeRecord upsert(ArchetypeRecord archetypeRecord) throws Exception {
        return archetypeRecordDao.upsert(archetypeRecord);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List queryRecordByArchetypeIdAndVersion(String archetypeId, String tagName) {
        return archetypeRecordDao.queryRecordByArchetypeIdAndVersion(archetypeId, tagName);
    }

    @Override
    public ArchetypeRecord queryByPipId(String pipid) {
        return archetypeRecordDao.queryByPipId(pipid);
    }

    @Override
    public int queryArchetypeRecoreds(String archetypeId) {
        return archetypeRecordDao.queryArchetypeRecoreds(archetypeId);
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
                String archetype_type;
                if (tagRecord.getBranch().endsWith(Dict._SNAPSHOT)) {
                    //SNAPSHOT分支后打包后为测试版本
                    archetype_type = Constants.RECORD_TEST_TYPE;
                } else if (tagRecord.getBranch().endsWith(Dict._RELEASE)) {
                    //tag打包为正式版本
                    archetype_type = Constants.RECORD_OFFICIAL_TYPE;
                    //SNAPSHOT分支合并到master分支后拉取tag，并以tag打包
                    gitlabService.createTags(gitlabId, tagRecord.getBranch(), Dict.MASTER, token);
                } else {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{tagRecord.getBranch()});
                }
                Object result = gitlabService.createPipeline(gitlabId, tagRecord.getBranch(), tagRecord.getVariables(), token);
                String pipid = CommonUtils.getPipId(result);
                ArchetypeRecord archetypeRecord = new ArchetypeRecord(
                        tagRecord.getArchetype_id(),
                        tagRecord.getBranch(),
                        TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME),
                        tagRecord.getUpdate_user(),
                        tagRecord.getRelease_log(),
                        archetype_type, //版本
                        Constants.PREPACKAGE, //未打包
                        pipid);
                archetypeRecordDao.upsert(archetypeRecord);
            }
        }
    }

    @Override
    public void pipiCallBack(String version, String pipid) throws Exception {
        ArchetypeRecord archetypeRecord = archetypeRecordDao.queryByPipId(pipid);
        if (archetypeRecord != null) {
            archetypeRecord.setDate(Util.simdateFormat(new Date()));
            archetypeRecord.setPackagetype(Constants.PIPEPACKAGE);
            String target_version = version.split("-")[0];
            if (version.contains(Constants.RELEASE)) {
                ArchetypeIssue issue = archetypeIssueDao.queryByArchetypeIdAndVersion(archetypeRecord.getArchetype_id(), target_version);
                issue.setStage(Constants.ARCHETYPE_STAGE_PASSED);
                archetypeIssueDao.update(issue);
                //将当前版本设置为推荐版本，并且将之前的推荐版本设置为正式版本
                ArchetypeRecord record = archetypeRecordDao.queryByType(archetypeRecord.getArchetype_id(), Constants.RECORD_RECOMMEND_TYPE);
                if (record != null) {
                    record.setType(Constants.RECORD_OFFICIAL_TYPE);
                    archetypeRecordDao.update(record);
                }
                archetypeRecord.setType(Constants.RECORD_RECOMMEND_TYPE);
            }
            archetypeRecordDao.upsert(archetypeRecord);
            archetypeScanService.scanComponentArchetype(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion());
            archetypeScanService.scanImageArchetype(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion());
        }
    }

    @Override
    public ArchetypeRecord queryLatestRelease(String archetypeId) {
        return archetypeRecordDao.queryLatestRelease(archetypeId);
    }

    @Override
    public void mergedSitAndMasterCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);

        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {

                ArchetypeInfo archetype = archetypeInfoDao.queryById(tagRecord.getArchetype_id());

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

                //将version放到variables
                String packetVersion = "";
                String ref = "";
                if ("SIT".equals(target_branch)) {
                    packetVersion = tagRecord.getTarget_version() + Dict._SNAPSHOT;
                    ref = target_branch;

                    Map<String, String> version = new HashMap<>();
                    version.put(Dict.KEY, Dict.VERSION);
                    version.put(Dict.VALUE, packetVersion);
                    variables.add(version);
                    Object result = gitlabService.createPipeline(gitlabId, ref, variables, token);


                } else {
                    packetVersion = tagRecord.getTarget_version() + Dict._RELEASE;

                    String tag = packetVersion;
                    //判断Tag是否已存在
                    boolean existTag = gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, tag);
                    if (existTag) {
                        throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + tag + "已存在  "});
                    }
                    gitlabService.createTags(gitlabId, tag, Dict.MASTER, token);
                    ref = packetVersion;

                    //保存tag
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("app_id", tagRecord.getArchetype_id());
                    map.put("release_node_name", tagRecord.getRelease_node_name());
                    map.put("tag", tag);
                    iReleaseNodeService.saveTag(map);

                    Map<String, String> version = new HashMap<>();
                    version.put(Dict.KEY, Dict.VERSION);
                    version.put(Dict.VALUE, packetVersion);
                    variables.add(version);
                    Object result = gitlabService.createPipeline(gitlabId, ref, variables, token);

                 
                    ArchetypeRecord archetypeRecord = new ArchetypeRecord(tagRecord.getArchetype_id(),packetVersion,  TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));       
                    archetypeRecord.setPackagetype(Constants.PIPEPACKAGE);
                    archetypeRecordDao.upsert(archetypeRecord);
                }
            }
        }
    }
}

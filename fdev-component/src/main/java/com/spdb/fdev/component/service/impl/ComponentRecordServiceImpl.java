package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
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
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RefreshScope
public class ComponentRecordServiceImpl implements IComponentRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ComponentRecordServiceImpl.class);

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${history.mpasscomponent.url}")
    private String history_mpasscomponent_url;

    private IComponentRecordDao componentRecordDao;

    private IRoleService roleService;

    private IAppService appService;

    private IComponentInfoDao componentInfoDao;

    private ITagRecordDao tagRecordDao;

    private IGitlabSerice gitlabService;

    private IMpassComponentDao mpassComponentDao;

    private IComponentApplicationDao componentApplicationDao;

    private RestTransport restTransport;

    private IMailService mailService;

    @Autowired
    private IArchetypeRecordDao iArchetypeRecordDao;

    @Autowired
    ReleaseNodeServiceImpl  iReleaseNodeService;

    @Autowired
    private IBaseImageRecordDao ibaseimagerecorddao;

    @Autowired
    private IMpassArchetypeIssueService iMpassArchetypeIssueService;

    @Autowired
    private IMpassArchetypeDao iMpassArchetypeDao;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Override
    public List query(ComponentRecord componentRecord) throws Exception {
        return componentRecordDao.query(componentRecord);
    }

    @Override
    public List queryByComponentIdAndRegexVersion(ComponentRecord componentRecord) {
        return componentRecordDao.queryByComponentIdAndRegexVersion(componentRecord);
    }

    @Override
    public List queryByComponentIdAndVersion(ComponentRecord componentRecord) {
        return componentRecordDao.queryByComponentIdAndVersion(componentRecord);
    }

    @Override
    public ComponentRecord queryByComponentIdAndVersion(String component_id, String version) {
        return componentRecordDao.queryByComponentIdAndVersion(component_id, version);
    }

    @Override
    public ComponentRecord queryByComponentIdAndType(String componentId, String type) {
        return componentRecordDao.queryByComponentIdAndType(componentId, type);
    }

    @Override
    public ComponentRecord save(ComponentRecord componentRecord) throws Exception {
        return componentRecordDao.save(componentRecord);
    }

    @Override
    public ComponentRecord update(ComponentRecord componentRecord) throws Exception {
        //校验用户权限 必须为基础架构负责人或组件管理员
        ComponentInfo componentInfo = componentInfoDao.queryById(componentRecord.getComponent_id());
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(componentInfo.getManager_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足,必须为基础架构负责人或组件管理员"});
            }
        }
        //SNAPSHOT和RC只能设置为测试版本
        if (isSnapshotOrRc(componentRecord.getVersion())
                && !Constants.RECORD_TEST_TYPE.equals(componentRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_COMPONENTRECORD_ERROR, new String[]{});
        }
        //RELEASE版本可以修改为推荐，正式和废弃，如果修改为推荐版本，需要将之前推荐版本设置为正式版本
        if ((!isSnapshotOrRc(componentRecord.getVersion()))
                && Constants.RECORD_TEST_TYPE.equals(componentRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_COMPONENTRECORD_ERROR, new String[]{});
        }
        //如果设置为推荐版本，要将之前的推荐版本设置为正式版本
        if ((!isSnapshotOrRc(componentRecord.getVersion())) && Constants.RECORD_RECOMMEND_TYPE.equals(componentRecord.getType())) {
            ComponentRecord record = componentRecordDao.queryByType(componentRecord.getComponent_id(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                record.setType(Constants.RECORD_OFFICIAL_TYPE);
                componentRecordDao.update(record);
            }
        }
        return componentRecordDao.update(componentRecord);
    }

    /**
     * 判断是否SNAPSHOT或Rc版本
     *
     * @param version
     * @return
     */
    boolean isSnapshotOrRc(String version) {
        if (version.endsWith(Constants.SNAPSHOT)
                || version.endsWith(Constants.SNAPSHOT_LOWCASE)
                || version.contains(Constants.RC)
                || version.contains(Constants.RC_LOWCASE)) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否SNAPSHOT或Rc版本
     *
     * @param version
     * @return
     */
    boolean isAlphaOrRcOrBeta(String version) {
        if (version.contains(Constants.ALPHA)
                || version.contains(Constants.RC)
                || version.contains(Constants.RC_LOWCASE)
                || version.contains(Constants.BETA)) {
            return true;
        }
        return false;
    }


    @Override
    public ComponentRecord queryById(ComponentRecord componentRecord) throws Exception {
        return componentRecordDao.queryById(componentRecord);
    }

    /**
     * merge请求合并后操作
     *
     * @param state
     * @param iid
     * @param project_id
     * @throws Exception
     */
    @Override
    public void mergedCallBack(String state, Integer iid, Integer project_id) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);
        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            String branch = tagRecord.getBranch();
            String target_version = tagRecord.getTarget_version();
            String component_id = tagRecord.getComponent_id();
            String release_log = tagRecord.getRelease_log();
            if (Dict.MERGED.equals(merge_state)) {
                //查询项目上的所有的tag
                List<Map<String, Object>> projectTagsList = gitlabService.getProjectTagsList(gitlabId, token);
                int num = 1;
                for (Map<String, Object> tagMap : projectTagsList) {
                    String branchName = (String) tagMap.get(Dict.NAME);
                    if (branchName.contains(branch + "-RC")) {
                        num++;
                    }
                }
                String tag_name = target_version + "-RC" + num;
                if ("1.7".equals(tagRecord.getJdk())) {
                    tag_name = target_version + "-jdk7" + "-RC" + num;
                }
                String packetTag = branch + "-RC" + num;
                gitlabService.createTags(gitlabId, packetTag, Dict.MASTER, token);
                List<Map<String, String>> variables = tagRecord.getVariables();
                HashMap<String, String> version = new HashMap<>();
                version.put(Dict.KEY, Dict.VERSION);
                version.put(Dict.VALUE, tag_name);
                variables.add(version);
                Object result = gitlabService.createPipeline(gitlabId, packetTag, tagRecord.getVariables(), token);
                String pipid = CommonUtils.getPipId(result);
                ComponentRecord componentRecord = new ComponentRecord(component_id, tag_name, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME), tagRecord.getUpdate_user(), release_log, tagRecord.getJdk(), Constants.PREPACKAGE);
                componentRecord.setType(Constants.RECORD_TEST_TYPE);//如果是rc阶段打的包，设置为测试版本
                componentRecord.setPipid(pipid);
                componentRecordDao.upsert(componentRecord);
            }
        }
    }

    @Override
    public int queryCompoentRecoreds(String componentId) {
        return componentRecordDao.queryCompoentRecoreds(componentId);
    }

    /**
     * 如果mpass组件设置为废弃版本，发送所有使用此组件的应用负责人和行内负责人相关邮件
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ComponentRecord updateForMpass(Map param) throws Exception {
        ComponentRecord componentRecord = CommonUtils.map2Object(param, ComponentRecord.class);
        //校验用户权限 必须为组件管理员
        MpassComponent mpassComponent = mpassComponentDao.queryById(componentRecord.getComponent_id());
        if (!roleService.isComponentManager(mpassComponent.getManager())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足,必须为组件管理员"});
        }
        ComponentRecord result = this.updateForMpass(componentRecord);
        //如果设置为废弃版本，发送所有使用此组件的应用负责人废弃邮件
        if ((!isAlphaOrRcOrBeta(componentRecord.getVersion()))
                && StringUtils.isNotBlank(componentRecord.getType())
                && Constants.RECORD_DISCARD_TYPE.equals(componentRecord.getType())) {
            //查询用户信息
            Map<String, Object> currentUserMap = roleService.queryUserbyid(CommonUtils.getSessionUser().getId());
            //获取所有使用此组件的应用列表
            ComponentApplication componentApplication = new ComponentApplication();
            componentApplication.setComponent_id(componentRecord.getComponent_id());
            List<ComponentApplication> componentApplications = componentApplicationDao.query(componentApplication);
            Set<String> users = new HashSet<>();
            if (!CommonUtils.isNullOrEmpty(componentApplications)) {
                //1.拼接邮件内容
                String component_name_cn = mpassComponent.getName_cn();
                HashMap hashMap = new HashMap();
                hashMap.put(Dict.NAME_CN, component_name_cn);
                hashMap.put(Dict.VERSION, componentRecord.getVersion());
                hashMap.put(Dict.RELEASE_LOG, StringUtils.isNotBlank(componentRecord.getRelease_log()) ? componentRecord.getRelease_log() : "");
                String history = history_mpasscomponent_url + mpassComponent.getId();
                hashMap.put(Dict.HISTORY_MPASSCOMPONENT_URL, history);
                hashMap.put(Dict.USER_NAME_CN, currentUserMap.get(Dict.USER_NAME_CN));
                String mailContent = mailService.mpassMailDestroyContent(hashMap);
                //2. 获取所有使用此组件的负责人
                for (ComponentApplication comp : componentApplications) {
                    try {
                        Map app = appService.queryAPPbyid(comp.getApplication_id());
                        //获取应用的负责人
                        // 行内负责人
                        List<Map> spdb_managers = (List<Map>) app.get(Dict.SPDB_MANGERS);
                        users.addAll(this.getManageIds(spdb_managers));
                        // 应用负责人
                        List<Map> dev_managers = (List<Map>) app.get(Dict.DEV_MANAGERS);
                        users.addAll(this.getManageIds(dev_managers));
                    } catch (Exception e) {
                        logger.error("获取项目{}信息失败,{}", comp.getApplication_id(), e.getMessage());
                    }
                }
                //3.获取用户的邮箱
                Map<String, Object> mapId = new HashMap<>();
                mapId.put(Dict.IDS, users);
                mapId.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
                Map<String, Map> userMap = (Map<String, Map>) restTransport.submit(mapId);
                List<String> email_receivers = new ArrayList<>();
                Set<Map.Entry<String, Map>> entries = userMap.entrySet();
                if (!CommonUtils.isNullOrEmpty(entries)) {
                    for (Map.Entry<String, Map> entry : entries) {
                        try {
                            String email = (String) entry.getValue().get(Dict.EMAIL);
                            email_receivers.add(email);
                        } catch (Exception e) {
                            logger.error("获取人员邮箱信息错误" + entry.getKey());
                        }
                    }
                }
                //4.发送邮件
                HashMap<String, String> sendMap = new HashMap();
                sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                String topic = Constants.SUBJECT_DESTORY + mpassComponent.getName_cn() + "(" + componentRecord.getVersion() + ")";
                mailService.sendEmail(topic, Dict.TEMPLATE_FCOMPONENT_MPASSDESTROY, sendMap, email_receivers.toArray(new String[email_receivers.size()]));
            }
        }
        return result;
    }

    @Override
    public ComponentRecord updateForMpass(ComponentRecord componentRecord) throws Exception {
        //alpha、beta、rc只能设置为测试版本
        if (isAlphaOrRcOrBeta(componentRecord.getVersion())
                && StringUtils.isNotBlank(componentRecord.getType())
                && !Constants.RECORD_TEST_TYPE.equals(componentRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_MPASSCOMPONENTRECORD_ERROR, new String[]{});
        }
        //RELEASE版本可以修改为推荐，正式和废弃
        if ((!isAlphaOrRcOrBeta(componentRecord.getVersion()))
                && StringUtils.isNotBlank(componentRecord.getType())
                && Constants.RECORD_TEST_TYPE.equals(componentRecord.getType())) {
            throw new FdevException(ErrorConstants.UPDATE_MPASSCOMPONENTRECORD_ERROR, new String[]{});
        }
        //如果设置为推荐版本，要将之前的推荐版本设置为正式版本
        if ((!isAlphaOrRcOrBeta(componentRecord.getVersion()))
                && StringUtils.isNotBlank(componentRecord.getType())
                && Constants.RECORD_RECOMMEND_TYPE.equals(componentRecord.getType())) {
            List<ComponentRecord> recordList = componentRecordDao.queryListByType(componentRecord.getComponent_id(), Constants.RECORD_RECOMMEND_TYPE);
            if (!CommonUtils.isNullOrEmpty(recordList)) {
                for (ComponentRecord record : recordList) {
                    record.setType(Constants.RECORD_OFFICIAL_TYPE);
                    componentRecordDao.update(record);
                }
            }
        }
        return componentRecordDao.update(componentRecord);
    }

    @Override
    public List<ComponentRecord> getAlphaOrRcVersion(String componentId, String alphaOrRc, String numVersion, Boolean containPrepackage) {
        return componentRecordDao.getAlphaOrRcVersion(componentId, alphaOrRc, numVersion, containPrepackage);
    }

    @Override
    public void upsert(ComponentRecord componentRecord) throws Exception {
        componentRecordDao.upsert(componentRecord);
    }

    @Override
    public List<ComponentRecord> queryByComponentIdAndBranchs(String component_id, String numVersion, List<String> branchList) {
        return componentRecordDao.queryByComponentIdAndBranchs(component_id, numVersion, branchList);
    }

    @Override
    public List<ComponentRecord> queryByComponentIdAndBranch(String component_id, String numVersion, String feature_branch, String stage) {
        return componentRecordDao.queryByComponentIdAndBranch(component_id, numVersion, feature_branch, stage);
    }

    @Override
    public void delete(ComponentRecord componentRecord) throws Exception {
        componentRecordDao.delete(componentRecord);
    }


    @Override
    public Map<String, Object> queryIssueData(Map requestparam) throws ParseException {
        String date = (String) requestparam.get(Dict.END_DATE);
        String endTime = null;
        if (!CommonUtils.isNullOrEmpty(requestparam.get(Dict.END_DATE))) {
            endTime = CommonUtils.getTomorrowTime(date);
        }
        Map<String, Object> componentNum = componentRecordDao.queryIssueData((String) requestparam.get(Dict.START_DATE), endTime);
        Map<String, Integer> iArchetypeRecordNum = iArchetypeRecordDao.queryIssueData((String) requestparam.get(Dict.START_DATE), endTime);
        Map<String, Integer> ibaseimagerecordNum = ibaseimagerecorddao.queryIssueData((String) requestparam.get(Dict.START_DATE), endTime);
        Map<String, Integer> iMpassArchetypeNum = iMpassArchetypeDao.queryIssueData((String) requestparam.get(Dict.START_DATE), endTime);
        componentNum.put(Dict.ARCHETYPES, iArchetypeRecordNum);
        componentNum.put(Dict.BASEIMAGE, ibaseimagerecordNum);
        componentNum.put(Dict.MPASSARCHETYPES, iMpassArchetypeNum);
        return componentNum;
    }

    @Override
    public List<ComponentRecord> queryByIssueId(String id) throws Exception {
        return componentRecordDao.queryByIssueId(id);
    }

    @Override
    public void mergedSitAndReleaseCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);

        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {

                ComponentInfo componentInfo = new ComponentInfo();
                componentInfo.setId(tagRecord.getComponent_id());
                ComponentInfo component = componentInfoDao.queryById(componentInfo);

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

                //将jdkVersion放到variables
                HashMap<String, String> jdkMap = new HashMap<>();
                jdkMap.put(Dict.KEY, Dict.JDKVERSION);
                jdkMap.put(Dict.VALUE, "3-jdk-8");
                variables.add(jdkMap);

                //将version放到variables
                String packetVersion = "";
                if ("SIT".equals(target_branch)) {
                    packetVersion = "sit";
                } else {
                    packetVersion = target_branch;
                }

                HashMap<String, String> version = new HashMap<>();
                version.put(Dict.KEY, Dict.VERSION);
                version.put(Dict.VALUE, packetVersion);
                variables.add(version);
                Object result = gitlabService.createPipeline(gitlabId, target_branch, variables, token);
            }
        }
    }

    @Override
    public void mergedMasterCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception {
        String gitlabId = String.valueOf(project_id);
        String merge_state = state;
        String merge_request_id = String.valueOf(iid);

        //查tag_record表。找到这个merge的相关携带信息
        TagRecord tagRecord = tagRecordDao.findByMidAndGid(gitlabId, merge_request_id);
        if (tagRecord != null) {
            if (Dict.MERGED.equals(merge_state)) {

                gitlabService.createTags(gitlabId, tagRecord.getTarget_version(), Dict.MASTER, token);
                //保存tag
                Map<String, String> map = new HashMap<String, String>();
                map.put("app_id", tagRecord.getComponent_id());
                map.put("release_node_name", tagRecord.getRelease_node_name());
                map.put("tag", tagRecord.getTarget_version());
                iReleaseNodeService.saveTag(map);

                Object result = gitlabService.createPipeline(gitlabId, tagRecord.getTarget_version(), tagRecord.getVariables(), token);

                //将正式版本保存到record表
                String packetVersion = null;
                List<Map<String, String>> mapList = tagRecord.getVariables();
                if (mapList != null && mapList.size() > 0) {
                    for (Map map1 : mapList) {
                        if ("version".equals(map1.get("key"))) {
                            packetVersion = (String) map1.get("value");
                        }
                    }
                }
                logger.info("packetVersion:"+packetVersion);
               
                ComponentRecord componentRecord = new ComponentRecord(tagRecord.getComponent_id(), packetVersion, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                componentRecord.setPackagetype(Constants.PIPEPACKAGE);
            
                ComponentRecord record = componentRecordDao.upsert(componentRecord);
            }
        }
    }

    /**
     * 获取应用模块应用管理员的所有id
     *
     * @param list
     * @return
     */
    private Set<String> getManageIds(List<Map> list) {
        Set<String> result = new HashSet<>();
        if (!CommonUtils.isNullOrEmpty(list)) {
            for (Map map : list) {
                String id = (String) map.get(Dict.ID);
                result.add(id);
            }
        }
        return result;
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
    public void setAppService(IAppService appService) {
        this.appService = appService;
    }

    @Autowired
    public void setComponentInfoDao(IComponentInfoDao componentInfoDao) {
        this.componentInfoDao = componentInfoDao;
    }

    @Autowired
    public void setTagRecordDao(ITagRecordDao tagRecordDao) {
        this.tagRecordDao = tagRecordDao;
    }

    @Autowired
    public void setGitlabService(IGitlabSerice gitlabService) {
        this.gitlabService = gitlabService;
    }

    @Autowired
    public void setMpassComponentDao(IMpassComponentDao mpassComponentDao) {
        this.mpassComponentDao = mpassComponentDao;
    }

    @Autowired
    public void setComponentApplicationDao(IComponentApplicationDao componentApplicationDao) {
        this.componentApplicationDao = componentApplicationDao;
    }

    @Autowired
    public void setRestTransport(RestTransport restTransport) {
        this.restTransport = restTransport;
    }

    @Autowired
    public void setMailService(IMailService mailService) {
        this.mailService = mailService;
    }
}

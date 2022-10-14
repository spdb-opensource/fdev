package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.GitUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ArchetypeScanServiceImpl implements IArchetypeScanService {

    private static final Logger logger = LoggerFactory.getLogger(ArchetypeScanServiceImpl.class);

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${nexus.search}")
    private String nexus_search;

    @Value("${nexus.version.resolve}")
    private String nexus_version_resolve;

    @Value("${nexus.version.detail}")
    private String nexus_version_detail;

    @Value("${python.version}")
    private String python_version;

    @Value("${python.path}")
    private String python_path;

    @Value("${scan.skip.app.type}")
    private String scan_skip_app_type;

    @Value("${scan.app.count}")
    private int scan_app_count;

    @Value("${cache.app.isdelete}")
    private boolean cache_app_isdelete;

    @Autowired
    private IArchetypeRecordDao archetypeRecordDao;

    @Autowired
    private IAppService appService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IApplicationArchetypeDao applicationArchetypeDao;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IArchetypeInfoDao archetypeInfoDao;

    @Autowired
    private IComponentArchetypeDao componentArchetypeDao;

    @Autowired
    private GitlabServiceImpl gitlabSerice;

    @Autowired
    private IImageArchetypeService imageArchetypeService;

    /**
     * 异步操作，调用nexus接口，获取组件所有历史版本
     *
     * @param archetypeInfo
     * @throws Exception
     */
    @Override
    @Async
    public void initArchetypeHistory(ArchetypeInfo archetypeInfo) {
        try {
            int preCount = archetypeRecordDao.queryArchetypeRecoreds(archetypeInfo.getId());
            JSONArray jsonArray = commonScanService.getNexus(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId());
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    ArchetypeRecord archetypeRecord = new ArchetypeRecord();
                    archetypeRecord.setArchetype_id(archetypeInfo.getId());
                    archetypeRecord.setVersion(job.getString(Dict.VERSION));
                    // 如果是RELEASE，设置为正式版本，最新自研组件的RELEASE版本设置为推荐版本，再将之前的推荐版本设置为正式版本
                    if (archetypeRecord.getVersion().endsWith(Constants.RELEASE)) {
                        archetypeRecord.setType(Constants.RECORD_OFFICIAL_TYPE);
                        String latest = commonScanService.getLatestVersion(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId());
                        if (StringUtils.isNotBlank(latest)
                                && archetypeRecord.getVersion().equals(latest)) {
                            ArchetypeRecord record = archetypeRecordDao.queryByType(archetypeRecord.getArchetype_id(), Constants.RECORD_RECOMMEND_TYPE);
                            if (record != null) {
                                record.setType(Constants.RECORD_OFFICIAL_TYPE);
                                archetypeRecordDao.update(record);
                            }
                            archetypeRecord.setType(Constants.RECORD_RECOMMEND_TYPE);
                        }
                    }
                    // 如果是SNAPSHOT或RC，设置为测试版本，
                    if (archetypeRecord.getVersion().endsWith(Constants.SNAPSHOT)
                            || archetypeRecord.getVersion().endsWith(Constants.SNAPSHOT_LOWCASE)) {
                        archetypeRecord.setType(Constants.RECORD_TEST_TYPE);
                    }
                    //获取当前版本的推送时间
                    archetypeRecord.setDate(Util.simdateFormat(commonScanService.getLastChangeDate(job)));
                    // 如果当前组件的版本已存在，进行更新操作
                    ArchetypeRecord record = archetypeRecordDao.queryByArchetypeIdAndVersion(
                            archetypeRecord.getArchetype_id(), archetypeRecord.getVersion());
                    if (record == null) {
                        archetypeRecordDao.save(archetypeRecord);
                    } else {
                        record.setUpdate_user(archetypeRecord.getUpdate_user());
                        record.setDate(archetypeRecord.getDate());
                        record.setType(archetypeRecord.getType());
                        archetypeRecordDao.update(record);
                    }

                }
            }
            int afterCount = archetypeRecordDao.queryArchetypeRecoreds(archetypeInfo.getId());
            //判断组件历史版本是否有新增，且不为第一次录入时，做扫描组件应用关系操作
            if (preCount != 0 && afterCount != 0 && afterCount > preCount) {
                this.initArchetypeApplication(archetypeInfo);
            }
        } catch (Exception e) {
            logger.error("获取骨架{}历史信息失败,{}", archetypeInfo.getName_en(), e.getMessage());
        }
        logger.info("获取骨架{}历史信息完成", archetypeInfo.getName_en());
    }


    /**
     * 扫描骨架和应用关联
     *
     * @param archetypeInfo
     */
    @Override
    @Async
    public void initArchetypeApplication(ArchetypeInfo archetypeInfo) throws Exception {
        List<Map<String, Object>> appList = appService.getAppList();
        StringBuffer sb = new StringBuffer();
        if (appList != null && appList.size() > 0) {
            int length = appList.size();
            if (scan_app_count != 0) {
                length = scan_app_count;
            }
            for (int i = 0; i < length; i++) {
                Map map = appList.get(i);
                String id = (String) map.get(Dict.ID);
                String name_en = (String) map.get(Dict.NAME_EN);
                String git = (String) map.get(Dict.GIT);
                String type_name = (String) map.get(Dict.TYPE_NAME);
                if (ComponentEnum.AppTypeEnum.Java.getValue().equals(type_name) || ComponentEnum.AppTypeEnum.Container.getValue().equals(type_name)) {
                    try {
                        componentScanService.gitOperation(name_en, git);// 进行git clone或git pull操作
                        ApplicationArchetype applicationArchetype = this.getApplicationArchetype(id, nas_apps_path + name_en,
                                archetypeInfo);
                        if (applicationArchetype != null) {
                            // 根据组件id和应用id，判断当前应用和骨架关系是否已存在，存在则更新原有数据
                            ApplicationArchetype archetype = applicationArchetypeDao
                                    .queryByArchetypeIdAndAppId(applicationArchetype);
                            if (archetype == null) {
                                applicationArchetypeDao.save(applicationArchetype);
                            } else {
                                archetype.setUpdate_time(Util.simdateFormat(new Date()));
                                archetype.setArchetype_version(applicationArchetype.getArchetype_version());
                                applicationArchetypeDao.update(archetype);
                            }
                        } else {
                            sb.append(name_en).append(";");
                        }
                        //本地仓库的地址
                        String localRepository = nas_apps_path + name_en;
                        File partDirGit = new File(localRepository + "/.git");
                        // 扫描完成后切换回主分支
                        Git myGit = Git.open(partDirGit);// 获取myGit对象
                        GitUtils.checkoutBranch(Dict.MASTER, myGit);
                        if (myGit != null)
                            myGit.close();
                    } catch (Exception e) {
                        logger.error("扫描项目{}骨架信息失败,{}", map.get(Dict.NAME_EN), e.getMessage());
                    } finally {
                        //根据配置选择是否删除缓存应用
                        if (cache_app_isdelete) {
                            String localRepository = nas_apps_path + name_en;
                            CommonUtils.deleteFile(new File(localRepository));
                        }
                    }
                }
            }
        }
        logger.info("扫描骨架和应用关联完成");
        logger.info("sb={},size={}", sb, sb.toString().split(";").length);
    }

    /**
     * 通过groupId，Artifactid判断， 项目是否为骨架生成，如果包含则返回一条关联记录
     *
     * @param dir
     * @param archetypeInfo
     * @return
     */
    @Override
    public ApplicationArchetype getApplicationArchetype(String application_id, String dir, ArchetypeInfo archetypeInfo) throws Exception {
        List<Dependency> dependencyList = commonScanService.getDependencyList(dir, Constants.COMPONENT_ARCHETYPE);
        ApplicationArchetype applicationArchetype = null;
        if (dependencyList != null && dependencyList.size() > 0) {
            for (Dependency dependency : dependencyList) {
                if (archetypeInfo.getGroupId().equals(dependency.getGroupId())
                        && archetypeInfo.getArtifactId().equals(dependency.getArtifactId())) {
                    applicationArchetype = new ApplicationArchetype();
                    applicationArchetype.setArchetype_id(archetypeInfo.getId());
                    applicationArchetype.setApplication_id(application_id);
                    applicationArchetype.setArchetype_version(dependency.getVersion());
                    applicationArchetype.setUpdate_time(Util.simdateFormat(new Date()));
                    break;
                }
            }
        }
        return applicationArchetype;
    }

    @Override
    public void scanComponentArchetype(String archetype_id, String version) {
        String name_en = null;
        try {
            List<ComponentArchetype> result = new ArrayList<>();
            List<ComponentInfo> componentInfoList = componentInfoDao.query(new ComponentInfo());
            ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetype_id);
            name_en = archetypeInfo.getName_en();
            String git = archetypeInfo.getGitlab_url() + ".git";
            componentScanService.gitOperation(name_en, git, version);
            for (ComponentInfo componentInfo : componentInfoList) {
                try {
                    ComponentArchetype componentArchetype = this.getComponentArchetype(archetype_id, version, nas_apps_path + name_en, componentInfo);
                    if (componentArchetype != null) {
                        //根据组件id和骨架id及骨架版本，判断当前版本骨架和组件关系是否已存在，存在则更新原有数据
                        ComponentArchetype archetype = componentArchetypeDao.queryByArchetypeIdAndAppId(componentArchetype);
                        if (archetype == null) {
                            componentArchetypeDao.save(componentArchetype);
                        } else {
                            archetype.setUpdate_time(Util.simdateFormat(new Date()));
                            archetype.setComponent_version(componentArchetype.getComponent_version());
                            componentArchetypeDao.update(archetype);
                        }
                        result.add(componentArchetype);
                    }
                } catch (Exception e) {
                    logger.error("扫描组件{}失败,{}", componentInfo.getName_cn(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("扫描骨架{}失败,{}", name_en, e.getMessage());
        } finally {
            CommonUtils.deleteFile(new File(nas_apps_path + name_en));
        }
    }

    /**
     * 通过groupId，Artifactid判断， 项目依赖列表中是否包含组件，如果包含则返回一条关联记录
     *
     * @param dir
     * @param component
     * @return
     */
    @Override
    public ComponentArchetype getComponentArchetype(String archetype_id, String archetype_version, String dir, ComponentInfo component)
            throws Exception {
        List<Dependency> dependencyList = commonScanService.getDependencyList(dir, Constants.COMPONENT_COMPONENT);
        ComponentArchetype componentArchetype = null;
        if (dependencyList != null && dependencyList.size() > 0) {
            for (Dependency dependency : dependencyList) {
                if (component.getGroupId().equals(dependency.getGroupId())
                        && component.getArtifactId().equals(dependency.getArtifactId())) {
                    componentArchetype = new ComponentArchetype();
                    componentArchetype.setComponent_id(component.getId());
                    componentArchetype.setArchetype_id(archetype_id);
                    componentArchetype.setArchetype_version(archetype_version);
                    componentArchetype.setComponent_version(dependency.getVersion());
                    componentArchetype.setUpdate_time(Util.simdateFormat(new Date()));
                    break;
                }
            }
        }
        return componentArchetype;
    }

    /**
     * 获取当前骨架使用镜像情况
     *
     * @param archetype_id
     * @param version
     */
    @Override
    public void scanImageArchetype(String archetype_id, String version) throws Exception {
        ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(archetype_id);
        String gitlabId = gitlabSerice.queryProjectIdByUrl(archetypeInfo.getGitlab_url());
        Integer gitId = Integer.parseInt(gitlabId);
        String fileDir = "ci%2Fgitlab-ci%2FDockerfile";
        String dockerFileContent = gitlabSerice.getGitLabFileContent(gitId, fileDir, version);
        if (dockerFileContent.contains("\r\n")) {
            dockerFileContent = dockerFileContent.replace("\r\n", "\n");
        }
        String[] contentSplit = dockerFileContent.split("\n");
        for (int i = 0; i < contentSplit.length; i++) {
            String line = contentSplit[i];
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (line.startsWith("FROM")) {
                String[] split = line.split("/");
                String imageName = split[split.length - 1].trim();
                String[] image = imageName.split(":");
                ImageArchetype imageArchetype = new ImageArchetype();
                imageArchetype.setArchetype_id(archetype_id);
                imageArchetype.setArchetype_version(version);
                imageArchetype.setImage_name(image[0]);
                imageArchetype.setImage_tag(image[1]);
                imageArchetype.setUpdate_time(Util.simdateFormat(new Date()));
                ImageArchetype query = imageArchetypeService.queryByArchetypeIdAndImageName(imageArchetype);
                if (null == query) {
                    imageArchetypeService.save(imageArchetype);
                } else {
                    query.setUpdate_time(imageArchetype.getUpdate_time());
                    query.setImage_tag(imageArchetype.getImage_tag());
                    imageArchetypeService.update(query);
                }
                break;
            }
        }
    }
}

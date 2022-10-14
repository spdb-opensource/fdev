package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.*;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.runnable.RunnableFactory;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ComponnetScanServiceImpl implements IComponentScanService {

    private static final Logger logger = LoggerFactory.getLogger(ComponnetScanServiceImpl.class);

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${nexus.search}")
    private String nexus_search;

    @Value("${nexus.version.detail}")
    private String nexus_version_detail;

    @Value("${nexus.version.resolve}")
    private String nexus_version_resolve;

    @Value("${python.path}")
    private String python_path;

    @Value("${python.version}")
    private String python_version;

    @Value("${gitlab.manager.username}")
    private String name;

    @Value("${gitlab.manager.password}")
    private String password;

    @Value("${lock.scanComponent.timeout}")
    private int lock_scancmp_timeout;

    @Value("${scan.skip.app.type}")
    private String scan_skip_app_type;

    @Value("${scan.app.count}")
    private int scan_app_count;

    @Value("${cache.app.isdelete}")
    private boolean cache_app_isdelete;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IAppService appService;

    @Autowired
    private IComponentApplicationDao componentApplicationDao;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IMpassComponentDao mpassComponentDao;

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Autowired
    private IGitlabUserService gitlabUserService;

    @Autowired
    private RunnableFactory runnableFactory;

    @Autowired
    private IImageApplicationDao imageApplicationDao;

    /**
     * 调用app模块接口获取所有应用，判断进行git clone或者git pull操作
     *
     * @param name_en 项目名
     * @param git     git路径
     */
    @Override
    public void gitOperation(String name_en, String git) {
        // 本地仓库的 地址
        String localRepository = nas_apps_path + name_en;
        File partDirGit = new File(localRepository + "/.git");
        try {
            // 首次本地没有项目的时候进行克隆
            if (!partDirGit.exists()) {
                // git clone
                GitUtilsNew.gitCloneFromGitlab(git, localRepository);
                logger.info(git + "克隆项目文件时成功");
            }
            GitUtilsNew.gitPullFromGitlab(localRepository);
        } catch (Exception e) {
            logger.error("未查询到当前分支信息,{}", e.getMessage());
            CommonUtils.deleteFile(new File(localRepository));
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST);//当前分支不存在
        }
    }

    /**
     * 调用app模块接口获取所有应用，判断进行git clone或者git pull操作
     * 根据传入分支，进行分支切换
     *
     * @param name_en 项目名
     * @param git     git路径
     */
    @Override
    public void gitOperation(String name_en, String git, String version) {
        // 本地仓库的 地址
        String localRepository = nas_apps_path + name_en;
        File partDirGit = new File(localRepository + "/.git");
        Git gitBranch = null;
        try {
            // 首次本地没有项目的时候进行克隆
            if (!partDirGit.exists()) {
                // git clone
                GitUtilsNew.gitCloneFromGitlab(git, localRepository);
                logger.info(git + "克隆项目文件时成功");
            }
            GitUtilsNew.gitPullFromGitlab(localRepository);
            if (version.contains(Constants.SNAPSHOT)) {
                gitBranch = GitUtils.getGit(localRepository);
                if (!GitUtils.checkLocalExisitBranch(gitBranch, version)) {//判断本地仓库是否存在对应分支
                    GitUtils.createLocalBranchAndRealectRemotBranch(gitBranch, version, version);
                } else {
                    if (!version.equals(gitBranch.getRepository().getBranch()))//判断本地仓库是否是对应的环境分支，若不是切切换当前环境分支
                        GitUtils.checkoutBranch(version, gitBranch);
                }
                GitUtils.gitPullFromGitlab(gitBranch, name, password, version);//通过指定分支pull远程分支至本地
            }
        } catch (Exception e) {
            logger.error("未查询到当前分支信息,{}", e.getMessage());
            CommonUtils.deleteFile(new File(localRepository));
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST, new String[]{version});//当前分支不存在
        } finally {
            if (null != gitBranch)
                gitBranch.close();
        }
    }

    /**
     * 异步操作，调用nexus接口，获取组件所有历史版本
     *
     * @param componentInfo
     * @throws Exception
     */
    @Override
    @Async
    public void initComponentHistory(ComponentInfo componentInfo) {
        try {
            int preCount = componentRecordDao.queryCompoentRecoreds(componentInfo.getId());
            JSONArray jsonArray = commonScanService.getNexus(componentInfo.getGroupId(), componentInfo.getArtifactId());
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    ComponentRecord componentRecord = new ComponentRecord();
                    componentRecord.setComponent_id(componentInfo.getId());
                    componentRecord.setVersion(job.getString(Dict.VERSION));
                    // 如果是RELEASE，设置为正式版本，最新自研组件的RELEASE版本设置为推荐版本，再将之前的推荐版本设置为正式版本
                    if (componentRecord.getVersion().endsWith(Constants.RELEASE)) {
                        componentRecord.setType(Constants.RECORD_OFFICIAL_TYPE);
                        String latest = commonScanService.getLatestVersion(componentInfo.getGroupId(), componentInfo.getArtifactId());
                        ComponentInfo info = componentInfoDao.queryById(componentInfo.getId());
                        if (StringUtils.isNotBlank(latest)
                                && componentRecord.getVersion().equals(latest)
                                && Constants.INNER_SOURCE.equals(info.getSource())) {
                            ComponentRecord record = componentRecordDao.queryByType(componentRecord.getComponent_id(), Constants.RECORD_RECOMMEND_TYPE);
                            if (record != null) {
                                record.setType(Constants.RECORD_OFFICIAL_TYPE);
                                componentRecordDao.update(record);
                            }
                            componentRecord.setType(Constants.RECORD_RECOMMEND_TYPE);
                        }
                    }
                    // 如果是SNAPSHOT或RC，设置为测试版本，
                    if (componentRecord.getVersion().contains(Constants.RC)
                            || componentRecord.getVersion().contains(Constants.RC_LOWCASE)
                            || componentRecord.getVersion().endsWith(Constants.SNAPSHOT)
                            || componentRecord.getVersion().endsWith(Constants.SNAPSHOT_LOWCASE)) {
                        componentRecord.setType(Constants.RECORD_TEST_TYPE);
                    }
                    //获取当前版本的推送时间
                    componentRecord.setDate(Util.simdateFormat(commonScanService.getLastChangeDate(job)));
                    // 组件jdk版本不为空时，同步设置组件历史版本字段
                    if (StringUtils.isNotBlank(componentInfo.getJdk_version())) {
                        componentRecord.setJdk_version(componentInfo.getJdk_version());
                    }
                    // 如果当前组件的版本已存在，进行更新操作
                    ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(
                            componentRecord.getComponent_id(), componentRecord.getVersion());
                    if (record == null) {
                        componentRecordDao.save(componentRecord);
                    } else {
                        record.setUpdate_user(componentRecord.getUpdate_user());
                        record.setDate(componentRecord.getDate());
                        record.setType(componentRecord.getType());
                        componentRecordDao.update(record);
                    }

                }
            }
            int afterCount = componentRecordDao.queryCompoentRecoreds(componentInfo.getId());
            //判断组件历史版本是否有新增，且不为第一次录入时，做扫描组件应用关系操作
            if (preCount != 0 && afterCount != 0 && afterCount > preCount) {
                this.initComponentApplication(componentInfo);
            }
        } catch (Exception e) {
            logger.error("获取组件{}历史信息失败,{}", componentInfo.getName_en(), e.getMessage());
        }

    }

    /**
     * 针对app模块的应用，进行git pull和git clone操作 异步操作，扫描fdev录入应用扫描组件的使用情况 添加组件和应用关系
     *
     * @param componentInfo
     */
    @Override
    @Async
    public void initComponentApplication(ComponentInfo componentInfo) throws Exception {
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
                    logger.info("开始扫描{}", type_name);
                    try {
                        this.gitOperation(name_en, git);// 进行git clone或git pull操作
                        ComponentApplication componentApplication = this.getComponentApplication(id, nas_apps_path + name_en,
                                componentInfo);
                        if (componentApplication != null) {
                            // 获取最新的组件版本，判断应用中用到的是否最新版本，0：是，1：不是
                            String latestVersion = commonScanService.getLatestVersion(componentInfo.getGroupId(), componentInfo.getArtifactId());
                            if (StringUtils.isNotBlank(latestVersion)
                                    && componentApplication.getComponent_version().equals(latestVersion)) {
                                componentApplication.setIsLatest(Constants.LATEST);
                            } else {
                                componentApplication.setIsLatest(Constants.NOTLATEST);
                            }
                            // 根据组件id和应用id，判断当前应用和组件关系是否已存在，存在则更新原有数据
                            ComponentApplication application = componentApplicationDao
                                    .queryByComponentIdAndAppId(componentApplication);
                            if (application == null) {
                                componentApplicationDao.save(componentApplication);
                            } else {
                                application.setUpdate_time(Util.simdateFormat(new Date()));
                                application.setComponent_version(componentApplication.getComponent_version());
                                application.setIsLatest(componentApplication.getIsLatest());
                                componentApplicationDao.update(application);
                            }
                        } else {
                            sb.append(name_en).append(";");
                        }
                    } catch (Exception e) {
                        logger.error("扫描应用{}使用组件情况失败{}", map.get(Dict.NAME_EN), e.getMessage());
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
        logger.info("扫描Java应用使用组件完成");
        logger.info("sb=" + sb + "size=" + sb.toString().split(";").length);
    }

    /**
     * 通过groupId，Artifactid判断， 项目依赖列表中是否包含组件，如果包含则返回一条关联记录
     *
     * @param dir
     * @param component
     * @return
     */
    @Override
    public ComponentApplication getComponentApplication(String application_id, String dir, ComponentInfo component)
            throws Exception {
        List<Dependency> dependencyList = commonScanService.getDependencyList(dir, Constants.COMPONENT_COMPONENT);
        return getComponentApplication(application_id, dir, component, dependencyList);
    }

    /**
     * 通过groupId，Artifactid判断， 项目依赖列表中是否包含组件，如果包含则返回一条关联记录
     *
     * @param dir
     * @param component
     * @return
     */
    @Override
    public ComponentApplication getComponentApplication(String application_id, String dir, ComponentInfo component, List<Dependency> dependencyList) {
        ComponentApplication componentApplication = null;
        if (dependencyList != null && dependencyList.size() > 0) {
            for (Dependency dependency : dependencyList) {
                if (component.getGroupId().equals(dependency.getGroupId())
                        && component.getArtifactId().equals(dependency.getArtifactId())) {
                    componentApplication = new ComponentApplication();
                    componentApplication.setComponent_id(component.getId());
                    componentApplication.setApplication_id(application_id);
                    componentApplication.setComponent_version(dependency.getVersion());
                    componentApplication.setUpdate_time(Util.simdateFormat(new Date()));
                    break;
                }
            }
        }
        return componentApplication;
    }

    /**
     * 通过npm坐标name判断， 如果包含组，则拼接为@npmGroup/npmName
     * 项目依赖列表中是否包含组件，如果包含则返回一条关联记录
     *
     * @param dir
     * @param component
     * @return
     */
    @Override
    public ComponentApplication getComponentApplication(String application_id, String dir, MpassComponent component)
            throws Exception {
        List<Map> dependencyList = commonScanService.getDependencyMapList(dir, Constants.COMPONENT_MPASS_COMPONENT);
        return getComponentApplication(application_id, dir, component, dependencyList);
    }

    /**
     * 通过npm坐标name判断， 如果包含组，则拼接为@npmGroup/npmName
     * 项目依赖列表中是否包含组件，如果包含则返回一条关联记录
     *
     * @param dir
     * @param component
     * @return
     */
    @Override
    public ComponentApplication getComponentApplication(String application_id, String dir, MpassComponent component, List<Map> dependencyList) {
        ComponentApplication componentApplication = null;
        if (dependencyList != null && dependencyList.size() > 0) {
            for (Map map : dependencyList) {
                String mpassKey = StringUtils.isNotBlank(component.getNpm_group()) ? "@" + component.getNpm_group() + "/" + component.getNpm_name() : component.getNpm_name();
                if (mpassKey.equals(map.get(Dict.NAME))) {
                    componentApplication = new ComponentApplication();
                    componentApplication.setComponent_id(component.getId());
                    componentApplication.setApplication_id(application_id);
                    componentApplication.setComponent_version((String) map.get(Dict.VERSION));
                    componentApplication.setUpdate_time(Util.simdateFormat(new Date()));
                    break;
                }
            }
        }
        return componentApplication;
    }

    /**
     * 调用python脚本 生成maven项目
     */
    @Override
    @Async
    public void initMavenArcheType(String path, String groupId, String artifactId, String applicationName,
                                   String http_url_to_repo) throws Exception {
        Git git = null;
        String localTempProjectPath = path + artifactId;
        String localTempArchePath = path + Dict.ARCHETYPE;
        String gitlabId = gitlabSerice.queryProjectIdByUrl(http_url_to_repo.substring(0, http_url_to_repo.length() - 4));
        gitlabUserService.changeFdevRole(gitlabId, Dict.MAINTAINER);//设置权限，增加为Maintainer
        GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, Dict.MASTER);// 将待持续集成的项目clone到本地
        String[] cmdA = {"python", python_path + "initMaven.py", localTempArchePath, groupId, artifactId,
                applicationName};
        Process pr = Runtime.getRuntime().exec(cmdA);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));){
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();
            pr.waitFor();
            // 将生成的maven项目拷贝到本地临时项目
            FileUtils.copyDirectory(new File(localTempArchePath + File.separator + artifactId),
                    new File(localTempProjectPath));
            // 生成的pom文件增加distributionManagement节点
            this.addDistributionManagementToPom(localTempProjectPath + File.separator + Dict.POM);
            git = GitUtils.getGit(localTempProjectPath);
            GitUtils.gitPushFromGitlab(git, "生成maven项目并推送", name, password);// 将本地临时项目推送到远程
            logger.info("生成maven项目成功");
        } catch (Exception e) {
            logger.error("生成maven项目失败,{}", e.getMessage());
        } finally {
            if (git != null) {
                git.close();
            }
            CommonUtils.deleteFile(new File(localTempArchePath + File.separator + artifactId));// 清除本地项目临时仓库
            CommonUtils.deleteFile(new File(localTempProjectPath));// 清除本地项目临时仓库
            gitlabUserService.changeFdevRole(gitlabId, Dict.REPORTER);//回收权限，重新设置为Reporter
        }
    }

    @Override
    @Async
    public void initImageApplication(BaseImageInfo baseImageInfo) throws Exception {
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
                Integer gitlab_id = (Integer) map.get(Dict.GITLAB_PROJECT_ID);
                try {
                    String fileDir = "gitlab-ci%2FDockerfile";
                    String dockerFileContent = gitlabSerice.getGitLabFileContent(gitlab_id, fileDir, Dict.MASTER);
                    if (dockerFileContent.contains("\r\n")) {
                        dockerFileContent = dockerFileContent.replace("\r\n", "\n");
                    }
                    String[] contentSplit = dockerFileContent.split("\n");
                    for (int j = 0; j < contentSplit.length; j++) {
                        String line = contentSplit[j];
                        if (StringUtils.isEmpty(line)) {
                            continue;
                        }
                        if (line.startsWith("FROM")) {
                            String[] split = line.split("/");
                            String imageName = split[split.length - 1].trim();
                            String[] image = imageName.split(":");
                            ImageApplication imageApplication = new ImageApplication();
                            imageApplication.setApplication_id(id);
                            imageApplication.setImage_name(image[0]);
                            imageApplication.setImage_tag(image[1]);
                            imageApplication.setUpdate_time(Util.simdateFormat(new Date()));
                            //查询当前应用和镜像关联是否存在，存在则进行更新
                            ImageApplication query = imageApplicationDao.queryByApplicationAndImage(id, imageApplication.getImage_name());
                            if (null == query) {
                                imageApplicationDao.save(imageApplication);
                            } else {
                                query.setUpdate_time(imageApplication.getUpdate_time());
                                query.setImage_tag(imageApplication.getImage_tag());
                                imageApplicationDao.update(query);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("扫描应用{}使用镜像情况失败,{}", map.get(Dict.NAME_EN), e.getMessage());
                }
            }
        }
        logger.info("扫描应用使用镜像完成");
    }

    @Override
    public void initMultiComponentHistory(ComponentInfo componentInfo) throws Exception {
        int preCount = componentRecordDao.queryCompoentRecoreds(componentInfo.getId());
        JSONArray jsonArray = commonScanService.getMultiNexus(componentInfo.getGroupId(), componentInfo.getArtifactId());
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject job = jsonArray.getJSONObject(i);
                ComponentRecord componentRecord = new ComponentRecord();
                componentRecord.setComponent_id(componentInfo.getId());
                componentRecord.setVersion(job.getString(Dict.VERSION));
                // 如果是RELEASE，设置为正式版本，最新自研组件的RELEASE版本设置为推荐版本，再将之前的推荐版本设置为正式版本
                if (componentRecord.getVersion().endsWith(Constants.RELEASE)) {
                    componentRecord.setType(Constants.RECORD_OFFICIAL_TYPE);
                    //TODO 多模块组件 暂时无法查询最新版本
                    /*String latest = commonScanService.getLatestVersion(componentInfo.getGroupId(), componentInfo.getArtifactId());
                    ComponentInfo info = componentInfoDao.queryById(componentInfo.getId());
                    if (StringUtils.isNotBlank(latest)
                            && componentRecord.getVersion().equals(latest)
                            && Constants.INNER_SOURCE.equals(info.getSource())) {
                        ComponentRecord record = componentRecordDao.queryByType(componentRecord.getComponent_id(), Constants.RECORD_RECOMMEND_TYPE);
                        if (record != null) {
                            record.setType(Constants.RECORD_OFFICIAL_TYPE);
                            componentRecordDao.update(record);
                        }
                        componentRecord.setType(Constants.RECORD_RECOMMEND_TYPE);
                    }*/
                }
                // 如果是SNAPSHOT或RC，设置为测试版本，
                if (componentRecord.getVersion().contains(Constants.RC)
                        || componentRecord.getVersion().contains(Constants.RC_LOWCASE)
                        || componentRecord.getVersion().endsWith(Constants.SNAPSHOT)
                        || componentRecord.getVersion().endsWith(Constants.SNAPSHOT_LOWCASE)) {
                    componentRecord.setType(Constants.RECORD_TEST_TYPE);
                }
                //获取当前版本的推送时间
                //componentRecord.setDate(Util.simdateFormat(commonScanService.getLastChangeDate(job)));
                // 组件jdk版本不为空时，同步设置组件历史版本字段
                if (StringUtils.isNotBlank(componentInfo.getJdk_version())) {
                    componentRecord.setJdk_version(componentInfo.getJdk_version());
                }
                // 如果当前组件的版本已存在，进行更新操作
                ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(
                        componentRecord.getComponent_id(), componentRecord.getVersion());
                if (record == null) {
                    componentRecordDao.save(componentRecord);
                } else {
                    record.setUpdate_user(componentRecord.getUpdate_user());
                    record.setDate(componentRecord.getDate());
                    record.setType(componentRecord.getType());
                    componentRecordDao.update(record);
                }

            }
        }
        int afterCount = componentRecordDao.queryCompoentRecoreds(componentInfo.getId());
        //判断组件历史版本是否有新增，且不为第一次录入时，做扫描组件应用关系操作
        if (preCount != 0 && afterCount != 0 && afterCount > preCount) {
            this.initComponentApplication(componentInfo);
        }
    }



    /**
     * 获取mpass所有组件版本
     *
     * @param mpassComponent
     */
    @Override
    @Async
    public void initMpassComponentHistory(MpassComponent mpassComponent) {
        try {
            int preCount = componentRecordDao.queryCompoentRecoreds(mpassComponent.getId());
            MpassComponent mpassComponentNew = mpassComponentDao.queryById(mpassComponent.getId());
            String result = ShellUtils.cellShell(mpassComponentNew.getNpm_name(), mpassComponentNew.getNpm_group(), true);
            if (StringUtils.isNotBlank(result) && result.length() > 1) {
                String[] versionArray = result.substring(1, result.length() - 1).split(",");
                if (!CommonUtils.isNullOrEmpty(versionArray)) {
                    String updateDate = Util.simdateFormat(new Date());
                    for (int i = versionArray.length - 1; i > 0; i--) {
                        String version = versionArray[i];
                        version = version.replaceAll("[\\[|\\]|\\']", "").trim();
                        ComponentRecord componentRecord = new ComponentRecord();
                        componentRecord.setComponent_id(mpassComponentNew.getId());
                        componentRecord.setType(Constants.RECORD_OFFICIAL_TYPE);//设置为正式版本
                        componentRecord.setVersion(version);
                        componentRecord.setDate(updateDate);//设置时间
                        // 如果是alpha、beta或rc，设置为测试版本，
                        if (componentRecord.getVersion().contains(Constants.ALPHA)
                                || componentRecord.getVersion().contains(Constants.BETA)
                                || componentRecord.getVersion().contains(Constants.RC_LOWCASE)) {
                            componentRecord.setType(Constants.RECORD_TEST_TYPE);
                        }
                        // 如果当前组件的版本已存在，进行更新操作
                        ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(
                                componentRecord.getComponent_id(), componentRecord.getVersion());
                        if (record == null) {
                            componentRecordDao.save(componentRecord);
                        } else {
                            record.setUpdate_user(componentRecord.getUpdate_user());
                            if (StringUtils.isBlank(record.getDate()))
                                record.setDate(componentRecord.getDate());
                            record.setType(componentRecord.getType());
                            componentRecordDao.update(record);
                        }
                    }
                }
            }
            int afterCount = componentRecordDao.queryCompoentRecoreds(mpassComponent.getId());
            //判断组件历史版本是否有新增，且不为第一次录入时，做扫描组件应用关系操作
            if (preCount != 0 && afterCount != 0 && afterCount > preCount) {
                this.initMpassComponentApplication(mpassComponentNew);
            }
        } catch (Exception e) {
            logger.error("获取Mpass组件{}历史信息失败,{}", mpassComponent.getName_en(), e.getMessage());
        }
    }

    /**
     * 获取应用依赖前端组件情况
     *
     * @param mpassComponent
     * @throws Exception
     */
    @Override
    @Async
    public void initMpassComponentApplication(MpassComponent mpassComponent) throws Exception {
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
                if (ComponentEnum.AppTypeEnum.Vue.getValue().equals(type_name)) {
                    try {
                        this.gitOperation(name_en, git);// 进行git clone或git pull操作
                        ComponentApplication componentApplication = this.getComponentApplication(id, nas_apps_path + name_en,
                                mpassComponent);
                        if (componentApplication != null) {
                            // 根据组件id和应用id，判断当前应用和组件关系是否已存在，存在则更新原有数据
                            ComponentApplication application = componentApplicationDao
                                    .queryByComponentIdAndAppId(componentApplication);
                            if (application == null) {
                                componentApplicationDao.save(componentApplication);
                            } else {
                                application.setUpdate_time(Util.simdateFormat(new Date()));
                                application.setComponent_version(componentApplication.getComponent_version());
                                application.setIsLatest(componentApplication.getIsLatest());
                                componentApplicationDao.update(application);
                            }
                        } else {
                            sb.append(name_en).append(";");
                        }
                    } catch (Exception e) {
                        logger.error("扫描Vue应用{}使用Mpass组件情况失败,{}", map.get(Dict.NAME_EN), e.getMessage());
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
        logger.info("扫描Vue应用使用Mpass组件完成");
        logger.info("result={},size={}", sb, sb.toString().split(";").length);
    }


    /**
     * 给pom文件增加distributionManagement节点
     *
     * @param path
     * @throws Exception
     */
    private void addDistributionManagementToPom(String path) throws Exception {
        File file = new File(path);
        Document document = null;
        Element root = null;
        SAXReader reader = new SAXReader();
        document = reader.read(path);
        root = document.getRootElement();
        Element distributionManagement = root.addElement("distributionManagement");
        // 增加repository节点
        Element repository = distributionManagement.addElement("repository");
        Element repositoryid = repository.addElement(Dict.ID);
        repositoryid.setText("releases");
        Element repositoryurl = repository.addElement(Dict.URL);
        repositoryurl.setText("${nexus.release.url}");
        // 增加snapshotRepository节点
        Element snapshotRepository = distributionManagement.addElement("snapshotRepository");
        Element snapshotRepositoryid = snapshotRepository.addElement(Dict.ID);
        snapshotRepositoryid.setText("snapshots");
        Element snapshotRepositoryurl = snapshotRepository.addElement(Dict.URL);
        snapshotRepositoryurl.setText("${nexus.snapshots.url}");
        // 写入pom文件
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setEncoding("UTF-8");
        XMLWriter xmlWriter = new XMLWriter(new FileWriter(file), outputFormat);
        xmlWriter.write(document);
        xmlWriter.close();
    }
}

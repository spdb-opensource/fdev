package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class SonarScanServiceImpl implements MergeEventService {

    private static Logger logger = LoggerFactory.getLogger(SonarScanServiceImpl.class);

    @Value("${sonar.switch}")
    private boolean sonarSwitch;
    @Value("${git.clone.user}")
    private String gitCloneUser;
    @Value("${git.clone.password}")
    private String gitClonePwd;
    @Value("${dsonar.host}")
    private String dSonarHost;
    @Value("${dsonar.scm.provider}")
    private String dSonarScmProvider;
    @Value("${dsonar.login}")
    private String dSonarLogin;
    @Value("${sonar.image.name}")
    private String sonarImageName;
    @Value("${fdev.env}")
    private String fdevEnv;
    @Value("${sonar.environment.slug}")
    private String sonarEnvironmentSlug;
    @Value("${branch.specific.symbol}")
    private String branchSpecificSymbol;
    @Value("${sonar.file}")
    private String sonarFile;
    @Value("${kafka.topic}")
    private String kafkaTopic;

    @Resource
    private GitLabService gitLabService;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private RequestService requestService;

    private static String jsonString;

    static {
        jsonString = CommonUtil.getTemplateJson("kafka-template/sonar.json");
    }

    @Override
    public void doMerge(Map<String, Object> parse) {
        logger.info("SonarScanServiceImpl");
        // 解析object_attributes
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        String iid = String.valueOf(attributes.get(Dict.IID));
        String sourceBranch = (String) attributes.get(Dict.SOURCE_BRANCH);
        // 合并的目标分支
        String mergeBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        String state = (String) attributes.get(Dict.STATE);
        // 解析project
        Map<String, Object> projectInfo = (Map<String, Object>) parse.get(Dict.PROJECT);
        String gitHttpUrl = (String) projectInfo.get(Dict.GIT_HTTP_URL);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        String projectName = (String) projectInfo.get(Dict.NAME);
        if (!Dict.SIT_UP.equals(mergeBranch) || !Dict.MERGED.equals(state)) {
            return;
        }
        logger.info("合并到SIT并且合并成功，应用名：{}，源分支：{}", projectName, sourceBranch);
        // 合到SIT & 合并成功，sonar扫描
        if (!sonarSwitch) {
            return;
        }
        // 获取应用相关信息
        Map<String, Object> appInfoMap = getAppInfoMap(projectId);
        if (checkAppType((String) appInfoMap.get(Dict.TYPE_ID))) {
            logger.info("非Java微服务不扫描，应用名：{}", projectName);
            return;
        }
        // 扫描源分支
        String appNameEn = (String) appInfoMap.get(Dict.NAME_EN);
        sonarScan(projectId, projectName, appNameEn, sourceBranch, gitHttpUrl);
        // 扫描目标分支
        sonarScan(projectId, projectName, appNameEn, mergeBranch, gitHttpUrl);
    }

    /**
     * 扫描指定分支
     *
     * @param projectId   应用GitLab的 project id
     * @param projectName 应用GitLab的 project name
     * @param appNameEn   应用英文名
     * @param branch      扫描分支
     * @param gitHttpUrl  git clone地址
     */
    private void sonarScan(Integer projectId, String projectName, String appNameEn, String branch, String gitHttpUrl) {
        // 根据projectId获取应用英文名
        String projectKey = appNameEn;
        String sonarProjectName = appNameEn;
        String projectDir = " /ebank/devops/" + projectName;
        String diffFile = "";
        String logDir = "";
        StringBuilder sonarTaskIdCmd = new StringBuilder();
        if (!Dict.SIT_UP.equals(branch)) {
            String specificBranch = this.replaceSpecificSymbol(branch);
            projectKey = appNameEn + "::" + specificBranch;
            sonarProjectName = appNameEn + "::" + specificBranch;
            // 更新任务模块的扫描时间
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(Dict.FEATURE_ID, branch);
            paramMap.put(Dict.WEB_NAME_EN, appNameEn);
            String scanTime = TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME);
            paramMap.put(Dict.SCAN_TIME, scanTime);
            requestService.updateTaskSonar(paramMap);
            logger.info("nameEn:{},branch:{},scanTime:{}", appNameEn, branch, scanTime);
            // 增量扫描，只扫与master分支的差异文件
            diffFile = this.getDiffFile(projectId, Dict.MASTER, branch);
            if (StringUtils.isEmpty(diffFile)) {
                paramMap.remove(Dict.SCAN_TIME);
                paramMap.put(Dict.SONAR_ID, "0000000");
                requestService.updateTaskSonar(paramMap);
                return;
            }
            // feature分支日志重定向
            logDir = "/fwebhook/sonar/" + appNameEn + "-" + specificBranch + ".log";
            // 组装获取sonar task id的命令
            String sonarReportPath = projectDir + "/target/sonar/report-task.txt";
            sonarTaskIdCmd.append(" ; sonar.py ")
                    .append(fdevEnv).append(" ")
                    .append(appNameEn).append(" ").append(branch).append(" ").append(sonarReportPath);
        }
        StringBuilder sonarCommand = new StringBuilder();
        sonarCommand.append("git clone -b \\\"").append(branch).append("\\\"")
                .append(gitHttpUrl.replace("http://", " http://" + gitCloneUser + ":" + gitClonePwd + "@"))
                .append(" ").append(projectDir).append(" && cd ").append(projectDir);
        sonarCommand.append(" && mvn clean package -U -Dmaven.test.skip=true");
        if (!Dict.SIT_UP.equals(branch)) {
            sonarCommand.append(" > ").append(logDir);
        }
        sonarCommand.append(" && mvn sonar:sonar -Dsonar.host.url=").append(dSonarHost)
                .append(" -Dsonar.login=").append(dSonarLogin)
                .append(" -Dsonar.scm.provider=").append(dSonarScmProvider)
                .append(" -Dsonar.projectKey=").append(projectKey)
                .append(" -Dsonar.projectName=").append(sonarProjectName);
        if (StringUtils.isNotEmpty(diffFile)) {
            sonarCommand.append(" -Dsonar.inclusions=").append(diffFile);
        }
        long currentTimeMillis = System.currentTimeMillis();
        String logDirCmd = " >> " + logDir;
        if (StringUtils.isNotEmpty(sonarTaskIdCmd.toString())) {
            sonarCommand.append(logDirCmd).append(sonarTaskIdCmd).append(" ").append(logDir);
        }
        String sonarData = jsonString.replace(Constants.SONAR_NAME, Dict.SONAR + "-" + projectName)
                .replace(Constants.TIMESTAMP, currentTimeMillis + "")
                .replace(Constants.SONAR_IMAGE_NAME, sonarImageName)
                .replace(Constants.SONAR_COMMAND, sonarCommand)
                .replace(Constants.SONAR_ENVIRONMENT_SLUG, sonarEnvironmentSlug)
                .replace(Constants.PROIECT_ID, projectId + "")
                .replace(Constants.PROJECT_NAME, appNameEn)
                .replace(Constants.BRANCH, branch);
        // 发送数据
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(Dict.PROJECT_ID, projectId);
        kafkaService.sendData(kafkaTopic, sonarData.getBytes(), logMap);
    }

    /**
     * 获取变更文件路径
     *
     * @param projectId
     * @param from
     * @param to
     * @return
     */
    private String getDiffFile(Integer projectId, String from, String to) {
        Map<String, Object> compare = gitLabService.compare(projectId, from, to);
        List<Map<String, Object>> diffList = (List<Map<String, Object>>) compare.get(Dict.DIFFS);
        // 无代码变更
        if (CollectionUtils.isEmpty(diffList)) {
            return "";
        }
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < diffList.size(); i++) {
            Map<String, Object> diffMap = diffList.get(i);
            String newPath = (String) diffMap.get(Dict.NEW_PATH);
            // 判断是否为指定扫描的文件类型
            String[] sonarFileList = sonarFile.split(";");
            for (String fileSuffix : sonarFileList) {
                if (newPath.endsWith(fileSuffix)) {
                    pathBuilder.append(newPath);
                    if (i != diffList.size() - 1) {
                        pathBuilder.append(",");
                    }
                    break;
                }
            }
        }
        return pathBuilder.toString();
    }

    /**
     * 替换分支中的特殊符号
     *
     * @param branch
     * @return
     */
    private String replaceSpecificSymbol(String branch) {
        String specificBranch = branch;
        String[] specificSymbolSplit = branchSpecificSymbol.split(";");
        for (String specificSymbol : specificSymbolSplit) {
            if (specificBranch.contains(specificSymbol)) {
                specificBranch = specificBranch.replace(specificSymbol, "_");
            }
        }
        return specificBranch;
    }

    /**
     * 判断应用类型
     *
     * @param appTypeId
     * @return
     */
    private boolean checkAppType(String appTypeId) {
        boolean flag = true;
        String typeName = getTypeName(appTypeId);
        if (Constants.APP_TYPE_JAVA.equals(typeName)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 对应用的部分信息进行懒加载
     *
     * @param projectId
     * @return
     */
    @LazyInitProperty(redisKeyExpression = "fwebhook.appTypeId.{projectId}")
    private Map<String, Object> getAppInfoMap(Integer projectId) {
        Map<String, Object> appInfoMap = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.ID, projectId);
        param.put(Dict.REST_CODE, "getAppByGitId");
        Map<String, Object> appMap;
        try {
            appMap = (Map<String, Object>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.OTHER_APP_ERROR, new String[]{"调应用模块接口(/fapp/api/app/getAppByGitId)出错：" + e.getMessage()});
        }
        appInfoMap.put(Dict.NAME_EN, appMap.get(Dict.NAME_EN));
        appInfoMap.put(Dict.TYPE_ID, appMap.get(Dict.TYPE_ID));
        return appInfoMap;
    }

    /**
     * 对应用类型进行懒加载
     *
     * @param typeId
     * @return
     */
    @LazyInitProperty(redisKeyExpression = "fwebhook.appTypeName.{typeId}")
    private String getTypeName(String typeId) {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.ID, typeId);
        param.put(Dict.REST_CODE, "queryAppType");
        List<Map<String, Object>> appTypeList;
        try {
            appTypeList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.OTHER_APP_ERROR, new String[]{"调应用模块接口(/fapp/api/type/query)出错：" + e.getMessage()});
        }
        Map<String, Object> appTypeMap = appTypeList.get(0);
        return (String) appTypeMap.get(Dict.LABEL);
    }

}
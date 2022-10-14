package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.EmailConstants;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdRecordDao;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IAsyncAutoReleaseService;
import com.spdb.fdev.release.service.IKafkaService;
import com.spdb.fdev.release.service.IMailService;
import com.spdb.fdev.release.transport.GitlabTransport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class AsyncAutoReleaseServiceImpl implements IAsyncAutoReleaseService {
	private static final Logger logger = LoggerFactory.getLogger(AsyncAutoReleaseServiceImpl.class);
	@Value("${scripts.path}")
	private String scripts_path;
	@Value("${gitlab.manager.username}")
	private String gitCloneUser;
	@Value("${gitlab.manager.password}")
	private String gitClonePwd;
	@Value("${fdev.release.branch}")
	private String branch;
	@Value("${fdev.release.projectId}")
	private String projectId;
	@Value("${fdev.release.gitHttpUrl}")
	private String gitHttpUrl;
	@Value("${fdev.release.projectName}")
	private String projectName;
	@Value("${fdev.release.releaseImageName}")
	private String releaseImageName;
	@Autowired
	private IProdRecordDao prodRecordDao;
	@Autowired
	private Environment enviroment;
	private static String jsonString;

	@Value("${gitlab.api.url}")
	private String gitApiUrl;

	@Value("${release.deploy.url}")
	private String deployUrl;
	
	@Autowired
    private IMailService mailService;
	
	@Autowired
	IKafkaService kafkaService;

	static {
		jsonString = CommonUtils.getTemplateJson("kafka-template/release.json");
	}

	@Async("taskExecutor")
	@Override
	public void autoRelease(String prod_id) throws Exception {
		String host_name = enviroment.getProperty(Dict.HOSTNAME);
		String CI_ENVIRONMENT_SLUG = enviroment.getProperty(Dict.CI_ENVIRONMENT_SLUG);
		ProdRecord prodRecord = prodRecordDao.queryDetail(prod_id);
		String image_deliver_type = prodRecord.getImage_deliver_type();
		String scc_flag = prodRecord.getScc_prod();
		if(CommonUtils.isNullOrEmpty(scc_flag)){
		    scc_flag = "0"; //非scc变更给个默认值，防止传空会阻止脚本往下走
        }
		String currentTimeMillis = CommonUtils.formatDate(CommonUtils.RELEASEDATE);
		String projectDir = "/workspace/ebank_fdev/devops/" + projectName+"_"+currentTimeMillis;
		try {
			prodRecordDao.saveHostName(prod_id,host_name);
			StringBuilder releaseCommand = new StringBuilder();
			if(!Constants.IMAGE_DELIVER_TYPE_NEW.equals(image_deliver_type)){
				releaseCommand.append("python").append(" ").append("de_auto_release.py ").append(" ").append(prod_id);
			}else{
				releaseCommand.append("python").append(" ").append("auto_release.py ").append(" ").append(prod_id).append(" ").append(scc_flag);
			}
			String releaseData = jsonString.replace(Constants.RELEASE_NAME, Dict.RELEASE)
					.replace(Constants.TIMESTAMP, currentTimeMillis)
					.replace(Constants.RELEASE_COMMAND, releaseCommand)
					.replace(Constants.RELEASE_IMAGE_NAME, releaseImageName)
					.replace(Constants.CI_PROJECT_NAME, projectName)
					.replace(Constants.CI_PROJECT_URL, gitHttpUrl)
					.replace(Constants.CI_PROJECT_BRANCH, branch)
					.replace(Constants.CI_ENVIRONMENT_SLUG, CI_ENVIRONMENT_SLUG)
					.replace(Constants.CI_PROJECT_DIR, projectDir);
			// 发送数据
			kafkaService.sendData(Dict.RELEASE, projectName, releaseData);
		}catch (FdevException fe){
			if (ErrorConstants.AUTO_RELEASE_FAILED.equals(fe.getCode())){
				prodRecordDao.updateStatus(prod_id, Constants.PROD_RECORD_STSTUS_FAILED);
			}
			throw fe;
		}
	}

	@Async("taskExecutor")
	@Override
	public void cacheImage(String app_name_en, String pro_image_uri) throws Exception {
		try {
			logger.info("begin caching image: {}, image_url: {}" , app_name_en, pro_image_uri);
			CommonUtils.runPythonThreeParamter(scripts_path + "cache_image.py", pro_image_uri, pro_image_uri.split(":")[1], app_name_en);
			logger.info("end caching image: {}, image_url: {}" , app_name_en, pro_image_uri);
		} catch (FdevException fe) {
			logger.info("error caching image: {}, image_url: {}" , app_name_en, pro_image_uri);
			logger.error("缓存镜像失败", fe);
		}
	}

	@Async("dockerImageExecutor")
	@Override
	public void pushDockerImage(Integer gitlab_project_id, String name_en, String pro_image_uri, String fake_image_uri,
								String release_node_name, String version, String prod_id, String application_id,
								String logPath, String logFileName, String type,String caasEnv,String sccEnv) {
		try {
			logger.info("应用[{}]在{}投产窗口下开始异步推送镜像,变更版本号{}", name_en, release_node_name, version);

			String CI_ENVIRONMENT_SLUG = enviroment.getProperty(Dict.CI_ENVIRONMENT_SLUG);
			String currentTimeMillis = CommonUtils.formatDate(CommonUtils.RELEASEDATESS);
			String projectDir = "/workspace/ebank_fdev/devops/" + projectName+"_"+currentTimeMillis;
			StringBuilder releaseCommand = new StringBuilder();
			releaseCommand.append("python").append(" ").append("push_docker_image.py")
					.append(" ").append(gitlab_project_id)
					.append(" ").append(name_en)
					.append(" ").append(pro_image_uri)
					.append(" ").append(CommonUtils.isNullOrEmpty(fake_image_uri)?"None":fake_image_uri)
					.append(" ").append(prod_id)
					.append(" ").append(application_id)
					.append(" ").append(logPath)
					.append(" ").append(logFileName)
					.append(" ").append(type)
					.append(" ").append(caasEnv)
					.append(" ").append(sccEnv);
			String releaseData = jsonString.replace(Constants.RELEASE_NAME, Dict.RELEASE)
					.replace(Constants.TIMESTAMP, currentTimeMillis)
					.replace(Constants.RELEASE_COMMAND, releaseCommand)
					.replace(Constants.RELEASE_IMAGE_NAME, releaseImageName)
					.replace(Constants.CI_PROJECT_NAME, projectName)
					.replace(Constants.CI_PROJECT_URL, gitHttpUrl)
					.replace(Constants.CI_PROJECT_BRANCH, branch)
					.replace(Constants.CI_ENVIRONMENT_SLUG, CI_ENVIRONMENT_SLUG)
					.replace(Constants.CI_PROJECT_DIR, projectDir);
			// 发送数据
			kafkaService.sendData(Dict.RELEASE, projectName, releaseData);
			logger.info("应用[{}]在{}投产窗口下异步推送镜像完成,变更版本号{}", name_en, release_node_name, version);
		} catch (FdevException fe) {
			logger.info("应用[{}]在{}投产窗口下异步推送镜像出现异常,变更版本号{}", name_en, release_node_name, version);
		}

	}

	@Override
	public void deploy(Map<String,Object> requestParam) throws Exception {
		String appName = (String) requestParam.get("appName");
		String appGitUrl = (String) requestParam.get("appGitUrl");
		String branch = (String) requestParam.get("branch");
		String deployBranch = (String) requestParam.get("deployBranch");
		String appId = (String) requestParam.get("appId");
		List<String> emails = (List<String>) requestParam.get("emails");
		
		try {
			String currentTimeMillis = CommonUtils.formatDate(CommonUtils.RELEASEDATESS);
			String releaseData = jsonString.replace(Constants.RELEASE_NAME, Dict.RELEASE)
					.replace(Constants.TIMESTAMP, currentTimeMillis)
					.replace(Constants.RELEASE_COMMAND, "python /opt/fdev-helper/deploy_retail_sit2.py")
					.replace(Constants.RELEASE_IMAGE_NAME, deployUrl)
					.replace(Constants.CI_PROJECT_NAME, appName)
					.replace(Constants.CI_PROJECT_URL, appGitUrl)
					.replace(Constants.CI_PROJECT_ID, appId)
					.replace(Constants.CI_PROJECT_BRANCH, branch)
					.replace(Constants.CI_ENVIRONMENT_SLUG, deployBranch)
					.replace(Constants.CI_API_V4_URL, gitApiUrl)
					.replace(Constants.CI_PROJECT_DIR, "/workspace/ebank_fdev/devops/"+ appName + "_" + currentTimeMillis);
			// 发送数据
			kafkaService.sendData(Dict.RELEASE, projectName, releaseData);
		}catch (FdevException fe){
			throw fe;
		}
		
		HashMap<String, String> model = new HashMap<String, String>();
    	model.put("project_name", appName);
        try {
			mailService.sendEmail("SIT2环境部署申请，审批已通过。","frelease_taskDeployReviews", model, emails);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

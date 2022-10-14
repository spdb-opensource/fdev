package com.spdb.fdev.release.service.impl;

import java.util.*;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.EmailConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.service.IMailService;
import com.spdb.fdev.release.service.ISendEmailService;
import com.spdb.fdev.release.service.IUserService;

@Service
@RefreshScope
public class SendEmailServiceImpl implements ISendEmailService{
	@Autowired
	private IMailService mailService;
	@Autowired
	IUserService userService;
	@Value("${fdev.email.control.enabled}")
	private boolean emailControlEnabled;
	@Value("${link.port}")
	private String port;

    private static Logger logger_batch = LoggerFactory.getLogger("logger_batch");


	@Async("taskExecutor")
	@Override
	public void sendEmailReviews(List<HashMap> model) throws Exception {
		if(emailControlEnabled) {	
			List<String> users=new ArrayList<>();
			for (HashMap map : model) {	
				List devManagers = (List) map.get(Dict.SPDB_MASTER);
				for (Object object : devManagers) {
					Map userMap=(Map) object; 
					users.add((String)userMap.get(Dict.USER_NAME_EN));
				}
				List bankManager=(List)map.get(Dict.MASTER);
				for (Object object : bankManager) {
					Map userMap=(Map) object; 
					users.add((String)userMap.get(Dict.USER_NAME_EN));
				}			
				List<String> email = new ArrayList<>();
				for (int i = 0; i < users.size(); i++) {
					Map<String, Object> userinfo=userService.queryUserInfo(users.get(i));
					if(!CommonUtils.isNullOrEmpty(userinfo)) {
						email.add((String)userinfo.get(Dict.EMAIL));
					}
				}
				map.put(Dict.PORT, port);

				Map taskReview = (Map)map.get(Dict.REVIEW);
				List dataBaseAlter = (List)taskReview.get(Dict.DATA_BASE_ALTER);
				if(!CommonUtils.isNullOrEmpty(dataBaseAlter)){
					Map dataBaseAlterContend = (Map)dataBaseAlter.get(0);
					if("是".equals(dataBaseAlterContend.get(Dict.NAME)) && !(Boolean)dataBaseAlterContend.get(Dict.AUDIT)){
						mailService.sendEmail(EmailConstants.TITLE_TASKDBREVIEWS,EmailConstants.EMAIL_TASKDBREVIEWS, map, email);
					}else{
						mailService.sendEmail(EmailConstants.TITLE_TASKREVIEWS,EmailConstants.EMAIL_TASKREVIEWS, map, email);
					}
				} else{
					mailService.sendEmail(EmailConstants.TITLE_TASKREVIEWS,EmailConstants.EMAIL_TASKREVIEWS, map, email);
				}
			}
		}
	}
	
	@Async("taskExecutor")
	@Override
	public void sendEmailUpdateNode(HashMap model) throws Exception {
		if(emailControlEnabled) {			
			List<Map> users = (List<Map>) model.get(Dict.USER);
			List<String> email = new ArrayList<>();
			for (int i = 0; i < users.size(); i++) {
				String username=(String) users.get(i).get("user_name_en");
				Map<String, Object> userinfo=userService.queryUserInfo(username);
				if(!CommonUtils.isNullOrEmpty(userinfo)) {
					email.add((String)userinfo.get(Dict.EMAIL));
				}
			}
			model.put(Dict.PORT, port);
			mailService.sendEmail(EmailConstants.TITLE_RELEASEUPDATENODE,EmailConstants.EMAIL_RELEASEUPDATENODE, model, email);		
		}
	}

	@Override
	public void sendEmailCleanDelayedTasks(HashMap model) throws Exception {
		if(emailControlEnabled) {
			List<String> users = new ArrayList<>();
			//行内负责人
			List<Map<String, String>> spdb_masters = (List<Map<String, String>>) model.get(Dict.SPDB_MASTER);
			for (Map<String, String> spdb : spdb_masters) {
				users.add(spdb.get(Dict.USER_NAME_EN));
			}
			//任务负责人
			List<Map<String, String>> masters = (List<Map<String, String>>) model.get(Dict.MASTER);
			for (Map<String, String> master : masters) {
				users.add(master.get(Dict.USER_NAME_EN));
			}
			//开发人员
			List<Map<String, String>> developers = (List<Map<String, String>>) model.get(Dict.DEVELOPER);
			for (Map<String, String> developer : developers) {
				users.add(developer.get(Dict.USER_NAME_EN));
			}
			List<String> email = new ArrayList<>();
			// 通过英文名称获取邮箱
			for (String name : users) {
				Map<String, Object> userinfo = userService.queryUserInfo(name);
				if(!CommonUtils.isNullOrEmpty(userinfo)) {
					email.add((String)userinfo.get(Dict.EMAIL));
				}
			}
			// 投产窗口的端口号
			model.put(Dict.PORT, port);
			mailService.sendEmail(EmailConstants.TITLE_RELEASETASKNOTICE, EmailConstants.EMAIL_RELEASETASKNOTICE, model, email);
		}
	}

	@Override
	public void sendConfigCheckNotify(HashMap model) throws Exception {
			List<String> users = new ArrayList<>();
			// 行内负责人
			List<Map<String, String>> spdb_managers = (List<Map<String, String>>) model.get(Dict.SPDB_MANAGERS);
			for (Map<String, String> manager : spdb_managers) {
				users.add(manager.get(Dict.USER_NAME_EN));
			}
			// 应用负责人
			List<Map<String, String>> dev_managers = (List<Map<String, String>>) model.get(Dict.DEV_MANAGERS);

			for(Iterator iterator = dev_managers.iterator(); iterator.hasNext();){
				Map<String,String> manager = (Map)iterator.next();
				if(!users.contains(manager.get(Dict.USER_NAME_EN))){
					users.add(manager.get(Dict.USER_NAME_EN));
				}
			}

			mailService.sendUserNotify(Constants.CONFIG_CHECKOUT_NOTIFY, users, Constants.CONFIG_FILE_REFRESH, (String)model.get(Dict.HYPERLINK), "0");

	}

	@Override
	public void sendEmailRqrmntConfirmNotify(HashMap model) throws Exception {
		List email = new ArrayList ((Set)model.get(Dict.EMAIL));
		email.remove(null);
		if(CommonUtils.isNullOrEmpty(email)){
			return;
		}
		mailService.sendEmail(EmailConstants.RQRMNT_CONFIRM_FILE_NOTIFY, EmailConstants.RQRMNT_CONFIRM_FILE, model, email);
	}

	@Override
	public void sendEmailRqrmntConfirmNotifyNotAllow(HashMap model) throws Exception {
		List email = new ArrayList ((Set)model.get(Dict.EMAIL));
		email.remove(null);
		if(CommonUtils.isNullOrEmpty(email)){
			return;
		}
		mailService.sendEmail(EmailConstants.RQRMNT_CONFIRM_FILE_NOTIFY_NOT_ALLOW, EmailConstants.RQRMNT_CONFIRM_FILE_NOT_ALLWO, model, email);
	}
}

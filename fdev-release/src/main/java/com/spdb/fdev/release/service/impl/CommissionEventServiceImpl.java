package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class CommissionEventServiceImpl implements ICommissionEventService{
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IReleaseTaskService releaseTaskService;
	@Autowired
	private IMailService mailService;
	@Value("${link.port}")
	private String port;

	@Override
	public void addAuditTaskCommissionEvent(String task_id, String type) throws Exception {
		Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
		Map<String, Object> map=new HashMap<>();
		List<Map<String, String>> users =(List<Map<String, String>>) taskInfo.get(Dict.SPDB_MASTER);
		Set<String> ids = new HashSet<>();
		List<String> user_name_ens = new ArrayList<>();
		for (Map<String, String> user : users) {
			ids.add(user.get(Dict.ID));
			user_name_ens.add(user.get(Dict.USER_NAME_EN));
		}
		User user = CommonUtils.getSessionUser();
		ReleaseTask releaseTask = releaseTaskService.findOneTask(task_id,type);
		String link = "xxx" + port + "/fdev/#/release/list/"
				+ releaseTask.getRelease_node_name() + "/joblist";
		map.put(Dict.USER_IDS, new ArrayList<>(ids));
		map.put(Dict.CREATE_USER_ID, user.getId());
		map.put(Dict.TARGET_ID, task_id);
		map.put(Dict.MODULE, Dict.RELEASE);
		map.put(Dict.DESCRIPTION, "任务投产待确认");
		map.put(Dict.LINK, link);
		map.put(Dict.TYPE, Dict.AUDIT_TASK);
		// 推送实时消息在右上角提示
		//type=3试运行待确认
		if("3".equals(type)){
			mailService.sendUserNotify(taskInfo.get(Dict.NAME) + "--" + Constants.CONFIRM_TESTRUN, user_name_ens, Constants.CONFIRM_TESTRUN, link, "0");
		}else {
			// 添加待办
			userService.addCommissionEvent(map);
			mailService.sendUserNotify(taskInfo.get(Dict.NAME) + "--" + Constants.CONFIRM_RELEASE, user_name_ens, Constants.CONFIRM_RELEASE, link, "0");
		}
	}

	@Override
	public void updateAuditTaskCommissionEvent(String task_id) throws Exception {
		Map<String, Object> map = new HashMap<>();
		User user = CommonUtils.getSessionUser();
		map.put(Dict.TARGET_ID, task_id);
		map.put(Dict.TYPE, Dict.AUDIT_TASK);
		map.put(Dict.EXECUTOR_ID, user.getId());
		map.put(Dict.MODULE, Dict.RELEASE);
		userService.updateCommissionEvent(map);	
	}
	
	@Override
	public void updateTaskArchivedCommissionEvent(String task_id) throws Exception {
		Map<String, Object> map = new HashMap<>();
		User user = CommonUtils.getSessionUser();
		map.put(Dict.TARGET_ID, task_id);
		map.put(Dict.TYPE, Dict.TASK_ARCHIVED);
		map.put(Dict.EXECUTOR_ID, user.getId());
		map.put(Dict.MODULE, Dict.RELEASE);
		userService.updateCommissionEvent(map);
	}

	@Override
	public void deleteCommissionEvent(String task_id) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.TARGET_ID, task_id);
		map.put(Dict.TYPE, Dict.AUDIT_TASK);
		map.put(Dict.MODULE, Dict.RELEASE);
		userService.deleteCommissionEvent(map);
	}

	@Override
	public void addRelDevopsCommissionEvent(Map<String, Object> application, String saveId, String id) throws Exception{
		Map<String, Object> map = new HashMap<>();
		List<Map<String, String>> dev_mangers = (List<Map<String, String>>) application.get(Dict.DEV_MANAGERS);
		List<Map<String, String>> spdb_mangers = (List<Map<String, String>>) application.get(Dict.SPDB_MANAGERS);
		dev_mangers.addAll(spdb_mangers);
		List<String> userIds = getUserIds(dev_mangers);
		User user = CommonUtils.getSessionUser();
		String git_url = (String)application.get(Dict.GIT);
		String link = git_url.replace(".git","") + "/merge_requests/" + id;
		map.put(Dict.USER_IDS, userIds);
		map.put(Dict.CREATE_USER_ID, user.getId());
		map.put(Dict.TARGET_ID, saveId);
		map.put(Dict.MODULE, Dict.RELEASE);
		map.put(Dict.DESCRIPTION, "任务提交发布");
		map.put(Dict.LINK, link);
		map.put(Dict.TYPE, Dict.RELEASE_PUBLISH);
		userService.addCommissionEvent(map);
	}

	@Override
	public void updateCommissionEvent(String targetId, String userId, String type) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.TARGET_ID, targetId);
		map.put(Dict.TYPE, type);
		map.put(Dict.EXECUTOR_ID, userId);
		map.put(Dict.MODULE, Dict.RELEASE);
		userService.updateCommissionEvent(map);
	}

    @Override
    public void addAuditConfigChangedCommission(List<Map<String, String>> managerList,
                                                String targetId, String release_node_name) throws Exception {
	    Map<String, Object> map = new HashMap<>();
        List<String> userIds = getUserIds(managerList);
        String link = "xxx" + port + "/fdev/#/release/list/"
                + release_node_name + "/applist";
        map.put(Dict.USER_IDS, userIds);
        map.put(Dict.TARGET_ID, targetId);
        map.put(Dict.MODULE, Dict.RELEASE);
        map.put(Dict.DESCRIPTION, "投产应用配置文件比对确认");
        map.put(Dict.LINK, link);
        map.put(Dict.TYPE, Dict.RELEASE_APPLICATION_CONFIG_CHANGED);
        userService.addCommissionEvent(map);
    }

    /**
	 * 获取行内负责人和应用负责人id
	 * @param list
	 * @return
	 */
	private List<String> getUserIds(List<Map<String, String>> list){
		Set<String> ids = new HashSet<>();
		for (Map<String, String> user : list) {
			ids.add(user.get(Dict.ID));
		}
		return new ArrayList<>(ids);
	}
}

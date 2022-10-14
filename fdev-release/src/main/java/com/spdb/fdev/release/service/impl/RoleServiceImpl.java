package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IProdRecordService;
import com.spdb.fdev.release.service.IRoleService;
import com.spdb.fdev.release.service.ITaskService;
import com.spdb.fdev.release.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class RoleServiceImpl implements IRoleService {
	@Value("${fdev.role.control.enabled}")
	private boolean roleControlEnabled;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IProdRecordService prodRecordService;
	@Autowired
	private UserVerifyUtil userVerifyUtil;
	@Autowired
	private ReleaseNodeServiceImpl releaseNodeService;

	@Autowired
	IAppService appService;

	private Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

	
	/**
	 *  @param group_id 组id
	 */
	@Override
	public boolean isGroupReleaseManager(String group_id) throws Exception {
		if (roleControlEnabled) {
			// 通过用户英文名获取用户详情
			User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getSession().getAttribute(Dict._USER);
			if(CommonUtils.isNullOrEmpty(user)) {
				return false;
			}
            if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                logger.info("卡点管理员操作！！！");
                return true;
            }
			boolean flag = false;
			List<Map<String, Object>> groups = userService.queryChildGroupByGroupId(user.getGroup_id());
            // 查找当前用户所属的第三层级组
			Map<String,Object> threeLevelGroup = userService.getThreeLevelGroup(user.getGroup_id());
			if(!CommonUtils.isNullOrEmpty(threeLevelGroup)){
				String threeLevelId = (String) threeLevelGroup.get(Dict.ID);
				String status = (String) threeLevelGroup.get(Dict.STATUS);
				if(!"0".equals(status)){ // 0-废弃
					if(!user.getGroup_id().equals(threeLevelId)){ // 认为当前用户所属组小于第三层级组
						groups = userService.queryChildGroupByGroupId(threeLevelId);
					}
				}
			}
			// 判断用户角色是否为投产管理员
			List<String> role_id = user.getRole_id();
			if (role_id.contains(Constants.ROLE_RELEASE)) {
				for (Map<String, Object> map : groups) {// 且该投产窗口的id为该用户 组id或自组id
					if (group_id.equals(map.get(Dict.ID))) {
						flag = true;
						break;
					}
				}
			}
			return flag;
		}
		return true;

	}

	/**
	 * 投产管理员权限
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isReleaseManager() throws Exception {
		if (roleControlEnabled) {
			User user = CommonUtils.getSessionUser();
			List<String> role_id = user.getRole_id();
			if (role_id.contains(Constants.ROLE_RELEASE)){
				return  true;
			}
            if(userVerifyUtil.userRoleIsSPM(role_id)) {
                logger.info("卡点管理员操作！！！");
                return true;
            }
			return false;
		}
		return true;
	}

	@Override
	public boolean isTaskSpdbManager(String task_id) throws Exception {
		if (roleControlEnabled) {
			Map taskInfo = taskService.queryTaskInfo(task_id);
			if (CommonUtils.isNullOrEmpty(taskInfo)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关任务信息" });
			}
            User user = CommonUtils.getSessionUser();
            if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                logger.info("卡点管理员操作！！！");
                return true;
            }
			// * 当前操作人员operate_user（session中获取“_USER”）必须为该任务的行内负责人
			List<Map> task_spdb_managers = (List<Map>) taskInfo.get(Dict.SPDB_MASTER);
			if (CommonUtils.isNullOrEmpty(task_spdb_managers)) {
				throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[] { "任务信息未包含行内负责人信息" });
			}
			boolean isTaskSpdbManager = false;
			for (Map task_manager : task_spdb_managers) {
				if (task_manager != null && task_manager.get(Dict.ID).equals(CommonUtils.getSessionUser().getId())) {
					isTaskSpdbManager = true;
				}
			}
			return isTaskSpdbManager;
		}
		return true;
	}

	@Override
	public boolean isTaskManager(String task_id) throws Exception {
		if (roleControlEnabled) {
			Map taskInfo = taskService.queryTaskInfo(task_id);
			if (CommonUtils.isNullOrEmpty(taskInfo)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关任务信息" });
			}
            User user = CommonUtils.getSessionUser();
            if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                logger.info("卡点管理员操作！！！");
                return true;
            }
			// 当前操作人员operate_user（session中获取“_USER”）必须为该任务的任务负责人或行内负责人
			List<Map> task_managers = (List<Map>) taskInfo.get(Dict.MASTER);
			List<Map> spdb_managers = (List<Map>) taskInfo.get(Dict.SPDB_MASTER);
			task_managers.addAll(spdb_managers);
			if (CommonUtils.isNullOrEmpty(task_managers)) {
				throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[] { "任务信息未包含任务负责人信息" });
			}
			boolean isTaskManager = false;
			for (Map task_manager : task_managers) {
				// 遍历任务管理员列表 若与session中的用户id相同则返回为真
				if (task_manager != null && task_manager.get(Dict.ID).equals(CommonUtils.getSessionUser().getId())) {
					isTaskManager = true;
				}
			}
			return isTaskManager;
		}
		return true;
	}

	@Override
	public boolean isApplicationManager(String application_id) throws Exception {
		if (roleControlEnabled) {
			Map<String, Object> application = appService.queryAPPbyid(application_id);
			if (CommonUtils.isNullOrEmpty(application)
					|| CommonUtils.isNullOrEmpty(application.get(Dict.DEV_MANAGERS))) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "无法查询到应用信息" });
			}
			User user = CommonUtils.getSessionUser();
			if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
				logger.info("卡点管理员操作！！！");
				return true;
			}
			List<Map<String, String>> list = (List<Map<String, String>>) application.get(Dict.DEV_MANAGERS);
			List<Map<String, String>> spdb_list = (List<Map<String, String>>) application.get(Dict.SPDB_MANAGERS);
			list.addAll(spdb_list);
			if (CommonUtils.isNullOrEmpty(list)) {
				logger.error("can't find this app info! @@@@@appid={}", application_id);
				throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[] { "应用信息未包含应用负责人信息" });
			}
			boolean flag = false;
			for (Map<String, String> map : list) {
				if (map != null && map.get(Dict.USER_NAME_EN).equals(user.getUser_name_en())) {
					flag = true;
				}
			}
			return flag;
		}
		return true;
	}

	@Override
	public boolean isAppSpdbManager(String application_id) throws Exception {
		if (roleControlEnabled) {
			Map<String, Object> application = appService.queryAPPbyid(application_id);
			if (CommonUtils.isNullOrEmpty(application)
					|| CommonUtils.isNullOrEmpty(application.get(Dict.SPDB_MANAGERS))) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "无法查询到应用信息" });
			}
			User user = CommonUtils.getSessionUser();
            if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                logger.info("卡点管理员操作！！！");
                return true;
            }
			List<Map<String, String>> list = (List<Map<String, String>>) application.get(Dict.SPDB_MANAGERS);
			if (CommonUtils.isNullOrEmpty(list)) {
				logger.error("can't find this app info! @@@@@appid={}", application_id);
				throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[] { "应用信息未包含应用负责人信息" });
			}
			boolean flag = false;
			for (Map<String, String> map : list) {
				if (map != null && map.get(Dict.USER_NAME_EN).equals(user.getUser_name_en())) {
					flag = true;
				}
			}
			return flag;
		}
		return true;
	}

	@Override
	public boolean isChildGroup(String group_id, String target_group_id) throws Exception {
			List<Map<String, Object>> groups = userService.queryChildGroupByGroupId(target_group_id);
			for (Map<String, Object> map : groups) {
				String id = (String) map.get(Dict.ID);
				if (id.equals(group_id)) {
					return true;
				}
			}
			return false;
	}

	@Override
	public boolean isParentGroup(String group_id, String target_group_id) throws Exception {
			List<Map<String, Object>> groups = userService.queryParentGroupByGroupId(target_group_id);
			for (Map<String, Object> map : groups) {
				String id = (String) map.get(Dict.ID);
				if (id.equals(group_id)) {
					return true;
				}
			}
			return false;
	}

	@Override
	public void isGroupReleaseManagerByProd(String prod_id) throws Exception {
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
		if (CommonUtils.isNullOrEmpty(prodRecord)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"prod not exist"});
		}
		User user = CommonUtils.getSessionUser();
		if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
			logger.info("卡点管理员操作！！！");
		}else {
			if (!isGroupReleaseManager(prodRecord.getOwner_groupId())) {
				throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"must be group release manager"});
			}
		}
	}

	@Override
	public boolean isUserOperatorProd(String prod_id) throws Exception {
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
		if(CommonUtils.isNullOrEmpty(prodRecord)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST , new String [] {"prod not exist"});
		}
		if(!isGroupReleaseManager(prodRecord.getOwner_groupId())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isReleaseContact(String releaseNodeName) throws Exception {
		if (roleControlEnabled) {
			// 通过用户英文名获取用户详情
			User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getSession().getAttribute(Dict._USER);
			if(CommonUtils.isNullOrEmpty(user)) {
				return false;
			}
			if(userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
				logger.info("卡点管理员操作！！！");
				return true;
			}
			boolean flag = false;
			ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
			List<String> contact = releaseNode.getRelease_contact();
			for(String ContactNameEn : contact){
				if(user.getUser_name_en().equals(ContactNameEn)){
					flag = true;
					break;
				}
			}
			return flag;
		}
		return true;
	}
}

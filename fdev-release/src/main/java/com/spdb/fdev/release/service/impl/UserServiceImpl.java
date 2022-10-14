package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.ICacheService;
import com.spdb.fdev.release.service.IUserService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送User模块的业务接口实习类
 */

/**
 * 发送用户模块的业务实现类 2019年3月26日
 */
@Service
public class UserServiceImpl implements IUserService {
	@Autowired
	private RestTransport transport;

	@Autowired
	IAppService appService;

	@Autowired
	ICacheService cacheService;

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	public String queryAppManagerGitlabToken(String application_id) throws Exception {
		//发app模块获取应用信息
		Map<String, Object> application = appService.queryAPPbyid(application_id);
		//获取该应用的应用负责人列表
		if(CommonUtils.isNullOrEmpty(application)) {
			logger.error("can't find appcation info !@@@@@appid={}", application_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String[]{"无法获取应用负责人gitlab token"});
		}
		List<Map<String, String>> manager_list = (List<Map<String, String>>) application.get(Dict.DEV_MANAGERS);
		if(CommonUtils.isNullOrEmpty(manager_list)) {
			logger.error("can't find appcation manager list!@@@@@appid={}", application_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String [] {"无法获取应用负责人gitlab token"});
		}
		String token = null;
		// 遍历应用负责人
		for(Map<String, String> app_manager_map : manager_list) {
		    try {
                token = cacheService.queryUserGitlabToken(app_manager_map.get(Dict.USER_NAME_EN));
            } catch(Exception e) {
                logger.error("can't find appcation manager token!@@@@@user_name={}", app_manager_map.get(Dict.USER_NAME_EN));
            }
			if(!CommonUtils.isNullOrEmpty(token)) {
				break;
			}
		}
		if(CommonUtils.isNullOrEmpty(token)) {
			logger.error("can't find appcation manager token!@@@@@appid={}", application_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String[]{"无法获取应用负责人gitlab token"});
		}
		return token;
	}

	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.userdetail.{user_name_en}")
	public Map<String, Object> queryUserInfo(String user_name_en) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.USER_NAME_EN, user_name_en);
		map.put(Dict.REST_CODE, "queryUser");
		List<Map<String, Object>> info = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(info)) {
			logger.error("can't find user info !@@@@@ usernameen={}", user_name_en);
			return null;
		}
		return  info.get(0);
	}

	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.groupdetail.{group_id}")
	public Map<String, Object> queryGroupDetail(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, group_id);
		map.put(Dict.REST_CODE, "queryGroup");
		List<Map<String, Object>> submit = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(submit)) {
			logger.error("can't find group info !@@@@@groupid={}", group_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String [] {"获取组信息失败"});
		}
		return submit.get(0);
	}

	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.childgroup.{group_id}")
	public List<Map<String, Object>> queryChildGroupByGroupId(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, group_id);
		map.put(Dict.REST_CODE, "queryChildGroupById");
		List<Map<String, Object>> submit = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(submit)) {
			logger.error("can't find group info !@@@@@groupid={}", group_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String [] {"获取组信息失败"});
		}
		return submit;
	}

	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.threeLevelGroup.{group_id}")
	public Map<String, Object> getThreeLevelGroup(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, group_id);
		map.put(Dict.REST_CODE, "getThreeLevelGroup");
		return (Map<String, Object>) transport.submit(map);
	}


	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.parentgroup.{group_id}")
	public List<Map<String, Object>> queryParentGroupByGroupId(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, group_id);
		map.put(Dict.REST_CODE, "queryParentGroupById");
		List<Map<String, Object>> submit = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(submit)) {
			logger.error("can't find group info !@@@@@groupid={}", group_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String [] {"获取组信息失败"});
		}
		return submit;
	}

	@Override
	public List<Map<String, Object>> queryAllGroupManagers(String group_id,String role_id) throws Exception {
		Map<String, Object> map = new HashMap<>();
		List<String> list=new ArrayList<>();
		list.add(role_id);
		map.put(Dict.GROUP_ID, group_id);
		map.put(Dict.ROLE_ID, list);
		map.put(Dict.REST_CODE, "queryUser");
		return (List<Map<String, Object>>) transport.submit(map);
	}

	@Override
	@LazyInitProperty(redisKeyExpression = "frelease.userinfo.{id}")
	public Map<String, Object> queryUserById(String id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, id);
		map.put(Dict.REST_CODE, "queryUser");
		List<Map<String, Object>> info = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(info)) {
			logger.error("can't find user info !@@@@@ id="+id);
			return null;
		}
		return info.get(0);
	}

	@Override
	public Map<String, Object> queryUserByGitUser(String userName) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.GIT_USER, userName);
		map.put(Dict.REST_CODE, "queryUser");
		List<Map<String, Object>> info = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(info)) {
			logger.error("can't find user info !@@@@@ gitUser=" + userName);
			return null;
		}
		return info.get(0);
	}

	@Override
	public List<Map<String, String>> queryAppSpdbManager(String application_id) throws Exception {
		Map<String, Object> application = appService.queryAPPbyid(application_id);
		List<Map<String, String>> spdb_managers=new ArrayList<>();
		if(!CommonUtils.isNullOrEmpty(application)) {
			spdb_managers = (List<Map<String, String>>) application.get(Dict.SPDB_MANAGERS);
			List<Map<String, String>> users = new ArrayList<>();
			for (Map<String, String> map : spdb_managers) {
				Map<String, Object> user = IUserService.class.cast(AopContext.currentProxy()).queryUserInfo(map.get(Dict.USER_NAME_EN));
				if(!CommonUtils.isNullOrEmpty(user)) {
					map.put(Dict.USER_ID, (String)user.get(Dict.ID));
				}
				users.add(map);
			}
			return users;
		} else {
			return spdb_managers;
		}
	}

	@Override
	public List<Map<String, String>> queryAppDevManager(String application_id) throws Exception {
		Map<String, Object> application = appService.queryAPPbyid(application_id);
		List<Map<String, String>> dev_managers=new ArrayList<>();
		if(!CommonUtils.isNullOrEmpty(application)) {
			dev_managers = (List<Map<String, String>>) application.get(Dict.DEV_MANAGERS);
			List<Map<String, String>> users=new ArrayList<>();
			for (Map<String, String> map : dev_managers) {
				Map<String, Object> user = IUserService.class.cast(AopContext.currentProxy()).queryUserInfo(map.get(Dict.USER_NAME_EN));
				if(!CommonUtils.isNullOrEmpty(user)) {
					map.put(Dict.USER_ID, (String)user.get(Dict.ID));
				}
				users.add(map);
			}
			return users;
		}else {
			return dev_managers;
		}
	}

	@Override
	public void addCommissionEvent(Map map) throws Exception {
		map.put(Dict.REST_CODE, "addCommissionEvent");
		transport.submit(map);
	}

	@Override
	public void updateCommissionEvent(Map map) throws Exception {
		map.put(Dict.REST_CODE, "updateByTargetIdAndType");
		transport.submit(map);
	}

	@Override
	public void deleteCommissionEvent(Map map) throws Exception {
		map.put(Dict.REST_CODE, "deleteCommissionEvent");
		transport.submit(map);
	}

	@Override
	public List<Map<String, Object>> queryGroup() throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.REST_CODE, "queryGroup");
		List<Map<String, Object>> submit = (List<Map<String, Object>>) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(submit)) {
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String [] {"获取组信息失败"});
		}
		return submit;
	}

	@Override
	public String queryGroupNameById(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.GROUPID, group_id);
		map.put(Dict.REST_CODE, "queryGroupNameById");
		String owner_group_name = (String) transport.submit(map);
		if(CommonUtils.isNullOrEmpty(owner_group_name)) {
			logger.error("can't find groupName info !@@@@@groupid={}", group_id);
			throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String [] {"获取小组名称失败"});
		}
		return owner_group_name;
	}

}
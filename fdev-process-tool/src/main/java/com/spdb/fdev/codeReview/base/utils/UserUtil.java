package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.w3c.dom.css.DOMImplementationCSS;

import java.util.*;

@Component
public class UserUtil {
	@Autowired
	private RestTransport restTransport;

	/**
	 * 获取组的三级父组，若当前组为3级之内，则直接返回自身.
	 * @param groupId
	 * @return
	 */
	public Map getThreeLevelGroup(String groupId) throws Exception {
		Map map = new HashMap();
		map.put(Dict.ID,groupId);
		map.put(Dict.REST_CODE,Dict.GETTHREELEVELGROUP);
		return (Map)restTransport.submit(map);
	}

	/**
	 * 批量查询用户信息，返回格式为[{userId:userInfoMap},{},..]。用户id为key，用户信息为value，组装而成的集合
	 * @param userIds
	 * @return
	 * @throws Exception
	 */
	public Map getUserByIds(Set<String> userIds) throws Exception {
		Map map = new HashMap();
		map.put(Dict.IDS,userIds);
		map.put(Dict.REST_CODE,Dict.QUERYBYUSERCOREDATA);
		return (Map)restTransport.submit(map);
	}

	/**
	 * 获取用户所在组的三级组，如用户所在组为1，2，3级，则直接返回
	 * @param userId
	 * @return
	 */
	public String getUserThreeLevelGroup(String userId) throws Exception {
		Set<String> param = new HashSet();
		param.add(userId);
		Map userInfo = getUserByIds(param);
		return  (String)getThreeLevelGroup((String)((Map)userInfo.get(userId)).get(Dict.GROUP_ID)).get(Dict.ID);
	}

	/**
	 * 批量获取小组中文名，并以map的形式存储
	 * @param groupIds
	 * @return
	 */
	public Map getGroupsNameByIds(Set<String> groupIds) throws Exception {
		Map resultMap = new HashMap();
		Map param = new HashMap();
		param.put(Dict.ALLFLAG,true);
		param.put(Dict.GROUPIDS, groupIds);
		param.put(Dict.REST_CODE,Dict.QUERYGROUPBYIDS);
		List<Map> groups = (List<Map>)restTransport.submit(param);
		for(Map group : groups){
			resultMap.put(group.get(Dict.ID),group.get(Dict.NAME));
		}
		return resultMap;
	}

	/**
	 * 根据用户id获取中文名(从返回的用户信息中剥离)
	 * @param userId
	 * @return
	 */
	public String getUserNameById(String userId,Map userInfo) throws Exception {
		Map<String,Object> user = (Map<String,Object>)userInfo.get(userId);
		return (String)user.get(Dict.USER_NAME_CN);
	}

	/**
	 * 获取某角色下所有人的邮箱
	 * @return
	 */
	public Set<String> getEmailsByRoleId(Set<String> roleIds) throws Exception {
		Set<String> result = new HashSet<>();
		Map param = new HashMap();
		param.put(Dict.ROLE_ID,roleIds);
		param.put(Dict.REST_CODE,Dict.QUERYUSERCOREDATA);
		List<Map> users = (List<Map>)restTransport.submit(param);
		for(Map user : users){
			result.add((String) user.get(Dict.EMAIL));
		}
		return result;
	}

	public List<String> getEmailByIds(Set<String> userIds) throws Exception {
		List<String> result = new ArrayList<>();
		Map param = new HashMap();
		param.put(Dict.IDS,userIds);
		param.put(Dict.REST_CODE,Dict.GETUSERSINFOBYIDS);
		List<Map> users = (List<Map>)restTransport.submit(param);
		for(Map user : users){
			result.add((String) user.get(Dict.EMAIL));
		}
		return result;
	}
}

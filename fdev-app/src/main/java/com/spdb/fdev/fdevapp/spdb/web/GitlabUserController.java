package com.spdb.fdev.fdevapp.spdb.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabUserService;

import io.swagger.annotations.Api;

@Api(tags = "gitlab api")
@RequestMapping("/api/gitlabapi")
@RestController
@RefreshScope
public class GitlabUserController {
	@Autowired
	private IGitlabAPIService gitlab;
	@Autowired
	private IGitlabUserService gitlabUserService;
	@Value("${gitlab.token}")
	private String gitlab_token;
	@Autowired
	private UserVerifyUtil userVerifyUtil;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestValidate(NotEmptyFields = { Dict.ROLE, Dict.ID, Dict.GIT_USER_ID })
	@RequestMapping(value = "/addMember", method = RequestMethod.POST)
	public JsonResult addMember(@RequestBody Map requestParam) throws Exception {
		String role = (String) requestParam.get(Dict.ROLE);
		String id = (String) requestParam.get(Dict.ID);
		String user_id = (String) requestParam.get(Dict.GIT_USER_ID);
		Object result = null;
		// 通过应用id查询项目id
		String projectid = gitlab.getProjectIDByApp(id);
		if (CommonUtils.isNullOrEmpty(projectid)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "@@@@@ the project can't find" });
		}
		// 查询项目用户列表
		if (CommonUtils.isNullOrEmpty(user_id)) {
			throw new FdevException(ErrorConstants.USR_EXISTED, new String[] { "@@@@ gitlab user not exist!" });
		}
		try {
			result = gitlabUserService.addMember(projectid, user_id, role, gitlab_token);
		} catch (HttpClientErrorException e) {
			logger.error("error message:" + e);
			String message = new String(e.getResponseBodyAsByteArray());
			JSONObject json = JSONObject.parseObject(message);
			Map map = json.toJavaObject(Map.class);
			Object msg = map.get("message");
			if (msg instanceof Map && ((Map) msg).containsKey(Dict.ACCESS_LEVEL)) {
				logger.error(message);
				return JsonResultUtil.buildSuccess(message);
			} else if (msg instanceof String && ((String) msg).startsWith(Dict.MEMBER)) {
				logger.error(message);
				try {
					gitlabUserService.editProjectMember(projectid, user_id, role, gitlab_token);
				} catch (FdevException e2) {
					logger.error("edit project member error "+e2);
					return JsonResultUtil.buildSuccess(message);
				}
			}
			logger.error("this user add member error" + user_id);
			return JsonResultUtil.buildSuccess(message);
		}
		return JsonResultUtil.buildSuccess(result);
	}

	@RequestValidate(NotEmptyFields = { Dict.ROLE, Dict.ID, Dict.GIT_USER_ID })
	@RequestMapping(value = "/addMemberList", method = RequestMethod.POST)
	public JsonResult addMemberList(@RequestBody Map requestParam) throws Exception {
		String role = (String) requestParam.get(Dict.ROLE);
		String id = (String) requestParam.get(Dict.ID);
		List<String> user_ids = (List<String>) requestParam.get(Dict.GIT_USER_ID);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		String projectid = gitlab.getProjectIDByApp(id);// 通过应用id获取项目id

		if (CommonUtils.isNullOrEmpty(projectid)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "@@@@@ the project can't find" });
		}

		// 获取 需要添加权限 的用户英文名列表
		for (String user_id : user_ids) {// 遍历用户英文名列表
			if (!CommonUtils.isNullOrEmpty(user_id)) {
				try {
					gitlabUserService.addMember(projectid, user_id, role, gitlab_token);
				} catch (HttpClientErrorException e) {
					logger.error("error message:" + e);
					String message = new String(e.getResponseBodyAsByteArray());
					JSONObject json = JSONObject.parseObject(message);
					Map jsonmap = json.toJavaObject(Map.class);
					Object msg = jsonmap.get("message");
					if (msg instanceof Map && ((Map) msg).containsKey(Dict.ACCESS_LEVEL)) {
						logger.error(message);
					} else if (msg instanceof String && ((String) msg).startsWith(Dict.MEMBER)) {
						logger.error(message);
						try {
							gitlabUserService.editProjectMember(projectid, user_id, role, gitlab_token);
						} catch (FdevException e2) {
							e2.printStackTrace();
							logger.error("edit project member error "+e2);
						}
					}
					logger.error("this user add member error" + user_id);
				}
			}
		}
		return JsonResultUtil.buildSuccess(result);
	}

	@RequestValidate(NotEmptyFields = { Dict.GIT_USER_ID, Dict.PATH, Dict.ROLE })
	@RequestMapping(value = "/addGroupMember", method = RequestMethod.POST)
	public JsonResult addGroupMember(@RequestBody Map requestParam) throws Exception {
		String user_id = (String) requestParam.get(Dict.GIT_USER_ID);
		String path = (String) requestParam.get(Dict.PATH);
		String role = (String) requestParam.get(Dict.ROLE);
		if (CommonUtils.isNullOrEmpty(user_id)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { " @@@@@ can't find user info" });
		}
		Object addGroupMember = gitlabUserService.addGroupMember(path, user_id, role, gitlab_token);
		return JsonResultUtil.buildSuccess(addGroupMember);
	}

	@RequestValidate(NotEmptyFields = { Dict.ROLE, Dict.ID, Dict.GIT_USER_ID })
	@RequestMapping(value = "/addResourceMember", method = RequestMethod.POST)
	public JsonResult addResourceMember(@RequestBody Map requestParam) throws Exception {
		String role = (String) requestParam.get(Dict.ROLE);
		String id = (String) requestParam.get(Dict.ID);
		String user_id = (String) requestParam.get(Dict.GIT_USER_ID);
		Object result = null;
		// 查询项目用户列表
		try {
			result = gitlabUserService.addResourceMember(id, user_id, role, gitlab_token);
		} catch (HttpClientErrorException e) {
			String message = new String(e.getResponseBodyAsByteArray());
			JSONObject json = JSONObject.parseObject(message);
			Map map = json.toJavaObject(Map.class);
			Object msg = map.get("message");
			if (msg instanceof Map && ((Map) msg).containsKey(Dict.ACCESS_LEVEL)) {
				logger.error(message);
				return JsonResultUtil.buildSuccess(msg);
			} else if (msg instanceof String && ((String) msg).startsWith(Dict.MEMBER)) {
				logger.error(message);
				try {
					gitlabUserService.editProjectMember(id, user_id, role, gitlab_token);
				} catch (Exception e2) {
					e2.printStackTrace();
					logger.error("edit project member error "+e2);
				}
				return JsonResultUtil.buildSuccess(msg);
			}
			logger.error("this user add member error" + user_id);
		}
		return JsonResultUtil.buildSuccess(result);
	}

	@RequestValidate(NotEmptyFields = { Dict.GIT_USER_ID })
	@RequestMapping(value = "/checkGitlabUser", method = RequestMethod.POST)
	public JsonResult checkGitlabUser(@RequestBody Map requestParam) throws Exception {
		String user_id = (String) requestParam.get(Dict.GIT_USER_ID);
		return JsonResultUtil.buildSuccess(gitlabUserService.checkGitlabUser(user_id, gitlab_token));
	}

	@RequestValidate(NotEmptyFields = { Dict.TOKEN })
	@RequestMapping(value = "/checkGitlabToken", method = RequestMethod.POST)
	public JsonResult checkGitlabToken(@RequestBody Map requestParam) throws Exception {
		String token = (String) requestParam.get(Dict.TOKEN);
		boolean flag = gitlabUserService.checkGitlabToken(token);
		return JsonResultUtil.buildSuccess(flag);
	}

}

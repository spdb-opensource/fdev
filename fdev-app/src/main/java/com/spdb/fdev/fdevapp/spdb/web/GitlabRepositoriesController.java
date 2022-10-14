package com.spdb.fdev.fdevapp.spdb.web;

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

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabRepositoriesService;

import io.swagger.annotations.Api;

@Api(tags = "gitlab api")
@RequestMapping("/api/gitlabapi")
@RestController
@RefreshScope
public class GitlabRepositoriesController {
	@Autowired
	private IGitlabRepositoriesService gitlabRepositoriesService;
	@Autowired
	private IGitlabAPIService gitlabAPIService;
	@Autowired
	private UserVerifyUtil userVerifyUtil;
	@Value("${gitlab.token}")
	private String gitlab_token;

	@RequestValidate(NotEmptyFields = { Dict.ID})
	@RequestMapping(value = "/queryRepositoriesTree", method = RequestMethod.POST)
	public JsonResult queryRepositoriesTree(@RequestBody Map requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String projectid = gitlabAPIService.getProjectIDByApp(id);
		if (CommonUtils.isNullOrEmpty(projectid)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
		}
		Object result = gitlabRepositoriesService.queryRepositoriesTree(projectid, gitlab_token);
		return JsonResultUtil.buildSuccess(result);
	}

	@RequestValidate(NotEmptyFields = { Dict.ID,Dict.BRANCH })
	@RequestMapping(value = "/changeMasterBranch", method = RequestMethod.POST)
	public JsonResult changeMasterBranch(@RequestBody Map requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String branch = (String) requestParam.get(Dict.BRANCH);
		String projectid = gitlabAPIService.getProjectIDByApp(id);
		if (CommonUtils.isNullOrEmpty(projectid)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] {"未查询到相关项目，项目id："+ id });
		}
		gitlabRepositoriesService.changeMasterBranch(projectid, gitlab_token, branch);
		return JsonResultUtil.buildSuccess(null);
	}

	@RequestValidate(NotEmptyFields = { Dict.ID,Dict.END_DATE })
	@RequestMapping(value = "/queryMergeRequestList", method = RequestMethod.POST)
	public JsonResult queryMergeRequestList(@RequestBody Map requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String end_date = (String) requestParam.get(Dict.END_DATE);
		String projectid = gitlabAPIService.getProjectIDByApp((String) requestParam.get(Dict.ID));
		if (CommonUtils.isNullOrEmpty(projectid)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
		}
		Object result = gitlabRepositoriesService.queryMergeRequestList(projectid, gitlab_token, end_date);		
		return JsonResultUtil.buildSuccess(JSONObject.parse((String)result));
	}
	
	
	@RequestValidate(NotEmptyFields= {Dict.PATH})
	@RequestMapping(value = "/queryTaskFileList", method = RequestMethod.POST)
	public JsonResult queryTaskFileList(@RequestBody Map requestParam) throws Exception {
		String path=(String) requestParam.get(Dict.PATH);
	    List<Map<String, String>> result = gitlabRepositoriesService.queryTaskFileList(path);
		return JsonResultUtil.buildSuccess(result);
	}

}

package com.spdb.fdev.fdevapp.spdb.web;

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

import com.spdb.fdev.common.JsonResult;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabVariablesService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(tags = "gitlab api")
@RequestMapping("/api/gitlabapi")
@RestController
@RefreshScope
public class GitlabVariablesController {
	
    @Autowired
    private IGitlabVariablesService gitlabVariablesService;
    @Autowired
    private IGitlabAPIService gitlabAPIService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Value("${gitlab.token}")
    private String gitlab_token;


    @RequestValidate(NotEmptyFields = { Dict.ID })
    @RequestMapping(value = "/queryVariables", method = RequestMethod.POST)
    public JsonResult queryVariables(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.queryVariables(projectid,gitlab_token);

        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = { Dict.ID, Dict.KEY })
    @RequestMapping(value = "/queryVariableDetails", method = RequestMethod.POST)
    public JsonResult queryVariableDetails(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String key = (String) requestParam.get(Dict.KEY);
        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.queryVariableDetails(projectid, key, gitlab_token);
        return JsonResultUtil.buildSuccess(result);

    }

    @RequestValidate(NotEmptyFields = { Dict.ID, Dict.KEY, Dict.VALUE })
    @RequestMapping(value = "/createVariable", method = RequestMethod.POST)
    public JsonResult createVariable(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String key = (String) requestParam.get(Dict.KEY);
        String value = (String) requestParam.get(Dict.VALUE);
        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.createVariable(projectid, key, value, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = { Dict.ID,Dict.KEY, Dict.VALUE })
    @RequestMapping(value = "/updateVariable", method = RequestMethod.POST)
    public JsonResult updateVariable(
    @RequestBody @ApiParam(name = "参数", value = "示例{ \"应用Id\":\"项目id\",\"key\":\"参数key\",\"key\":\"参数key\",\"token\":\"验证码\" }") Map requestParam)
    throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String key = (String) requestParam.get(Dict.KEY);
        String value = (String) requestParam.get(Dict.VALUE);
        String projectid = gitlabAPIService.getProjectIDByApp((String) requestParam.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.updateVariable(projectid, key, value, gitlab_token);

        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = { Dict.ID,Dict.KEY })
    @RequestMapping(value = "/deleteVariable", method = RequestMethod.POST)
    public JsonResult deleteVariable(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String key = (String) requestParam.get(Dict.KEY);

        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.deleteVariable(projectid, key, gitlab_token);

        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = { Dict.ID, Dict.REF, Dict.CRON, Dict.KEY, Dict.VALUE, Dict.DESCRIPTION})
    @RequestMapping(value = "/createPipelineSchedule", method = RequestMethod.POST)
    public JsonResult createPipelineSchedule(@RequestBody Map<String, String> requestParam) throws Exception {
        String id = requestParam.get(Dict.ID);
        String ref = (String) requestParam.get(Dict.REF);
        String cron = (String) requestParam.get(Dict.CRON);
        String key = (String) requestParam.get(Dict.KEY);
        String value = (String) requestParam.get(Dict.VALUE);
        String description = (String) requestParam.get(Dict.DESCRIPTION);
        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Map<String, Object> result = gitlabVariablesService.createPipelineSchedule(projectid, description, ref, cron,
        key, value, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = { Dict.ID})
    @RequestMapping(value = "/queryPipelineSchedule", method = RequestMethod.POST)
    public JsonResult queryPipelineSchedule(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String projectid = gitlabAPIService.getProjectIDByApp(id);
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.queryPipelineSchedule(projectid, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields= {Dict.ID,Dict.BRANCH})
    @RequestMapping(value = "/deletePipelineSchedule", method = RequestMethod.POST)
    public JsonResult deletePipelineSchedule(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        String branch = (String) requestParam.get(Dict.BRANCH);
        String projectid = gitlabAPIService.getProjectIDByApp((String) requestParam.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(projectid)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到相关项目，项目id："+ id });
        }
        Object result = gitlabVariablesService.deletePipelineSchedule(projectid, branch, gitlab_token);
        return JsonResultUtil.buildSuccess(result);
    }
}

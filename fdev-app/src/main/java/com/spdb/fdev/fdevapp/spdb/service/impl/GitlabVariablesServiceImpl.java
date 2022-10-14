package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabVariablesService;

@Service
@RefreshScope
public class GitlabVariablesServiceImpl implements IGitlabVariablesService {
	@Value("${gitlib.path}")
	private String url;// gitlab地址http://xxx/api/v4/
	@Value("${gitlib.cron_timezone}")
	private String cron_timezone;

	@Autowired
	private GitlabTransport gitlabTransport;

	@Override
	public Object queryVariables(String id, String token) throws Exception {
		String variables_url = CommonUtils.projectUrl(url) + "/" + id + "/variables";
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN, token);
		return gitlabTransport.submitGet(variables_url, header);
	}

	@Override
	public Object queryVariableDetails(String id, String key, String token) throws Exception {
		String variables_url = CommonUtils.projectUrl(url) + "/" + id + "/variables/" + key;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN, token);
		return gitlabTransport.submitGet(variables_url, header);
	}

	@Override
	public Object createVariable(String id, String key, String value, String token) throws Exception {
		String variables_url = CommonUtils.projectUrl(url) + "/" + id + "/variables/";
		Map<String, String> param = new HashMap<String, String>();
		param.put(Dict.KEY, key);
		param.put(Dict.VALUE, value);
		param.put(Dict.PRIVATE_TOKEN_L, token);
		return gitlabTransport.submitPost(variables_url, param);
	}

	@Override
	public Object updateVariable(String id, String key, String value, String token) throws Exception {
		String variables_url = CommonUtils.projectUrl(url) + "/" + id + "/variables/" + key + "?=values" + value;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN_L, token);
		return gitlabTransport.submitPut(variables_url, header);
	}

	@Override
	public Object deleteVariable(String id, String key, String token) throws Exception {
		String variables_url = CommonUtils.projectUrl(url) + "/" + id + "/variables/" + key;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN_L, token);
		return gitlabTransport.submitDelete(variables_url, header);
	}

	@Override
	public Map<String, Object> createPipelineSchedule(String id, String description, String ref, String cron,
			String key, String value, String token) throws Exception {
		String pipelines_url = CommonUtils.projectUrl(url) + "/" + id + "/pipeline_schedules";
		Map<String, String> param = new HashMap<String, String>();
		param.put(Dict.PRIVATE_TOKEN_L, token);
		param.put(Dict.DESCRIPTION, description);
		param.put(Dict.REF, ref);
		param.put(Dict.CRON, cron);
		param.put("cron_timezone", this.cron_timezone);
		Object obj = gitlabTransport.submitPost(pipelines_url, param);
		Map pipeline = (Map) JSONObject.parse((String) obj);
		int pipeline_id = (int) pipeline.get(Dict.ID);
		String pipelines_variable_url = pipelines_url + "/" + pipeline_id + "/variables";
		Map<String, String> variable_param = new HashMap<String, String>();
		variable_param.put(Dict.PRIVATE_TOKEN_L, token);
		variable_param.put(Dict.KEY, key);
		variable_param.put(Dict.VALUE, value);
		Object variable = gitlabTransport.submitPost(pipelines_variable_url, variable_param);
		pipeline.put(Dict.VARIABLES, variable);
		return pipeline;
	}

	@Override
	public Object queryPipelineSchedule(String id, String token) throws Exception {
		String pipelines_url = CommonUtils.projectUrl(url) + "/" + id + "/pipeline_schedules";
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN, token);
		return  gitlabTransport.submitGet(pipelines_url, header);
	}

	@Override
	public Object deletePipelineSchedule(String id, String pipelineScheduleId, String token)
			throws Exception {
		String pipelines_url = CommonUtils.projectUrl(url) + "/" + id + "/pipeline_schedules/" + pipelineScheduleId;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		header.add(Dict.PRIVATE_TOKEN, token);
		return gitlabTransport.submitDelete(pipelines_url, header);
	}

}

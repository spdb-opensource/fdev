
package com.spdb.fdev.fuser.spdb.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.service.GitlabService;
import com.spdb.fdev.fuser.spdb.transport.GitlabTransport;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
@RefreshScope
public class GitlabServiceImpl implements GitlabService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    GitlabTransport gitlabTransport;
    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;
	@Value("${gitlab.ebank.groupId}")
	private String ebankId;

	/**
     * 查询gitlab中的username
     * @return
	 * @throws Exception 
     */
	public String queryGitlabUsername(String git_user_id) throws Exception {
		String Url = this.gitlabApiUrl + "users/" + git_user_id;
	    MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
	    Object submitGet = gitlabTransport.submitGet(Url, header);
	    JSONObject jsonObject = JSONObject.parseObject((String) submitGet);
	    String gitlabUsername = (String) jsonObject.get("username");

		return gitlabUsername;
	}


	/**
	 * 用户离职的时候，从ebank组中去除相应的gitlab用户
	 *
	 * @param git_user_id
	 * @return
	 */
	@Override
	public void removeGitlabUser(String git_user_id, String token) throws Exception {
		String Url = this.gitlabApiUrl + "groups/" + ebankId + "/members/" + git_user_id;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("PRIVATE-TOKEN", token);
		gitlabTransport.submitDelete(Url, header);
	}

	/**
	 *
	 * 查询该用户的gitlal用户是否存在与ebank内
	 * @param git_user_id
	 * @param token
	 * @return
	 */
	@Override
	public Object queryGitlabUserOfGroup(String git_user_id, String token) throws Exception {
		String url = this.gitlabApiUrl + "groups/" + ebankId + "/members/" + git_user_id;
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("PRIVATE-TOKEN", token);
		return gitlabTransport.submitGet(url, header);
	}




}

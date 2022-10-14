package com.mantis.service.impl;

import java.util.List;
import java.util.Map;

import com.mantis.util.DES3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.mantis.dao.MantisFileDao;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.service.MantisFileService;
import com.mantis.service.RoleVaildateService;
import com.mantis.util.MantisRestTemplate;
import com.mantis.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;


@Service
@RefreshScope
public class MantisFileServiceImpl implements MantisFileService{
	@Autowired
	private MantisFileDao mantisFileDao;
	@Autowired
	private MantisRestTemplate MantisRestTemplate;
	@Value("${manits.issue.url}")
	private String mantis_url;
	@Value("${manits.admin.token}")
	private String mantis_token;
	@Value("${spring.profiles.active}")
	private String env;
	@Autowired
	private RoleVaildateService roleVaildateService;
	@Autowired
	private DES3 des3;
	
	@Override
	public void addFile(Map<String, Object> map) throws Exception {
		String id = String.valueOf(map.get(Dict.ID));
		if(!roleVaildateService.userCanEditIssueVaildate(id)) {
			throw new FtmsException(ErrorConstants.NO_ROLE_EDIT_ISSUE);
		}
		String mantis_token = (String)map.get(Dict.MANTIS_TOKEN);
		if(Utils.isEmpty(mantis_token)) {
			throw new FtmsException(ErrorConstants.DO_NOT_HAVE_MANITS_TOKEN);
		}
		//新的玉衡对mantis-token解密
		if (env.equals("sit-new") || env.equals("rel-new") || env.equals("pro-new")){
			mantis_token = des3.decrypt(mantis_token);
		}
		List<Map<String,String>> files= (List<Map<String,String>>) map.get(Dict.FILES);
		String url = new StringBuilder(mantis_url)
				.append("/api/rest/issues/")
				.append(id).append("/files").toString();
		for (Map<String, String> file : files) {
			String content = file.get(Dict.CONTENT);
			String[] split = content.split("base64,");
			file.put(Dict.CONTENT, split[1]);
		}
		try {
			MantisRestTemplate.sendPost(url, mantis_token, map);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.ADD_ISSUES_FILE_ERROR);
		}
		
	}
	@Override
	public void deleteFile(Map<String, String> map) throws Exception {
		String id = String.valueOf(map.get(Dict.ID));
		if(!roleVaildateService.userCanEditIssueVaildate(id)) {
			throw new FtmsException(ErrorConstants.NO_ROLE_EDIT_ISSUE);
		};
		String file_id = map.get(Dict.FILE_ID);
		try {
			mantisFileDao.deleteFile(file_id);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DELETE_ISSUES_FILE_ERROR);
		}
	}
	
	
	@Override
	public List<Map<String,Object>> queryIssueFiles(String id) throws Exception {
		List<Map<String,Object>> list = mantisFileDao.queryIssueFiles(id);
		for (Map<String, Object> file : list) {
			String file_type = (String) file.get(Dict.FILE_TYPE);
			String[] split = file_type.split("=");
			String encoder = split[1];
			if(file_type.startsWith(Dict.TEXT)) {
				byte[] content_blob = (byte[]) file.get(Dict.CONTENT);
				String content;
				if(encoder.equals("iso-8859-1")) {
					 content = new String(content_blob,"GBK");
				}else {
					 content = new String(content_blob,encoder);
				}
			file.put(Dict.CONTENT, content);
			}
		}
		return list;
	}



}

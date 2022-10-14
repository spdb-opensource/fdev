package com.spdb.fdev.spdb.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.IEmailService;

@Service
public class EmailServiceImpl implements IEmailService {
	@Value("${fnotify.host}")
	private String fnotifyHost;//通知模块接口
	@Autowired
    RestTemplate restTemplate;
	
	@Override
	public void sendEmail(String subject, String content, List<String> to){
		HashMap<String, Object> mailParams = new HashMap<>();
		mailParams.put(Dict.SUBJECT, subject);
        mailParams.put(Dict.CONTENT, content);
        mailParams.put(Dict.TO, to);
        Map responseMap = this.restTemplate.postForObject(fnotifyHost, mailParams, Map.class, new Object[0]);
	}

}

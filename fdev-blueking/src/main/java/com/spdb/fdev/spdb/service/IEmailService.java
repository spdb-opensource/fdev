package com.spdb.fdev.spdb.service;

import java.util.*;

public interface IEmailService {
	//通过调用通知模块接口发送邮件
	void sendEmail(String subject, String content, List<String> to);
	
}

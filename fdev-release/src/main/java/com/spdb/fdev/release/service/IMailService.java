package com.spdb.fdev.release.service;

import java.util.HashMap;
import java.util.List;

public interface IMailService {

	/***
	 * 邮件发送
	 * @param subject
	 * @param templateName
	 * @param model
	 * @param to
	 * @throws Exception
	 */
	void sendEmail(String subject, String templateName, HashMap<String, String> model, List<String> to) throws Exception;

	/***
	 * 发送用户通知
	 * @param content	通知内容
	 * @param target	通知对象集合
	 * @param type		消息类型
	 * @param hyperlink	相关链接地址
	 */
	void sendUserNotify(String content, List<String> target, String desc, String hyperlink, String type) throws Exception;

}

package com.spdb.fdev.release.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ISendEmailService {
	/**
	 * 发送邮件通知任务关联项审核
	 * 发送的人员为该任务的负责人和行内负责人
	 * @throws Exception
	 */
	void sendEmailReviews(List<HashMap> model) throws Exception;

	/**
	 * 发邮件提醒全组任务负责人，投产窗口已更新
	 * @param model
	 * @throws Exception
	 */
	void sendEmailUpdateNode(HashMap model) throws Exception;

	/**
	 * 任务即将投产发送提醒
	 * @param model
	 * @throws Exception
	 */
	void sendEmailCleanDelayedTasks(HashMap model) throws Exception;

	/***
	 * 配置文件有变化，发fdev通知提醒审核
	 * @throws Exception
	 */
	void sendConfigCheckNotify(HashMap model) throws Exception;

	/**
	 *  提醒需求相关人员，未及时上传投产确认书提醒
	 * @param model
	 * @throws Exception
	 */
	void sendEmailRqrmntConfirmNotify(HashMap model)throws Exception;

	/**
	 * 提醒需求相关人员，未及时上传投产确认书 不予测试
	 * @param model
	 * @throws Exception
	 */
	void sendEmailRqrmntConfirmNotifyNotAllow(HashMap model) throws Exception;
}

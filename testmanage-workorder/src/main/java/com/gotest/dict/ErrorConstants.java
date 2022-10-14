/**
 * ErrorConstants.java Created on Thu Dec 16 09:30:08 CST 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.gotest.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Thu Dec 16 09:30:08 CST 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  数据库插入错误！{0}  */
    public static final String DATA_INSERT_ERROR="WO00001";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  数据查询失败！{0}  */
    public static final String DATA_QUERY_ERROR="WO00002";
/**  数据库更新错误！{0}  */
    public static final String DATA_UPDATE_ERROR="W000005";
/**  该状态工单不可执行打回操作！  */
    public static final String ERROR_STAGE_ORDER_ROLLBACK="W000009";
/**  工单报表导出失败！  */
    public static final String EXPORT_EXCEL_ERROR="WO00005";
/**  登录已过期，请重新登录！  */
    public static final String GET_CURRENT_USER_INFO_ERROR="COM9999";
/**  gitlab请求异常！{0}  */
    public static final String GITLAB_REQUEST_ERROR="COM0011";
/**  该工单并不来自fdev，属于玉衡自建工单  */
    public static final String MAINTASKNO_NOT_EXIST_ERROR="W000012";
/**  mantis平台异常！  */
    public static final String MANTIS_ERROR="COM8000";
/**  邮件无有收件人，无法发送！  */
    public static final String NO_MAIL_TARGET_ERROR="W000014";
/**  已存在该工单任务名！  */
    public static final String ORDER_NAME_REPORT_ERROR="W000017";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COM0007";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COM0006";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="COM0001";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  服务器错误  */
    public static final String SERVER_ERROR="COM0002";
/**  拆分工单失败！{0}  */
    public static final String SPLIT_ORDER_FAIL="W000018";
/**  工单下有任务尚未进入uat  */
    public static final String SUBTASK_NOT_FINISH_ERROR="W000016";
/**  请先补充内测实际完成时间！  */
    public static final String SUPPLEMENT_UAT_DATE="WO00006";
/**  任务编号已存在！  */
    public static final String TASKNO_REPET_ERROR="WO00004";
/**  该任务无提测信息  */
    public static final String TASK_NOT_SUBMIT_SIT_ERROR="W000013";
/**  查询范围过大  */
    public static final String TIMES_OUT_OF_RANGE="W000015";
/**  当前工单存在{0}案例，无法进入下一阶段！  */
    public static final String UNQUALIFIED_CASE_ERROR="W000007";
/**  当前工单存在{0}缺陷，无法进入下一阶段！  */
    public static final String UNQUALIFIED_MANTIS_ERROR="W000008";
/**  更新工单计划时间失败！{0}  */
    public static final String UPDATA_DATE_FAIL="W000019";
/**  修改manits缺陷失败!  */
    public static final String UPDATE_MANTIS_ISSUES_ERROR="W000010";
/**  更新工单失败!{0}  */
    public static final String UPDATE_ORDER_ERROR="W000021";
/**  实施单元未归档，不允许工单改为已投产  */
    public static final String UPDATE_WORKORDER_STATUS_FAIL="W000020";
/**  文件上传失败!  */
    public static final String UPLOAD_LOAD_ERROR="W000022";
/**  该查询条件下未查询到用户数据  */
    public static final String USER_NOT_EXIST_ERROR="W000011";
/**  工单编号已存在！  */
    public static final String WORKNO_REPET_ERROR="WO00003";
}

/**
 * ErrorConstants.java Created on Mon Sep 07 13:54:53 CST 2020
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.manager.ftms.util;
/**
 * FDEV系统错误字典文件
 * Creation date: (Mon Sep 07 13:54:53 CST 2020)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  数据插入错误！{0}  */
    public static final String ADD_ERROR="COM0001";
/**  案例批量新增失败！  */
    public static final String BATCH_ADD_ERROR="COM1006";
/**  {0}  */
    public static final String BATCH_ADD_FORMAT_ERROR="COM9998";
/**  批量新增模版下载失败！  */
    public static final String BATCH_ADD_TEMPLATE_DOWNLOAD_ERROR="COM1007";
/**  批量删除中关系id为{0}的案例下存在缺陷，故无法删除  */
    public static final String BATCH_DELETE_CONTAIN_ISSUE_ERROR="COM1018";
/**  计划下不存在案例！  */
    public static final String CASE_NOT_EXIST_ERROR="COM1015";
/**  统计审批案例总数异常!  */
    public static final String COUNT_TESTCASE_NUM_EXCEPTION="COM1002";
/**  数据删除失败!  */
    public static final String DATA_DEL_ERROR="COM1004";
/**  数据导出失败!{0}  */
    public static final String DATA_EXPORT_ERROR="COM1008";
/**  数据已存在{0}  */
    public static final String DATA_IS_EXIST="COM0007";
/**  操作的数据不存在！  */
    public static final String DATA_NOT_EXIST_ERROR="COM1010";
/**  该案例下存在缺陷，请先删除缺陷后再操作！  */
    public static final String DELETE_CASE_INCLUDE_ISSUE_ERROR="COM1017";
/**  批量上传文件格式错误，第【{0}】行【{1}】字段  */
    public static final String EXCEL_CONTENT_FORMAT_ERROR="COM1005";
/**  该案例执行状态已存在,请勿重复点击!  */
    public static final String EXECUTE_RESULT_STATUS_EXIST="COM1003";
/**  已执行案例无法再次编辑，请新建或复制案例  */
    public static final String EXE_CASE_MODIFY_FORBID="COM1019";
/**  {0}编号案例审批失败！  */
    public static final String EXIST_TESTCASE_APPROVAL_FAIL="COM1012";
/**  登录已过期,请重新登录!  */
    public static final String GET_CURRENT_USER_INFO_ERROR="COM9999";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COM0006";
/**  请求参数异常!{0}  */
    public static final String PARAM_ERROR="COM0004";
/**  工单下不存在计划！  */
    public static final String PLAN_NOT_EXIST_ERROR="COM1013";
/**  查询数据异常!{0}  */
    public static final String QUERY_DATA_EXCEPTION="COM1016";
/**  您没有权限执行此项操作！  */
    public static final String ROLE_ERROR="COM1009";
/**  查询数据异常!  */
    public static final String SELECT_DATA_EXCEPTION="COM1001";
/**  数据状态错误{0}  */
    public static final String STATUS_ERROR="COM0002";
/**  系统内部错误！  */
    public static final String SYSTEM_ERROR="COM9997";
/**  无此案例  */
    public static final String TESTCASE_CAN_NOT_FOUND="COM0008";
/**  该案例状态不允许被复用!  */
    public static final String TESTCASE_CAN_NOT_USE="COM1011";
/**  该案例状态异常!{0}  */
    public static final String TESTCASE_STATUS_ERROR="COM0005";
/**  工单下不存在收件人！  */
    public static final String TOMAIL_NOT_EXIST_ERROR="COM1014";
/**  数据更新错误{0}  */
    public static final String UPDATE_ERROR="COM0003";
}

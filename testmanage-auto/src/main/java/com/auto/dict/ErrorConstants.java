/**
 * ErrorConstants.java Created on Sun Sep 27 16:33:11 CST 2020
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.auto.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Sun Sep 27 16:33:11 CST 2020)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  新增manits缺陷失败  */
    public static final String ADD_MANTIS_ISSUES_ERROR="TU0011";
/**  新增生产问题失败  */
    public static final String ADD_PRO_ISSUES_ERROR="TU0012";
/**  数据删除失败，{0}请稍后重试！  */
    public static final String DATA_DELETE_ERROR="TU0005";
/**  数据新增失败，请稍后重试！  */
    public static final String DATA_INSERT_ERROR="TU0006";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  数据查询失败，请稍后重试!  */
    public static final String DATA_QUERY_ERROR="TU0008";
/**  数据修改失败，请稍后重试！  */
    public static final String DATA_UPDATE_ERROR="TU0007";
/**  您未添加mantis平台Token，请添加后重试  */
    public static final String DO_NOT_HAVE_MANITS_TOKEN="TU0010";
/**  fdev登录失败  */
    public static final String FDEV_LOGIN_FAILURE="TU00003";
/**  登录已过期，请重新登录！  */
    public static final String GET_CURRENT_USER_INFO_ERROR="COM9999";
/**  登录失败！{0}  */
    public static final String LOGIN_ERROR="TU00002";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COM0007";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COM0006";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="COM0001";
/**  该工单下已存在同名的执行计划  */
    public static final String REPET_PLAN_LIST_NAME="TU0004";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  服务器错误  */
    public static final String SERVER_ERROR="COM0002";
/**  该缺陷报告人和开发人员,或分派人员相同异常!  */
    public static final String UPDATE_MANTIS_PERSONNEL_ERROR="MS0008";
/**  生成自动化代码失败，请稍后重试!  */
    public static final String GENERATE_FILE_ERROR="TU0013";
/**  执行自动化代码失败，请稍后重试!  */
    public static final String EXCUTE_FILE_ERROR="TU0014";
}

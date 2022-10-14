/**
 * ErrorConstants.java Created on Thu Jun 10 11:18:40 GMT+08:00 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.fdevinterface.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Thu Jun 10 11:18:40 GMT+08:00 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {

private ErrorConstants() {}
/**  解析文件出错：{0}  */
    public static final String ANALYSIS_FILE_ERROR="ITF0006";
/**  文件读写传输错误：{0}  */
    public static final String APP_FILE_ERROR="ITF0017";
/**  调用方应用不存在!{0}  */
    public static final String CALLING_APP_NOT_EXIST="ITF0025";
/**  GitLab clone url 错误！  */
    public static final String CLONE_URL_ERROR="ITF0004";
/**  数据不存在：{0}  */
    public static final String DATA_NOT_EXIST="ITF0003";
/**  导出接口调用关系表出错：{0}  */
    public static final String EXPORT_RELATION_EXCEL_ERROR="ITF0016";
/**  GitLab服务异常！  */
    public static final String GITLAB_SERVER_ERROR="ITF0005";
/**  接口统计信息扫描出错：{0}  */
    public static final String INTERFACE_STATISTICS_ERROR="ITF00026";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="ITF0022";
/**  无法获取该应用rest接口的调用信息!{0}  */
    public static final String INVAILD_OPERATION_INFO_INTERFACE="ITF0023";
/**  对象实体转换错误!{0}  */
    public static final String OBJECT_CAST_TO_MAP_ERROR="ITF0030";
/**  请求参数不能全部为空!{0}  */
    public static final String PARAM_CANNOT_BE_ALLEMPTY="ITF00024";
/**  请求参数不能为空!{0}  */
    public static final String PARAM_CANNOT_BE_EMPTY="ITF0002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="ITF0018";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  扫描REST接口调用方出错：{0}  */
    public static final String REST_CALLING_SCAN_ERROR="ITF0008";
/**  扫描REST接口提供方出错：{0}  */
    public static final String REST_PROVIDER_SCAN_ERROR="ITF0007";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  扫描VUE项目路由出错：{0}  */
    public static final String ROUTES_SCAN_ERROR="ITF00025";
/**  执行python脚本{0}出错：{1}  */
    public static final String RUN_PYTHON_ERROR="ITF00027";
/**  格式错误:{0}  */
    public static final String SCHEMA_ERROR="ITF00028";
/**  路由介质scp出错!{0}  */
    public static final String SCP_ROUTE_DATA_ERROR="ITF0029";
/**  服务器错误：{0}  */
    public static final String SERVER_ERROR="ITF0000";
/**  提供方应用不存在!{0}  */
    public static final String SERVICE_APP_NOT_EXIST="ITF0024";
/**  提供方接口不存在!{0}  */
    public static final String SERVICE_TRANS_NOT_EXIST="ITF0026";
/**  扫描SOAP接口调用方出错：{0}  */
    public static final String SOAP_CALLING_SCAN_ERROR="ITF0010";
/**  扫描SOAP接口提供方出错：{0}  */
    public static final String SOAP_PROVIDER_SCAN_ERROR="ITF0009";
/**  扫描SOP接口调用方出错：{0}  */
    public static final String SOP_CALLING_SCAN_ERROR="ITF0011";
/**  没有找到当前扫描应用{0}的src文件夹！  */
    public static final String SRC_FILE_NOT_EXIST="ITF0001";
/**  交易码列表长度不能超过50！  */
    public static final String TRANS_CODE_LIST_OVER="ITF0013";
/**  接口{0}已申请！  */
    public static final String TRANS_ID_IS_APPROVE="ITF0019";
/**  接口{0}正在审批中！  */
    public static final String TRANS_ID_IS_PROCESSED="ITF0020";
/**  交易ID列表长度不能超过50！  */
    public static final String TRANS_ID_LIST_OVER="ITF0014";
/**  扫描交易调用方出错：{0}  */
    public static final String TRANS_RELATION_SCAN_ERROR="ITF0015";
/**  扫描交易提供方出错：{0}  */
    public static final String TRANS_SCAN_ERROR="ITF0012";
/**  用户认证失败:{0}  */
    public static final String USR_AUTH_FAIL="ITF0021";
    /**  生成模版文件失败  */
    public static final String GENERATE_EXCEL_FAIL="ITF0031";
}

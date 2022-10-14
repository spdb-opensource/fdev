/**
 * ErrorConstants.java Created on Mon Jun 21 13:49:51 GMT+08:00 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Mon Jun 21 13:49:51 GMT+08:00 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {

private ErrorConstants() {}
/**  该应用未选择fdevci方式  */
    public static final String APP_NOT_CHOOSE_FDEV_CI="PIP0008";
/**  符合自定义规则的分支列表为空{0}  */
    public static final String BRANCHES_IS_NULL="PIP0012";
/**  数据不存在！{0}  */
    public static final String DATA_NOT_EXIST="PIP0001";
/**  空JOB不允许被修改或删除  */
    public static final String EMPTY_JOBENTITY_NOT_UPDATE_OR_DELETE="PIP0022";
/**  空模版不允许被修改或删除  */
    public static final String EMPTY_PIPELINETEMPLATE_NOT_UPDATE_OR_DELETE="PIP0022";
/**  文件上传minio失败  */
    public static final String FILE_UPLOAD_MINIO_FAILAD="OSS0001";
/**  gitlab请求错误  */
    public static final String GITLAB_REQUEST_ERROR="PIP0007";
/**  任务未开始或已经完成，停止失败{0}  */
    public static final String JOB_CANNOT_CANCEL="PIP0019";
/**  其他后台服务异常！{0}  */
    public static final String OTHER_APP_ERROR="PIP0005";
/**  请求参数{0}异常!{1}  */
    public static final String PARAMS_ERROR="COMS001";
/**  参数不合法！{0}  */
    public static final String PARAMS_IS_ILLEGAL="PIP0023";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COMM003";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请检查配置参数:{0}  */
    public static final String PARAM_CONFIG_ERROR="COMM007";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COMM004";
/**  流水线已经完成，停止失败{0}  */
    public static final String PIPELINE_IS_FINISHED="PIP0018";
/**  该流水线正在执行中，请稍后重试{0}  */
    public static final String PIPELINE_IS_RUNNING="PIP0015";
/**  该插件正在被流水线引用，不能删除!{0}  */
    public static final String PLUGIN_NOT_DELETE="PLUGIN000";
/**  该插件参数有误！{0}  */
    public static final String PLUGIN_PARAMS_ERROR="PLUGIN001";
/**  前面的阶段有未成功的任务，无法重试该任务{0}  */
    public static final String PREVIOUS_TASK_FAIL="PIP0016";
/**  用户查询失败{0}  */
    public static final String QUERY_USER_IS_FAILD="USR0001";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="PIP0004";
/**  重试失败，当前用户没有权限{0}  */
    public static final String RETRY_FAILED="PIP0020";
/**  重试流水线失败{0}  */
    public static final String RETRY_PIPELINE_FAILD="PIP0014";
/**  重试job失败{0}  */
    public static final String RETRY_JOB_FAILD="PIP0013";
/**  该用户无权限执行此操作！{0}  */
    public static final String ROLE_ERROR="PIP0003";
/**  服务器错误！{0}  */
    public static final String SERVER_ERROR="PIP0000";
/**  其他后台服务异常  */
    public static final String THIRD_SERVER_ERROR="COMM001";
/**  缺少交易码字段  */
    public static final String TRANS_CODE_CANNOT_BE_EMPTY="COMM006";
/**  交易码{0}未定义  */
    public static final String TRANS_CODE_UNDEFINED="COMM005";
/**  收藏或取消收藏失败，请重试{0}  */
    public static final String UPDATE_FOLLOW_STATUS_FAIL="PIP0017";
/**  用户{0}无权限操作!  */
    public static final String USER_IS_NOT_AUTH="USR0002";
/**  当前用户条线不存在{0}  */
    public static final String USER_IS_NOT_EXISTS_LINEID="PIP0021";
/**  当前用户不是该应用负责人{0}  */
    public static final String USER_NOT_APPMANAGER="PIP0010";
/**  当前用户不是创建人{0}  */
    public static final String USER_NOT_AUTHOR="PIP0011";
/**  用户认证失败  */
    public static final String USR_AUTH_FAIL="COM0012";
/**  错误的插件下标  */
    public static final String WRONG_PLUGIN_INDEX="PIP0006";
}

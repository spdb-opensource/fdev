/**
 * ErrorConstants.java Created on Mon Dec 21 16:18:19 GMT+08:00 2020
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.fdevtask.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Mon Dec 21 16:18:19 GMT+08:00 2020)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  新增代办失败  */
    public static final String ADD_TODO_ERROR="ATE0001";
/**  公告信息存储失败！  */
    public static final String ANNOUNCE_SAVE_ERROR="NTF0002";
/**  读取application.properties文件出错！  */
    public static final String APPLICATION_FILE_ERROR="ITF0011";
/**  文件读写传输错误  */
    public static final String APP_FILE_ERROR="APP0004";
/**  没有找到服务名称!  */
    public static final String APP_NAME_NOT_FOUND="ITF0010";
/**  投产应用信息不存在！  */
    public static final String APP_NOT_EXIST="RLS0004";
/**  其他后台服务异常  */
    public static final String APP_THIRD_SERVER_ERROR="APP0006";
/**  自动化发布脚本执行失败  */
    public static final String AUTO_RELEASE_FAILED="RLS0014";
/**  任务阶段{0}->{1}变更失败!  */
    public static final String CHANGE_TASK_STAGE_ERROR = "TSE0004";
/**  请输入正确的HTTP克隆地址!  */
    public static final String CLONE_URL_ERROR="ITF0008";
/**  Merge Request提交失败!{0}  */
    public static final String CREATE_MERGE_REQUEST_ERROR="RLS0002";
/**  TAG创建失败!{0}  */
    public static final String CREATE_TAG_ERROR="RLS0001";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  时间格式不正确！  */
    public static final String DATE_FROMAT_ERROR="RLS0007";
/**  删除文件失败  */
    public static final String DELETE_FILE_ERROR="TASK001";
/**  删除任务失败:{0}!  */
    public static final String DELETE_TASK_ERROR="DTE0001";
/**  此应用存在任务未确认投产,请联系行内项目负责人审核后提交发布  */
    public static final String EXIST_TASK_NOT_AUDITED="RLS0008";
/**  此应用下存在任务未完成关联项审核，请联系任务相关人员审核  */
    public static final String EXIST_TASK_REVIEWS_NOT_AUDIT="RLS0009";
/**  数据库审核材料仅支持上传.zip压缩包格式，请重新上传!  */
    public static final String FILE_FMT_ERROR="ITF0016";
/**  对文件MD5出错！  */
    public static final String FILE_MD5_ERROR="ITF0012";
/**  文件{0}上传GitLab失败！  */
    public static final String FILE_UPLOAD_GITLAB_FAILED="COM0009";
/**  初审失败  */
    public static final String FIRST_EVIEW_ERROR="TASK005";
/**  生成EXCEL失败  */
    public static final String GENERATE_EXCEL_ERROR="TSK0004";
/**  GitLab请求异常！{0}  */
    public static final String GITLAB_REQUEST_ERROR="RLS0015";
/**  gitlab服务异常！{0}  */
    public static final String GITLAB_SERVER_ERROR="APP0005";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="RLS0003";
/**  该任务关联多个主任务  */
    public static final String MAIN_TASK_ERROR="MTE0001";
/**  分支合并状态异常！  */
    public static final String MERGE_STATE_ERROR="RLS0005";
/**  通知信息查询失败！  */
    public static final String NOTIFY_QUERY_ERROR="NTF0005";
/**  其他用户正在进行变更介质准备，请勿重复操作！  */
    public static final String OTHER_USER_AUDITING="RLS0013";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COM0007";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  变更模板文件上传文件失败  */
    public static final String PROD_TEMP_FILE_UPLOAD_FAILED="RLS0010";
/**  当前变更模板无变更应用目录，无法添加变更应用，请更换变更模板后添加变更应用！  */
    public static final String PROD_TEMP_NOT_EXIST_PROD_APPLICATION="RLS0011";
/**  读取json文件出错！  */
    public static final String READ_JSON_FILE_ERROR="ITF0013";
/**  redmine权限验证失败  */
    public static final String REDMINE_AUTH_FORBBIDEN="RSE0002";
/**  redmine其他异常{0}  */
    public static final String REDMINE_OTHER_ERROR="RSE0003";
/**  redmine服务异常  */
    public static final String REDMINE_SERVER_ERROR="RSE0001";
/**  投产时间不能早于当前时间！{0}  */
    public static final String RELEASE_DATE_ERROR="RLS0006";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="COM0001";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  RestHeader.json文件格式不正确!  */
    public static final String RESTHEADER_FORMAT_ERROR="ITF0005";
/**  RestHeader.json文件未找到或不存在!  */
    public static final String RESTHEADER_IS_NOTEXIST="ITF0004";
/**  审核人不能重复,另一个审人为{0}{1}  */
    public static final String REVIEW_REVIEWERS_ERROR="TASK004";
/**  审核状态异常{0}  */
    public static final String REVIEW_STATUS_ERROR="TASK003";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  发送需求模块请求异常--{0}  */
    public static final String RQRMNT_ERROR="RQR0001";
/**  解析schema-config.xml文件出错！  */
    public static final String SCHEMA_CONFIG_FILE_ERROR="ITF0014";
/**  schema文件格式不正确!  */
    public static final String SCHEMA_FORMAT_ERROR="ITF0006";
/**  schema文件不存在!  */
    public static final String SCHEMA_IS_NOTEXIST="ITF0003";
/**  schema中param为空！  */
    public static final String SCHEMA_PARAM_NULL="ITF0001";
/**  {0}的restUriMapping内param属性中URL不能为空!  */
    public static final String SCHEMA_PARAM_URL_NULL="ITF0002";
/**  发送邮件失败  */
    public static final String SEND_EMAIL_ERROR="TASK008";
/**  服务器错误  */
    public static final String SERVER_ERROR="COM0002";
/**  {0}文件中serviceId不一致!  */
    public static final String SERVICEID_IS_UNLIKE="ITF0007";
/**  创建数据库审核失败  */
    public static final String SQL_REVIEW_ERROR="TASK002";
/**  src文件夹不存在!  */
    public static final String SRC_FILE_NOT_EXIST="ITF0009";
/**  任务已归档，不予修改  */
    public static final String TASK_STAGE_ERROR="TSE0001";
/**  发送测试通知失败，请联系fdev管理人员！  */
    public static final String TEST_NOTIFY_ERROR="TSK0001";
/**  测试平台服务异常{0}  */
    public static final String TEST_SERVER_ERROR="TSE0002";
/**  关联主子任务失败  */
    public static final String TEST_SERVER_ERROR2="TSE0003";
/**  解析*-transport.xml文件出错！  */
    public static final String TRANSPORT_FILE_ERROR="ITF0015";
/**  当前已在变更目录{0}上传文件，选择变更模板[{1}]无此目录，请删除目录下文件后选择此模板！  */
    public static final String UNCOMPATIBLE_TEMPLATE="RLS0012";
/**  更新审核失败  */
    public static final String UPDATE_REVIEW_ERROR="TASK007";
/**  用户通知信息存储失败！  */
    public static final String USERMESSAGE_SAVE_ERROR="NTF0003";
/**  用户通知信息状态更新失败！  */
    public static final String USERMESSAGE_UPDATESTATUS_ERROR="NTF0004";
/**  用户认证失败  */
    public static final String USR_AUTH_FAIL="USR0004";
/**  公司,小组,权限,角色,标签有不存在项  */
    public static final String USR_CHECK_ENTITY_ERROR="USR0010";
/**  删除对象存在子节点，无法删除  */
    public static final String USR_DELETE_ERROR="USR0006";
/**  用户邮箱格式错误  */
    public static final String USR_EMAILERROR="USR0008";
/**  用户已存在  */
    public static final String USR_EXISTED="USR0009";
/**  首次登入  */
    public static final String USR_FIRST_LOGIN="USR1000";
/**  该小组中文名已经存在  */
    public static final String USR_GROUP_EXEIT="USR0013";
/**  该数据有用户使用,无法操作  */
    public static final String USR_INUSE_ERROR="USR0012";
/**  登录失败  */
    public static final String USR_LOGIN_FAIL="USR0005";
/**  用户不存在  */
    public static final String USR_NOT_EXIST="USR0011";
/**  无该父节点  */
    public static final String USR_NO_PARENTID="USR0014";
/**  websocket发送信息失败！  */
    public static final String WEBSOCKET_SEND_ERROR="NTF0001";
    /**  指定的初审人不能在复审人中  */
    public static final String REVIEW_ERROR = "TASK009";
    /**  任务名不能重名  */
    public static final String TASK_NAME_EROOR = "TASK006";
    /**   发起数据库审核时，必须要上传 */
    public static final String Review_Fail = "TASK0010";
    /**    审核关联 */
    public static final String REVIEW_RELE_FIRST = "TASK0011";
    public static final String REVIEW_RELE_SECOND = "TASK0012";
    /**    更新失败 */
    public static final String UPDATE_FALSE = "TASK0013";
    public static final String FILE_NOT_EXIT = "TASK0014";
    public static final String TASK_ERROR = "TASK0015";
    /**    查询条件不可为空 */
    public static final String JQL_TASKID_ERROR = "JIRA001";

    /**  jira权限验证失败  */
    public static final String JIRA_ERROR = "JIRA002";
    /**  jira服务异常  */
    public static final String JIRA_EXCEPTION = "JIRA003";

    /**  任务名不存在！  */
    public static final String TASK_NOT_EXIST = "TASK0016";

    /**  发送流程模块请求异常--{0}  */
    public static final String PROCESS_ERROR="PRO0001";

    /**  计划开始时间不得大于计划结束时间 */
    public static final String PLAN_DATE_ERROR="PRO0002";

    /**  开始时间不得大于结束时间 */
    public static final String DATE_ERROR="PRO0003";

    /**  {0}不能晚于{1} */
    public static final String DATE_AFTER_ERROR="PRO0004";

}

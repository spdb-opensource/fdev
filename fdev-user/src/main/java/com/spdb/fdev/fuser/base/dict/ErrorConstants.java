/**
 * ErrorConstants.java Created on Sat Feb 20 15:36:54 CST 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.fuser.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Sat Feb 20 15:36:54 CST 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  添加mantis用户失败{0}  */
    public static final String ADD_MANTISUSER_ERROR="USR0019";
/**  投产应用信息不存在！  */
    public static final String APP_NOT_EXIST="RLS0004";
/**  该目录已被使用，不可删除  */
    public static final String ASSET_CALALOG_IS_USED="RLS0017";
/**  自动化发布脚本执行失败  */
    public static final String AUTO_RELEASE_FAILED="RLS0014";
/**  不可更换与之前相同的投产窗口  */
    public static final String CAN_NOT_CHANGE_NODE_SAME_AS_BEFORE="RLS0020";
/**  Merge Request提交失败!{0}  */
    public static final String CREATE_MERGE_REQUEST_ERROR="RLS0002";
/**  TAG创建失败!{0}  */
    public static final String CREATE_TAG_ERROR="RLS0001";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  时间格式不正确！  */
    public static final String DATE_FROMAT_ERROR="RLS0007";
/**  该变更已存在相同应用  */
    public static final String EXIST_APP_IN_PROD="RLS0019";
/**  已存在同名应用变更镜像，请先删除该应用再添加  */
    public static final String EXIST_LEGACY_APP_CAN_NOT_ADD="RLS0021";
/**  此应用存在任务未确认投产,请联系行内负责人审核后提交发布  */
    public static final String EXIST_TASK_NOT_AUDITED="RLS0008";
/**  此应用下存在任务未完成关联项审核，请联系任务相关人员审核  */
    public static final String EXIST_TASK_REVIEWS_NOT_AUDIT="RLS0009";
/**  文件{0}上传GitLab失败！  */
    public static final String FILE_UPLOAD_GITLAB_FAILED="COM0009";
/**  获取应用厂商负责人信息失败  */
    public static final String GET_APP_DEV_MANAGERS_INFO_ERROR="RLS0015";
/**  该应用在gitlab中不存在！  */
    public static final String GITLAB_PROJECT_NOT_EXIST="RLS0018";
/**  gitlab请求异常  */
    public static final String GITLAB_REQUEST_ERROR="COM0011";
/**  gitlab服务异常  */
    public static final String GITLAB_SERVER_ERROR="COM0010";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="RLS0003";
/**  分支合并状态异常！  */
    public static final String MERGE_STATE_ERROR="RLS0005";
/**  其他用户正在进行变更介质准备，请勿重复操作！  */
    public static final String OTHER_USER_AUDITING="RLS0013";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COM0007";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COM0006";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  变更模板文件上传文件失败  */
    public static final String PROD_TEMP_FILE_UPLOAD_FAILED="RLS0010";
/**  当前变更模板无变更应用目录，无法添加变更应用，请更换变更模板后添加变更应用！  */
    public static final String PROD_TEMP_NOT_EXIST_PROD_APPLICATION="RLS0011";
/**  投产时间不能早于当前时间！{0}  */
    public static final String RELEASE_DATE_ERROR="RLS0006";
/**  投产窗口已归档  */
    public static final String RELEASE_NODE_ARCHIVED="RLS0016";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="COM0001";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  配置文件gitlab地址不存在  */
    public static final String RESOURCE_IS_NOT_EXIST="COM0013";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  服务器错误  */
    public static final String SERVER_ERROR="COM0002";
/**  当前已在变更目录{0}上传文件，选择变更模板[{1}]无此目录，请删除目录下文件后选择此模板！  */
    public static final String UNCOMPATIBLE_TEMPLATE="RLS0012";
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
/**  账号名或密码错误!{0}  */
    public static final String USR_INCORRECT_USERNAME_PASSWORD="USR0020";
/**  该数据有用户使用,无法操作  */
    public static final String USR_INUSE_ERROR="USR0012";
/**  标签已被使用，无法删除  */
    public static final String USR_LABELUSE_ERROR="USR0017";
/**  行内用户请使用LDAP方式登陆  */
    public static final String USR_LDAP_CHANNEL_LOGIN="USR0021";
/**  用户已离职，无法登录  */
    public static final String USR_LOGIN_DIMISSION_ERROR="USR0016";
/**  登录失败  */
    public static final String USR_LOGIN_FAIL="USR0005";
/**  用户不存在  */
    public static final String USR_NOT_EXIST="USR0011";
/**  无该父节点  */
    public static final String USR_NO_PARENTID="USR0014";
/**  密码错误{0}  */
    public static final String USR_PASSWORD_ERROR="USR0015";
/**  用户权限错误!{0}  */
    public static final String USR_PERMISSION_ERROR="USR0022";
/**  模拟用户失败，失败原因：{0}  */
    public static final String USR_SIMULATEUSER_ERROR="USR0018";
}

/**
 * ErrorConstants.java Created on Thu May 26 10:12:35 CST 2022
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.base.dict;
/**
 * FDEV系统错误字典文件
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  应用{0}未绑定系统  */
    public static final String APPLICATION_NOT_BIND_SYSTEM="RLS0053";
/**  投产应用信息不存在！  */
    public static final String APP_NOT_EXIST="RLS0004";
/**  该目录已被使用，不可删除  */
    public static final String ASSET_CALALOG_IS_USED="RLS0017";
/**  自动化发布脚本执行失败  */
    public static final String AUTO_RELEASE_FAILED="RLS0014";
/**  批量任务已在变更中添加，不可删除  */
    public static final String BATCHTASK_IS_USED_CAN_NOT_DELETE="RLS0065";
/**  应用{0}对应分支{1}不存在！  */
    public static final String BRANCH_NOT_EXIST="RLS0042";
/**  不可更换与之前相同的投产窗口  */
    public static final String CAN_NOT_CHANGE_NODE_SAME_AS_BEFORE="RLS0020";
/**  {0}投产窗口下已有任务，不允许修改投产窗口类型！  */
    public static final String CAN_NOT_UPDATE_RELEASE_NODE_TYPE="RLS0045";
/**  投产应用配置文件变化未审核，不能添加变更！  */
    public static final String CONFIG_FILE_WITHOUT_AUDIT="RLS0041";
/**  投产应用配置文件变化未审核，不能选择TAG！  */
    public static final String CONFIG_FILE_WITHOUT_AUDIT_TAG="RLS0056";
/**  创建{0}分支失败  */
    public static final String CREATE_BRANCH_FAILED="RLS00030";
/**  Merge Request提交失败!{0}  */
    public static final String CREATE_MERGE_REQUEST_ERROR="RLS0002";
/**  TAG创建失败!{0}  */
    public static final String CREATE_TAG_ERROR="RLS0001";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  时间格式不正确！  */
    public static final String DATE_FROMAT_ERROR="RLS0007";
/**  删除{0}分支失败  */
    public static final String DELETE_BRANCH_FAILED="RLS00031";
/**  TAG删除失败!{0}  */
    public static final String DELETE_TAG_ERROR="RLS0032";
/**  该应用未在{0}平台部署过！{1}  */
    public static final String DEPLOY_TYPE_ERROR="RLS0058";
/**  用户{0}不在预设置的运维账户中  */
    public static final String ERROR_OPER_USER="COM0015";
/**  用户{0}账号密码输入错误  */
    public static final String ESF_USER_VERIFY_FAILED="COM0014";
/**  该变更已存在相同应用  */
    public static final String EXIST_APP_IN_PROD="RLS0019";
/**  已在同一个总介质目录下添加此应用，请勿重复添加  */
    public static final String EXIST_APP_IN_PROD_ASSETS_VERSION="RLS0024";
/**  已存在同名应用变更镜像，请先删除该应用再添加  */
    public static final String EXIST_LEGACY_APP_CAN_NOT_ADD="RLS0021";
/**  此应用存在任务未确认投产,请联系行内负责人审核后提交发布  */
    public static final String EXIST_TASK_NOT_AUDITED="RLS0008";
/**  此应用下存在任务未完成关联项审核，请联系任务相关人员审核  */
    public static final String EXIST_TASK_REVIEWS_NOT_AUDIT="RLS0009";
/**  拉取分支失败！{0}  */
    public static final String FAIL_PULL_BRANCH="RLS0044";
/**  文件{0}上传GitLab失败！  */
    public static final String FILE_UPLOAD_GITLAB_FAILED="COM0009";
/**  生成模版文件失败  */
    public static final String GENERATE_EXCEL_FAIL="RLS0029";
/**  获取应用厂商负责人信息失败  */
    public static final String GET_APP_DEV_MANAGERS_INFO_ERROR="RLS0015";
/**  该应用在gitlab中不存在！  */
    public static final String GITLAB_PROJECT_NOT_EXIST="RLS0018";
/**  gitlab请求异常！{0}  */
    public static final String GITLAB_REQUEST_ERROR="COM0011";
/**  gitlab服务异常  */
    public static final String GITLAB_SERVER_ERROR="COM0010";
/**  未查找到相关小组标识！  */
    public static final String GROUP_ABBR_NOT_EXIST="RLS0028";
/**  小组标识已被其他小组占用，请更换  */
    public static final String GROUP_ABBR_REPET_INSERT_REEOR="CM00014";
/**  应用{0}已经添加过{1}平台的信息！  */
    public static final String HAS_TYPE_PALATFORM="RLS0061";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="RLS0003";
/**  旧版自动化发布弹性扩展必须填写变更批次，请删除后重新添加！  */
    public static final String LACK_PROD_SEQUENCE="RLS0040";
/**  请完善变更模板的小组系统缩写!  */
    public static final String LACK_SYSTEM_ABBR="RLS0031";
/**  锁定人不是同一人！  */
    public static final String LOCK_PEOPLE_NOT_SAME_PERSON="RLS0060";
/**  分支合并状态异常！  */
    public static final String MERGE_STATE_ERROR="RLS0005";
/**  开始时间和结束时间不能同时为空  */
    public static final String N0T_START_OR_END_DATE_ERROR="RLS0051";
/**  {0}分支已存在,必须手动删除  */
    public static final String NEED_DELETE_SAME_BRANCH="RLS0022";
/**  应用{0}无镜像标签！  */
    public static final String NO_PRO_IMG_URI="RLS0062";
/**  没有上送镜像标签！  */
    public static final String NO_TAG="RLS0064";
/**  其他用户正在进行变更介质准备，请勿重复操作！  */
    public static final String OTHER_USER_AUDITING="RLS0013";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COMM003";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COMM004";
/**  同一组同一时刻只能有一个发布说明!  */
    public static final String PRODNOTE_REPEAT="CM0020";
/**  同一组同一时刻只能有一个变更!  */
    public static final String PRODRECORD_REPEAT="CM0018";
/**  变更模板文件上传文件失败  */
    public static final String PROD_TEMP_FILE_UPLOAD_FAILED="RLS0010";
/**  当前变更模板无docker目录，无法添加变更应用，请更换变更模板后添加变更应用！  */
    public static final String PROD_TEMP_NOT_EXIST_PROD_APPLICATION="RLS0011";
/**  变更记录与变更模版类型不相符  */
    public static final String PROD_TYEP_DIFF_WHIT_TEMP_TPYE="RLS0025";
/**  已存在此变更版本的变更记录，请勿重复添加  */
    public static final String PROD_VERSION_REPET="RLS0027";
/**  项目初始化尚未完成，请稍后重试！  */
    public static final String PROJECT_INIT_FAILED="RLS0048";
/**  任务[{0}]当前不在uat或rel阶段，无法进行生产打包  */
    public static final String REALEASE_NOT_IN_UAT_REL="RLS0035";
/**  任务{0}当前未完成安全测试，无法进行生产打包  */
    public static final String REALEASE_NOT_SAFET_TEST="CM0022";
/**  投产时间不能早于当前时间！{0}  */
    public static final String RELEASE_DATE_ERROR="RLS0006";
/**  该应用未曾在{0}平台投产！{1}  */
    public static final String RELEASE_DEPLOY_TYPE_ERROR="RLS0059";
/**  缺少{0}环境FDEV配置文件，请检查！{1}  */
    public static final String RELEASE_LACK_FDEV_CONFIG="RLS0037";
/**  该应用未曾投产！{0}  */
    public static final String RELEASE_LACK_MIRROR_URI="RLS0038";
/**  投产窗口已归档  */
    public static final String RELEASE_NODE_ARCHIVED="RLS0016";
/**  {0}投产窗口已过期  */
    public static final String RELEASE_NODE_EXPIRED="RLS0039";
/**  投产窗口下已存在{0}，无法删除！  */
    public static final String RELEASE_NODE_NOT_DELETABLE="RLS0055";
/**  当前任务所属应用{0}已存在于{1}{2}下投产窗口,请勿挂载其他小组投产窗口  */
    public static final String RELEASE_NODE_NOT_EQUALS="RLS0034";
/**  {0}投产窗口不存在  */
    public static final String RELEASE_NODE_NOT_EXIST="RLS0036";
/**  当前应用不在{0}租户下  */
    public static final String RELEASE_NODE_NOT_LEASEHOLDER="CM0021";
/**  投产窗口与应用类型不匹配！{0}  */
    public static final String RELEASE_NODE_TYPE_UNNORMAL="RLS0043";
/**  未包含可提测需求！  */
    public static final String RELEASE_RQRMNT_INFO_LIST_EMPTY="RLS0052";
/**  无特殊流程内容！  */
    public static final String RELEASE_RQRMNT_INFO_LIST_NOT_INCLUDE_SPECIAL="RLS0054";
/**  同一批次下不能添加相同应用  */
    public static final String REPEAT_PROD_APP_SCALE="RLS0030";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="COM0001";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  配置文件地址不存在，请重新输入  */
    public static final String RESOURCE_GIT_URL_NOT_EXIST="CM00016";
/**  配置文件地址不能相同!  */
    public static final String RESOURCE_GIT_URL_REPEAT="CM0017";
/**  配置文件gitlab地址不存在  */
    public static final String RESOURCE_IS_NOT_EXIST="COM0013";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  获取gk值异常！  */
    public static final String SDK_GK_ERROR="RLS0063";
/**  服务器错误  */
    public static final String SERVER_ERROR="COM0002";
/**  开始时间不能大于结束时间  */
    public static final String START_DATE_ERROR="RLS0050";
/**  系统名称已被其他系统占用，请更换  */
    public static final String SYSTEM_ABBR_REPET_INSERT_REEOR="CM00015";
/**  任务为确认投产  */
    public static final String TASK_NOT_AUDIT="RLS0023";
/**  {0}变更模板已存在！  */
    public static final String TEMPLATE_EXISTS="RLS0046";
/**  模版正在被使用，不可删除  */
    public static final String TEMPLATE_IS_USED_CAN_NOT_DELETE="RLS0026";
/**  {0}存在多个变更模板，请删除！  */
    public static final String TEMPLATE_MUST_BE_DELETE="RLS0047";
/**  {0}没有变更模板，请添加！  */
    public static final String TEMPLATE_NOT_EXISTS="RLS0049";
/**  其他后台服务异常{0}  */
    public static final String THIRD_SERVER_ERROR="COMM001";
/**  当前已在变更目录{0}上传文件，选择变更模板[{1}]无此目录，请删除目录下文件后选择此模板！  */
    public static final String UNCOMPATIBLE_TEMPLATE="RLS0012";
/**  更新任务状态至已投产失败!  */
    public static final String UPDATE_TASKSTATUS_FAILED="CM0019";
/**  用户认证失败  */
    public static final String USR_AUTH_FAIL="COM0012";
/**  变更版本格式不符合规范！  */
    public static final String VERSION_NOT_PERMIT="RLS0057";
}

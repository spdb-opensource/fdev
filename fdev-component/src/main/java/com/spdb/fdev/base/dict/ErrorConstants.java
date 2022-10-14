/**
 * ErrorConstants.java Created on Tue Mar 02 18:29:16 GMT+08:00 2021
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
/**  分支名称错误{0}  */
    public static final String BRANCHNAMEERROR="CMP0016";
/**  该分支不存在{0}  */
    public static final String BRANCH_DONOT_EXIST="CMP0017";
/**  分支已存在{0}  */
    public static final String BRANCH_EXISTS="CMP0042";
/**  {0}添加持续集成失败  */
    public static final String CONTINUE_INTEGRATION_ERROR="CMP0019";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="CMP0004";
/**  保存部署模板出错：{0}！  */
    public static final String DEPLOY_TEMPLATE_SAVE_ERROR="CMP0032";
/**  当前开发分支不能废弃!{0}  */
    public static final String DEV_DESTORY_FORBID="CMP0046";
/**  开发需求{0}已存在  */
    public static final String DEV_ISSUE_EXIST="CMP0048";
/**  当前开发分支不能迁移!{0}  */
    public static final String DEV_TEANSFER_FORBID="CMP0049";
/**  文件上传GitLab失败！  */
    public static final String FILE_UPLOAD_GITLAB_FAILED="CMP0005";
/**  Gitlab创建{0}项目失败  */
    public static final String GITLAB_CREATE_ERROR="CMP0018";
/**  该{0}已被录入，已录入的{1}英文名为{2}！  */
    public static final String GITLAB_ID_ERROR="CMP0050";
/**  gitlab请求异常{0}  */
    public static final String GITLAB_REQUEST_ERROR="CMP0006";
/**  gitlab服务异常{0}  */
    public static final String GITLAB_SERVER_ERROR="CMP0007";
/**  当前只能有一个开发阶段优化需求  */
    public static final String IMAGE_ISSUE_LIMIT="CMP0031";
/**  当前镜像{0}版本不存在  */
    public static final String IMAGE_NOT_EXIST="CMP0028";
/**  仅镜像试用(stage)阶段允许发布  */
    public static final String IMAGE_PACKAGE_STAGE="CMP0030";
/**  扫描组件{0}的应用使用情况失败  */
    public static final String INIT_COMPONENTAPPLICATION_ERROR="CMP0023";
/**  获取组件{0}在NEXUS仓库历史版本失败  */
    public static final String INIT_COMPONENTRECORD_ERROR="CMP0022";
/**  {0}添加MAVEN骨架失败  */
    public static final String INIT_MAVENARCHETYPE_ERROR="CMP0020";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="CMP0033";
/**  当前优化需求不能废弃!{0}  */
    public static final String ISSUE_DESTORY_FORBID="CMP0040";
/**  恢复为正式版本需为Invalid版本  */
    public static final String IS_NOT_INVALID="CMP0029";
/**  JDK版本传值错误{0}  */
    public static final String JDKERROR="CMP0015";
/**  当前项目分支存在未合并Merege操作{0}  */
    public static final String MERGE_EXISTS="CMP0038";
/**  调用{0}模块接口{1}失败,错误信息如下{2}  */
    public static final String MODULE_REQUEST_ERROR="CMP0047";
/**  当前有版本{0}未完成Release发布  */
    public static final String OPTIMIZE_ARCHETYPE_LIMIT_ERROR="CMP0026";
/**  当前有版本{0}未完成Release发布  */
    public static final String OPTIMIZE_COMPONENT_LIMIT_ERROR="CMP0025";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COMM003";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COMM004";
/**  版本{0}格式错误,{1}  */
    public static final String PATTERN_ERROR="CMP0044";
/**  当前项目分支存在未完成Pipeline操作{0}  */
    public static final String PIPELINE_EXISTS="CMP0039";
/**  当前组件{0}在Gitlab不存在  */
    public static final String PROJECT_NOT_EXIST_IN_GITLAB="CMP0021";
/**  获取依赖树失败  */
    public static final String QUERY_DEPENDENCY_TREE="CMP0027";
/**  须经过一次beta出包才可以release发布  */
    public static final String RELEASE_PACKAGE_LIMIT="CMP0041";
/**  当前RELEASE窗口不能废弃!{0}  */
    public static final String RELEASE_WINDOW_DESTORY_FORBID="CMP0045";
/**  RELEASE窗口已存在{0}  */
    public static final String RELEASE_WINDOW_EXISTS="CMP0043";
/**  数据重复插入!{0}  */
    public static final String REPET_INSERT_REEOR="CMP0002";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="CMP0034";
/**  配置文件gitlab地址不存在  */
    public static final String RESOURCE_IS_NOT_EXIST="CMP0008";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="CMP0009";
/**  请勿频繁发起扫描操作，可在{0}分钟后重试！  */
    public static final String SCAN_OPERATION_TOO_FREQUENT="CMP0001";
/**  服务器错误  */
    public static final String SERVER_ERROR="CMP0010";
/**  状态错误{0}  */
    public static final String STAGE_ERROR="CMP0013";
/**  Tag已存在{0}  */
    public static final String TAG_EXISTS="CMP0012";
/**  其他后台服务异常  */
    public static final String THIRD_SERVER_ERROR="COMM001";
/**  标题已存在{0}  */
    public static final String TITLE_EXISTS="CMP0037";
/**  SNAPSHOT和RC只能设置为测试版本,RELEASE版本不能设置为测试版本  */
    public static final String UPDATE_COMPONENTRECORD_ERROR="CMP0024";
/**  alpha、beta和rc只能设置为测试版本,正式发布版本不能设置为测试版本  */
    public static final String UPDATE_MPASSCOMPONENTRECORD_ERROR="CMP0035";
/**  Gitlab地址错误{0}  */
    public static final String URLERROR="CMP0014";
/**  用户认证失败!{0}  */
    public static final String USR_AUTH_FAIL="CMP0011";
/**  版本已存在{0}  */
    public static final String VERSION_EXISTS="CMP0036";
}

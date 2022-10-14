/**
 * ErrorConstants.java Created on Fri Mar 26 14:02:27 GMT+08:00 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.fdevenvconfig.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Fri Mar 26 14:02:27 GMT+08:00 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {

private ErrorConstants() {}
/**  申请更新实体与环境映射信息出错：{0}！  */
    public static final String APPLY_UPDATE_MODEL_ENV_ERROR="ENV0008";
/**  文件读写传输错误  */
    public static final String APP_FILE_ERROR="ENV0004";
/**  应用信息不存在！  */
    public static final String APP_NOT_EXIST="RLS0004";
/**  蓝鲸接口异常  */
    public static final String BULEKING_POST_ERROR="ENV0024";
/**  同步生产信息太频繁，请10分钟后重试  */
    public static final String BULEKING_POST_LIMIT="ENV0025";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="COM0005";
/**  Decrypt Error！  */
    public static final String DECODE_ERROR="ENV0015";
/**  删除实体{0}出错：{1}！  */
    public static final String DELETE_MODEL_ERROR="ENV0013";
/**  保存部署模板出错：{0}！  */
    public static final String DEPLOY_TEMPLATE_SAVE_ERROR="ENV0001";
/**  Encrypt Error！  */
    public static final String ENCODE_ERROR="ENV0014";
/**  环境不存在，该变动已被撤销  */
    public static final String ENV_NOT_EXIT_ERROR="ENV0020";
/**  gitlab服务异常！{0}  */
    public static final String GITLAB_SERVER_ERROR="ENV0005";
/**  新增实体与环境映射信息出错：{0}！  */
    public static final String INSERT_MODEL_ENV_ERROR="ENV0009";
/**  新增实体组出错：{0}！  */
    public static final String INSERT_MODEL_SET_ERROR="ENV0010";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="ENV0003";
/**  实体中不存在该属性:{0}，该变动已被撤销  */
    public static final String MODEL_ENV_FILED_ERROR="ENV0021";
/**  实体与环境映射中属性{0}为必填项，该变动已被撤销  */
    public static final String MODEL_ENV_FILED_MISS_ERROR="ENV0022";
/**  实体与环境映射不存在，该变动已被撤销  */
    public static final String MODEL_ENV_NOT_EXIT_ERROR="ENV0023";
/**  属性字段{0}的属性值不能包含空格！  */
    public static final String MODEL_ENV_VALUE_ERROR="ENV0017";
/**  实体不存在，该变动已被撤销  */
    public static final String MODEL_NOT_EXIT_ERROR="ENV0019";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  {0},没有找到对应的值  */
    public static final String PROPERTIES_ERROR="EVN0026";
/**  上传配置文件出错：{0}！  */
    public static final String PUSH_CONFIG_FILE_ERROR="ENV0002";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0008";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  执行脚本{0}出错：{1}！  */
    public static final String RUN_SCRIPT_ERROR="ENV0006";
/**  保存{0}出错：{1}！  */
    public static final String SAVE_ERROR="ENV0016";
/**  服务器错误:{0}!  */
    public static final String SERVER_ERROR="COM0002";
/**  更新实体{0}与环境{1}映射信息出错：{2}！  */
    public static final String UPDATE_MODEL_ENV_ERROR="ENV0007";
/**  更新实体{0}出错：{1}！  */
    public static final String UPDATE_MODEL_ERROR="ENV0012";
/**  用户认证失败:{0}!  */
    public static final String USR_AUTH_FAIL="ENV0018";
/**  验证码填写错误，请重新填写正确的验证码！  */
    public static final String VERFITYCODE_ERROR="ENV0011";
}

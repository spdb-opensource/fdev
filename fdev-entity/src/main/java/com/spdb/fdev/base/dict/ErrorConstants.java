/**
 * ErrorConstants.java Created on Fri Jul 02 14:55:56 CST 2021
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Fri Jul 02 14:55:56 CST 2021)
 * @author: Auto Generated
 */
public class ErrorConstants {

private ErrorConstants() {}
/**  文件读写传输错误  */
    public static final String APP_FILE_ERROR="ENV0004";
/**  配置模版中等号左边的key {0} 不能重复！  */
    public static final String CONFIGFILE_KEY_ERROR="ENT0003";
/**  存在依赖，无法删除!  */
    public static final String CON_NOT_DELETE="ENV0005";
/**  数据不存在！{0}  */
    public static final String DATA_NOT_EXIST="COMM0001";
/**  实体中英文名不可重复！  */
    public static final String ENTITY_NAME_ERROR="ENT0006";
/**  存在依赖，无法{0}!  */
    public static final String ENT_UPDATE_ERROR="ENT0005";
/**  环境中文名不能重复！{0}  */
    public static final String ENV_NAMECN_ERROR="ENV0003";
/**  环境英文名不能重复！{0}  */
    public static final String ENV_NAMEEN_ERROR="ENV0001";
/**  无法获取相关数据!{0}  */
    public static final String INVAILD_OPERATION_DATA="ENV0006";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COM0003";
/**  实体属性中文名不能重复！{0}  */
    public static final String PROPERTIES_NAMECN_ERROR="ENT0002";
/**  实体属性英文名不能重复！{0}  */
    public static final String PROPERTIES_NAMEEN_ERROR="ENT0001";
/**  上传配置文件出错：{0}！  */
    public static final String PUSH_CONFIG_FILE_ERROR="ENV0002";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="COM0005";
/**  该用户无权限执行此操作!{0}  */
    public static final String ROLE_ERROR="COM0004";
/**  服务器错误！{0}  */
    public static final String SERVER_ERROR="COMM0000";
}

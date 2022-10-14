/**
 * ErrorConstants.java Created on Mon Aug 17 18:59:58 GMT+08:00 2020
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.spdb.fdev.base.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Mon Aug 17 18:59:58 GMT+08:00 2020)
 * @author: Auto Generated
 */
public class ErrorConstants {

private ErrorConstants() {}
/**  数据不存在！{0}  */
    public static final String DATA_NOT_EXIST="SON0001";
/**  请求参数不能为空！{0}  */
    public static final String PARAM_CANNOT_BE_EMPTY="SON0002";
/**  请求重复提交！{0}  */
    public static final String REQUEST_ALREADY_EXIST="SON0004";
/**  该用户无权限执行此操作！{0}  */
    public static final String ROLE_ERROR="SON0003";
/**  服务器错误！{0}  */
    public static final String SERVER_ERROR="SON0000";
/**  非Java微服务不扫描！{0}  */
    public static final String SONAR_REJECTED="SON0005";
}

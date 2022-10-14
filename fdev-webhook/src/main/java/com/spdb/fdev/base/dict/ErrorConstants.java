/**
 * ErrorConstants.java Created on Sat Aug 08 08:22:43 GMT+08:00 2020
 * <p>
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.spdb.fdev.base.dict;

/**
 * FDEV系统错误字典文件
 * Creation date: (Sat Aug 08 08:22:43 GMT+08:00 2020)
 *
 * @author: Auto Generated
 */
public class ErrorConstants {

    private ErrorConstants() {
    }

    /**
     * 数据不存在！{0}
     */
    public static final String DATA_NOT_EXIST = "WHK0001";
    /**
     * 其他后台服务异常！{0}
     */
    public static final String OTHER_APP_ERROR = "WHK0005";
    /**
     * 请求参数不能为空！{0}
     */
    public static final String PARAM_CANNOT_BE_EMPTY = "WHK0002";
    /**
     * 请求重复提交！{0}
     */
    public static final String REQUEST_ALREADY_EXIST = "WHK0004";
    /**
     * 该用户无权限执行此操作！{0}
     */
    public static final String ROLE_ERROR = "WHK0003";
    /**
     * 服务器错误！{0}
     */
    public static final String SERVER_ERROR = "WHK0000";
}

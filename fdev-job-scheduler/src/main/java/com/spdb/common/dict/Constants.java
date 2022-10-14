/**
 * ErrorConstants.java Created on Mon Jan 13 15:57:19 GMT+08:00 2020
 * <p>
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.spdb.common.dict;

/**
 * 字典文件
 * Creation date: (Mon Jan 13 15:57:19 GMT+08:00 2020)
 *
 * @author: Auto Generated
 */
public class Constants {
    /**
     * Class {0} load Exception!
     */
    public static final String CLASSNOTFOUNDEXCEPTION = "COMM005";
    /**
     * 不能只填开始时间，请补充结束时间！
     */
    public static final String ENDTIME_IS_NOT_EXIST = "COMM009";
    /**
     * {0} 已存在！
     */
    public static final String IS_EXIST = "COMM007";
    /**
     * {0} 不存在！
     */
    public static final String IS_NOT_EXIST = "COMM008";
    /**
     * 交易成功返回码
     */
    public static final String I_SUCCESS = "AAAAAAA";
    /**
     * 交易成功响应信息
     */
    public static final String M_SUCCESS = "交易执行成功";
    /**
     * 请求参数{0}不能同时为空!
     */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY = "COMM003";
    /**
     * 请求参数{0}不能为空!
     */
    public static final String PARAM_CANNOT_BE_EMPTY = "COMM002";
    /**
     * 请求参数{0}异常!{1}
     */
    public static final String PARAM_ERROR = "COMM004";
    /**
     * {0}调度任务异常
     */
    public static final String SCHEDULEREXCEPTION = "COMM006";
    /**
     * 其他后台服务异常
     */
    public static final String THIRD_SERVER_ERROR = "COMM001";
    /**
     * 暂停策略日期需唯一
     */
    public static final String PAUSED_DATE_ERROR = "COMM010";

    /**  Job执行模板类  */
    public static final String JOB_CLASS_NAME="com.spdb.quartz.JobTemplate";
}

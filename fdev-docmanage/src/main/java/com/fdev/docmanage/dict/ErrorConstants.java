/**
 * ErrorConstants.java Created on Tue Aug 11 20:12:51 CST 2020
 * 
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.fdev.docmanage.dict;
/**
 * FDEV系统错误字典文件
 * Creation date: (Tue Aug 11 20:12:51 CST 2020)
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  无效请求!  */
    public static final String BAD_REQUEST_ERROR="WPS0007";
/**  资源冲突!  */
    public static final String CONFLICT_ERROR="WPS0002";
/**  用户操作数据添加失败!  */
    public static final String DATA_ADD_ERROR="COMS008";
/**  minio文件删除失败  */
    public static final String FILE_DELETE_MINIO_FAILAD="OSS0004";
/**  minio文件下载失败  */
    public static final String FILE_DOWLOAD_MINIO_FAILAD="OSS0002";
/**  文件{0}下载失败！  */
    public static final String FILE_DOWNLOAD_GITLAB_FAILED="COMS003";
/**  minio文件路径不存在  */
    public static final String FILE_PATH_NO_EXIST="OSS0003";
/**  文件保存至临时目录失败！  */
    public static final String FILE_SAVE_ERROR="COMS005";
/**  文件{0}上传失败！  */
    public static final String FILE_UPLOAD_GITLAB_FAILED="COMS002";
/**  文件上传minio失败  */
    public static final String FILE_UPLOAD_MINIO_FAILAD="OSS0001";
/**  获取文件列表失败！  */
    public static final String FILE_VIEW_ERROR="COMS006";
/**  拒绝访问!  */
    public static final String FORBIDDEN_ERROR="WPS0004";
/**  gitlab服务异常！{0}  */
    public static final String GITLAB_SERVER_ERROR="COMS004";
/**  资源不存在!  */
    public static final String NOT_FOUND_ERROR="WPS0005";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMS007";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COMS001";
/**  请求频率太高!  */
    public static final String TOO_MANY_REQUESTS_ERROR="WPS0006";
/**  请求未认证!  */
    public static final String UNAUTHENTICATED_ERROR="WPS0003";
/**  用户鉴权失败!  */
    public static final String USR_AUTH_FAIL="COMS0009";
/**  创建上传事务失败!  */
    public static final String WPS_ERROR="WPS0008";
}

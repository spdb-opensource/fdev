package com.spdb.fdev.release.service;

import com.spdb.fdev.common.JsonResult;

import java.util.Map;

public interface IMergedInfoCallBackService {

    /***
     * release*合并到master分支
     *  @param requestParam 请求参数
     * @return
     */
    JsonResult mergedCallBack(Map<String, Object> requestParam) throws Exception;

    /***
     * 任意分支合并到transition*分支
     * @param requestParam 请求参数
     * @return
     */
    JsonResult testRunMergedCallBack(Map<String, Object> requestParam) throws Exception;
}

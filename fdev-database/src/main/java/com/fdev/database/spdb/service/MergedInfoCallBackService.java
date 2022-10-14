package com.fdev.database.spdb.service;

import com.spdb.fdev.common.JsonResult;


public interface MergedInfoCallBackService {

    /***
     * release*合并到master分支
     * @return
     */
    JsonResult mergedCallBack(Integer projectId, String sourceBranch, String mergeBranch) throws Exception;

    /***
     * release*合并到测试分支
     * @return
     */
    JsonResult mergedCallBackSit(Integer projectId, String sourceBranch, String mergeBranch) throws Exception;
    
}

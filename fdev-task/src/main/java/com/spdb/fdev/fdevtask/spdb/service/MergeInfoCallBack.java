package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.common.JsonResult;

public interface MergeInfoCallBack {


    JsonResult mergeInfoCallBack(String project_id, String iid, String state, String username) throws Exception;
}

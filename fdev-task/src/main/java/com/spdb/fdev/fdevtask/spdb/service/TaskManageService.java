package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.common.JsonResult;

import java.util.Map;

public interface TaskManageService {
    void checkAuthentication(Map<String, Object> requestParam) throws Exception;

    JsonResult generateTestPackage(Map<String, Object> requestParam) throws Exception;

    JsonResult checkIamsProperties(Map<String, Object> requestParam) throws Exception;

    JsonResult queryParamFile() throws Exception;

    void generatePackageFile(Map<String, Object> requestParam) throws Exception;
}

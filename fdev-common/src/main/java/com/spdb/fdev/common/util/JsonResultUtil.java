package com.spdb.fdev.common.util;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.dict.Constants;

public class JsonResultUtil {

    public static String getApplicationName() {
        return applicationName;
    }

    public static void setApplicationName(String applicationName) {
        JsonResultUtil.applicationName = applicationName;
    }

    private static String applicationName;

    public static JsonResult buildSuccess(Object data) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode(Constants.I_SUCCESS);
        jsonResult.setMsg(Constants.M_SUCCESS);
        jsonResult.setData(data);
        return jsonResult;
    }

    public static JsonResult buildSuccess() {
        return buildSuccess(null);
    }

    public static JsonResult buildError(String code, String msg) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode(code);
        jsonResult.setMsg(msg + "[" + applicationName + "]" );
        return jsonResult;
    }

}

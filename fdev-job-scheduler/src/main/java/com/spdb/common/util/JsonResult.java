package com.spdb.common.util;

import com.spdb.common.dict.Constants;

import java.io.Serializable;

/**
 * @author lizz
 */
public class JsonResult implements Serializable {

    private String code;

    private String msg;

    private Object data;

    private static String applicationName;

    public JsonResult() {
    }

    public JsonResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public JsonResult(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static JsonResult buildSuccess() {
        return buildSuccess(null);
    }

    public static JsonResult buildSuccess(Object data) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode(Constants.I_SUCCESS);
        jsonResult.setMsg(Constants.M_SUCCESS);
        jsonResult.setData(data);
        return jsonResult;
    }

    public static JsonResult buildError(String msg) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode("500");
        jsonResult.setMsg(msg + "[" + applicationName + "]");
        return jsonResult;
    }

    public static JsonResult buildError(String code, String msg) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode(code);
        jsonResult.setMsg(msg + "[" + applicationName + "]");
        return jsonResult;
    }

    public String getCode() {
        return code;
    }

    /**
     * 返回交易执行结果状态
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 返回交易执行结果消息
     *
     * @param msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    /**
     * 返回交易执行结果数据
     *
     * @param data
     */
    public void setData(Object data) {
        this.data = data;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    /**
     * 系统名
     *
     * @param applicationName
     */
    public static void setApplicationName(String applicationName) {
        JsonResult.applicationName = applicationName;
    }
}

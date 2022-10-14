package com.manager.ftms.util;

/**
 * 案例状态，已经异常
 */
public enum CaseStatus {

    // 正案例//接口类型
    IS_CaseS(1, "正案例"), THE_CaseS(1, "正案例"),
    //异常
    ERROR_PARAME(11111111, "参数异常，请稍后重试"),
    ERROR_SELECT(11111111, "查询异常，请稍后重试"),
    ERROR_INSERT(11111111, "新增异常，请稍后重试"),
    ERROR_UPDATE(11111111, "修改异常，请稍后重试"),
    ERROR_DELETE(11111111, "删除异常，请稍后重试"),
    ERROR_SYSTEM(11111111, "系統异常，请稍后重试");
    private final int code;
    private final String vasue;

    public int getCode() {
        return this.code;
    }

    public String getVasue() {
        return this.vasue;
    }

    CaseStatus(int code, String value) {
        this.code = code;
        this.vasue = value;
    }


}

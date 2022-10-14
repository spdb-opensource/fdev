package com.spdb.fdev.common.exception;

public class FdevException extends RuntimeException {
    private String code;

    private String message;

    private Object[] args;

    public FdevException(String code) {
        this.code = code;
    }

    public FdevException(String code, Object[] args) {
        this.code = code;
        this.args = args;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}

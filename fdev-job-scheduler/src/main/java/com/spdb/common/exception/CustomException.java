package com.spdb.common.exception;

/**
 * 自定义异常
 *
 * @author xxx
 */
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = -8166115114830213014L;

    private String code;

    private Object[] args;

    public CustomException() {
        super();
    }

    public CustomException(String code) {
        super(code);
        this.setCode(code);
    }

    public CustomException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public CustomException(String code, Object[] args) {
        this.setCode(code);
        this.setArgs(args);
    }

    public CustomException(String code, String message, Object[] args) {
        super(message);
        this.setCode(code);
        this.setArgs(args);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}

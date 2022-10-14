package com.spdb.fdev.base.utils;

import java.io.Serializable;

public class JsonResult implements Serializable {

	private static final long serialVersionUID = 7712930619725114298L;

	public static final String SUCCESS = "AAAAAA";
	
	public static final String ERROR = "999999";
	
	private String return_code;
	
	private String message;
	
	private Object data;
	
	/**
	 * Exception Handler
	 * @param e
	 */
	public JsonResult(Throwable e) {
		return_code = ERROR;
		data = null;
		message = e.getMessage();
	}
	
	public JsonResult(JsonResultBuilder jsonResultBuilder) {
		this.return_code = jsonResultBuilder.return_code;
		this.data = jsonResultBuilder.data;
		this.message = jsonResultBuilder.message;
	}

	public static class JsonResultBuilder {
		private String return_code;
		
		private String message;
		
		private Object data;
		
		public JsonResultBuilder return_code(String return_code) {
			this.return_code = return_code;
			return this;
		}
		
		public JsonResultBuilder data(Object data) {
			this.data = data;
			return this;
		}
		
		public JsonResultBuilder message(String message) {
			this.message = message;
			return this;
		}
		
		public JsonResult build() {
			return new JsonResult(this);
		}
	}
	
	public String getreturn_code() {
		return return_code;
	}

	public String getMessage() {
		return message;
	}

	public Object getData() {
		return data;
	}

	@Override
	public String toString() {
		return "JsonResult [return_code=" + return_code + ", message=" + message + ", data=" + data + "]";
	}

	
	
	

}

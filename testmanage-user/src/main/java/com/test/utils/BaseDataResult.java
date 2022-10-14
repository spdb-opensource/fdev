package com.test.utils;

import java.util.List;

/**
 * @description(//TODO 请添加这个交易的描述)
 * @date 2018年1月18日 上午9:14:10
 * @author huxianjin
 * @version v1.0
 * @since v1.0
 */
public class BaseDataResult {
	
	private Integer code;
	private String msg;
	private Integer count;
	private List data;
	
	public Integer getCode() {
		return code == null ? 0 : code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMsg() {
		return msg == null ? "" : msg.trim();
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Integer getCount() {
		return count == null ? 0 : count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List getData() {
		return data;
	}
	public void setData(List data) {
		this.data = data;
	}

}

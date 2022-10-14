package com.fdev.docmanage.entity;

public class AuthParameter {
	/**
	 * 应用id
	 * */
	private String clientId;
	/**
	 * 返回类型 此处固定为code
	 * */
	private String responseType;
	/**
	 * 不少于8个字符。用于保持请求和回调的状态，授权服务器在回调时（重定向用户浏览器到“redirect_uri”时），
	 * 会在Query Parameter中原样回传该参数
	 * */
	private String state;
	/**
	 * 授权后要回调的url
	 * */
	private String redirectUri;
	/**
	 * 以空格分隔的权限
	 * */
	private String scope;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getResponseType() {
		return responseType;
	}
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getRedirectUri() {
		return redirectUri;
	}
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	
}

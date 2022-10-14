package com.fdev.docmanage.entity;

public class HeaderParameter {
    /**
     * Basic base64(AppID:AppSecret)
     */
    private String authorization;

    /**
     * Content-type
     */
    private String contentType;

    /**
     * Cookie
     */
    private String cookie;

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

	@Override
	public String toString() {
		return "HeaderParameter [authorization=" + authorization + ", contentType=" + contentType + ", cookie=" + cookie
				+ "]";
	}

}

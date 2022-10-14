package com.fdev.docmanage.entity;


public class TokenParameter {

    private String grantType;

    private String code;

    private String redirectUri;

    private String scope;

    private String refreshToken;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

	@Override
	public String toString() {
		return "TokenParameter [grantType=" + grantType + ", code=" + code + ", redirectUri=" + redirectUri + ", scope="
				+ scope + ", refreshToken=" + refreshToken + "]";
	}
    
}

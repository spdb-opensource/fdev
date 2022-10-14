package com.fdev.docmanage.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class Token {

    @JSONField(name="access_token")
    private String accessToken;

    @JSONField(name="expires_in")
    private Long expiresIn;

    @JSONField(name="scope")
    private String scope;

    @JSONField(name="token_type")
    private String tokenType;

    @JSONField(name="refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

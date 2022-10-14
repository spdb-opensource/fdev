package com.fdev.docmanage.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class Constants {
    public static int notificationTimes = 0;

    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8";
    
    public static final String CONTENT_TYPE_FORM1 = "multipart/form-data; charset=UTF-8";

    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    @Value("${app.id}")
    private String appId;

    @Value("${app.secret}")
    private String appSecret;

    @Value("${app.graph.prefix}")
    private String graphPrefix;

    @Value("${app.authorization}")
    private String authorization;

    @Value("${app.redirectUri}") 
    private String redirectUri;

    @Value("${app.name}")
    private String appName;

    @Value("${app.scope}")
    private String scope;

    //@Value("${app.webhook.prefix}")
    private String webhookPrefix;

    //@Value("${app.wps.sid}")
    private String wpssid;

    private final String responseType = "code";
    
    @Value("${app.volume}")
    private String volume;
    public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getGraphPrefix() {
        return graphPrefix;
    }

    public void setGraphPrefix(String graphPrefix) {
        this.graphPrefix = graphPrefix;
    }

    public String getWebhookPrefix() {
        return webhookPrefix;
    }

    public void setWebhookPrefix(String webhookPrefix) {
        this.webhookPrefix = webhookPrefix;
    }

    public String getWpssid() {
        return wpssid;
    }

    public void setWpssid(String wpssid) {
        this.wpssid = wpssid;
    }
}

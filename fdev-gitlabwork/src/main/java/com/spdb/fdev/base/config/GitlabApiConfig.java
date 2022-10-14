package com.spdb.fdev.base.config;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitlabApiConfig implements InitializingBean {

    @Value("${gitlab.url}")
    private String url;

    @Value("${gitlab.token}")
    private String token;

    private GitLabApi gitLabApi;

    @Override
    public void afterPropertiesSet() {
        gitLabApi = new GitLabApi(url, token);
    }

    public GitLabApi getGitLabApi() {
        return gitLabApi;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public String getApiUrl() {
        return url + "api/v4/";
    }
}

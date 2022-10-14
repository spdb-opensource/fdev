package com.spdb.fdev.fdevapp.spdb.entity;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RefreshScope
@Configuration
public class AuthorityManagers {

    @Value("#{'${auth-Manager}'.split(',')}")
    private List<String> authManagers;//应用实体操作权限
    @Value("#{'${env-Manager}'.split(',')}")
    private List<String> envManagers;//环境配置管理员权限
    @Value("${userStuckPoint.RoleId}")
    private List<String> stuckPointRoleId;//卡点管理员权限

    public List<String> getStuckPointRoleId() {
        return stuckPointRoleId;
    }

    public List<String> getEnvManagers() {
        return envManagers;
    }

    public List<String> getAuthManagers() {
        return authManagers;
    }

}
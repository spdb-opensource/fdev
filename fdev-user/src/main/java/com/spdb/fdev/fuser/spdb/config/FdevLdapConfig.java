package com.spdb.fdev.fuser.spdb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@RefreshScope
public class FdevLdapConfig {
    @Value("${spring.ldap.urls}")
    private String url;
    @Value("${spring.ldap.base}")
    private String base;
    @Value("${spring.ldap.username}")
    private String dn;
    @Value("${spring.ldap.password}")
    private String password;

    @Bean(name="fdevldap")
    public LdapTemplate getLdapTemplate() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(url);
        ldapContextSource.setBase(base);
        ldapContextSource.setUserDn(dn);
        ldapContextSource.setPassword(password);
        ldapContextSource.setCacheEnvironmentProperties(false);
        ldapContextSource.afterPropertiesSet();
        return new LdapTemplate(ldapContextSource);
    }
}

package com.spdb.fdev.release.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class ManufactureLdapConfig {

    @Value("${manufacture.spring.ldap.urls}")
    private String url;
    @Value("${manufacture.spring.ldap.base}")
    private String base;
    @Value("${manufacture.spring.ldap.dn}")
    private String dn;
    @Value("${manufacture.spring.ldap.password}")
    private String password;

    @Bean(name="manufactureldap")
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
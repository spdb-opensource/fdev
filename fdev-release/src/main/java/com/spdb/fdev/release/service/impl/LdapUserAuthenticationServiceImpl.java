package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.config.LdapConfig;
import com.spdb.fdev.release.config.ManufactureLdapConfig;
import com.spdb.fdev.release.service.LdapUserAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

@Service
public class LdapUserAuthenticationServiceImpl implements LdapUserAuthenticationService {

    protected Log logger = LogFactory.getLog(getClass());

    @Autowired
    private LdapConfig uiasldap;
    @Autowired
    private ManufactureLdapConfig manufactureldap;

    public boolean login(String userNameEn, String passWd){
        boolean spdbres = authenticate(userNameEn, passWd);// 行内用户ldap认证
        boolean res = authenticateManu(userNameEn, passWd);// 厂商用户ldap认证
        if (!res && !spdbres) {
            //行内和厂商的ldap认证均未通过
            logger.error("ldap authenticate fail");
            return false;
        }
        return true;
    }

    public boolean authenticate(String username, String password) {
        LdapTemplate uiasLdapTemplate = uiasldap.getLdapTemplate();
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mailNickname", username));
        return uiasLdapTemplate.authenticate("", filter.encode(), password);
    }

    public boolean authenticateManu(String username, String password) {
        LdapTemplate manufactureLdapTemplate = manufactureldap.getLdapTemplate();
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mailNickname", username));
        return manufactureLdapTemplate.authenticate("", filter.encode(), password);
    }
}

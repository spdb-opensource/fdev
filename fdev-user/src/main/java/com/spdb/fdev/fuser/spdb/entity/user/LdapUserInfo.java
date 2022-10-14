package com.spdb.fdev.fuser.spdb.entity.user;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.stereotype.Component;

import javax.naming.Name;

/**
 * @Author liux81
 * @DATE 2021/9/15
 */
@Entry(objectClasses = {"top","person","organizationalPerson","user"})
@Component
public final class LdapUserInfo {
    @Id
    private Name dn;

    @Attribute(name = "mailNickname")
    private String mailNickname;

    @Attribute(name = "mail")
    private String mail;

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getMailNickname() {
        return mailNickname;
    }

    public void setMailNickname(String mailNickname) {
        this.mailNickname = mailNickname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}

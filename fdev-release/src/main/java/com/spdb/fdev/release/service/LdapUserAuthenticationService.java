package com.spdb.fdev.release.service;


public interface LdapUserAuthenticationService {

    public boolean login(String userNameEn, String passWd);

}

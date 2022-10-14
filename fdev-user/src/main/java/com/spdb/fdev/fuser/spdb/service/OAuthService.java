package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.OAuth;

import java.util.List;

public interface OAuthService {
    public List<OAuth> query(OAuth oAuth) throws Exception;
    public OAuth add(OAuth oAuth) throws Exception;
}

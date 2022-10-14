package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.OAuth;

import java.util.List;

public interface OAuthDao {
    public List<OAuth> query(OAuth oAuth) throws  Exception;
    public OAuth add(OAuth oAuth) throws  Exception;
}

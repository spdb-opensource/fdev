package com.spdb.fdev.fuser.spdb.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.fuser.spdb.dao.OAuthDao;
import com.spdb.fdev.fuser.spdb.entity.user.OAuth;
import com.spdb.fdev.fuser.spdb.service.OAuthService;

@Service
public class OAuthServiceImpl implements OAuthService {
    @Autowired
    private OAuthDao oAuthDao;
    @Override
    public List<OAuth> query(OAuth oAuth) throws Exception {
        return oAuthDao.query(oAuth);
    }

    @Override
    public OAuth add(OAuth oAuth) throws Exception {
        return oAuthDao.add(oAuth);
    }
}

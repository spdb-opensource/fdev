package com.spdb.fdev.gitlabwork.service.impl;


import com.spdb.fdev.gitlabwork.dao.CastDao;
import com.spdb.fdev.gitlabwork.entiy.SitMergedInfo;
import com.spdb.fdev.gitlabwork.service.CastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CastServiceImpl implements CastService {

    @Autowired
    private CastDao castDao;

    @Override
    public void save(SitMergedInfo obj) {
        castDao.save(obj);
    }

    @Override
    public List<SitMergedInfo> query(Map query) {
        return castDao.query(query);
    }


}

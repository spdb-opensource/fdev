package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.spdb.dao.IlabelServiceDao;
import com.spdb.fdev.fdevenvconfig.spdb.service.IlabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IlabelServiceImpl implements IlabelService {

    @Autowired
    private IlabelServiceDao ilabelServiceDao;
    public static final Logger LOGGER = LoggerFactory.getLogger(IlabelServiceImpl.class);


    @Override
    public Map queryAllLabels() {
        return ilabelServiceDao.queryAllLabels();
    }
}

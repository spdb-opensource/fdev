package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.IErrorsDao;
import com.spdb.fdev.release.entity.ErrorCollection;
import com.spdb.fdev.release.service.IErrorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ErrorsServiceImpl implements IErrorsService {

    @Autowired
    IErrorsDao errorsDao;

    @Override
    public void save(ErrorCollection errorCollection) {
        errorsDao.save(errorCollection);
    }
}

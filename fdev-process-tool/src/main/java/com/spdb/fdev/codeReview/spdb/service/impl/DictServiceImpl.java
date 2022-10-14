package com.spdb.fdev.codeReview.spdb.service.impl;

import com.spdb.fdev.codeReview.spdb.dao.IDictDao;
import com.spdb.fdev.codeReview.spdb.entity.DictEntity;
import com.spdb.fdev.codeReview.spdb.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Service
public class DictServiceImpl implements IDictService {

    @Autowired
    IDictDao dictDao;

    @Override
    public void add(DictEntity dictEntity) {
        dictDao.add(dictEntity);
    }

    @Override
    public List<DictEntity> query(Map param) {
        return dictDao.query(param);
    }
}

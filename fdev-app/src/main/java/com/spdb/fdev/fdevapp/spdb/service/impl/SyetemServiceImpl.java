package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.ISystemDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;
import com.spdb.fdev.fdevapp.spdb.service.ISystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SyetemServiceImpl implements ISystemService {


    @Autowired
    private ISystemDao systemDao;

    @Override
    public List<AppSystem> getSystem(AppSystem param) {
        return this.systemDao.findSystemByParam(param);
    }

    @Override
    public List<AppSystem> querySystemByIds(Map<String, Object> requestMap) {
        List params = (List) requestMap.get(Dict.IDS);
        if ( CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        List<AppSystem> AppSystem = systemDao.querySystemByIds(params);
        return AppSystem;
    }

    @Override
    public AppSystem save(AppSystem systemEntity) {
        return this.systemDao.save(systemEntity);
    }
}

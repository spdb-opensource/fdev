package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.spdb.dao.DictDao;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import com.spdb.fdev.fdemand.spdb.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictDao dictDao;

    @Override
    public List<DictEntity> query(DictEntity dict) throws Exception {
        return dictDao.query(dict);
    }

    @Override
    public DictEntity queryByCode(String code) throws Exception {
        return dictDao.queryByCode(code);
    }

    @Override
    public Map<String, String> getCodeNameMap(List<DictEntity> dicts) throws Exception {
        return dicts.stream().collect(Collectors.toMap(DictEntity::getCode, DictEntity::getValue));
    }

    @Override
    public Map<String, String> getCodeNameMapByTypes(Set<String> types) throws Exception {
        return getCodeNameMap(queryByTypes(types));
    }

    @Override
    public List<DictEntity> queryByTypes(Set<String> types) throws Exception {
        return dictDao.queryByTypes(types);
    }

}

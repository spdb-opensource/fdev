package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;

import java.util.List;

public interface ISystemDao {

    AppSystem save(AppSystem systemEntity);

    List<AppSystem> findSystemByParam(AppSystem param);

    List<AppSystem> querySystemByIds(List params);
}

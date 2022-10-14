package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.AppType;

import java.util.List;

public interface IAppTypeDao {

    public AppType save(AppType appType)  throws Exception;

    public List<AppType> query(AppType appType) throws Exception;

    public AppType update(AppType appType) throws Exception;

    public AppType findById(String id) throws Exception;
}

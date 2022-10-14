package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppModel;

import java.util.List;

public interface IAppModelDao {
    List<AppModel> query(AppModel appModel) throws Exception;

    AppModel add(AppModel appModel);
}

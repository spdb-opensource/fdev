package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppModel;

import java.util.List;

public interface IAppModelService {
    List<AppModel> query(AppModel appModel) throws Exception;

    AppModel add(AppModel appModel);
}

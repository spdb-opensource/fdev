package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;

import java.util.List;
import java.util.Map;

public interface ISystemService {

    AppSystem save(AppSystem systemEntity);

    List<AppSystem> getSystem(AppSystem param);

    List<AppSystem> querySystemByIds(Map<String, Object> requestMap);
}

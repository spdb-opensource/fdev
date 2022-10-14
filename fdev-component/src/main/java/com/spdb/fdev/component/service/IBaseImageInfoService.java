package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.BaseImageInfo;

import java.util.List;
import java.util.Map;

public interface IBaseImageInfoService {
    List query(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo save(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo update(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo queryById(String id);

    BaseImageInfo queryByName(String name);

    Map queryMetaData(String name);

    void relDevops(Map<String, String> map) throws Exception;
}

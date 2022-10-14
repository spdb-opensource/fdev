package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.BaseImageInfo;

import java.util.List;

public interface IBaseImageInfoDao {
    List query(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo save(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo update(BaseImageInfo baseImageInfo) throws Exception;

    BaseImageInfo queryById(String id);

    BaseImageInfo queryByName(String name);

    BaseImageInfo queryByGitlabUrl(String gitlabUrl);

    Integer queryDataByType(String start_date,String end_date);

    BaseImageInfo queryByGitlabId(String gitlabId);

    List<BaseImageInfo> getBaseImageByIds(List params) throws Exception;
}

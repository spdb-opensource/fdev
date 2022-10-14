package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ComponentInfo;

import java.util.List;
import java.util.Map;


public interface IComponentInfoDao {

    List query(ComponentInfo componentInfo) throws Exception;

    ComponentInfo save(ComponentInfo componentInfo) throws Exception;

    void delete(ComponentInfo componentInfo) throws Exception;

    ComponentInfo queryById(ComponentInfo componentInfo) throws Exception;

    ComponentInfo queryById(String id) throws Exception;

    ComponentInfo queryByNameEn(String name_en) throws Exception;

    ComponentInfo queryByGroupIdAndArtifact(String groupId,String artifactId);

    ComponentInfo queryByWebUrl(String web_url);

    List<ComponentInfo> queryByType(String type) throws Exception;

    ComponentInfo update(ComponentInfo componentInfo) throws Exception;

    List<ComponentInfo> queryByUserId(String user_id);

    Integer queryDataByType(String startTime,String endTime);

    List<ComponentInfo> getComponentByIds(List params) throws Exception;
}

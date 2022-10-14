package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.YapiInterface;
import com.spdb.fdev.fdevinterface.spdb.entity.YapiProject;

import java.util.List;
import java.util.Map;

public interface YapiDataExportDao {
    //查看是否存在此项目
    YapiProject findOnlyProjectList(String projectID);

    //向数据库添加项目信息
    void addProject(YapiProject yapiProject);

    //添加接口
    void addInterface(List<YapiInterface> yapiInterfaceList);

    //更新数据库项目信息
    void updateProject(YapiProject yapiProject);

    //跟新接口列表
    void updateInterfaceList(List<YapiInterface> yapiInterfaceList, String projectID);

    //更新接口信息
    void updateInterface(YapiInterface yapiInterface);

    //查询项目列表返回 文档id 项目id 项目名称  录入人  接口列表为null
    Map<String, Object> findProjectList(Map request);

    //查询项目列表返回 文档id 项目id 项目名称  录入人  接口列表不为null
    Map<String, Object> yapiProjectListWithInterfaces(Map request);

    //分页修改
    List<Object> getYapiInterfaceList(String projectID, String yapi_token);

    //删除项目应用
    void deleteProjet(Map request);

    //删除单个接口
    void deleteInterface(Map request);
}

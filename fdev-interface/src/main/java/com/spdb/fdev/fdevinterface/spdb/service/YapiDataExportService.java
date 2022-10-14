package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.Map;

public interface YapiDataExportService {
    //添加项目基本信息
    String addProjectInfor(Map request);

    //添加接口
    void importInterface(Map request);

    //查询项目列表返回 文档id 项目id 项目名称  录入人  接口列表为null
    Map<String, Object> yapiProjectList(Map request);

    //jsonSchemaDrfat03到jsonSchemaDrfat04的转换
    String convertJsonSchema(Map request);

    //查询项目列表返回 文档id 项目id 项目名称  录入人  接口列表不为null
    Map<String, Object> yapiProjectListWithInterfaces(Map request);

    //删除项目应用
    void deleteProject(Map request);

    //删除单个接口
    void deleteInterface(Map request);
}

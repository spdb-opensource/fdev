package com.fdev.database.spdb.service;


public interface ScannParseService {

    /**
     * 扫描xml获取应用和库表的关联信息
     * @param dataType
     * @param dataName
     */
    void ScannAndGetxml(String dataType, String dataName);
}

package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.List;
import java.util.Map;

public interface IDeployFileService {

    /**
     * 部署文件依赖搜索
     *
     * @param modelNameEn
     * @param fieldNameEn
     * @return
     */
    List<Map> queryDeployDependency(String modelNameEn, String fieldNameEn) throws Exception;

}

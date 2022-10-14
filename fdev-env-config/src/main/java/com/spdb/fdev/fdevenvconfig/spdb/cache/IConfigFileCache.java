package com.spdb.fdev.fdevenvconfig.spdb.cache;

import java.util.List;
import java.util.Map;

public interface IConfigFileCache {
    Map<String, Map> getUsersByIdsOrUserNameEn(String key, Map<String, List<String>> param) throws Exception;

    List<Map> preQueryConfigDependency(Map<String, Object> requestParam) throws Exception;

}

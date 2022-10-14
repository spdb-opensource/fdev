package com.fdev.database.spdb.service;



import com.fdev.database.spdb.entity.App;

import java.util.List;
import java.util.Map;

public interface AppService {


    List<Map<String, Object>> getAppList(String gitlabid) throws Exception;

    List<App> query(App app) throws Exception;

    List<Map> queryMyApps(String id) throws Exception;

    List<App> queryAppByUser(String user_name_en);

    void updateManager(String appId, List<Map> spdb_managers);
}

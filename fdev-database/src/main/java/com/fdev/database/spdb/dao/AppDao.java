package com.fdev.database.spdb.dao;


import com.fdev.database.spdb.entity.App;

import java.util.List;
import java.util.Map;

public interface AppDao {

    void save(App app);

    /**
     * 查询应用信息（不支持负责人查询）
     * @param app
     * @return
     * @throws Exception
     */
    List<App> queryAppName(App app) throws  Exception;

    /**
     * 根据用户英文名查询应用
     * @param user_name_en
     * @return
     */
    List<App> queryAppByUser(String user_name_en);

    void updateManager(String appId, List<Map> spdb_managers);
}

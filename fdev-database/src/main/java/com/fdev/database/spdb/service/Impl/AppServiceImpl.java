package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.util.CommonUtils;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AppServiceImpl implements AppService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;

    @Resource
    private AppDao appDao;

    @Override
    public List<Map<String, Object>> getAppList(String projectId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(projectId))
        send_map.put(Dict.GITLAB_PROJECT_ID, projectId);
        send_map.put(Dict.REST_CODE, Dict.QUERYAPPS);
        return (ArrayList) restTransport.submit(send_map);
    }

    @Override
    public List<App> query(App app) throws Exception {
        return appDao.queryAppName(app);
    }

    @Override
    public List<Map> queryMyApps(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.USER_ID, id);
        send_map.put(Dict.REST_CODE,Dict.QUERYMYAPPS);
        return (ArrayList) restTransport.submit(send_map);
    }

    @Override
    public List<App> queryAppByUser(String user_name_en) {
        return appDao.queryAppByUser(user_name_en);
    }

    @Override
    public void updateManager(String appId, List<Map> spdb_managers) {
        appDao.updateManager(appId, spdb_managers);
    }

}

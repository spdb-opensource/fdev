package com.spdb.fdev.fdevtask.spdb.service;

import java.util.List;
import java.util.Map;

public interface ServiceApi {

    public void addProjectMember(Map params) throws Exception;

    public Map queryApp(String id);

    public List<Map> queryProjectMember(String id);

    public Map queryVersionDetail(String id);

    void updateByTask(Map<String, Object> params) ;
}

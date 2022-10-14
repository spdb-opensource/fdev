package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.RoutesDao;
import com.spdb.fdev.fdevinterface.spdb.dao.util.QueryUtil;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesRelation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RoutesDaoImpl implements RoutesDao {

    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    public void saveRoutesApi(List<RoutesApi> routesApiList) {
        mongoTemplate.insert(routesApiList, RoutesApi.class);
    }

    @Override
    public void saveRoutesRelation(List<RoutesRelation> routesRelationList) {
        mongoTemplate.insert(routesRelationList, RoutesRelation.class);
    }

    @Override
    public Map queryRoutesApi(Map params) {
        Criteria criteria = QueryUtil.getRoutesCriteria(params);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, RoutesApi.class);
        Integer page = (Integer) params.get("page");
        Integer pageNum = (Integer) params.get("pageNum");
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<RoutesApi> list = mongoTemplate.find(query, RoutesApi.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.TOTAL, total);
        map.put(Dict.LIST, list);
        return map;
    }

    @Override
    public Map queryRoutesRelation(Map params) {
        Criteria criteria = QueryUtil.getRoutesRelationCriteria(params);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, RoutesRelation.class);
        Integer page = (Integer) params.get("page");
        Integer pageNum = (Integer) params.get("pageNum");
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<RoutesRelation> list = mongoTemplate.find(query, RoutesRelation.class);
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.TOTAL, total);
        map.put(Dict.LIST, list);
        return map;
    }

    /**
     * 获取路由模型
     *
     * @param appName
     * @param branch
     * @return
     */
    @Override
    public List<RoutesRelation> queryRoutes(String appName, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SOURCE_PROJECT).is(appName);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, RoutesRelation.class);
    }

    @Override
    public void deleteRoutes(String appName, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(appName);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, RoutesApi.class);
    }

    @Override
    public void deleteRoutesRelation(String appName, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.SOURCE_PROJECT).is(appName);
        criteria.and(Dict.BRANCH).is(branch);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, RoutesRelation.class);
    }

    @Override
    public List<RoutesApi> queryRoutesApiList(String appServiceId, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(appServiceId);
        criteria.and(Dict.BRANCH).is(branchName);
        criteria.and(Dict.IS_NEW).is(1);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, RoutesApi.class);
    }

    @Override
    public void updateIsNew(String projectName, String branchName) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(projectName);
        criteria.and(Dict.BRANCH).is(branchName);
        criteria.and(Dict.IS_NEW).is(1);
        Update update = Update.update(Dict.IS_NEW, 0);
        mongoTemplate.updateMulti(new Query(criteria), update, RoutesApi.class);
    }

    @Override
    public void deleteRoutesApi(String projectName, String branchName, int ver) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(projectName);
        criteria.and(Dict.BRANCH).is(branchName);
        if (ver != -9) {
            criteria.and(Dict.VER).lte(ver);
        }
        mongoTemplate.remove(new Query(criteria), RoutesApi.class);
    }

    @Override
    public RoutesApi queryRoutesDetail(String name, String targetProject, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAME).is(name);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.PROJECT_NAME).is(targetProject);
        criteria.and(Dict.IS_NEW).is(1);
        return mongoTemplate.findOne(new Query(criteria), RoutesApi.class);
    }

    @Override
    public RoutesApi queryRoutesById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.L_ID).is(id);
        return mongoTemplate.findOne(new Query(criteria), RoutesApi.class);
    }

    @Override
    public List<RoutesApi> queryRoutesDetailVer(String name, String projectName, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAME).is(name);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.PROJECT_NAME).is(projectName);
        return mongoTemplate.find(new Query(criteria), RoutesApi.class);
    }

    @Override
    public void updateScanType(String appNameEn, String branch, Integer ver) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(appNameEn);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.IS_NEW).is(1);
        mongoTemplate.updateMulti(new Query(criteria), Update.update(Dict.TYPE, Constants.AUTO_SCAN), RoutesApi.class);
    }
}

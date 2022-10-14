package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.AppJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
@Repository
public class AppJsonDaoImpl implements AppJsonDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(AppJson appJson) {
        mongoTemplate.save(appJson);
    }

    @Override
    public Map<String, Object> getAppJsonList(Map<String, Object> requestMap) {
        String projectName = (String) requestMap.get(Dict.PROJECT_NAME);
        String branch = (String) requestMap.get(Dict.BRANCH);
        String name = (String) requestMap.get(Dict.NAME);
        Integer page = (Integer) requestMap.get(Dict.PAGE);
        Integer pageNum = (Integer) requestMap.get(Dict.PAGE_NUM);
        boolean ishistory =requestMap.keySet().contains(Dict.IS_HISTORY)? (boolean)requestMap.get(Dict.IS_HISTORY) : false;
        Integer isNew = ishistory? 0 : 1 ;
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(projectName)) {
            Pattern pattern = Pattern.compile("^.*" + projectName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.PROJECT_NAME).regex(pattern);
        }
        if (StringUtils.isNotEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        }
        if (StringUtils.isNotEmpty(name)) {
            Pattern pattern = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.PROJECT_JSON + "." + Dict.ROUTES + "." + Dict.NAME).regex(pattern);
        }
        criteria.and(Dict.IS_NEW).is(isNew);
        Query query = new Query(criteria);
        if (requestMap.keySet().contains(Dict.PAGE_NUM) && requestMap.keySet().contains(Dict.PAGE)){
            query.skip((page - 1L) * pageNum).limit(pageNum);
        }
        Long total = mongoTemplate.count(query, AppJson.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
        List<AppJson> list = mongoTemplate.find(query,AppJson.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public List<AppJson> deleteAppJson(String projectName, String branch, Integer routesVersion) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(projectName);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.ROUTES_VERSION).lte(routesVersion);
        Query query = new Query(criteria);
        List<AppJson> appJsonList = mongoTemplate.find(query, AppJson.class);
        mongoTemplate.remove(query, RoutesApi.class);
        return appJsonList;
    }

    @Override
    public List<AppJson> getAppJsonLast(Map<String, String> nameAndBranchMap) {
        List<AppJson> appJsonList = new ArrayList<>();
        for (Map.Entry<String, String> entry : nameAndBranchMap.entrySet()) {
            Criteria criteria = new Criteria();
            criteria.and(Dict.PROJECT_NAME).is(entry.getKey()).and(Dict.BRANCH).is(entry.getValue());
            Query query = new Query(criteria);
            query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
            query.skip(0).limit(1);
            AppJson appJson = mongoTemplate.findOne(query, AppJson.class);
            if (appJson != null) {
                appJsonList.add(appJson);
            }
        }
        return appJsonList;
    }

    @Override
    public AppJson getLastBranch(String appNameEn, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).regex(appNameEn);
        Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
        criteria.and(Dict.BRANCH).regex(pattern);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
        return mongoTemplate.findOne(query, AppJson.class);
    }

    @Override
    public void updateIsNew(String projectName , String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(projectName).and(Dict.BRANCH).is(branch).and(Dict.IS_NEW).is(1);
        mongoTemplate.findAndModify(new Query(criteria), Update.update(Dict.IS_NEW, 0), AppJson.class);
    }

    @Override
    public List<AppJson> queryLastAppJson(String branch , String project_name) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(project_name)) {
//            Pattern pattern = Pattern.compile("^.*" + project_name + ".*$", Pattern.CASE_INSENSITIVE);
//            criteria.and(Dict.PROJECT_NAME).regex(pattern);
        	criteria.and(Dict.PROJECT_NAME).is(project_name);
        }
        if (StringUtils.isNotEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, Dict.CICD_TIME));
        return mongoTemplate.find(query, AppJson.class);
    }
}

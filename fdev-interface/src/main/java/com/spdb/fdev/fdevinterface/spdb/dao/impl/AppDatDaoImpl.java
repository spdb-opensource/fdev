package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.AppDatDao;
import com.spdb.fdev.fdevinterface.spdb.entity.AppDat;
import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2020/7/30 16:31
 */
@Repository
public class AppDatDaoImpl implements AppDatDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(AppDat appDat) {
        mongoTemplate.save(appDat);
    }

    @Override
    public Map<String, Object> getAppDatList(Map<String, Object> requestMap) {
        String projectName = (String) requestMap.get(Dict.PROJECT_NAME);
        String branch = (String) requestMap.get(Dict.BRANCH);
        String name = (String) requestMap.get(Dict.NAME);
        Integer page = (Integer) requestMap.get(Dict.PAGE);
        Integer pageNum = (Integer) requestMap.get(Dict.PAGE_NUM);
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
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, AppDat.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<AppDat> list = mongoTemplate.find(query, AppDat.class);
        Map<String, Object> restMap = new HashMap<>();
        restMap.put(Dict.TOTAL, total);
        restMap.put(Dict.LIST, list);
        return restMap;
    }

    @Override
    public List<AppDat> deleteAppDat(String projectName, String branch, Integer routesVersion) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).is(projectName);
        criteria.and(Dict.BRANCH).is(branch);
        criteria.and(Dict.ROUTES_VERSION).lte(routesVersion);
        Query query = new Query(criteria);
        List<AppDat> appDatList = mongoTemplate.find(query, AppDat.class);
        mongoTemplate.remove(query, RoutesApi.class);
        return appDatList;
    }

    @Override
    public List<AppDat> getAppDatTarName(Map<String, String> nameAndBranchMap) {
        List<AppDat> appDatList = new ArrayList<>();
        for (Map.Entry<String, String> entry : nameAndBranchMap.entrySet()) {
            Criteria criteria = new Criteria();
            criteria.and(Dict.PROJECT_NAME).is(entry.getKey()).and(Dict.BRANCH).is(entry.getValue());
            Query query = new Query(criteria);
            query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
            query.skip(0).limit(1);
            AppDat appDat = mongoTemplate.findOne(query, AppDat.class);
            if (appDat != null) {
                appDatList.add(appDat);
            }
        }
        return appDatList;
    }

    @Override
    public AppDat getLastBranch(String appNameEn, String branch) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PROJECT_NAME).regex(appNameEn);
        Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
        criteria.and(Dict.BRANCH).regex(pattern);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
        return mongoTemplate.findOne(query, AppDat.class);
    }

    @Override
    public List<AppDat> queryLastAppDat(String branch, String project_name) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(project_name)) {
            Pattern pattern = Pattern.compile("^.*" + project_name + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.PROJECT_NAME).regex(pattern);
        }
        if (StringUtils.isNotEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CICD_TIME));
        return mongoTemplate.find(query, AppDat.class);
    }

}

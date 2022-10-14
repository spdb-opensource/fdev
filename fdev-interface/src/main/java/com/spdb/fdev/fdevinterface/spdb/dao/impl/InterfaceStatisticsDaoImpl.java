package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceStatisticsDao;
import com.spdb.fdev.fdevinterface.spdb.entity.InterfaceStatistics;

@Repository
public class InterfaceStatisticsDaoImpl implements InterfaceStatisticsDao {
	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public void saveInterfaceList(List<InterfaceStatistics> list) {
		mongoTemplate.insert(list, InterfaceStatistics.class);
	}

	@Override
	public Map queryInterfaceList(Map params) {
		String name = (String)params.get(Dict.NAME);
		String targetServiceName = (String)params.get("targetServiceName");
		String sourceServiceName = (String)params.get("sourceServiceName");
		Integer page = (Integer)params.get("page");
		Integer pageNum = (Integer)params.get("pageNum");
		Criteria criteria = new Criteria();
		if (!StringUtils.isEmpty(name)) {
			Pattern pattern = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.NAME).regex(pattern);
		}
		if (!StringUtils.isEmpty(targetServiceName)) {
			criteria.and(Dict.TARGET_SERVICE_NAME).is(targetServiceName);
		}
		if (!StringUtils.isEmpty(sourceServiceName)) {
			criteria.and(Dict.SOURCE_SERVICE_NAME).is(sourceServiceName);
		}
		Query query = new Query(criteria);
		Long total = mongoTemplate.count(query, InterfaceStatistics.class);
        if (page == null) {
            page = 1;
        }
        if (pageNum == null) {
            pageNum = 10;
        }
        query.skip((page - 1L) * pageNum).limit(pageNum);
        List<InterfaceStatistics> interfaceList = mongoTemplate.find(query, InterfaceStatistics.class);
        Map map = new HashMap();
        map.put(Dict.TOTAL, total);
        map.put(Dict.LIST, interfaceList);
        return map;
	}

	@Override
	public void clearInterfaceList(String projectName) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.TARGET_SERVICE_NAME).is(projectName);
		Query query = new Query(criteria);
		if(mongoTemplate.count(query, InterfaceStatistics.class) != 0){
			mongoTemplate.remove(query,InterfaceStatistics.class);
		}
	}

}

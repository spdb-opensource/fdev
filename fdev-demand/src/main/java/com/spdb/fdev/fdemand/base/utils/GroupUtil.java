package com.spdb.fdev.fdemand.base.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.transport.RestTransport;

import io.micrometer.core.instrument.Statistic;
@Configuration
public class GroupUtil {
	@Autowired
	private RestTransport restTransport;
	
	
	/**
	 * 查詢小組全量信息
	 * @return
	 * @throws Exception 
	 */
	@LazyInitProperty(redisKeyExpression = "groupAll")
	public List<Map<String,String>> queryGroupAll() throws Exception {	
		Map<String, String> map2=new HashMap<String, String>();
		Object res =new Object();
        map2.put(Dict.REST_CODE, "queryGroup");
        return (List<Map<String, String>>)restTransport.submit(map2);
	}
	
	public Set<String> getGroupByParent(List<String> groupids) {
		List<Map<String,String>> groups = null;
		try {
			groups = queryGroupAll();
		} catch (Exception e) {
			System.out.println();
		}
		if (CommonUtils.isNullOrEmpty(groups)) {
			throw new FdevException("查询全量小组错误");
		}
		if (CommonUtils.isNullOrEmpty(groupids)) {
			return null;
		}
		Set<String>result =new HashSet<String>();
		result.addAll(groupids);
		int size =result.size();
		do {
			size=result.size();
			for(Map<String, String> map:groups) {
				if (result.contains(map.get("parent_id"))) {
					result.add(map.get("id"));
					//groups.remove(map);
					
				}
				
			}
		}while(size!=result.size());
		return result;
	}


}

package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.ScanRecordDao;
import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Repository
public class ScanRecordDaoImpl implements ScanRecordDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void save(ScanRecord scanRecord) {
        scanRecord.setScanTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.save(scanRecord, Dict.INTERFACE_SCAN_RECORD);
    }

    @Override
    public Map getScanRecord(String serviceId, String branch, String type,String startTime, String endTime, List<String> serviceIdList, String recentlyScanFlag) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(serviceId)) {
            Pattern pattern = Pattern.compile("^.*" + serviceId + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_ID).regex(pattern);
        }else if ( !Util.isNullOrEmpty(serviceIdList) ) {
            criteria.and(Dict.SERVICE_ID).in(serviceIdList);
        }
        if (!StringUtils.isEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        }
        if (!StringUtils.isEmpty(type)) {
            Pattern pattern = Pattern.compile("^.*" + type + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TYPE).regex(pattern);
        }
        //按时间区间进行筛选
        if (!StringUtils.isEmpty(startTime) || !StringUtils.isEmpty(endTime) ) {
        	if(!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            	criteria.and(Dict.SCAN_TIME).gte(startTime).lte(endTime);
            }else if(!StringUtils.isEmpty(startTime)) {
            	criteria.and(Dict.SCAN_TIME).gte(startTime);
            }else if(!StringUtils.isEmpty(endTime)) {
            	criteria.and(Dict.SCAN_TIME).lte(endTime);
            }
        }
        
        Map map = new HashMap();
        //筛选同应用同分支最近一次
        if("1".equals(recentlyScanFlag)) {
        	//查询出应用id和分支分组的所有最新扫描时间
            Aggregation aggregation = Aggregation.newAggregation(
            	Aggregation.match(criteria),
          		Aggregation.group(Dict.SERVICE_ID,Dict.BRANCH).max(Dict.SCAN_TIME).as(Dict.SCAN_TIME),
          		Aggregation.sort(new Sort(Sort.Direction.DESC, Dict.SCAN_TIME))
            );
          	List<ScanRecord> scanRecordGroupList = mongoTemplate.aggregate(aggregation, Dict.INTERFACE_SCAN_RECORD, ScanRecord.class).getMappedResults();
          	map.put(Dict.GROUPLIST, scanRecordGroupList);
        }
    	Query query = new Query(criteria);
    	query.with(new Sort(Sort.Direction.DESC, Dict.SCAN_TIME));
        List<ScanRecord> scanRecordList = mongoTemplate.find(query, ScanRecord.class);
        map.put(Dict.LIST, scanRecordList);
        return map;
    }
    
    /**
     * 定时清理超过一个月的扫描记录
     */
	@Override
	public int clearScanRecord(){
		
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_DATE_TIME);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		String date = sdf.format(calendar.getTime());
		
		//查询出应用id和分支分组的所有最新扫描时间
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.group(Dict.SERVICE_ID,Dict.BRANCH).max(Dict.SCAN_TIME).as(Dict.SCAN_TIME)
        );
        List<Map> mappedResults = mongoTemplate.aggregate(aggregation, Dict.INTERFACE_SCAN_RECORD, Map.class).getMappedResults();
        List<Criteria> criterias = new ArrayList<>();
        mappedResults.stream().forEach(s -> {
        	String latestTime = (String)s.get(Dict.SCAN_TIME);
        	String serviceId = (String)s.get(Dict.SERVICE_ID);
        	String branch = (String)s.get(Dict.BRANCH);
            Criteria criteria1 = new Criteria();
    		criteria1.and(Dict.SERVICE_ID).is(serviceId).and(Dict.BRANCH).is(branch);
    		int flag = compareDateTimeString(date,latestTime);
        	if (flag == 1) {//一个月内更新过
        		criteria1.and(Dict.SCAN_TIME).lt(date);
			}else if (flag == -1){//一个月内无扫描记录，则保留该应用该分支的最新一条数据，删除其余数据
				criteria1.and(Dict.SCAN_TIME).lt(latestTime);
			}else {
				return;
			}
        	criterias.add(criteria1);
        });
    	Query query = new Query().addCriteria(
    			new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
		long count = mongoTemplate.count(query, Dict.INTERFACE_SCAN_RECORD);
		if (count > 0) {
			mongoTemplate.remove(query, Dict.INTERFACE_SCAN_RECORD);
		}
		
		return (int)count;
	}
	
	
	private int compareDateTimeString(String dateString1, String dateString2) {
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_DATE_TIME);
		try {
			Date date1 = sdf.parse(dateString1);
			Date date2 = sdf.parse(dateString2);
			if (date1.before(date2)) {
				return 1;
			}else{
				return -1;
			}
		} catch (ParseException e) {
		}
		return 0;
	}

	@Override
	public int timingClearScanRecord() {
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_DATE_TIME);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		String date = sdf.format(calendar.getTime());
		Criteria criteria = new Criteria();
        //筛选6个月前的数据
        criteria.and(Dict.SCAN_TIME).lte(date);
        Query query = new Query(criteria);
        long count = mongoTemplate.count(query, Dict.INTERFACE_SCAN_RECORD);
		if (count > 0) {
			mongoTemplate.remove(query, Dict.INTERFACE_SCAN_RECORD);
		}
		return (int)count;
	}

}

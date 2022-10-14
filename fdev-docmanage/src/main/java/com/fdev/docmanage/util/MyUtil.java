package com.fdev.docmanage.util;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fdev.docmanage.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;

public class MyUtil {
	
	 /**
     * 数据处理
     *
     * @param
     * @return
     */
    public static JSONObject dataProcessing(String data) {
    	
		JSONObject jsonObject = JSONArray.parseObject(data);
    	if(data.contains("code")) {
    		if("conflict".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.CONFLICT_ERROR);
    		}else if("bad_request".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.BAD_REQUEST_ERROR);
    		}else if("unauthenticated".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.UNAUTHENTICATED_ERROR);
    		}else if("forbidden".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.FORBIDDEN_ERROR);
    		}else if("too_many_requests".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.TOO_MANY_REQUESTS_ERROR);
    		}else if("not_found".equals(jsonObject.get("code"))) {
    			throw new FdevException(ErrorConstants.NOT_FOUND_ERROR);
    		}
    	}
    	return jsonObject;
    }
    
    /**
     * 把 date转换成localDateTime类型
     */
    public static LocalDateTime dateToLocalDateTime(Date date){
        if (date == null){
            return null;
        }
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }
    
    /**
     * 获取 date 与 isoDate 相差后的 date
     *  将date 时间增加 8个小时
     */
    public static Date getIOSDate(Date resDate){
        return getDateAdd(resDate, 8*60*60*1000);
    }

    /**
     * date类型 增加 时间
     */
    public static Date getDateAdd(Date resDate, long time){
        if(resDate == null)
            return null;
        return new Date(resDate.getTime() + time);
    }
}

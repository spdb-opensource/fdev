package com.plan.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.text.ParseException;

/*import com.alibaba.fastjson.JSONObject;*/

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


public class Utils {
	public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String INPUT_DATE = "yyyy/MM/dd";

    /**
     * 将Map转换为对象
     */
   /* public static <T> T parseMap2Object(Map<String, Object> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }*/

    //获取当前时间的年月日 2014-01-01
    public static String dateUtil(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
    public static Date parseDate(String str, String date) throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat(date);
        return sf.parse(str);
    }
    
    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }
    
    @SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
		if (obj == null)
			return true;
		else if (obj instanceof CharSequence)
			return ((CharSequence) obj).length() == 0;
		else if (obj instanceof Collection)
			return ((Collection) obj).isEmpty();
		else if (obj instanceof Map)
			return ((Map) obj).isEmpty();
		else if (obj.getClass().isArray())
			return Array.getLength(obj) == 0;

		return false;
	}
    
    public static Long dateStrToLong(String inputTime){
		if(isEmpty(inputTime)) {
			return null;
		}
        Long outTime = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            outTime = simpleDateFormat.parse(inputTime).getTime()/1000;
        } catch (ParseException e) {
            return outTime;
        }
        return outTime;
    }

    public static Long dateStrToLongModel2(String inputTime){
        if(isEmpty(inputTime)) {
            return null;
        }
        Long outTime = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            outTime = simpleDateFormat.parse(inputTime).getTime()/1000;
        } catch (ParseException e) {
            return outTime;
        }
        return outTime;
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
//        JSONObject json = JSONObject.fromObject(cls);
//        return (Map)json;
    }

}


package com.csii.pe.spdb.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
    public static final String INPUT_DATE = "yyyy/MM/dd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.INPUT_DATE);

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Collection) {
                List objList = (List) obj;
                if (objList.isEmpty()) {
                    return true;
                }
                objList.remove("");
                objList.remove(null);
                if (objList.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                if (objMap.isEmpty()) {
                    return true;
                }
                objMap.remove("");
                objMap.remove(null);
                if (objMap.isEmpty()) {
                    return true;
                }
                return false;
            }
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断实体类中所有的属性是否存在null，存在null返回true
     */
    public static boolean checkObjFieldIsNull(Object obj) throws Exception {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            Object o = f.get(obj);
            if (f.get(obj) == null) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    /**
     * id生成器，引用 ObjectId,转成String
     */
    public static String createId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }

    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    public static String calculationDay(String str, int num) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, num);
        return sf.format(calendar.getTime());
    }

    public static Date parseDate(String str, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(date);
        return sf.parse(str);
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
    }

    public static boolean isThreeDayAgo(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(INPUT_DATE);
        Calendar cal = Calendar.getInstance();
        long time1 = 0;
        long time2 = 0;
        try {
            cal.setTime(sdf.parse(date));
            time1 = cal.getTimeInMillis();
            cal.setTime(new Date());
            time2 = cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        if (between_days > 3) {
            return true;
        }
        return false;
    }


}

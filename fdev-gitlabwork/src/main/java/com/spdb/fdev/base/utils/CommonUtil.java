package com.spdb.fdev.base.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonUtil {

    private CommonUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String INPUT_DATE = "yyyy/MM/dd";
    public static final String DATE_FORMATE = "yyyy/MM/dd";

    public static String dateFormate(Date date, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    /**
     * date 日期,format 格式，将format格式的日期转换成下面
     * 将日期 转换为 yyyy-MM-dd HH:mm:ss
     * @throws ParseException
     */
    public static String formatDate(String date, String format) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date sdfdate  = sf.parse(date);
            sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将日期yyyy/MM/dd HH:mm 转换为 yyyy-MM-dd HH:mm:ss失败" + e.getMessage());
        }
        return null;
    }

    /**
     * 将endTime增加一天
     * @param dateTime
     * @return
     */
    public static String timeAddOneDay(String dateTime){
        try {
            SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            Date sdfdate  = sf.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(sdfdate);
            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            sdfdate = c.getTime();
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将endTime添加一天失败！" + e.getMessage());
        }
        return "";
    }

    /**
     * 处理时间，将gitlab传过来的时间yyyy-MM-dd HH:mm:ss UTC 转换为 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime
     */
    public static String handleTime(String dateTime) {
        if (StringUtils.isEmpty(dateTime))
            return null;
        dateTime = dateTime.replace(" UTC","");
        try {
            SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            Date sdfdate  = sf.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(sdfdate);
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 8);
            sdfdate = c.getTime();
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将日期yyyy-MM-dd HH:mm:ss UTC 转换为 yyyy-MM-dd HH:mm:ss失败" + e.getMessage());
        }
        return dateTime;
    }

    /**
     * 判断输入数据是否是空
     *
     * @param obj 输入字符串
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o == null || o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((obj == null) || (("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
                    return true;
                }
                return false;
            }
            if (obj instanceof List) {
                List objList = (List) obj;
                if (objList == null || objList.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                if (objMap == null || objMap.isEmpty()) {
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

}

package com.spdb.common.util;

import com.spdb.common.dict.Dict;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 时间处理工具类
 * @author: lizz
 * @since: 2019-12-11 10:26
 **/
public class DateUtil {

    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 根据预定格式格式化当前日期
     *
     * @param str
     * @return
     */
    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    /**
     * 根据预定格式以及日期，格式化日期
     *
     * @param str
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String str, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        return sf.parse(date);
    }

    /**
     * 获取当天时间0点
     *
     * @return
     */
    public static Date getTodayStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime();
    }

    /**
     * 获取当天时间23点
     *
     * @return
     */
    public static Date getTodayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }

    public static Map getEsDateScope(long date) throws Exception{
        Map map = new HashMap();
        Date parse = new Date(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);
        calendar.add(Calendar.DATE,-1);
        Date startTime = calendar.getTime();
        calendar.setTime(parse);
        calendar.add(Calendar.DATE,1);
        Date endTime = calendar.getTime();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        map.put(Dict.STARTTIME, sdf2.format(startTime));
        map.put(Dict.ENDTIME, sdf2.format(endTime));
        return map;
    }


    /**
     * 根据日期获取当天时间0点
     *
     * @return
     */
    public static Date getDateStartTime(Date date) {
        LocalDateTime localDateTime = LocalDateTime.of(asLocalDate(date), LocalTime.MIN);
        return asDate(localDateTime);
    }

    /**
     * 获取当天时间23点
     *
     * @return
     */
    public static Date getDateEndTime(Date date) {
        LocalDateTime localDateTime = LocalDateTime.of(asLocalDate(date), LocalTime.MAX);
        return asDate(localDateTime);
    }

    /**
     * LocalDate 转换 Date
     *
     * @param localDate
     * @return
     */
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDateTime 转换 Date
     *
     * @param localDateTime
     * @return
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转换 LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date 转换 LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static boolean isEffectiveDate(long nowTime, long startTime, long endTime) {
        if (nowTime == startTime || nowTime == endTime) {
            return true;
        }
        return nowTime > startTime && nowTime < endTime;
    }

    public static void main(String[] args) {
        System.out.println("StartTime:" + getTodayStartTime().getTime());
        System.out.println("EndTime:" + getTodayEndTime().getTime());
    }
}

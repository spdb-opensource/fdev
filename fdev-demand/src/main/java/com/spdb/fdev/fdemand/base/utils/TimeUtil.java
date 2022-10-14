package com.spdb.fdev.fdemand.base.utils;

import com.spdb.fdev.fdemand.spdb.service.impl.DemandBaseInfoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Util
 * <p>
 * 工具类
 *
 * @blame Android Team
 */
public class TimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(DemandBaseInfoServiceImpl.class);

    private static final String timestamp = "yyyy-MM-dd HH:mm:ss";

    public static final String datastamp = "yyyy-MM-dd";

    public static final String datastamp1 = "yyyyMMdd";

    public static final String datastamp2 = "yyyy/MM/dd";

    public static String formatTodayHs() {
        SimpleDateFormat sf = new SimpleDateFormat(timestamp);
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date date = new Date();
        return sf.format(date);
    }

    public static String formatToday() {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp);
        Date date = new Date();
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sf.format(date);
    }

    public static String formatToday2() {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp2);
        Date date = new Date();
        return sf.format(date);
    }

    public static Date parseDateStr(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp);
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date parse = sf.parse(date);
        return parse;
    }


    /**
     * 日期格式化
     *
     * @param date
     * @return
     */
    public static String getDateStamp(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp);
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sf.format(date);
    }

    /**
     * 根据当前时间生成时间戳
     */
    public static String getTimeStamp(Date date) {
        return new SimpleDateFormat(timestamp).format(date);
    }

    /**
     * 将任务模块yyyy/MM/dd格式时间转换为yyyy-MM-dd
     */
    public static String timeConvert(String date) {
        return date.replaceAll("/", "-");
    }

    /**
     * 往前推todayLast天 datastamp yyyy-MM-dd
     *
     * @return
     */
    public static String todayLastTwoWeek(int todayLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - todayLast);
        return new SimpleDateFormat(datastamp).format(calendar.getTime());
    }

    /**
     * 往前推date天 timestamp yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String yesterday(int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - date);
        return new SimpleDateFormat(timestamp).format(calendar.getTime());
    }

    /**
     * yyyy/MM/dd格式时间转换为yyyy-MM-dd
     *
     * @return
     */
    public static String timeConvertNew(String timeStr) {
        if (!CommonUtils.isNullOrEmpty(timeStr)) {
            Date date = null;
            try {
                if (!(timeStr.indexOf("/") == -1)) {
                    date = new SimpleDateFormat(datastamp2).parse(timeStr);
                } else if (timeStr.length() == 8) {
                    date = new SimpleDateFormat(datastamp1).parse(timeStr);
                } else if (!(timeStr.indexOf("-") == -1)) {
                    date = new SimpleDateFormat(datastamp).parse(timeStr);
                }
            } catch (ParseException e) {
            }
            timeStr = new SimpleDateFormat(datastamp).format(date);
        }
        return timeStr;
    }

    /**
     * 获取两个时间的差
     * date1-date2
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int subtractTime(String str1, String str2) {
        DateFormat df = new SimpleDateFormat(datastamp);
        int diff = 0;
        try {
            Date date1 = df.parse(str1);
            Date date2 = df.parse(str2);
            diff = (int) ((date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            logger.error("——————计算两个时间差值异常——————");
        }
        return diff;
    }
}

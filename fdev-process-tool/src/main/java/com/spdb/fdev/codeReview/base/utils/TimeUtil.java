package com.spdb.fdev.codeReview.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Util
 * <p>
 * 工具类
 *
 * @blame Android Team
 */
public class TimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(TimeUtil.class);

    private static final String timestamp = "yyyy-MM-dd HH:mm:ss";

    public static final String datastamp = "yyyy-MM-dd";

    public static final String datastamp1 = "yyyyMMdd";
    
    public static final String datastamp2 = "yyyy/MM/dd";

    public static final String datastamp3 = "yyyy-MM-dd hh:mm";

    public static String formatTodayHs() {
        SimpleDateFormat sf = new SimpleDateFormat(timestamp);
        Date date = new Date();
        return sf.format(date);
    }

    public static String formatToday() {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp);
        Date date = new Date();
        return sf.format(date);
    }

    /**
     * 日期格式化
     *
     * @param date
     * @return
     */
    public static String getDateStamp(Date date) {
        return new SimpleDateFormat(datastamp).format(date);
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
     * 将yyyy-MM-dd格式时间转换为yyyy/MM/dd
     */
    public static String timeConvert2(String date) {
        return date.replaceAll("-", "/");
    }

    /**
     * 往前推todayLast天
     * @return
     */
    public static String todayLastTwoWeek(int todayLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)- todayLast );
        return new SimpleDateFormat(datastamp).format(calendar.getTime());
    }

    /**
     * 往前推1天
     * @return
     */
    public static String yesterday(int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)- 1 );
        return new SimpleDateFormat(timestamp).format(calendar.getTime());
    }
    /**
     * yyyy/MM/dd格式时间转换为yyyy-MM-dd
     * @return
     */
    public static String timeConvertNew(String timeStr) {
    	if(!CommonUtils.isNullOrEmpty(timeStr)) {
    		Date date = null;
    		try {
	    		if(!(timeStr.indexOf("/") == -1)) {
					 date = new SimpleDateFormat(datastamp2).parse(timeStr);
	    		}else if (timeStr.length() == 8 ) {
	    			 date = new SimpleDateFormat(datastamp1).parse(timeStr);
	    		}else if(!(timeStr.indexOf("-") == -1)) {
	    			 date = new SimpleDateFormat(datastamp).parse(timeStr);
	    		}
    		} catch (ParseException e) {
			}
    		timeStr = new SimpleDateFormat(datastamp).format(date);
    	}
    	return timeStr ;
    }

    /**
     * 获取两个时间的差
     * date1-date2
     * @param str1
     * @param str2
     * @return
     */
    public static int subtractTime(String str1, String str2){
        DateFormat df = new SimpleDateFormat(datastamp);
        int diff = 0;
        try {
            Date date1 = df.parse(str1);
            Date date2 = df.parse(str2);
            diff = (int) ((date1.getTime() - date2.getTime())/(1000*60*60*24));
        } catch (ParseException e) {
            logger.error("——————计算两个时间差值异常——————");
        }
        return diff;
    }

    /**
     * date1在date2之后，则返回true，否则false
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareTime(String date1, String date2){
        SimpleDateFormat format = new SimpleDateFormat(datastamp2);
        try {
            Date parse1 = format.parse(date1);
            Date parse2 = format.parse(date2);
            if(parse1.compareTo(parse2) > 0){
                return true;
            }
        } catch (ParseException e) {
            logger.error("——————比较时间异常——————");
        }
        return false;
    }

    /**
     * date1在date2之后，则返回true，否则false
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareTime2(String date1, String date2){
        SimpleDateFormat format = new SimpleDateFormat(timestamp);
        try {
            Date parse1 = format.parse(date1);
            Date parse2 = format.parse(date2);
            if(parse1.compareTo(parse2) > 0){
                return true;
            }
        } catch (ParseException e) {
            logger.error("——————比较时间异常——————");
        }
        return false;
    }

    /**
     * date1在date2之后，则返回true，否则false
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareTime3(String date1, String date2){
        SimpleDateFormat format = new SimpleDateFormat(datastamp3);
        try {
            Date parse1 = format.parse(date1);
            Date parse2 = format.parse(date2);
            if(parse1.compareTo(parse2) > 0){
                return true;
            }
        } catch (ParseException e) {
            logger.error("——————比较时间异常——————");
        }
        return false;
    }

    /**
     * 将yyyy/MM/dd转换为yyyy-MM-dd
     * @param str1
     * @return
     */
    public static String formatConversion(String str1){
        return str1.replace("/","-");
    }
}

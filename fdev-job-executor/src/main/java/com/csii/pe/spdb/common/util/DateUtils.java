package com.csii.pe.spdb.common.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils {

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static final String FORMAT_DATE = "yyyy-MM-dd";

    /**
     * 获取 周日之后 指定天数 的 日期
     */
    public static List<String> getAfterDateMap(int num) {
        List<String> resDate = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar todayTime = Calendar.getInstance();
        todayTime.setTime(new Date());
//        todayTime.set(Calendar.DAY_OF_WEEK, 1); //获取当前日期 所属周的第一天    周日~~ 周六
//        todayTime.set(Calendar.DATE, todayTime.get(Calendar.DATE) + 7); //获取这个周日的日期
        for (int i = 1; i <= num; i++) {
            todayTime.add(Calendar.DAY_OF_YEAR, 1);
            resDate.add(simpleDateFormat.format(todayTime.getTime()));
        }
        return resDate;
    }

    public static String getDate(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }

    public static String getLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return dateToString(calendar.getTime());
    }

    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        return sdf.format(date);
    }

}

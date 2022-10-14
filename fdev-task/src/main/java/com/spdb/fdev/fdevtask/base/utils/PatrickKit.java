package com.spdb.fdev.fdevtask.base.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工具类
 */

public class PatrickKit {

    /**
     * 构建map
     * @param keys
     * @param values
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map buildMap(List<K> keys, List<V> values) {
        return keys.stream()
                .collect(Collectors.toMap(
                        key -> key, key -> values.get(keys.indexOf(key))));
    }

    /**
     * 根据开始日期需要的工作日天数。计算工作截止日期
     * @param startDate
     * @param workDay
     * @return
     */
    public static Date getWorkDay(Date startDate,int workDay){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        for (int i = 0; i < workDay; i++) {
            calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)+1);
            if (isWeekend(calendar)){
                workDay+=1;
                calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)+1);
                continue;
            }
        }
        return calendar.getTime();
    }

    /**
     * 确认工作日
     * @param calendar
     * @param period
     * @return
     */
    public static String getDatePresentSomeWorkDay(Calendar calendar, int period) {
        for (int i = 0; i < period; i++) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
            if (isWeekend(calendar)) {
                period = period + 1;
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                continue;
            }
        }
        SimpleDateFormat df = new SimpleDateFormat(CommonUtils.DATE_PATTERN_F1);
       // calendar.set(Calendar.DATE, calendar.get(Calendar.DATE));
        return df.format(calendar.getTime());
    }

    public static String  getDatePresentSomeWorkDay1(Calendar calendar,int period){
        String[]  list = {"2020/10/01","2020/10/02","2020/10/03","2020/10/04","2020/10/05","2020/10/06","2020/10/07",
                "2020/10/08","2021/01/01","2021/02/11","2021/02/12","2021/02/15","2021/02/16","2021/02/17",
                "2021/02/26","2021/04/03","2021/04/04","2021/04/05","2021/05/03","2021/06/14","2021/09/21",
                "2021/10/01","2021/10/02","2021/10/03","2021/10/04","2021/10/05","2021/10/06", "2021/10/07"};
        SimpleDateFormat df = new SimpleDateFormat(CommonUtils.DATE_PATTERN_F1);
        for (int i=0;i<period;i++){
            calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)-1);
            if (Calendar.SATURDAY == calendar.get(Calendar.SATURDAY) || Calendar.SUNDAY == calendar.get(Calendar.SUNDAY)
                    || isHoliday(df.format(calendar.getTime()),list)){
                period = period+1;
                calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)-1);
                continue;
            }
        }
        calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)+1);
        return df.format(calendar.getTime());
    }

    /**
     * 判断是否是周末
     * @param calendar
     * @return
     */
    public static boolean isWeekend(Calendar calendar){
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            return true;
        }else return false;
    }


    public static boolean isHoliday(String sdate, String[] list){
        for (String date:list){
            if (date.equals(sdate)){
                return true;
            }
        }
        return false;
    }
}

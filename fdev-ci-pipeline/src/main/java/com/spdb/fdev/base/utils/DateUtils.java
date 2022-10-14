package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Dict;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {

    public static final String FORMAT_DATE = "yyyy-MM-dd";

    public static final String FORMAT_DATE_2 = "yyyy/MM/dd";

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static final String HOUR_DATE = "HH:mm:ss";

    public static final String EAST_8_AREA_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    public static String getFormat(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        return sdf.parse(date);
    }

    public static Date stringToDateNoException(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        Date now = null;
        try {
            now = sdf.parse(date);
        } catch (ParseException e) {}
        return now;
    }

    public static String formatEast8Time(String eastDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(EAST_8_AREA_DATE);
        Date date;
        try {
            date = sdf.parse(eastDate);
        } catch (ParseException e) {
            return null;
        }
        return dateTimeToString(date);
    }

    public static String long2StringDate(long mills) {
        Date date = new Date();
        date.setTime(mills);
        return dateToString(date);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        return sdf.format(date);
    }

    public static String dateToString2(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_2);
        return sdf.format(date);
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        return sdf.format(new Date());
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        return sdf.format(new Date());
    }

    public static String dateTimeToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        return sdf.format(date);
    }

    public static Date stringToDateTime(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        return sdf.parse(date);
    }


    private static String getYearMonth(Calendar calendar) {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = (calendar.get(Calendar.MONTH) + 1) > 9 ?
                String.valueOf((calendar.get(Calendar.MONTH) + 1)) : "0" + (calendar.get(Calendar.MONTH) + 1);
        return year + "-" + month;
    }

    private static String getYearMonth2(Calendar calendar) {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = (calendar.get(Calendar.MONTH) + 1) > 9 ?
                String.valueOf((calendar.get(Calendar.MONTH) + 1)) : "0" + (calendar.get(Calendar.MONTH) + 1);
        return year + "/" + month;
    }

    /**
     * 获取两日期之间所有月份
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月份列表
     * @throws ParseException 日期格式不正确
     */
    public static List<String> getMonths(String startDate, String endDate) throws ParseException {
        if (endDate.compareToIgnoreCase(startDate) < 0) {
            return null;
        }
        Calendar start = Calendar.getInstance();
        start.setTime(stringToDate(startDate));
        Calendar end = Calendar.getInstance();
        end.setTime(stringToDate(endDate));
        String endMonth = getYearMonth(end);
        List<String> months = new ArrayList<>();
        // 递归比较月份
        comparedMonth(months, start, endMonth);
        return months;
    }

    private static void comparedMonth(List<String> months, Calendar start, String endMonth) {
        String startMonth = getYearMonth(start);
        months.add(startMonth);
        if (!endMonth.equals(startMonth)) {
            start.add(Calendar.MONTH, 1);
            comparedMonth(months, start, endMonth);
        }
    }

    public static List<String> getDefaultMonths() {
        Calendar start = Calendar.getInstance();
        start.setTime(new Date());
        start.add(Calendar.MONTH, -5);
        Calendar end = Calendar.getInstance();
        end.setTime(new Date());
        String endMonth = getYearMonth(end);
        List<String> months = new ArrayList<>();
        // 递归比较月份
        comparedMonth(months, start, endMonth);
        return months;
    }

    /**
     * 将costTime转换成时间戳
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Long getCostTimeByStringDate(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(HOUR_DATE);
        Date parse = sf.parse(date);
        long time = parse.getTime() + 8 * 60 * 60 * 1000;
        return time;
    }

    public static Long getSecondsByCost(String costTime) {
        Long result;
        try {
            String[] time = costTime.split(":");
            Integer hour = Integer.valueOf(time[0]);
            Integer minute = Integer.valueOf(time[1]);
            Integer second = Integer.valueOf(time[2]);
            result = Long.valueOf(hour * 60 * 60 + minute * 60 + second);
        } catch (Exception e) {
            result = 0L;
        }
        return result;
    }

    /**
     * 花费总时间long 转换成 float
     *
     * @param cost
     * @return
     */
    public static float costTimeLongToFloat(long cost) {
        float f = (float) cost / (1000 * 60);
        BigDecimal b = new BigDecimal(f);
        float min = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return min;
    }

    public static List<String> getWeekDaysList(Integer number, Date date, Integer week) {
        List<String> weekList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for (int i = 0; i < number; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            String endDate = dateToString(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.add(Calendar.WEEK_OF_MONTH, -week);
            String startDate = dateToString(calendar.getTime());
            weekList.add(startDate + "-" + endDate);
        }
        return weekList;
    }

    public static Map<String, List<String>> getWeekDaysMap(Integer number, Date date, Integer week) {
        Map<String, List<String>> weekMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for (int i = 0; i < number; i++) {
            String keyDate;
            List<String> days = new ArrayList<>();
            String start = "";
            String end = "";
            for (int j = 0; j < 7 * week; j++) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                keyDate = dateToString(calendar.getTime());
                days.add(keyDate);
                if (j == 0) {
                    end = dateToString2(calendar.getTime());
                } else if (j == 7 * week - 1) {
                    start = dateToString2(calendar.getTime());
                }
            }
            weekMap.put(start + "-" + end, days);
        }
        return weekMap;
    }

    public static Map<String, List<String>> getMonthDaysMap(Integer number, Date date) {
        Map<String, List<String>> weekMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for (int i = 0; i < number; i++) {
            int lastDay = calendar.getActualMaximum(Calendar.DATE);
            calendar.set(Calendar.DAY_OF_MONTH, lastDay);
            String keyDate = dateToString(calendar.getTime());
            List<String> days = new ArrayList<>();
            days.add(keyDate);
            for (int j = 1; j < lastDay; j++) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                keyDate = dateToString(calendar.getTime());
                days.add(keyDate);
            }
            weekMap.put(getYearMonth2(calendar), days);
            calendar.add(Calendar.MONTH, -1);
        }
        return weekMap;
    }

    public static List<String> getMonthList(Integer number, Date date) {
        List<String> monthList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        monthList.add(getYearMonth(calendar));
        for (int i = 1; i < number; i++) {
            calendar.add(Calendar.MONTH, -1);
            monthList.add(getYearMonth(calendar));
        }
        return monthList;
    }

    public static List<String> getDayList(String startTime, String endsTime) throws ParseException {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(stringToDate(startTime));
        Calendar calendar2 = Calendar.getInstance();
        List<String> dateList = new ArrayList<>();
        calendar2.setTime(stringToDate(endsTime));
        while (!calendar1.equals(calendar2)) {
            String date = dateToString(calendar1.getTime());
            dateList.add(date);
            calendar1.add(Calendar.DATE, 1);
        }
        dateList.add(endsTime);
        return dateList;
    }

    public static List<String> getMonthListNew(Integer number, Date date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -number);
        calendar.add(Calendar.DATE, 1);
        List<String> dateList = new ArrayList<>();
        String startTime = dateToString(calendar.getTime());
        String endTime = dateToString(date);
        String ss = startTime + "-" + endTime;
        dateList.add(ss);
        return dateList;
    }

    public static String getPreDay(String date) {
        Date d;
        try {
            d = stringToDate(date);
        } catch (ParseException e) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, -1);
        return dateToString(calendar.getTime());
    }

    public static String plusOneDay(String date) {
        Date d;
        try {
            d = stringToDate(date);
        } catch (ParseException e) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, 1);
        return dateToString(calendar.getTime());
    }

    public static String get3MonthAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        return dateToString(calendar.getTime());
    }

    public static String getLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return dateToString(calendar.getTime());
    }

    public static boolean dateAfter(String date) {
        boolean flag = false;
        if (dateToString(new Date()).compareTo(date) <= 0) {
            flag = true;
        }
        return flag;
    }

    public static Map<String, List<String>> getDefaultMonth(int number) {
        // 目的是返回一个周期  number传2 返回两个月的
        //  结尾日期截至到昨天
        Date date = new Date();
        int end = Integer.valueOf(DateUtils.dateToString(date).substring(5, 7));
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        calendar1.add(Calendar.MONTH, 1 - number);
        int mon = Integer.valueOf(DateUtils.dateToString(calendar1.getTime()).substring(5, 7));
        Map<String, List<String>> returnData = new HashMap<>();
        for (int i = mon; i <= end; i++) {
            int lastDay = calendar1.getActualMaximum(Calendar.DAY_OF_MONTH);
            int firstDay = calendar1.getActualMinimum(Calendar.DAY_OF_MONTH);
            int year = calendar1.get(Calendar.YEAR);
            int avg = (firstDay + lastDay - 1) / 2;
            List<String> temp = new ArrayList<>();
            for (int j = firstDay; j <= avg; j++) {
                String dd = year + "-" + String.format("%02d", i) + "-" + String.format("%02d", j);
                temp.add(dd);
            }
            returnData.put(i + "月上", temp);
            List<String> temp1 = new ArrayList<>();
            for (int k = avg + 1; k <= lastDay; k++) {
                String dd = year + "-" + String.format("%02d", i) + "-" + String.format("%02d", k);
                temp1.add(dd);
            }
            returnData.put(i + "月下", temp1);
            calendar1.add(Calendar.MONTH, 1);
        }
        return returnData;
    }

    public static Map<String, List<String>> getDefaultMonthTemp(int number) {
        // 目的是返回一个周期  number传2 返回两个月的
        //  结尾日期截至到昨天  倒推两个月
        //
        Date date = new Date();
        int end = Integer.valueOf(DateUtils.dateToString(date).substring(5, 7));
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        calendar1.add(Calendar.MONTH, 1 - number);
        int mon = Integer.valueOf(DateUtils.dateToString(calendar1.getTime()).substring(5, 7));
        Map<String, List<String>> returnData = new HashMap<>();
        Date date1 = new Date();
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date1);
        yesterday.add(Calendar.DATE, -1);
        String beforeDay = DateUtils.dateToString(yesterday.getTime());
        int dayNum = Integer.parseInt(beforeDay.substring(8));
        int monthNum = Integer.parseInt(beforeDay.substring(5, 7));
        int lastDay = calendar1.getActualMaximum(calendar1.DAY_OF_MONTH);
        int firstDay = calendar1.getActualMinimum(calendar1.DAY_OF_MONTH);
        int year = calendar1.get(Calendar.YEAR);
        int avg = (firstDay + lastDay - 1) / 2;
        if (dayNum <= avg) {
            List<String> temp = new ArrayList<>();
            for (int j = firstDay; j <= dayNum; j++) {
                String dd = year + "-" + String.format("%02d", monthNum) + "-" + String.format("%02d", j);
                temp.add(dd);
            }
            returnData.put(monthNum + "月上", temp);
        } else {
            List<String> temp = new ArrayList<>();
            for (int j = firstDay; j <= avg; j++) {
                String dd = year + "-" + String.format("%02d", monthNum) + "-" + String.format("%02d", j);
                temp.add(dd);
            }
            returnData.put(monthNum + "月上", temp);
            List<String> temp1 = new ArrayList<>();
            for (int k = avg + 1; k <= dayNum; k++) {
                String dd = year + "-" + String.format("%02d", monthNum) + "-" + String.format("%02d", k);
                temp1.add(dd);
            }
            returnData.put(monthNum + "月下", temp1);
        }
        int initNum = 2 * number  - returnData.size();
        Date date2 = new Date();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        calendar2.add(Calendar.MONTH, -1);
        while (initNum > 0) {
            int monthNum1 = Integer.parseInt(DateUtils.dateToString(calendar2.getTime()).substring(5,7));
            int lastDayNum = calendar2.getActualMaximum(calendar2.DAY_OF_MONTH);
            int firstDayNum = calendar2.getActualMinimum(calendar2.DAY_OF_MONTH);
            int avgNum = (firstDayNum + lastDayNum - 1) / 2;
            if (initNum > 0) {
                List<String> temp4 = new ArrayList<>();
                for (int k = avgNum + 1; k <= lastDayNum; k++) {
                    String dd = year + "-" + String.format("%02d", monthNum1) + "-" + String.format("%02d", k);
                    temp4.add(dd);
                }
                returnData.put(monthNum1 + "月下", temp4);
                initNum --;
            }
            if (initNum > 0) {
                List<String> temp3 = new ArrayList<>();
                for (int j = firstDayNum; j <= avgNum; j++) {
                    String dd = year + "-" + String.format("%02d", monthNum1) + "-" + String.format("%02d", j);
                    temp3.add(dd);
                }
                returnData.put(monthNum1 + "月上", temp3);
                initNum --;
            }
            calendar2.add(Calendar.MONTH, -1);
        }
        return returnData;
    }

}

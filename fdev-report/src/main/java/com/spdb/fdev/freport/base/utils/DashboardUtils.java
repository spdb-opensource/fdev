package com.spdb.fdev.freport.base.utils;

import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardUtils {

    private static int column = 6;

    /**
     * @param cycle 0：按星期
     *              1：按月
     *              2：按季度
     * @return
     */
    public static Map<String, Date[]> handleCycle(Integer cycle) {
        Map<String, Date[]> periodMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        switch (cycle) {
            case 0://按星期
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.add(Calendar.DAY_OF_WEEK, -calendar.get(Calendar.DAY_OF_WEEK) + 1);//以周一为第一天计算这里 + 1
                calendar.add(Calendar.WEEK_OF_MONTH, -column);
                for (int i = 0; i < column; i++) {
                    TimeUtils.setDayMin(calendar);//开始时间条件
                    calendar.add(Calendar.WEEK_OF_MONTH, 1);
                    calendar.add(Calendar.DAY_OF_WEEK, 1);//开始时间多取1天展示更加连贯
                    Date startDate = calendar.getTime();//“展示开始日期”也可以作为“条件开始日期”
                    calendar.add(Calendar.DAY_OF_WEEK, -1);//还原1天
                    TimeUtils.setDayMax(calendar);//结束时间条件
                    calendar.add(Calendar.WEEK_OF_MONTH, 1);
                    Date endDate = calendar.getTime();//“展示结束日期”并不能作为“条件结束日期”
                    calendar.add(Calendar.WEEK_OF_MONTH, -1);
                    periodMap.put(TimeUtils.FORMAT_DATESTAMP_2.format(startDate) + "-" + TimeUtils.FORMAT_DATESTAMP_2.format(endDate), new Date[]{startDate, endDate});
                }
                break;
            case 1://按月
                calendar.add(Calendar.MONTH, -column);
                for (int i = 0; i < column; i++) {
                    calendar.add(Calendar.MONTH, 1);
                    TimeUtils.setMonthMin(calendar);
                    Date startDate = calendar.getTime();
                    TimeUtils.setMonthMax(calendar);
                    Date endDate = calendar.getTime();
                    periodMap.put(TimeUtils.FORMAT_MONTHSTAMP.format(endDate), new Date[]{startDate, endDate});
                }
                break;
            case 2://按季度
                break;
        }
        return periodMap;
    }

    private static Map<String, List<String>> getWeekDaysMap(Integer week) {
        Map<String, List<String>> weekMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 6; i++) {
            String keyDate;
            List<String> days = new ArrayList<>();
            String start = "";
            String end = "";
            for (int j = 0; j < 7 * week; j++) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                keyDate = TimeUtils.FORMAT_DATESTAMP.format(calendar.getTime());
                days.add(keyDate);
                if (j == 0) {
                    end = TimeUtils.FORMAT_DATESTAMP_2.format(calendar.getTime());
                } else if (j == 7 * week - 1) {
                    start = TimeUtils.FORMAT_DATESTAMP_2.format(calendar.getTime());
                }
            }
            weekMap.put(start + "-" + end, days);
        }
        return weekMap;
    }

    private static Map<String, List<String>> getMonthDaysMap() {
        Map<String, List<String>> weekMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 6; i++) {
            int lastDay = calendar.getActualMaximum(Calendar.DATE);
            calendar.set(Calendar.DAY_OF_MONTH, lastDay);
            String keyDate = TimeUtils.FORMAT_DATESTAMP.format(calendar.getTime());
            List<String> days = new ArrayList<>();
            days.add(keyDate);
            for (int j = 1; j < lastDay; j++) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                keyDate = TimeUtils.FORMAT_DATESTAMP.format(calendar.getTime());
                days.add(keyDate);
            }
            weekMap.put(TimeUtils.FORMAT_MONTHSTAMP.format(calendar.getTime()), days);
            calendar.add(Calendar.MONTH, -1);
        }
        return weekMap;
    }

    public static String[] getStartAndEndByPeriodMap(Map<String, Date[]> periodsMap) {
        return getStartAndEndByPeriodMap(periodsMap, TimeUtils.FORMAT_TIMESTAMP);
    }

    public static String[] getStartAndEndByPeriodMap(Map<String, Date[]> periodsMap, SimpleDateFormat sdf) {
        String[] periodsMapKeyArray = periodsMap.keySet().toArray(new String[]{});
        String start = sdf.format(periodsMap.get(periodsMapKeyArray[0])[0]);
        String end = sdf.format((periodsMap.get(periodsMapKeyArray[periodsMapKeyArray.length - 1])[1]));
        return new String[]{start, end};
    }

}



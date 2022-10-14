package com.spdb.fdev.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    private static final String STANDARDDATEPATTERNS = "yyyy-MM-dd HH:mm:ss";
    private static final String STANDARDDATEP = "yyyy-MM-dd HH:mm";

    public static String formatToday() {
        SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEPATTERNS);
        Date date = new Date();
        return sf.format(date);
    }

    public static Date getDate(String date, String fromFormat) throws ParseException {
        SimpleDateFormat fromSimpleDateFormat = new SimpleDateFormat(fromFormat);
        return fromSimpleDateFormat.parse(date);
    }

    public static String getDate(String date, String fromFormat, String toFormat) throws ParseException {
        Date fromDate = getDate(date, fromFormat);
        return new SimpleDateFormat(toFormat).format(fromDate);
    }

    /**
     * 获取当天日期 yyyy-MM-dd HH:MM
     * @return
     */
    public static String getTodayHM(){
        SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEP);
        return sf.format(new Date());
    }
}

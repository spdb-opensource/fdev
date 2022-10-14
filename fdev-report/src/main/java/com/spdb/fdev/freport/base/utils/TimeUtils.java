package com.spdb.fdev.freport.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Util
 * <p>
 * 工具类
 */
public class TimeUtils {

    public static final String DATESTAMP = "yyyy-MM-dd";

    public static final String DATESTAMP_1 = "yyyyMMdd";

    public static final String DATESTAMP_2 = "yyyy/MM/dd";

    public static final String MONTHSTAMP = "yyyy-MM";

    public static final String MONTHSTAMP_1 = "yyyyMM";

    public static final String MONTHSTAMP_2 = "yyyy/MM";

    public static final SimpleDateFormat FORMAT_TIMESTAMP = new SimpleDateFormat(DATESTAMP + " HH:mm:ss");

    public static final SimpleDateFormat FORMAT_DATESTAMP = new SimpleDateFormat(DATESTAMP);

    // public static final SimpleDateFormat FORMAT_DATESTAMP_1 = new SimpleDateFormat(DATASTAMP_1);

    public static final SimpleDateFormat FORMAT_DATESTAMP_2 = new SimpleDateFormat(DATESTAMP_2);

    public static final SimpleDateFormat FORMAT_MONTHSTAMP = new SimpleDateFormat(MONTHSTAMP);

    // public static final SimpleDateFormat FORMAT_MONTHSTAMP_1 = new SimpleDateFormat(MONTHSTAMP_1);

    public static final SimpleDateFormat FORMAT_MONTHSTAMP_2 = new SimpleDateFormat(MONTHSTAMP_2);

    public static Date getMonthMin(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setMonthMin(calendar);
        return calendar.getTime();
    }

    public static Date getMonthMax(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setMonthMax(calendar);
        return calendar.getTime();
    }

    public static Date getDayMin(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDayMin(calendar);
        return calendar.getTime();
    }

    public static Date getDayMax(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDayMax(calendar);
        return calendar.getTime();
    }

    public static void setMonthMin(Calendar calendar) {
        calendar.set(Calendar.DATE, 1);
        setDayMin(calendar);
    }

    public static void setMonthMax(Calendar calendar) {
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        setDayMax(calendar);
    }

    //2021-09-09 00:00:00 <= condition < 2021-09-10 00:00:00
    //2021-09-09 00:00:00 <= condition <= 2021-09-09 23:59:59.999

    public static void setDayMin(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static void setDayMax(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

}

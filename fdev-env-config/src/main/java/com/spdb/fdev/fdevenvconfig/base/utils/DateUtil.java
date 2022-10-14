package com.spdb.fdev.fdevenvconfig.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String DATETIME_ISO_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_COMPACT_FORMAT = "yyyyMMddHHmmss";
    public static final String DATE_SLASH_FORMAT = "yyyy/MM/dd";

    public static String getDate(Date date, String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(date);
    }

    public static Date getDate(String date, String fromFormat) throws ParseException {
        SimpleDateFormat fromSimpleDateFormat = new SimpleDateFormat(fromFormat);
        return fromSimpleDateFormat.parse(date);
    }

    public static String getDate(String date, String fromFormat, String toFormat) throws ParseException {
        Date fromDate = getDate(date, fromFormat);
        return new SimpleDateFormat(toFormat).format(fromDate);
    }

    public static String getCurrentDate(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }

}

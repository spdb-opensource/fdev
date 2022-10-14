package com.spdb.fdev.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static final String DATE_UTC_FORMAT ="yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATETIME_ISO_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String getDate(String date, String fromFormat) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(fromFormat);
        Date newdate = formatter.parse(date);
        SimpleDateFormat sdf=new SimpleDateFormat(DATETIME_ISO_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));//设置时区为东八区
        String sDate=sdf.format(newdate);
        return sDate;
    }
}

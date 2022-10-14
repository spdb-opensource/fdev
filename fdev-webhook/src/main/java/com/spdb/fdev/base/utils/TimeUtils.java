package com.spdb.fdev.base.utils;

import java.text.SimpleDateFormat;

public class TimeUtils {

    private TimeUtils() {
    }

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String getFormat(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }

}

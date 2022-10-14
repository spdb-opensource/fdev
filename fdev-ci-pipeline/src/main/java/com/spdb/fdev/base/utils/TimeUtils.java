package com.spdb.fdev.base.utils;

import java.text.SimpleDateFormat;

/**
 * @Author: Hxy
 * @Date: 2020/12/1 8:22 上午
 */
public class TimeUtils {

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE = "yyyy-MM-dd";

    public static String getDate(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }
}

package com.spdb.fdev.fdevinterface.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    private static Logger logger = LoggerFactory.getLogger(TimeUtils.class);
    private TimeUtils() {
    }

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE = "yyyy-MM-dd_HH-mm-ss";
    public static final String FORMAT_DATE_PROD = "yyyy_MM_dd-HH-mm-ss";

    public static String getFormat(String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat(pattern);
        return sdt.format(System.currentTimeMillis());
    }

    public static String getFormat(String time , String pattern) {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = null;
        try {
            date = sdt.parse(time);
        } catch (ParseException e) {
            logger.error("转换时间 {} 的时候出现错误，错误信息如下{}",time , e);
        }
        return new SimpleDateFormat(pattern).format(date);
    }

}

package com.spdb.fdev.fdevapp.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

	public static final String STANDARDDATEPATTERN = "yyyyMMddHHmm";
	public static final String STANDARDDATEPATTERNS="yyyy-MM-dd HH:mm:ss"; 
	public static final String STANDARDDATE="yyyy-MM"; 


	/**
	 * 获取当前时间 毫秒级
	 * 
	 * @return
	 */
	public static long getTime() {
		return System.currentTimeMillis();
	}

	public static String getFormat(String pattern) {
		SimpleDateFormat sdt = new SimpleDateFormat(pattern);
		return sdt.format(System.currentTimeMillis());
	}

	/**
	 * 获取当前时间的前一天时间(减去8小时)
	 *
	 * @return
	 */
	public static String getTheDayBefore(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STANDARDDATEPATTERNS);
		Calendar calendar =Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Long time = calendar.getTimeInMillis()-28800000;
		calendar.setTimeInMillis(time);
		Date date = calendar.getTime();
		return simpleDateFormat.format(date);
	}

	public static String formatDate(String str) {
		SimpleDateFormat sf = new SimpleDateFormat(str);
		Date date = new Date();
		return sf.format(date);
	}
}

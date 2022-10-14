package com.spdb.fdev.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

	public static final String STANDARDDATEPATTERN = "yyyyMMddHHmm";
	public static final String STANDARDDATEPATTERNS="yyyy-MM-dd HH:mm:ss";
	public static final String STANDARDDATE="yyyy-MM";
	private static final String STANDARD_YYYYMMDD = "yyyyMMdd";
	private static final String STANDARD_YYYY_MM_DD = "yyyy-MM-dd";

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

	public static String formatDate(String str) {
		SimpleDateFormat sf = new SimpleDateFormat(str);
		Date date = new Date();
		return sf.format(date);
	}

    /**
     * 获取n天后的日期，以yyyyMMdd格式的字符串返回
     * @param date 当前日期
     * @param n 后几天
     * @return 以yyyyMMdd格式的字符串返回
     * @throws ParseException 传入日期格式不正确
     */
	public static String getDaysAgoStringDate(String date, int n) throws ParseException {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_YYYYMMDD);
		calendar.setTime(sdf.parse(date));
		calendar.set(Calendar.DATE, calendar.get(calendar.DATE) + n);
		return sdf.format(calendar.getTime());
	}

	/**
	 * 传入日期与当前日期比较
	 * @param stringDate
	 * @return 若传入日期小于当前日期返回true
	 * @throws ParseException
	 */
	public static boolean compareDate(String stringDate) throws ParseException {
		boolean flag = false;
		SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_YYYY_MM_DD);
		String today = sdf.format(new Date());
		Date date = sdf.parse(stringDate);
		if(date.before(new Date()) && !stringDate.equals(today)) {
			flag = true;
		}
		return flag;
	}

}

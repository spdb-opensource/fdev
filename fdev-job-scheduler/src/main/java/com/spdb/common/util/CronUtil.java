package com.spdb.common.util;

import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: Tool class
 * @author: lizz
 * @since: 2019-10-25 10:21
 **/
public class CronUtil {

    private static final String TRANS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String TRANS_CRON_FORMAT_ONCE = "ss mm HH dd MM ? yyyy";

    private static final String TRANS_CRON_FORMAT_DAY = "ss mm HH dd/ * ? *";

    private static final String TRANS_CRON_FORMAT_WEEK = "ss mm HH ? * -- *";

    private static final String TRANS_CRON_FORMAT_MONTH = "ss mm HH dd MM/1 ? *";

    private static final String DAY = "day";

    private static final String MONTH = "month";

    private static final String WEEK = "week";

    /**
     * 生成只执行一次的cron
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static String getCronByOnce(String dateStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(TRANS_DATE_FORMAT);
        Date parse = format.parse(dateStr);
        return fmtDateToStr(parse, TRANS_CRON_FORMAT_ONCE);
    }

    /**
     * 生成每月或每周或每天执行的cron
     *
     * @param period
     * @param startDateStr
     * @return
     * @throws ParseException
     */
    public static String getDateToCron(String period, String startDateStr) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat(TRANS_DATE_FORMAT);
        Date startDate = format.parse(startDateStr);
        StringBuffer cronStringBuffer = new StringBuffer();
        if (DAY.equals(period)) {
            String startDateCron = fmtDateToStr(startDate, TRANS_CRON_FORMAT_DAY).trim();
            cronStringBuffer.append(startDateCron).insert(cronStringBuffer.lastIndexOf("/") + 1, "1");
        } else if (MONTH.equals(period)) {
            String startDateCron = fmtDateToStr(startDate, TRANS_CRON_FORMAT_MONTH).trim();
            cronStringBuffer.append(startDateCron);
        } else if (WEEK.equals(period)) {
            String startDateCron = fmtDateToStr(startDate, TRANS_CRON_FORMAT_WEEK).trim();
            cronStringBuffer.append(startDateCron.replaceAll("--", period));
        } else {
            throw new Exception("period is error.");
        }
        return cronStringBuffer.toString();
    }

    /**
     * cron 表达式转换日期
     *
     * @param cron
     * @return
     */
    public static String getCronToDate(String cron) {
        String format = null;
        StringBuffer stringBuffer = new StringBuffer(cron);
        int lastIndexOf = stringBuffer.lastIndexOf("/");
        stringBuffer.deleteCharAt(lastIndexOf);
        String dateFormat = TRANS_CRON_FORMAT_ONCE;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date date = sdf.parse(stringBuffer.toString());
            sdf = new SimpleDateFormat(TRANS_DATE_FORMAT);
            format = sdf.format(date);
        } catch (Exception e) {
            return null;
        }
        return format;
    }

    /**
     * 格式转换 日期-字符串
     *
     * @param date
     * @param dtFormat
     * @return
     */
    public static String fmtDateToStr(Date date, String dtFormat) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dtFormat);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @param cronExpression cron表达式
     * @param numTimes       下一(几)次运行的时间
     * @return
     */
    public static List<Long> getNextExecTime(String cronExpression, Integer numTimes) {
        List<Long> list = new ArrayList<>();
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(cronExpression);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 这个是重点，一行代码搞定
        List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, numTimes);
        for (Date date : dates) {
            list.add(date.getTime());
        }
        return list;
    }

    /**
     * 计算时间区间之间触发时间列表
     *
     * @param cronExpression
     * @param startTime
     * @param endTime
     * @param quartzCalender
     * @return
     */
    public static List<Long> getNextExecTimeBetween(String cronExpression, org.quartz.Calendar quartzCalender, Date startTime, Date endTime) {
        List<Long> list = new ArrayList<>();
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(cronExpression);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 这个是重点，一行代码搞定
        List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, quartzCalender, startTime, endTime);
        for (Date date : dates) {
            list.add(date.getTime());
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        String result = getDateToCron("day", "2019-10-28 01:03:00");
        //String result = getDateToCron("MON", "2018-08-11 12:11:00");
        // String result = getDateToCron("2017-01-01 12:12:12");
        //String result = getDateToCron("month", "2019-01-01 12:00:00");
        //String result = getCronToDate("12 12 12 01 01/1 ? 2018");
        List<Long> strList = CronUtil.getNextExecTime("00 09 15 * * ?", 2);
        //Date date = DateUtil.parseDate(DateUtil.STANDARDDATEPATTERN,strList.get(0));
        System.out.println(strList);
        System.out.println(strList.get(0));
        List<Long> schedTimeList =
                CronUtil.getNextExecTimeBetween("00 57 10 * * ?", null, new Date(), DateUtil.getTodayEndTime());
        for (Long schedTime : schedTimeList) {
            System.out.println(schedTime);
        }
        // System.out.println(strList);

    }
}

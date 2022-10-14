package com.spdb.fdev.fdemand.base.utils;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * DashboradUtils
 * Dashborad的工具类
 **/
public class DashboradUtils {

    private static Logger logger = LoggerFactory.getLogger(DashboradUtils.class);// 控制台日志打印

    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH-mm";
    public static final String STANDARDDATEPATTERNS = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARDDATE = "yyyy-MM-dd";


    /**
     * 拼接fdev 的组的信息
     */
    public static List<LinkedHashMap<String, String>> getGroupFullName(List<LinkedHashMap<String, String>> groupList) {
        for (LinkedHashMap<String, String> groupObj : groupList) {
            String parentId = groupObj.get(Dict.PARENT_ID);
            StringBuffer fullName = new StringBuffer(groupObj.get(Dict.NAME));
            if (CommonUtils.isNullOrEmpty(parentId)) {
                groupObj.put(Dict.LABEL, fullName.toString());
                continue;
            } else {
                int i = 0;
                while (i < groupList.size()) {
                    if (groupList.get(i).get(Dict.ID).equals(parentId)) {
                        fullName.insert(0, groupList.get(i).get(Dict.NAME) + "-");
                        if (CommonUtils.isNullOrEmpty(groupList.get(i).get(Dict.PARENT_ID))) {
                            break;
                        } else {
                            parentId = groupList.get(i).get(Dict.PARENT_ID);
                            i = 0;
                        }
                    }
                    i++;
                }
                groupObj.put(Dict.LABEL, fullName.toString());
            }
        }
        return groupList;
    }

    /**
     * 统计实施单元数据
     */
    public static Map getStatisImplUnits() {
        Map dataMap = new HashMap<>();
        dataMap.put("groupId", "");
        dataMap.put("groupName", "");
        dataMap.put("pgzkj", 0);
        dataMap.put("pgzyw", 0);
        dataMap.put("dsskj", 0);
        dataMap.put("dsspdkj", 0);
        dataMap.put("dssyw", 0);
        dataMap.put("dsspdyw", 0);
        dataMap.put("kaifakj", 0);
        dataMap.put("kaifayw", 0);
        dataMap.put("cssitkj", 0);
        dataMap.put("cssityw", 0);
        dataMap.put("csuatkj", 0);
        dataMap.put("csuatyw", 0);
        dataMap.put("csrelkj", 0);
        dataMap.put("csrelyw", 0);
        dataMap.put("csprokj", 0);
        dataMap.put("csproyw", 0);

        dataMap.put("kfqdcqkj", 0);
        dataMap.put("kfqdcqyw", 0);
        dataMap.put("tccqkj", 0);
        dataMap.put("tccqyw", 0);
        dataMap.put("procqkj", 0);
        dataMap.put("procqyw", 0);
        dataMap.put("kaifarjfh", 0);
        dataMap.put("totalkj", 0);
        dataMap.put("totalyw", 0);

        dataMap.put("deferkj", 0);
        dataMap.put("deferyw", 0);
        return dataMap;
    }

    public static void statisImplUnitsArray(FdevImplementUnit implunit, Map statismodel, String type) throws Exception {
        //7天时间
        Date futureDay = new Date(todayLastDate().getTime() + 7 * 24 * 3600 * 1000);
        long futureDayTime = futureDay.getTime();
        //        //当前时间
        long now = new Date().getTime();
        //计划开始时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        String plan_start_date = implunit.getPlan_start_date();
        Date parse = simpleDateFormat.parse(plan_start_date);
        if (CommonUtils.isNullOrEmpty(implunit.getImplement_unit_status_special()) || implunit.getImplement_unit_status_special().equals(3)) {
            switch (implunit.getImplement_unit_status_normal()) {
                //1：评估中、2：待实施、3：开发中、4：sit、5：uat、6：rel，7：已投产8：已归档 9：已撤销
                case 1://新增“评估中”列
                    statisImplUnitKejiYw(statismodel, "pgz", type);//我只能说我是第一次这样命名，在我还是新手的时候我都没这么做过
                    break;
                case 2:
                    statisImplUnitKejiYw(statismodel, "dss", type);
                    //判断是否一周内启动[启动日期<=当前日期(最大时间)+7]
                    if (!CommonUtils.isNullOrEmpty(implunit.getPlan_start_date()) && parse.getTime() > now && parse.getTime() <= futureDayTime) {
                        statisImplUnitKejiYw(statismodel, "dsspd", type);
                    }
                    break;
                case 3:
                    statisImplUnitKejiYw(statismodel, "kaifa", type);
                    break;
                case 4:
                    statisImplUnitKejiYw(statismodel, "cssit", type);
                    break;
                case 5:
                    statisImplUnitKejiYw(statismodel, "csuat", type);
                    break;
                case 6:
                    statisImplUnitKejiYw(statismodel, "csrel", type);
                    break;
                case 7:
                    statisImplUnitKejiYw(statismodel, "cspro", type);
                    break;
            }
        } else {
            statisImplUnitKejiYw(statismodel, "defer", type);
        }
    }

    /**
     * 统计超期实施单元
     */
    public static void statisImplUnitsBeOverdue(FdevImplementUnit implunit, Map statismodel, String type) throws ParseException {
        long now = new Date().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        SimpleDateFormat simpleDateFormats = new SimpleDateFormat(CommonUtils.INPUT_DATE);
        //计划启动开发日期
        String plan_start_date = implunit.getPlan_start_date();
        //计划提交用户测试日期sit
        String plan_inner_test_date = implunit.getPlan_inner_test_date();
        //计划提交业测日期uat
        String plan_test_date = implunit.getPlan_test_date();
        //计划测试完成日期rel
        String plan_test_finish_date = implunit.getPlan_test_finish_date();
        //计划投产日期
        String plan_product_date = implunit.getPlan_product_date();
        if (plan_start_date.contains("/")) {

        }
        long plan_start_time = simpleDateFormat.parse(plan_start_date).getTime();
        long plan_test_time = 0;
        if (!CommonUtils.isNullOrEmpty(plan_test_date)) {
            plan_test_time = simpleDateFormat.parse(plan_test_date).getTime();
        }
        long plan_test_finish_time = 0;
        if (!CommonUtils.isNullOrEmpty(plan_test_finish_date)) {
            plan_test_finish_time = simpleDateFormat.parse(plan_test_finish_date).getTime();
        }
        if (CommonUtils.isNullOrEmpty(implunit.getImplement_unit_status_special()) || implunit.getImplement_unit_status_special().equals(3)) {
            switch (implunit.getImplement_unit_status_normal()) {
                //待实施
                case 2:
                    if (!CommonUtils.isNullOrEmpty(plan_start_date) && now > plan_start_time) {
                        statisImplUnitKejiYw(statismodel, "kfqdcq", type);
                    }
                    if (!CommonUtils.isNullOrEmpty(plan_test_date) && now > plan_test_time) {
                        statisImplUnitKejiYw(statismodel, "tccq", type);
                    }
                    if (!CommonUtils.isNullOrEmpty(plan_test_finish_date) && now > plan_test_finish_time) {
                        statisImplUnitKejiYw(statismodel, "procq", type);
                    }
                    break;
                //开发中、sit
                case 3:
                case 4:
                    if (!CommonUtils.isNullOrEmpty(plan_test_date) && now > plan_test_time) {
                        statisImplUnitKejiYw(statismodel, "tccq", type);
                    }
                    if (!CommonUtils.isNullOrEmpty(plan_test_finish_date) && now > plan_test_finish_time) {
                        statisImplUnitKejiYw(statismodel, "procq", type);
                    }
                    break;
                //uat、rel
                case 5:
                case 6:
                    if (!CommonUtils.isNullOrEmpty(plan_test_finish_date) && now > plan_test_finish_time) {
                        statisImplUnitKejiYw(statismodel, "procq", type);
                    }
                    break;
            }
        }
    }


    /**
     * 获取格式日期
     */
    public static Date getDefFormatDate(String dateStr) {
        return getFormatDate(dateStr, "yyyy-MM-dd");
    }

    public static Date getFormatDate(String dateStr, String strFormat) {
        if (CommonUtils.isNullOrEmpty(dateStr)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
        Date resDate = null;
        try {
            resDate = sdf.parse(dateStr);
        } catch (ParseException e) {
        	logger.error("获取格式日期失败！" + e.getMessage());
        }
        return resDate;
    }

    public static String rqrmntDateToTaskDate(String str) {
        if (CommonUtils.isNullOrEmpty(str))
            return null;
        return dateToOtherFormat(str, "yyyy-MM-dd", "yyyy/MM/dd");
    }

    public static String dateToOtherFormat(String date, String format, String otherFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(otherFormat);
        return sdf.format(getFormatDate(date, format));
    }

    /**
     * 获取当天的 00：00：00
     *
     * @return
     */
    public static Date todayFirstDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当天的 23：59：59
     *
     * @return
     */
    public static Date todayLastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取格式日期 当天 23：59：59
     */
    public static Date getDefDayMaxDate(String dateStr) {
        return getDayMaxDate(dateStr, "yyyy-MM-dd");
    }

    public static Date getDayMaxDate(String dateStr, String strFormat) {
        Date resDate = getFormatDate(dateStr, strFormat);
        return getDateAdd(resDate, 23 * 60 * 60 * 1000 + 59 * 60 * 1000 + 59 * 1000);
    }


    /**
     * 把 date转换成localDateTime类型
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 把 localDateTime类型转换程date类型
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 将str日期转为date
     *
     * @param date
     * @param format
     * @return
     */
    public static Date strDateToDate(String date, String format) {
        if (date == null)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date newDate = null;
        try {
            newDate = sdf.parse(date);
        } catch (ParseException e) {
            logger.error("str日期转换成date类型失败，" + e.getMessage());
        }
        return newDate;
    }

    /**
     * 获取 date 与 isoDate 相差后的 date
     * 将date 时间增加 8个小时
     */
    public static Date getIOSDate(String dateStr) {
        Date resDate = getFormatDate(dateStr, "yyyy-MM-dd");
        return getDateAdd(resDate, 8 * 60 * 60 * 1000);
    }

    public static Date getIOSDate(Date resDate) {
        return getDateAdd(resDate, 8 * 60 * 60 * 1000);
    }

    /**
     * date类型 增加 时间
     */
    public static Date getDateAdd(Date resDate, long time) {
        if (resDate == null)
            return null;
        return new Date(resDate.getTime() + time);
    }

    /**
     * 'create-info': 0,
     * 'create-app': 0,
     * 'create-feature': 0,
     * 'develop': 1,
     * 'sit': 2,
     * 'uat': 3,
     * 'rel': 4,
     * 'production': 5
     */
    public static Map<String, Integer> getTaskStageMap() {
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("create-info", 0);
        dataMap.put("create-app", 0);
        dataMap.put("create-feature", 0);
        dataMap.put("develop", 1);
        dataMap.put("sit", 2);
        dataMap.put("uat", 3);
        dataMap.put("rel", 4);
        dataMap.put("production", 5);
        return dataMap;
    }

    /**
     * const groupfilterMap = [
     * {key: "板块1-财富组", value: '零售金融-板块1-财富组'},
     * {key: "板块2-微生活组", value: '零售金融-板块2-微生活组'},
     * {key: "板块3-互联组", value: '零售金融-板块3-互联组'},
     * {key: "板块4-转账组", value: '零售金融-板块4-转账组'},
     * {key: "板块5-公共组", value: '零售金融-板块5-公共组'},
     * {key: "板块6-APP组", value: '零售金融-板块6-APP组'},
     * {key: "板块7-账户组", value: '零售金融-板块7-账户组'},
     * {key: "板块8-生态组", value: '零售金融-板块8-生态组'},
     * {key: "支付组", value: '互联网应用-支付组'},
     * {key: "公司组", value: '互联网应用-公司组'},
     * {key: "公共组", value: '互联网应用-公共组'},
     * {key: "移动营销组", value: '互联网应用-移动营销组'}
     * ];
     */
    public static Map<String, String> getGroupfilterMap() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("板块1-财富组", "零售金融-板块1-财富组");
        dataMap.put("板块2-微生活组", "零售金融-板块2-微生活组");
        dataMap.put("板块3-互联组", "零售金融-板块3-互联组");
        dataMap.put("板块4-转账组", "零售金融-板块4-转账组");
        dataMap.put("板块5-公共组", "零售金融-板块5-公共组");
        dataMap.put("板块6-APP组", "零售金融-板块6-APP组");
        dataMap.put("板块7-账户组", "零售金融-板块7-账户组");
        dataMap.put("板块8-生态组", "零售金融-板块8-生态组");
        dataMap.put("支付组", "互联网应用-支付组");
        dataMap.put("公司组", "互联网应用-公司组");
        dataMap.put("公共组", "互联网应用-公共组");
        dataMap.put("移动营销组", "互联网应用-移动营销组");
        return dataMap;
    }

    /**
     * 统计需求数据
     * let statisgroup = {
     * groupId:'',groupName:'',
     * yxkj:0,yxyw:0,
     * ypgkj:0,ypgyw:0,
     * pgkj:0,pgyw:0,
     * dsskj:0,dsspdkj:0,dssyw:0,dsspdyw:0,
     * kaifakj:0,kaifayw:0,
     * cssitkj:0,cssityw:0,
     * csuatkj:0,csuatyw:0,
     * csrelkj:0,csrelyw:0,
     * csprokj:0,csproyw:0,
     * totalkj:0,totalyw:0
     * };
     */
    public static Map getStatisgroup() {
        Map dataMap = new HashMap<>();
        dataMap.put("groupId", "");
        dataMap.put("groupName", "");
        dataMap.put("yxkj", 0);
        dataMap.put("yxyw", 0);
        dataMap.put("ypgkj", 0);
        dataMap.put("ypgyw", 0);
        dataMap.put("pgkj", 0);
        dataMap.put("pgyw", 0);
        dataMap.put("dsskj", 0);
        dataMap.put("dsspdkj", 0);
        dataMap.put("dssyw", 0);
        dataMap.put("dsspdyw", 0);
        dataMap.put("kaifakj", 0);
        dataMap.put("kaifayw", 0);
        dataMap.put("cssitkj", 0);
        dataMap.put("cssityw", 0);
        dataMap.put("csuatkj", 0);
        dataMap.put("csuatyw", 0);
        dataMap.put("csrelkj", 0);
        dataMap.put("csrelyw", 0);
        dataMap.put("csprokj", 0);
        dataMap.put("csproyw", 0);
        //暂缓统计
        dataMap.put("waitkj", 0);
        dataMap.put("waityw", 0);
        //超期统计
        dataMap.put("kfqdcqkj", 0);
        dataMap.put("kfqdcqyw", 0);
        dataMap.put("tccqkj", 0);
        dataMap.put("tccqyw", 0);
        dataMap.put("procqkj", 0);
        dataMap.put("procqyw", 0);
        dataMap.put("kaifarjfh", 0);
        dataMap.put("ywxqrjfh", 0);
        //非本组牵头的需求 统计
        dataMap.put("nogdevelopkj", 0);
        dataMap.put("nogdevelopyw", 0);
        dataMap.put("nogtestkj", 0);
        dataMap.put("nogtestyw", 0);

        dataMap.put("totalkj", 0);
        dataMap.put("totalyw", 0);
        //项目组规模
        dataMap.put("count", 0);
        return dataMap;
    }

    public static void statisImplUnitKejiYw(Map statismodel, String prefix, String type) {
        statismodel.put(prefix + type, (int) statismodel.get(prefix + type) + 1);
    }

    /**
     * //统计各状态 需求数
     *
     * @param demand
     * @param statismodel
     */
    public static void statisDemandArray(DemandBaseInfo demand, Map statismodel) {
        Date futureDay = new Date(todayLastDate().getTime() + 7 * 24 * 3600 * 1000);
        //统计暂缓
        if (!CommonUtils.isNullOrEmpty(demand.getDemand_status_special()) &&
                (demand.getDemand_status_special() == 1 || demand.getDemand_status_special() == 2)) {
            statisRqrmntKejiYw(statismodel, demand, "wait");
            return;
        }
        if (null == demand.getDemand_status_normal())
            return;
        switch (demand.getDemand_status_normal()) {
            case 0://"预评估"
                statisRqrmntKejiYw(statismodel, demand, "ypg");
                break;
            case 1://"需求评估"
                statisRqrmntKejiYw(statismodel, demand, "pg");
                break;
            case 2://"待实施"
                statisRqrmntKejiYw(statismodel, demand, "dss");
                //判断是否一周后启动[启动日期<=当前日期(最大时间)+7]
                if (!CommonUtils.isNullOrEmpty(demand.getPlan_start_date()) &&
                        strDateToDate(demand.getPlan_start_date(), STANDARDDATE).getTime() > todayLastDate().getTime() &&
                        strDateToDate(demand.getPlan_start_date(), STANDARDDATE).getTime() <= futureDay.getTime()) {
                    statisRqrmntKejiYw(statismodel, demand, "dsspd");
                }
                break;
            case 3://"开发中"
                statisRqrmntKejiYw(statismodel, demand, "kaifa");
                break;
            case 4://"SIT"
                statisRqrmntKejiYw(statismodel, demand, "cssit");
                break;
            case 5://"UAT"
                statisRqrmntKejiYw(statismodel, demand, "csuat");
                break;
            case 6://"REL"
                statisRqrmntKejiYw(statismodel, demand, "csrel");
                break;
            case 7://"已投产"
                statisRqrmntKejiYw(statismodel, demand, "cspro");
                break;
        }

    }


    //统计 非本组牵头需求 数量
    public static void statisRqrmntKejiYw(Map statismodel, DemandBaseInfo item, String prefix) {
        //如果计划日期为空，则不计数
        //预评估只有受理日期和创建时间，没有则不记
//        if (prefix.equals("ypg")) {
//            if (CommonUtils.isNullOrEmpty(item.getAccept_date())
//                    || CommonUtils.isNullOrEmpty(item.getDemand_create_time()))
//                return;
//        }else
//            //余下的阶段都有相应的计划日期了，若没有不记
//            if (CommonUtils.isNullOrEmpty(item.getPlan_inner_test_date()) ||
//                    CommonUtils.isNullOrEmpty(item.getPlan_product_date()) ||
//                    CommonUtils.isNullOrEmpty(item.getAccept_date()) ||
//                    CommonUtils.isNullOrEmpty(item.getPlan_start_date()) ||
//                    CommonUtils.isNullOrEmpty(item.getDemand_create_time()) ||
//                    CommonUtils.isNullOrEmpty(item.getPlan_test_date()) ||
//                    CommonUtils.isNullOrEmpty(item.getPlan_test_finish_date())) {
//                return;
//            }
        String type = item.getDemand_type();
        if (type == null || CommonUtils.isNullOrEmpty(statismodel))
            return;
        if (type.indexOf("tech") >= 0) {
            statismodel.put(prefix + "kj", (int) statismodel.get(prefix + "kj") + 1);
        } else {
            statismodel.put(prefix + "yw", (int) statismodel.get(prefix + "yw") + 1);
        }
    }


    /**
     * 统计超期需求: 最新状态不延期，则该需求整体不延期；如多个状态都延期，只在最新状态下显示延期
     */
    public static void statisRqrmntBeOverdue(DemandBaseInfo rqritem, Map statismodel) {
        //计划启动日期
        String planStartDate = rqritem.getPlan_start_date();
        //LocalDateTime estInitRealDate = rqritem.getEstInitRealDate();
        //计划提测日期 sit
        String planInnerTestDate = rqritem.getPlan_inner_test_date();
//        LocalDateTime estSitRealDate = rqritem.getEstUatRealDate();
        //计划投产日期 pro
        String planProductDate = rqritem.getPlan_product_date();
//        LocalDateTime estRelComplRealDate = rqritem.getEstRelComplRealDate();
        if (CommonUtils.isNullOrEmpty(rqritem.getDemand_status_normal()))
            return;
        switch (rqritem.getDemand_status_normal()) {
            case 0://"预评估"
            case 1://"需求评估"
            case 2://待实施
                //当前日期的最小时间大于各阶段日期
                if (!CommonUtils.isNullOrEmpty(planStartDate) && todayFirstDate().getTime() > strDateToDate(planStartDate, STANDARDDATE).getTime()
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "kfqdcq");//开发启动超期

                } else if (!CommonUtils.isNullOrEmpty(planInnerTestDate) && todayFirstDate().getTime() > strDateToDate(planInnerTestDate, STANDARDDATE).getTime()
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "tccq");

                } else if (!CommonUtils.isNullOrEmpty(planProductDate) && todayFirstDate().getTime() > strDateToDate(planProductDate, STANDARDDATE).getTime()
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "procq");
                }
        		/*if(!CommonUtils.isNullOrEmpty(estSitDate) && todayFirstDate().getTime() > localDateTimeToDate(estSitDate).getTime()) {
            		statisRqrmntKejiYw(statismodel, rqritem, "tccq");//提测超期
            	}
            	if(!CommonUtils.isNullOrEmpty(estRelComplDate) && todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime()) {
            		statisRqrmntKejiYw(statismodel, rqritem, "procq");//投产超期
            	}*/
                break;
            case 3://"开发中"
            case 4://"SIT"
                //当前日期的最小时间大于计划启动日期
                if (!CommonUtils.isNullOrEmpty(planInnerTestDate) && todayFirstDate().getTime() > strDateToDate(planInnerTestDate, STANDARDDATE).getTime()
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "tccq");

                } else if (!CommonUtils.isNullOrEmpty(planProductDate) && todayFirstDate().getTime() > strDateToDate(planProductDate, STANDARDDATE).getTime()
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "procq");
                }
            	/*if(!CommonUtils.isNullOrEmpty(estRelComplDate) && todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime()) {
            		statisRqrmntKejiYw(statismodel, rqritem, "procq");
            	}*/
                break;
            case 5://"UAT"
            case 6://"REL"
                //当前日期的最小时间大于计划启动日期
                if (!CommonUtils.isNullOrEmpty(planProductDate) && todayFirstDate().getTime() > strDateToDate(planProductDate, STANDARDDATE).getTime()
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_start_date())
                        && !CommonUtils.isNullOrEmpty(rqritem.getReal_test_date())
                        && CommonUtils.isNullOrEmpty(rqritem.getReal_product_date())) {
                    statisRqrmntKejiYw(statismodel, rqritem, "procq");
                }
                break;
        }

    }

    /**
     * 获取到每个任务的需求编号，去重，返回
     *
     * @param groupTasks 任务信息数据
     */
    public static Set<String> getRqrmntsNo(List<LinkedHashMap> groupTasks) {
        Set<String> rqrmntNos = new HashSet();
        if (CommonUtils.isNullOrEmpty(groupTasks)) {
            return rqrmntNos;
        }
        for (LinkedHashMap groupTask : groupTasks) {
            if (!CommonUtils.isNullOrEmpty(groupTask.get("rqrmnt_no")))
                rqrmntNos.add((String) groupTask.get("rqrmnt_no"));
        }
        return rqrmntNos;
    }


    /**
     * 统计 非本组牵头的需求
     */
    public static void statisOthersRqrmnt(Set<String> groupids, List<DemandBaseInfo> demandBaseInfos, Map statisgroup) {
        if (CommonUtils.isNullOrEmpty(demandBaseInfos))
            return;
        //过滤掉需求 所属小组 与 当前组 相同 的需求
        Iterator<DemandBaseInfo> rqrmntsIterator = demandBaseInfos.iterator();
        while (rqrmntsIterator.hasNext()){
            DemandBaseInfo item = rqrmntsIterator.next();
            if (CommonUtils.isNullOrEmpty(item.getDemand_leader_group()) || groupids.contains(item.getDemand_leader_group()))
                rqrmntsIterator.remove();
        }
        for (DemandBaseInfo rqrmnt : demandBaseInfos) {
            Integer rqrmntState = rqrmnt.getDemand_status_normal();
            switch (rqrmntState) {
                case 4://"SIT"
                case 5://"UAT"
                case 6://"REL"
                    statisRqrmntKejiYw(statisgroup, rqrmnt, "nogtest");
                    break;
                case 3://"开发中"
                    statisRqrmntKejiYw(statisgroup, rqrmnt, "nogdevelop");
                    break;
            }
        }
    }


    /**
     * 统计非本组牵头的需求
     *
     * @param demand  需求实体
     * @param result  返回值map
     * @param groupId 前端的单个groupId
     */
    public static void statisOthersDemand(DemandBaseInfo demand, Map result, String groupId) {
        if (CommonUtils.isNullOrEmpty(demand))
            return;
        Integer demandStatusSpecial = demand.getDemand_status_special();
        //过滤暂缓中，或恢复中，且没null的
        if (CommonUtils.isNullOrEmpty(demandStatusSpecial) || (demandStatusSpecial == 1 || demandStatusSpecial == 2))
            return;
        //过滤掉需求，本组牵头的需求
        String demand_leader_group = demand.getDemand_leader_group();
        if (demand_leader_group.equals(groupId))
            return;
        Integer demandState = demand.getDemand_status_normal();
        if (null == demandState)
            return;
        switch (demandState) {
            case 4://"SIT"
            case 5://UAT
            case 6://"REL"
                statisRqrmntKejiYw(result, demand, "nogtest");
                break;
            case 3://"开发中"
                statisRqrmntKejiYw(result, demand, "nogdevelop");
                break;
        }
    }

    public static Integer pareStage(String stage) {
        final Map<String, Integer> stages = new HashMap();
        stages.put("create-info", 1);
        stages.put("create-app", 2);
        stages.put("create-feature", 3);
        stages.put("develop", 4);
        stages.put("sit", 5);
        stages.put("uat", 6);
        stages.put("rel", 7);
        stages.put("production", 8);
        stages.put("file", 9);
        stages.put("abort", 10);
        stages.put("discard", 11);
        return stages.containsKey(stage) ? stages.get(stage) : -1;
    }

    public static String minStage(List stageList) {
        stageList.sort(Comparator.comparingInt(DashboradUtils::pareStage));
        return (String) stageList.get(0);
    }


    public static String minStage(String stage1, String stage2) {
        List<String> stageList = new ArrayList<>();
        stageList.add(stage1);
        stageList.add(stage2);
        return minStage(stageList);
    }
}

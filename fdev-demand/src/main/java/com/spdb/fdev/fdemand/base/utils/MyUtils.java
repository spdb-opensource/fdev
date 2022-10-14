package com.spdb.fdev.fdemand.base.utils;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

public class MyUtils {

	private static Logger logger = LoggerFactory.getLogger(MyUtils.class);// 控制台日志打印

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
	 * 获取 date 与 isoDate 相差后的 date 将date 时间增加 8个小时
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
	 * 'create-info': 0, 'create-app': 0, 'create-feature': 0, 'develop': 1, 'sit':
	 * 2, 'uat': 3, 'rel': 4, 'production': 5
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
	 * rqrmntStateMap = { '待实施': 0, '开发中': 1, 'SIT': 2, 'UAT': 3, 'REL': 4, '已投产': 5
	 */
	public static Map<String, Integer> getRqrmntStateMap() {
		Map<String, Integer> dataMap = new HashMap<>();
		dataMap.put("待实施", 0);
		dataMap.put("开发中", 1);
		dataMap.put("SIT", 2);
		dataMap.put("UAT", 3);
		dataMap.put("REL", 4);
		dataMap.put("已投产", 5);
		return dataMap;
	}

	/**
	 * const groupfilterMap = [ {key: "板块1-财富组", value: '零售金融-板块1-财富组'}, {key:
	 * "板块2-微生活组", value: '零售金融-板块2-微生活组'}, {key: "板块3-互联组", value: '零售金融-板块3-互联组'},
	 * {key: "板块4-转账组", value: '零售金融-板块4-转账组'}, {key: "板块5-公共组", value:
	 * '零售金融-板块5-公共组'}, {key: "板块6-APP组", value: '零售金融-板块6-APP组'}, {key: "板块7-账户组",
	 * value: '零售金融-板块7-账户组'}, {key: "板块8-生态组", value: '零售金融-板块8-生态组'}, {key: "支付组",
	 * value: '互联网应用-支付组'}, {key: "公司组", value: '互联网应用-公司组'}, {key: "公共组", value:
	 * '互联网应用-公共组'}, {key: "移动营销组", value: '互联网应用-移动营销组'} ];
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
	 * 统计需求数据 let statisgroup = { groupId:'',groupName:'', yxkj:0,yxyw:0,
	 * ypgkj:0,ypgyw:0, pgkj:0,pgyw:0, dsskj:0,dsspdkj:0,dssyw:0,dsspdyw:0,
	 * kaifakj:0,kaifayw:0, cssitkj:0,cssityw:0, csuatkj:0,csuatyw:0,
	 * csrelkj:0,csrelyw:0, csprokj:0,csproyw:0, totalkj:0,totalyw:0 };
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
		// 超期统计
		dataMap.put("kfqdcqkj", 0);
		dataMap.put("kfqdcqyw", 0);
		dataMap.put("tccqkj", 0);
		dataMap.put("tccqyw", 0);
		dataMap.put("procqkj", 0);
		dataMap.put("procqyw", 0);
		dataMap.put("kaifarjfh", 0);
		dataMap.put("ywxqrjfh", 0);
		// 非本组牵头的需求 统计
		dataMap.put("nogdevelopkj", 0);
		dataMap.put("nogdevelopyw", 0);
		dataMap.put("nogtestkj", 0);
		dataMap.put("nogtestyw", 0);

		dataMap.put("totalkj", 0);
		dataMap.put("totalyw", 0);
		// 项目组规模
		dataMap.put("count", 0);
		return dataMap;
	}

	/*
	 * public static void statisRqrmntArray(RqrmntAdd rqritem, Map statismodel){
	 * Date futureDay = new Date(todayLastDate().getTime() + 7*24*3600*1000); switch
	 * (rqritem.getRqrmntState()){ case "预评估": statisRqrmntKejiYw(statismodel,
	 * rqritem,"ypg"); break; case "需求评估": statisRqrmntKejiYw(statismodel,
	 * rqritem,"pg"); break; case "待实施": statisRqrmntKejiYw(statismodel,
	 * rqritem,"dss"); //判断是否一周后启动[启动日期<=当前日期(最大时间)+7] if
	 * (!CommonUtils.isNullOrEmpty(rqritem.getEstInitDate()) &&
	 * localDateTimeToDate(rqritem.getEstInitDate()).getTime() >
	 * todayLastDate().getTime() &&
	 * localDateTimeToDate(rqritem.getEstInitDate()).getTime() <=
	 * futureDay.getTime()){ statisRqrmntKejiYw(statismodel, rqritem, "dsspd"); }
	 * break; case "开发中": statisRqrmntKejiYw(statismodel, rqritem,"kaifa"); break;
	 * case "SIT": statisRqrmntKejiYw(statismodel, rqritem,"cssit"); break; case
	 * "UAT": statisRqrmntKejiYw(statismodel, rqritem,"csuat"); break; case "REL":
	 * statisRqrmntKejiYw(statismodel, rqritem,"csrel"); break; case "已投产":
	 * statisRqrmntKejiYw(statismodel, rqritem,"cspro"); break; }
	 * 
	 * }
	 */
	/*
	 * public static void statisRqrmntKejiYw(Map statismodel, RqrmntAdd item, String
	 * prefix){ if(!"ypg".equals(prefix) && !"pg".equals(prefix) &&
	 * !"dss".equals(prefix) && "dsspd".equals(prefix)) { if
	 * (CommonUtils.isNullOrEmpty(item.getEstSitDate()) ||
	 * CommonUtils.isNullOrEmpty(item.getEstRelComplDate()) ||
	 * CommonUtils.isNullOrEmpty(item.getCaseRcvDate()) ||
	 * CommonUtils.isNullOrEmpty(item.getEstInitDate())){ return ; } } if
	 * (item.getRqrmntType().indexOf("科技") >= 0){ statismodel.put(prefix+"kj",
	 * (int)statismodel.get(prefix+"kj")+1); }else { statismodel.put(prefix+"yw",
	 * (int)statismodel.get(prefix+"yw")+1); } }
	 */

	/**
	 * 拼接fdev 的组的信息
	 */
	public static List<LinkedHashMap<String, String>> getGroupFullName(List<LinkedHashMap<String, String>> groupList) {
		for (LinkedHashMap<String, String> groupObj : groupList) {
			String parentId = groupObj.get("parent_id");
			StringBuffer fullName = new StringBuffer(groupObj.get("name"));
			if (CommonUtils.isNullOrEmpty(parentId)) {
				groupObj.put("label", fullName.toString());
				continue;
			} else {
				int i = 0;
				while (i < groupList.size()) {
					if (groupList.get(i).get("id").equals(parentId)) {
						fullName.insert(0, groupList.get(i).get("name") + "-");
						if (CommonUtils.isNullOrEmpty(groupList.get(i).get("parent_id"))) {
							break;
						} else {
							parentId = groupList.get(i).get("parent_id");
							i = 0;
						}
					}
					i++;
				}
				groupObj.put("label", fullName.toString());
			}
		}
		return groupList;
	}

	/**
	 * 判断输入数据是否是空
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isNullOrEmpty(Object obj) {
		if (obj instanceof Object[]) {
			Object[] o = (Object[]) obj;
			if (o.length == 0) {
				return true;
			}
			return false;
		} else {
			if (obj instanceof String) {
				if ((("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
					return true;
				}
				return false;
			}
			if (obj instanceof List) {
				List objList = (List) obj;
				if (objList.isEmpty()) {
					return true;
				}
				return false;
			}
			if (obj instanceof Map) {
				Map objMap = (Map) obj;
				if (objMap.isEmpty()) {
					return true;
				}
				return false;
			}
			if (obj instanceof Set) {
				Set objSet = (Set) obj;
				if (objSet.isEmpty()) {
					return true;
				}
				return false;
			}
			if ((obj == null) || (("").equals(obj))) {
				return true;
			}
		}
		return false;
	}

	public static void statisImplUnitKejiYw(Map statismodel, String prefix, String type) {
		statismodel.put(prefix + type, (int) statismodel.get(prefix + type) + 1);
	}

	/**
	 * 统计实施单元数据
	 */
	public static Map getStatisImplUnits() {
		Map dataMap = new HashMap<>();
		dataMap.put("groupId", "");
		dataMap.put("groupName", "");
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
		return dataMap;
	}

	/*
	 * public static void statisImplUnitsArray(Implunit implunit, Map
	 * statismodel,String type){ Date futureDay = new Date(todayLastDate().getTime()
	 * + 7*24*3600*1000); switch (implunit.getImplUnitState()){ case "待实施":
	 * statisImplUnitKejiYw(statismodel,"dss",type); //判断是否一周后启动[启动日期<=当前日期(最大时间)+7]
	 * if (!CommonUtils.isNullOrEmpty(implunit.getPlanDevDate()) &&
	 * localDateTimeToDate(implunit.getPlanDevDate()).getTime() >
	 * todayLastDate().getTime() &&
	 * localDateTimeToDate(implunit.getPlanDevDate()).getTime() <=
	 * futureDay.getTime()){ statisImplUnitKejiYw(statismodel,"dsspd",type); }
	 * break; case "开发中": statisImplUnitKejiYw(statismodel,"kaifa",type); break;
	 * case "SIT": statisImplUnitKejiYw(statismodel,"cssit",type); break; case
	 * "UAT": statisImplUnitKejiYw(statismodel,"csuat",type); break; case "REL":
	 * statisImplUnitKejiYw(statismodel,"csrel",type); break; case "已投产":
	 * statisImplUnitKejiYw(statismodel,"cspro",type); break; }
	 * 
	 * }
	 */
	/**
	 * 
	 * 统计超期需求: 最新状态不延期，则该需求整体不延期；如多个状态都延期，只在最新状态下显示延期
	 * 
	 */
	/*
	 * public static void statisRqrmntBeOverdue(RqrmntAdd rqritem, Map statismodel){
	 * //计划启动日期 LocalDateTime estInitDate = rqritem.getEstInitDate(); LocalDateTime
	 * estInitRealDate = rqritem.getEstInitRealDate(); //计划提测日期 LocalDateTime
	 * estSitDate = rqritem.getEstSitDate(); LocalDateTime estSitRealDate =
	 * rqritem.getEstUatRealDate(); //计划投产日期 LocalDateTime estRelComplDate =
	 * rqritem.getEstRelComplDate(); LocalDateTime estRelComplRealDate =
	 * rqritem.getEstRelComplRealDate(); switch (rqritem.getRqrmntState()){ case
	 * "预评估": case "需求评估": case "待实施": //当前日期的最小时间大于各阶段日期
	 * if(!CommonUtils.isNullOrEmpty(estInitDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(estInitDate).getTime() &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "kfqdcq");//开发启动超期
	 * 
	 * }else if (!CommonUtils.isNullOrEmpty(estSitDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(estSitDate).getTime() &&
	 * !CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "tccq");
	 * 
	 * }else if (!CommonUtils.isNullOrEmpty(estRelComplDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime()
	 * && !CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * !CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "procq"); }
	 * if(!CommonUtils.isNullOrEmpty(estSitDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(estSitDate).getTime()) { statisRqrmntKejiYw(statismodel,
	 * rqritem, "tccq");//提测超期 } if(!CommonUtils.isNullOrEmpty(estRelComplDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime())
	 * { statisRqrmntKejiYw(statismodel, rqritem, "procq");//投产超期 } break; case
	 * "开发中": case "SIT": //当前日期的最小时间大于计划启动日期
	 * if(!CommonUtils.isNullOrEmpty(estSitDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(estSitDate).getTime() &&
	 * !CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "tccq");
	 * 
	 * }else if (!CommonUtils.isNullOrEmpty(estRelComplDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime()
	 * && !CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * !CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "procq"); }
	 * if(!CommonUtils.isNullOrEmpty(estRelComplDate) && todayFirstDate().getTime()
	 * > localDateTimeToDate(estRelComplDate).getTime()) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "procq"); } break; case "UAT": case
	 * "REL": //当前日期的最小时间大于计划启动日期 if(!CommonUtils.isNullOrEmpty(estRelComplDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(estRelComplDate).getTime()
	 * && !CommonUtils.isNullOrEmpty(rqritem.getEstInitRealDate()) &&
	 * !CommonUtils.isNullOrEmpty(rqritem.getEstUatRealDate()) &&
	 * CommonUtils.isNullOrEmpty(rqritem.getEstRelComplRealDate())) {
	 * statisRqrmntKejiYw(statismodel, rqritem, "procq"); } break; }
	 * 
	 * }
	 */

	/**
	 * 
	 * 统计超期实施单元
	 *
	 */
	/*
	 * public static void statisImplUnitsBeOverdue(Implunit implunit, Map
	 * statismodel, String type) { // 计划启动开发日期 LocalDateTime planDevDate =
	 * implunit.getPlanDevDate(); // 计划提交用户测试日期 LocalDateTime planUatDate =
	 * implunit.getPlanUatDate(); // 计划投产日期 LocalDateTime planRelComplDate =
	 * implunit.getPlanRelComplDate(); switch (implunit.getImplUnitState()) { case
	 * "待实施": // 当前日期的最小时间大于各阶段日期 if (!CommonUtils.isNullOrEmpty(planDevDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(planDevDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "kfqdcq", type); } if
	 * (!CommonUtils.isNullOrEmpty(planUatDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(planUatDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "tccq", type); } if
	 * (!CommonUtils.isNullOrEmpty(planRelComplDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(planRelComplDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "procq", type); } break; case "开发中": case
	 * "SIT": // 当前日期的最小时间大于计划启动日期 if (!CommonUtils.isNullOrEmpty(planUatDate) &&
	 * todayFirstDate().getTime() > localDateTimeToDate(planUatDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "tccq", type); } if
	 * (!CommonUtils.isNullOrEmpty(planRelComplDate) && todayFirstDate().getTime() >
	 * localDateTimeToDate(planRelComplDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "procq", type); } break; case "UAT": case
	 * "REL": // 当前日期的最小时间大于计划启动日期 if (!CommonUtils.isNullOrEmpty(planRelComplDate)
	 * && todayFirstDate().getTime() >
	 * localDateTimeToDate(planRelComplDate).getTime()) {
	 * statisImplUnitKejiYw(statismodel, "procq", type); } break; }
	 * 
	 * }
	 */
	/**
	 * 判断两个list是否相同(不考虑元素顺序)
	 */
	public static boolean isListEqual(List list1, List list2) {
		boolean flag1 = list1.containsAll(list2);
		boolean flag2 = list2.containsAll(list1);
		return flag1 && flag2;
	}

	public static User getLoginUser() {
		User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getSession().getAttribute(Dict._USER);
		if (CommonUtils.isNullOrEmpty(user)) {
			throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
		}
		return user;
	}

	/**
	 * 统计 非本组牵头的需求
	 */
	/*
	 * public static void statisOthersRqrmnt(Set<String> groupids, List<RqrmntForUI>
	 * rqrmnts, Map statisgroup) { if (CommonUtils.isNullOrEmpty(rqrmnts)) return;
	 * // 过滤掉需求 所属小组 与 当前组 相同 的需求 Iterator<RqrmntForUI> rqrmntsIterator =
	 * rqrmnts.iterator(); while (rqrmntsIterator.hasNext()) { RqrmntForUI item =
	 * rqrmntsIterator.next(); if (CommonUtils.isNullOrEmpty(item.getFdevGroup()) ||
	 * groupids.containsAll(item.getFdevGroup())) rqrmntsIterator.remove(); } for
	 * (RqrmntForUI rqrmnt : rqrmnts) {
	 * 
	 * String rqrmntState = rqrmnt.getRqrmntState(); switch (rqrmntState) { case
	 * "SIT": case "UAT": case "REL": statisRqrmntKejiYw(statisgroup, rqrmnt,
	 * "nogtest"); break; case "开发中": statisRqrmntKejiYw(statisgroup, rqrmnt,
	 * "nogdevelop"); break; } }
	 * 
	 * }
	 */

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

    public static void createInnerTestReport(Map<String, Object> workOrder, XSSFWorkbook workbook, int index) throws Exception {
		XSSFCellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		style.setFillBackgroundColor(new XSSFColor(Color.decode("#c0c0c0")));
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		Sheet sheet = workbook.createSheet((String) workOrder.get("workOrderNo"));
		sheet.setDefaultColumnWidth(20);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 3));
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));
		setCellValue(workbook, index, 0, 0, style, "工单名称");
		setCellValue(workbook, index, 0, 1, style, (String) workOrder.get("mainTaskName"));
		setCellValue(workbook, index, 1, 0, "实施单元/研发单元");
		setCellValue(workbook, index, 1, 1, (String) workOrder.get("unit"));
		setCellValue(workbook, index, 1, 2, "工单编号");
		setCellValue(workbook, index, 1, 3, (String) workOrder.get("workOrderNo"));
		setCellValue(workbook, index, 2, 0, "测试小组长");
		setCellValue(workbook, index, 2, 1, (String) workOrder.get("groupLeader"));
		setCellValue(workbook, index, 2, 2, "测试人员");
		setCellValue(workbook, index, 2, 3, (String) workOrder.get("testers"));
		setCellValue(workbook, index, 3, 0, "测试结果");
		setCellValue(workbook, index, 3, 1, (String) workOrder.get("testResult"));
		setCellValue(workbook, index, 4, 0, "测试范围");
		setCellValue(workbook, index, 4, 1, (String) workOrder.get("testRange"));
		int i = 5;
		if (!CommonUtils.isNullOrEmpty(workOrder.get("planData"))) {
			List<Map<String, Object>> planDataList = (List<Map<String, Object>>) workOrder.get("planData");
			for (Map<String, Object> planData : planDataList) {
				sheet.addMergedRegion(new CellRangeAddress(i, i, 1, 3));
				setCellValue(workbook, index, i, 0, style, "测试计划");
				setCellValue(workbook, index, i, 1, style, (String) planData.get("planName"));
				i++;
				setCellValue(workbook, index, i, 0, "测试案例总数");
				setCellValue(workbook, index, i, 1, String.valueOf(planData.get("allCase")));
				setCellValue(workbook, index, i, 2, "执行通过数");
				setCellValue(workbook, index, i, 3, String.valueOf( planData.get("sumSucc")));
				i++;
				setCellValue(workbook, index, i, 0, "案例执行失败数");
				setCellValue(workbook, index, i, 1, String.valueOf(planData.get("sumFail")));
				setCellValue(workbook, index, i, 2, "案例执行阻塞数");
				setCellValue(workbook, index, i, 3, String.valueOf(planData.get("sumBlock")));
				i++;
				setCellValue(workbook, index, i, 0, "有效缺陷数");
				setCellValue(workbook, index, i, 1, (String) planData.get("validMantis"));
				setCellValue(workbook, index, i, 2, "无效缺陷数");
				setCellValue(workbook, index, i, 3, (String) planData.get("braceMantis"));
				i++;
				setCellValue(workbook, index, i, 0, "设备信息");
				setCellValue(workbook, index, i, 1, (String) planData.get("deviceInfo"));
				setCellValue(workbook, index, i, 2, "无效用例数");
				setCellValue(workbook, index, i, 3, String.valueOf(planData.get("invalidCase")));
				i++;
				setCellValue(workbook, index, i, 0, "计划开始时间");
				setCellValue(workbook, index, i, 1, (String) planData.get("planStartDate"));
				setCellValue(workbook, index, i, 2, "计划结束时间");
				setCellValue(workbook, index, i, 3, (String) planData.get("planEndDate"));
				i++;
			}
		}
	}

	/**
	 * excel填值
	 *
	 * @param workbook
	 *            excel对象
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
	public static void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, XSSFCellStyle style, String cellValue)	throws Exception {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet == null) {
			sheet = workbook.createSheet(String.valueOf(sheetIndex));
		}
		if (sheet.getRow(rowIndex) == null) {
			sheet.createRow(rowIndex);
		}
		if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
			sheet.getRow(rowIndex).createCell(cellIndex);
		}

		sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(style);
		sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
	}

	/**
	 * excel填值
	 *
	 * @param workbook
	 *            excel对象
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
	public static void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex,String cellValue)	throws Exception {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet == null) {
			sheet = workbook.createSheet(String.valueOf(sheetIndex));
		}
		if (sheet.getRow(rowIndex) == null) {
			sheet.createRow(rowIndex);
		}
		if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
			sheet.getRow(rowIndex).createCell(cellIndex);
		}

		sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
	}
    // 统计 非本组牵头需求 数量
	/*public static void statisRqrmntKejiYw(Map statismodel, RqrmntForUI item, String prefix) {
		// 如果计划日期为空，则不计数
		if (CommonUtils.isNullOrEmpty(item.getEstSitDate()) || CommonUtils.isNullOrEmpty(item.getEstRelComplDate())
				|| CommonUtils.isNullOrEmpty(item.getCaseRcvDate())
				|| CommonUtils.isNullOrEmpty(item.getEstInitDate())) {
			return;
		}
		String type = item.getRqrmntType();
		if (type == null || CommonUtils.isNullOrEmpty(statismodel))
			return;
		if (type.indexOf("科技") >= 0) {
			statismodel.put(prefix + "kj", (int) statismodel.get(prefix + "kj") + 1);
		} else {
			statismodel.put(prefix + "yw", (int) statismodel.get(prefix + "yw") + 1);
		}
	}*/
}
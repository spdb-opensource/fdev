
package com.spdb.fdev.fdemand.base.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.UserInfo;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class CommonUtils {
    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String INPUT_DATE = "yyyy/MM/dd";

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o == null || o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((obj == null) || (("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
                    return true;
                }
                return false;
            }
            if (obj instanceof List) {
                List objList = (List) obj;
                if (objList == null || objList.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                if (objMap == null || objMap.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Set) {
                Set objSet = (Set) obj;
                if (objSet == null || objSet.isEmpty()) {
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

    /**
     * 通过key或者实体类的field获得对应的值
     *
     * @param o   取值对象
     * @param key 可以是map中的key或者是实体对象的field
     * @return key或field在object的对应值
     * @author xxx
     */
    public static Object returnObjByKey(Object o, String key) {
        if (o != null) {
            if (o instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) o;
                return map.get(key);
            } else if (o instanceof Set) {
                return o;
            } else if (o instanceof List) {
                return o;
            } else {
                Class<?> clazz = o.getClass();
                try {
                    Field field = clazz.getDeclaredField(key);
                    field.setAccessible(true);
                    return field.get(o);
                } catch (Exception e) {
                    logger.error("根据属性获取对应值出错");
                }
            }
        }
        return null;
    }


    /**
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public static User getSessionUser() throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
    }


    public static Map beanToMap(Object cls) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
    }

    public static HashMap beanToHashMap(Object cls) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, HashMap.class);
    }

    /**
     * map转bean
     *
     * @return 实体bean
     * @throws Exception
     */
    public static <T> T jsonToBean(JSONObject json, Class<T> cls) throws Exception {
        JSONObject beanJson = new JSONObject();

        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            if (json.containsKey(name)) {
                beanJson.put(name, json.get(name));
            }
        }
        return (T) JSONObject.toBean(beanJson, cls);
    }

    public static <T> T map2Object(Map map, Class<T> clazz) {
        Object obj = null;
        try {
            obj = clazz.newInstance();
            for (Object key : map.keySet()) {
                try {
                    Field field = clazz.getDeclaredField(key.toString());
                    field.setAccessible(true);
                    field.set(obj, map.get(key));
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
    }

    /**
     * 删除文件夹及其下面的子文件夹和文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File subFile : listFiles) {
                        deleteFile(subFile);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            logger.error("删除失败，请人工删除，{}", file.getAbsolutePath());
        }
    }

    public static Map<String, Object> object2Map(Object o) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (o == null)
            return map;
        Class clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if ("serialVersionUID".equals(field.getName()) || "_id".equals(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                if (field.get(o) != null)
                    map.put(field.getName(), field.get(o));
            }
        } catch (Exception e) {
            logger.error("对象转map出错");
        }
        return map;
    }

    public static Integer pareStage(String stage) {
        final Map<String, Integer> stages = new HashMap();
        stages.put(Dict.TASK_STAGE_CREATE_INFO, 2);
        stages.put(Dict.TASK_STAGE_DEVELOP, 3);
        stages.put(Dict.TASK_STAGE_SIT, 4);
        stages.put(Dict.TASK_STAGE_UAT, 5);
        stages.put(Dict.TASK_STAGE_REL, 6);
        stages.put(Dict.TASK_STAGE_PRODUCTION, 7);
        return stages.containsKey(stage) ? stages.get(stage) : -1;
    }

    public static String implementUnitLeader(FdevImplementUnit fdevImplementUnit) {
        if (CommonUtils.isNullOrEmpty(fdevImplementUnit)) {
            return "";
        }
        String result = "";
        List<UserInfo> userInfo = fdevImplementUnit.getImplement_leader_all();
        if (CommonUtils.isNullOrEmpty(userInfo)) {
            return "";
        }
        for (UserInfo user : userInfo) {
            result += user.getUser_name_cn() + "，";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    //获取当前周日期
    public static String getCalendar(int week) {
        SimpleDateFormat sf = new SimpleDateFormat(DATE_PARSE);
        Calendar cld = Calendar.getInstance(Locale.CHINA);
        cld.setFirstDayOfWeek(Calendar.MONDAY);//以周一为第一天
        cld.setTimeInMillis(System.currentTimeMillis());//当前时间
        cld.set(Calendar.DAY_OF_WEEK, week);
        return sf.format(cld.getTime());
    }

    //拼接用户中文名
    public static String appendUserCn(DemandBaseInfo demandBaseInfo, List<Map> queryUser) {
        StringBuffer stringBuffer = new StringBuffer();
        Set<String> techContacts = demandBaseInfo.getDemand_leader();
        if (null == techContacts)  techContacts = new HashSet<>();
        ArrayList<String> techContact = new ArrayList<>(techContacts);
        for (int i = 0; i < techContact.size(); i++) {
            for (Map user : queryUser) {
                if (user.get(Dict.ID).equals(techContact.get(i))) {
                    if (i == (techContact.size() - 1)) {
                        stringBuffer.append(user.get(Dict.USER_NAME_CN));
                    } else {
                        stringBuffer.append(user.get(Dict.USER_NAME_CN)).append(",");
                    }
                }
            }
        }
        return stringBuffer.toString();
    }

    //截取日期 2020-03-22T08:00 --> 2020-03-22
    public static String getSubstring(String date) {
        String substringBefore = StringUtils.substringBefore(date, "T");
        return substringBefore;
    }

    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    //计算延期天数
    public static long countDay(String date1, String date2) throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat(DATE_PARSE);
        Date parse1 = sf.parse(date1);
        Date parse2 = sf.parse(date2);
        long date = (parse1.getTime() - parse2.getTime()) / (60 * 60 * 24 * 1000);
        return date;
    }

    //拼接需求
    public static Map appendMap(String demandNo, String demandName, String date, String appendUserCn, long countDay) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(appendUserCn)) {
            hashMap.put(Dict.DEMAND_NO, demandNo);
            hashMap.put(Dict.DEMAND_NAME, demandName);
            hashMap.put(Dict.DATE, date);
            hashMap.put(Dict.APPENDUSERCN, "无");
            hashMap.put(Dict.COUNTDAY, String.valueOf(countDay));
        } else {
            hashMap.put(Dict.DEMAND_NO, demandNo);
            hashMap.put(Dict.DEMAND_NAME, demandName);
            hashMap.put(Dict.DATE, date);
            hashMap.put(Dict.APPENDUSERCN, appendUserCn);
            hashMap.put(Dict.COUNTDAY, String.valueOf(countDay));
        }
        return hashMap;
    }

    //拼接科技负责人邮箱
    public static List<String> appendUserEmail(DemandBaseInfo demandBaseInfo, List<Map> queryUser) {
        List<String> list = new ArrayList<>();
        Set<String> techContacts = demandBaseInfo.getDemand_leader();
        if (null == techContacts)  techContacts = new HashSet<>();
        ArrayList<String> techContact = new ArrayList<>(techContacts);
        for (int i = 0; i < techContact.size(); i++) {
            for (Map user : queryUser) {
                if (user.get(Dict.ID).equals(techContact.get(i))) {
                    String email = (String) user.get(Dict.EMAIL);
                    if (!list.contains(email) && !isNullOrEmpty(email)) {
                        list.add(email);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 拼接邮件主题 subject
     * 【开发/提测启动提醒】本周[{0}]即将到期需求提醒通知
     * 【开发/提测启动告警】本周[{0}]仍然延期的需求告警通知
     *
     * @param subject
     * @param groupName
     * @return
     */
    public static String appendSubject(String subject, String groupName) {
        StringBuffer stringBuffer = new StringBuffer();
        if (subject.equals(Dict.FDEMAND_EXPIREDMSG)) {
            stringBuffer.append("【开发/提测启动提醒】本周[").append(groupName).append("]即将到期需求提醒通知");
        } else if (subject.equals(Dict.FDEMAND_OVERDUEMSG)) {
            stringBuffer.append("【开发/提测启动告警】[").append(groupName).append("]仍然延期的需求告警通知");
        }
        return stringBuffer.toString();
    }

    //拼接各组固定收件人邮箱
    public static List<String> appendGroupReceiver(String rqrmntGroupReceiver, String groupId) {
        List<Map> listMap = new ArrayList<>();
        String[] split = rqrmntGroupReceiver.split(";");
        for (int i = 0; i < split.length; i++) {
            String[] groupSplit = split[0].split(":");
            HashMap hashMap = new HashMap<>();
            hashMap.put(Constants.GROUPID, groupSplit[0]);
            hashMap.put(Dict.GROUPRECEIVER, groupSplit[1]);
            if (!listMap.contains(hashMap)) {
                listMap.add(hashMap);
            }
        }
        List<String> list = new ArrayList<>();
        if (!isNullOrEmpty(listMap)) {
            for (Map map : listMap) {
                String groupid = (String) map.get(Constants.GROUPID);
                if (groupId.equals(groupid)) {
                    String groupReceiver = (String) map.get(Dict.GROUPRECEIVER);
                    String[] receiver = groupReceiver.split(",");
                    for (int i = 0; i < receiver.length; i++) {
                        if (!list.contains(receiver[i])) {
                            list.add(receiver[i]);
                        }
                    }
                }
            }
        }
        return list;
    }
    
    //拼接各组固定收件人邮箱
    public static List<String> appendGroupReceiverNew(String rqrmntGroupReceiver, String groupId) {
        List<Map> listMap = new ArrayList<>();
        String[] split = rqrmntGroupReceiver.split(";");
        for (int i = 0; i < split.length; i++) {
            String[] groupSplit = split[i].split(":");
            HashMap hashMap = new HashMap<>();
            hashMap.put(Constants.GROUPID, groupSplit[0]);
            hashMap.put(Dict.GROUPRECEIVER, groupSplit[1]);
            if (!listMap.contains(hashMap)) {
                listMap.add(hashMap);
            }
        }
        List<String> list = new ArrayList<>();
        if (!isNullOrEmpty(listMap)) {
            for (Map map : listMap) {
                String groupid = (String) map.get(Constants.GROUPID);
                if (groupId.equals(groupid)) {
                    String groupReceiver = (String) map.get(Dict.GROUPRECEIVER);
                    String[] receiver = groupReceiver.split(",");
                    for (int i = 0; i < receiver.length; i++) {
                        if (!list.contains(receiver[i])) {
                            list.add(receiver[i]);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * 时间格式化成字符串
     */
    public static String dateFormat(Date date, String pattern) {
        if (isNullOrEmpty(pattern)) {
            pattern = DATE_PARSE;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 转换任务阶段
     *
     * @param stage
     * @return
     */
    public static Integer getStage(String stage) {
        final Map<String, Integer> stages = new HashMap();
        stages.put(Dict.TASK_STAGE_CREATE_INFO, 2);
        stages.put(Dict.TASK_STAGE_CREATE_APP, 2);
        stages.put(Dict.TASK_STAGE_CREATE_FEATURE, 2);
        stages.put(Dict.TASK_STAGE_DEVELOP, 3);
        stages.put(Dict.TASK_STAGE_SIT, 4);
        stages.put(Dict.TASK_STAGE_UAT, 5);
        stages.put(Dict.TASK_STAGE_REL, 6);
        stages.put(Dict.TASK_STAGE_PRODUCTION, 7);
        stages.put(Dict.TASK_STAGE_FILE, 8);
        return stages.containsKey(stage) ? stages.get(stage) : -1;
    }

    public static String getLittleTime(String a, String b) {
        if (a.compareTo(b) < 0)
            return a;
        else return b;
    }

    public static String getElderTime(String a, String b) {
        if (a.compareTo(b) > 0)
            return a;
        else return b;
    }
 
    /**
     * 判断date1不大于dete2
     * @param dateStr1 格式 yyyy-mm-dd
     * @param dateStr2 格式 yyyy-mm-dd
     * @return
     */
    public static boolean getJudgementDate(String dateStr1,String dateStr2) throws ParseException {
    	if(isNullOrEmpty(dateStr1) || isNullOrEmpty(dateStr2) ) {
    		return true;
    	}
        SimpleDateFormat sf = new SimpleDateFormat(DATE_PARSE);
        Date date1 = new Date();
    	try {
            date1 = sf.parse(dateStr1);
        }catch (ParseException e){
            return false;
        }

        Date date2 = new Date();
        try {
            date2 = sf.parse(dateStr2);
        }catch (ParseException e){
            return true ;
        }

        int compareTo = date2.compareTo(date1);// 0=相等 1 = 左>右
        if( compareTo >= 0){
            return true; //date1 <= dete2
        }
        return false;//date1 > dete2
    }

    /**
     * 判断 dete2 > date1
     * @param dateStr1 格式 yyyy-mm-dd
     * @param dateStr2 格式 yyyy-mm-dd
     * @return
     */
    public static boolean isDelay(String dateStr1 ,String dateStr2) throws ParseException {
        //实际日期为空 则获取当前时间判断是否大于计划时间
        if(isNullOrEmpty(dateStr2)){
            dateStr2 = TimeUtil.formatToday();//当前时间
        }
        //计划日期为空 默认不延期
        if(isNullOrEmpty(dateStr1) || isNullOrEmpty(dateStr2) ) {
            return false; // 为空默认 date1<dete2
        }

        SimpleDateFormat sf = new SimpleDateFormat(DATE_PARSE);
        Date date1 = new Date();
        try {

            date1 = sf.parse(dateStr1);
        }catch (ParseException e){
            //计划日期规则错误默认不延期
            return false;
        }

        Date date2 = new Date();
        try {
            date2 = sf.parse(dateStr2);
        }catch (ParseException e){
            dateStr2 = TimeUtil.formatToday();
        }finally {
            //实际日期规则错误 赋予当前日期
            date2 = sf.parse(dateStr2);
        }

        int compareTo = date2.compareTo(date1);// 0=相等 1 = 左>右
        if( compareTo > 0 ){
            return true; //date2 > dete1
        }
        return false;//date2 <= dete1
    }

    /**
     * 判断行内行外
     * @param email
     * @return isInt
     */
    public static boolean isInt(String email) {
        boolean isInt= false;//默认行外
        if(!isNullOrEmpty(email) && email.endsWith("com.cn")) {
            isInt = true;//行内
        }
        return isInt;
    }

    /**
     * 过滤问题邮箱 结尾不等于 spdb.com.cn 或 spdbdev.com
     * @param emailList
     * @return isInt
     */
    public static /*List<String>*/ void manageEmailList(List<String> emailList) {

        //List<String> list = new ArrayList<>();
        if(!isNullOrEmpty(emailList)) {
            emailList.removeIf(email -> !email.endsWith("spdbdev.com") && !email.endsWith("spdb.com.cn"));
            //两种写法
            /*list = emailList.stream().filter(email ->
                    email.endsWith("spdbdev.com") || email.endsWith("spdb.com.cn")).collect(Collectors.toList());*/
        }
        //return list;
    }

    /**
     * 判断两位小数
     * @param str
     * @return boolean
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
    public static String fdevApproveState(String approveState) {
        switch (approveState) {
            case Constants.PASS:
                return "通过";
            case Constants.REJECT:
                return "拒绝";
            case Constants.NOSUBMIT:
                return "未提交";
            default:
                return "待审批";//默认待审批
        }
    }
    public static String approveType(String approveType) {
        switch (approveType) {
            case Constants.OVERDUEAPPROVE:
                return "超期审批";
            case Constants.DEVOVERDUE:
                return "开发审批&超期审批";
            default:
                return "开发审批";//默认开发审批
        }
    }

    /**
     * 将url转化成project_url
     *
     * @param url
     * @return project_url
     */
    public static String projectUrl(String url) {
        String project_url = url + Dict.PROJECTS;
        return project_url;
    }
}

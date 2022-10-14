package com.spdb.fdev.fdevtask.base.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TaskReview;
import com.spdb.fdev.fdevtask.spdb.entity.TaskReviewChild;
import io.swagger.annotations.ApiModelProperty;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class CommonUtils implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(ApplicationContextAware.class);

    private static ApplicationContext applicationContext;

    public static final String datastamp = "yyyy-MM-dd";

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }


    /**
     * 判断输入数据是否是空
     *
     * @param obj 输入字符串
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
                if ((obj == null) || (("").equals(((String) obj).trim()))) {
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
            if (obj instanceof Set) {
                Set objList = (Set) obj;
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
            if (obj instanceof JSONObject) {
                JSONObject json = (JSONObject) obj;
                if (json == null || json.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof JSONArray) {
                JSONArray json = (JSONArray) obj;
                if (json == null || json.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof JSONNull) {
                return true;
            }
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotNullOrEmpty(String... param) {
        for (String va : param) {
            if (va == null || "".equals(va)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断实体类中所有的属性是否存在null，存在null返回true
     */
    public static boolean checkObjFieldIsNull(Object obj) throws Exception {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            if (f.get(obj) == null) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    //JSONObject转map
    public static Map converMap(JSONObject json) {

        Map map = new HashMap<>();
        Iterator iterator = json.keySet().iterator();
        while (iterator.hasNext()) {
            String next = (String) iterator.next();
            Object o = json.get(next);
            map.put(next, o);
        }
        return map;
    }

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

    // 日期时间转换
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final String DATE_PATTERN_F1 = "yyyy/MM/dd";

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /* *
     * yyyy-MM-dd 日期转 yyyy/MM/dd
     * */
    public static String dateParse(String time) {
        if (isNullOrEmpty(time)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        SimpleDateFormat sdfx = new SimpleDateFormat(DATE_PATTERN_F1);
        try {
            return sdfx.format(sdf.parse(time));
        } catch (Exception e) {
            return "";
        }
    }

    /* *
     * yyyy/MM/dd 日期转 yyyy-MM-dd
     * */
    public static String dateParse2(String time) {
        if (isNullOrEmpty(time)) {
            return "";
        }
        SimpleDateFormat sdfx = new SimpleDateFormat(DATE_PATTERN);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_F1);
        try {
            if (time.contains("/")) {
                return sdfx.format(sdf.parse(time));
            } else {
                return time;
            }
        } catch (Exception e) {
            return time;
        }
    }

    /**
     * 时间格式化成字符串
     */
    public static String dateFormat(Date date, String pattern) {
        if (!isNotNullOrEmpty(pattern)) {
            pattern = DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 字符串格式成日期
     *
     * @throws Exception
     */
    public static Date dateParse(String dateTimeString, String pattern) throws Exception {
        if (!isNotNullOrEmpty(pattern)) {
            pattern = DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateTimeString);
    }

    /**
     * 下划线转驼峰
     */
    public static String toCamlCase(String line, boolean smallCamel) {
        if (line == null || "".endsWith(line)) {
            return line;
        }
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(smallCamel && matcher.start() == 0 ? Character.toLowerCase(word.charAt(0))
                    : Character.toUpperCase(word.charAt(0)));
            int index = word.lastIndexOf('_');
            if (index > 0) {
                sb.append(word.substring(1, index).toLowerCase());
            } else {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰转下划线
     */

    public static String toLine(String str) {
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // json对象过滤
    public static JSONObject filter(JSONObject datas) {
        JSONObject temp = new JSONObject();
        Iterator<Object> da = datas.keys();
        while (da.hasNext()) {
            String ki = (String) da.next();
            Object vi = datas.get(ki);

            if (null == vi || vi instanceof JSONNull) {
                temp.put(ki, "");
            } else if (vi instanceof String) {
                try {
                    JSONObject str = JSONObject.fromObject(vi);
                    temp.put(ki, filter(str));
                } catch (Exception e) {
                    temp.put(ki, vi);
                }
            } else if (vi instanceof JSONArray) {
                temp.put(ki, filter((JSONArray) vi));
            } else if (vi instanceof JSONObject) {
                temp.put(ki, (filter((JSONObject) vi)));
            } else {
                temp.put(ki, vi);
            }
        }
        return temp;
    }

    // json数组过滤
    public static JSONArray filter(JSONArray datas) {
        if (null == datas) {
            return null;
        }
        JSONArray result = new JSONArray();
        for (Object da : datas) {
            if (null == da) {
                continue;
            }
            if (da instanceof String) {
                result.add(da);
            }
            if (da instanceof JSONArray) {
                result.add(filter((JSONArray) da));
            }
            if (da instanceof JSONObject) {
                result.add(filter((JSONObject) da));
            }
        }
        return result;
    }

    public static <T> T mapToBean(Map map, Class<T> cls) throws Exception {
        JSONObject json = JSONObject.fromObject(map);
        return jsonToBean(json, cls);
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
//        JSONObject json = JSONObject.fromObject(cls);
//        return (Map)json;
    }

    //多个数组合并一个数组
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        ArrayList<T[]> list = new ArrayList();
        for (T[] array : rest) {
            if (array != null && array.length > 0) {
                list.add(array);
            } else {
                continue;
            }
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : list) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static Integer pareStage(String stage) {
        final Map<String, Integer> stages = new HashMap();
        stages.put(Dict.TASK_STAGE_CREATE_INFO, 1);
        stages.put(Dict.TASK_STAGE_CREATE_APP, 2);
        stages.put(Dict.TASK_STAGE_CREATE_FEATURE, 3);
        stages.put(Dict.TASK_STAGE_DEVELOP, 4);
        stages.put(Dict.TASK_STAGE_SIT, 5);
        stages.put(Dict.TASK_STAGE_UAT, 6);
        stages.put(Dict.TASK_STAGE_REL, 7);
        stages.put(Dict.TASK_STAGE_PRODUCTION, 8);
        stages.put(Dict.TASK_STAGE_FILE, 9);
        stages.put(Dict.TASK_STAGE_ABORT, 10);
        stages.put(Dict.TASK_STAGE_DISCARD, 11);
        return stages.containsKey(stage) ? stages.get(stage) : -1;
    }


    public static Date getLastWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -7);
        return c.getTime();
    }

    public static List<String> getLastSixWeek(Date date) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_F1);
        for (int i = 0; i <= 5; i++) {
            dates.add(simpleDateFormat.format(date));
            date = getLastWeek(date);
        }
        return dates;
    }

    /**
     * 校验应用英文名
     *
     * @param appName
     * @return
     */
    public static boolean validateAppName(String appName) {
        if (isNullOrEmpty(appName))
            return false;
        Pattern humpPattern = Pattern.compile("^[a-z][a-z0-9-]*[a-z]$");
        Matcher matcher = humpPattern.matcher(appName);
        if (!matcher.find()) {
            return false;
        }
        int count = 0;
        Pattern pattern = Pattern.compile("-");
        matcher = pattern.matcher(appName);
        while (matcher.find()) {
            count++;
        }
        if (count != 2 || appName.contains("--")) {
            return false;
        }
        return true;
    }

    /**
     * 判断日期是否改变
     *
     * @param newTask
     * @param oldTask
     * @return
     */

    public static Map taskDateChanged(FdevTask newTask, FdevTask oldTask) {
        Map<String, String> timeMapNew = new HashMap<>();
        timeMapNew.put(Dict.PLAN_START_TIME, newTask.getPlan_start_time());
        timeMapNew.put(Dict.PLAN_INNER_TEST_TIME, newTask.getPlan_inner_test_time());
        timeMapNew.put(Dict.PLAN_UAT_TEST_START_TIME, newTask.getPlan_uat_test_start_time());
        timeMapNew.put(Dict.PLAN_UAT_TEST_STOP_TIME, newTask.getPlan_uat_test_stop_time());
        timeMapNew.put(Dict.PLAN_REL_TEST_TIME, newTask.getPlan_rel_test_time());
        timeMapNew.put(Dict.PLAN_FIRE_TIME, newTask.getPlan_fire_time());
        Map<String, String> timeMapOld = new HashMap<>();
        timeMapOld.put(Dict.PLAN_START_TIME, oldTask.getPlan_start_time());
        timeMapOld.put(Dict.PLAN_INNER_TEST_TIME, oldTask.getPlan_inner_test_time());
        timeMapOld.put(Dict.PLAN_UAT_TEST_START_TIME, oldTask.getPlan_uat_test_start_time());
        timeMapOld.put(Dict.PLAN_UAT_TEST_STOP_TIME, oldTask.getPlan_uat_test_stop_time());
        timeMapOld.put(Dict.PLAN_REL_TEST_TIME, oldTask.getPlan_rel_test_time());
        timeMapOld.put(Dict.PLAN_FIRE_TIME, oldTask.getPlan_fire_time());
        Map result = new HashMap();
        List dateList = Arrays.asList(Dict.PLAN_REL_TEST_TIME, Dict.PLAN_INNER_TEST_TIME, Dict.PLAN_FIRE_TIME, Dict.PLAN_START_TIME,
                Dict.PLAN_UAT_TEST_START_TIME, Dict.PLAN_UAT_TEST_STOP_TIME);
        for (Object n : dateList) {
            if ((null != timeMapOld.get(n) && null != timeMapNew.get(n) && !timeMapOld.get(n).equals(timeMapNew.get(n)))
                    || (null == timeMapOld.get(n) && null != timeMapNew.get(n))) {
                result.put(n, timeMapNew.get(n));
            }
        }
        return result;
    }

    public static boolean isBetweenDates(String date, String startDate, String endDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_F1);
        Date parseDate = simpleDateFormat.parse(date);
        Date parseStartDate;
        Date parseEndDate;
        if (!isNullOrEmpty(startDate) && !isNullOrEmpty(endDate)) {
            parseStartDate = simpleDateFormat.parse(startDate);
            parseEndDate = simpleDateFormat.parse(endDate);
            if (date.equals(startDate) || date.equals(endDate)) {
                return true;
            }
            return parseDate.after(parseStartDate) && parseDate.before(parseEndDate);
        } else if (isNullOrEmpty(startDate) && !isNullOrEmpty(endDate)) {
            if (date.equals(endDate)) {
                return true;
            }
            parseEndDate = simpleDateFormat.parse(endDate);
            return parseDate.before(parseEndDate);
        } else if (!isNullOrEmpty(startDate) && isNullOrEmpty(endDate)) {
            if (date.equals(startDate)) {
                return true;
            }
            parseStartDate = simpleDateFormat.parse(startDate);
            return parseDate.after(parseStartDate);
        } else {
            return true;
        }
    }

    //获取task对象字段注解
    public static List getTaskAnnotationValue() {
        List values = new ArrayList();
        Field[] fields = FdevTask.class.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                if (field.isAnnotationPresent(ApiModelProperty.class)) {
                    ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
                    String value = apiModelProperty.value();
                    String str = field.getName();
                    if (!Dict.ID.equals(str)) {
                        values.add(value);
                    }
                }
            }
        }
        return values;
    }

    //
    public static Map obj2Map(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj == null)
            return map;
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj) != null ? field.get(obj) : null);
            }
        } catch (Exception e) {

        }
        return map;
    }


    //只对要发邮件的部分做了比较
    public static boolean taskEquals(Object ol, Object ne) {
        if (ol == ne) return true;
        if (isNullOrEmpty(ol) && isNullOrEmpty(ne)) return true;
        if (isNullOrEmpty(ol) && !isNullOrEmpty(ne)) return false;
        if (!isNullOrEmpty(ol) && isNullOrEmpty(ne)) return false;
        Field[] fields = FdevTask.class.getDeclaredFields();
        try {
            if (fields != null) {
                for (Field field : fields) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    switch (field.getName()) {
                        case Dict.STAGE:
                        case Dict.REDMINE_ID:
                        case Dict.CREATOR:
                        case Dict.DESC:
                        case Dict.PLAN_START_TIME:
                        case Dict.PLAN_INNER_TEST_TIME:
                        case Dict.PLAN_UAT_TEST_START_TIME:
                        case Dict.PLAN_UAT_TEST_STOP_TIME:
                        case Dict.PLAN_REL_TEST_TIME:
                        case Dict.PLAN_FIRE_TIME:
                            Object o = field.get(ol);
                            Object n = field.get(ne);
                            if (!compareString(o, n)) return false;
                            break;
                        case Dict.MASTER:
                        case Dict.TESTER:
                        case Dict.SPDB_MASTER:
                        case Dict.DEVELOPER:
                            String[] o1 = (String[]) field.get(ol);
                            String[] n1 = (String[]) field.get(ne);
                            if (!Arrays.equals(o1, n1)) return false;
                            break;
                        case Dict.REVIEW:
                            TaskReview r1 = (TaskReview) field.get(ol);
                            TaskReview r2 = (TaskReview) field.get(ne);
                            // 都不为null
                            if (r1 != null && r2 != null) {
                                Field[] reviewFields = new Field[2];
                                reviewFields[0] = TaskReview.class.getDeclaredField("other_system");
                                reviewFields[1] = TaskReview.class.getDeclaredField("data_base_alter");
                                if (reviewFields != null) {
                                    for (Field reviewField : reviewFields) {
                                        if (!reviewField.isAccessible()) {
                                            reviewField.setAccessible(true);
                                        }
                                        TaskReviewChild[] tmp1 = (TaskReviewChild[]) reviewField.get(r1);
                                        TaskReviewChild[] tmp2 = (TaskReviewChild[]) reviewField.get(r2);
                                        // 都不为null
                                        if (tmp1 != null && tmp2 != null) {
                                            int length = tmp1.length;
                                            if (tmp2.length != length) {
                                                return false;
                                            }
                                            for (int i = 0; i < length; i++) {
                                                TaskReviewChild obj1 = tmp1[i];
                                                TaskReviewChild obj2 = tmp2[i];
                                                if (!(obj1.getName() == null ? obj2.getName() == null : obj1.getName().equals(obj2.getName())))
                                                    return false;
                                            }
                                        }
                                        // 至少一个null
                                        if (tmp1 == tmp2) {
                                            continue;
                                        }
                                        if ((tmp1 == null && tmp2 != null) || (tmp1 != null && tmp2 == null)
                                        ) return false;
                                    }
                                }
                            }
                            // 至少一个null
                            if (r1 == r2) {
                                break;
                            }
                            if ((r1 == null && r2 != null) || (r1 != null && r2 == null)
                            ) return false;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return true;
    }

    private static boolean compareString(Object o, Object n) {
        if (o == n) {
            return true;
        }
        if (!(o == null ? n == null : o.equals(n))) {
            return false;
        }
        return true;
    }

    public static String dateCompare(String plan, String real) {
        //        2018/05/31
        String result = "";
        if (isNotNullOrEmpty(plan, real)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_F1);
                long planTime = sdf.parse(plan).getTime();
                long realTime = sdf.parse(real).getTime();
                if (planTime - realTime < 0) {
                    long diff = realTime - planTime;
                    long day = diff / (24 * 60 * 60 * 1000);
                    result = String.valueOf((int) day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static int taskDateCompare(String plan, String real) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_F1);
        int result = 0;
        if (isNullOrEmpty(real)){
            real = sdf.format(new Date());
        }
        if (isNotNullOrEmpty(plan)) {
            try {
                long planTime = sdf.parse(plan).getTime();
                long realTime = sdf.parse(real).getTime();
                if (planTime - realTime < 0) {
                    long diff = realTime - planTime;
                    long day = diff / (24 * 60 * 60 * 1000);
                    result = (int)day;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String[] distinctArray(String[] arr) {
        Set set = new HashSet(Arrays.asList(arr));
        String[] result = (String[]) set.toArray(new String[set.size()]);
        return result;
    }

    public static String typeMapping(String type) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(Dict.TYPE_MAP);
        return resourceBundle.getString(type);
    }

    /**
     * 合并相同结构的Map
     * 支持 key为int map list string(int 值)
     *
     * @param map1
     * @param map2
     * @return
     */
    public static Map mergeMap(Map map1, Map map2) {
        Map result = new HashMap();
        if (isNullOrEmpty(map1)) {
            return map2;
        }
        if (isNullOrEmpty(map2)) {
            return map1;
        }
        try {
            for (Object k : map1.keySet()) {
                Object v1 = map1.get(k);
                Object v2 = map2.get(k);
                if (v1 instanceof Integer || v1 instanceof String) {
                    result.put(k, Integer.valueOf((String) v1) + Integer.valueOf((String) v2));
                }
                if (v1 instanceof List) {
                    result.put(k, ((List) v1).addAll((List) v2));
                }
                if (v1 instanceof Map) {
                    ((Map) v1).putAll((Map) v2);
                    result.put(k, v1);
                }
            }
        } catch (Exception e) {
            return map1;
        }
        return result;
    }

    public static String encode(String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(token.getBytes());
        String encodeToken = new String(encode);
        return encodeToken;
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

    public static String minStage(List stageList) {
        stageList.sort(Comparator.comparingInt(CommonUtils::pareStage));
        return (String) stageList.get(0);
    }

    public static String minStage(String stage1, String stage2) {
        List<String> stageList = new ArrayList<>();
        stageList.add(stage1);
        stageList.add(stage2);
        return minStage(stageList);
    }

    /*正常状态*/
    public static List getNoAbortAndNoFile() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        stagelist.add(Dict.TASK_STAGE_PRODUCTION);
        return stagelist;
    }

    /*非删除阶段*/
    public static List getNoAbortList() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        stagelist.add(Dict.TASK_STAGE_PRODUCTION);
        stagelist.add(Dict.TASK_STAGE_FILE);
        return stagelist;
    }

    /*进行中阶段*/
    public static List getGoingList() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        return stagelist;
    }


    //非删除、非待实施
    public static List getNoAbortTodoList() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        stagelist.add(Dict.TASK_STAGE_PRODUCTION);
        stagelist.add(Dict.TASK_STAGE_FILE);
        return stagelist;
    }

    //实施阶段
    public static List getWaitTodoList() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        return stagelist;
    }

    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8").replaceAll("%", "a");
        } catch (UnsupportedEncodingException e) {
            logger.error("文件名转码失败", e.getMessage());
        }
        return url;
    }
    
    /*测试状态*/
    public static List getTestTask() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        return stagelist;
    }

    /**
     * 类型转换
     */
    public static <T> List<T> castList(Object object, Class<T> clazz){
        List<T> result = new ArrayList<T>();
        if (object == null ){
            return result;
        }
        if(object instanceof List<?>){
            for (Object o:(List<?>)object) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return result;
    }

    public static String getJiraTaskStatus(Integer fdevTaskStatus){
        if(fdevTaskStatus < 4){
            return Constants.WAIT_HANDLE;
        }
        if(fdevTaskStatus < 5){
            return Constants.IN_HAND;
        }
        if(fdevTaskStatus < 7){
            return Constants.TESTING;
        }
        if(fdevTaskStatus < 10){
            return Constants.DONE;
        }
        return Constants.WAIT_HANDLE;
    }

    /**
     * 获取相差天数
     * @param date1
     * @param date2
     * @return
     */
    public static int getDiffDays(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PATTERN);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        try {
            calendar1.setTime(sdf.parse(date1));
            calendar2.setTime(sdf.parse(date2));
        }catch (ParseException e) {
            throw new FdevException(ErrorConstants.DATE_FROMAT_ERROR);
        }
        long diffTime = Math.abs(calendar1.getTimeInMillis() - calendar2.getTimeInMillis());
        int diffDays = (int) (diffTime/1000/60/60/24);
        return diffDays;
    }

    /**
     * 判断是否数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if (CommonUtils.isNullOrEmpty(str))
            return false;
        Pattern humpPattern = Pattern.compile("^[\\d]+$");
        Matcher matcher = humpPattern.matcher(str);
        if (!matcher.find()) {
            return false;
        }
        return true;
    }

    /**
     * 获取应用类型英文名
     * @param typeName
     * @return
     */
    public static String getTypeNameEN(String typeName) {
        switch (typeName) {
            case "Vue应用" :
                return Constants.APPLICATION_VUE;
            case "IOS应用" :
                return Constants.APPLICATION_IOS;
            case "Android应用" :
                return Constants.APPLICATION_ANDROID;
            case "Java微服务" :
                return Constants.APPLICATION_JAVA;
            case "容器化项目" :
                return Constants.APPLICATION_DOCKER;
            case "老版服务" :
                return Constants.APPLICATION_OLDSERVICE;
            default:
                logger.info(">>>>>>应用类型不正确{}", typeName);
                return typeName;
        }
    }

    /**
     * 获取应用类型中文名
     * @param applicationType
     * @return
     */
    public static String getTypeNameCN(String applicationType) {
        switch (applicationType) {
            case Constants.APPLICATION_VUE :
                return "Vue应用";
            case Constants.APPLICATION_IOS :
                return "IOS应用";
            case Constants.APPLICATION_ANDROID :
                return "Android应用";
            case Constants.APPLICATION_JAVA :
                return "Java微服务";
            case Constants.APPLICATION_DOCKER :
                return "容器化项目";
            case Constants.APPLICATION_OLDSERVICE :
                return "老版服务";
            case Constants.COMPONENT_WEB :
                return "前端组件";
            case Constants.COMPONENT_SERVER :
                return "后端组件";
            case Constants.ARCHETYPE_WEB :
                return "前端骨架";
            case Constants.ARCHETYPE_SERVER :
                return "后端骨架";
            case Constants.IMAGE :
                return "镜像";
            default:
                logger.info(">>>>>>应用类型不正确{}", applicationType);
                return applicationType;
        }
    }

    /**
     * 判断关联项目是否是否应用
     * @param applicationType
     * @return
     */
    public static boolean isApp(String applicationType) {
        if(CommonUtils.isNullOrEmpty(applicationType)) {
            return false;
        }
        if(applicationType.startsWith("app")) {
            return true;
        }
        return false;
    }

    /**
     * 是否组件
     * @param applicationType
     * @return
     */
    public static boolean isComponent(String applicationType) {
        if(Constants.COMPONENT_SERVER.equals(applicationType)
                || Constants.COMPONENT_WEB.equals(applicationType)) {
            return true;
        }
        return false;
    }

    /**
     * 是否骨架
     * @param applicationType
     * @return
     */
    public static boolean isArchetype(String applicationType) {
        if(Constants.ARCHETYPE_SERVER.equals(applicationType)
                || Constants.ARCHETYPE_WEB.equals(applicationType)) {
            return true;
        }
        return false;
    }

    /**
     * 获取投产窗口类型
     * @param applicationType 1：微服务窗口2：原生窗口3：试运行窗口 4：组件 5：骨架 6：镜像
     * @return
     */
    public static String getReleaseType(String applicationType) {
        if(CommonUtils.isNullOrEmpty(applicationType)) {
            return null;
        }
        if(Constants.APPLICATION_ANDROID.equals(applicationType)
                || Constants.APPLICATION_IOS.equals(applicationType)) {
            return "2";
        }else if(isComponent(applicationType)) {
            return "4";
        }else if(isArchetype(applicationType)) {
            return "5";
        }else if(Constants.IMAGE.equals(applicationType)) {
            return "6";
        }else{
            return "1";
        }
    }
    public static Date parseDateStr(String dateStr) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(datastamp);
        return sf.parse(dateStr);
    }

    /**
     * 日期加 day天 yyyy-MM-dd
     *
     * @return
     */
    public static String yesterday(Date date,int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + day);
        return new SimpleDateFormat(datastamp).format(calendar.getTime());
    }

    /**
     * 导出数据到excel表格
     * @param response
     * @param dataList
     * @param columnMap
     * @param fileName
     * @throws IOException
     */
    public static void exportDataToExcel(HttpServletResponse response, List<Map> dataList, LinkedHashMap<String, String> columnMap, String fileName) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            //创建sheet页
            Sheet sheet = workbook.createSheet("sheet1");
            //创建表头
            int excelRow = 0;
            Row titleRow = sheet.createRow(excelRow++);
            //设置表头
            List<String> titleList = new ArrayList<>(columnMap.values());
            for (int i = 0; i < titleList.size(); i++) {
                //创建单元格
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(titleList.get(i));
            }
            //所取字段key集合
            List<String> keys = new ArrayList<>(columnMap.keySet());
            Map<String, Object> data;
            Row dataRow;
            for (int i = 0; i < dataList.size(); i++) {
                data = dataList.get(i);
                //一条数据创建一行
                dataRow = sheet.createRow(excelRow++);
                Cell cell;
                String text;
                String key;
                for (int j = 0; j < keys.size(); j++) {
                    //一个字段创建一个单元格
                    cell = dataRow.createCell(j);
                    key = keys.get(j);
                    if ((key.equals(Dict.SIT) || key.equals(Dict.UAT) || key.equals(Dict.REL_LOWERCASE)) && data.get(key) == null) {
                        text = "0";
                    } else {
                        text = String.valueOf(data.get(key));
                    }
                    cell.setCellValue(text);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8") +".xlsx");
        workbook.write(response.getOutputStream());
    }
}

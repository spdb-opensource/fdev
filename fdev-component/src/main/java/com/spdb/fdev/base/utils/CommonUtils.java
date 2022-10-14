
package com.spdb.fdev.base.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
public class CommonUtils {
    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String INPUT_DATE = "yyyy/MM/dd";
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    @Autowired
    private  UserVerifyUtil userVerifyUtilClass;
    private static CommonUtils commonUtils;

    @PostConstruct
    public void init(){
        commonUtils = this;
        commonUtils.userVerifyUtilClass = this.userVerifyUtilClass;
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
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public static User getSessionUser() throws Exception {
        User user = commonUtils.userVerifyUtilClass.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));

        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
    }


    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
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


    /**
     * 生成redisson锁key
     *
     * @param objectUniqueKey
     * @return
     */
    public static String generateRlockKey(String objectUniqueKey) {
        return new StringBuilder(Constants.REDISSON_LOCK_NAMESPACE).append(":").append(objectUniqueKey).toString();
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
     * 将url转化成project_url
     *
     * @param url
     * @return project_url
     */
    public static String projectUrl(String url) {
        String project_url = url + Dict.PROJECTS;
        return project_url;
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
     * JSON处理
     *
     * @param result
     * @return
     * @throws IOException
     */
    public static String getPipId(Object result) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = JSONObject.fromObject(result);
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        return String.valueOf(merge.get(Dict.ID));
    }

    /**
     * 将配置文件参数转为list
     */
    public static List convertStringToList(String param) {
        String[] exceptArray = param.split(",");
        List<String> exceptList = Arrays.asList(exceptArray);
        return exceptList;
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

    /**
     * 解析 配置模板 的数据信息
     *
     * @param content
     * @return
     */
    public static void checkKeyIsEquals(String content) {
        content = content.replace("\r", "");
        String[] split = content.split("\n");
        List<String> result = new ArrayList<>();
        for (String line : split) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] lineSplit = line.split("=", 2);
            if (lineSplit.length <= 1) {
                continue;
            }
            result.add(lineSplit[0]);
        }
        Set<String> repeat = checkRepeat(result);
        if (null != repeat && !repeat.isEmpty()) {
            throw new FdevException(ErrorConstants.DEPLOY_TEMPLATE_SAVE_ERROR, new String[]{"左边的key" + repeat + "不能重复！"});
        }
    }

    /**
     * 检查list有哪些重复元素
     *
     * @param list
     * @return
     */
    public static Set<String> checkRepeat(List<String> list) {
        Set<String> set = new HashSet<>();
        Set<String> repeat = new HashSet<>();
        for (String string : list) {
            if (!set.add(string)) {
                repeat.add(string);
            }
        }
        return repeat;
    }

    /**
     * 替换url的关键字
     *
     * @param source
     * @return
     */
    public static String replateGitKey(String source) {
        if (StringUtils.isNotBlank(source)) {
            if (source.contains("."))
                source = source.replaceAll("\\.", "%2E");
            if (source.contains("/")) {
                source = source.replaceAll("/", "%2F");
            }
        }
        return source;
    }

    /**
     * list判断空
     *
     * @param list
     * @return
     */
    public static boolean listIsNullOrEmpty(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }


    /**
     * 去空，去serialVersionUID，去_id
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List formatList(List<T> list) {
        List<Map> result = new ArrayList<>();
        try {
            if (!isNullOrEmpty(list)) {
                for (T t : list) {
                    result.add(object2Map(t));
                }
                return result;
            }
        } catch (Exception e) {
            logger.error("类型转换失败" + e.getMessage());
            return list;
        }
        return list;
    }

    /**
     * 人员校验
     *
     * @param userId
     * @param set
     * @return
     */
    public static boolean checkUser(String userId, HashSet<Map<String, String>> set) {
        if (set != null & set.size() > 0) {
            for (Map map : set) {
                String id = (String) map.get(Dict.ID);
                if (userId.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取骨架管理员的所有id
     *
     * @param set
     * @return
     */
    public static Set<String> getManageIds(HashSet<Map<String, String>> set) {
        Set<String> result = new HashSet<>();
        if (!CommonUtils.isNullOrEmpty(set)) {
            for (Map map : set) {
                String id = (String) map.get(Dict.ID);
                result.add(id);
            }
        }
        return result;
    }

    /**
     * 获取近6周时间
     */
    public static List<String> getLastSixWeek(Date date) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PARSE);
        for (int i = 0; i <= 5; i++) {
            if (i == 0) {
                Calendar instance = Calendar.getInstance();
                instance.setTime(date);
                instance.add(Calendar.DAY_OF_MONTH, 1);
                Date time = instance.getTime();
                dates.add(simpleDateFormat.format(time));
                date = getLastWeek(date);
                continue;
            }
            dates.add(simpleDateFormat.format(date));
            date = getLastWeek(date);
        }
        return dates;
    }

    public static Date getLastWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -7);
        return c.getTime();
    }

    public static String dateCompare(String plan, String real) {
        String result = "";
        if (isNotNullOrEmpty(plan, real)) {
            try {
                //当前时间
                long now = new Date().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PARSE);
                SimpleDateFormat sdfs = new SimpleDateFormat(INPUT_DATE);
                long planTime = sdfs.parse(plan).getTime();
                long realTime = sdf.parse(real).getTime();
                if (planTime - realTime < 0) {
                    long diff = now - planTime;
                    long day = diff / (24 * 60 * 60 * 1000);
                    result = String.valueOf((int) day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
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
     * 时间格式化成字符串
     */
    public static String dateFormat(Date date, String pattern) {
        if (!isNotNullOrEmpty(pattern)) {
            pattern = DATE_PARSE;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 获取当前时间的前一天
     */
    public static String getPreviousTime(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        Date parse = simpleDateFormat.parse(date);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date newTime = instance.getTime();
        date = simpleDateFormat.format(newTime);
        return date;
    }

    /**
     * 获取当前时间的下一天
     */
    public static String getTomorrowTime(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        Date parse = simpleDateFormat.parse(date);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) + 1);
        Date time = instance.getTime();
        String endTime = simpleDateFormat.format(time);
        return endTime;
    }
}

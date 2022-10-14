package com.spdb.fdev.base.utils;


import com.google.common.collect.Maps;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@Component
public class CommonUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public CommonUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

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

    /**
     * 解析模版json文件
     *
     * @param templatePath
     * @return
     */
    public static String getTemplateJson(String templatePath) {
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (Exception e) {
            logger.error("解析{}文件出错，{}！", templatePath, e.getMessage());
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"解析" + templatePath + "文件出错！"});
        }
        return stringBuilder.toString();
    }

    public static final String DATE_FORMATE = "yyyy/MM/dd";

    public static String dateFormate(Date date, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    public static Date dateFormate1(String string) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(string);
    }

    public static String dateFormate2(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
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

    /**
     * base64加密
     **/
    public static String encode(String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(token.getBytes());
        String encodeToken = new String(encode);
        return encodeToken;
    }

    /**
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public User getSessionUser() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        return (User)this.redisTemplate.opsForValue().get("_USER" + token);
    }


    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    /**
     * 将对象转换成map
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * 判断date1不大于dete2
     * @param date1 格式 yyyy-mm-dd
     * @param date2 格式 yyyy-mm-dd
     * @return
     */
    public static boolean getJudgementDate(String date1,String date2) {
        if(isNullOrEmpty(date1) || isNullOrEmpty(date2) ) {
            return true;
        }
        //截取当前日期年份
        int year1 = Integer.valueOf(date1.substring(0, 4));
        int year2 = Integer.valueOf(date2.substring(0, 4));
        //截取当前日期月份
        int moon1 = Integer.valueOf(date1.substring(5, 7));
        int moon2 = Integer.valueOf(date2.substring(5, 7));
        //截取当前日期
        int data1 = Integer.valueOf(date1.substring(8, 10));
        int data2 = Integer.valueOf(date2.substring(8, 10));
        if(year1 < year2) {
            return true;
        }else if(year1 == year2 && moon1 < moon2) {
            return true;
        }else if(year1 == year2 && moon1 == moon2 && data1<=data2) {
            return true;
        }else {
            return false;
        }

    }
    
    /**
     * 获取当天日期 yyyy-MM-dd
     * @return
     */
    public static String getToday(){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        return sf.format(new Date());
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
    
    public static String getDateYYYYMMdd(String date) {
    	if(!isNullOrEmpty(date)) {
    		return date.substring(0,10);
    	}
    	return date;
    }
    public static long countDay(String date1, String date2) throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date parse1 = sf.parse(date1);
        Date parse2 = sf.parse(date2);
        long date = (parse1.getTime() - parse2.getTime()) / (60 * 60 * 24 * 1000);
        return date;
    }
    
    /**
     * 拼接字符串
     * @param list 字符串集合
     * @param link 初始字符
     * @param condition 拼接条件 默认“，”号
     * @return
     */
    public static String getCollectionToString(List<String> list,String link,String condition) {
    	if(CommonUtil.isNullOrEmpty(list)) {
    		return link;
    	}
    	if(CommonUtil.isNullOrEmpty(condition)) {
    		condition = ",";
    	}
    	StringBuilder returnStringBuilder = new StringBuilder();
    	boolean firstFlag = true;
    	if(!CommonUtil.isNullOrEmpty(link)) {
    		returnStringBuilder.append(link);
    		firstFlag = false;
    	}
    	Set<String> leaderSet = new HashSet(list);
    	for(String str : leaderSet) {
    		if(firstFlag) {
    			returnStringBuilder.append(str);
    			firstFlag = false;
    		}else {
    			returnStringBuilder.append(condition).append(str);
    		}
    	}
    	return returnStringBuilder.toString();
    }

    /**
     * 将map转为实体对象
     *
     * @param map   需要被转换的map实例
     * @param clazz 实体类的Class对象
     * @return
     * @author xxx
     */
    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        Object obj = null;
        try {
            obj = clazz.newInstance();
            if (map.containsKey(Dict.SERIALVERSIONUID))
                map.remove(Dict.SERIALVERSIONUID);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(obj, entry.getValue());
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
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
     * 从文件内容里解析出引用的实体属性
     *
     * @param fileContent
     * @return
     */
    public static String getModelField(String fileContent) {
        if (StringUtils.isEmpty(fileContent)) {
            new ArrayList<>();
        }
        if (fileContent.contains("\r\n")) {
            fileContent = fileContent.replace("\r\n", "\n");
        }
        String[] fileContentSplit = fileContent.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : fileContentSplit) {
            if (isReplaceContent(line)) {
                result.append(getContent(line));
            }
        }
        return result.toString();
    }

    private static StringBuilder getContent(String line) {
        StringBuilder result = new StringBuilder();
        char[] chars = line.toCharArray();
        List<Integer> resultFirst = new ArrayList<>();
        List<Integer> resultLast = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            if ('$' == chars[i] && '<' == chars[i + 1]) {
                // 得到'<'的下标
                resultFirst.add(i + 1);
            }
            if ('>' == chars[i]) {
                // 得到'>'的下标
                resultLast.add(i);
            }
        }
        List<List<Integer>> comData = combinationNum(resultFirst, resultLast);
        comData.forEach(data -> result.append(line, data.get(0) + 1, data.get(1)).append("\n"));
        return result;
    }

    /**
     * 判断该行是否包括占位符“$<>”
     *
     * @param line
     * @return
     */
    private static boolean isReplaceContent(String line) {
        boolean flag = false;
        if (StringUtils.isNotEmpty(line) && line.contains("$<") && line.contains(">")) {
            flag = true;
        }
        return flag;
    }

    /**
     * 将出现"$<"的'<'下标和出现的'>'下标进行两两组合，如[[2,5],[8,13]]
     *
     * @param resultFirst
     * @param resultLast
     * @return
     */
    private static List<List<Integer>> combinationNum(List<Integer> resultFirst, List<Integer> resultLast) {
        List<List<Integer>> resultList = new ArrayList<>();
        for (int i = 0; i < resultFirst.size(); i++) {
            for (int j = 0; j < resultLast.size(); j++) {
                if (i + 1 >= resultFirst.size() && resultFirst.get(i) < resultLast.get(j)) {
                    List<Integer> list = new ArrayList<>();
                    list.add(resultFirst.get(i));
                    list.add(resultLast.get(j));
                    resultList.add(list);
                    break;
                }
                if (resultFirst.get(i) < resultLast.get(j) && resultLast.get(j) < resultFirst.get(i + 1)) {
                    List<Integer> list = new ArrayList<>();
                    list.add(resultFirst.get(i));
                    list.add(resultLast.get(j));
                    resultList.add(list);
                    break;
                }
            }
        }
        return resultList;
    }

    /**
     * 判断文件的内容是否 有实质内容 注释，为空
     *
     * @param content
     */
    public static boolean checkFileContentIsNone(String content) {
        String[] contentSplit = content.split("\n");
        if (isNullOrEmpty(content))
            return true;
        for (int i = 0; i < contentSplit.length; i++) {
            if (isNullOrEmpty(contentSplit[i]) || contentSplit[i].startsWith("#"))
                continue;
            else
                return false;
        }
        return true;
    }

}


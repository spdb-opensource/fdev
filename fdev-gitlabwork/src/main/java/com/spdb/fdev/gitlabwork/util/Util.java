package com.spdb.fdev.gitlabwork.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Util
 * <p>
 * 工具类
 *
 * @blame Android Team
 */
public class Util {

    /**
     * json字符串转list集合
     *
     * @param body
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> stringToList(String body) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(body);
        if (jsonArray.length() > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = objectMapper.readValue(jsonObject.toString(), Map.class);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * json字符串转map
     *
     * @param body
     * @return
     */
    public static Map<String, Object> stringToMap(String body) throws IOException {
        Map<String, Object> map = new HashMap<>();
        JSONArray jsonArray = new JSONArray(body);
        if (jsonArray.length() > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                map = objectMapper.readValue(jsonObject.toString(), Map.class);
            }
        }
        return map;
    }

    public static <T> T mapToBean(Map map, Class<T> cls) throws Exception {
        net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(map);
        return jsonToBean(json, cls);
    }

    public static <T> T jsonToBean(net.sf.json.JSONObject json, Class<T> cls) throws Exception {
        net.sf.json.JSONObject beanJson = new net.sf.json.JSONObject();

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
        return (T) net.sf.json.JSONObject.toBean(beanJson, cls);
    }

    /**
     * 日期格式化
     *
     * @param pattern
     * @return
     */
    public static SimpleDateFormat simpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }


    /**
     * http请求工具，通过gitlab接口获取相关信息
     *
     * @param url
     * @return
     */
    public static String httpMethodGetExchange(String url, RestTemplate restTemplate, String gitlabToken) {
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Private-Token", gitlabToken);
        //http body配置
        /*MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("search", "CommonModules");
        HttpEntity httpEntity = new HttpEntity(httpHeaders，multiValueMap);*/
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> result;
        String body;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            body = result.getBody();
        } catch (Exception e) {
            return null;
        }
        return body;
    }

    public static String httpMethodGetExchangeWithOutTryCatch(String url, RestTemplate restTemplate, String gitlabToken) {
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Private-Token", gitlabToken);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return result.getBody();
    }

    /**
     * RestTemplate的post方法复用
     * 调用Fdev用户模块和应用模块接口，获取信息
     *
     * @param url
     * @param restTemplate
     * @return
     */
    public static com.alibaba.fastjson.JSONObject httpPostForObject(String url, RestTemplate restTemplate) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("source", "back");
        Map<String, String> param = new HashMap<>();
        HttpEntity<Object> request = new HttpEntity<>(param, httpHeaders);
        com.alibaba.fastjson.JSONObject jsonObject = restTemplate.postForObject(url, request, com.alibaba.fastjson.JSONObject.class);
        return jsonObject;
    }

    /**
     * RestTemplate的post方法复用
     * 调用Fdev用户模块和应用模块接口，获取信息
     *
     * @param url
     * @param restTemplate
     * @return
     */
    public static com.alibaba.fastjson.JSONObject httpPostForObject(String url, RestTemplate restTemplate, Map<String, String> param) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("source", "back");
        HttpEntity<Object> request = new HttpEntity<>(param, httpHeaders);
        com.alibaba.fastjson.JSONObject jsonObject = restTemplate.postForObject(url, request, com.alibaba.fastjson.JSONObject.class);
        return jsonObject;
    }

    /**
     * 获取当前日期的前一天
     */
    public static String getDateBefore() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format.format(calendar.getTime());
        return dateString;
    }

    /**
     * 获取当前日期的前一周
     */
    public static String getWeekBefore() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -7);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format.format(calendar.getTime());
        return dateString;
    }

    /**
     * 根据开始时间和结束时间计算中间的日期集合
     *
     * @param beginDate 如2019-07-02
     * @param endDate   如2019-08-11
     * @return
     */
    public static List<String> getBetweenDates(String beginDate, String endDate) throws ParseException {
        List<String> result = new ArrayList<>();
        Date begin = Util.simpleDateFormat("yyyy-MM-dd").parse(beginDate);
        Date end = Util.simpleDateFormat("yyyy-MM-dd").parse(endDate);
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(begin);
        while (begin.getTime() <= end.getTime()) {
            result.add(new SimpleDateFormat("yyyy-MM-dd").format(calendarStart.getTime()));
            calendarStart.add(Calendar.DAY_OF_YEAR, 1);
            begin = calendarStart.getTime();
        }
        return result;
    }

    public static List<String> getBetweenDatesOther(String beginDate, String endDate) throws ParseException {
        List<String> result = new ArrayList<>();
        Date begin = Util.simpleDateFormat("yyyy-MM-dd").parse(beginDate);
        Date end = Util.simpleDateFormat("yyyy-MM-dd").parse(endDate);
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(begin);
        while (begin.getTime() <= end.getTime()) {
            result.add(new SimpleDateFormat("yyyy-MM-dd").format(calendarStart.getTime()));
            calendarStart.add(Calendar.DAY_OF_YEAR, 1);
            begin = calendarStart.getTime();
        }
        return result;
    }

    public static List<String> getMonthBetweenDates(String beginDate, String endDate) throws ParseException {
        List<String> result = new ArrayList<>();
        List<String> res = new ArrayList<>();
        res = getBetweenDatesOther(beginDate, endDate);
        Map<String, String> map = new HashMap<>();
        for (String str : res) {
            String key = str.substring(0, 7);
            if (!map.containsKey(key)) {
                map.put(key, key);
            }
        }
        map.forEach((k, v) -> {
            result.add(k);
        });

        return result;
    }

    /**
     * 判断输入数据是否是空
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
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前日期是否是最后一天
     */
    public static boolean isLastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当天前一天的日期
     */
    public static Date getPreDay() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    /**
     * 获取日期的此月第一天
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取日期的此月最后一天
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 判断今天是不是第一天
     */
    public static boolean isFirstDay(Date date) {
        String dateStr = simpleDateFormat("yyyy-MM-dd").format(date);
        if (dateStr.endsWith("-01"))
            return true;
        return false;
    }
}

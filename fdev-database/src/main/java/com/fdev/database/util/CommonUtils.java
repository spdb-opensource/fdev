package com.fdev.database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.collections.CollectionUtils.getCardinalityMap;

@Component
public class CommonUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);// 控制台日志打印

    /**
     * 通过 字符串返回类的get方法 获取属性
     *
     * @param obj
     * @param method
     * @return
     */
    public static Object getMethodByGet(Object obj, String method) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getMethods();
        try {
            for (Method mthod : methods) {
                if (String.format("get%s", method).equalsIgnoreCase(mthod.getName().toLowerCase())) {
                    return mthod.invoke(obj);
                }
            }
        } catch (Exception e) {
            logger.error("执行 获取 {} 对象获取 {} 值的时候出现错误", clazz.getName(), method);
        }
        return null;
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

    /**
     * 获取当前日期
     * str  日期转换格式
     */
    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    //查找的字符searchStr在字符串str中从左到右第count次出现的下标
    public static int getFromIndex(String str, String searchStr, Integer count) {
        Matcher matcher = Pattern.compile(searchStr).matcher(str);
        int index = 0;
        while (matcher.find()) {
            index++;
            if(index == count){
                 break;
            }
        }
        return  matcher.start();
    }

    /**
     * 统计指定字符在字符串中出现的次数
     *
     * @param content 字符串内容  str 指定字符
     * @return 次数
     */
    public static int CountNum(String content, String str) {
        int index = 0;
        int count = 0;
        while ((index = content.indexOf(str, index)) != -1) {
            index++;
            count++;
        }
        return  count;
    }

    /**
     * 比较两个List<Map>的值是否相等（包括数据长度、存储的值）
     * @param a
     * @param b
     * @return
     */
    public static boolean isEqualCollection(Collection a, Collection b){
        if(a.size() != b.size()){
            return  false;
        }
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        if(mapa.size() != mapb.size()){
            return false;
        }
        Iterator it = mapa.keySet().iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(getFreq(obj, mapa) != getFreq(obj, mapb)){
                return  false;
            }
        }
        return true;
    }
    //以obj为key,可以防止重复
    private static final int getFreq(Object obj, Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if(count != null){
            return  count.intValue();
        }
        return  0;
    }

}

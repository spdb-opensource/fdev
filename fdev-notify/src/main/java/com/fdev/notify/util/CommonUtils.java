package com.fdev.notify.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/9/9 16:58
 */
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
            if (obj instanceof Collection) {
                List objList = (List) obj;
                if (objList.isEmpty()) {
                    return true;
                }
                objList.remove("");
                objList.remove(null);
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
                objMap.remove("");
                objMap.remove(null);
                if (objMap.isEmpty()) {
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
}

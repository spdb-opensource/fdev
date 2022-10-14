package com.spdb.common.util;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author lizz
 */
public class Util {

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String VALUE_SEPARATOR = "=";

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            return o.length == 0;
        } else {
            if (obj instanceof String) {
                return "".equals(((String) obj).trim()) || "null".equals(((String) obj).trim());
            }
            if (obj instanceof List) {
                List objList = (List) obj;
                return objList.isEmpty();
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                return objMap.isEmpty();
            }
            return (obj == null) || (("").equals(obj));
        }
    }

    /**
     * 判断实体类中所有的属性是否存在null，存在null返回true
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static boolean checkObjFieldIsNull(Object obj) throws Exception {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            Object o = f.get(obj);
            if (null == o) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    /**
     * @param value
     * @param placeholderResolver
     * @param visitedPlaceholders
     * @return
     */
    public static String parseStringValue(String value, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {
        StringBuilder result = new StringBuilder(value);
        int startIndex = value.indexOf(PLACEHOLDER_PREFIX);

        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(placeholder)) {
                    throw new IllegalArgumentException("Circular placeholder reference '" + placeholder + "' in property definitions");
                }

                placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if (propVal == null && VALUE_SEPARATOR != null) {
                    int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + VALUE_SEPARATOR.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }

                if (propVal != null) {
                    propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                    result.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);

                    startIndex = result.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
                } else {
                    throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "' in value \"" + value + "\"");
                }

                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    /**
     * @param buf
     * @param startIndex
     * @return
     */
    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + PLACEHOLDER_PREFIX.length();
        int withinNestedPlaceholder = 0;

        while (index < buf.length()) {
            if (StringUtils.substringMatch(buf, index, PLACEHOLDER_SUFFIX)) {
                if (withinNestedPlaceholder <= 0) {
                    return index;
                }

                --withinNestedPlaceholder;
                index += PLACEHOLDER_SUFFIX.length();
            } else {
                ++index;
            }
        }
        return -1;
    }
}
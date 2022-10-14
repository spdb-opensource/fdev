package com.spdb.fdev.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.dict.Dict;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Configuration
@RefreshScope
public class Util {

    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String PLACEHOLDER_PREFIX = "${";
    public static final String PLACEHOLDER_SUFFIX = "}";
    public static final String VALUE_SEPARATOR = "=";

    @Value("${auth.jwt.token.secret}")
    private static String secret;

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
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
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
            Object o = f.get(obj);
            if (f.get(obj) == null) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }


    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    public static Date parseDate(String str, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        return sf.parse(date);
    }

    public static String getUrlById(String module, String id) {
        switch (module) {
            case Dict.FTASK:
                return "job/list/" + id;
            case Dict.FAPP:
                return "app/list/" + id;
            case Dict.FRELEASE:
                return "release/list/" + id + "joblist";
            default:
                return "";
        }
    }

    public static String parseStringValue(String value, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {
        StringBuilder result = new StringBuilder(value);
        int startIndex = value.indexOf(PLACEHOLDER_PREFIX);

        while(startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if(endIndex != -1) {
                String placeholder = result.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                String originalPlaceholder = placeholder;
                if(!visitedPlaceholders.add(placeholder)) {
                    throw new IllegalArgumentException("Circular placeholder reference '" + placeholder + "' in property definitions");
                }

                placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if(propVal == null && VALUE_SEPARATOR != null) {
                    int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
                    if(separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + VALUE_SEPARATOR.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if(propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }

                if(propVal != null) {
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

    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + PLACEHOLDER_PREFIX.length();
        int withinNestedPlaceholder = 0;

        while(index < buf.length()) {
            if(StringUtils.substringMatch(buf, index, PLACEHOLDER_SUFFIX)) {
                if(withinNestedPlaceholder <= 0) {
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

    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        Object obj = null;
        try {
            obj = clazz.newInstance();
            if (map.containsKey("serialVersionUID"))
                map.remove("serialVersionUID");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    Object value = entry.getValue();
                    if (value == null)
                        continue;
                    //判断类型是否一致，以及判断是否相互为父子类 或者 接口的实现
                    if (value.getClass().getTypeName().equals(field.getType().getTypeName()) || field.getType().isAssignableFrom(value.getClass()))
                        field.set(obj, value);
                    else {
//                        Field fieldObj = field.getClass().newInstance();
                        field.set(obj, map2Object((Map) value, field.getType()));
                    }
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
    }

    /**
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public static User getSessionUser(){
        return (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession().getAttribute(Dict._USER);
    }

    /**
     * 从session中获取用户信息
     *
     * @return 用户info
     * @throws Exception
     */
    public static Map getSessionFuser(){
        return (Map) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession().getAttribute(Dict._FUSER);
    }

    /**
     * 根据token获取当前登录用户user_name_en
     */
    public static String getUserByToken(String token) throws Exception {
        if (null == token || "".equals(token.trim())) {
            return null;
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT jwt = verifier.verify(token);
        Claim userNameEn = jwt.getClaims().get(Dict.USER_NAME_EN);
        if (null == userNameEn || "".equals(userNameEn.asString().trim())) {
            return null;
        }
        String[] args = userNameEn.asString().split("-");
        if (args.length == 2)
            return args[0];
        if (args.length == 3)
            return args[0] + "-" + args[1];
        return "";
    }

}


package com.spdb.fdev.freport.base.utils;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class CommonUtils{

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
                .getSession().getAttribute(EntityDict._USER);
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
    }

}

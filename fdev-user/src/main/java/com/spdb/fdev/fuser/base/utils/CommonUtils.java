package com.spdb.fdev.fuser.base.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.spdb.fdev.fuser.base.dict.Dict;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Configuration
public class CommonUtils implements ApplicationContextAware {
	
	public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
	public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String RELEASEDATE = "yyyyMMddHHmmss";
	public static final String DATE_PARSE = "yyyy-MM-dd";
	public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String INPUT_DATE = "yyyy/MM/dd";

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    /**
     * 判断输入数据是否是空
     *
     * @param inStr 输入字符串
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
            if ("serialVersionUID".equals(name) || "_id".equals(name)|| Dict.LEAVE_DATE.equals(name)|| Dict.AREA_ID.equals(name)
                    || Dict.EDUCATION.equals(name) || Dict.REMARK.equals(name) || Dict.RANK_ID.equals(name) || Dict.REDMINE_USER.equals(name) || Dict.SVN_USER.equals(name)
                    || Dict.FTMS_LEVEL.equals(name) || Dict.MANTIS_TOKEN.equals(name) || Dict.CREATETIME.equals(name)
                    || Dict.UPDATETIME.equals(name)) {
                continue;
            }
            Object o = f.get(obj);
            if (o == null) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    /**
     * 判断实体类中所有的属性是否全为null
     */
    public static boolean checkObjFieldIsAllNull(Object obj) throws Exception {
        boolean flag = true;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            Object o = f.get(obj);
            if (o == null) {
                continue;
            } else {
                return false;
            }
        }
        return flag;
    }


    /**
     * id生成器，引用 ObjectId,转成String
     */
    public static String createId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }

    public static String uuid(){
        String str="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<4;i++){
        	stringBuilder.append(str.charAt(new Random().nextInt(str.length())));
        }
        return stringBuilder.toString();
    }
    
    public static String formatDate(String str) {
		SimpleDateFormat sf = new SimpleDateFormat(str);
		Date date = new Date();
		return sf.format(date);
	}
    
    /**
     * date 日期
     * str  日期转换格式
     * 将不同类型的日期转换为yyyy-MM-dd HH:mm:ss
     * @throws ParseException 
     */
    public static String formatDate1(String date, String str) throws ParseException {
    	SimpleDateFormat sf = new SimpleDateFormat(str);
    	Date sdfdate  = sf.parse(date);
    	return formatDate2(sdfdate,"yyyy-MM-dd HH:mm:ss");
    }
    //将Date类型转换成String类型
    public static String formatDate2(Date date, String str) {
    	SimpleDateFormat sf = new SimpleDateFormat(str);
    	return sf.format(date);
    }
    
    //对象转map
    public static Map obj2Map(Object obj){
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj == null)
            return map;
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj)!=null?field.get(obj):null);
            }
        } catch (Exception e) {

        }
        return map;
    }

    /**
     * 将对象转换成map
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String ,Object> map = Maps.newHashMap();
        if(bean != null){
            BeanMap beanMap = BeanMap.create(bean);
            for(Object key : beanMap.keySet()){
                 map.put(key+"", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * 将map转换为对象
     * @param map
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, T bean){
          BeanMap beanMap = BeanMap.create(bean);
          beanMap.putAll(map);
          return bean;
    }

    /**
     * 将List<T>转换为List<Map>
     * @param objList
     * @param <T>
     * @return
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList){
        List<Map<String, Object>> list = Lists.newArrayList();
        if(objList != null && objList.size() > 0){
            Map<String, Object> map = null;
            T bean = null;
            for(T t : objList){
                map = beanToMap(t);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 将List<Map>转换成List<T>
     * @param maps
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz) throws Exception {
        List<T> list = Lists.newArrayList();
        if(maps != null && maps.size() > 0){
            Map<String, Object> map = null;
            T bean = null;
            for(Map map1 : maps){
                  bean = clazz.newInstance();
                  mapToBean(map1, bean);
                  list.add(bean);
            }
        }
         return list;
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
            throw new FdevException("COM0012");
        }
        return user;
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
                }
            }
        }
        return null;
    }

}



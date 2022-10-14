package com.fdev.database.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EntityUtils {
    /**
     * 实体类转Map
     */
     public static Map<String, Object> entityToMap(Object object){
           Map<String, Object> map = new HashMap<>();
           for(Field field : object.getClass().getDeclaredFields()){
               try {
                   boolean flag = field.isAccessible();
                   field.setAccessible(true);
                   Object o =field.get(object);
                   map.put(field.getName(), o);
                   field.setAccessible(flag);
               } catch (IllegalAccessException e) {
                   e.printStackTrace();
               }
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



}

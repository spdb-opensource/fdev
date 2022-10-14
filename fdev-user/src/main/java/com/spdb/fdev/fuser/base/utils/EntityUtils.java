package com.spdb.fdev.fuser.base.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spdb.fdev.fuser.base.dict.Dict;
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
     public static Map entityToMap(Object object){
           Map map = new HashMap<>();
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
    public static <T> Map beanToMap(T bean) {
        Map map = Maps.newHashMap();
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
    public static <T> T mapToBean(Map map, T bean){
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
    public static <T> List<Map> objectsToMaps(List<T> objList){
        List<Map> list = Lists.newArrayList();
        if(objList != null && objList.size() > 0){
            Map map = null;
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
    public static <T> List<T> mapsToObjects(List<Map> maps, Class<T> clazz) throws Exception {
        List<T> list = Lists.newArrayList();
        if(maps != null && maps.size() > 0){
            Map map = null;
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
     *去除object对象中的_id参数，避免redis无法获取_id中的参数
     * @param object
     * @return
     */
    public static Object removeObjectid(Object object){
        Map map =  beanToMap(object);
         if(!CommonUtils.isNullOrEmpty(object)){
             map.remove(Dict.OBJECTID);
         }
         return map;
    }


}

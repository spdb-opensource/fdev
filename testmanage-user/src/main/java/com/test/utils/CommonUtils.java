package com.test.utils;


import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
	
	public static void main(String[] args){
//		System.out.println(encoder("admin", "admin"));
	}
	
    public static Map beanToMap(Object obj) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(obj,Map.class);
    }
    /**
     * 判断实体类中所有的属性是否存在null，存在null返回true
     */
    public static boolean checkObjFieldIsNull(Object obj) throws Exception {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || name.contains("_id")) {
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

}
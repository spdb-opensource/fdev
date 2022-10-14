package com.fdev.database.util;

import java.util.*;
import java.util.stream.Collectors;

public class ValidateUtils {
    /**
     * 校验List<Map>中是否存在同一键值下的值是相同的
     * @param mapList
     * @param key
     * @return
     */
     public static Boolean checkRepeatKey(List<Map> mapList, String key){
         //获取mapList中的key的值出现相同的情况数量
         int count = mapList.stream().collect(Collectors.groupingBy(a -> a.get(key), Collectors.counting()))
                 .entrySet().stream().filter(entry -> entry.getValue() >1).map(entry -> entry.getKey())
                 .collect(Collectors.toList()).size();
         if(count > 0){
            return  true;
         }
         return false;
     }

}

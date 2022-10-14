package com.fdev.database.util;

import java.util.*;

public class DictUtils {

    /**
     * 将List<Map>中的字段取出到List<String>
     * @param collect
     * @param key
     * @return
     */
    public static List<String> collectByKey(List<Map> collect, String key){
        List<String> result = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(collect)) {
            for (Map map : collect) {
                result.add((String) map.get(key));
            }
        }
        return  result;
    }

    /**
     * 比较List<String>中oldFields比newFields少和多那些字段
     * @param oldFields
     * @param newFields
     * @return
     */
    public static Map compareFields(List<String> oldFields, List<String> newFields){
        Map diff = new HashMap();
        Map<String, Integer> map = new HashMap();
        List<String> addList = new ArrayList<>();
        List<String> delList = new ArrayList<>();
        for(String str : newFields){
             map.put(str, 1);
        }
        for(String str : oldFields){
            Integer s = map.get(str);
            if(s != null){
                map.put(str, 2);
                continue;
            }
            map.put(str, 3);
        }
        for(Map.Entry<String, Integer> entry : map.entrySet()){
             if(entry.getValue() == 1){
                 addList.add(entry.getKey());
             }else if(entry.getValue() == 3){
                 delList.add(entry.getKey());
             }
        }
        diff.put("addField",addList);
        diff.put("delField",delList);
        return  diff;
    }

    public static StringBuffer StrBuf(List<String> Fields){
        StringBuffer stringBuffer = new StringBuffer();
        if(!CommonUtils.isNullOrEmpty(Fields)){
            int i = 0;
            for(String str : Fields){
                if(i == 0){
                    stringBuffer.append(str);
                    i++;
                } else {
                    stringBuffer.append("、"+str);
                }
            }
        }
        return stringBuffer;
    }

}

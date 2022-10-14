package com.fdev.database.util;

import com.fdev.database.dict.Dict;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RefreshScope
public class SubstrUtils {

    private  static  String removeTables;

    @Value("${fdatabase.schema.removetables}")
    public  void setRemoveTables(String removeTables){
        this.removeTables = removeTables;
    }

    //获取字符串内容content中第一个括号（包含}、]、)）间的字符串，左括号：leftStr ，右括号：rightStr （支持单个括号）
    public static String getContentByChar(String content, String leftStr, String rightStr){
        String leftSplit = String.format("\\%s", leftStr);
        String[] strs = content.substring(content.indexOf(leftStr)).split(leftSplit);
        int index = 0;    //括号间的长度
        int num = -1;     //统计长度
        int m = -1;        //记录截取字符串中存在（的情况
        int n = 0;        //记录截取字符串中存在）的情况
        for(String s : strs) {
            m++;
            if(s.contains(rightStr)){
                n +=CommonUtils.CountNum(s, rightStr);
                if(n == m){
                    index= num + s.lastIndexOf(rightStr);
                    break;
                }
                if(n >= m){      //content中存在多余的）
                    //获取倒数第n-m+1个(配对的括号)  rightStr 字符串的索引
                    int k = StringUtils.lastOrdinalIndexOf(s, rightStr, n-m+1);
                    index= num + k;
                    break;
                }
            }
            num += s.length() + 1;
        }
        if(index == 0){
           return  "";  // content 异常
        }
        return content.substring(content.indexOf(leftStr)+1).substring(0, index);
    }

    //获取字符串中的索引
    public static Map getIndex(String content){
       String[] strs = content.trim().split(" ");
       int n = 0;
       Map IndexMap = new HashMap();
       if(content.contains(".") && content.contains("(")) {
           for (String str : strs) {
               if (str.contains(".")) {
                   String[] indexInfo = str.split("\\.");
                   if (n == 0) {   //第一个.是索引名称
                       IndexMap.put("indexName", replaceChar(indexInfo[1].trim()));
                   } else {       //第二个.是表名
                       //判断是否为不统计表名
                       String[] tables = removeTables.split(",");
                       for (String s : tables) {
                           if (indexInfo[1].equals(s)) {
                               return null;
                           }
                       }
                       IndexMap.put(Dict.TABLENAME, replaceChar(indexInfo[1].toUpperCase().trim()));
                   }
                   n++;
               }
           }
           List<String> indexColumnList = new ArrayList<>();
           String IndexColumnStrs = getContentByChar(content.trim(), "(", ")");
           String[] indexColumns = IndexColumnStrs.split(",");
           for (String indexColumn : indexColumns) {
               indexColumnList.add(replaceChar(indexColumn.trim()));
           }
           IndexMap.put("indexColumn", indexColumnList);
           return IndexMap;
       }
        return null;
    }

    /**
     * 去除字符串中的引号（单引号和双引号）
     * @param str
     * @return
     */
    public static String replaceChar(String str){
        if(str.contains("\"")){
           str = str.replace("\"", "");
        }
        if(str.contains("\'")){
           str = str.replace("\'", "");
        }
        if(str.contains("`")){
           str = str.replace("`", "");
        }
        return str;
    }

}

package com.fdev.database.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.entity.Database;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
@RefreshScope
public class DatabaseUtil {

    private  static  String removeTables;    //排除扫描的表名

    private static String scan_skip_app_type;

    @Value("${fdatabase.schema.removetables}")
    public  void setRemoveTables(String removeTables){
        this.removeTables = removeTables;
    }

    @Value("${scan.skip.app.type}")
    public void  setScan_skip_app_type(String scan_skip_app_type) { this.scan_skip_app_type = scan_skip_app_type; }

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JSONObject objectToJsonObject(Object object) throws JsonProcessingException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(object);
        return JSONObject.fromObject(json);
    }

    public static String objectToJson(Object object) throws JsonProcessingException {
        JSONObject jsonObject = objectToJsonObject(object);
        return jsonObject.toString();
    }

    //解析schema文件中的表名
    public static Map parseSchema(String filepath, String dataType) {
        Map result = new HashMap();
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(filepath);
            br = new BufferedReader(fileReader);
            String line;
            //获取所有内容
            String content = "";
            while ((line = br.readLine()) != null) {
                content += line;
            }
            //以分号分割字符串
            String[] contents = content.split(";");
            //获取所有建表语句
            List<String> tables = new ArrayList<>();
            for (String s : contents) {
                if (s.toUpperCase().contains("CREATE TABLE")) {
                    s = s.substring(s.toUpperCase().indexOf("CREATE TABLE"));
                    tables.add(s);
                }
            }
            //获取所有创建主键语句
            List<String> primarykeyStrs = new ArrayList<>();
            if(dataType.equals("oracle")){
                for (String s : contents) {
                    if (s.toUpperCase().contains("PRIMARY KEY")) {
                        primarykeyStrs.add(s);
                    }
                }
            }

            //获取所有创建唯一索引语句
            List<String> uniqueIndexList = new ArrayList<>();
            for (String s : contents) {
                if (s.toUpperCase().contains("CREATE UNIQUE INDEX")) {
                    s = s.substring(s.toUpperCase().indexOf("CREATE UNIQUE INDEX"));
                    uniqueIndexList.add(s);
                }
            }
            //获取所有创建索引语句
            List<String> IndexList = new ArrayList<>();
            for (String s : contents) {
                if (s.toUpperCase().contains("CREATE INDEX")) {
                    s = s.substring(s.toUpperCase().indexOf("CREATE INDEX"));
                    IndexList.add(s);
                }
            }

            List<Map> primaryKeyList = new ArrayList<>();
            List<Map> uniqueIndexInfoList = new ArrayList<>();
            List<Map> indexInfoList = new ArrayList<>();
            //解析创建表语句
            List<Map> tableInfoList = new ArrayList<>();
            for (String s : tables) {
                String[] s1 = s.toUpperCase().split("CREATE TABLE");
                String[] s2 = s1[s1.length - 1].split("\\(");
                String[] s3 = s2[0].split("\\.");
                String tableName = s3[s3.length - 1].trim();
                tableName = SubstrUtils.replaceChar(tableName);
                //判断是否为不统计表名
                String[] tableNames = removeTables.split(",");
                Boolean isflag = false;
                for(String str : tableNames){
                    if(tableName.equals(str.toUpperCase())){
                        isflag = true;
                        break;
                    }
                }
                if(isflag){
                    continue;
                }
                Map tableInfo = new HashMap();
                tableInfo.put(Dict.TABLENAME, tableName);
                
                //获取表中字段
                List<Map> columnList = new ArrayList<>();
                List<String> primaryKeys = new ArrayList<>();
                String columnsStr = SubstrUtils.getContentByChar(s1[1].toLowerCase(), "(", ")");
                String[] columns = columnsStr.split(", ");
                for (String str : columns) {
                    Map map1 = new HashMap();
                    String[] strs = str.trim().split(" ");
                    //获取主键和字段
                    if (str.toUpperCase().contains("PRIMARY KEY")) {
                        String primaryKeyStr = "";
                        if(str.contains("(")){
                           primaryKeyStr = SubstrUtils.getContentByChar(str, "(", ")");
                           String[] primaryKeyStrs = primaryKeyStr.split(",");
                            for(String primaryKey : primaryKeyStrs){
                                primaryKeys.add(SubstrUtils.replaceChar(primaryKey));
                            }
                        } else {    //获取mysql自增主键
                            String[] strs2 = str.trim().split(" ");
                            primaryKeys.add(strs2[0]);
                            map1.put("columnType", SubstrUtils.replaceChar(strs2[0]));
                            map1.put("column", strs2[1]);
                            columnList.add(map1);
                        }
                    } else{
                        //解析mysql创表语句中的索引
                        String str1 = str.trim().toUpperCase();
                        if(str1.startsWith("UNIQUE KEY") || str1.startsWith("KEY")){
                            //唯一索引
                            if(str1.startsWith("UNIQUE KEY")){
                                String keystr = "";
                                if(str.contains("UNIQUE KEY")){
                                    keystr = str.replace("UNIQUE KEY", "").trim();
                                } else if (str.contains("unique key")){
                                    keystr = str.replace("unique key", "").trim();
                                }
                                Map IndexMap = getIndexInfo(keystr, tableName);
                                uniqueIndexInfoList.add(IndexMap);
                            } else {   //普通索引
                                String keystr = "";
                                if(str.contains("KEY")){
                                    keystr = str.replace("KEY", "").trim();
                                } else if (str.contains("key")){
                                    keystr = str.replace("key", "").trim();
                                }
                                Map IndexMap = getIndexInfo(keystr, tableName);
                                indexInfoList.add(IndexMap);
                            }
                        } else {    //字段和字段类型
                            String column = strs[0].trim();
                            String columnType = "";
                            if(strs[1].contains("(") && !strs[1].contains(")")){  //解决oracle中类型中间存在空格情况
                                columnType = strs[1]+" "+strs[2];
                            } else {
                                columnType = strs[1].trim();
                            }
                            //去除字符串中的""
                            column = SubstrUtils.replaceChar(column);
                            map1.put("columnType", columnType);
                            map1.put("column", column);
                            columnList.add(map1);
                        }
                    }
                }
                tableInfo.put(Dict.COLUMNS, columnList);  //字段、类型
                tableInfo.put("primaryKey", primaryKeys);    //主键
                tableInfoList.add(tableInfo);
            }

            //获取主键
            if(dataType.equals("oracle")){
                for(String s : primarykeyStrs){
                    Map primarykeyInfo = getPrimaryKeyInfo(s);
                    if(!CommonUtils.isNullOrEmpty(primarykeyInfo)){
                        primaryKeyList.add(primarykeyInfo);
                    }
                }
            }
            
            //获取唯一索引
            for(String s : uniqueIndexList){
               Map uniqueIndexInfo = SubstrUtils.getIndex(s);
               if(!CommonUtils.isNullOrEmpty(uniqueIndexInfo)){
                   uniqueIndexInfoList.add(uniqueIndexInfo);
               }
            }
            //获取索引
            for(String s : IndexList){
                Map indexInfo = SubstrUtils.getIndex(s);
                if(!CommonUtils.isNullOrEmpty(indexInfo)){
                    indexInfoList.add(indexInfo);
                }
            }
            result.put("tableInfo", tableInfoList);
            result.put("primaryKeys", primaryKeyList);
            result.put("uniqueIndex", uniqueIndexInfoList);
            result.put("index", indexInfoList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭文件流
            try {
                br.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Map getPrimaryKeyInfo(String content) {
        String[] strs = content.trim().split(" ");
        Map PrimaryKeyMap = new HashMap();
        if(content.contains(".") && content.contains("(")) {
            for (String str : strs) {
                if (str.contains(".")) {
                    String[] strInfo = str.split("\\.");
                    PrimaryKeyMap.put(Dict.TABLENAME, SubstrUtils.replaceChar(strInfo[1].toUpperCase().trim()));
                    break;
                }
            }
            List<String> PrimaryKeyList = new ArrayList<>();
            String ColumnStrs = SubstrUtils.getContentByChar(content.trim(), "(", ")");
            String[] Columns = ColumnStrs.split(",");
            for (String Column : Columns) {
                PrimaryKeyList.add(SubstrUtils.replaceChar(Column.trim()));
            }
            PrimaryKeyMap.put("key", PrimaryKeyList);
        }
        return PrimaryKeyMap;
    }

    public static Map getIndexInfo(String str, String tableName) {
        Map IndexMap = new HashMap();
        String[] indexstrs = str.trim().split(" ");
        IndexMap.put("indexName", SubstrUtils.replaceChar(indexstrs[0].trim()));
        IndexMap.put(Dict.TABLENAME, tableName);
        if(str.contains("(") && str.contains(")")) {
            String indexkeyStr = SubstrUtils.getContentByChar(str, "(", ")");
            String[] indexkeys = indexkeyStr.split(",");
            List<String> indexColumnList = new ArrayList<>();
            for(String indexkey : indexkeys){
                indexColumnList.add(SubstrUtils.replaceChar(indexkey.trim()));
            }
            IndexMap.put("indexColumn", indexColumnList);
        }
        return IndexMap;
    }

    //对数组对象进行去重处理 (查询库中Schema中的所有库表信息)
    public static List<Database> setDatabase(List<Database> databases) {
        //统计同一表名存在的多种库类型、库名、表名的组合
        List<Database> setlist = databases.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.getDatabase_type()
                        + ";" + o.getDatabase_name() + ";" + o.getTable_name()))
        ), ArrayList::new));
        return setlist;
    }


    //解析schema文件中的表名
    public static List<String> getTableInXml(File file, List<Database> databases) {
        List<String> xmlTableName = new ArrayList<>();
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(file);
            br = new BufferedReader(fileReader);
            String line;
            //获取所有内容
            String content = "";
            while ((line = br.readLine()) != null) {
                content += line;
            }
            //多个空格替换成一个空格
            Pattern p1 = Pattern.compile("\\s+");
            Matcher m1 = p1.matcher(content);
            content = m1.replaceAll(" ");
            String[] strs = content.split(" ");
            for (Database database : databases) {
                for (String str : strs) {
                    String[] tables = str.split("[,(<]");
                    if (tables.length > 0) {
                        for (String table : tables) {
                            //处理存在 ibs_ptab_jnl$_PtabJnlIndex$ 表拆分的情况
                            if(table.contains("$") && CommonUtils.CountNum(table, "$") == 2){
                                String s = table.substring(table.indexOf("$"), table.lastIndexOf("$")+1);
                                table = table.replace(s, "0");
                            }
                            if (table.toUpperCase().trim().equals(database.getTable_name().toUpperCase())) {
                                xmlTableName.add(database.getTable_name().trim().toUpperCase());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭文件流
                br.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return xmlTableName;
    }

    //判断是否跳过扫描的应用类型，如Vue，IOS
    public static boolean isSkip(String type_name) {
        if (StringUtils.isBlank(type_name))
            return false;
        List<String> skipList = new ArrayList<>();
        String[] exceptArray = scan_skip_app_type.split(",");
        if (!CommonUtils.isNullOrEmpty(exceptArray)) {
            skipList = Arrays.asList(exceptArray);
        }
        if (skipList.size() > 0) {
            for (String type : skipList) {
                if (type_name.toUpperCase().contains(type)) {
                    return true;
                }
            }
        }
        return false;
    }

}

    


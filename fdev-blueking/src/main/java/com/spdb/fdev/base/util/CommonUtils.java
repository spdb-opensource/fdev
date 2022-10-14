package com.spdb.fdev.base.util;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spdb.fdev.base.dict.Dict;


public class CommonUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);// 控制台日志打印

    private CommonUtils() {
    }

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */

    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((("").equals(((String) obj).trim())) || ((Dict.NULL).equals(((String) obj).trim()))) {
                    return true;
                }
                return false;
            }
            if (obj instanceof List) {
                List objList = (List) obj;
                if (objList.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                if (objMap.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (("").equals(obj)) {
                return true;
            }
        }
        return false;
    }

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

    //读取文本转字符串throws Exception
    public static String byteToString(String path)  {
        File file = new File(path);
        try (FileInputStream in = new FileInputStream(file);) {
            int size = in.available();
            String str = "";
            byte buffer[] = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer);
            return str;
        }catch (FileNotFoundException e) {
            logger.error("{} 文件不存在", file);
        } catch (IOException e) {
            logger.error("读取文本转字符串");
        }
        return null;
    }

    //去除字符串空格回车tab
    public static String replaceBlank(String str) {
        String dest = "";
        if(!(CommonUtils.isNullOrEmpty(str))) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    //逐行读取txt文件
    public static Set byteToSet(String path) {
        File file = new File(path);
        try (FileInputStream in = new FileInputStream(file);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));){
            Set set = new HashSet();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                set.add(line);
            }
            return set;
        } catch (FileNotFoundException e) {
            logger.error("{} 文件不存在", file);
        } catch (IOException e) {
            logger.error("逐行读取txt文件异常");
        }
        return null;
    }
}

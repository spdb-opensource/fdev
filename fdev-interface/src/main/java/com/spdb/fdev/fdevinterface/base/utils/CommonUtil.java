package com.spdb.fdev.fdevinterface.base.utils;

import com.alibaba.fastjson.util.TypeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.*;

public class CommonUtil {
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
    private CommonUtil() {
    }

    private static final Map<String, BeanCopier> BEAN_COPIER_HASH_MAP = new HashMap<>();

    private static Map<String, String> contextPathMap;

    static {
        Map<String, String> map = new HashMap<>();
        ResourceBundle bundle = ResourceBundle.getBundle("application-context");
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            map.put(key, bundle.getString(key));
        }
        contextPathMap = map;
    }


    public static Map<String, String> getContextPathMap() {
        return contextPathMap;
    }

    /**
     * 将map转为实体对象
     *
     * @param map   需要被转换的map实例
     * @param clazz 实体类的Class对象
     * @return
     * @author xxx
     */
    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return TypeUtils.castToJavaBean(map, clazz);
    }

    /**
     * 将map list转为实体list
     *
     * @param mapList 需要被转换的map list
     * @param clazz   实体类的Class对象
     */
    public static <T> List<T> mapList2ObjectList(List<Map> mapList, Class<T> clazz) {
        List list = new ArrayList();
        for (Map map : mapList) {
            Object object = map2Object(map, clazz);
            list.add(object);
        }
        return (List<T>) list;
    }

    /**
     * 对象转换
     *
     * @param source 需要转换的对象
     * @param target 需要转成的Class对象
     * @param <T>
     * @return
     */
    public static <T> T convert(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        try {
            T t = target.newInstance();
            String beanKey = generateKey(source.getClass(), target);
            BeanCopier copier;
            if ((copier = BEAN_COPIER_HASH_MAP.get(beanKey)) == null) {
                synchronized (BEAN_COPIER_HASH_MAP) {
                    if ((copier = BEAN_COPIER_HASH_MAP.get(beanKey)) == null) {
                        copier = BeanCopier.create(source.getClass(), target, false);
                        BEAN_COPIER_HASH_MAP.put(beanKey, copier);
                    }
                }
            }
            copier.copy(source, t, null);
            return t;
        } catch (Exception e) {
            //此处不需要打出异常信息
        }
        return null;
    }

    /**
     * 判断输入数据是否是空
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


    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.getName() + class2.getName();
    }

    /**
     * 执行python脚本
     *
     * @param path
     * @param args
     * @return
     */
    public static String runPython(String path, String[] args) {
        String[] cmdA = new String[args.length + 2];
        cmdA[0] = "python";
        cmdA[1] = path;
        for (int i = 0; i < args.length; i++) {
            cmdA[i + 2] = args[i];
        }
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (LineNumberReader success = new LineNumberReader(new InputStreamReader(process.getInputStream()));
             LineNumberReader error = new LineNumberReader(new InputStreamReader(process.getErrorStream()));) {
            StringBuffer sc = new StringBuffer();
            StringBuffer er = new StringBuffer();
            String line1;
            while ((line1 = success.readLine()) != null) {
                sc.append(line1);
            }
            String line2;
            while ((line2 = error.readLine()) != null) {
                er.append(line2);
            }
            if (!StringUtils.isEmpty(er.toString())) {
                throw new FdevException(ErrorConstants.RUN_PYTHON_ERROR, new String[]{path, er.toString()});
            }
            return sc.toString();
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.RUN_PYTHON_ERROR, new String[]{path, e.getMessage()});
        }
    }

    public static Object runCmd(String cmd) {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try ( LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));){
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
           logger.error("执行 {} 的时候出现错误，错误信息如下{}", cmd, e);
        }
        return null;
    }



    /**
     * 将实体对象转为map
     *
     * @param o 实体对象实例
     * @return
     * @author xxx
     */
    public static Map<String, Object> object2Map(Object o) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (o == null)
            return map;
        Class clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(o));
            }
        } catch (Exception e) {
             throw new FdevException(ErrorConstants.OBJECT_CAST_TO_MAP_ERROR, new String[]{e.getMessage()});
        }
        return map;
    }



}

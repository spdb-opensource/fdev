package com.spdb.fdev.fdevapp.base.utils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import org.springframework.core.io.ClassPathResource;

import static org.apache.commons.collections.CollectionUtils.getCardinalityMap;

@Configuration
public class CommonUtils implements ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);// 控制台日志打印
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
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
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmptys(Object obj) {
        boolean bool = isNullOrEmpty(obj);
        if (bool)
            return bool;

        if (obj instanceof List) {
            List objList = (List) obj;
            if (objList.isEmpty()) {
                return true;
            }
            objList.remove(null);
            objList.remove("");
            if (objList.isEmpty()) {
                return true;
            }
            return false;
        }
        if (obj instanceof Set) {
            Set objSet = (Set) obj;
            if (objSet.isEmpty()) {
                return true;
            }
            objSet.remove(null);
            objSet.remove("");
            if (objSet.isEmpty()) {
                return true;
            }
            return false;
        }
        if (obj instanceof Map) {
            Map objMap = (Map) obj;
            if (objMap.isEmpty()) {
                return true;
            }
            objMap.remove("");
            objMap.remove(null);
            if (objMap.isEmpty()) {
                return true;
            }
            return false;
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
            if (Dict.SERIALVERSIONUID.equals(name) || Dict.OBJECTID.equals(name)
                    || Dict.DESC.equals(name) || Dict.TASK_ID.equals(name)) {
                continue;
            }
            if (f.get(obj) == null) {
                flag = true;
                return flag;
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

    public static Object runCmd(String cmd) {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
        } catch (IOException e) {
            logger.error("执行 " + cmd + " 的时候出现错误，错误信息如下" + e);
        }
        try (LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));){
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("执行 " + cmd + " 的时候出现错误，错误信息如下" + e);
        }
        return null;
    }

    /**
     * 根据属性名，通过get方法，获取对应属性值
     *
     * @param o         对象实例
     * @param fieldName 对象属性名称
     * @return
     * @author xxx
     */
    public static Object getGetMethod(Object o, String fieldName) {
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            if (("get" + fieldName).toLowerCase().equals(method.getName().toLowerCase())) {
                try {
                    Object object = method.invoke(o);
                    return object;
                } catch (Exception e) {
                    logger.error("执行 获取" + o.getClass().getName() + "对象获取" + fieldName + "值的时候出现错误，错误信息如下:\r" + e);
                }
            }
        }
        return null;
    }

    /**
     * 根据属性名获取该属性的指定注解的指定参数值
     *
     * @param o         对象实例
     * @param fieldName 对象属性名称
     * @param clazz     注解名称
     * @return
     * @author xxx
     */
    public static Object getFiledAnnotationVal(Object o, String fieldName, Class<?> clazz, String key) {
        try {
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (fieldName.equals(field.getName())) {
                    Annotation[] annotations = field.getAnnotations();
                    for (Annotation annotation : annotations) {
                        String annoName = annotation.annotationType().getName();
                        if (annoName.equals(clazz.getName())) {
                            InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                            Field declaredField = invocationHandler.getClass().getDeclaredField("memberValues");
                            declaredField.setAccessible(true);
                            Map map = (Map) declaredField.get(invocationHandler);
                            return map.get(key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("执行 获取" + o.getClass().getName() + "对象获取" + fieldName + "的" + clazz.getName() + "注解" + key
                    + "的属性时候出现错误，错误信息如下:\r" + e);
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
            logger.error("对象转map出错");
        }
        return map;
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
        Object obj = null;
        try {
            obj = clazz.newInstance();
            if (map.containsKey(Dict.SERIALVERSIONUID))
                map.remove(Dict.SERIALVERSIONUID);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(obj, entry.getValue());
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
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
                    logger.error("根据属性获取对应值出错");
                }
            }
        }
        return null;
    }

    /**
     * 删除文件夹及其下面的子文件夹和文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File subFile : listFiles) {
                        deleteFile(subFile);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            logger.error("删除失败，请人工删除，{}", e.getStackTrace());
        }
    }

    /**
     * 删除应用录入和删除后的项目文件
     *
     * @param file
     */
    public static void deleteCacheFile(File file) {
        logger.info("开始删除文件{}", file.getAbsolutePath());
        if (file.isFile()) {
            file.delete();
        } else {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File subFile : listFiles) {
                    deleteCacheFile(subFile);
                }
            }
            file.delete();
        }

    }

    /**
     * 根据传入内容写入文件
     *
     * @param file
     * @param content
     */
    public static void writeFile(File file, String content) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes("utf-8"));
        } catch (Exception e) {
            logger.error("写入文件失败!文件内容：" + content);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("关闭流错误!");
                }
            }
        }
    }

    /**
     * 将多级文件中的内容替换
     *
     * @param file
     * @param beReplaceStr 待替换字符
     * @param replaceStr   替换字符
     * @throws Exception
     */
    public static void replaceStrInMultiInFile(File file, String beReplaceStr, String replaceStr) throws Exception {
        if (file == null)
            throw new FdevException(Constant.M_DATA_ERROR, new String[]{"文件对象为空"});
        if (file.isFile()) {
            replaceStrInFile(file, beReplaceStr, replaceStr);
        } else {
            File[] listFiles = file.listFiles();
            for (File subFile : listFiles) {
                replaceStrInMultiInFile(subFile, beReplaceStr, replaceStr);
            }
        }
    }

    /**
     * 根据文件路径替换文件的内容
     *
     * @param filePathList 存放文件路径的集合
     * @param beReplaceStr 待替换字符串
     * @param replaceStr   替换字符串
     * @throws Exception
     * @author xxx
     */
    public static void replaceStrByFilePathList(List<String> filePathList, String beReplaceStr, String replaceStr) throws Exception {
        for (String filePath : filePathList) {
            replaceStrInFile(new File(filePath), beReplaceStr, replaceStr);
        }
    }

    /**
     * 将单个文件中的内容替换
     *
     * @param file
     * @param beReplaceStr 待替换字符
     * @param replaceStr   替换字符
     * @throws Exception
     * @author xxx
     */
    public static void replaceStrInFile(File file, String beReplaceStr, String replaceStr) throws Exception {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(beReplaceStr)) {
                    line = line.replace(beReplaceStr, replaceStr);
                }
                sb.append(line + "\n");
            }
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.print(sb.toString().substring(0, sb.lastIndexOf("\n")));
        } catch (Exception e) {
            logger.error(file == null ? "" : file.getAbsolutePath() + "文件替换" + beReplaceStr + "至" + replaceStr + "失败！");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {}
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {}
        }
    }

    /**
     * 将单个文件中的指定内容，只替换第一个
     * @param file
     * @param beReplaceStr
     * @param replaceStr
     * @throws Exception
     */
    public static void replaceFirstStrInFile(File file, String beReplaceStr, String replaceStr) throws Exception {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder sb = new StringBuilder();
            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains(beReplaceStr) && count == 0) {
                    line = line.replace(beReplaceStr, replaceStr);
                    count++;
                }
                sb.append(line + "\n");
            }
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.print(sb.toString().substring(0, sb.lastIndexOf("\n")));
        } catch (Exception e) {
            logger.error(file == null ? "" : file.getAbsolutePath() + "文件替换" + beReplaceStr + "至" + replaceStr + "失败！");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {}
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {}
        }
    }

    /**
     * 对持续集成文件缺失的环境信息补充
     *
     * @param file
     * @param env
     * @throws Exception
     */
    public static void addCIFile(File file, String env) throws Exception {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            if (sb.toString().indexOf("CI_FDEV_ENV") == -1) {
                sb.append("variables:" + "\n");
                sb.append("  CI_FDEV_ENV: \"" + env + "\"" + "\n");
            }
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.print(sb.toString().substring(0, sb.lastIndexOf("\n")));
        } catch (Exception e) {
            logger.info("添加持续集成环境信息失败");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {

            }
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {}
        }
    }

    /**
     * 替换文件内容
     *
     * @param file
     * @param content
     * @throws Exception
     */
    public static void replaceContent(File file, String content) throws Exception {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.print(content);
        } catch (Exception e) {
            logger.info("替换应用配置模块文件失败");
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * 将文件重命名
     *
     * @param srcFileName 原文件名称
     * @param desFileName 替换文件名称
     * @throws Exception
     * @author xxx
     */
    public static void renameFile(String srcFileName, String desFileName) throws Exception {
        if (StringUtils.isBlank(srcFileName))
            throw new FdevException(Constant.I_DATA_ERROR, new String[]{"原始文件名为空！"});
        if (StringUtils.isBlank(desFileName))
            throw new FdevException(Constant.I_DATA_ERROR, new String[]{"替换文件名为空！"});
        File srcFile = new File(srcFileName);
        File desFile = new File(desFileName);
        if (desFile.exists())//判断将要修改成的文件名是否已经存在，如果已经存在，是不能重命名的，故先删除
            desFile.delete();
        srcFile.renameTo(desFile);
    }

    /**
     * 将文件夹及其子文件夹的文件或单个文件的路径中的部分路径替换成指定的字符串
     * 例如 文件原路径：/home/spdb/fdev/test.txt   参数出入为 beReplaceStr:fdev,replaceStr:test 产出：/home/spdb/test/test.txt
     *
     * @param file         原始文件对象实例
     * @param beReplaceStr 待替换的字符串
     * @param replaceStr   替换的字符串
     * @param filePathList 存放替换后的路径的集合
     * @throws Exception
     * @author xxx
     */
    public static void getReplacePathFiles(File file, String beReplaceStr, String replaceStr, List<String> filePathList) throws Exception {
        if (file.isFile()) {
            String srcFilePath = file.getAbsolutePath();
            String desFilePath = srcFilePath.replace(beReplaceStr, replaceStr);
            filePathList.add(desFilePath);
        } else {
            File[] listFiles = file.listFiles();
            for (File subFile : listFiles) {
                getReplacePathFiles(subFile, beReplaceStr, replaceStr, filePathList);
            }
        }
    }

    /**
     * 获取需求字符串替换的持续集成文件
     */
    public static void getContinuousPathFiles(File file, List<String> filePathList) throws Exception {
        if (file.isFile()) {
            String filePath = file.getAbsolutePath();
            filePathList.add(filePath);
        } else {
            File[] listFiles = file.listFiles();
            for (File subFile : listFiles) {
                getContinuousPathFiles(subFile, filePathList);
            }
        }
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取date前一周的日期
     *
     * @param date
     * @return
     */
    public static Date getLastWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -7);
        return c.getTime();
    }

    /**
     * 获取date前6周的日期
     *
     * @param date
     * @return
     */
    public static List<Date> getLastSixWeek(Date date) {
        List<Date> dates = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            dates.add(date);
            date = getLastWeek(date);
        }
        return dates;
    }

    public static String getDateStr(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * 反射调用指定对象的指定方法
     *
     * @param o          被调用对象实例
     * @param methodName 方法名
     * @param param      方法中的形参的类型
     * @param param      方法中的形参
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object reflectByMethod(Object o, String methodName, Class<Object>[] paramClass, Object[] param) throws IllegalAccessException, IllegalArgumentException {
        Method method = null;
        try {
            method = o.getClass().getDeclaredMethod(methodName, paramClass);
            return method.invoke(o, param);
        } catch (NoSuchMethodException | SecurityException e) {//判断是否存在该方法，debug使用
            logger.debug(e.getMessage());
        } catch (InvocationTargetException e) {
            throw (FdevException) e.getTargetException();
        }
        return null;
    }

    /**
     * 获取当前调用的方法名
     *
     * @return
     */
    public static String getCurrentMethodName() {
        Thread thread = Thread.currentThread();
        StackTraceElement[] stackTrace = thread.getStackTrace();
        return stackTrace[1].getMethodName();
    }

    /**
     * 将方法名的首字母转为大写（主要用于驼峰命名规则下的方法名）
     *
     * @return
     */
    public static String transformMethodName(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }

    /**
     * 将字符串转为list或queue
     *
     * @param str
     * @return
     */
    public static <T> T str2Queue(String str, T desc) {
        String[] split = str.split(",");
        for (String subStr : split) {
            if (desc instanceof Queue) {
                Queue queue = (Queue) desc;
                queue.add(subStr);
            }
            if (desc instanceof List) {
                List list = (List) desc;
                list.add(subStr);
            }
        }
        return desc;
    }

    /**
     * 将字符串转为xml
     *
     * @param content
     * @return
     */
    public static String getPomNode(String content) {
        if (CommonUtils.isNullOrEmpty(content)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"can't find this tag，pom文件错误或不存在"});
        }
        String artifactId = null;
        try {
            Document document = DocumentHelper.parseText(content);
            Element root = document.getRootElement();
            artifactId = root.element("artifactId").getTextTrim();
        } catch (DocumentException e) {
            logger.error("pom文件解析错误");
        }
        return artifactId;
    }

    /**
     * 将字符串转为properties
     *
     * @param content
     * @return
     */
    public static String getPropertiesNode(String content) {
        if (CommonUtils.isNullOrEmpty(content)) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Properties properties = new Properties();
        String property = null;
        try {
            properties.load(inputStream);
            property = properties.getProperty("spring.application.name");
        } catch (IOException e) {
            logger.error("properties文件解析错误");
        }
        return property;
    }

    /**
     * 将路径中的符号转义
     *
     * @param file
     * @return
     */
    public static String fileUtil(String file) {
        file = file.replace("/", "%2F");
        file = file.replace(".", "%2E");
        return file;
    }

    /**
     * 将url转化成project_url
     *
     * @param url
     * @return project_url
     */
    public static String projectUrl(String url) {
        String project_url = url + Dict.PROJECTS;
        return project_url;
    }

    /**
     * str to map
     */

    public static Map str2Map(String str) {
        Map map = new HashMap();
        try {
            String[] strs = str.split(",");
            for (String mapping : strs) {
                String[] kv = mapping.split(":");
                map.put(kv[0], kv[1]);
            }
        } catch (Exception e) {
            logger.error("string convert to map error:" + e.getMessage());
        }
        return map;
    }

    /**
     * base64加密
     **/
    public static String encode(String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(token.getBytes());
        String encodeToken = new String(encode);
        return encodeToken;
    }

    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8").replaceAll("%", "a");
        } catch (UnsupportedEncodingException e) {
            logger.error("文件名转码失败", e.getMessage());
        }
        return url;
    }

    /**
     * 解析模版json文件
     *
     * @param templatePath
     * @return
     */
    public static String getTemplateJson(String templatePath) {
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (Exception e) {
            logger.error("解析{}文件出错，{}！", templatePath, e.getMessage());
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"解析" + templatePath + "文件出错！"});
        }
        return stringBuilder.toString();
    }


    /**
     * 根据两个时间戳计算差值，并得到差值的具体的时间
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static Long getTimestampDiffValue(Long startTime, Long endTime) {

        Long dvalueTime = 0L;
        if (startTime > endTime){
            dvalueTime = startTime - endTime;
        }else{
            dvalueTime = endTime - startTime;
        }
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        day = dvalueTime / (24 * 60 * 60 * 1000);
        hour = (dvalueTime / (60 * 60 * 1000) - day * 24);
        min = ((dvalueTime / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (dvalueTime / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return dvalueTime;
//        if (day != 0) return (day + "天" + hour + "小时" + min + "分钟" + sec + "秒");
//        if (hour != 0) return (hour + "小时" + min + "分钟" + sec + "秒");
//        if (min != 0) return (min + "分钟" + sec + "秒");
//        if (sec != 0) return (sec + "秒");
//        return ("0秒");
    }

    /**
     * 比较两个List<Map>的值是否相等（包括数据长度、存储的值）
     * @param a
     * @param b
     * @return
     */
    public static boolean isEqualCollection(Collection a, Collection b){
        if(a.size() != b.size()){
            return  false;
        }
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        if(mapa.size() != mapb.size()){
            return false;
        }
        Iterator it = mapa.keySet().iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(getFreq(obj, mapa) != getFreq(obj, mapb)){
                return  false;
            }
        }
        return true;
    }
    //以obj为key,可以防止重复
    private static final int getFreq(Object obj, Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if(count != null){
            return  count.intValue();
        }
        return  0;
    }

    /**
     * 根据骨架名称选择对应的模板文件
     * @param archeTypeName
     * @return
     */
    public static String getTemplateByArcheType(String archeTypeName) {
        switch (archeTypeName) {
            case "vue-archetype" :
                return "mobcli-vue";
            case "ms-online-archetype" :
                return "online";
            case "mspmk-web-archetype" :
                return "web";
            case "msper-web-archetype" :
                return "web";
            case "ms-web-archetype" :
                return "msent-web";
            case "job-executor-archetype" :
                return "online";
            case "msent-web-archetype" :
                return "msent-web";
            case "msemk-web-archetype" :
                return "msent-web";
            case "mgmt-web-demo" :
                return "msent-web";
            case "ient-submit-demo" :
                return "online";
            case "nbh-online-demo" :
                return "online";
            default:
                return "";
        }
    }
}
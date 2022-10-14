package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {

    private CommonUtil() {
    }

    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String INPUT_DATE_HOURS_SECOND = "yyyy/MM/dd HH:mm";
    public static final String INPUT_DATE_HOURS = "yyyy/MM/dd HH";
    public static final String INPUT_DATE = "yyyy/MM/dd";
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);


    /**
     * date 日期,format 格式，将format格式的日期转换成下面
     * 将日期 转换为 yyyy-MM-dd HH:mm:ss
     * @throws ParseException
     */
    public static String formatDate(String date, String format) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date sdfdate  = sf.parse(date);
            sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将日期yyyy/MM/dd HH:mm 转换为 yyyy-MM-dd HH:mm:ss失败" + e.getMessage());
        }
        return null;
    }

    /**
     * 将endTime增加一天
     * @param dateTime
     * @return
     */
    public static String timeAddOneDay(String dateTime){
        try {
            SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            Date sdfdate  = sf.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(sdfdate);
            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            sdfdate = c.getTime();
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将endTime添加一天失败！" + e.getMessage());
        }
        return "";
    }

    /**
     * 处理时间，将gitlab传过来的时间yyyy-MM-dd HH:mm:ss UTC 转换为 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime
     */
    public static String handleTime(String dateTime) {
        if (StringUtils.isEmpty(dateTime))
            return null;
        dateTime = dateTime.replace(" UTC","");
        try {
            SimpleDateFormat sf = new SimpleDateFormat(STANDARDDATEPATTERN);
            Date sdfdate  = sf.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(sdfdate);
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 8);
            sdfdate = c.getTime();
            return sf.format(sdfdate);
        }catch (ParseException e) {
            logger.error("将日期yyyy-MM-dd HH:mm:ss UTC 转换为 yyyy-MM-dd HH:mm:ss失败" + e.getMessage());
        }
        return dateTime;
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

    public static final String DATE_FORMATE = "yyyy/MM/dd";

    public static String dateFormate(Date date, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    /**
     * 创建文件，并写入内容
     *
     * @param fileDir
     * @param fileContent
     */
    public static void createFile(String fileDir, String fileContent) {
        File file = new File(fileDir);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            logger.info("创建文件{}出错，文件内容:{}，错误信息：{}", file, fileContent, e.getMessage());
        }
        try (PrintWriter printWriter = new PrintWriter(fileDir, StandardCharsets.UTF_8.name())) {
            printWriter.write(fileContent);
        } catch (IOException e) {
            logger.info("写入文件{}出错，文件内容:{}，错误信息：{}", file, fileContent, e.getMessage());
        }
    }

    /**
     * id生成器，引用 ObjectId,转成String
     */
    public static String createId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }

    /**
     * 判断输入数据是否是空
     *
     * @param obj 输入字符串
     * @return
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

    //对象转map
    public static Map obj2Map(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj == null)
            return map;
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj) != null ? field.get(obj) : null);
            }
        } catch (Exception e) {

        }
        return map;
    }


}

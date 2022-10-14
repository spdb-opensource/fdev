package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {

    private CommonUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

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
            if (obj instanceof Set) {
                Set objSet = (Set) obj;
                if (objSet.isEmpty()) {
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

    public static String dateFormate(Date date, String formate){
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
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
     * base64加密
     **/
    public static String encode(String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(token.getBytes());
        String encodeToken = new String(encode);
        return encodeToken;
    }

}

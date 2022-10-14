package com.spdb.fdev.fdevenvconfig.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2019/7/5 13:08
 */
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
                if ((("").equals(((String) obj).trim())) || ((Constants.NULL).equals(((String) obj).trim()))) {
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

    /**
     * 根据属性名，通过get方法，获取对应属性值
     *
     * @param o
     * @param fieldName
     * @return
     * @throws Exception
     * @author xxx
     */
    public static Object getGetMethod(Object o, String fieldName) {
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            if (("get" + fieldName).equalsIgnoreCase(method.getName().toLowerCase())) {
                try {
                    return method.invoke(o);
                } catch (Exception e) {
                    logger.error("执行 获取 {} 对象获取 {} 值的时候出现错误，错误信息如下:\r {}", o.getClass().getName(), fieldName, e);
                }
            }
        }
        return null;
    }

    /**
     * 根据属性名获取该属性的指定注解的指定参数值
     *
     * @param o
     * @param fieldName
     * @param clazz
     * @param key
     * @return
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
     * 将对象转为map
     *
     * @param o
     * @return
     * @author xxx
     */
    public static Map<String, Object> object2Map(Object o) {
        Map<String, Object> map = new HashMap<>();
        if (o == null)
            return map;
        Class clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (Constants.SERIALVERSIONUID.equals(fieldName)) {
                    continue;
                }
                field.setAccessible(true);
                map.put(fieldName, field.get(o));
            }
        } catch (Exception e) {
            logger.error("对象转map出错");
        }
        return map;
    }

    public static <T> T map2Object(Map map, Class<T> clazz) {
        Object obj = null;
        try {
            obj = clazz.newInstance();
            for (Object key : map.keySet()) {
                try {
                    Field field = clazz.getDeclaredField(key.toString());
                    field.setAccessible(true);
                    field.set(obj, map.get(key));
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
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

    /**
     * 检查list有哪些重复元素
     *
     * @param list
     * @return
     */
    public static Set<String> checkRepeat(List<String> list) {
        Set<String> set = new HashSet<>();
        Set<String> repeat = new HashSet<>();
        for (String string : list) {
            if (!set.add(string)) {
                repeat.add(string);
            }
        }
        return repeat;
    }

    /**
     * 解析 配置模板 的数据信息
     *
     * @param content
     * @return
     */
    public static void checkKeyIsEquals(String content) {
        content = content.replace("\r", "");
        String[] split = content.split("\n");
        List<String> result = new ArrayList<>();
        for (String line : split) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] lineSplit = line.split("=", 2);
            if (lineSplit.length <= 1) {
                continue;
            }
            result.add(lineSplit[0]);
        }
        Set<String> repeat = checkRepeat(result);
        if (null != repeat && !repeat.isEmpty()) {
            throw new FdevException(ErrorConstants.DEPLOY_TEMPLATE_SAVE_ERROR, new String[]{"左边的key" + repeat + "不能重复！"});
        }
    }

    /**
     * 判断文件的内容是否 有实质内容 注释，为空
     *
     * @param content
     */
    public static boolean checkFileContentIsNone(String content) {
        String[] contentSplit = content.split("\n");
        if (isNullOrEmpty(content))
            return true;
        for (int i = 0; i < contentSplit.length; i++) {
            if (isNullOrEmpty(contentSplit[i]) || contentSplit[i].startsWith("#"))
                continue;
            else
                return false;
        }
        return true;
    }

    /**
     * 判断map，list<map>值是否相等,更新版本号
     *
     * @param before
     * @param after
     * @param type   one 值是否一样，zero NAME_EN是否一样
     * @param <T>
     * @return
     */
    public static <T> boolean CompareMapOrList(T before, T after, String type) {
        boolean result = true;
        if (before instanceof Map) {
            if (!before.equals(after)) {
                result = false;
            }
        }
        if (before instanceof List) {
            if (((List) before).size() != ((List) after).size()) {
                result = false;
            } else {
                for (Object objBefore : (List) before) {
                    boolean bol = false;
                    Map mapB = (Map) objBefore;
                    for (Object objAfter : (List) after) {
                        Map mapA = (Map) objAfter;
                        if (Constants.ONE.equals(type)) {
                            if (mapB.equals(mapA)) {
                                bol = true;
                                break;
                            }
                        } else {
                            if (mapB.get(Dict.NAME_EN).equals(mapA.get(Dict.NAME_EN))) {
                                bol = true;
                                break;
                            }
                        }

                    }
                    if (bol) {
                        continue;
                    } else {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static <T> String CompareGetVersion(T before, T after, String oldVersion) {
        double upVersion;
        try {
            upVersion = Double.parseDouble(oldVersion);
        } catch (Exception e) {
            upVersion = 0.1;
            logger.error("类型转换错误,{}", e.getMessage());
        }
        if (!CompareMapOrList(before, after, Constants.ONE)) {
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(upVersion));
            BigDecimal staticDecimal = new BigDecimal(Constants.UP_VERSION);
            BigDecimal decimalResult = bigDecimal.add(staticDecimal);
            return decimalResult.toString();
        } else {
            return String.valueOf(upVersion);
        }
    }

    public static String uuid() {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            stringBuilder.append(str.charAt(new Random().nextInt(str.length())));
        }
        return stringBuilder.toString();
    }

    /**
     * 解析模版文件
     *
     * @param templatePath
     * @return
     */
    public static InputStream getTemplateYml(String templatePath) {
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        try {
            return classPathResource.getInputStream();
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"解析" + templatePath + "文件出错！"});
        }
    }

    /**
     * 通过 json schema 校验 json 内容
     *
     * @param dataString
     * @param schemaString
     * @return
     */
    public static ProcessingReport validateJsonSchema(String dataString, String schemaString) {
        try {
            JsonNode dataNode = JsonLoader.fromString(dataString);
            JsonNode schemaNode = JsonLoader.fromString(schemaString);
            JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
            return schema.validate(dataNode);
        } catch (IOException | ProcessingException ex) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{dataString + "格式异常！"});
        }
    }

    public static Object runCmd(String cmd) throws IOException {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = Runtime.getRuntime().exec(cmdA);
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
            if (StringUtils.isNotBlank(sc.toString())) {
                return sc.toString();
            }
            if (!CommonUtils.isNullOrEmpty(er.toString())) {
                if (er.toString().contains(Dict.UNAUTHORIZED)) {
                    return Dict.USER_OR_PASSWORDERROR;
                } else {
                    return Dict.IP_ERROR;
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("cmd: {}执行失败", cmd);
        }
        return null;
    }

    public static List<Map> compareByEnvSourt(List<Map> maps, List<String> envSort) {
        List<Map> sortedMaps = maps.stream().sorted((x, y) -> {
            Integer xIndex = envSort.size() + 1;
            Integer yIndex = envSort.size() + 1;
            for (String str : envSort) {
                if (x.get(Dict.LABELS) != null && ((List) x.get(Dict.LABELS)).contains(str)) {
                    xIndex = envSort.indexOf(str);
                }
            }
            for (String str : envSort) {
                if (y.get(Dict.LABELS) != null && ((List) y.get(Dict.LABELS)).contains(str)) {
                    yIndex = envSort.indexOf(str);
                }
            }
            return xIndex.compareTo(yIndex);
        }).collect(Collectors.toList());
        return sortedMaps;
    }

    /**
     * 检查是否配置了配置文件中不存在的应用key
     *
     * @param variables
     * @param fileContext
     */
    public static void checkAppkey(List<Map<String, String>> variables, String fileContext) {
        List<String> keyList = new ArrayList<>();
        // 获取得到所有输入的appKey
        for (Map<String, String> variable : variables) {
            keyList.add(variable.get(Constants.APPKEY));
        }
        // 查询出来的outSideTemplate
        String[] eqSplit = fileContext.split("\n");
        List<String> list = new ArrayList<>();
        for (String s : eqSplit) {
            if (s.contains("=")) {
                list.add(s.split("=")[0]);
            }
        }
        List<String> illegalKeyList = new ArrayList<>();
        //对比key
        for (String key : keyList) {
            //如果不存在就抛出错误
            if (!list.contains(key)) {
                illegalKeyList.add(key);
            }
        }
        //判断非法key列表的尺寸是否大于0，若大于0则存在非法key，将key全部输出
        if (CollectionUtils.isNotEmpty(illegalKeyList)) {
            throw new FdevException(ErrorConstants.SAVE_ERROR, new String[]{"优先生效参数", "配置文件中不存在的应用key有: " + illegalKeyList});
        }
    }

    public static void checkBlankSpace(String filedName, String value) {
        String after = value.replace(" ", "");
        if (after.length() < value.length()) {
            throw new FdevException(ErrorConstants.MODEL_ENV_VALUE_ERROR, new String[]{filedName});
        }
    }

}

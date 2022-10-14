package com.manager.ftms.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.ftms.controller.TestCaseController;
import com.manager.ftms.entity.WeekReport;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import freemarker.template.Template;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class Utils {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Resource
    private RestTransport restTransport;

    @Autowired
    WebApplicationContext applicationContext;

    public static final String FULL_TIME = "yyyy-MM-dd HH:mm:ss";
    private static Logger logger = LoggerFactory.getLogger(TestCaseController.class);

    /**
     * 将Map转换为对象
     *
     * @param map
     * @param cls
     * @return
     */
    public static <T> T parseMap2Object(Map<String, Object> map, Class<T> cls) {
        return com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(map), cls);
    }

    // 获取当前时间的年月日 2014-01-01
    public static String dateUtil(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    //日期转换
    public static String transformDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.format(date);
    }

    /**
     * 读取paramMapper.properties文件的内容
     *
     * @param key
     * @return value
     */
    public static String getParamMapperProperties(String key) {
        Properties properties = new Properties();
        String value = key;
        try (InputStream in = Utils.class.getClassLoader().getResourceAsStream("paramMapper.properties");
             InputStreamReader reader = new InputStreamReader(in, "utf-8");) {
            properties.load(reader);
            value = properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {
        if (obj == null)
            return true;
        else if (obj instanceof CharSequence)
            return ((CharSequence) obj).length() == 0;
        else if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();
        else if (obj instanceof Map)
            return ((Map) obj).isEmpty();
        else if (obj.getClass().isArray())
            return Array.getLength(obj) == 0;

        return false;
    }

    // 将字符串时间，转换成long
    public static Long dateStrToLong(String inputTime) {
        Long outTime = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            outTime = simpleDateFormat.parse(inputTime).getTime() / 1000;
        } catch (ParseException e) {
            return outTime;
        }
        return outTime;
    }

    public static <T> T mapToBean(Map map, Class<T> cls) throws Exception {
        JSONObject json = JSONObject.fromObject(map);
        return jsonToBean(json, cls);
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
    }

    /**
     * map转bean
     *
     * @return 实体bean
     * @throws Exception
     */
    public static <T> T jsonToBean(net.sf.json.JSONObject json, Class<T> cls) throws Exception {
        JSONObject beanJson = new JSONObject();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            if (json.containsKey(name)) {
                beanJson.put(name, json.get(name));
            }
        }
        return (T) JSONObject.toBean(beanJson, cls);
    }

    public static String formatDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    public String getCurrentUserEnName() {
        Map<String, Object> userInfo = null;
        String userEnName;
        try {
            userInfo = redisUtils.getCurrentUserInfoMap();
            userEnName = (String) userInfo.get(Dict.USER_NAME_EN);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        return userEnName;
    }

    /**
     * 发送邮件
     *
     * @param subject
     * @param templateName
     * @param model
     * @param to
     * @param filePaths
     * @throws Exception
     */
    public void sendEmail(String subject, String templateName, HashMap model, List<String> to, List<String> filePaths) throws Exception {
        model.put(Dict.SUBJECT, subject);
        model.put(Dict.TEMPLATE_NAME, templateName);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put(Dict.SUBJECT, subject); //主题
        mailParams.put(Dict.CONTENT, content); //模版
        mailParams.put(Dict.TO, to);       //发送给
        mailParams.put(Dict.FILEPATHS, filePaths); //附件地址
        mailParams.put(Dict.REST_CODE, "sendMailUrl");
        try {
            restTransport.submitSourceBack(mailParams);
        } catch (Exception e) {
            logger.error("发送邮件失败" + e.getMessage());
            throw new FtmsException(ErrorConstants.SYSTEM_ERROR, new String[]{"发送邮件失败"});
        }
    }

    /**
     * @param templatePath 模版文件名
     * @param replace      替换的字段和value的map集合
     * @return 邮件正文
     * @throws Exception
     */
    private String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }


    public static Map getStatisgroup() {
        Map dataMap = new HashMap<>();
        dataMap.put("taskNum", 0);
        dataMap.put("testcaseNum", 0);
        dataMap.put("executeNum", 0);
        dataMap.put("mantisNum", 0);
        dataMap.put("requireRoleNum", 0);
        dataMap.put("funcMantisNum", 0);
        dataMap.put("errorNum", 0);
        dataMap.put("requireNum", 0);
        dataMap.put("historyNum", 0);
        dataMap.put("adviseNum", 0);
        dataMap.put("javaNum", 0);
        dataMap.put("packNum", 0);
        dataMap.put("otherNum", 0);
        dataMap.put("dataNum", 0);
        dataMap.put("enNum", 0);
        return dataMap;
    }

    public static void statisDefectArray(WeekReport weekReport, Map statismodel) {
        statismodel.put("taskNum", (int) statismodel.get("taskNum") + 1);
        statismodel.put("testcaseNum", (int) statismodel.get("testcaseNum") + weekReport.getTestcaseNum());
        statismodel.put("executeNum", (int) statismodel.get("executeNum") + weekReport.getExecuteNum());
        statismodel.put("mantisNum", (int) statismodel.get("mantisNum") + weekReport.getMantisNum());
        statismodel.put("requireRoleNum", (int) statismodel.get("requireRoleNum") + weekReport.getRequireRoleNum());
        statismodel.put("funcMantisNum", (int) statismodel.get("funcMantisNum") + weekReport.getFuncMantisNum());
        statismodel.put("errorNum", (int) statismodel.get("errorNum") + weekReport.getErrorNum());
        statismodel.put("requireNum", (int) statismodel.get("requireNum") + weekReport.getRequireNum());
        statismodel.put("historyNum", (int) statismodel.get("historyNum") + weekReport.getHistoryNum());
        statismodel.put("adviseNum", (int) statismodel.get("adviseNum") + weekReport.getAdviseNum());
        statismodel.put("javaNum", (int) statismodel.get("javaNum") + weekReport.getJavaNum());
        statismodel.put("packNum", (int) statismodel.get("packNum") + weekReport.getPackNum());
        statismodel.put("otherNum", (int) statismodel.get("otherNum") + weekReport.getOtherNum());
        statismodel.put("dataNum", (int) statismodel.get("dataNum") + weekReport.getDataNum());
        statismodel.put("enNum", (int) statismodel.get("enNum") + weekReport.getEnNum());
    }

    public static void statisDefectAllNum(Map statisAll, Map statismodel) {
        statisAll.put("taskNum", (int) statisAll.get("taskNum") + (int) statismodel.get("taskNum"));
        statisAll.put("testcaseNum", (int) statisAll.get("testcaseNum") + (int) statismodel.get("testcaseNum"));
        statisAll.put("executeNum", (int) statisAll.get("executeNum") + (int) statismodel.get("executeNum"));
        statisAll.put("mantisNum", (int) statisAll.get("mantisNum") + (int) statismodel.get("mantisNum"));
        statisAll.put("requireRoleNum", (int) statisAll.get("requireRoleNum") + (int) statismodel.get("requireRoleNum"));
        statisAll.put("funcMantisNum", (int) statisAll.get("funcMantisNum") + (int) statismodel.get("funcMantisNum"));
        statisAll.put("errorNum", (int) statisAll.get("errorNum") + (int) statismodel.get("errorNum"));
        statisAll.put("requireNum", (int) statisAll.get("requireNum") + (int) statismodel.get("requireNum"));
        statisAll.put("historyNum", (int) statisAll.get("historyNum") + (int) statismodel.get("historyNum"));
        statisAll.put("adviseNum", (int) statisAll.get("adviseNum") + (int) statismodel.get("adviseNum"));
        statisAll.put("javaNum", (int) statisAll.get("javaNum") + (int) statismodel.get("javaNum"));
        statisAll.put("packNum", (int) statisAll.get("packNum") + (int) statismodel.get("packNum"));
        statisAll.put("otherNum", (int) statisAll.get("otherNum") + (int) statismodel.get("otherNum"));
        statisAll.put("dataNum", (int) statisAll.get("dataNum") + (int) statismodel.get("dataNum"));
        statisAll.put("enNum", (int) statisAll.get("enNum") + (int) statismodel.get("enNum"));
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.group.{groupId}")
    public Map<String, Object> queryGroupDetailById(String groupId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "fdev.user.queryGroupDetail");
        sendMap.put(Dict.ID, groupId);
        List<Map> groups = (List<Map>) restTransport.submitSourceBack(sendMap);
        if (!Util.isNullOrEmpty(groups)) {
            return groups.get(0);
        } else {
            return null;
        }
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{userNameEn}")
    public Map<String, Object> queryUserCoreDataByNameEn(String userNameEn) throws Exception {
        Map sendData = new HashMap<String, String>();
        sendData.put(Dict.USER_NAME_EN, userNameEn);
        sendData.put(Dict.REST_CODE, "queryByUserCoreData");
        List<Map> users = (List<Map>) restTransport.submitSourceBack(sendData);
        if (!Util.isNullOrEmpty(users)) {
            return users.get(0);
        }
        return null;
    }

    /**
     * 判断是否为新玉衡
     *
     * @return
     */
    public String isNewFtms() {
        if (applicationContext.getServletContext().getContextPath().contains("-new")) {
            return "1";
        }
        return "0";
    }
}

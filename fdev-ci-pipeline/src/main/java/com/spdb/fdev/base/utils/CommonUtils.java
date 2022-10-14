package com.spdb.fdev.base.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IUserService;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class CommonUtils {
    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String INPUT_DATE = "yyyy/MM/dd";
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
    // 日期时间转换
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_PATTERN_F1 = "yyyy/MM/dd";

    public static  String getPort(String env){
        String port = "";
        switch (env){
            case "sit" : port = "9093";
                break;
            case "rel" : port = "9091";
                break;
            case "pro" : port = "8080";
                break;
        }
        return port;
    }

    public static String getCostDate(String dateStr1, String dateStr2, String format) throws Exception{
        SimpleDateFormat sf = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        Date date = sf.parse(dateStr1, pos);
        ParsePosition pos1 = new ParsePosition(0);
        Date date1 = sf.parse(dateStr2, pos1);
        long cost;
        if (date1.getTime() > date.getTime())
            cost = date1.getTime() - date.getTime();
        else
            cost = date.getTime() - date1.getTime();
        long hour = cost/(1000*60*60);
        long min = (cost/(1000*60) - hour * 60);
        long second = (cost/1000) - hour * 60 * 60 - min * 60;
        StringBuilder sb = new StringBuilder(String.format("%02d", hour));
        sb.append(":").append(String.format("%02d", min)).append(":").append(String.format("%02d", second));
        return sb.toString();
    }

    public static Long getCostTime(String dateStr1, String dateStr2, String format) throws Exception{
        SimpleDateFormat sf = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        Date date = sf.parse(dateStr1, pos);
        ParsePosition pos1 = new ParsePosition(0);
        Date date1 = sf.parse(dateStr2, pos1);
        long cost;
        if (date1.getTime() > date.getTime())
            cost = date1.getTime() - date.getTime();
        else
            cost = date.getTime() - date1.getTime();
        return cost;
    }

    /**
     * 将long类型的时间转成字符串11:11:11
     * @param time
     * @return
     * @throws Exception
     */
    public static String longTimeToDate(long time) throws Exception{
        long hour = time/(1000*60*60);
        long min = (time/(1000*60) - hour * 60);
        long second = (time/1000) - hour * 60 * 60 - min * 60;
        StringBuilder sb = new StringBuilder(String.format("%02d", hour));
        sb.append(":").append(String.format("%02d", min)).append(":").append(String.format("%02d", second));
        return sb.toString();
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
     * 判断实体类中所有的属性是否存在null，存在null返回true
     */
    public static boolean checkObjFieldIsNull(Object obj) throws Exception {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            Object o = f.get(obj);
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

    public static String formatDate(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        return sf.format(date);
    }

    public static String formatYesterDay(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return sf.format(calendar.getTime());
    }

    public static String archivedDay(String str) {
        SimpleDateFormat sf = new SimpleDateFormat(str);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -4);
        return sf.format(calendar.getTime());
    }

    public static Date parseDate(String str, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(date);
        return sf.parse(str);
    }

    //日期格式 yyyy-MM-dd
    public static boolean isArchivePermited(String release_date) {
        String formatDate = CommonUtils.archivedDay(CommonUtils.DATE_PARSE);
        if (release_date.compareTo(formatDate) < 0) {
            return true;
        }
        return false;
    }

//    /**
//     * 从session中获取用户信息
//     *
//     * @return 用户英文名
//     * @throws Exception
//     */
//    public static User getSessionUser() throws Exception {
//        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//                .getSession().getAttribute(Dict._USER);
//        if (CommonUtils.isNullOrEmpty(user)) {
//            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
//        }
//        return user;
//    }
//
//    /**
//     * 获取当前用户的 author对象
//     */
//    public static Author getAuthor() throws Exception {
//        Author author = new Author();
//        User user = getSessionUser();
//        /*BeanUtils.copyProperties(user,author);*/
//        author.setId(user.getId());
//        author.setNameEn(user.getUser_name_en());
//        author.setNameCn(user.getUser_name_cn());
//        return author;
//    }

    public static <T> T mapToBean(Map map, Class<T> cls) throws Exception {
        JSONObject json = JSONObject.fromObject(map);
        return jsonToBean(json, cls);
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

    /**
     * 将map转为实体对象，2021-04-16 13:36:31 修改为可以对实体嵌套实体的也可以进行转换
     *
     * @param map   需要被转换的map实例
     * @param clazz 实体类的Class对象
     * @return
     * @author c-lisl1
     */
    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        Object obj = null;
        try {
            obj = clazz.newInstance();
            if (map.containsKey("serialVersionUID"))
                map.remove("serialVersionUID");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    //判断类型是否一致，以及判断是否相互为父子类 或者 接口的实现
                    if (value.getClass().getTypeName().equals(field.getType().getTypeName()) || field.getType().isAssignableFrom(value.getClass()))
                        field.set(obj, value);
                    else if (field.getType().getSimpleName().contains("boolean") || field.getType().getSimpleName().contains("Boolean")) {
                        field.set(obj, value);
                    } else {
//                        Field fieldObj = field.getClass().newInstance();
                        field.set(obj, map2Object((Map) value, field.getType()));
                    }
                } catch (NoSuchFieldException | SecurityException e) {
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return (T) obj;
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
    public static <T> T jsonToBean(JSONObject json, Class<T> cls) throws Exception {
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

    public static Object runCmd(String cmd) throws IOException {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = null;
        LineNumberReader br = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
            br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("执行 {} 的时候出现错误，错误信息如下{}", cmd, e);
        }finally {
            if(process != null ){
                process.destroy();
            }
            if(br != null ){
                br.close();
            }
        }
        return null;
    }

    public static boolean isEarlierThanToday(String date) throws ParseException {
        String today = formatDate(DATE_PARSE);
        SimpleDateFormat sf = new SimpleDateFormat(DATE_PARSE);
        Date dateBefore = sf.parse(date);
        Date dateNow = sf.parse(today);
        if (dateBefore.before(dateNow)) {
            return false;
        }
        return true;
    }

    public static long compareDateWithToday(String date) throws ParseException {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long inputTime = parseDate(date, DATE_PARSE).getTime();
        long todayTime = calendar.getTime().getTime();
        long time = inputTime - todayTime;
        long day = time / (1000*3600*24);
        return day;
    }

    public static void createDirectory(String loacl_path) {
        File directory = new File(loacl_path);
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdirs();
            logger.info("下载文件时目录构建成功");
        } else {
            logger.error("{}目录已存在", loacl_path);
        }
    }

    /**
     * 时间格式化成字符串
     */
    public static String dateFormat(Date date, String pattern) {
        if (!isNotNullOrEmpty(pattern)) {
            pattern = DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static boolean isNotNullOrEmpty(String... param) {
        for (String va : param) {
            if (va == null || "".equals(va)) {
                return false;
            }
        }
        return true;
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

    public static String readJsonFile(String templatePath) {
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

    public static PriorityBlockingQueue<JobExe> queue = new PriorityBlockingQueue();

    public static void addJobExe(JobExe jobExe) throws Exception{
         queue.add(jobExe);
    }

    public static JobExe getJobExe() throws Exception{
        return (JobExe) queue.poll();
    }

    public static boolean stagesCheck(List<Stage>stages){
        if(stages.size() == 0){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"stages"});
        }
        for(Stage stage : stages){
            if (CommonUtils.isNullOrEmpty(stage.getName())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"stage.name"});
            }
            if(CommonUtils.isNullOrEmpty(stage.getJobs())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage.getName() + ".jobs"});
            } else {
                for(Job job : stage.getJobs()) {
                    if(CommonUtils.isNullOrEmpty(job.getName())){
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage.getName() + ".job.name"});
                    }
                    if(CommonUtils.isNullOrEmpty(job.getSteps())){
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage.getName()+"."+job.getName()+".steps"});
                    }else {
                        for(Step step: job.getSteps()){
                            if(CommonUtils.isNullOrEmpty(step.getName())){
                                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage.getName()+"."+job.getName()+".step.name"});
                            }
                            if(CommonUtils.isNullOrEmpty(step.getPluginInfo())){
                                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{stage.getName()+"."+job.getName()+"."+step.getName()+".pluginInfo"});
                            }
                        }

                    }
                }
            }
        }
        return true;
    }

    public static Map<String,Map> listToMap(List<Map> list, String keyName){
        Map<String,Map> resultMap = new HashMap<>();
        if(list == null  || list.size() == 0){
            return resultMap;
        }
        //如果map中包含keyname，用它的值做key放入新map
        for (Map map : list){
            if(!CommonUtils.isNullOrEmpty(map) && map.containsKey(keyName)){
                resultMap.put((String) map.get(keyName),map);
            }
        }
        return resultMap;
    }

    /**
     * 将key映射得到field上的注解
     *
     * @param key
     * @return
     */
    public static String mapperValueKey(String key, Class target) throws NoSuchFieldException {
        //这个实体类的所有的属性字段
        Field declaredField = target.getDeclaredField(key);
        org.springframework.data.mongodb.core.mapping.Field fieldDeclaredAnnotation = declaredField.getDeclaredAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
        if (CommonUtils.isNullOrEmpty(fieldDeclaredAnnotation)) {
            logger.error("key没有对应的注解Field字段的值");
            throw new FdevException("key没有对应的注解Field字段的值");
        }else
            return fieldDeclaredAnnotation.value();
    }


    /**
     * 获取当前批次号
     *
     * @return
     */
    public static String getCurrentBatchNo(){
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.RELEASEDATE);
        String currentBatchNo = sdf.format(new Date());
        return currentBatchNo;
    }

    /**
     * 根据实体模板文件内容来解析获取所有的实体模板英文名
     * @param content
     * @return
     */
    public static List getEntityTempalteNames(String content)
    {
        List<String> entityTemplatelList = new ArrayList();
        Pattern regex = Pattern.compile("\\$([^.]*)\\.");
        Matcher matcher = regex.matcher(content);
        while (matcher.find()) {
            entityTemplatelList.add(matcher.group(1));
        }
        //使用流操作去重
        entityTemplatelList = entityTemplatelList.stream().distinct().collect(Collectors.toList());
        return entityTemplatelList;
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
     * 校验流水线设置触发规则定时任务的cron表达式
     * @param pipeline
     */
    public static void checkCronExpression(Pipeline pipeline) {
        if (pipeline == null){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"流水线数据不能为空！！！"});
        }
        //校验cron表达式
        if (!CommonUtils.isNullOrEmpty(pipeline.getTriggerRules()) &&
                !CommonUtils.isNullOrEmpty(pipeline.getTriggerRules().getSchedule())){
            Schedule schedule = pipeline.getTriggerRules().getSchedule();
            if (!CommonUtils.isNullOrEmpty(schedule.getSwitchFlag()) &&
                    !CommonUtils.isNullOrEmpty(schedule.getScheduleParams()) && schedule.getSwitchFlag()){
                for (Map scheduleMap : schedule.getScheduleParams()){
                    if (!CommonUtils.isNullOrEmpty(scheduleMap.get(Dict.CRON))){
                        String cron = (String) scheduleMap.get(Dict.CRON);
                        boolean validExpression = CronExpression.isValidExpression(cron);
                        if (!validExpression){
                            throw new FdevException(ErrorConstants.PARAM_CONFIG_ERROR, new String[]{"您设置的触发规则定时类型cron表达式不正确"});
                        }
                    }
                }
            }
        }
    }

    /**
     * 写入文件
     * @param file
     * @param sourceUrl
     * @param targetUrl
     * @throws IOException
     */
    public static void write2FileByChar(MultipartFile file, String sourceUrl, String targetUrl) throws IOException {
        InputStream fis = null;
        InputStreamReader isr = null;
        FileOutputStream fos = null;
        File outFile = new File(targetUrl);
        try {
            fos = new FileOutputStream(outFile);
            if (CommonUtils.isNullOrEmpty(sourceUrl)){
                fis = file.getInputStream();
            }else {
                File sourceFile = new File(sourceUrl);
                fis = new FileInputStream(sourceFile);
            }
            //直接用inputstream会乱码
            isr = new InputStreamReader(fis, "GBK");
            char[] bytes = new char[2048];
            int len;
            while ((len = isr.read(bytes)) != -1){
                String info =new String(bytes,0,len);
                logger.info("从源文件写出的文件内容为：********************************"+info);
                fos.write(info.getBytes());
                logger.info("写入到目标文件内容为：***********************************"+info);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (fis != null){
                fis.close();
            }
            if (isr != null){
                isr.close();
            }
            if (fos != null){
                fos.close();
            }
        }
    }

    public static <T> T copyEntityObj(Object obj, Class<T> clazz) throws JsonProcessingException {
        /************************************/
        String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(obj);
        return com.alibaba.fastjson.JSONObject.parseObject(jsonString, clazz);
        /****************************************/
    }

    public static <T> Boolean checkoutAllFields(T obj1, T obj2) throws NoSuchFieldException, IllegalAccessException {
        //对比class的type
        if (obj1.getClass().getTypeName().equals(obj2.getClass().getTypeName())) {
            for (Field declaredField2 : obj2.getClass().getDeclaredFields()) {
                declaredField2.setAccessible(true);
                String fieldName2 = declaredField2.getName();
                Field declaredField1 = obj1.getClass().getDeclaredField(fieldName2);
                declaredField1.setAccessible(true);
                //对比字段的type
                if (declaredField2.getGenericType().getTypeName().equals(declaredField1.getGenericType().getTypeName())) {
                    //获取entity各自的value对比
                    Object value2 = declaredField2.get(obj2);
                    Object value1 = declaredField1.get(obj1);
                    if ((value2 != null && value1 != null) && value2.equals(value1)) {
                        //一致
                    } else if (value2 != null && !value2.equals(value1)) {
                        return false;
                    }else if (value1 != null && value1.equals(value2)) {
                        return false;
                    }
                } else {
                    throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"entity field type is not same!"});
                }
            }
        } else {
            throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"entity type is not same!"});
        }
        return true;
    }


    /**
     * 计算出当前时间与目标时间的差值是否大于半小时，大于半小时返回true
     *
     * @param startTime
     * @param format
     * @return
     * @throws ParseException
     */
    public static Boolean calcTimesTarCurrent(String startTime, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date startDate = sdf.parse(startTime);
        long startDateTime = startDate.getTime();
        Date currentDate = new Date();
        long currentDateTime = currentDate.getTime();
        if (startDateTime > currentDateTime) {
            return false;
        }else {
            long subTime = currentDateTime - startDateTime;
            if (subTime > 30 * 60 * 1000)
                return true;
        }
        return false;
    }
}

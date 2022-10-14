
package com.spdb.fdev.base.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.base.dict.Constants;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Configuration
public class CommonUtils {
    public static final String DATE_COMPACT_FORMAT = "yyyyMMdd";
    public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RELEASEDATE = "yyyyMMddHHmmss";
    public static final String RELEASEDATESS = "yyyyMMddHHmmssSSS";
    public static final String DATE_PARSE = "yyyy-MM-dd";
    public static final String AUTH_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String INPUT_DATE = "yyyy/MM/dd";
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

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

    /**
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public static User getSessionUser() throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
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

    public static Object runCmd(String cmd) {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = null;
        LineNumberReader br = null;
        InputStreamReader reader = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
            reader = new InputStreamReader(process.getInputStream());
            br = new LineNumberReader(reader);
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("执行 {} 的时候出现错误，错误信息如下{}", cmd, e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception exception) {
                logger.error("关闭流是吧");
            }

        }
        return null;
    }


    public static Object runPython(String path, String cmd) {
        String[] cmdA = {"python", path, cmd};
        Process process = null;
        LineNumberReader success = null;
        LineNumberReader error = null;
        InputStreamReader inputStreamReader = null;
        InputStreamReader reader = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            success = new LineNumberReader(inputStreamReader);
            reader = new InputStreamReader(process.getErrorStream());
            error = new LineNumberReader(reader);
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
            if (!CommonUtils.isNullOrEmpty(er.toString())) {
                FdevException fdevException = new FdevException(ErrorConstants.AUTO_RELEASE_FAILED);
                fdevException.setMessage(er.toString());
                throw fdevException;
            }
            return sc.toString();
        } catch (IOException e) {
            logger.error("执行{}的时候出现错误，错误信息如下{}", path, e);
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
                if (success != null) {
                    success.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (Exception exception) {
                logger.error("关闭流失败");
            }
        }
        return null;
    }

    public static Object runPythonThreeParamter(String path, String args1, String args2, String args3) {
        logger.info("脚本目录：{},参数1：{},参数2：{},参数3：{}", path, args1, args2, args3);
        String[] cmdA = {"python", path, args1, args2, args3};
        Process process = null;
        LineNumberReader success = null;
        LineNumberReader error = null;
        InputStreamReader inputStreamReader = null;
        InputStreamReader reader = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            success = new LineNumberReader(inputStreamReader);
            reader = new InputStreamReader(process.getErrorStream());
            error = new LineNumberReader(reader);
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
            if (!CommonUtils.isNullOrEmpty(er.toString())) {
                FdevException fdevException = new FdevException(ErrorConstants.AUTO_RELEASE_FAILED);
                fdevException.setMessage(er.toString());
                throw fdevException;
            }
            return sc.toString();
        } catch (IOException e) {
            logger.error("执行{}的时候出现错误，错误信息如下{}", path, e);
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
                if (success != null) {
                    success.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                logger.error("关闭流失败");
            }
        }
        return null;
    }

    public static Object runPythonArray(String path, String[] args) {
        String[] cmdA = new String[args.length + 2];
        cmdA[0] = "python";
        cmdA[1] = path;
        for(int i=0;i<args.length;i++){
            cmdA[i+2] = args[i];
        }
        Process process = null;
        LineNumberReader success = null;
        LineNumberReader error = null;
        InputStreamReader inputStreamReader = null;
        InputStreamReader reader = null;
        try {
            process = Runtime.getRuntime().exec(cmdA);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            success = new LineNumberReader(inputStreamReader);
            reader = new InputStreamReader(process.getErrorStream());
            error = new LineNumberReader(reader);
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
            if (!CommonUtils.isNullOrEmpty(er.toString())) {
                FdevException fdevException = new FdevException(ErrorConstants.AUTO_RELEASE_FAILED);
                fdevException.setMessage(er.toString());
                throw fdevException;
            }
            return sc.toString();
        } catch (IOException e) {
            logger.error("执行{}的时候出现错误，错误信息如下{}", path, e);
        } finally {
            try {
                if (error != null) {
                    error.close();
                }
                if (success != null) {
                    success.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                logger.error("关闭流失败");
            }
        }
        return null;
    }

    public static void gitClone(String localRepository, String gitlabRepository) throws Exception {
        // 本地仓库的 地址
        File partDirGit = new File(localRepository + "/.git");
        // 首次本地没有项目的时候进行克隆
        if (!partDirGit.exists()) {
            // git clone
            GitUtils.gitCloneFromGitlab(gitlabRepository, localRepository);
            logger.info("克隆项目文件时成功");
        }
    }

    private static String saveFile(String dir, MultipartFile multipartFile) throws Exception {
        // 保存文件
        StringBuilder builder = new StringBuilder();
        if (!"".equals(multipartFile.getOriginalFilename())) {
            builder.append(multipartFile.getOriginalFilename());
            File file1 = new File(new File(dir).getAbsoluteFile() + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(file1);
        }
        logger.info("保存文件成功！");
        return builder.toString();
    }

    /*public static String md5(InputStream is) {
        StringBuilder md5 = new StringBuilder();
        try {
            byte[] buf = new byte[1024 * 1024];
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            int num;
            do {
                num = is.read(buf);
                if (num > 0) {
                    md5Digest.update(buf, 0, num);
                }
            } while (num != -1);
            is.close();
            byte[] b = md5Digest.digest();
            for (int i = 0; i < b.length; i++) {
                // 每个字节转换成两位的十六进制数
                md5.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception e) {
            logger.error("转换错误 {}", e);
        }
        return md5.toString();
    }*/

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

    /**
     * 生成redisson锁key
     *
     * @param objectUniqueKey
     * @return
     */
    public static String generateRlockKey(String objectUniqueKey) {
        return new StringBuilder(Constants.REDISSON_LOCK_NAMESPACE).append(":").append(objectUniqueKey).toString();
    }

    /**
     * 上传文件到git项目
     * @param files 文件
     * @param uploadUserName 上传人名字
     * @param localRepository 本地目录
     * @param gitlabRepository git项目名称
     * @throws Exception
     */
    public static void fileUploadDir(MultipartFile files, String uploadUserName, String localRepository,
                                  String gitlabRepository) throws Exception {
        // 从git克隆或拉取更新项目
        gitCloneOrPull(localRepository, gitlabRepository);
        File file = new File(localRepository);
        // 判断将要创建的文件夹是否存在
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
            logger.info("上传文件时目录构建成功");
        }
        // 保存文件
        String fileString = saveFile(localRepository, files);
        GitUtils.gitPushFromGitlab(localRepository, uploadUserName + "上传了" + fileString);
        logger.info("文件上传的接口，push文件成功！");
    }

    public static void deleteFile(String localRepository, String gitlabRepository, String fileAddress, String delUser) {
        // 从git克隆或拉取更新项目
        gitCloneOrPull(localRepository, gitlabRepository);
        File file = new File(localRepository + fileAddress);
        if(file.exists()) {
            file.delete();
        }
        GitUtils.deleteFilePushToGitlab(localRepository, delUser + "删除了文件" + fileAddress);
    }

    public static void gitCloneOrPull(String localRepository, String gitlabRepository) {
        // 本地仓库的 地址
        File partDirGit = new File(localRepository + "/.git");
        // 首次本地没有项目的时候进行克隆
        if (!partDirGit.exists()) {
            // git clone
            GitUtils.gitCloneFromGitlab(gitlabRepository, localRepository);
            logger.info("克隆项目文件时成功");
        } else {
            // 项目拉取更新本地文件
            GitUtils.gitPullFromGitlab(localRepository);
        }
    }

    public static void createDirectory(String loacl_path) {
        File directory = new File(loacl_path);
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdirs();
            logger.info("{}目录构建成功", loacl_path);
        } else {
            logger.error("{}目录已存在", loacl_path);
        }
    }

    private static String encode(String str, String charset) throws UnsupportedEncodingException {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(str);
        StringBuffer b = new StringBuffer();
        while(m.find()) {
            m.appendReplacement(b, URLEncoder.encode(m.group(0), charset));
        }
        m.appendTail(b);
        return b.toString();
    }

    public static void pressToZip(String directory_name, String zip_name) {
        ZipOutputStream zos = null;
        FileOutputStream fos =null;
        try {
            fos = new FileOutputStream(zip_name);
            zos = new ZipOutputStream(fos);
            File sourceFile = new File(directory_name);
            compress(sourceFile, zos, sourceFile.getName());
        } catch (Exception e) {
            logger.error("异常:{}", e);
        } finally {
            if(zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws IOException {
        byte[] buf = new byte[2048];
        if(sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = null;
            try {
                in = new FileInputStream(sourceFile);
                while((len = in.read(buf)) != -1) {
                    zos.write(buf, 0 , len);
                }
                zos.closeEntry();
            } finally {
                if(in!=null) {
                    in.close();
                }
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            } else {
                for(File file : listFiles) {
                    compress(file, zos, name + "/" + file.getName());
                }
            }
        }
    }

    public static boolean deleteDirectory(File project, String localProjectPath) {
        if(CommonUtils.isNullOrEmpty(project) || !project.exists()) {
            logger.info("本地目录不正确，请检查{}", localProjectPath);
            return false;
        }
        File[] files = project.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                deleteDirectory(file, null);
            } else {
                file.delete();
            }
        }
        project.delete();
        return true;
    }

    public static String numberAfterFillZero(String str, int length){
        StringBuilder sb = new StringBuilder(str);
        if(sb.length() >= length){
            return sb.toString();
        }else{
            while (sb.length() < length){
                sb.append("0");
            }
        }
        return sb.toString();
    }

    public static boolean laterThanOneDay(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(INPUT_DATE);
        Date d = sf.parse(date);
//        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, 2);
        Date plus = calendar.getTime();
        boolean flag = false;
        if(plus.after(new Date())) {
            flag = true;
        }
        return flag;
    }

    public static String switchRelTestOrPackage(String str){
        if(CommonUtils.isNullOrEmpty(str)){
            return "";
        }
        switch (str){
            case "0" : str = "未打包";
                break;
            case "1" : str = "未提测";
                break;
            case "2" : str = "已提测";
                break;
            default:break;
        }
        return str;
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
            if(null == map) {
                throw  new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
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
}

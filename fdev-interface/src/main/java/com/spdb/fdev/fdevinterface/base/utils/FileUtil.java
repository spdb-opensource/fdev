package com.spdb.fdev.fdevinterface.base.utils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileUtil {

    private FileUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * clone远程项目
     *
     * @param cloneUrl
     * @param branchName
     * @param gitClonePath
     */
    public static void cloneProject(String appServiceId, String cloneUrl, String branchName, String gitClonePath, String gitCloneUser, String gitClonePassword, String commonServiceCloneUrl) {
        if (Dict.MSPER_WEB_COMMON_SERVICE.equals(appServiceId)) {
            cloneUrl = commonServiceCloneUrl;
        }
        if (StringUtils.isEmpty(cloneUrl)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"没有获取到该应用" + appServiceId + "的git clone url!"});
        }
        // 检测本地文件夹
        File localFile = new File(gitClonePath);
        if (localFile.exists()) {
            FileUtil.deleteFiles(localFile);
        } else
            localFile.mkdir();

        // clone git 代码
        CloneCommand clone = Git.cloneRepository().setURI(cloneUrl).setDirectory(localFile).setBranch(branchName);
        // SSH方式
        if (cloneUrl.contains(Dict.GIT_A)) {
            throw new FdevException(ErrorConstants.CLONE_URL_ERROR);
        }
        // HTTP方式
        if (cloneUrl.contains(Dict.HTTP) || cloneUrl.contains(Dict.HTTPS)) {
            UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(gitCloneUser, gitClonePassword);
            clone.setCredentialsProvider(user);
        } else {
            throw new FdevException(ErrorConstants.CLONE_URL_ERROR);
        }
        Git git = null;
        try {
            git = clone.call();
        } catch (Exception e) {
            logger.error("{}", ErrorConstants.GITLAB_SERVER_ERROR + e.getMessage());
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    /**
     * 删除文件夹
     *
     * @param file 文件
     */
    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        boolean result = file.delete();
        if (!result) {
            logger.info("删除{}文件失败！", file.getName());
        }
    }

    /**
     * 获取src文件夹的路径
     *
     * @param localPath 文件路径
     * @return
     */
    public static List<String> getSrcPath(String localPath, List<String> srcPathList) {
        File file = new File(localPath);
        if (!file.exists() || !file.isDirectory()) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"Clone文件夹为空"});
        }
        // 获取localPath路径下的所有文件
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                if ((f.getName().contains("."))) {
                    continue;
                }
                // 判断文件名是否为src
                if (Dict.SRC.equals(f.getName())) {
                    // 获取src文件的路径
                    String path = f.getPath();
                    srcPathList.add(path);
                } else {
                    // 在其他文件夹中寻找src
                    getSrcPath(f.getPath(), srcPathList);
                }
            }
        }
        return srcPathList;
    }

    /**
     * 获取指定文件或者文件夹路径
     *
     * @param srcPath
     * @param targetPath
     * @return
     */
    public static String getFilePath(List<String> srcPath, String mainResources, String targetPath) {
        String filePath = null;
        for (String src : srcPath) {
            filePath = src + mainResources + targetPath;
            File file = new File(filePath);
            if (file.exists()) {
                return filePath;
            }
            filePath = null;
        }
        return filePath;
    }

    /**
     * 获取xml文件根元素
     *
     * @param xmlPath
     * @param errorCode 解析接口类型的错误码
     * @return
     */
    public static Element getXmlRootElement(String xmlPath, String errorCode) {
        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) {
            return null;
        }
        SAXReader saxReader = new SAXReader();
        Document document;
        try (FileInputStream fileInputStream = new FileInputStream(xmlFile)) {
            // 不需要验证、读取DTD文件
            saxReader.setValidation(false);
            saxReader.setEntityResolver(new IgnoreDTDEntityResolver());
            document = saxReader.read(fileInputStream);
        } catch (Exception e) {
            logger.error("{}", Constants.ANALYSIS_FILE_ERROR + xmlPath + "，" + e.getMessage());
            throw new FdevException(errorCode, new String[]{Constants.ANALYSIS_FILE_ERROR + xmlPath + "！"});
        }
        return document.getRootElement();
    }

    /**
     * 获取文件夹下所有子文件路径（不包括子文件夹）
     *
     * @param filesPath 文件夹路径
     * @return
     */
    public static List<String> getsubFilesPath(String filesPath) {
        File files = new File(filesPath);
        if (!files.exists() || !files.isDirectory()) {
            logger.info("不能获取到{}文件夹的子文件路径！", filesPath);
        }
        List<String> subFilesPath = new ArrayList<>();
        File[] subFiles = files.listFiles();
        for (File file : subFiles) {
            if (!file.isDirectory()) {
                subFilesPath.add(file.getPath());
            }
        }
        return subFilesPath;
    }

    /**
     * 获取trans文件夹下所有子文件路径
     *
     * @param filesPath 文件夹路径
     * @return
     */
    public static List<String> getSubTransFilesPath(String filesPath, List<String> subFilesPath) {
        File files = new File(filesPath);
        if (!files.exists() || !files.isDirectory()) {
            logger.info("不能获取到{}文件夹的子文件路径！", filesPath);
        }
        File[] subFiles = files.listFiles();
        for (File file : subFiles) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                getSubTransFilesPath(file.getPath(), subFilesPath);
            }
            if (!Constants.TRS_DEMO.equals(fileName) && fileName.endsWith(Constants.XML_FILE)
                    && (fileName.contains(Constants.TRANS_FILE) || fileName.contains(Constants.TRS_FILE)))
                subFilesPath.add(file.getPath());
        }
        return subFilesPath;
    }

    /**
     * 解析json报文参数
     *
     * @param jsonObject
     * @return
     */
    public static List<Param> analysisParams(JsonObject jsonObject, Integer schemaType) {
        if (jsonObject == null) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        JsonObject properties = jsonObject.getAsJsonObject(Dict.PROPERTIES);
        if (properties == null) {
            return Lists.newArrayList();
        }
        //遍历JsonObject中的Required并存入集合
        List<String> requiedStr = new ArrayList<>();
        if (schemaType == 4) {
            JsonArray required = jsonObject.getAsJsonArray(Dict.REQUIRED);
            if (required != null) {
                for (JsonElement jsonElement : required) {
                    requiedStr.add(jsonElement.getAsString());
                }
            }
        }
        for (Map.Entry<String, JsonElement> propEntry : properties.entrySet()) {
            Param param = new Param();
            param.setName(propEntry.getKey());
            JsonObject value = propEntry.getValue().getAsJsonObject();
            if (schemaType == 4) {
                if (requiedStr.contains(propEntry.getKey())) {
                    value.addProperty(Dict.REQUIREDAFTER, "true");
                } else {
                    value.addProperty(Dict.REQUIREDAFTER, "false");
                }
            }
            for (Map.Entry<String, JsonElement> valueEntry : value.entrySet()) {
                switch (valueEntry.getKey()) {
                    case Dict.DESCRIPTION:
                        param.setDescription(valueEntry.getValue().getAsString());
                        break;
                    case Dict.TYPE:
                        param.setType(valueEntry.getValue().getAsString());
                        break;
                    case Dict.REQUIRED:
                        if (schemaType != 4) {
                            param.setRequired(valueEntry.getValue().getAsString().equals(Dict.TRUE) ? 1 : 0);
                        }
                        break;
                    case Dict.REQUIREDAFTER:
                        if (schemaType == 4) {
                            param.setRequired(valueEntry.getValue().getAsString().equals(Dict.TRUE) ? 1 : 0);
                        }
                        break;
                    case Dict.MAXLENGTH:
                        param.setMaxLength(valueEntry.getValue().getAsInt());
                        break;
                    case Dict.PROPERTIES:
                        param.setParamList(analysisParams(value, schemaType));
                        break;
                    case Dict.ITEMS:
                        param.setParamList(analysisItem(param, valueEntry.getValue().getAsJsonObject(), schemaType));
                        break;
                    default:
                        break;
                }
            }
            paramList.add(param);
        }
        return paramList;
    }

    /**
     * 解析json报文参数里的items
     *
     * @param item
     * @return
     */
    public static List<Param> analysisItem(Param param, JsonObject item, Integer schemaType) {
        if (item == null) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        JsonObject properties = new JsonObject();
        try {
            String type = item.get(Dict.TYPE).getAsString();
            param.setType(type + "[]");
            if (Dict.OBJECT.equals(type)) {
                if (item.keySet().contains(Dict.PROPERTIES)) {
                    properties = item.getAsJsonObject(Dict.PROPERTIES);
                }
            }
            if (Dict.ARRAY.equals(type)) {
                Param itemParam = new Param();
                List<Param> subParamList = analysisItem(itemParam, item.getAsJsonObject(Dict.ITEMS), schemaType);
                itemParam.setParamList(subParamList);
                paramList.add(itemParam);
            }
        } catch (Exception e) {
            logger.info("items结点下没有type或者properties属性！");
            properties = item;
        }
        //遍历JSonObject中的Required并存入集合
        List<String> requiedStr = new ArrayList<>();
        if (schemaType == 4) {
            JsonArray required = item.getAsJsonArray(Dict.REQUIRED);
            if (required != null) {
                for (JsonElement jsonElement : required) {
                    requiedStr.add(jsonElement.getAsString());
                }
            }
        }
        for (Map.Entry<String, JsonElement> propEntry : properties.entrySet()) {
            Param itemParam = new Param();
            itemParam.setName(propEntry.getKey());
            JsonObject value = propEntry.getValue().getAsJsonObject();
            if (schemaType == 4) {
                if (requiedStr.contains(propEntry.getKey())) {
                    value.addProperty(Dict.REQUIREDAFTER, "true");
                } else {
                    value.addProperty(Dict.REQUIREDAFTER, "false");
                }
            }
            for (Map.Entry<String, JsonElement> valueEntry : value.entrySet()) {
                switch (valueEntry.getKey()) {
                    case Dict.DESCRIPTION:
                        itemParam.setDescription(valueEntry.getValue().getAsString());
                        break;
                    case Dict.TYPE:
                        itemParam.setType(valueEntry.getValue().getAsString());
                        break;
                    case Dict.REQUIRED:
                        if (schemaType != 4) {
                            itemParam.setRequired(valueEntry.getValue().getAsString().equals(Dict.TRUE) ? 1 : 0);
                        }
                        break;
                    case Dict.REQUIREDAFTER:
                        if (schemaType == 4) {
                            itemParam.setRequired(valueEntry.getValue().getAsString().equals(Dict.TRUE) ? 1 : 0);
                        }
                        break;
                    case Dict.MAXLENGTH:
                        itemParam.setMaxLength(valueEntry.getValue().getAsInt());
                        break;
                    case Dict.PROPERTIES:
                        itemParam.setParamList(analysisParams(value, schemaType));
                        break;
                    case Dict.ITEMS:
                        itemParam.setParamList(analysisItem(itemParam, valueEntry.getValue().getAsJsonObject(), schemaType));
                        break;
                    default:
                        break;
                }
            }
            paramList.add(itemParam);
        }
        return paramList;
    }

    /**
     * 兼容交易详情
     *
     * @param paramList
     */
    public static List<Map<String, Object>> convertToTransDetail(List<Param> paramList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Param param : paramList) {
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.NAME, param.getName());
            map.put(Dict.CONTENT, param.getDescription());
            map.put(Dict.OPTION, param.getRequired());
            map.put(Dict.LENGTH, param.getMaxLength());
            String type = param.getType();
            map.put(Dict.TYPE, type);
            List<Param> itemList = param.getParamList();
            if (CollectionUtils.isNotEmpty(itemList)) {
                List<Map<String, Object>> mapItemList = convertToTransDetail(itemList);
                map.put(Dict.ITEM, mapItemList);
            }
            mapList.add(map);
        }
        return mapList;
    }

    public static void export(String excelPath, String temp, XSSFWorkbook workBook) {
        try {
            File tf = new File(excelPath);
            File fp = tf.getParentFile();
            if (!fp.exists()) {
                if (!fp.mkdirs()) {
                    throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"创建目录失败"});
                }
            }
            File file = new File(temp);
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File ff : files) {
                    if (!ff.delete()) {
                        throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + temp + "文件失败"});
                    }
                }
                if (!tf.createNewFile()) {
                    throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                }
            } else {
                if (!tf.createNewFile()) {

                    throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                }
            }
            logger.info("excelPath = " + excelPath);
            try (OutputStream out = new FileOutputStream(excelPath)) {
                workBook.write(out);
                out.flush();
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"生成文件失败\n" + e.getMessage()});
        }
    }
    public static void newExport(String excelPath, String temp, XSSFWorkbook workBook) {
        try {
            File tf = new File(excelPath);
            File fp = tf.getParentFile();
            if (!fp.exists()) {
                if (!fp.mkdirs()) {
                    throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"创建目录失败"});
                }
            }
            File file = new File(temp);
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File ff : files) {
                    if (!ff.delete()) {
                        throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + temp + "文件失败"});
                    }
                }
                if (!tf.createNewFile()) {
                    throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                }
            } else {
                if (!tf.createNewFile()) {

                    throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                }
            }
            try (OutputStream out = new FileOutputStream(excelPath)) {
                workBook.write(out);
                out.flush();
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"生成文件失败\n" + e.getMessage()});
        }
    }
    /**
     * 下载excel
     *
     * @param fileName  excel名称
     * @param excelPath excel路径
     * @param response  请求头
     */
    public static void commonDown(String fileName, String excelPath, HttpServletResponse response) {
        File file = new File(excelPath);
        if (!file.exists()) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"{} 文件不存在", fileName});
        }
        try (OutputStream output = response.getOutputStream();
             FileInputStream in = new FileInputStream(excelPath)) {
            // 读取文件
            response.reset();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "content-type");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            response.flushBuffer();
            logger.info("导出成功");
        } catch (Exception e2) {
            logger.error("导出失败: {}", e2.getMessage());
        }
        File execle = new File(excelPath);
        if (!execle.delete()) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + excelPath + "文件失败"});
        }
    }

    /**
     * 下载文件
     *
     * @param fileName  文件名称
     * @param worldpath 文件路径
     * @param response  请求头
     */
    public static void downloadFile(String fileName, String worldpath, HttpServletResponse response) {
        File file = new File(worldpath);
        if (!file.exists()) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"{} 文件不存在", fileName});
        }
        try (OutputStream output = response.getOutputStream();
             FileInputStream in = new FileInputStream(worldpath)) {
            // 读取文件
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            response.flushBuffer();
            logger.info("导出成功");
        } catch (Exception e2) {
            logger.error("导出失败: {}", e2.getMessage());
        }
    }

    /**
     * 判断是否为空或null
     *
     * @param obj
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
                if ((obj == null) || (("").equals(((String) obj).trim()))) {
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
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + fileDir + "出错，" + e.getMessage()});
        }
        try (PrintWriter printWriter = new PrintWriter(fileDir, StandardCharsets.UTF_8.name())) {
            printWriter.write(fileContent);
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"生成" + fileDir + "出错，" + e.getMessage()});
        }
    }

    /**
     * 创建文件目录
     *
     * @param fileDir
     */
    public static void createDir(String fileDir) {
        File localFile = new File(fileDir);
        // 不存在，则创建，存在的话不做任何操作
        if (!localFile.exists()) {
            localFile.mkdir();
        }
    }

    /**
     * 删除文件目录
     *
     * @param fileDir
     */
    public static void deleteDir(String fileDir) {
        File localFile = new File(fileDir);
        // 存在，则删除
        if (localFile.exists()) {
            FileUtil.deleteFiles(localFile);
        }
    }

    /**
     * 将字符串转换为层级json格式
     *
     * @param jsonStr json格式字符串
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    stringBuilder.append(current);
                    break;
                case '{':
                case '[':
                    stringBuilder.append(current);
                    if (!isInQuotationMarks) {
                        stringBuilder.append('\n');
                        indent++;
                        addIndentBlank(stringBuilder, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        stringBuilder.append('\n');
                        indent--;
                        addIndentBlank(stringBuilder, indent);
                    }
                    stringBuilder.append(current);
                    break;
                case ',':
                    stringBuilder.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        stringBuilder.append('\n');
                        addIndentBlank(stringBuilder, indent);
                    }
                    break;
                default:
                    stringBuilder.append(current);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 添加空格
     *
     * @param stringBuilder
     * @param indent
     */
    private static void addIndentBlank(StringBuilder stringBuilder, int indent) {
        for (int i = 0; i < indent; i++) {
            stringBuilder.append('\t');
        }
    }

}



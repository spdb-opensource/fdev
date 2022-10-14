package com.fdev.docmanage.web;

import java.io.File;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fdev.docmanage.entity.*;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.service.AppFilesService;
import com.fdev.docmanage.service.AuthService;
import com.fdev.docmanage.service.UserInfoService;
import com.fdev.docmanage.util.CommonUtils;
import com.fdev.docmanage.util.EncryptionTool;
import com.fdev.docmanage.util.HttpClientUtil;
import com.fdev.docmanage.util.MyUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;

/**
 * Fdev平台对接Wps平台接口
 */
@RestController
@EnableScheduling
@RequestMapping(value = "/api/wps")
public class WpsController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Token token = null;

    @Resource
    private Constants constants;

    @Resource
    private AuthService authService;

    @Resource
    private HttpClientUtil httpClientUtil;

    @Resource
    private AppFilesService appFilesService;

    @Resource
    private UserInfoService userInfoService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 初始化完成执行方法
     *
     * @throws Exception
     */
    @PostConstruct
    public void init() {
        getToken();
    }

    /**
     * 获得token
     *
     * @throws Exception
     */
    @Scheduled(fixedRate = 3000000)
    public void getToken() {
        try {
            TokenParameter tokenParameter = new TokenParameter();
            tokenParameter.setGrantType("client_credentials");
            tokenParameter.setScope(constants.getScope());

            HeaderParameter headerParameter = new HeaderParameter();
            headerParameter.setContentType(Constants.CONTENT_TYPE_FORM);
            headerParameter.setAuthorization(constants.getAuthorization());

            String json = authService.getToken(tokenParameter, headerParameter);
            logger.info("method: getToken, data: {}, tokenParameter:{}, headerParameter:{}", json, tokenParameter.toString(), headerParameter.toString());
            // 如果成功获取了token
            if (!StringUtils.isEmpty(json)) {
                token = JSON.parseObject(json, Token.class);
                logger.info("method: getToken, token: {}", token);
            } else {
                token = null;
            }
        } catch (Exception e) {
            logger.error("getToken: mssage: {}", e.getMessage());
        }
    }


    /**
     * 上传文件 会话
     *
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {"file"})
    @RequestMapping(value = "/volumes/files/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult uploadFile(HttpServletRequest request, MultipartFile uploadFile) throws Exception {
        if (!userVerifyUtil.userGroupVerify("互联网应用")) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户不是互联网应用组的用户，身份验证失败"});
        }
        if (CommonUtils.isNullOrEmpty(uploadFile)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"uploadFile（文件）"});
        }
        String file = request.getParameter("file");
        String uploadStage = request.getParameter("uploadStage");
        String userObj = request.getParameter("user");
        User user = JSONObject.parseObject(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) || CommonUtils.isNullOrEmpty(user.getUser_name_cn()) &&
                CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }
        Long fileSize = uploadFile.getSize();
        String fileName = uploadFile.getOriginalFilename();
        //判断token值
        if (token == null) {
            getToken();
        }
        String fileNameConflictBehavior = "rename";
        //查询该上传阶段是否有文件上传记录，若有，删除该文件
        if (!CommonUtils.isNullOrEmpty(uploadStage)) {
            List<UserInfo> userInfoList = userInfoService.queryUserInfoByUploadStage(uploadStage, file);
            for (UserInfo userInfo : userInfoList) {
                HeaderParameter head = new HeaderParameter();
                head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
                PathParameter pathParameter = new PathParameter();
                pathParameter.setFile(userInfo.getFileId());
                pathParameter.setVolume(constants.getVolume());
                appFilesService.deleteFile(head, pathParameter);
                //更新操作记录
                userInfoService.updateUserOperation(userInfo.getFileId(),uploadStage);
            }
        } else {
            logger.info("非设计还原审核处上传文件");
        }

        //第一步 创建上传事务  createUpload
        String uploadStep1 = createUpload(token, constants.getVolume(), file, fileName, fileSize,fileNameConflictBehavior);
        MyUtil.dataProcessing(uploadStep1);
        logger.info("method: uploadFile, uploadStep1: {}, token: {}", uploadStep1, token);
        //第二步 上传文件至文件服务器
        /**--------------------------- 步骤1返回参数解析 用于第二步上传文件传入参数 ------------------------------- **/
        JSONObject jsonObject = JSON.parseObject(uploadStep1);
        //参数
        String feedback = jsonObject.getString("feedback");
        String uploadRequests = jsonObject.getString("upload_requests");
        JSONObject jsonObject1 = JSON.parseObject(uploadRequests);
        String value = jsonObject1.getString("value");

        List<String> ss = JSON.parseArray(value, String.class);
        String temp = ss.get(0);

        String requestParam = JSON.parseObject(temp).getString("request");
        JSONObject jsonObject2 = JSON.parseObject(requestParam);
        //参数url
        String url = jsonObject2.getString("url");

        String form = jsonObject2.getString("form");
        List<String> s2 = JSON.parseArray(form, String.class);
        //参数key x-amz-credential x-amz-date x-amz-algorithm x-amz-signature policy
        String key = "", xAmzCredential = "", xAmzDate = "", xAmzAlgorithm = "", xAmzSignature = "", policy = "";
        key = JSON.parseObject(s2.get(0)).getString("value");
        xAmzCredential = JSON.parseObject(s2.get(1)).getString("value");
        xAmzDate = JSON.parseObject(s2.get(2)).getString("value");
        xAmzAlgorithm = JSON.parseObject(s2.get(3)).getString("value");
        xAmzSignature = JSON.parseObject(s2.get(4)).getString("value");
        policy = JSON.parseObject(s2.get(5)).getString("value");
        /***************** 向云平台发送上传文档*******************/
        Map<String, String> mapHeader = new HashMap<String, String>();
        mapHeader.put("Content-Type", Constants.CONTENT_TYPE_FORM1);
        mapHeader.put("Authorization", token.getTokenType() + " " + token.getAccessToken());
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put("file", uploadFile);
        mapParam.put("key", key);
        mapParam.put("x-amz-credential", xAmzCredential);
        mapParam.put("x-amz-date", xAmzDate);
        mapParam.put("x-amz-algorithm", xAmzAlgorithm);
        mapParam.put("x-amz-signature", xAmzSignature);
        mapParam.put("policy", policy);
        String uploadStep2 = httpClientUtil.doPostWithFile(url, mapParam, mapHeader);
        ;
        MyUtil.dataProcessing(uploadStep2);
        logger.info("method: uploadFile, uploadStep2: {}", uploadStep2);

        //第三步 提交上传事务    commitUpload
        /*************** ① 得到上传第二步返回的参数值     进而解析出  供上传第三步传递参数 *****************/
        JSONObject jsonObject3 = JSON.parseObject(uploadStep2);
        String sha1 = jsonObject3.getString("sha1");
        String storekey = jsonObject3.getString("storekey");

        /********** ② 发送上传  commit事务*******************/
        String uploadStep3 = commitUpload(token, constants.getVolume(), file, feedback, storekey, sha1, uploadFile);
        ;
        logger.info("method: uploadFile, uploadStep3: {}", uploadStep3);
        JSONObject result = MyUtil.dataProcessing(uploadStep3);
        String fileInfo = result.getString("file");
        JSONObject fileInfoMap = JSON.parseObject(fileInfo);
        String fileId = fileInfoMap.getString("id");

        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "文件上传");
        userMap.put("fileName", fileName);
        userMap.put("fileId", fileId);
        userMap.put("parentId", file);
        userMap.put("type", "wps");
        userMap.put("uploadStage", uploadStage);
        userInfoService.addUserInfo(userMap);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 上传-1 获取上传事务
     *
     * @throws Exception
     */
    public String createUpload(Token token, String volume, String file, String fileName, Long fileSize, String fileNameConflictBehavior) throws Exception {
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(file);
        pathParameter.setVolume(volume);

        RequestParameterBody requestBody = new RequestParameterBody();
        requestBody.setFileSize(fileSize);
        requestBody.setFileName(fileName);
        requestBody.setFileNameConflictBehavior(fileNameConflictBehavior);
        requestBody.setUploadMethod("POST");
        String createUpload = appFilesService.createUpload(head, pathParameter, requestBody);
        logger.info(createUpload);
        return createUpload;
    }

    /**
     * 上传-3 提交上传事务
     *
     * @throws Exception
     */
    public String commitUpload(Token token, String volume, String file, String feedback, String storekey, String sha1,
                               MultipartFile uploadFile) throws Exception {
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(file);
        pathParameter.setVolume(volume);
        pathParameter.setTransaction(storekey);

        RequestParameterBody requestBody = new RequestParameterBody();
        requestBody.setFeedback(feedback);
        requestBody.setStatus("commited");

        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(sha1)) {
            if (uploadFile != null) {
                File dest = File.createTempFile("temp", null);
                uploadFile.transferTo(dest);
                map.put("sha1", EncryptionTool.getFileSha1(dest));
            }
        } else {
            map.put("sha1", sha1);
        }
        requestBody.setFileHashes(map);
        String commitUpload = appFilesService.commitUpload(head, pathParameter, requestBody);
        logger.info(commitUpload);
        return commitUpload;
    }


    /**
     * 获取文件列表
     *
     * @throws Exception
     */
    @RequestMapping(value = "/volumes/files/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult retrieveDir(@RequestBody Map<String, String> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("value", new ArrayList<>());
            return JsonResultUtil.buildSuccess(map);
        }
        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());
        //下一页游标
        String next = null;
        String retrieveDir;
        Map<String, Object> retrieveDirMap = new HashMap<String, Object>();
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        try {
            do {
                retrieveDir = appFilesService.retrieveDir(head, pathParameter, next);
                JSONObject jsonObject = JSON.parseObject(retrieveDir);
                retrieveDirMap = (Map<String, Object>) jsonObject;
                lists.addAll((List<Map<String, Object>>) retrieveDirMap.get("value"));
                next = jsonObject.getString("next");
            } while (!CommonUtils.isNullOrEmpty(next));
        } catch (Exception e) {
            logger.error("method: uploadFile, message:{}, stack", e.getMessage(), e);
            throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
        }
        Map<String, Object> fileList = new HashMap<String, Object>();
        if (!"root".equals(requestParam.get("file"))) {
            //参数解析
            JSONObject jsonObject = JSON.parseObject(retrieveDir);
            String value = jsonObject.getString("value");
            List<String> list = JSON.parseArray(value, String.class);

            List<Map<String, Object>> list1 = null;
            Map<String, Object> map = new HashMap<String, Object>();
            //判断该文件夹下是否有文件
            if (list != null && list.size() > 0) {
                String fileVersions = null;
                list1 = new ArrayList<Map<String, Object>>();
                for (String file : list) {
                    if (!"folder".equals(JSON.parseObject(file).getString("type"))) {
                        JSONObject jsonObject1 = JSON.parseObject(file);
                        map = (Map<String, Object>) jsonObject1;
                        String fileId = jsonObject1.getString("id");
                        fileVersions = queryVersions(token, fileId, constants.getVolume());
                        JSONObject jsonObject2 = JSON.parseObject(fileVersions);
                        //文件版本列表
                        String value1 = jsonObject2.getString("value");
                        List<String> ss = JSON.parseArray(value1, String.class);
                        String temp = ss.get(0);
                        String number = JSON.parseObject(temp).getString("number");
                        map.put("number", number);
                        //返回字段增加uploadStage
						List<String> fileIds = new ArrayList<>();
						fileIds.add(fileId);
						Map mapUserInfo = new HashMap<>();
						mapUserInfo.put("fileIds", fileIds);
						List<UserInfo> userInfoList = userInfoService.queryUserInfo(mapUserInfo);
						if(!CommonUtils.isNullOrEmpty(userInfoList)){
						    for(UserInfo userInfo : userInfoList){
                                map.put("uploadStage",userInfo.getUploadStage());
                            }
						}else {
							map.put("uploadStage","null");
						}
                        list1.add(map);
                    } else {
                        JSONObject jsonObject1 = JSON.parseObject(file);
                        map = (Map<String, Object>) jsonObject1;
                        String folderId = (String) map.get("id");
                        PathParameter parameter = new PathParameter();
                        parameter.setFile(folderId);
                        parameter.setVolume(constants.getVolume());
                        //获得子文件夹（设计稿  审核稿）下的列表
                        String childList = appFilesService.retrieveDir(head, parameter, null);
                        JSONObject result = MyUtil.dataProcessing(childList);
                        //返回字段增加uploadStage
						Map<String, Object> mapResult = new HashMap<>();
						mapResult = result;
						List<Map> resultList = (List<Map>)mapResult.get("value");
						for(int i =0 ; i<resultList.size(); i++){
							mapResult = resultList.get(i);
							Map mapUserInfo = new HashMap<>();
							List<String> fileIds = new ArrayList<>();
							fileIds.add((String)mapResult.get("id"));
							mapUserInfo.put("fileIds", fileIds);
							List<UserInfo> userInfoList = userInfoService.queryUserInfo(mapUserInfo);
							if(!CommonUtils.isNullOrEmpty(userInfoList)){
                                for(UserInfo userInfo : userInfoList){
                                    mapResult.put("uploadStage",userInfo.getUploadStage());
                                }
							}else {
								mapResult.put("uploadStage","null");
							}
						}

						map.put("childFile", resultList);
						map.put("number", "1");
						list1.add(map);
                    }

                }
                fileList.put("value", list1);
                JSONObject json = new JSONObject(fileList);
                retrieveDir = JSON.toJSONString(json) + "\n";
                JSONObject result = MyUtil.dataProcessing(retrieveDir);
                return JsonResultUtil.buildSuccess(result);
            }
        }
        fileList.put("value", lists);
        JSONObject json = new JSONObject(fileList);
        retrieveDir = JSON.toJSONString(json) + "\n";
        JSONObject result = MyUtil.dataProcessing(retrieveDir);
        logger.info("method: retrieveDir, data: {}", retrieveDir);
        return JsonResultUtil.buildSuccess(result);

    }


    /**
     * 下载文件
     *
     * @throws Exception
     */
    @RequestMapping(value = "/volumes/files/content", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult downloadFile(@RequestBody Map<String, String> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件id）"});
        }
        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());
        String volumes;
        try {
            volumes = (appFilesService.download(head, pathParameter)).replace("yundoc-dev.spdb.com", "xxx:8080");
        } catch (Exception e) {
            logger.error("method: uploadFile, message:{}, stack", e.getMessage(), e);
            throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
        }
        JSONObject result = MyUtil.dataProcessing(volumes);
        logger.info("method: volumes, data: {}", volumes);
        return JsonResultUtil.buildSuccess(result);

    }

    /**
     * 文件预览
     *
     * @throws Exception
     */
    @RequestMapping(value = "/volumes/files/preview", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult preview(@RequestBody Map<String, String> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件id）"});
        }
        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());

        PreviewParameter previewParameter = new PreviewParameter();
        previewParameter.setCopyable(true);
        previewParameter.setPrintable(true);
        previewParameter.setExpiration("2116-10-15T17:00:00+08:00");
        previewParameter.setExtuserid(requestParam.get("ext_userid"));
        String preview;
        try {
            preview = (appFilesService.getPreview(head, pathParameter, previewParameter)).replace("weboffice.spdb.com", "xxx:8207");
        } catch (Exception e) {
            logger.error("method: preview, message: {}, stack:{}", e.getMessage(), e);
            throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
        }
        JSONObject result = MyUtil.dataProcessing(preview);
        return JsonResultUtil.buildSuccess(result);

    }

    /**
     * 在线编辑
     *
     * @throws Exception
     */
    @RequestMapping(value = "/volumes/files/editor", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult editor(@RequestBody Map<String, String> requestParam) throws Exception {
        if (!userVerifyUtil.userGroupVerify("互联网应用")) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户不是互联网应用组的用户，身份验证失败"});
        }
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件id）"});
        }
        String userObj = requestParam.get("user");
        User user = JSONObject.parseObject(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) || CommonUtils.isNullOrEmpty(user.getUser_name_cn()) &&
                CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }
        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());

        PreviewParameter previewParameter = new PreviewParameter();
        previewParameter.setExtuserid(requestParam.get("ext_userid"));
        previewParameter.setAccount_sync("1");
        previewParameter.setWrite("1");
        String editor;
        try {
            editor = appFilesService.getEditor(head, pathParameter, previewParameter);
        } catch (Exception e) {
            logger.error("method: preview, message: {}, stack:{}", e.getMessage(), e);
            throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
        }
        JSONObject result = MyUtil.dataProcessing(editor);
        JSONObject jsonObject = JSON.parseObject(editor);

        //参数
        String url = jsonObject.getString("url").replace("weboffice.spdb.com", "xxx:8207");
        String wps_sid = jsonObject.getString("wps_sid");
        String apptoken = jsonObject.getString("apptoken");

        String editorUrl = url + "&wps_sid=" + wps_sid + "&Apptoken=" + apptoken;
        Map<String, String> map = new HashMap<String, String>();
        map.put("url", editorUrl);

        JSONObject fileInfo = queryFileByFileId(requestParam);
        String fileName = fileInfo.getString("name");
        String parentId = fileInfo.getString("parent_id");


        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "文件编辑");
        userMap.put("fileName", fileName);
        userMap.put("fileId", requestParam.get("file"));
        userMap.put("parentId", parentId);
        userMap.put("type", "wps");
        userInfoService.addUserInfo(userMap);

        logger.info("method: preview, data: {}", editor);
        return JsonResultUtil.buildSuccess(map);

    }


    /**
     * 创建文件夹
     *
     * @throws Exception
     */
    @RequestMapping(value = "/volumes/files/children", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createDir(@RequestBody Map<String, String> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件夹路径）"});
        }
        if (CommonUtils.isNullOrEmpty(requestParam.get("file_name"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file_name（文件夹名）"});
        }
        User user = getUser();
        //查询文件列表
        Map<String, String> requestParam1 = new HashMap<>();
        requestParam1.put("file", "root");
        requestParam1.put("volume", constants.getVolume());
        JsonResult retrieveDir1 = retrieveDir(requestParam);
        JSONObject jsonFile = JSON.parseObject(retrieveDir1.getData().toString());
        String valueFile = jsonFile.getString("value");
        List<String> fileList = JSON.parseArray(valueFile, String.class);
        Map<String, String> fileMap = new HashMap<>();
        for (int i = 0; i < fileList.size(); i++) {
            String fileTemp = fileList.get(i);
            if ("folder".equals(JSON.parseObject(fileTemp).getString("type"))) {
                fileMap.put(JSON.parseObject(fileTemp).getString("name"), JSON.parseObject(fileTemp).getString("id"));
            }
        }
        //查询文件夹是否存在, 如果存在，则返回文件夹id
        String fileName = requestParam.get("file_name");
        if (fileMap.containsKey(fileName)) {
            String fileId = fileMap.get(fileName);
            String createExit = "{\"id\":\"" + fileId + "\"}";
            JSONObject result = MyUtil.dataProcessing(createExit);
            return JsonResultUtil.buildSuccess(result);
        }

        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());

        RequestParameterBody requestBody = new RequestParameterBody();
        requestBody.setFileName(requestParam.get("file_name"));
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String createFolder = appFilesService.createDir(head, pathParameter, requestBody);
        JSONObject result = MyUtil.dataProcessing(createFolder);
        logger.info("method: createDir, data: {}", createFolder);


        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "创建文件夹");
        userMap.put("type", "wps");
        userMap.put("fileName", requestParam.get("file_name"));
        userMap.put("fileId", result.getString("id"));
        userMap.put("parentId", requestParam.get("file"));

        userInfoService.addUserInfo(userMap);

        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 获取卷列表
     *
     * @throws Exception
     */
    @RequestMapping("/volumes")
    @ResponseBody
    public JsonResult volumes() throws Exception {
        //判断token值
        if (token == null) {
            getToken();
        }

        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        String volumes;
        try {
            volumes = appFilesService.appVolumes(head);
        } catch (Exception e) {
            logger.info("method: volumes, message: {}, stack:{}", e.getMessage(), e);
            throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
        }
        logger.info("method: volumes, data: {}", volumes);
        JSONObject result = MyUtil.dataProcessing(volumes);
        return JsonResultUtil.buildSuccess(result);

    }

    /**
     * 删除文件
     */
    @RequestMapping("/deleteFile")
    @ResponseBody
    public JsonResult deleteFile(@RequestBody Map<String, String> requestParam) throws Exception {
        if (!userVerifyUtil.userGroupVerify("互联网应用")) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户不是互联网应用组的用户，身份验证失败"});
        }
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件id）"});
        }
        String userObj = requestParam.get("user");
        User user = JSONObject.parseObject(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) || CommonUtils.isNullOrEmpty(user.getUser_name_cn()) &&
                CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }
        //判断token值
        if (token == null) {
            getToken();
        }
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());

        JSONObject fileInfo = queryFileByFileId(requestParam);
        String fileName = fileInfo.getString("name");
        String parentId = fileInfo.getString("parent_id");

        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "文件删除");
        userMap.put("type", "wps");
        userMap.put("fileName", fileName);
        userMap.put("fileId", requestParam.get("file"));
        userMap.put("parentId", parentId);

        String value = appFilesService.deleteFile(head, pathParameter);
        userInfoService.addUserInfo(userMap);
        if (CommonUtils.isNullOrEmpty(value)) {
            return JsonResultUtil.buildSuccess("true");
        } else {
            JSONObject result = MyUtil.dataProcessing(value);
            return JsonResultUtil.buildSuccess(result);
        }

    }


    /**
     * 获取文件版本列表
     */
    public String queryVersions(Token token, String fileId, String volumeId) throws Exception {
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(fileId);
        pathParameter.setVolume(volumeId);

        String getVersions = appFilesService.queryVersions(head, pathParameter);
        logger.info(getVersions);
        return getVersions;
    }

    /**
     * 获取某个文件(根据文件id)
     */
    public JSONObject queryFileByFileId(@RequestBody Map<String, String> requestParam) throws Exception {
        //判断token值
        if (token == null) {
            getToken();
        }
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setFile(requestParam.get("file"));
        pathParameter.setVolume(constants.getVolume());
        String getFile = appFilesService.queryFileByFileId(head, pathParameter);
        JSONObject result = MyUtil.dataProcessing(getFile);
        return result;
    }

    /**
     * 创建文件夹
     */
    @RequestMapping(value = "/volumes/files/createFolder", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createFolder(@RequestBody Map<String, String> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file（文件夹路径）"});
        }
        if (CommonUtils.isNullOrEmpty(requestParam.get("file_name"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"file_name（文件夹名）"});
        }
        String userObj = requestParam.get("user");
        User user = JSONObject.parseObject(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) || CommonUtils.isNullOrEmpty(user.getUser_name_cn()) &&
                CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }
        HeaderParameter head = new HeaderParameter();
        head.setAuthorization(token.getTokenType() + " " + token.getAccessToken());
        head.setContentType(Constants.CONTENT_TYPE_JSON);

        PathParameter pathParameter = new PathParameter();
        pathParameter.setVolume(constants.getVolume());

        RequestParameterBody requestBody;
        //查询文件列表
        Map<String, String> requestParam1 = new HashMap<>();
        requestParam1.put("file", "root");
        requestParam1.put("volume", constants.getVolume());
        JsonResult retrieveDir1 = retrieveDir(requestParam);
        JSONObject jsonFile = JSON.parseObject(retrieveDir1.getData().toString());
        String valueFile = jsonFile.getString("value");
        List<String> fileList = JSON.parseArray(valueFile, String.class);
        Map<String, String> fileMap = new HashMap<>();
        for (int i = 0; i < fileList.size(); i++) {
            String fileTemp = fileList.get(i);
            if ("folder".equals(JSON.parseObject(fileTemp).getString("type"))) {
                fileMap.put(JSON.parseObject(fileTemp).getString("name"), JSON.parseObject(fileTemp).getString("id"));
            }
        }
        //查询文件夹是否存在, 如果存在，则返回文件夹id
        String fileName = requestParam.get("file_name");
        if (fileMap.containsKey(fileName)) {
            String fileId = fileMap.get(fileName);
            String createExit = "{\"folderId\":\"" + fileId + "\"}";
            JSONObject result = MyUtil.dataProcessing(createExit);
            Map<String, String> map = new HashMap<String, String>();
            map.put("file", fileId);
            map.put("volume", constants.getVolume());
            JsonResult files = retrieveDir(map);
            String data = files.getData().toString();
            JSONObject jsonObject1 = JSON.parseObject(data);
            String value = jsonObject1.getString("value");
            List<String> fileValue = JSON.parseArray(value, String.class);
            String designsId = null;
            String auditDraftId = null;
            String iosUploadId = null;
            String androidUploadId = null;
            if (!CommonUtils.isNullOrEmpty(fileValue)) {
                for (String item : fileValue) {
                    JSONObject jsonObject = JSON.parseObject(item);
                    String name = jsonObject.getString("name");
                    String folderId = jsonObject.getString("id");
                    if (("设计稿").equals(name)) {
                        designsId = folderId;
                        result.put("designsId", folderId);
                    }
                    if (("审核稿").equals(name)) {
                        auditDraftId = folderId;
                        result.put("auditDraftId", folderId);
                    }
                    if (("IOS截图").equals(name)) {
                        iosUploadId = folderId;
                        result.put("iosUploadId", folderId);
                    }
                    if (("安卓截图").equals(name)) {
                        androidUploadId = folderId;
                        result.put("androidUploadId", folderId);
                    }
                }
            }
            if (CommonUtils.isNullOrEmpty(designsId)) {
                pathParameter.setFile(fileId);
                requestBody = new RequestParameterBody();
                requestBody.setFileName("设计稿");
                requestBody.setFileType("folder");
                requestBody.setFileNameConflictBehavior("fail");
                requestBody.setParents(true);
                String designs = appFilesService.createDir(head, pathParameter, requestBody);
                MyUtil.dataProcessing(designs);
                JSONObject JSONObjectDesigns = JSON.parseObject(designs);
                designsId = JSONObjectDesigns.getString("id");
                result.put("designsId", designsId);
            }
            if (CommonUtils.isNullOrEmpty(auditDraftId)) {
                pathParameter.setFile(fileId);
                requestBody = new RequestParameterBody();
                requestBody.setFileName("审核稿");
                requestBody.setFileType("folder");
                requestBody.setFileNameConflictBehavior("fail");
                requestBody.setParents(true);
                String auditDraft = appFilesService.createDir(head, pathParameter, requestBody);
                MyUtil.dataProcessing(auditDraft);
                JSONObject JSONObjectAuditDraft = JSON.parseObject(auditDraft);
                auditDraftId = JSONObjectAuditDraft.getString("id");
                result.put("auditDraftId", auditDraftId);
            }
            if (CommonUtils.isNullOrEmpty(iosUploadId)) {
                pathParameter.setFile(fileId);
                requestBody = new RequestParameterBody();
                requestBody.setFileName("IOS截图");
                requestBody.setFileType("folder");
                requestBody.setFileNameConflictBehavior("fail");
                requestBody.setParents(true);
                String iosUpload = appFilesService.createDir(head, pathParameter, requestBody);
                MyUtil.dataProcessing(iosUpload);
                JSONObject JSONObjectIosUpload = JSON.parseObject(iosUpload);
                iosUploadId = JSONObjectIosUpload.getString("id");
                result.put("iosUploadId", iosUploadId);
            }
            if (CommonUtils.isNullOrEmpty(androidUploadId)) {
                pathParameter.setFile(fileId);
                requestBody = new RequestParameterBody();
                requestBody.setFileName("安卓截图");
                requestBody.setFileType("folder");
                requestBody.setFileNameConflictBehavior("fail");
                requestBody.setParents(true);
                String androidUpload = appFilesService.createDir(head, pathParameter, requestBody);
                MyUtil.dataProcessing(androidUpload);
                JSONObject JSONObjectAndroidUpload = JSON.parseObject(androidUpload);
                androidUpload = JSONObjectAndroidUpload.getString("id");
                result.put("androidUpload", androidUpload);
            }

            return JsonResultUtil.buildSuccess(result);
        }

        //若文件夹不存在，则新增
        //判断token值
        if (token == null) {
            getToken();
        }
        pathParameter.setFile(requestParam.get("file"));
        requestBody = new RequestParameterBody();
        requestBody.setFileName(requestParam.get("file_name"));
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String createFolder = appFilesService.createDir(head, pathParameter, requestBody);
        JSONObject result = MyUtil.dataProcessing(createFolder);
        String folderId = result.getString("id");

        pathParameter.setFile(folderId);
        //新建设计稿目录
        requestBody.setFileName("设计稿");
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String designs = appFilesService.createDir(head, pathParameter, requestBody);
        MyUtil.dataProcessing(designs);
        JSONObject JSONObjectDesigns = JSON.parseObject(designs);
        String designsId = JSONObjectDesigns.getString("id");
        //新建审核稿目录
        requestBody.setFileName("审核稿");
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String auditDraft = appFilesService.createDir(head, pathParameter, requestBody);
        MyUtil.dataProcessing(auditDraft);
        JSONObject JSONObjectAuditDraft = JSON.parseObject(auditDraft);
        String auditDraftId = JSONObjectAuditDraft.getString("id");
        //新建IOS截图目录
        requestBody.setFileName("IOS截图");
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String iosUpload = appFilesService.createDir(head, pathParameter, requestBody);
        MyUtil.dataProcessing(iosUpload);
        JSONObject JSONObjectIosUpload = JSON.parseObject(iosUpload);
        String iosUploadId = JSONObjectIosUpload.getString("id");
        //新建安卓截图目录
        requestBody.setFileName("安卓截图");
        requestBody.setFileType("folder");
        requestBody.setFileNameConflictBehavior("fail");
        requestBody.setParents(true);
        String androidUpload = appFilesService.createDir(head, pathParameter, requestBody);
        MyUtil.dataProcessing(androidUpload);
        JSONObject JSONObjectAndroidUpload = JSON.parseObject(androidUpload);
        String androidUploadId = JSONObjectAndroidUpload.getString("id");

        result.clear();
        result.put("folderId", folderId);
        result.put("designsId", designsId);
        result.put("auditDraftId", auditDraftId);
        result.put("iosUploadId", iosUploadId);
        result.put("androidUploadId", androidUploadId);


        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "创建文件夹");
        userMap.put("type", "wps");
        userMap.put("fileName", requestParam.get("file_name"));
        userMap.put("fileId", result.getString("folderId"));
        userMap.put("parentId", requestParam.get("file"));
        userInfoService.addUserInfo(userMap);

        return JsonResultUtil.buildSuccess(result);
    }

    public User getUser() throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if (CommonUtils.isNullOrEmpty(user)) {
            logger.error("----->" + ErrorConstants.USR_AUTH_FAIL);
            user = new User();
            user.setUser_name_cn("跨模块调用");
            user.setUser_name_en("cuocuo");
        }
        return user;
    }

}

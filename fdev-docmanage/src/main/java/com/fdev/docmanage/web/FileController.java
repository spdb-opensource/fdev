package com.fdev.docmanage.web;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.service.IFileService;
import com.fdev.docmanage.service.UserInfoService;
import com.fdev.docmanage.util.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @RequestMapping(value = "/rn_release",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult test(HttpServletRequest request){
        Map map = new HashMap();
        map.put("iosPath",request.getParameter("iosPath"));
        map.put("iosJson",request.getParameter("iosJson"));
        map.put("androidPath",request.getParameter("androidPath"));
        map.put("androidJson",request.getParameter("androidJson"));

        return JsonResultUtil.buildSuccess("数据："+map);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @RequestValidate(NotEmptyFields = {Dict.MODULENAME, Dict.PATH, Dict.FILES})
    @RequestMapping(value = "/filesUpload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult filesUpload(HttpServletRequest request, MultipartFile[] files) throws Exception {

        String moduleName = request.getParameter(Dict.MODULENAME);
        String userObj = request.getParameter("user");
        User user = JSONObject.parseObject(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) || CommonUtils.isNullOrEmpty(user.getUser_name_cn()) &&
                CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }

        String[] path = request.getParameterValues(Dict.PATH);

        for (int i = 0; i < path.length; i++) {
            path[i] = moduleName + "/" + path[i];
        }
        fileService.fileUpload(path, files);

        StringBuilder fileName = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            fileName.append(path[i]).append(";");
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "文件上传");
        userMap.put("type", "minio");
        userMap.put("fileName", fileName.toString());

        userInfoService.addUserInfo(userMap);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.MODULENAME, Dict.PATH})
    @RequestMapping(value = "/getFilesList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult getFilesList(@RequestBody Map<String, String> requestParam) throws Exception {
        String path = requestParam.get(Dict.MODULENAME) + "/" + requestParam.get(Dict.PATH);
        List<String> tree = fileService.getFiles(path);
        return JsonResultUtil.buildSuccess(tree);
    }

    @RequestMapping(value = "/filesDownload", method = RequestMethod.GET)
    @ResponseBody
    public void filesDownload(HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        String moduleName = request.getParameter(Dict.MODULENAME);
        String path = request.getParameter(Dict.PATH);
        if (CommonUtils.isNullOrEmpty(moduleName) && CommonUtils.isNullOrEmpty(path)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"moduleName（模块名）、path（路径"});
        }
        path = moduleName + "/" + path;
        fileService.fileDownload(response, path);
    }

    @RequestValidate(NotEmptyFields = {Dict.MODULENAME, Dict.PATH})
    @RequestMapping(value = "/filesDelete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult filesDelete(@RequestBody Map<String, Object> requestParam) throws Exception {

        //判断user信息是否有其它微服务模块上传，若无，取用户token信息
        Object userObj = requestParam.get("user");
        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.convertValue(userObj, User.class);
        if (CommonUtils.isNullOrEmpty(user) ||
                CommonUtils.isNullOrEmpty(user.getUser_name_cn()) && CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            user = getUser();
        }
        //删除文件操作
        List<String> paths = (List<String>) requestParam.get(Dict.PATH);
        String[] path = paths.toArray(new String[0]);
        for (int i = 0; i < path.length; i++) {
            path[i] = requestParam.get(Dict.MODULENAME) + "/" + path[i];
        }
        fileService.fileDelete(path);
        //记录用户操作
        StringBuffer fileName = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            fileName.append(path[i]).append(";");
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("user_name_cn", user.getUser_name_cn());
        userMap.put("user_name_en", user.getUser_name_en());
        userMap.put("operation", "文件删除");
        userMap.put("type", "minio");
        userMap.put("fileName", fileName.toString());

        userInfoService.addUserInfo(userMap);
        return JsonResultUtil.buildSuccess();
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

    /**
     * 文件夹下载
     *pathName:string,folder:[path:String,fileName:String,fileType:file/folder],
     *      * zipName:压缩包名,folderName:文件夹名称,fileType:folder
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/downloadFolder", method = RequestMethod.POST)
    public void downloadFolder(@RequestBody Map<String, Object> param,HttpServletResponse response) throws Exception {
        fileService.minioDowloadFolder(response, param);
    }
}

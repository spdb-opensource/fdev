package com.spdb.fdev.pipeline.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IPipelineService;
import com.spdb.fdev.pipeline.service.IPluginService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/api/plugin")
@RestController
public class PluginController {
    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPipelineService pipelineService;

    @Autowired
    private IUserService userService;



    @RequestMapping(value = "/queryPluginList", method = RequestMethod.POST)
    public JsonResult queryPlugin(@RequestBody Map request) throws Exception {
        //查询插件列表
        List<String> key = (List<String>) request.get(Dict.KEY);//模糊查询数据
        return JsonResultUtil.buildSuccess(pluginService.queryPlugin(key, request));
    }

    @PostMapping(value = "/demoTest")
    public String DemoTest(@RequestBody Map request){

        return "接口已发通！";
    }

    @RequestMapping(value = "/addPlugin", consumes = {"multipart/form-data"})
    public JsonResult addPlugin(@RequestParam(value = "script") MultipartFile script,
                                @RequestParam(value = "pluginName", required = false) String pluginName,
                                @RequestParam(value = "pluginDesc", required = false) String pluginDesc,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "open") Boolean open,
                                @RequestParam(value = "entityTemplateList",  required = false) String entityTemplateList,
                                @RequestParam(value = "params", required = false) String params,
                                @RequestParam(value = "artifacts", required = false) String artifacts) throws Exception {
        List<Map> paramsMap = JSONArray.parseArray(params, Map.class);
        Map categoryMap = null;
        if (!CommonUtils.isNullOrEmpty(category))
            categoryMap = JSONObject.parseObject(category, Map.class);
        Map artifactsMap = JSONObject.parseObject(artifacts, Map.class);
        List<Map> entityTemplateListMap = JSONArray.parseArray(entityTemplateList, Map.class);
        pluginService.savePlugin(script,pluginName,pluginDesc,categoryMap,open,entityTemplateListMap,paramsMap, artifactsMap, categoryMap);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/editPlugin", consumes = {"multipart/form-data"})
    public JsonResult editPlugin(@RequestParam(value = "script") MultipartFile script,
                                 @RequestParam(value = "nameId") String nameId,
                                 @RequestParam(value = "pluginName", required = false) String pluginName,
                                 @RequestParam(value = "pluginDesc", required = false) String pluginDesc,
                                 @RequestParam(value = "releaseLog") String releaseLog,
                                 @RequestParam(value = "open") Boolean open,
                                 @RequestParam(value = "version") String version,
                                 @RequestParam(value = "entityTemplateList", required = false) String entityTemplateList,
                                 @RequestParam(value = "params", required = false) String params,
                                 @RequestParam(value = "scriptUpdateFlag") Boolean scriptUpdateFlag,
                                 @RequestParam(value = "category") String category,
                                 @RequestParam(value = "artifacts", required = false) String artifacts) throws Exception {

        pluginService.editPlugin(nameId, pluginName, pluginDesc, script, releaseLog, entityTemplateList, params, open,version, artifacts, category, scriptUpdateFlag);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/delPlugin", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PLUGINCODE})
    public JsonResult delPlugin(@RequestBody Map requestParam) throws Exception {
        String pluginCode = (String) requestParam.get(Dict.PLUGINCODE);
        //根据插件id查询该插件是否正在被流水线引用,若有则返回流水线list
        Map<String, Object> pipelineMap =  pipelineService.queryPipelineByPluginCode(pluginCode);

        return JsonResultUtil.buildSuccess(pipelineMap);
    }

    @RequestMapping(value = "/queryPluginHistory", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.NAMEID})
    public JsonResult queryPluginHistory(@RequestBody Map requestParam) throws Exception {
        String nameId = (String) requestParam.get(Dict.NAMEID);
        List<Map<String,String>> pluginHistoryList = pluginService.queryPluginHistory(nameId);
        return JsonResultUtil.buildSuccess(pluginHistoryList);
    }


    /**
     * 查询单个插件详情，该接口需要返回script的内容
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryPluginDetail", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PLUGINCODE})
    public JsonResult queryPluginDetail(@RequestBody Map requestParam) throws Exception {
        String pluginCode = (String) requestParam.get(Dict.PLUGINCODE);
        return JsonResultUtil.buildSuccess(pluginService.getPluginFullInfo(pluginCode));
    }

    @RequestMapping(value = "/queryMarkDown", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.PLUGINCODE})
    public JsonResult queryMarkDowm(@RequestBody Map requestParam) throws Exception{
        String pluginCode = (String) requestParam.get(Dict.PLUGINCODE);
        return JsonResultUtil.buildSuccess(pluginService.queryMarkDowm(pluginCode));
    }
    @RequestMapping(value = "/queryEntityTemplateByContent", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ENTITYTEMPLATECONTENT})
    public JsonResult queryEntityTemplateByContent(@RequestBody Map  requestParam) throws Exception {
        String str= (String) requestParam.get(Dict.ENTITYTEMPLATECONTENT);
        return JsonResultUtil.buildSuccess(pluginService.queryEntityTemplateByContent(str));
    }

    @RequestMapping(value = "/statisticsPluginUseCount",method = RequestMethod.POST)
    public JsonResult statisticsPluginUseCount() throws Exception {
        pluginService.statisticsPluginUseCount();
        return JsonResultUtil.buildSuccess();
    }
    //用户给插件评价，如果用户已经对插件评价则更新评价的信息否则添加评价信息
    @RequestMapping(value = "/upsertPluginEvaluate",method = RequestMethod.POST)
    @NoNull(require = {Dict.NAMEID,Dict.OPERATIONDIFFICULTY,Dict.ROBUSTNESS,Dict.LOGCLARITY}, parameter = PluginEvaluate.class)
    public JsonResult upsertPluginEvaluate(@RequestBody PluginEvaluate pluginEvaluate) throws Exception {
        User user = userService.getUserFromRedis();
        pluginService.upsertPluginEvaluate(user,pluginEvaluate);
        return JsonResultUtil.buildSuccess();
    }
    //查询这个用户是否给这个插件评价的信息
    @RequestMapping(value = "/queryPluginEvaluate",method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.NAMEID})
    public JsonResult queryPluginEvaluate(@RequestBody Map  requestParam) throws Exception {
        User user = userService.getUserFromRedis();
        String nameId = (String) requestParam.get(Dict.NAMEID);
        PluginEvaluate pluginEvaluate = pluginService.queryPluginEvaluate(user,nameId);
        return JsonResultUtil.buildSuccess(pluginEvaluate);
    }

    /**
     * 获取插件的yaml参数信息
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getYamlConfigById",method = RequestMethod.POST)
    public JsonResult getYamlConfigById(@RequestBody Map  requestParam) {
        String configId = (String) requestParam.get(Dict.CONFIGID);
        YamlConfig result = this.pluginService.getYamlConfigById(configId);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 如果过来configId为空的时候，即为新增，若不为空的时候即为编辑(状态为失效)
     *
     * @param yamlConfig
     * @return
     */
    @RequestMapping(value = "/addYamlConfig",method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.CONFIGTEMPLATEID})
    public JsonResult addYamlConfig(@RequestBody YamlConfig yamlConfig){
       // YamlConfig addEntity = CommonUtils.map2Object(requestParam, YamlConfig.class);
        yamlConfig.setType(Dict.PRIVATE);
        return JsonResultUtil.buildSuccess(this.pluginService.addYamlConfig(yamlConfig));
    }


    @GetMapping(value = "/downloadMinio")
    public void downloadMinio(@RequestParam("bucket") String bucket, @RequestParam("object") String object, HttpServletResponse response) throws Exception {
        this.pluginService.downloadMinio(bucket, object, response);
    }

    @RequestMapping(value = "/uploadArt", consumes = {"multipart/form-data"})
    public JsonResult uploadArt(@RequestParam(value = "file") MultipartFile file,
                                 @RequestParam(value = "path") String path) throws Exception {

        pluginService.uploadArt(file, path);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/uploadMinio", consumes = {"multipart/form-data"})
    public JsonResult uploadMinio(@RequestParam(value = "file") MultipartFile file,
                                @RequestParam(value = "bucket") String bucket,
                                @RequestParam(value = "object") String object) throws Exception {

        pluginService.uploadMinio(file, bucket, object);
        return JsonResultUtil.buildSuccess();
    }
}

package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.cache.IConfigFileCache;
import com.spdb.fdev.fdevenvconfig.spdb.service.IConfigFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置文件 Controller层
 *
 * @author xxx
 * @date 2019/7/29 11:54
 */
@RestController
@RequestMapping("/api/v2/configfile")
public class ConfigFileController {

    @Autowired
    private IConfigFileService configFileService;
    @Autowired
    private IConfigFileCache configFileCache;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 用于保存配置模板
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/saveConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.CONTENT, Dict.FEATURE_BRANCH})
    public JsonResult saveConfigTemplate(@RequestBody Map<String, Object> requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get(Dict.PROJECT_ID)) && CommonUtils.isNullOrEmpty(requestParam.get(Dict.GITLABID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数id上送异常"});
        }
        return JsonResultUtil.buildSuccess(this.configFileService.saveConfigTemplate(requestParam));
    }

    /**
     * 配置模板回显
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/queryConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.FEATURE_BRANCH, Dict.PROJECT_ID})
    public JsonResult queryConfigTemplate(@RequestBody Map<String, String> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.queryConfigTemplate(requestParam));
    }

    /**
     * 配置模板预览
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/previewConfigFile")
    @RequestValidate(NotEmptyFields = {Constants.ENV_NAME, Dict.CONTENT})
    public JsonResult previewConfigFile(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.previewConfigFile(requestParam));
    }

    /**
     * 配置文件依赖搜索
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/queryConfigDependency")
    @RequestValidate(NotEmptyFields = {Constants.MODEL_NAME_EN})
    public JsonResult queryConfigDependency(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileCache.preQueryConfigDependency(requestParam));
    }

    /**
     * 依赖搜索结果导出
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @GetMapping("/exportDependencySearchResult")
    @RequestValidate(NotEmptyFields = {Constants.MODEL_NAME_EN})
    public void exportDependencySearchResult(@RequestParam Map<String, Object> requestParam, HttpServletResponse response) throws Exception {
        String rangeString = (String) requestParam.get(Constants.RANGE);
        List<String> range = new ArrayList<>();
        for (String branch : rangeString.split(","))
            range.add(branch);
        requestParam.put(Constants.RANGE, range);
        List<Map> maps = this.configFileCache.preQueryConfigDependency(requestParam);
        this.configFileService.exportDependencySearchResult(maps, response);
    }

    /**
     * 以pro开头的tag触发的持续集成调用此接口
     * 1.接收参数写入properties文件并上传到项目master分支指定位置下
     * 2.生成应用生产环境的yaml文件
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/saveConfigProperties")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_PROJECT_ID, Constants.BRANCH})
    public JsonResult saveConfigProperties(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.saveConfigProperties(map));
    }

    /**
     * 上传配置文件到开发配置中心
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/saveDevConfigProperties")
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID, Dict.CONTENT, Constants.LABELS, Dict.FEATURE_BRANCH})
    public JsonResult saveDevConfigProperties(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.saveDevConfigProperties(requestParam));
    }

    /**
     * 持续集成写入 auto-config 组下 文件并上传到项目master分支制定位置下
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/saveWarMd5ToGitlab")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID, Dict.TAG_NAME, Dict.CONTENT, Dict.PIPELINE_ID})
    public JsonResult saveWarMd5Properties(@RequestBody Map map) throws Exception {
        this.configFileService.saveWarMd5ToGitlab(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 绑定部署信息上传对应测试环境配置文件到配置中心
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/saveSitConfigProperties")
    @RequestValidate(NotEmptyFields = {Dict.APPID, Dict.ENVTESTLIST, Dict.ENV_TEST_CONFIG})
    public JsonResult saveSitConfigProperties(@RequestBody Map<String, Object> requestParam) throws Exception {
        this.configFileService.saveSitConfigProperties(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 校验配置文件中必填实体属性是否有值
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/checkConfigFile")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.ENV_NAME, Dict.CONTENT, Dict.TAG})
    public JsonResult checkConfigFile(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.checkConfigFile(requestParam));
    }

    @PostMapping("/batchSaveConfigFile")
    public JsonResult batchSaveConfigFile(@RequestBody Map request) throws Exception {
        List<Map> requestList = (List<Map>) request.get(Dict.LIST);
        for (Map map : requestList) {
            if (CommonUtils.isNullOrEmpty(map.get(Dict.APP_ID))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.APP_ID});
            }
            if (CommonUtils.isNullOrEmpty(map.get(Dict.GITLABID))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.GITLABID});
            }
            if (CommonUtils.isNullOrEmpty(map.get(Dict.APPNAME))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.APPNAME});
            }
            if (CommonUtils.isNullOrEmpty(map.get(Dict.TAGNAME))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TAGNAME});
            }
        }
        return JsonResultUtil.buildSuccess(this.configFileService.batchSaveConfigFile(requestList));
    }

    @PostMapping("/batchPreviewConfigFile")
    public JsonResult batchPreviewConfigFile(@RequestBody Map request) throws Exception {
        List<Map> requestList = (List<Map>) request.get(Dict.LIST);
        return JsonResultUtil.buildSuccess(this.configFileService.batchPreviewConfigFile(requestList));
    }
}

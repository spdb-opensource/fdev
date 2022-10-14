package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppDeployMappingService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RequestMapping("/api/v2/appDeploy")
@RestController
public class AppDeployMappingController {

    @Autowired
    private IAppDeployMappingService appDeployMappingService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 查询应用deploy时需要的变量映射关系
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryVariablesMapping")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID, Constants.ENV})
    public JsonResult queryVariablesMapping(@RequestBody @ApiParam Map requestMap) throws Exception {
        requestMap.put(Constants.TYPE, "deploy");
        return JsonResultUtil.buildSuccess(appDeployMappingService.queryVariablesMapping(requestMap));
    }

    /**
     * 查询应用deploy时需要的所有变量映射关系
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryAllVariablesMapping")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID, Constants.ENV})
    public JsonResult queryAllVariablesMapping(@RequestBody @ApiParam Map requestMap) throws Exception {
        requestMap.put(Constants.TYPE, "deploy");
        return JsonResultUtil.buildSuccess(appDeployMappingService.queryAllVariablesMapping(requestMap));
    }

    @PostMapping("/querySccVariablesMapping")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID, Constants.ENV})
    public JsonResult querySccVariablesMapping(@RequestBody @ApiParam Map requestMap) throws Exception {
        requestMap.put(Constants.TYPE, "deploy");
        return JsonResultUtil.buildSuccess(appDeployMappingService.querySccVariablesMapping(requestMap));
    }

    /**
     * 查询应用deploy时需要的变量映射关系
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryVariablesMappingCn")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID, Constants.ENV})
    public JsonResult queryVariablesMappingCn(@RequestBody @ApiParam Map requestMap) throws Exception {
        requestMap.put(Constants.TYPE, "deploy");
        return JsonResultUtil.buildSuccess(appDeployMappingService.queryVariablesMappingwithCn(requestMap));
    }

    @PostMapping("/queryByGitlabId")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID})
    public JsonResult queryConfigRepi(@RequestBody @ApiParam Map requestMap) {
        Integer gitlabId = Integer.valueOf(requestMap.get(Constants.GITLAB_ID).toString());
        return JsonResultUtil.buildSuccess(appDeployMappingService.queryTagsByGitlabId(gitlabId));
    }

    /**
     * 查询应用在某套环境下的镜像仓库用户名和密码
     *
     * @param requestMap
     * @return
     */
    @PostMapping("queryImageUserAndPwd")
    @NoNull(require = {Constants.GITLAB_ID, Constants.ENV}, parameter = LinkedHashMap.class)
    public JsonResult queryImageUserAndPwd(@RequestBody Map<String, String> requestMap) throws Exception {
        requestMap.put(Constants.TYPE, Constants.DEPLOY);
        return JsonResultUtil.buildSuccess(appDeployMappingService.queryImagepwd(requestMap));
    }
    
    @PostMapping("/querySpecifiedVariablesMapping")
    public JsonResult querySpecifiedVariablesMapping(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(appDeployMappingService.querySpecifiedVariablesMapping(requestParam));
    }
    

}

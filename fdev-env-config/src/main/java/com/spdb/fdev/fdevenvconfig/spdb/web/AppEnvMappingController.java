package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAPPEnvMapingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.QueryDeployInfoService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/v2/appEnv")
@RestController
public class AppEnvMappingController {

    @Autowired
    private IAPPEnvMapingService iappEnvMapingService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Autowired
    private QueryDeployInfoService QueryDeployInfoService;

    @PostMapping("/addAppEnvMapping")
    @RequestValidate(NotEmptyFields = {Dict.APPID, Dict.NETWORK})
    public JsonResult addAppEnvMapping(@RequestBody Map requestParam) throws Exception {

        this.iappEnvMapingService.addAppEnvMapping(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryProEnvByAppId")
    @RequestValidate(NotEmptyFields = {Constants.APP_ID})
    public JsonResult queryProEnvByAppId(@RequestBody @ApiParam Map requestMap) {
        return JsonResultUtil.buildSuccess(iappEnvMapingService.queryProEnvByAppId(requestMap));
    }

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryProEnvByGitLabId")
    @RequestValidate(NotEmptyFields = {Constants.GITLAB_ID})
    public JsonResult queryProEnvByGitLabId(@RequestBody @ApiParam Map requestMap) {
        return JsonResultUtil.buildSuccess(iappEnvMapingService.queryProEnvByGitLabId(requestMap));
    }


    @PostMapping("/queryDefinedInfo")
    @RequestValidate(NotEmptyFields = {Dict.DEFINED_DEPLOY_ID})
    public JsonResult queryDefinedInfo(@RequestBody Map requestMap) {
        return JsonResultUtil.buildSuccess(QueryDeployInfoService.queryVariablesById(requestMap));
    }


}

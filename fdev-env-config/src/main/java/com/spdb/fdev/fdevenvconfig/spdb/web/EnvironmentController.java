package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.service.IEnvironmentService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IlabelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @author xxx
 * @date 2019/7/5 13:10
 */
@Api(tags = "环境接口")
@RequestMapping("/api/v2/env")
@RestController
public class EnvironmentController {

    @Autowired
    private IEnvironmentService environmentService;

    @Autowired
    private IlabelService ilabelService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 查询全量的环境信息
     *
     * @param environment
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    public JsonResult query(@RequestBody @ApiParam Environment environment) {
        return JsonResultUtil.buildSuccess(this.environmentService.query(environment));
    }

    @PostMapping("/queryAutoEnv")
    public JsonResult queryAutoEnv(@RequestBody @ApiParam Environment environment) throws Exception {
        return JsonResultUtil.buildSuccess(this.environmentService.queryAutoEnv(environment));
    }


    /**
     * 新增环境
     *
     * @param environment
     * @return
     * @throws Exception
     */
    @PostMapping("/add")
    @NoNull(require = {Constants.NAME_EN, Constants.NAME_CN}, parameter = Environment.class)
    public JsonResult save(@RequestBody @ApiParam Environment environment) throws Exception {
        return JsonResultUtil.buildSuccess(this.environmentService.save(environment));
    }

    /**
     * 删除环境
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/delete")
    @RequestValidate(NotEmptyFields = {Constants.ID, Dict.VERFITYCODE})
    public JsonResult delete(@RequestBody Map map) throws Exception {
        this.environmentService.delete(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改环境
     *
     * @param environment
     * @return
     * @throws Exception
     */
    @PostMapping("/update")
    @NoNull(require = {Constants.ID, Constants.NAME_EN, Constants.NAME_CN}, parameter = Environment.class)
    public JsonResult update(@RequestBody @ApiParam Environment environment) throws Exception {
        return JsonResultUtil.buildSuccess(this.environmentService.update(environment));
    }

    /**
     * 通过标签查询环境信息，若传一个标签是模糊查询，反之为精确查询
     *
     * @param environment
     * @return
     * @throws Exception
     */
    @PostMapping("/queryByLabels")
    @NoNull(require = {Constants.LABELS}, parameter = Environment.class)
    public JsonResult queryByLabels(@RequestBody @ApiParam Environment environment) throws Exception {
        return JsonResultUtil.buildSuccess(this.environmentService.queryByLabels(environment));
    }

    @PostMapping("/queryAllLabels")
    public JsonResult queryAllLabels() throws Exception {
        return JsonResultUtil.buildSuccess(this.ilabelService.queryAllLabels());
    }

    /**
     * 通过标签模糊查询环境信息(若包括多个网段标签，需要拆开去查)
     *
     * @param requestParamMap
     * @return
     */
    @PostMapping("/queryByLabelsFuzzy")
    @RequestValidate(NotEmptyFields = {Constants.LABELS})
    public JsonResult queryByLabelsFuzzy(@RequestBody Map<String, Object> requestParamMap) {
        return JsonResultUtil.buildSuccess(this.environmentService.queryByLabelsFuzzy(requestParamMap));
    }

    @PostMapping("/queryEnvByAppId")
    @RequestValidate(NotEmptyFields = {Constants.APP_ID})
    public JsonResult queryEnvByAppId(@RequestBody Map<String, String> requestParamMap) {
        return JsonResultUtil.buildSuccess(this.environmentService.queryEnvByAppId(requestParamMap));
    }

    @PostMapping("/querySccEnvByAppId")
    @RequestValidate(NotEmptyFields = {Constants.APP_ID})
    public JsonResult querySccEnvByAppId(@RequestBody Map<String, String> requestParamMap) throws Exception {
        return JsonResultUtil.buildSuccess(this.environmentService.querySccEnvByAppId(requestParamMap));
    }

}

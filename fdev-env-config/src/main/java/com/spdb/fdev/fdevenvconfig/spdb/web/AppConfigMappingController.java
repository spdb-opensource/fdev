package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppConfigMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/7 15:24
 * @Version 1.0
 */
@RequestMapping("/api/v2/appConfig")
@RestController
public class AppConfigMappingController {

    @Autowired
    private IAppConfigMappingService appConfigMappingService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * webhook 调用。 扫描 gitlab 对应分支的配置文件 保存到数据库
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/saveAppConfigByWebHook")
    public JsonResult saveAppConfigByWebHook(@RequestBody Map<String, Object> requestParam) throws Exception {

        this.appConfigMappingService.saveAppConfigByWebHook(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 通过 应用 gitlab id ，分支名，环境，node 查询 实体环境映射值
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/queryConfigVariables")
    @NoNull(require = {Dict.GITLABID, Dict.BRANCH, Dict.ENV_NAME, Dict.NODE}, parameter = LinkedHashMap.class)
    public JsonResult queryConfigVariables(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appConfigMappingService.queryConfigVariables(requestParam));
    }
}

package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ICaasYamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/3-0:05
 * @Description: caasYaml 外部调用接口
 */
@RestController
@RequestMapping("/api/v2/caas")
public class CaasYamlController {

    @Autowired
    private ICaasYamlService iCaasYamlService;

    /*
    * @author:guanz2
    * @Description: caas 生成老yaml接口
    * 返回值：老yaml, 以json格式返回，并且本地生成对应的文件
    */
    @PostMapping("/getCaasOldYaml")
    @NoNull(require = {Dict.DEPLOYNAME}, parameter = LinkedHashMap.class)
    public JsonResult getCaasOldYaml(@RequestBody Map map) throws Exception {
        String deployment_name = (String)map.get(Dict.DEPLOYNAME);
        return JsonResultUtil.buildSuccess(iCaasYamlService.getCaasOldYaml(deployment_name));
    }

    /*
    * @author:guanz2
    * @Description: caas 生成新yaml接口
    * 返回值：新yaml, 以json格式返回
    */
    @PostMapping("/getCaasNewYaml")
    @NoNull(require = {Dict.DEPLOYNAME, Dict.TAG}, parameter = LinkedHashMap.class)
    public JsonResult getCaasNewYaml(@RequestBody Map map) throws Exception {
        String deployment_name = (String)map.get(Dict.DEPLOYNAME);
        String tag  = (String)map.get(Dict.TAG);
        return JsonResultUtil.buildSuccess(iCaasYamlService.getCaasNewYaml(deployment_name, tag));
    }
}

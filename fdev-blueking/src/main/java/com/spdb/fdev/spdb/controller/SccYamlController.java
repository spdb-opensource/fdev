package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ISccYamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/6-10:28
 * @Description: sccYaml外部调用接口
 *
 */
@RestController
@RequestMapping("/api/v2/scc")
public class SccYamlController {

    @Autowired
    private ISccYamlService sccYamlService;


    /*
    * @author:guanz2
    * @Description: scc 生成老yaml接口
    * 返回值：老yaml, 以json格式返回，并且本地生成对应的文件
    */
   @PostMapping("/getSccOldYaml")
   @NoNull(require = {"deploy_name"}, parameter = LinkedHashMap.class)
    public JsonResult getSccOldYaml(@RequestBody Map map) throws Exception {
        String resource_code = (String) map.get("deploy_name");
        return JsonResultUtil.buildSuccess(sccYamlService.getSccOldYaml(resource_code));
    }

    /*
     * @author:guanz2
     * @Description: scc 生成新yaml接口
     * 返回值：新yaml, 以json格式返回
     */
    @PostMapping("/getSccNewYaml")
    @NoNull(require = {"deploy_name", "tag"}, parameter = LinkedHashMap.class)
    public JsonResult getSccNewYaml(@RequestBody Map map) throws Exception {
        String resource_code = (String)map.get("deploy_name");
        String tag = (String) map.get("tag");
        return JsonResultUtil.buildSuccess(sccYamlService.getSccNewYaml(resource_code, tag));
    }
}

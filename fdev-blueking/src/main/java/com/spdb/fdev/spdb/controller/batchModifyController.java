package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.IBatchModifyService;
import com.spdb.fdev.spdb.service.ICaasModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Author:guanz2
 * @Date:2022/2/23-17:05
 * @Description:批量修改
 * */
@RestController
@RequestMapping("/api/v2/batchModify")
public class batchModifyController {

    @Autowired
    private IBatchModifyService batchModifyService;

    /**
    * @author:guanz2
    * @Description:批量修改esf应用云上、云下的env
    */
    @PostMapping("/esfModifyEnv")
    @NoNull(parameter = ArrayList.class)
    public JsonResult updateCaasModifyKey(@RequestBody List<Map<String,Object>> list) throws Exception {
        batchModifyService.updateEsfEnv(list);
        return JsonResultUtil.buildSuccess();
    }


    //修改云上yaml文件，目前仅仅支持relicas, cpu,memory
    @PostMapping("/sccModifyYamlBatch")
    @NoNull(parameter = ArrayList.class)
    public JsonResult sccModifyYamlBatch(@RequestBody List<Map<String, Object>> list) throws Exception {
        batchModifyService.updateSccYamlBatch(list);
        return JsonResultUtil.buildSuccess();
    }


    //云上、云下应用添加initContainers
    @PostMapping("/addInitContainers")
    @NoNull(require = {"deploy_list"}, parameter = LinkedHashMap.class)
    public JsonResult addInitContainers(@RequestBody Map<String, List> deploy_list) throws Exception {
        batchModifyService.addInitContainers(deploy_list.get("deploy_list"));
        return JsonResultUtil.buildSuccess();
    }

    //云上修改滚动发布参数
    @PostMapping("/sccModifyRollingParams")
    public JsonResult sccModifyRollingParams(@RequestBody Map<String, List> data) throws Exception {
       batchModifyService.updateSccRollingParams(data.get("pro"), data.get("gray"));
       return JsonResultUtil.buildSuccess();
    }
}

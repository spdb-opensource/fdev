package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ISccDeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:guanz2
 * @Date:2021/10/5-22:52
 * @Description: scc 拉取数据， 手动发起请求，拉取scc的数据
 */

@RequestMapping("/api/v2/scc")
@RestController
public class SccPullData {
    @Autowired
    private ISccDeploymentService sccDeployService;

    @GetMapping("/pullSccBluekingInfo")
    public JsonResult pullSccBluekingInfo() throws Exception {
        sccDeployService.pullBluekingDataByManual();
        return JsonResultUtil.buildSuccess();
    }
}

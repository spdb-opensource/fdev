package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ICaasDeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:guanz2
 * @Date:2021/10/2-12:36
 * @Description: caas 拉取数据 , 手动发起请求，拉取caas数据
 */
@RequestMapping("/api/v2/caas")
@RestController
public class CaasPullData {

    @Autowired
    private ICaasDeploymentService caasDeploymentService;

    @GetMapping("/pullCaasBluekingInfo")
    public JsonResult pullCaasBluekingInfo() throws Exception {
        caasDeploymentService.pullBluekingDataByManual();
        return JsonResultUtil.buildSuccess();
    }
}

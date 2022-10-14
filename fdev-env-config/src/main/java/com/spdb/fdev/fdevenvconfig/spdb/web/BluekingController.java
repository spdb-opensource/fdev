package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.annotation.requestLimit.RequestLimit;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.service.IBluekingService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: lisy26
 * @date: 2021/1/4 15:36
 * @ClassName: BluekingController
 * @Description: 从蓝鲸信息平台获取生产信息内容, 并展示在fdev中
 */
@Api(tags = "蓝鲸获取生产信息接口")
@RequestMapping("/api/v2/blueking")
@RestController
public class BluekingController {

    @Autowired
    private IBluekingService bluekingService;

    /**
     * @Author： lisy26
     * @Description： 同步蓝鲸生产信息
     * @Date： 2021/1/4 16:00
     * @Param: []
     * @return: com.spdb.fdev.common.JsonResult
     **/
    @GetMapping("/updateBluekingInfo")
    //用户权限和请求频率校验
    @RequestLimit
    public JsonResult updateBluekingInfo() throws Exception {
        bluekingService.updateBlueking();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @Author： lisy26
     * @Description： 查询蓝鲸全量数据
     * @Date： 2021/1/7 15:35
     * @Param: [pageNum, pageSize, name]
     * @return: com.spdb.fdev.common.JsonResult
     **/
    @PostMapping("/getAllDeployments")
    @NoNull(require = {Dict.PAGE, Dict.PERPAGE}, parameter = LinkedHashMap.class)
    public JsonResult getAllDeployments(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.bluekingService.getAllDeployments(map));

    }

    /**
     * @Author： lisy26
     * @Description： 查询蓝鲸应用详情
     * @Date： 2021/1/15 0:44
     * @Param: [map]
     * @return: com.spdb.fdev.common.JsonResult
     **/
    @PostMapping("/listDeploymentDetail")
    @NoNull(require = {Dict.DEPLOYMENT, Dict.CLUSTER, Dict.AREA}, parameter = LinkedHashMap.class)
    public JsonResult listDeploymentDetail(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.bluekingService.listDeploymentDetail(map));
    }
}

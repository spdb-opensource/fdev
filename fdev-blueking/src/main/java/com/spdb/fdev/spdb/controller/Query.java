package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ICaasModifyKeyService;
import com.spdb.fdev.spdb.service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/12/10-16:28
 * @Description:查询caas scc 数据信息
 */
@RestController
@RequestMapping("/api/v2/query")
public class Query {

    @Autowired
    private IQueryService queryService;

    @PostMapping("/proInfo")
    public JsonResult getProInformation() throws Exception {
      return JsonResultUtil.buildSuccess(queryService.queryAllCount());
    }

    @PostMapping("/clusters")
    @NoNull(require = {Dict.DEPLOYNAME}, parameter = LinkedHashMap.class)
    public JsonResult getClusterInformation(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(queryService.queryClusterInfo((String) map.get(Dict.DEPLOYNAME)));
    }

    @PostMapping("/caasDeploymentDetail")
    @NoNull(require = {Dict.DEPLOYNAME, Dict.CLUSTER, Dict.VLAN}, parameter = LinkedHashMap.class)
    public JsonResult getCaasDeploymentDetail(@RequestBody Map map) throws Exception {
        String deploy_name = (String) map.get(Dict.DEPLOYNAME);
        String cluster = (String) map.get(Dict.CLUSTER);
        String vlan = (String) map.get(Dict.VLAN);
        return JsonResultUtil.buildSuccess(queryService.getCaasDeploymentDetail(deploy_name, cluster, vlan));
    }

    @PostMapping("/sccDeploymentDetail")
    @NoNull(require = {Dict.DEPLOYNAME, Dict.NAMESPACE, Dict.VLAN}, parameter = LinkedHashMap.class)
    public JsonResult getSccDeploymentDetail(@RequestBody Map map) throws Exception {
        String deploy_name = (String) map.get(Dict.DEPLOYNAME);
        String namespace = (String) map.get(Dict.NAMESPACE);
        String vlan = (String) map.get(Dict.VLAN);
        return JsonResultUtil.buildSuccess(queryService.getSccDeploymentDetail(deploy_name, namespace, vlan));
    }

    @PostMapping("/deployment")
    @NoNull(require = {Dict.PAGE, Dict.PAGESIZE, Dict.PARAMS}, parameter = LinkedHashMap.class)
    public JsonResult getDeployment(@RequestBody Map map) throws Exception {
        String page = (String) map.get(Dict.PAGE);
        String pageSize = (String) map.get(Dict.PAGESIZE);
        Map params = (Map) map.get(Dict.PARAMS);
        return JsonResultUtil.buildSuccess(queryService.getDeployment(page, pageSize, params));
    }
}

package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;
import com.spdb.fdev.fdevapp.spdb.entity.DomainEntity;
import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;
import com.spdb.fdev.fdevapp.spdb.service.IDomainService;
import com.spdb.fdev.fdevapp.spdb.service.IServiceSystemService;
import com.spdb.fdev.fdevapp.spdb.service.ISystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "服务系统")
@RestController
@RequestMapping(value = "/api/serviceSystem")
public class ServiceSystemController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private IServiceSystemService serviceSystemService;

    @Autowired
    private IDomainService domainService;

    @Autowired
    private ISystemService systemService;

    @ApiOperation(value = "应用所属业务域")
    @RequestMapping(value = "/queryServiceSystem", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult query(@RequestBody Map<String, Object> requestMap) throws Exception {
        List<ServiceSystem> list = serviceSystemService.query(CommonUtils.map2Object(requestMap, ServiceSystem.class));
        return JsonResultUtil.buildSuccess(list);
    }

    @ApiOperation(value = "应用所属域")
    @ResponseBody
    @RequestMapping(value = "/queryDomains", method = RequestMethod.POST)
    public JsonResult queryDomains(@RequestBody Map<String, Object> requestMap) throws Exception {
        List<DomainEntity> result = domainService.query(CommonUtils.map2Object(requestMap, DomainEntity.class));
        logger.info("服务域查询 返回 ： " + result);
        return JsonResultUtil.buildSuccess(result);
    }


    @ApiOperation(value = "应用所属系统")
    @RequestMapping(value = "/querySystem", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult querySystem(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(this.systemService.getSystem(CommonUtils.map2Object(requestMap, AppSystem.class)));
    }

//    @ApiOperation(value = "应用所属系统")
//    @RequestMapping(value = "/saveSystem", method = RequestMethod.POST)
//    @ResponseBody
//    public JsonResult saveSystem(@RequestBody Map<String, Object> requestMap) throws Exception {
//        AppSystem system = CommonUtils.map2Object(requestMap, AppSystem.class);
//        return JsonResultUtil.buildSuccess(this.systemService.save(system));
//    }


    @ApiOperation(value = "根据应用id的list集合查询系统")
    @RequestMapping(value = "/querySystemByIds", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult querySystemByIds(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(this.systemService.querySystemByIds(requestMap));
    }



}

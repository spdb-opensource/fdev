package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.spdb.entity.AppType;
import com.spdb.fdev.fdevapp.spdb.service.IAppTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.FastArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "应用类型接口")
@RequestMapping("/api/type")
@RestController
public class AppTypeController {
    @Autowired
    private IAppTypeService appTypeService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public JsonResult save(@RequestBody @ApiParam AppType appType) throws Exception {
        return JsonResultUtil.buildSuccess(this.appTypeService.save(appType));
    }

    @RequestMapping(value = "/findbyid", method = RequestMethod.POST)
    @NoNull(require = {Dict.ID}, parameter = AppType.class)
    public JsonResult findById(@RequestBody @ApiParam AppType appType) throws Exception {
        return JsonResultUtil.buildSuccess(this.appTypeService.findById(appType.getId()));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @NoNull(require = {Dict.ID}, parameter = AppType.class)
    public JsonResult update(@RequestBody @ApiParam AppType appType) throws Exception {
        return JsonResultUtil.buildSuccess(this.appTypeService.update(appType));
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public JsonResult query(@RequestBody @ApiParam AppType appType) throws Exception {
        List<AppType> list = this.appTypeService.query(appType);
        List<Map> mapList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (AppType type : list) {
                Map map = new HashMap();
                map.put(Dict.LABEL, type.getName());
                map.put(Dict.VALUE, type.getId());
                mapList.add(map);
            }
        }
        return JsonResultUtil.buildSuccess(mapList);
    }

}

package com.spdb.fdev.fdevinterface.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.service.RoutesService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RequestMapping("/api/interface")
@RestController
public class RoutesController {

    @Resource
    private RoutesService routesService;

    /**
     * 查询路由列表
     *
     * @param params
     * @return
     */
    @PostMapping("/queryRoutes")
    public JsonResult queryRoutes(@RequestBody Map params) {
        if (StringUtils.isEmpty(params.get(Dict.NAME)) && StringUtils.isEmpty(params.get(Dict.PROJECTNAME))
                && StringUtils.isEmpty(params.get(Dict.PATH)) && StringUtils.isEmpty(params.get(Dict.MODULE))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        return JsonResultUtil.buildSuccess(routesService.getRoutesApi(params));
    }

    /**
     * 查询路由调用关系
     *
     * @param params
     * @return
     */
    @PostMapping("/queryRoutesRelation")
    public JsonResult queryRoutesRelation(@RequestBody Map params) {
        if (StringUtils.isEmpty(params.get(Dict.NAME)) && StringUtils.isEmpty(params.get("sourceProject"))
                && StringUtils.isEmpty(params.get("targetProject"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        return JsonResultUtil.buildSuccess(routesService.getRoutesRelation(params));
    }

    /**
     * 获取路由各个版本的详情
     *
     * @param params
     * @return
     */
    @PostMapping("/queryRoutesDetailVer")
    public JsonResult queryRoutesDetailVer(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(routesService.getRoutesDetailVer(params));
    }
}

package com.spdb.fdev.fdevinterface.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;
import com.spdb.fdev.fdevinterface.spdb.service.AppDatService;
import com.spdb.fdev.fdevinterface.spdb.service.AppJsonService;
import com.spdb.fdev.fdevinterface.spdb.service.RoutesJsonService;
import com.spdb.fdev.fdevinterface.spdb.service.TotalJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/27 16:33
 */
@RequestMapping("/api/interface")
@RestController
public class RoutesDatController {

    @Autowired
    private RoutesJsonService routesDatService;
    @Autowired
    private AppDatService appDatService;
    @Autowired
    private AppJsonService appJsonService;
    @Autowired
    private TotalJsonService totalDatService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 持续集成时，生成相关介质入口
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/buildTestDatTar")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.BRANCH, Dict.PROJECT_JSON})
    public JsonResult buildTestDatTar(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(routesDatService.buildTestDatTar(requestMap));
    }

    /**
     * 投产介质准备时，生成相关介质入口
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/buildProDatTar")
    @RequestValidate(NotEmptyFields = {Dict.ENV, Dict.PROD_ID, Dict.PROD_VERSION, Dict.PROJECT_NAME})
    public JsonResult buildProDatTar(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(routesDatService.buildProDatTar(requestMap));
    }

    /**
     * 应用路由配置介质列表
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/queryAppJsonList")
    public JsonResult queryAppJsonList(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(appJsonService.getAppJsonList(requestMap));
    }

    /**
     * 查询最新的应用路由
     * @param requestMap
     * @return
     */
    @PostMapping("/queryLastAppJson")
    public JsonResult queryLastAppJson(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(appJsonService.queryLastAppJson(requestMap));
    }


    /**
     * 总路由配置介质列表
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/queryTotalJsonList")
    public JsonResult queryTotalJsonList(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(totalDatService.getTotalJsonList(requestMap));
    }

    /**
     * 总路由配置介质历史记录
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/queryTotalJsonHistory")
    @RequestValidate(NotEmptyFields = {Dict.ENV})
    public JsonResult queryTotalJsonHistory(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(totalDatService.queryTotalJsonHistory(requestMap));
    }

    /**
     * 导出路由配置介质
     *
     * @param fileName
     * @return
     */
    @GetMapping("/downloadTar/{fileName}")
    public void exportFile(@PathVariable String fileName, HttpServletResponse response) {
        if (!fileName.endsWith(Constants.TAR_FILE)&&!fileName.endsWith(Constants.JSON_FILE)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        routesDatService.exportFile(fileName, response);
    }
    
    /**
     * 持续集成时，云上路由介质生成
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/buildSccDatTar")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.BRANCH, Dict.PROJECT_JSON})
    public JsonResult buildSccDatTar(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(routesDatService.buildSccDatTar(requestMap));
    }

}

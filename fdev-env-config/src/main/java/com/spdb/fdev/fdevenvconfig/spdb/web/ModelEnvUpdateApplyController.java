package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.ModelEnvUpdateApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实体与环境映射更新卡点
 */
@RestController
@RequestMapping("/api/v2/modelEnvUpdateApply")
public class ModelEnvUpdateApplyController {

    @Autowired
    private ModelEnvUpdateApplyService modelEnvUpdateApplyService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 新增申请记录【权限：环境配置管理员】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/save")
    public JsonResult save(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelEnvUpdateApplyService.save(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 申请记录列表
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/list")
    public JsonResult list(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(modelEnvUpdateApplyService.listModelEnvUpdateApplysByPage(requestMap));
    }

    /**
     * 核对，返回属性映射值的差异【权限：申请人】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/compare")
    public JsonResult compare(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(modelEnvUpdateApplyService.compare(requestMap));
    }

    /**
     * 下载受影响的应用信息【权限：申请人】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/downloadAppInfo")
    public JsonResult downloadAppInfo(@RequestBody Map<String, Object> requestMap, HttpServletResponse response) throws Exception {
        return JsonResultUtil.buildSuccess(modelEnvUpdateApplyService.downloadAppInfo(requestMap, response));
    }

    /**
     * 完成核对，立即生效【权限：申请人】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/finish")
    public JsonResult finish(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelEnvUpdateApplyService.finish(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 恢复，将overtime的记录改为checking【权限：环境配置管理员】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/update")
    public JsonResult update(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelEnvUpdateApplyService.update(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 取消，即放弃本次修改【权限：申请人】
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/cancel")
    public JsonResult cancel(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelEnvUpdateApplyService.cancel(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 导出world文件 接口
     *
     * @param fileName
     * @return
     */
    @GetMapping("/downloadWorld/{fileName}")
    public void exportFile(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        if (!fileName.endsWith(Dict.FILE_DOC)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请求参数不被准许"});
        }
        modelEnvUpdateApplyService.exportFile(fileName, response);
    }

    @PostMapping("/checkConnectionDocker")
    @NoNull(require = {Constants.FDEV_CAAS_SERVICE_REGISTRY, Constants.FDEV_CAAS_USER, Dict.FDEV_CAAS_PD}, parameter = LinkedHashMap.class)
    public JsonResult checkConnectionDocker(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(modelEnvUpdateApplyService.checkConnectionDocker(requestMap));
    }

}

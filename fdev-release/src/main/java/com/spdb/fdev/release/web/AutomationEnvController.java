package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.AutomationEnv;
import com.spdb.fdev.release.service.IAutomationEnvService;
import com.spdb.fdev.release.service.IRoleService;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RequestMapping("/api/automationenv")
@RestController
public class AutomationEnvController {

    private static final Logger logger = LoggerFactory.getLogger(AutomationEnvController.class);

    @Autowired
    private IAutomationEnvService automationEnvService;

    @Autowired
    private IRoleService roleService;

    @PostMapping(value = "/query")
    public JsonResult query() {
        List<AutomationEnv> automationEnvList = automationEnvService.query();
        List<Map<String, String>> list = new ArrayList<>();
        for (AutomationEnv ae : automationEnvList) {
            Map<String, String> map = new HashMap<>();
            map.put(Dict.ID, ae.getId());
            map.put(Dict.ENV_NAME, ae.getEnv_name());
            map.put(Dict.FDEV_ENV_NAME, ((Map<String, String>) ae.getFdev_env_name().get("proc")).get("dmz"));
            map.put(Dict.DESCRIPTION, ae.getDescription());
            list.add(map);
        }
        return JsonResultUtil.buildSuccess(list);
    }

    @RequestValidate(NotEmptyFields = {Dict.ENV_NAME, Dict.DESCRIPTION , "platform"})
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        automationEnvService.addAutomationEnv(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.ENV_NAME, Dict.DESCRIPTION, "platform"})
    @PostMapping(value = "/update")
    public JsonResult update(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        automationEnvService.update(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = requestParam.get(Dict.ID);
        automationEnvService.deleteById(id);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/queryByProdType")
    public JsonResult queryByProdType(@RequestBody @ApiParam Map<String, String> requestParam) {
        List<AutomationEnv> list = automationEnvService.query();
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/queryToProdScale")
    public JsonResult queryToProdScale(@RequestBody @ApiParam Map<String, String> requestParam) {
        List<AutomationEnv> automationEnvList = automationEnvService.query();
        List<Map<String, Object>> list = new ArrayList<>();
        for (AutomationEnv ae : automationEnvList) {
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.ID, ae.getId());
            map.put(Dict.ENV_NAME, ae.getEnv_name());
            map.put("platform",ae.getPlatform());
            map.put(Dict.DESCRIPTION, ae.getDescription());
            //处理scc环境
            if (null != ae.getScc_fdev_env_name()) {
                Map<String, Object> scc_fdev_env_name = ae.getScc_fdev_env_name();
                if (null != scc_fdev_env_name.get("proc")) {  // scc生产
                    Map<String, Object> proc = (Map<String, Object>) scc_fdev_env_name.get("proc");
                    if (!CommonUtils.isNullOrEmpty(proc.get("dmz"))) { // 网银网段
                        String dmz_str = (String) proc.get("dmz");
                        map.put("pro_dmz_scc", dmz_str);
                    }
                    if (!CommonUtils.isNullOrEmpty(proc.get("biz"))) { // 业务网段
                        String biz_str = (String) proc.get("biz");
                        map.put("pro_biz_scc", biz_str);
                    }
                } else {
                    map.put("pro_dmz_scc", "");
                    map.put("pro_biz_scc", "");
                }
                if (null != scc_fdev_env_name.get("gray")) {  // scc灰度
                    Map<String, Object> gray = (Map<String, Object>) scc_fdev_env_name.get("gray");
                    if (!CommonUtils.isNullOrEmpty(gray.get("dmz"))) { // 网银网段
                        String dmz_str  = (String) gray.get("dmz");
                        map.put("gray_dmz_scc", dmz_str);
                    }
                    if (!CommonUtils.isNullOrEmpty(gray.get("biz"))) { // 业务网段
                        String biz_str =  (String) gray.get("biz");
                        map.put("gray_biz_scc", biz_str);
                    }

                } else {
                    map.put("gray_dmz_scc", "");
                    map.put("gray_biz_scc", "");
                }
            } else {
                map.put("pro_dmz_scc", "");
                map.put("pro_biz_scc", "");
                map.put("gray_dmz_scc", "");
                map.put("gray_biz_scc", "");
            }
            // 处理caas环境
            if (null != ae.getFdev_env_name()) {
                Map<String, Object> fdev_env_name = ae.getFdev_env_name();
                if (null != fdev_env_name.get("proc")) {  // caas生产
                    Map<String, Object> proc = (Map<String, Object>) fdev_env_name.get("proc");
                    if (!CommonUtils.isNullOrEmpty(proc.get("dmz"))) { // 网银网段
                        String dmz_str =  (String) proc.get("dmz");
                        map.put("pro_dmz_caas", dmz_str);
                    }
                    if (!CommonUtils.isNullOrEmpty(proc.get("biz"))) { // 业务网段
                        String biz_str = (String) proc.get("biz");
                        map.put("pro_biz_caas", biz_str);
                    }
                } else {
                    map.put("pro_dmz_caas", "");
                    map.put("pro_biz_caas", "");
                }
                if (null != fdev_env_name.get("gray")) {  // caas灰度
                    Map<String, Object> gray = (Map<String, Object>) fdev_env_name.get("gray");
                    if (!CommonUtils.isNullOrEmpty(gray.get("dmz"))) { // 网银网段
                        String dmz_str =  (String) gray.get("dmz");
                        map.put("gray_dmz_caas", dmz_str);
                    }
                    if (!CommonUtils.isNullOrEmpty(gray.get("biz"))) { // 业务网段
                        String biz_str =  (String) gray.get("biz");
                        map.put("gray_biz_caas", biz_str);
                    }

                } else {
                    map.put("gray_dmz_caas", "");
                    map.put("gray_biz_caas", "");
                }
            } else {
                map.put("pro_dmz_caas", "");
                map.put("pro_biz_caas", "");
                map.put("gray_dmz_caas", "");
                map.put("gray_biz_caas", "");
            }
            list.add(map);
        }
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/queryEnvParams")
    public JsonResult queryEnvParams(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        Map<String, Object> result = automationEnvService.queryEnvParams(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    @PostMapping(value = "/queryAutomationEnv")
    public JsonResult queryAutomationEnv(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        List<AutomationEnv> automationEnvList = automationEnvService.query();
        List<Map<String, Object>> list = new ArrayList<>();
        automationEnvList.forEach(automationEnv -> {
            Map<String,Object> envMap = new HashMap<>();
            envMap.put(Dict.ENV_NAME, automationEnv.getEnv_name());
            envMap.put(Dict.DESCRIPTION, automationEnv.getDescription());
            list.add(envMap);
        });
        return JsonResultUtil.buildSuccess(list);
    }

}

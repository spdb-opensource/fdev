package com.spdb.fdev.fdevinterface.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.service.YapiDataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @author c-bucc
 * @description 用于jsonSchemaDraft03向jsonSchemaDraft04转换，以及Yapi数据的导出
 * @creatDate 2020-08-18
 */
@RequestMapping("/api/interface")
@RestController
public class YapiDataExportController {
    @Resource
    private YapiDataExportService yapiDataExportService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * @param requeset (Yapi项目token)
     * @return
     * @description 向数据库中添加项目的基本信息, 包括项目名称，项目ID，token，导入人，接口
     * @author c-bucc
     * @creatDate 2020-08-18
     */
    @PostMapping("/importYapiProject")
    public JsonResult importYapiProject(@RequestBody Map requeset) throws Exception {
        return JsonResultUtil.buildSuccess(yapiDataExportService.addProjectInfor(requeset));
    }

    /**
     * @param request (yapi_token Yapi项目token  , interface_id 接口的id)
     * @return
     * @description 对项目中的接口进行添加或更新
     * @author c-bucc
     * @creatDate 2020-08-18
     */
    @PostMapping("/importYapiInterface")
    public JsonResult importYapiInterface(@RequestBody Map request) throws Exception {
        yapiDataExportService.importInterface(request);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @return
     * @description 查询项目列表
     * @author c-bucc api/interface/yapiProjectList
     * @creatDate 2020-08-18
     */
    @PostMapping("/yapiProjectList")
    public JsonResult yapiProjectList(@RequestBody Map request) {
        return JsonResultUtil.buildSuccess(yapiDataExportService.yapiProjectList(request));
    }

    /**
     * @return
     * @description 查询接口列表
     * @author c-bucc api/interface/yapiInterfaceList
     * @creatDate 2020-08-18
     */
    @PostMapping("/yapiInterfaceList")
    public JsonResult yapiInterfaceList(@RequestBody Map request) {
        return JsonResultUtil.buildSuccess(yapiDataExportService.yapiProjectListWithInterfaces(request));
    }

    /**
     * @param request (jsonSchemaDraft03的内容)
     * @return
     * @description jsonSchemaDraft03向jsonSchemaDraft04的转换
     * @author c-buccc
     * @creatDate 2020-08-18
     */
    @PostMapping("/convertJsonSchema")
    public JsonResult convertJsonSchema(@RequestBody Map request) {
        String changeAfterJson = yapiDataExportService.convertJsonSchema(request);
        return JsonResultUtil.buildSuccess(changeAfterJson);
    }

    /**
     * @return
     * @description 删除项目应用
     * @author c-bucc api/interface/deleteYapiProject
     * @creatDate 2020-08-26
     */
    @PostMapping("/deleteYapiProject")
    public JsonResult deleteYapiProject(@RequestBody Map request) throws Exception {
        yapiDataExportService.deleteProject(request);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @return
     * @description 删除单个接口
     * @author c-bucc api/interface/deleteYapiInterface
     * @creatDate 2020-08-26
     */
    @PostMapping("/deleteYapiInterface")
    public JsonResult deleteYapiInterface(@RequestBody Map request) throws Exception {
        yapiDataExportService.deleteInterface(request);
        return JsonResultUtil.buildSuccess();
    }
}

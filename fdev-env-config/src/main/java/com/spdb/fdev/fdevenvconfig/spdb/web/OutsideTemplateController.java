package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.entity.OutSideTemplate;
import com.spdb.fdev.fdevenvconfig.spdb.service.IOutsideTemplateService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v2/outSideTemplate")
public class OutsideTemplateController {

    @Autowired
    private IOutsideTemplateService outSideTemplate;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 用于存储外部参数配置模版(优先生效)
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping("/outsideTemplateSave")
    @RequestValidate(NotEmptyFields = {Dict.PROJECT_ID, Dict.BRANCH})
    public JsonResult outsideTemplateSave(@RequestBody @ApiParam Map<String, Object> requestMap) throws Exception {

        String project_id = (String) requestMap.get(Dict.PROJECT_ID);
        String branch = (String) requestMap.get(Dict.BRANCH);
        List<Map<String, String>> variables = (List<Map<String, String>>) requestMap.get(Dict.VARIABLES);
        return JsonResultUtil.buildSuccess(this.outSideTemplate.outsideTemplateSave(project_id, branch, variables));
    }

    /**
     * 查询外部参数配置模版
     *
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    public JsonResult queryOutsideTemplate(@RequestBody @ApiParam OutSideTemplate outSideTemplate) throws Exception {
        return JsonResultUtil.buildSuccess(this.outSideTemplate.queryOutsideTemplate(outSideTemplate));
    }

    /**
     * 更新外部参数配置模版
     *
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    @PostMapping("/update")
    public JsonResult updateOutsideTemplate(@RequestBody @ApiParam OutSideTemplate outSideTemplate) throws Exception {

        return JsonResultUtil.buildSuccess(this.outSideTemplate.updateOutsideTemplate(outSideTemplate));
    }

}

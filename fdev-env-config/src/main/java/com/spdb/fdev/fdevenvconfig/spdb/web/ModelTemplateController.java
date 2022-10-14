package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: lisy26
 * @date: 2020/11/16 14:02
 * @ClassName ModelTemplateController
 * @Description
 **/
@Api(tags = "实体模板接口")
@RequestMapping("/api/v2/modelTemplate")
@RestController
public class ModelTemplateController {

    @Autowired
    private IModelTemplateService modelTemplateService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;


    /**
     * 新增实体模板信息
     *
     * @param modelTemplate
     * @return
     * @throws Exception
     */
    @PostMapping("/add")
    @NoNull(require = {Dict.NAMEEN, Dict.NAMECN,Dict.ENVKEY,Dict.ENVKEYPROPKEY,Dict.ENVKEYPROPNAMECN,Dict.ENVKEYDATATYPE},
            parameter = ModelTemplate.class)
    public JsonResult add(@RequestBody @ApiParam ModelTemplate modelTemplate) throws Exception {
//        //验证用户是否是互联网应用组用户
//        if (!userVerifyUtil.userGroupVerify("互联网应用")) {
//            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户不是互联网应用组的用户，身份验证失败"});
//        }
        modelTemplateService.add(modelTemplate);
        return JsonResultUtil.buildSuccess("新增实体模板成功!");
    }

    /**
     * 实体模板分页查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/pageQuery")
    @NoNull(require = {Dict.PAGE, Dict.PERPAGE}, parameter = LinkedHashMap.class)
    public JsonResult query(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelTemplateService.pageQuery(map));
    }

    /**
     * 实体模板详情查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    @NoNull(require = {Dict.ID}, parameter = LinkedHashMap.class)
    public JsonResult queryById(@RequestBody Map map) throws Exception {
        String id = (String) map.get("id");
        return JsonResultUtil.buildSuccess(this.modelTemplateService.queryById(id));
    }
    
    /**
     * 实体模板详情查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/queryByNameEn")
    @NoNull(require = {Dict.NAMEEN}, parameter = LinkedHashMap.class)
    public JsonResult queryByNameEn(@RequestBody Map map) throws Exception {
        String nameEn = (String) map.get(Dict.NAMEEN);
        return JsonResultUtil.buildSuccess(this.modelTemplateService.queryByNameEn(nameEn));
    }

}

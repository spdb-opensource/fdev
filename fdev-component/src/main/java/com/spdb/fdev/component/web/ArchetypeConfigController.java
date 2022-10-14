package com.spdb.fdev.component.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.service.IArchetypeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ArchetypeConfigController {

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    /**
     * 保存骨架配置模版
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/saveConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.BRANCH, Dict.ARCHETYPE_ID, Dict.CONTENT})
    public JsonResult saveConfigTemplate(@RequestBody Map hashMap) throws Exception {
        Object object = archetypeInfoService.saveConfigTemplate(hashMap);
        return JsonResultUtil.buildSuccess(object);
    }

    /**
     * 查询骨架配置模版
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.BRANCH, Dict.ARCHETYPE_ID})
    public JsonResult queryConfigTemplate(@RequestBody Map hashMap) throws Exception {
        String content = archetypeInfoService.queryConfigTemplate(hashMap);
        return JsonResultUtil.buildSuccess(content);
    }


}

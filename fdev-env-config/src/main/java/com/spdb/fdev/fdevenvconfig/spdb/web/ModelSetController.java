package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.ModelSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/modelSet")
public class ModelSetController {

    @Autowired
    private ModelSetService modelSetService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 获取实体组模板
     *
     * @return
     */
    @PostMapping("/getTemplate")
    public JsonResult getModelSetTemplate() {
        return JsonResultUtil.buildSuccess(modelSetService.getModelSetTemplate());
    }

    /**
     * 根据实体组模板，获取二级分类下的所有实体
     *
     * @return
     */
    @PostMapping("/getModles")
    public JsonResult getModles(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(modelSetService.getModles(requestMap));
    }

    /**
     * 新增实体组
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/save")
    public JsonResult save(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelSetService.saveModelSet(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 获取实体组列表
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/list")
    public JsonResult list(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(modelSetService.listModelSetsByPage(requestMap));
    }

    /**
     * 更新实体组
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/update")
    public JsonResult update(@RequestBody Map<String, Object> requestMap) throws Exception {
        modelSetService.updateModelSet(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 删除实体组
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/delete")
    public JsonResult delete(@RequestBody Map<String, Object> requestMap) throws Exception {

        modelSetService.deleteModelSet(requestMap);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 获取实体组模板包含的实体
     *
     * @return
     */
    @PostMapping("/queryTemplateContainsModel")
    public JsonResult queryTemplateContainsModel() {
        return JsonResultUtil.buildSuccess(modelSetService.queryTemplateContainsModel());
    }
}

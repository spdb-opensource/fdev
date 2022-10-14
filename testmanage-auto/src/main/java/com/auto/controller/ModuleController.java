package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.entity.Module;
import com.auto.service.IModuleService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/module")
public class ModuleController {

    @Autowired
    private IModuleService iModuleService;

    /**
     * 新增
     *
     * @param module
     * @return
     * @throws Exception
     */
    @RequestMapping("/addModule")
    public JsonResult addModule(@RequestBody Module module) throws Exception {
        iModuleService.addModule(module);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryModule")
    public JsonResult queryModule(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iModuleService.queryModule(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.MODULE, data);
        result.put(Dict.SIZE, data.size());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteModule")
    @RequestValidate(NotEmptyFields = {Dict.MODULEID})
    public JsonResult deleteModule(@RequestBody Map map) throws Exception {
        iModuleService.deleteModule(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateModule")
    @RequestValidate(NotEmptyFields = {Dict.MODULEID, Dict.MODULENO})
    public JsonResult updateModule(@RequestBody Map map) throws Exception {
        iModuleService.updateModule(map);
        return JsonResultUtil.buildSuccess();
    }
}

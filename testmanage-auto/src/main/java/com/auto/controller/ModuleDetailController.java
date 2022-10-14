package com.auto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auto.dict.Dict;
import com.auto.entity.ModuleDetail;
import com.auto.service.IModuleDetailService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/moduledetail")
public class ModuleDetailController {

    @Autowired
    private IModuleDetailService iModuleDetailService;

    /**
     * 新增
     *
     * @param moduleDetail
     * @return
     * @throws Exception
     */
    @RequestMapping("/addModuleDetail")
    public JsonResult addModuleDetail(@RequestBody ModuleDetail moduleDetail) throws Exception {
        iModuleDetailService.addModuleDetail(moduleDetail);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryModuleDetail")
    public JsonResult queryModuleDetail(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iModuleDetailService.queryModuleDetail(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.MODULE, data);
        result.put(Dict.SIZE, data.size());
        return JsonResultUtil.buildSuccess(result);
    }
    
    /**
     * 根据moduleId查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryModuleDetailByModuleId")
    @RequestValidate(NotEmptyFields = {Dict.MODULEID})
    public JsonResult queryModuleDetailByModuleId(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iModuleDetailService.queryModuleDetailByModuleId(map.get(Dict.MODULEID));
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
    @RequestMapping("/deleteModuleDetail")
    @RequestValidate(NotEmptyFields = {Dict.MODULEDETAILID})
    public JsonResult deleteModuleDetail(@RequestBody Map map) throws Exception {
        iModuleDetailService.deleteModuleDetail(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateModuleDetail")
    @RequestValidate(NotEmptyFields = {Dict.MODULEDETAILID, Dict.MODULEID, Dict.ELEMENTSTEPNO})
    public JsonResult updateModuleDetail(@RequestBody Map map) throws Exception {
        iModuleDetailService.updateModuleDetail(map);
        return JsonResultUtil.buildSuccess();
    }
}

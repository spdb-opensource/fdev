package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.entity.Data;
import com.auto.service.IDataService;
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
@RequestMapping("/data")
public class DataController {

    @Autowired
    private IDataService iDataService;

    /**
     * 新增
     *
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping("/addData")
    @RequestValidate(NotEmptyFields = {Dict.LABEL})
    public JsonResult addData(@RequestBody Data data) throws Exception {
        iDataService.addData(data);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryData")
    public JsonResult queryData(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iDataService.queryData(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.DATA, data);
        result.put(Dict.SIZE, data.size());
        return JsonResultUtil.buildSuccess(result);
    }
    
    /**
     * 根据testcaseNo查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryDataByTestCaseNo")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    public JsonResult queryDataByTestCaseNo(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iDataService.queryDataByTestCaseNo(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.DATA, data);
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
    @RequestMapping("/queryDataByModuleId")
    @RequestValidate(NotEmptyFields = {Dict.MODULEID})
    public JsonResult queryDataByModuleId(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iDataService.queryDataByModuleId(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.DATA, data);
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
    @RequestMapping("/deleteData")
    @RequestValidate(NotEmptyFields = {Dict.DATAID})
    public JsonResult deleteData(@RequestBody Map map) throws Exception {
        iDataService.deleteData(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateData")
    @RequestValidate(NotEmptyFields = {Dict.DATAID,Dict.LABEL})
    public JsonResult updateData(@RequestBody Map map) throws Exception {
        iDataService.updateData(map);
        return JsonResultUtil.buildSuccess();
    }
}

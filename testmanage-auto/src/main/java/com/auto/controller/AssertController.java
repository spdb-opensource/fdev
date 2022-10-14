package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.entity.Assert;
import com.auto.entity.Data;
import com.auto.service.IAssertService;
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
@RequestMapping("/assert")
public class AssertController {

    @Autowired
    private IAssertService iAssertService;

    /**
     * 新增
     *
     * @param asrt
     * @return
     * @throws Exception
     */
    @RequestMapping("/addAssert")
    @RequestValidate(NotEmptyFields = {Dict.LABEL})
    public JsonResult addAssert(@RequestBody Assert asrt) throws Exception {
        iAssertService.addAssert(asrt);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryAssert")
    public JsonResult queryAssert(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iAssertService.queryAssert(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.ASSERT, data);
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
    @RequestMapping("/queryAssertByTestCaseNo")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    public JsonResult queryAssertByTestCaseNo(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> data = iAssertService.queryAssertByTestCaseNo(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.ASSERT, data);
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
    @RequestMapping("/deleteAssert")
    @RequestValidate(NotEmptyFields = {Dict.ASSERTID})
    public JsonResult deleteAssert(@RequestBody Map map) throws Exception {
        iAssertService.deleteAssert(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateAssert")
    @RequestValidate(NotEmptyFields = {Dict.ASSERTID,Dict.LABEL})
    public JsonResult updateAssert(@RequestBody Map map) throws Exception {
        iAssertService.updateAssert(map);
        return JsonResultUtil.buildSuccess();
    }
}

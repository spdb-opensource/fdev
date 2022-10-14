package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.entity.Testcase;
import com.auto.service.ICaseService;
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
@RequestMapping("/case")
public class CaseController {

    @Autowired
    private ICaseService iCaseService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addCase")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENAME})
    public JsonResult addCase(@RequestBody Map<String, String> map) throws Exception {
        iCaseService.addCase(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryCase")
    public JsonResult queryMenu(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> menu = iCaseService.queryCase(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.CASE, menu);
        result.put(Dict.SIZE, menu.size());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteCase")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    public JsonResult deleteCase(@RequestBody Map map) throws Exception {
        iCaseService.deleteCase(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateCase")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO, Dict.TESTCASENAME})
    public JsonResult updateCase(@RequestBody Map map) throws Exception {
        iCaseService.updateCase(map);
        return JsonResultUtil.buildSuccess();
    }
}

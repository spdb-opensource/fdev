package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.service.ICaseDetailService;
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
@RequestMapping("/casedetail")
public class CaseDetailController {

    @Autowired
    private ICaseDetailService iCaseDetailService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addCaseDetail")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO, Dict.STEPNO})
    public JsonResult addCaseDetail(@RequestBody Map<String, String> map) throws Exception {
        iCaseDetailService.addCaseDetail(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryCaseDetail")
    public JsonResult queryCaseDetail(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> menu = iCaseDetailService.queryCaseDetail(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.CASEDETAIL, menu);
        result.put(Dict.SIZE, menu.size());
        return JsonResultUtil.buildSuccess(result);
    }
    
    /**
     * 根据testcaseNo查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryCaseDetailByTestCaseNo")
    @RequestValidate(NotEmptyFields = {Dict.TESTCASENO})
    public JsonResult queryCaseDetailByTestCaseNo(@RequestBody Map<String, String> map) throws Exception {
        List<Map<String, String>> menu = iCaseDetailService.queryCaseDetailByTestCaseNo(map.get(Dict.TESTCASENO));
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.CASEDETAIL, menu);
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
    @RequestMapping("/deleteCaseDetail")
    @RequestValidate(NotEmptyFields = {Dict.DETAILID})
    public JsonResult deleteCaseDetail(@RequestBody Map<String, String[]> map) throws Exception {
        iCaseDetailService.deleteCaseDetail(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateCaseDetail")
    @RequestValidate(NotEmptyFields = {Dict.DETAILID, Dict.TESTCASENO, Dict.STEPNO})
    public JsonResult updateCaseDetail(@RequestBody Map<String, String> map) throws Exception {
        iCaseDetailService.updateCaseDetail(map);
        return JsonResultUtil.buildSuccess();
    }
}

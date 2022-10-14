package com.auto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auto.dict.Dict;
import com.auto.entity.Element;
import com.auto.service.IElementService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/element")
public class ElementController {

    @Autowired
    private IElementService iElementService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addElement")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTNAME})
    public JsonResult addElement(@RequestBody Map<String, String> map) throws Exception {
    	iElementService.addElement(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryElement")
    public JsonResult queryElement(@RequestBody Map<String, String> map) throws Exception {
        List<Element> element = iElementService.queryElement(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.ELEMENT, element);
        result.put(Dict.SIZE, element.size());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteElement")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTID})
    public JsonResult deleteElement(@RequestBody Map map) throws Exception {
    	iElementService.deleteElement(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateElement")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTID, Dict.ELEMENTNAME})
    public JsonResult updateElement(@RequestBody Map map) throws Exception {
    	iElementService.updateElement(map);
        return JsonResultUtil.buildSuccess();
    }
}

package com.auto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auto.dict.Dict;
import com.auto.entity.ElementDic;
import com.auto.service.IElementDicService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/elementdic")
public class ElementDicController {

    @Autowired
    private IElementDicService iElementDicService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addElementDic")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTDICMETHOD})
    public JsonResult addElementDic(@RequestBody Map<String, String> map) throws Exception {
    	iElementDicService.addElementDic(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryElementDic")
    public JsonResult queryElementDic(@RequestBody Map<String, String> map) throws Exception {
        List<ElementDic> elementDic = iElementDicService.queryElementDic(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.ELEMENTDIC, elementDic);
        result.put(Dict.SIZE, elementDic.size());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteElementDic")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTDICID})
    public JsonResult deleteElementDic(@RequestBody Map map) throws Exception {
    	iElementDicService.deleteElementDic(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateElementDic")
    @RequestValidate(NotEmptyFields = {Dict.ELEMENTDICID, Dict.ELEMENTDICMETHOD})
    public JsonResult updateElementDic(@RequestBody Map map) throws Exception {
    	iElementDicService.updateElementDic(map);
        return JsonResultUtil.buildSuccess();
    }
}

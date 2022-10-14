package com.auto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auto.dict.Dict;
import com.auto.entity.MenuSheet;
import com.auto.service.IMenuService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private IMenuService iMenuService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addMenu")
    @RequestValidate(NotEmptyFields = {Dict.MENUNO})
    public JsonResult addMenu(@RequestBody Map<String, String> map) throws Exception {
        iMenuService.addMenu(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryMenu")
    public JsonResult queryMenu(@RequestBody Map<String, String> map) throws Exception {
        List<MenuSheet> menu = iMenuService.queryMenu(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.MENU, menu);
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
    @RequestMapping("/deleteMenu")
    @RequestValidate(NotEmptyFields = {Dict.MENUSHEETID})
    public JsonResult deleteMenu(@RequestBody Map map) throws Exception {
        iMenuService.deleteMenu(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateMenu")
    @RequestValidate(NotEmptyFields = {Dict.MENUSHEETID,Dict.MENUNO})
    public JsonResult updateMenu(@RequestBody Map map) throws Exception {
        iMenuService.updateMenu(map);
        return JsonResultUtil.buildSuccess();
    }
}

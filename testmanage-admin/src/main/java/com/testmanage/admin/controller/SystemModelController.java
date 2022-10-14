package com.testmanage.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.testmanage.admin.entity.SystemModel;
import com.testmanage.admin.service.IFunctionMenuService;
import com.testmanage.admin.service.ISystemModelService;
import com.testmanage.admin.util.Dict;

@RestController
@RequestMapping("/api/systemModel")
public class SystemModelController {
	
    @Autowired
    private ISystemModelService systemModelService;
    @Autowired
    private IFunctionMenuService functionMenuService;

    //案例库,复用页面树形结构接口
    @RequestMapping("/listAll")
    @ResponseBody
    public JsonResult listAll() throws Exception{
        List<Map<String, Object>> systemModels = systemModelService.listAllToMap();
        List<Map> list = new ArrayList<>();
        for (Map<String, Object> map : systemModels) {
			list.add(map);
			List<Map<String, Object>> parentMenus = functionMenuService.queryMenuBysysId((Integer)map.get(Dict.SYS_ID));
			for (Map funcList : parentMenus) {
				list.add(funcList);
			}
		}
        return JsonResultUtil.buildSuccess(list);
    }
    
    //查询全部系统
    @RequestMapping("/queryAllSystem")
    @ResponseBody
    public JsonResult queryAllSystem() throws Exception{
        List<SystemModel> list = systemModelService.listAll();
        return JsonResultUtil.buildSuccess(list);
    }
    
}

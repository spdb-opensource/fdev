package com.testmanage.admin.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.testmanage.admin.entity.FunctionMenu;
import com.testmanage.admin.entity.SystemModel;
import com.testmanage.admin.service.IFunctionMenuService;
import com.testmanage.admin.service.ISystemModelService;
import com.testmanage.admin.util.CommonUtil;
import com.testmanage.admin.util.Dict;
import com.testmanage.admin.util.ErrorConstants;

@RestController
@RequestMapping("/api/functionMenu")
public class FunctionMenuController {

	private static final Logger logger = LoggerFactory.getLogger(FunctionMenuController.class);
	
    @Autowired
    private IFunctionMenuService functionMenuService;
    @Autowired
    private ISystemModelService systemModelService;
    

    //添加功能菜单
    @RequestMapping(value = "/addFunctionMenu", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addFunctionMenu(@RequestBody Map<String,Object> requestParam) throws Exception{
    	if((Integer)requestParam.get(Dict.SYS_FUNC_ID) < 1) {
    		throw new FtmsException(ErrorConstants.PARAM_ERROR,new String[] {Dict.SYS_FUNC_ID,"非法参数值"});
    	}
    	if((Integer)requestParam.get(Dict.LEVEL) < 1) {
    		throw new FtmsException(ErrorConstants.PARAM_ERROR,new String[] {Dict.LEVEL,"非法参数值"});
    	}
    	if(CommonUtil.isNullOrEmpty((String)requestParam.get(Dict.FUNC_MODEL_NAME))) {
    		throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {Dict.FUNC_MODEL_NAME});
    	}
    	if((Integer)requestParam.get(Dict.PARENT_ID) < 0) {
    		throw new FtmsException(ErrorConstants.PARAM_ERROR,new String[] {Dict.PARENT_ID,"非法参数值"});
    	}
    	functionMenuService.save(requestParam);
    	return JsonResultUtil.buildSuccess("添加功能菜单成功");
    }

    
    //修改功能菜单
    @RequestMapping(value = "/updateFunctionMenu", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateFunctionMenu(@RequestBody Map<String,Object> requestParam) throws Exception{
    	if (CommonUtil.isNullOrEmpty(requestParam.get(Dict.FUNCID))) {
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {Dict.FUNCID});
		}
    	if (CommonUtil.isNullOrEmpty(requestParam.get(Dict.FUNCNAME))) {
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {Dict.FUNCNAME});
		}
    	Integer funcId = (Integer)requestParam.get(Dict.FUNCID);
    	String funcName = (String)requestParam.get(Dict.FUNCNAME);
    	FunctionMenu query = functionMenuService.queryMenuDetailByFuncId(funcId);
    	if (CommonUtil.isNullOrEmpty(query)) {
    		throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"查询数据异常!"});
		}
    	FunctionMenu functionMenu = new FunctionMenu();
    	functionMenu.setFunc_model_name(funcName);
    	functionMenu.setLevel(query.getLevel());
    	functionMenu.setParent_id(query.getParent_id());
    	functionMenu.setSys_func_id(query.getSys_func_id());
    	FunctionMenu queryFuncDetail = functionMenuService.queryFuncDetail(functionMenu);
    	if (!CommonUtil.isNullOrEmpty(queryFuncDetail)) {
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR, new String[] {"该功能菜单名称已存在,请更换!"});
		}
    	try {
    		functionMenuService.update(funcId, funcName);
		} catch (Exception e) {
			logger.error("修改功能菜单失败: ", e);
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"修改功能菜单失败"});
		}
        return JsonResultUtil.buildSuccess("修改功能菜单成功");
    }

    
    //根据系统id查询功能菜单列表
    @RequestMapping("/queryFuncMenuBySysId")
    @ResponseBody
    public JsonResult queryFuncMenuBySysId(@RequestBody Map<String,Object> requestParam) throws Exception{
    	if(CommonUtil.isNullOrEmpty(requestParam.get(Dict.SYS_ID))) {
    		logger.error("系统不能为空");
    		return JsonResultUtil.buildSuccess();
    	}
    	Integer sysId = (Integer)requestParam.get(Dict.SYS_ID);
        List<Map<String, Object>> list = functionMenuService.queryMenuBysysId(sysId);
        return JsonResultUtil.buildSuccess(list);
    }
       
        
    //根据系统id和菜单级别查询功能菜单列表,联动查询
    @RequestMapping(value = "/queryMenuBySysIdAndLever", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryMenuBySysIdAndLever(@RequestBody Map<String,Object> requestParam) throws Exception{
    	List<FunctionMenu> menuList = functionMenuService.queryMenuBySysIdAndLever(requestParam);
        return JsonResultUtil.buildSuccess(menuList);
    }
    
    
    //根据功能菜单id查询菜单详情和所属系统详情,返回map
    @RequestMapping(value = "/queryMenuAndSystemByFuncId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryMenuAndSystemByFuncId(@RequestBody Map<String,Object> requestParam) throws Exception{
    	if((Integer)requestParam.get(Dict.FUNCID) < 0) {
    		throw new FtmsException(ErrorConstants.PARAM_ERROR,new String[] {Dict.FUNCID,"非法参数值"});
    	}
    	Integer funcId = (Integer)requestParam.get(Dict.FUNCID);
    	FunctionMenu functionMenu = functionMenuService.queryMenuDetailByFuncId(funcId);
    	if(CommonUtil.isNullOrEmpty(functionMenu)) {
    		throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"此功能菜单不存在"});
    	} 
    	SystemModel systemModel = systemModelService.query(functionMenu.getSys_func_id());
    	if(CommonUtil.isNullOrEmpty(systemModel)) {
    		throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"此系统不存在"});
    	}
    	Map<String,Object> map = new HashMap<>();
    	map.put("functionMenu", functionMenu);
    	map.put("systemModel", systemModel);
        return JsonResultUtil.buildSuccess(map);
    }
    
    
    //修改存量数据功能菜单
    //@RequestMapping(value = "/updateData", method = RequestMethod.POST)
    //@ResponseBody
    public JsonResult updateData() throws Exception{
    	//查询全部一级菜单
    	List<FunctionMenu> lists = functionMenuService.queryAll();
    	for (FunctionMenu functionMenu : lists) {
			try {
				//循环修改菜单
				functionMenuService.updateData(functionMenu.getFunc_id(), functionMenu.getFunc_model_name(),
						"0,"+functionMenu.getFunc_id(), functionMenu.getFunc_model_name(), functionMenu.getSys_func_id());
			} catch (Exception e) {
				logger.error("修改功能菜单失败: ", e);
				throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"修改功能菜单失败"});
			} 
		}
        return JsonResultUtil.buildSuccess();
    }


	//删除菜单功能
	@RequestMapping(value = "/delFunctionMenu", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult delFunctionMenu(@RequestBody Map<String,Object> requestParam) throws Exception{
		if (CommonUtil.isNullOrEmpty(requestParam.get(Dict.FUNCID))){
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_BOTH_EMPTY,new String[]{"funcId为空!"});
		}
		Integer func_id = Integer.valueOf((String) requestParam.get(Dict.FUNCID));
		this.functionMenuService.delete(func_id);
		return JsonResultUtil.buildSuccess();
	}


}

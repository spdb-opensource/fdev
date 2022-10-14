package com.spdb.fdev.fdevinterface.spdb.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceStatisticsService;

/**
 * fdev接口统计相关Controller，内部使用
 * 
 * @author xxx
 *
 */
@RequestMapping(value = "/api/interface")
@RestController
public class InterfaceStatisticsController {
	@Autowired
	InterfaceStatisticsService InterfaceStatisticsService;
	
	/**
	 * 初始化接口统计数据（仅上线调用一次）
	 * @param params
	 * @return
	 */
	@PostMapping(value="/initList")
	public JsonResult initList(@RequestBody Map params){
		InterfaceStatisticsService.initList();
		return JsonResultUtil.buildSuccess();
	}

	/**
	 * 接口统计查询
	 * 
	 * @param params
	 * @return
	 */
	@PostMapping(value = "/interfaceStatistics")
	public JsonResult interfaceStatistics(@RequestBody Map params) {
		if (StringUtils.isEmpty(params.get(Dict.NAME)) && StringUtils.isEmpty(params.get("targetServiceName")) && StringUtils.isEmpty(params.get("sourceServiceName"))) {
			throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
		}
		return JsonResultUtil.buildSuccess(InterfaceStatisticsService.queryInterfaceStatistics(params));
	}

}

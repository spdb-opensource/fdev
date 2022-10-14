package com.test.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.test.dict.Dict;
import com.test.service.TestCaseService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.utils.MyUtils;


@RequestMapping("/testcase")
@RestController
public class TestCaseController {
	private static final Logger logger = LoggerFactory.getLogger(TestCaseController.class);
	@Autowired
	private TestCaseService testCaseService;

	@RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
	@PostMapping(value = "/countUserTestCaseByTime")
	public JsonResult countAddTestCaseByTime(@RequestBody Map<String,Object> requestMap)throws Exception {
		String startDate = (String)requestMap.get(Dict.STARTDATE);
		String endDate = (String)requestMap.get(Dict.ENDDATE);
		String groupId = (String)requestMap.get(Dict.GROUPID);
		String username = (String)requestMap.get(Dict.USERNAME);
		boolean isParent = (boolean)requestMap.get(Dict.ISPARENT);
		List<Map> list = new ArrayList<>();
		list = testCaseService.countUserTestCaseByTime(groupId,startDate, endDate, username, isParent);
		return JsonResultUtil.buildSuccess(list);
	}
	
	@PostMapping(value = "/exportExcelUser")
	public void exportExcelUser(@RequestBody Map<String,Object> requestMap,HttpServletResponse resp)throws Exception {
		String startDate = (String)requestMap.get(Dict.STARTDATE);
		String endDate = (String)requestMap.get(Dict.ENDDATE);
		String groupId = (String)requestMap.get(Dict.GROUPID);
		String username = (String)requestMap.get(Dict.USERNAME);
		boolean isParent = (boolean)requestMap.get(Dict.ISPARENT);
		testCaseService.exportExcelUser(startDate, endDate, groupId, username, isParent, resp);
	}
}

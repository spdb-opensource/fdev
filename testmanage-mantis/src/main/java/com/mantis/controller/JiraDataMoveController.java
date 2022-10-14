package com.mantis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mantis.service.JiraDataMoveService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;

@RestController
@RequestMapping("/jira")
public class JiraDataMoveController {
	@Autowired
	JiraDataMoveService jiraDataMoveService;
	
	@PostMapping(value = "/jiraDateMove")
	public JsonResult jiraDateMove() throws Exception {
		jiraDataMoveService.jiraDateMove();
		return JsonResultUtil.buildSuccess(null);
	}	
}

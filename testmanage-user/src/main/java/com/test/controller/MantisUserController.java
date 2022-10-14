package com.test.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.service.MantisUserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;

@RequestMapping("/mantis")
@RestController
public class MantisUserController {
	@Autowired
	private MantisUserService mantisUserService;

	@PostMapping(value = "/branchAddMantisUser")
	public JsonResult branchAddMantisUser(@RequestBody Map<String, String> requestMap) throws Exception {
		mantisUserService.branchAddMantisUser();
		return JsonResultUtil.buildSuccess(null);
	}

}

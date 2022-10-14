package com.mantis.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mantis.dict.Dict;
import com.mantis.entity.MantisIssue;
import com.mantis.service.MantisFileService;
import com.mantis.service.MantisService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/file")
public class MantisFileController {
	@Autowired
	private MantisFileService mantisFileService;
	
	@PostMapping(value = "/addFile")
	public JsonResult addFile(@RequestBody Map<String,Object> map) throws Exception {
		mantisFileService.addFile(map);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@PostMapping(value = "/deleteFile")
	public JsonResult deleteFile(@RequestBody Map<String,String> map) throws Exception {
		mantisFileService.deleteFile(map);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@PostMapping(value = "/queryIssueFiles")
	public JsonResult queryIssueFiles(@RequestBody Map<String,String> map) throws Exception {
		String id = map.get(Dict.ID);
		List<Map<String,Object>> list = mantisFileService.queryIssueFiles(id);
		return JsonResultUtil.buildSuccess(list);
	}

}

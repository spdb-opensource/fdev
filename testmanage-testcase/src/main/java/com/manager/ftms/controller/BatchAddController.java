package com.manager.ftms.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manager.ftms.service.BatchAddService;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;

@RestController
@RequestMapping("/batch")
public class BatchAddController {
	@Autowired
	private BatchAddService batchAddService;

	@PostMapping("/downloadTemplate")
	public void downloadTemplate(HttpServletResponse resp) throws Exception {
		batchAddService.downloadTemplate(resp);
	}

	@PostMapping(value = "/batchAdd",consumes = { "multipart/form-data" })
	public JsonResult batchAdd(@RequestParam(value=Dict.PLANID,required = false)String planId,@RequestParam(value=Dict.WORKNO,required = false)String workNo,
							   @RequestParam(value = Dict.FILE,required = false)MultipartFile file,HttpServletResponse resp) throws Exception {
		if(Utils.isEmpty(file)) {
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"文件"});
		}
		if(Utils.isEmpty(planId)) {
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"执行计划"});
		}
		if(Utils.isEmpty(workNo)) {
			throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"工单编号"});
		}
		batchAddService.batchAdd(planId,workNo,file,resp);
		return JsonResultUtil.buildSuccess(null);
	}

	@PostMapping(value = "/supplementTestcaseExeRecord")
	public void supplementTestcaseExeRecord() throws Exception{
		batchAddService.supplementTestcaseExeRecord();
	}

}

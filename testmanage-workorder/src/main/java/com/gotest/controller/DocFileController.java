package com.gotest.controller;

import com.gotest.service.IDocService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/doc")
public class DocFileController {
    @Autowired
    private IDocService docService;

    /**
     * 上传文件
     * @throws Exception
     */
    @RequestMapping(value = "/uploadDocFile", method = RequestMethod.POST)
    public JsonResult uploadDocFile(@RequestParam MultipartFile file, @RequestParam String fileType, @RequestParam String workNo) throws Exception {
        return JsonResultUtil.buildSuccess(docService.uploadDocFile(file,fileType, workNo));
    }

}

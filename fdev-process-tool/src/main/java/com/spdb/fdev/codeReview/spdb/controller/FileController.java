package com.spdb.fdev.codeReview.spdb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.spdb.entity.CodeFile;
import com.spdb.fdev.codeReview.spdb.service.IFileService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@CrossOrigin
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    IFileService fileService;

    @PostMapping("/upload")
    public JsonResult upload(@RequestParam(value = Dict.FILES) MultipartFile[] files,
                             @RequestParam(value = Dict.ORDERID) String orderId,
                             @RequestParam(value = Dict.FILETYPE)  String fileType) throws Exception {
        Integer orderStatus = null;
        User user = CommonUtils.getSessionUser();
        fileService.uploadFile(orderId,orderStatus, fileType, files, user);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/delete")
    @NoNull(require = {Dict.ID},parameter = CodeFile.class)
    public JsonResult delete(@RequestBody CodeFile codeFile) throws Exception {
        fileService.delete(codeFile.getId());
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryFiles")
    @NoNull(require = {Dict.ORDERID},parameter = CodeFile.class)
    public JsonResult queryFiles(@RequestBody CodeFile codeFile) throws Exception {
        return JsonResultUtil.buildSuccess(fileService.queryFiles(codeFile.getOrderId()));
    }

    @PostMapping("/downloadAll")
    @NoNull(require = {Dict.ORDERID},parameter = CodeFile.class)
    public void downloadAll(@RequestBody CodeFile codeFile, HttpServletResponse response) throws Exception {
        fileService.downloadAll(codeFile.getOrderId(),response);
    }
}

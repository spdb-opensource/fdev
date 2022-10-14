package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.service.IDocService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;

import java.util.*;

@RestController
@RequestMapping("/api/doc")
public class DocController {

    @Autowired
    private IDocService docService;

    /**
     * 根据需求id + 需求文档类型(可选) 可分页查询需求文档列表,index是页码，size是每页条数。size为0时表示不分页
     * pangxy1
     *
     * @param param
     * @return
     */
    @PostMapping("/queryDemandDoc")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult queryDemandDoc(@RequestBody Map<String, Object> param) {
        return JsonResultUtil.buildSuccess(docService.queryDemandDocPagination(param));

    }


    //上传和更新需求
    @RequestMapping(value = "/updateDemandDoc", method = RequestMethod.POST)
    public JsonResult updateDemandDoc(@RequestParam(value = "files") MultipartFile[] files,
                                      @RequestParam(value = "demand_id") String demand_id,
                                      @RequestParam(value = "doc_type") String doc_type, String doc_link,
                                      @RequestParam(value = "user_group_id") String user_group_id,
                                      @RequestParam(value = "user_group_cn") String user_group_cn,
                                      @RequestParam(value = "demand_kind",required = false) String demand_kind

    ) throws Exception {
        for (MultipartFile multipartFile : files) {
            if (multipartFile.getSize() >= (20 * 1024 * 1024)) {
                throw new FdevException(ErrorConstants.FILE_INFO_SIZE_ERROR);
            }
        }
        docService.uploadFile(demand_id, doc_type, doc_link, user_group_id, user_group_cn, files, demand_kind);
        return JsonResultUtil.buildSuccess();
    }


    //删除需求文档
    @RequestMapping(value = "/deleteDemandDoc", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    public JsonResult deleteTaskDoc(@RequestBody Map params) throws Exception {
        docService.deleteDemandDoc(params);
        return JsonResultUtil.buildSuccess();
    }
}

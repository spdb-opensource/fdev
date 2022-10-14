package com.spdb.fdev.fdevtask.spdb.web;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.IDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RefreshScope
@RequestMapping("/api/task/doc")
public class DocController {
    @Autowired
    private IDocService docService;

    @RequestValidate(NotEmptyFields = Dict.ID)
    @RequestMapping(value = "/testReportCreate", method = RequestMethod.POST)
    public JsonResult testReportCreate(@RequestBody Map request) throws Exception {
        this.docService.testReportCreate(request);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/batchUpdateTaskDoc", method = RequestMethod.POST)
    public JsonResult batchUpdateTaskDoc(@RequestBody Map params) throws Exception {
        String modelName = Optional.ofNullable((String) params.get("modelName")).orElse("fdev-task");
        String path = Optional.ofNullable((String) params.get("path")).orElse("");
        return JsonResultUtil.buildSuccess(docService.batchUpdateTaskDoc(modelName, path));
    }

    @RequestMapping(value = "/updateTaskDoc", method = RequestMethod.POST)
    public JsonResult updateTaskDoc(@RequestParam(value = "files") MultipartFile[] files,
                                    @RequestParam(value = "taskId") String taskId,
                                    @RequestParam(value = "datas") String datas
    ) throws Exception {
        List<Map<String, String>> doc = (List) JSONArray.parseArray(datas);
        return JsonResultUtil.buildSuccess(docService.uploadFile(taskId, doc, files));
    }

    @RequestMapping(value = "/deleteTaskDoc", method = RequestMethod.POST)
    public JsonResult deleteTaskDoc(@RequestBody Map params) throws Exception {
        String path = (String) params.get("path");
        String taskId = (String) params.get("taskId");
        return JsonResultUtil.buildSuccess(docService.deleteTaskDoc(taskId, path));
    }

    /**
     * 上传文件
     * @throws Exception
     */
    @RequestMapping(value = "/uploadDesignDoc", method = RequestMethod.POST)
    public JsonResult uploadDesignDoc(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value = "fileName") String fileName,
                                      @RequestParam(value = "taskId") String taskId,
                                      @RequestParam(value = "fileType") String fileType,
                                      @RequestParam(value = "uploadStage") String uploadStage,
                                      @RequestParam(value = "remark") String remark
    ) throws Exception {
        return JsonResultUtil.buildSuccess(docService.uploadDesignFile(file,fileName,taskId,fileType,uploadStage,remark));
    }
    /**
     * 删除文件
     * @throws Exception
     */
    @RequestMapping(value = "/deleteDesignDoc", method = RequestMethod.POST)
    public JsonResult deleteDesignDoc(@RequestBody Map params) throws Exception {
        String path = (String) params.get("path");
        String taskId = (String) params.get("taskId");
        return JsonResultUtil.buildSuccess(docService.deleteTaskDoc(taskId, path));
    }

    @RequestMapping(value = "/batchUpdateDesignDoc", method = RequestMethod.POST)
    public JsonResult batchUpdateDesignDoc(@RequestBody Map params) throws Exception {
        String modelName = Optional.ofNullable((String) params.get("modelName")).orElse("fdev-design");
        return JsonResultUtil.buildSuccess(docService.batchUpdateTaskDoc(modelName));
    }

    /**
     * 上传文件
     * @throws Exception
     */
    @PostMapping("/uploadSecurityTestDoc")
    public JsonResult uploadSecurityTestDoc(@RequestParam MultipartFile file,@RequestParam String fileType,
                                            @RequestParam String taskId) throws Exception {
        return JsonResultUtil.buildSuccess(docService.uploadSecurityTestDoc(file, fileType, taskId));
    }

    /**
     * 下载模板文件
     * @param resp
     * @param params
     * @throws Exception
     */
    @PostMapping("/downloadTemplateFile")
    public void downloadTemplateFile(HttpServletResponse resp,@RequestBody Map params) throws Exception {
        docService.downloadTemplateFile(resp, (String) params.get(Dict.FILETYPE));
    }
}

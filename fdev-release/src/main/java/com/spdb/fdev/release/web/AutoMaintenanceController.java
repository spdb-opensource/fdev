package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.entity.TemplateDocument;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequestMapping("/api/autoManage")
@RestController
@RefreshScope
public class AutoMaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(AutoMaintenanceController.class);

    @Value("${excel.template.minio.url}")
    private String excelTemplateUrl;

    @Autowired
    private IReleaseNodeService releaseNodeService;
    @Autowired
    private IReleaseTaskService releaseTaskService;
    @Autowired
    private IReleaseRqrmntService releaseRqrmntService;
    @Autowired
    private ITaskService taskService;
    @Autowired
    private IFileService fileService;
    @Autowired
    private ITemplateDocumentService templateDocumentService;

    @RequestValidate(NotEmptyFields = {Dict.DATE})
    @PostMapping(value = "/batchTaskFile")
    public JsonResult batchTaskFile(@RequestBody @ApiParam Map<String, String> requestParam) {
        String date = requestParam.get(Dict.DATE);
        try {
            List<ReleaseNode> list = releaseNodeService.queryFromDate(date);
            for(ReleaseNode releaseNode : list) {
                List<ReleaseTask> tasks = releaseTaskService.queryByReleaseNodeName(releaseNode.getRelease_node_name());
                for(ReleaseTask task : tasks) {
                    logger.info("投产窗口名称：{},任务id：{}", releaseNode.getRelease_node_name(), task.getTask_id());
                    releaseRqrmntService.addOrEditRqrmntTask(task.getTask_id(), releaseNode.getRelease_node_name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/uploadExcel", consumes = {"multipart/form-data"})
    public JsonResult uploadExcel(@RequestParam(Dict.SYSNAME_CN) String sysname_cn,
                                  @RequestParam(value = Dict.TEMPLATE_TYPE, required = false) String template_type,
                                  @RequestParam(Dict.FILES) MultipartFile[] files) throws Exception {
        String dir = excelTemplateUrl + sysname_cn + "/";
        if(!CommonUtils.isNullOrEmpty(template_type)) {
            String template_type_name = Dict.GRAY.equals(template_type) ? "灰度" : "生产";
            dir = dir + template_type_name + "/";
        }
        Set<String> pathSet = new HashSet<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String path = dir + fileName;
            fileService.uploadFiles(path, file, fileName, "fdev-release");
            pathSet.add(path);
        }
        TemplateDocument templateDocument = templateDocumentService.getDocument(sysname_cn, template_type);
        if(CommonUtils.isNullOrEmpty(templateDocument)){
            templateDocument = new TemplateDocument();
            ObjectId objectId = new ObjectId();
            templateDocument.set_id(objectId);
            templateDocument.setId(objectId.toString());
            templateDocument.setSysname_cn(sysname_cn);
            if(!CommonUtils.isNullOrEmpty(template_type)) {
                templateDocument.setTemplate_type(template_type);
            }
            templateDocument.setDocument_list(new ArrayList<>(pathSet));
            templateDocumentService.save(templateDocument);
        } else {
            List<String> documentList = templateDocument.getDocument_list();
            pathSet.addAll(documentList);
            templateDocument.setDocument_list(new ArrayList<>(pathSet));
            templateDocumentService.editDocumentList(templateDocument);
        }
        return JsonResultUtil.buildSuccess(templateDocument);
    }

}

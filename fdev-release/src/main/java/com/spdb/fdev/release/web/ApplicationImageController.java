package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ApplicationImage;
import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IApplicationImageService;
import com.spdb.fdev.release.service.IProdApplicationService;
import com.spdb.fdev.release.service.IProdRecordService;
import com.spdb.fdev.release.service.IReleaseApplicationService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api/applicationImage")
@RestController
public class ApplicationImageController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationImageController.class);

    @Autowired
    IApplicationImageService applicationImageService;
    @Autowired
    IProdRecordService prodRecordService;
    @Autowired
    private IReleaseApplicationService releaseApplicationService;
    @Autowired
    IProdApplicationService prodApplicationService;

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.PROD_ID, Dict.APPLICATION_ID})
    @PostMapping(value = "/queryPushImageUri")
    public JsonResult queryPushImageUri(@RequestBody @ApiParam Map<String, String> requestParam) {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String prod_id = requestParam.get(Dict.PROD_ID);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        List<ApplicationImage> list = applicationImageService.queryPushImageUri(release_node_name, prod_id, application_id);
        return JsonResultUtil.buildSuccess(list);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STATUS, Dict.PUSH_IMAGE_LOG})
    @PostMapping(value = "/updateStatusById")
    public JsonResult updateStatusById(@RequestBody @ApiParam Map<String, String> requestParam) {
        String id = requestParam.get(Dict.ID);
        String status = requestParam.get(Dict.STATUS);
        String push_image_log = requestParam.get(Dict.PUSH_IMAGE_LOG);
        String deploy_type = requestParam.get(Dict.DEPLOY_TYPE);
        applicationImageService.updateStatusLogById(id, status, push_image_log,deploy_type);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @PostMapping(value = "/checkPushImageAndAutoRelease")
    public JsonResult checkPushImageAndAutoRelease(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        Map<String, List<ApplicationImage>> applicationMap = applicationImageService.queryByProdId(prod_id);
        logger.info("变更id：{}", prod_id);
        logger.info("变更状态：{}，介质准备阶段：{}", prodRecord.getStatus(), prodRecord.getAuto_release_stage());
        logger.info("是否符合处理状态：{}", "1".equals(prodRecord.getStatus()) && "6".equals(prodRecord.getAuto_release_stage()));
        logger.info("查询是否有失败：{}", !CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR)));
        logger.info("总数是否为空：{}", CommonUtils.isNullOrEmpty(applicationMap.get(Dict.TOTAL)));
        logger.info("其他是否为空：{}", CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSHING))
                && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.JUST_START))
                && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR)));
        logger.info("是否符合修改正确状态：{}", CommonUtils.isNullOrEmpty(applicationMap.get(Dict.TOTAL))
                || (CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSHING))
                && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.JUST_START))
                && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR))));
        if("1".equals(prodRecord.getStatus()) && "6".equals(prodRecord.getAuto_release_stage())) {
            if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR))) {
                prodRecordService.updateProdStatus(prod_id, "4");
            } else if(CommonUtils.isNullOrEmpty(applicationMap.get(Dict.TOTAL))
                    || (CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSHING))
                    && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.JUST_START))
                    && CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR)))) {
                // 为使前端更新日志，此处延时5秒更改状态
                Thread.sleep(5000);
                prodRecordService.updateAutoReleaseStage(prod_id, "7");
                prodRecordService.updateProdStatus(prod_id, "3");
            }
        }

        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.STATUS})
    @PostMapping(value = "/updateByProdApplication")
    public JsonResult updateByProdApplication(@RequestBody @ApiParam Map<String, String> requestParam) {
        String prod_id = requestParam.get(Dict.PROD_ID);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String status = requestParam.get(Dict.STATUS);
        String push_image_log = requestParam.get(Dict.PUSH_IMAGE_LOG);
        String imageUri = requestParam.get(Dict.IMAGE_URI);
        applicationImageService.updateByProdApplication(prod_id, application_id, status, push_image_log,imageUri);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ASSETS_VERSION})
    @PostMapping(value = "/queryImagelistByProdAssetVersion")
    public JsonResult queryImagelistByProdAssetVersion(@RequestBody @ApiParam Map<String, String> requestParam) {
        String prod_assets_version = requestParam.get(Dict.PROD_ASSETS_VERSION);
        List<String> list = applicationImageService.queryImagelistByProdAssetVersion(prod_assets_version);
        return JsonResultUtil.buildSuccess(list);
    }

}

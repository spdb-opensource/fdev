package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.SystemReleaseInfo;
import com.spdb.fdev.release.service.IGitlabService;
import com.spdb.fdev.release.service.IGroupAbbrService;
import com.spdb.fdev.release.service.IProdRecordService;
import com.spdb.fdev.release.service.ISystemReleaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "变更参数管理")
@RequestMapping("/api/release")
@RestController
@RefreshScope
public class SystemReleaseInfoController {

    @Autowired
    IGitlabService gitlabService;

    private static Logger logger = LoggerFactory.getLogger(SystemReleaseInfoController.class);

    @Value("${gitlab.rootUrl}")
    private String gitRootUrl;

    @Autowired
    private ISystemReleaseInfoService systemReleaseInfoService;

    @Autowired
    private IGroupAbbrService groupAbbrService;

    @Autowired
    private IProdRecordService prodRecordService;

    @RequestValidate(NotEmptyFields = {Dict.SYSTEM_ID})
    @ApiOperation(value = "系统投产信息查询")
    @PostMapping(value = "/querySysRlsDetail")
    public JsonResult querySystemDetail(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String system_id = requestParam.get("system_id");
        SystemReleaseInfo result = systemReleaseInfoService.querySysRlsDetail(system_id);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "各系统投产信息查询")
    @PostMapping(value = "/querySysRlsInfo")
    public JsonResult querySystemInfo() throws Exception {
        List<Map<String, String>> list = systemReleaseInfoService.querySysRlsInfo();
        return JsonResultUtil.buildSuccess(list);
    }

    @RequestValidate(NotEmptyFields = {Dict.SYSTEM_ID})
    @PostMapping(value = "/updateSysRlsInfo")
    public JsonResult updateSysRlsInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String system_id = (String) requestParam.get(Dict.SYSTEM_ID);
        List<String> resource_giturl = (List<String>) requestParam.get(Dict.RESOURCE_GITURL);
        String resource_giturls = "";
        Set<String> checkRepeatUrl = new HashSet<>();
        checkRepeatUrl.addAll(resource_giturl);
        if (checkRepeatUrl.size() < resource_giturl.size()) {
            throw new FdevException(ErrorConstants.RESOURCE_GIT_URL_REPEAT);
        }
        if (!CommonUtils.isNullOrEmpty(resource_giturl)) {
            for (String giturl : resource_giturl) {
                try {
                    gitlabService.queryProjectIdByUrl(giturl);
                } catch (Exception e) {
                    throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
                }
            }
            for (int i = 0 ; i < resource_giturl.size(); i++) {
                String value =resource_giturl.get(i);
                if (!value.contains(gitRootUrl)) {
                    resource_giturl.set(i,gitRootUrl + value);
                }
            }
            resource_giturls = resource_giturl.stream().reduce((a, b) -> a + ";" + b).orElse("");
        }
        try {
            SystemReleaseInfo systemReleaseInfo = systemReleaseInfoService.updateSysRlsInfo(system_id, resource_giturls);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RESOURCE_GIT_URL_NOT_EXIST);
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @ApiOperation(value = "系统投产信息查询")
    @PostMapping(value = "/querySystemDetailByProdId")
    public JsonResult querySystemDetailByProdId(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        if(CommonUtils.isNullOrEmpty(prodRecord)){
            logger.error("prodRecord does not exist");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"投产变更不存在！"});
        }
        SystemReleaseInfo result = systemReleaseInfoService.querySysRlsDetail(prodRecord.getOwner_system());
        return JsonResultUtil.buildSuccess(result);
    }

}

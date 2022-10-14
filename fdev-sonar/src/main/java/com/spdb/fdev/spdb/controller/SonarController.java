package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.service.IAppService;
import com.spdb.fdev.spdb.service.ISonarService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "sonar scan")
@RequestMapping("/api/sonar")
@RestController
@RefreshScope
public class SonarController {

    @Value("${sonar.not.scan.id}")
    private String noSonarId;

    @Autowired
    ISonarService sonarService;
    @Autowired
    IAppService appService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "项目分析", notes = "扫描日期及扫描的版本")
    @RequestMapping("/projectAnalyses")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID})
    public JsonResult projectAnalyses(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.GITLABID);
        Map<String, Object> app = appService.queryByGitId(Integer.valueOf(id));
        return JsonResultUtil.buildSuccess(this.sonarService.projectAnalyses(app, null));
    }

    @ApiOperation(value = "Sonar 扫描 排行榜")
    @RequestMapping("/searchProject")
    public JsonResult searchProject() throws Exception {
        return JsonResultUtil.buildSuccess(this.sonarService.searchProject());
    }

    @ApiOperation(value = "searchTotalProjectMeasures")
    @RequestMapping("/searchTotalProjectMeasures")
    public JsonResult searchTotalProjectMeasures(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(this.sonarService.searchTotalProjectMeasures(requestMap));
    }

    @ApiOperation(value = "获取项目扫描详情", notes = "项目总代码行及各类型语言涉及的代码行，以及每个数据下对应sonarqube的链接地址")
    @RequestMapping("/getProjectInfo")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID})
    public JsonResult getProjectInfo(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.GITLABID);
        Map<String, Object> app = appService.queryByGitId(Integer.valueOf(id));
        return JsonResultUtil.buildSuccess(this.sonarService.getProjectInfo(app, null));
    }

    @ApiOperation(value = "获取项目扫描历史记录", notes = "bugs，漏洞，异味三者的增长趋势图")
    @RequestMapping("/getAnalysesHistory")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID})
    public JsonResult getAnalysesHistory(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.GITLABID);
        Map<String, Object> app = appService.queryByGitId(Integer.valueOf(id));
        return JsonResultUtil.buildSuccess(this.sonarService.getAnalysesHistory(app, null));
    }

    @ApiOperation(value = "获取项目代码树", notes = "代码问题表格统计及链接")
    @RequestMapping("/componentTree")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID})
    public JsonResult componentTree(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.GITLABID);
        Map<String, Object> app = appService.queryByGitId(Integer.valueOf(id));
        return JsonResultUtil.buildSuccess(this.sonarService.componentTree(app, null));
    }

    @ApiOperation(value = "扫描开发分支")
    @RequestMapping("/scanningFeatureBranch")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME})
    public JsonResult scanningFeatureBranch(@RequestBody Map<String, String> requestMap) throws Exception {
        String id = requestMap.get(Dict.ID);
        String branch_name = requestMap.get(Dict.BRANCH_NAME);
        Map app = appService.findById(id);
        return JsonResultUtil.buildSuccess(this.sonarService.scanningFeatureBranch(app, branch_name));
    }

    @ApiOperation(value = "开发分支项目分析", notes = "扫描日期及扫描的版本")
    @RequestMapping("/featureProjectAnalyses")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME})
    public JsonResult featureProjectAnalyses(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.ID);
        String branch_name = (String) requestMap.get(Dict.BRANCH_NAME);
        Map<String, Object> app = appService.findById(id);
        return JsonResultUtil.buildSuccess(this.sonarService.projectAnalyses(app, branch_name));
    }

    @ApiOperation(value = "获取项目开发分支扫描详情", notes = "项目总代码行及各类型语言涉及的代码行，以及每个数据下对应sonarqube的链接地址")
    @RequestMapping("/getProjectFeatureInfo")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME})
    public JsonResult getProjectFeatureInfo(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.ID);
        Map<String, Object> app = appService.findById(id);
        String branch_name = (String) requestMap.get(Dict.BRANCH_NAME);
        return JsonResultUtil.buildSuccess(this.sonarService.getProjectInfo(app, branch_name));
    }

    @ApiOperation(value = "获取项目开发分支代码树", notes = "代码问题表格统计及链接")
    @RequestMapping("/featureComponentTree")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME})
    public JsonResult featureComponentTree(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.ID);
        Map<String, Object> app = appService.findById(id);
        String branch_name = (String) requestMap.get(Dict.BRANCH_NAME);
        return JsonResultUtil.buildSuccess(this.sonarService.componentTree(app, branch_name));
    }

    @ApiOperation(value = "获取项目开发分支bug数量", notes = "代码数量")
    @RequestMapping("/getProjectBugs")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME, Dict.SONAR_ID})
    public JsonResult getProjectBugs(@RequestBody Map requestMap) throws Exception {
        String sonar_id = (String) requestMap.get(Dict.SONAR_ID);
        Map<String, Object> returnMap = new HashMap<>();
        // 特殊不扫描的代码
        if(noSonarId.equals(sonar_id)) {
            returnMap.put(Dict.STATUS, Constants.SUCCESS);
            returnMap.put(Dict.BUGS, "0");
            return JsonResultUtil.buildSuccess(returnMap);
        }
        String id = (String) requestMap.get(Dict.ID);
        Map<String, Object> app = appService.findById(id);
        String branch_name = (String) requestMap.get(Dict.BRANCH_NAME);
        String status = sonarService.getSonarStatus(sonar_id);
        if(!CommonUtil.isNullOrEmpty(status) && Constants.SUCCESS.equals(status)) {
            returnMap.put(Dict.STATUS, Constants.SUCCESS);
            returnMap.put(Dict.BUGS, ((Map<String, Object>)this.sonarService.getProjectInfo(app, branch_name).get(Dict.BUGS)).get(Dict.VALUE));
        } else if(Constants.PENDING.equals(status)) {
            returnMap.put(Dict.STATUS, Constants.PENDING);
        } else {
            returnMap.put(Dict.STATUS, Constants.RETRY);
        }
        return JsonResultUtil.buildSuccess(returnMap);
    }

    @ApiOperation(value = "获取项目开发分支扫描历史记录", notes = "bugs，漏洞，异味三者的增长趋势图")
    @RequestMapping("/getFeatureAnalysesHistory")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.BRANCH_NAME})
    public JsonResult getFeatureAnalysesHistory(@RequestBody Map requestMap) throws Exception {
        String id = (String) requestMap.get(Dict.ID);
        Map<String, Object> app = appService.findById(id);
        String branch_name = (String) requestMap.get(Dict.BRANCH_NAME);
        return JsonResultUtil.buildSuccess(this.sonarService.getAnalysesHistory(app, branch_name));
    }

}

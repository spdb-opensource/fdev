package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.service.DesignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * 设计还原审核相关接口
 */
@Api(tags = "需求接口")
@RequestMapping("/api/design")
@RestController
public class DesignController {

    @Resource
    private DesignService designService;

    @ApiOperation(value = "查询需求设计还原当前状态", notes = "设计还原相关")
    @PostMapping(value = "/getDesignInfo")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult getDesignInfo(@RequestBody Map params) throws Exception {
        String demandId = params.get(Dict.DEMAND_ID).toString();
        return JsonResultUtil.buildSuccess(designService.getDesignStateAndData(demandId));
    }


    @ApiOperation(value = "更新需求设计还原当前状态", notes = "设计还原相关")
    @PostMapping(value = "/updateDesignStage")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult updateDesignStage(@RequestBody Map params) throws Exception {
        String demandId = params.get(Dict.DEMAND_ID).toString();
        return JsonResultUtil.buildSuccess(designService.updateDesignState(demandId, params));
    }

    @ApiOperation(value = "更新审核意见", notes = "设计还原相关")
    @PostMapping(value = "/updateDesignRemark")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult updateDesignRemark(@RequestBody Map params) throws Exception {
        String demandId = params.get(Dict.DEMAND_ID).toString();
        String designRemark = Optional.
                ofNullable((String) params.get("designRemark")).orElse("");
        return JsonResultUtil.buildSuccess(designService.updateDesignRemark(demandId, designRemark));
    }

    /**
     * 上传设计还原文档
     *
     * @throws Exception
     */
    @RequestMapping(value = "/uploadDesignDoc", method = RequestMethod.POST)
    public JsonResult uploadDesignDoc(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value = "fileName") String fileName,
                                      @RequestParam(value = "demand_id") String demandId,
                                      @RequestParam(value = "fileType") String fileType,
                                      @RequestParam(value = "uploadStage") String uploadStage,
                                      @RequestParam(value = "remark") String remark
    ) throws Exception {

        return JsonResultUtil.buildSuccess(this.designService.uploadDesignFile(file, fileName, demandId, fileType, uploadStage, remark));
    }

    @RequestMapping(value = "/batchUpdateDesignDoc", method = RequestMethod.POST)
    public JsonResult batchUpdateDesignDoc(@RequestBody Map params) throws Exception {
        String modelName = Optional.ofNullable((String) params.get("modelName")).orElse("fdev-design");
        return JsonResultUtil.buildSuccess(designService.batchUpdateTaskDoc(modelName));
    }
    
    @ApiOperation("通过时间段、审核人、和组查询设计稿审核记录")
    @RequestMapping(value = "/queryReviewList")
    public JsonResult queryReviewList(@RequestBody Map params)throws Exception{
    	String reviewer = params.get("reviewer").toString();
    	String startDate = params.get("startDate").toString();
    	String endDate = params.get("endDate").toString();
    	String internetChildGroupId = params.get("internetChildGroupId").toString();
    	List<String> group= (List<String>) params.get("group");
    	return JsonResultUtil.buildSuccess(designService.queryReviewDetailList(reviewer,group,startDate,endDate,internetChildGroupId));
    	
    }

    @ApiOperation("下载设计还原审核记录")
    @PostMapping("/downLoadReviewList")
    public void downLoadReviewList(@RequestBody Map params, HttpServletResponse response) throws Exception {
        designService.downLoadReviewList(response,(String)params.get(Dict.REVIEWER),(List<String>)params.get(Dict.GROUP),
                (String)params.get(Dict.STARTDATE),(String)params.get(Dict.ENDDATE),
                (Map<String,String>)params.get(Dict.COLUMNMAP),(String)params.get(Dict.INTERNETCHILDGROUPID));
    }
}

package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.base.annotation.nonull.NoNull;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.dto.CommitStatisticsDto;
import com.spdb.fdev.freport.spdb.dto.DashboardDto;
import com.spdb.fdev.freport.spdb.dto.StatisticsDto;
import com.spdb.fdev.freport.spdb.entity.demand.DemandBaseInfo;
import com.spdb.fdev.freport.spdb.service.DevelopmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@Api(tags = "研发协作")
@RequestMapping(value = "/api/development")
public class DevelopmentController {

    private static Logger logger = LoggerFactory.getLogger(DevelopmentController.class);

    @Autowired
    private DevelopmentService developmentService;

    @ApiOperation(value = "新增需求变化趋势")
    @RequestMapping(value = "/queryDemandNewTrend", method = RequestMethod.POST)
    public JsonResult queryDemandNewTrend(@RequestBody DashboardDto dto) throws Exception {
        DemandBaseInfo demandBaseInfo = new DemandBaseInfo();
        BeanUtils.copyProperties(dto, demandBaseInfo);
        return JsonResultUtil.buildSuccess(this.developmentService.queryDemandNewTrend(dto.getCycle(), demandBaseInfo));
    }

    @ApiOperation(value = "投产需求变化趋势")
    @RequestMapping(value = "/queryDemandProTrend", method = RequestMethod.POST)
    public JsonResult queryDemandProTrend(@RequestBody DashboardDto dto) throws Exception {
        DemandBaseInfo demandBaseInfo = new DemandBaseInfo();
        BeanUtils.copyProperties(dto, demandBaseInfo);
        return JsonResultUtil.buildSuccess(this.developmentService.queryDemandProTrend(dto.getCycle(), demandBaseInfo));
    }

    @ApiOperation(value = "需求吞吐量变化趋势")
    @RequestMapping(value = "/queryDemandThroughputTrend", method = RequestMethod.POST)
    public JsonResult queryDemandThroughputTrend(@RequestBody DashboardDto dto) throws Exception {
        DemandBaseInfo demandBaseInfo = new DemandBaseInfo();
        BeanUtils.copyProperties(dto, demandBaseInfo);
        return JsonResultUtil.buildSuccess(this.developmentService.queryDemandThroughputTrend(dto.getCycle(), demandBaseInfo));
    }

    @ApiOperation(value = "任务变化趋势")
    @RequestMapping(value = "/queryTaskTrend", method = RequestMethod.POST)
    public JsonResult queryTaskTrend(@RequestBody DashboardDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryTaskTrend(dto.getCycle(), dto.getTaskStage()));
    }

    @ApiOperation(value = "新增应用变化趋势")
    @RequestMapping(value = "/queryAppNewTrend", method = RequestMethod.POST)
    public JsonResult queryAppNewTrend(@RequestBody DashboardDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryAppNewTrend(dto.getCycle()));
    }

    @ApiOperation(value = "需求吞吐量统计")
    @RequestMapping(value = "/queryDemandThroughputStatistics", method = RequestMethod.POST)
    @NoNull(require = {"groupIds"}, parameter = StatisticsDto.class)
    public JsonResult queryDemandThroughputStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryDemandThroughputStatistics(dto.getStartDate(), dto.getEndDate(), dto.getGroupIds(), dto.getShowDaily()));
    }

    @ApiOperation(value = "任务吞吐量统计")
    @RequestMapping(value = "/queryTaskThroughputStatistics", method = RequestMethod.POST)
//    @NoNull(require = {"groupIds", "demandType"}, parameter = StatisticsDto.class)
    public JsonResult queryTaskThroughputStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryTaskThroughputStatistics(dto.getStartDate(), dto.getEndDate(), dto.getGroupIds(), dto.getDemandType(), dto.getTaskType(), dto.getIncludeChild()));
    }

    @ApiOperation(value = "导出 - 任务吞吐量详情")
    @RequestMapping(value = "/exportTaskThroughputDetail", method = RequestMethod.POST)
    @NoNull(require = {"groupIds"}, parameter = StatisticsDto.class)
    public void exportTaskThroughputDetail(@RequestBody StatisticsDto dto, HttpServletResponse resp) throws Exception {
        XSSFWorkbook workbook = this.developmentService.exportTaskThroughputDetail(dto.getStartDate(), dto.getEndDate(), dto.getGroupIds(), dto.getDemandType(), dto.getTaskType(), dto.getIncludeChild());
        try {
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "taskDetail" + ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            logger.error("导出需求评估表时，数据读写错误" + e.getMessage());
        }
    }

    @ApiOperation(value = "各阶段任务数量统计")
    @RequestMapping(value = "/queryTaskPhaseStatistics", method = RequestMethod.POST)
    @NoNull(require = {"groupIds"}, parameter = StatisticsDto.class)
    public JsonResult queryTaskPhaseStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryTaskPhaseStatistics(dto.getGroupIds(), dto.getIncludeChild(), dto.getTaskPersonTypeForAvg()));
    }

    @ApiOperation(value = "指定时间内阶段发生变化的任务数量统计")
    @RequestMapping(value = "/queryTaskPhaseChangeStatistics", method = RequestMethod.POST)
    @NoNull(require = {"groupIds", "startDate", "endDate"}, parameter = StatisticsDto.class)
    public JsonResult queryTaskPhaseChangeStatistics(@RequestBody StatisticsDto dto) throws Exception {
        if (CommonUtils.isNullOrEmpty(dto.getDemandType())) dto.setDemandType(new ArrayList<>());
        if (CommonUtils.isNullOrEmpty(dto.getTaskType())) dto.setTaskType(new ArrayList<>());
        return JsonResultUtil.buildSuccess(this.developmentService.queryTaskPhaseChangeStatistics(dto.getStartDate(), dto.getEndDate(), dto.getGroupIds(), dto.getIncludeChild(), dto.getTaskPersonTypeForAvg(), dto.getDemandType(), dto.getTaskType()));
    }

    @ApiOperation(value = "获取项目下任务各阶段统计")
    @RequestMapping(value = "/queryAppTaskPhaseStatistics", method = RequestMethod.POST)
    @NoNull(require = {"appIds"}, parameter = StatisticsDto.class)
    public JsonResult queryAppTaskPhaseStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryAppTaskPhaseStatistics(dto.getAppIds()));
    }

    @ApiOperation(value = "需求统计 - 牵头")
    @RequestMapping(value = "/queryDemandStatistics", method = RequestMethod.POST)
    public JsonResult queryDemandStatistics(@RequestBody StatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryDemandStatistics(dto.getGroupIds(), dto.getPriority(), dto.getIncludeChild()));
    }

    @ApiOperation(value = "需求统计 - 参与")
    @RequestMapping(value = "/queryDemandByTaskStatistics", method = RequestMethod.POST)
    public JsonResult queryDemandByTaskStatistics(@RequestBody StatisticsDto dto) {
        // this.developmentService.queryDemandByTaskStatistics(dto.getGroupIds(), dto.getPriority(), dto.getIncludeChild());
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "代码统计")
    @RequestMapping(value = "/queryCommitStatistics", method = RequestMethod.POST)
    @NoNull(require = {"user"}, parameter = CommitStatisticsDto.class)
    public JsonResult queryCommitStatistics(@RequestBody CommitStatisticsDto dto) throws Exception {
        if (CommonUtils.isNullOrEmpty(dto.getStatisticRange())) dto.setStatisticRange(new ArrayList<>());
        return JsonResultUtil.buildSuccess(this.developmentService.queryCommitStatistics(dto));
    }

    @ApiOperation(value = "代码统计导出")
    @RequestMapping(value = "/exportCommitStatistics", method = RequestMethod.POST)
    @NoNull(require = {"user"}, parameter = CommitStatisticsDto.class)
    public void exportCommitStatistics(@RequestBody CommitStatisticsDto dto, HttpServletResponse resp) throws Exception {
        if (CommonUtils.isNullOrEmpty(dto.getStatisticRange())) dto.setStatisticRange(new ArrayList<>());
        developmentService.exportCommitStatistics(dto,resp);
    }

    @ApiOperation(value = "查询用户提交代码详情")
    @RequestMapping(value = "/queryCommitByUser", method = RequestMethod.POST)
    @NoNull(require = {"user"}, parameter = CommitStatisticsDto.class)
    public JsonResult queryCommitByUser(@RequestBody CommitStatisticsDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.developmentService.queryCommitByUser(dto));
    }

}

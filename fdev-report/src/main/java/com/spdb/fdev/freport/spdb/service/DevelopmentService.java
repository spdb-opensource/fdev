package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.dto.CommitStatisticsDto;
import com.spdb.fdev.freport.spdb.entity.demand.DemandBaseInfo;
import com.spdb.fdev.freport.spdb.entity.git.Commit;
import com.spdb.fdev.freport.spdb.vo.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface DevelopmentService {

    ProductVo queryDemandNewTrend(Integer cycle, DemandBaseInfo demand) throws Exception;

    ProductVo queryDemandProTrend(Integer cycle, DemandBaseInfo demand) throws Exception;

    ProductVo queryDemandThroughputTrend(Integer cycle, DemandBaseInfo demand) throws Exception;

    ProductVo queryTaskTrend(Integer cycle, String stage) throws Exception;

    ProductVo queryAppNewTrend(Integer cycle) throws Exception;

    List<List<Object>> queryDemandThroughputStatistics(String startTime, String endTime, List<String> groupIds, Boolean showDaily) throws Exception;

    Map<String, Object> queryTaskThroughputStatistics(String startTime, String endTime, List<String> groupIds, List<String> demandType, List<Integer> taskType, Boolean includeChild) throws Exception;

    XSSFWorkbook exportTaskThroughputDetail(String startTime, String endTime, List<String> groupIds, List<String> demandTypeList, List<Integer> taskTypeList, Boolean includeChild) throws Exception;

    List<TaskPhaseStatisticsVo> queryTaskPhaseStatistics(List<String> groupIds, Boolean includeChild, List<String> taskPersonTypeForAvg) throws Exception;

    List<TaskPhaseStatisticsVo> queryTaskPhaseChangeStatistics(String startTime, String endTime, List<String> groupIds, Boolean includeChild, List<String> taskPersonTypeForAv, List<String> demandType, List<Integer> taskType) throws Exception;

    List<TaskPhaseStatisticsVo> queryAppTaskPhaseStatistics(List<String> appIds) throws Exception;

    List<DemandStatisticsVo> queryDemandStatistics(List<String> groupIds, String priority, Boolean includeChild) throws Exception;

    PageVo<CommitStatisticsVo> queryCommitStatistics(CommitStatisticsDto dto) throws Exception;

    PageVo<Commit> queryCommitByUser(CommitStatisticsDto dto) throws Exception;

    void exportCommitStatistics(CommitStatisticsDto dto, HttpServletResponse resp) throws Exception;
}

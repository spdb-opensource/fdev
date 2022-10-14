package com.gotest.controller;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.OrderDimension;
import com.gotest.domain.WorkOrder;
import com.gotest.domain.WorkOrderStatus;
import com.gotest.service.*;
import com.gotest.utils.RedisUtil;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表接口 控制器层
 */
@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/statement")
public class ReportController {

    private Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private OrderDimensionService odService;

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private INotifyApi iNotifyApi;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 工单维度报表
     * 某时间段 案例总数 通过数 总缺陷数 未修复缺陷数 （工单状态也查）
     */
    @RequestMapping("/queryOrderDimension")
    public JsonResult queryOrderDimension(@RequestBody Map map) {
        List<OrderDimension> resultList = null;
        try {
            resultList = odService.queryOrderDimension(map);
        } catch (FtmsException e) {
            if (e.getCode().equals(ErrorConstants.SERVER_ERROR)) {
                logger.error(e.getMessage());
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"mantis 平台异常！"});
            }
            throw new FtmsException(ErrorConstants.SERVER_ERROR);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询数据异常！"});
        }
        return JsonResultUtil.buildSuccess(resultList);
    }

    /**
     * 导出工单维度报表
     * @param map
     * @return
     */
    @RequestMapping(value = "/exportOrderDimension")
    public void exportOrderDimension(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        odService.exportOrderDimension(map, resp);
    }

    /**
     * 导出 excel文件
     */
    @RequestMapping("/exportExcelReport")
    public void exportExcelReport(@RequestBody Map map, HttpServletResponse resp) {
        List<WorkOrderStatus> resultList;
        try {
            exportExcelService.generateProdExcel(map, resp);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"导出查询数据异常！"});
        }
    }

    /**
     * 折线图
     * @param map
     * @return
     */
    @RequestMapping("/queryDiscountChart")
    public JsonResult queryDiscountChart(@RequestBody Map map) {
        Map<String, Object> result = odService.queryDiscountChart(map);
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 饼状图 工单状态
     */
    @RequestMapping("/queryWorkOrderStatus")
    public JsonResult queryWorkOrderStatus(@RequestBody Map map) throws Exception{
        List<WorkOrderStatus> resultList;
            resultList = odService.queryWorkOrderStatus(map);
        return JsonResultUtil.buildSuccess(resultList);
    }

    /**
     * 工单sit报告 详情
     */
    @RequestMapping("/queryUpSitOrder")
    public JsonResult queryUpSitOrder(@RequestBody Map map) throws Exception{
        Integer currentPage = (Integer) map.get(Dict.CURRENTPAGE);
        Integer pageSize = (Integer) map.get(Dict.PAGESIZE);
        String done = String.valueOf(map.get(Dict.DONE));
        if (Util.isNullOrEmpty(currentPage) || Util.isNullOrEmpty(pageSize)) {
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"currentPage || pageSize"});
        }
        String sortManager = String.valueOf(map.getOrDefault(Dict.SORTMANAGER, Constants.FAIL_GET));
        String workOrderNo = "%" + ((String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET)).trim() + "%";
        String userEnName = ((String) map.getOrDefault(Dict.USERENNAME, Constants.FAIL_GET)).trim();
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        return JsonResultUtil.buildSuccess(odService.queryUpSitOrder(currentPage, pageSize, taskName,workOrderNo,userEnName,done,sortManager, orderType));
    }

    /**
     * 工单sit报告 总数--分页用
     */
    @RequestMapping("/queryUpSitOrderCount")
    public JsonResult queryUpSitOrderCount(@RequestBody Map map) {
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        String workOrderNo = "%" + ((String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET)).trim() + "%";
        String userEnName = ((String) map.getOrDefault(Dict.USERENNAME, Constants.FAIL_GET)).trim();
        String done = String.valueOf(map.get(Dict.DONE));
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        map.put(Dict.TOTAL, odService.queryUpSitOrderCount(taskName,workOrderNo,userEnName,done,orderType));
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 工单sit报告 导出数据
     */
    @RequestMapping("/exportSitReportData")
    public JsonResult exportSitReportData(@RequestBody Map map) throws Exception {
        String workNo = (String) map.get(Dict.WORKNO);
        String workManagerEn = (String) map.get(Dict.WORKMANAGEREN);
    	String groupLeadersEn = (String) map.get(Dict.GROUPLEADEREN);
    	String testersEn = (String) map.get(Dict.TESTERSEN);
        WorkOrder workOrder = workOrderService.queryWorkOrderByNo(workNo);
        if(Util.isNullOrEmpty(workOrder.getUatSubmitDate()) || workOrder.getUatSubmitDate() == 0){
            throw new FtmsException(ErrorConstants.SUPPLEMENT_UAT_DATE);
        }
        if (Util.isNullOrEmpty(workNo)) {
            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"workNo"});
        }
        map = odService.exportSitReportData(workNo, map);
        return JsonResultUtil.buildSuccess(map);
    }
    /**
     * 组维度报表
     *
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.GROUP})
    @RequestMapping("/queryDayTotalReport")
    public JsonResult queryDayTotalReport(@RequestBody Map map) throws Exception {
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String endDate = String.valueOf(map.get(Dict.ENDDATE));
        List<String> group = (List<String>)(map.get(Dict.GROUP));
        boolean isParent = (boolean)map.get(Dict.ISPARENT);
        return JsonResultUtil.buildSuccess(odService.queryDayTotalReport(startDate, endDate, group, isParent));
    }

    /**
     * 组维度报表导出
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/exportGroupStatement")
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.GROUP})
    public void exportGroupStatement(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        odService.exportGroupStatement(map, resp);
    }


    /**
     * 日报报表组表
     *
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    @RequestMapping("/queryDayGroupReport")
    public JsonResult queryDayGroupReport(@RequestBody Map<String, String> map) throws Exception {
        String startDate = map.get(Dict.STARTDATE);
        String endDate = map.get(Dict.ENDDATE);
        String groupId = map.get(Dict.GROUPID);
        return JsonResultUtil.buildSuccess(odService.queryDayGroupReport(startDate, endDate, groupId));
    }

    /**
     * 导出日报报表组表excel
     *
     * @param requestMap
     * @param resp
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    @RequestMapping(value = "/exportDayGroupReport")
    public void exportDayGroupReport(@RequestBody Map<String, String> requestMap, HttpServletResponse resp) throws Exception {
        odService.exportDayGroupReport(requestMap, resp);
    }

    /**
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    @RequestMapping("/exportReport")
    public JsonResult exportReport(@RequestBody Map<String, String> map) throws Exception {
        String startDate = map.get(Dict.STARTDATE);
        String endDate = map.get(Dict.ENDDATE);
        return JsonResultUtil.buildSuccess(odService.exportReport(startDate, endDate));
    }

    /**
     * 根据用户和期限查询工单信息
     * @param requestMap
     * @return
     */
    @RequestMapping("/queryOrderInfoByUser")
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE, Dict.USER_EN_NAME})
    public JsonResult queryOrderInfoByUser(@RequestBody Map<String, String> requestMap) {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            result = odService.queryOrderInfoByUser(requestMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据组id，选定日期范围查询组内工单情况
     * @param requestMap
     * @return
     */
    @RequestMapping("/queryGroupInfo")
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    public JsonResult queryGroupInfo(@RequestBody Map<String, String> requestMap) {
        Map<String, Integer> result = new HashMap<>();
        try {
            result = odService.queryGroupInfo(requestMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 导出个人维度报表
     * @param requestMap
     * @return
     */
    @RequestMapping("/exportPersonalDimensionReport")
    @RequestValidate(NotEmptyFields = {Dict.STARTDATE, Dict.ENDDATE})
    public void exportPersonalDimensionReport(@RequestBody Map<String, Object> requestMap, HttpServletResponse resp) throws Exception {
        odService.exportPersonalDimensionReport(requestMap, resp);
    }

    /**
     * 趋势图
     * @param map
     * @return
     */
    @RequestMapping("/tendencyChart")
    @RequestValidate(NotEmptyFields = {Dict.DATETYPE, Dict.STARTDATE, Dict.TIMES})
    public JsonResult tendencyChart(@RequestBody Map map) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String dateType = String.valueOf(map.get(Dict.DATETYPE));
        String startDate = String.valueOf(map.get(Dict.STARTDATE));
        String times = String.valueOf(map.get(Dict.TIMES));
        List<String> groupIds = (List<String>) map.get(Dict.GROUPIDS);
        result = odService.tendencyChart(dateType, startDate, times, groupIds);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 缺陷维度报表导出
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/exportMantisStatement")
    @RequestValidate(NotEmptyFields = {Dict.DATETYPE, Dict.STARTDATE, Dict.TIMES})
    public void exportMantisStatement(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        odService.exportMantisStatement(map, resp);
    }

    /**
     * 质量报表
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/qualityReport")
    @RequestValidate(NotEmptyFields = {Dict.GROUPID})
    public JsonResult qualityReport(@RequestBody Map map) throws Exception {
        Map<String, Object> data = odService.cacheQualityReport();
        List<Map<String, Object>> result = odService.qualityReportNew(map, data);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 质量报表导出
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/qualityReportExport")
    @RequestValidate(NotEmptyFields = {Dict.GROUPID})
    public void qualityReportExport(HttpServletResponse response, @RequestBody Map map) throws Exception {
        Map<String, Object> data = odService.cacheQualityReport();
        List<Map<String, Object>> result = odService.qualityReportNew(map, data);
        exportExcelService.qualityReportExport(response, result);
    }

    /**
     * 获取质量报表的全量底数
     * @return
     * @throws Exception
     */
    @RequestMapping("/cacheQualityReport")
    public JsonResult cacheQualityReport() throws Exception {
        redisUtil.removeCache("cacheQualityReport");
        Thread thread = new Thread(()->{
            try {
                odService.cacheQualityReport();
            } catch (Exception e) {
                logger.error("fail to cacheQualityReport");
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
            }
        });
        thread.start();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 质量报表
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/qualityReportNew")
    @RequestValidate(NotEmptyFields = {Dict.GROUPID})
    public JsonResult qualityReportNew(@RequestBody Map map) throws Exception {
        Map<String, Object> data = odService.cacheQualityReport();
        List<Map<String, Object>> result = odService.qualityReportNew(map,data);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询提测准时率详情
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/qualityReportNewUnit")
    @RequestValidate(NotEmptyFields = {Dict.FDEVGROUPID})
    public JsonResult qualityReportNewUnit(@RequestBody Map map) throws Exception {
        Map<String, Object> data = odService.cacheQualityReport();
        return JsonResultUtil.buildSuccess(odService.qualityReportNewUnit(map, data));
    }

    /**
     * 查询提测准时率详情
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/qualityReportNewUnitExport")
    @RequestValidate(NotEmptyFields = {Dict.FDEVGROUPID})
    public void qualityReportNewUnitExport(HttpServletResponse response, @RequestBody Map map) throws Exception {
        Map<String, Object> data = odService.cacheQualityReport();
        exportExcelService.qualityReportNewUnitExport(response, odService.qualityReportNewUnit(map, data), (String)map.get(Dict.REPORTTYPE));
    }
}



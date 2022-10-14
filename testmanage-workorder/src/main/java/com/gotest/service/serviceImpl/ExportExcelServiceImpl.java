package com.gotest.service.serviceImpl;


import com.gotest.controller.ReportController;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.domain.OrderDimension;
import com.gotest.service.ExportExcelService;
import com.gotest.service.OrderDimensionService;
import com.gotest.utils.MyUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class ExportExcelServiceImpl implements ExportExcelService {

	@Autowired
	private static Logger logger = LoggerFactory.getLogger(ReportController.class);
	@Autowired
	private OrderDimensionService odService;

	@Override
	public void generateProdExcel(Map dateMap, HttpServletResponse resp) throws Exception {
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			// workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			List<OrderDimension> list = odService.queryOrderDimension(dateMap);
			MyUtil.setCellValue(workbook, 0, 0, 0, "工单编号");
			MyUtil.setCellValue(workbook, 0, 0, 1, "主任务名称");
			MyUtil.setCellValue(workbook, 0, 0, 2, "工单状态");
			MyUtil.setCellValue(workbook, 0, 0, 3, "执行案例总数");
			MyUtil.setCellValue(workbook, 0, 0, 4, "未执行案例总数");
			MyUtil.setCellValue(workbook, 0, 0, 5, "案例通过数");
			MyUtil.setCellValue(workbook, 0, 0, 6, "案例失败数");
			MyUtil.setCellValue(workbook, 0, 0, 7, "案例阻塞数");
			MyUtil.setCellValue(workbook, 0, 0, 8, "缺陷总数");

			int i = 1;
			for (OrderDimension orderDimension : list) {
				MyUtil.setCellValue(workbook, 0, i, 0, orderDimension.getWorkNo());// 工单编号
				MyUtil.setCellValue(workbook, 0, i, 1, orderDimension.getMainTaskName());// 主任务名称
				MyUtil.setCellValue(workbook, 0, i, 2, orderDimension.getWorkStage());// 工单状态
				MyUtil.setCellValue(workbook, 0, i, 3, orderDimension.getCaseExecute().toString());// 执行案例总数
				MyUtil.setCellValue(workbook, 0, i, 4, orderDimension.getCaseNoExecute().toString());// 未执行案例总数
				MyUtil.setCellValue(workbook, 0, i, 5, orderDimension.getCasePass().toString());// 案例通过数
				MyUtil.setCellValue(workbook, 0, i, 6, orderDimension.getCaseFailure().toString());// 案例失败数
				MyUtil.setCellValue(workbook, 0, i, 7, orderDimension.getCaseBlock().toString());// 案例阻塞数
				MyUtil.setCellValue(workbook, 0, i, 8, orderDimension.getCaseMantis().toString());// 缺陷总数
				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Disposition", "attachment;filename=" + "workOrderReport.xlsx");
		workbook.write(resp.getOutputStream());
	}

	@Override
	public void exportUserAllOrder(List<Map> userOrderList, HttpServletResponse resp) throws Exception {
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			// workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			MyUtil.setCellValue(workbook, 0, 0, 0, "测试任务名称");
			MyUtil.setCellValue(workbook, 0, 0, 1, "需求编号/实施单元编号");
			MyUtil.setCellValue(workbook, 0, 0, 2, "工单编号");
			MyUtil.setCellValue(workbook, 0, 0, 3, "所属小组");
			MyUtil.setCellValue(workbook, 0, 0, 4, "工单负责人");
			MyUtil.setCellValue(workbook, 0, 0, 5, "测试小组长");
			MyUtil.setCellValue(workbook, 0, 0, 6, "测试人员");
			MyUtil.setCellValue(workbook, 0, 0, 7, "测试阶段");
            MyUtil.setCellValue(workbook, 0, 0, 8, "计划SIT开始时间");
            MyUtil.setCellValue(workbook, 0, 0, 9, "计划UAT开始时间");
            MyUtil.setCellValue(workbook, 0, 0, 10, "计划投产开始时间");
            MyUtil.setCellValue(workbook, 0, 0, 11, "工单类型");
			int i = 1;
			for (Map<String,String> map : userOrderList) {
				MyUtil.setCellValue(workbook, 0, i, 0, map.get(Dict.MAINTASKNAME));// 测试任务名称
				MyUtil.setCellValue(workbook, 0, i, 1, map.get(Dict.UNIT));// 需求编号/实施单元编号
				MyUtil.setCellValue(workbook, 0, i, 2, map.get(Dict.WORKORDERNO));// 工单编号
				MyUtil.setCellValue(workbook, 0, i, 3, map.get(Dict.FDEVGROUPNAME));// 所属小组
				MyUtil.setCellValue(workbook, 0, i, 4, map.get(Dict.WORKMANAGER));// 工单负责人
				MyUtil.setCellValue(workbook, 0, i, 5, map.get(Dict.GROUPLEADER));// 测试小组长
				MyUtil.setCellValue(workbook, 0, i, 6, map.get(Dict.TESTERS));// 测试人员
				MyUtil.setCellValue(workbook, 0, i, 7, map.get(Dict.WORKSTAGE));// 测试阶段
                MyUtil.setCellValue(workbook, 0, i, 8, map.get(Dict.PLANSITDATE));// 测试小组长
                MyUtil.setCellValue(workbook, 0, i, 9, map.get(Dict.PLANUATDATE));// 测试人员
                MyUtil.setCellValue(workbook, 0, i, 10, map.get(Dict.PLANPRODATE));// 测试阶段
                MyUtil.setCellValue(workbook, 0, i, 11, map.get(Dict.ORDERTYPE));// 工单类型
				i++;
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Disposition", "attachment;filename=" + "myOrderList.xlsx");
		workbook.write(resp.getOutputStream());
	}

	@Override
	public void qualityReportExport(HttpServletResponse response, List<Map<String, Object>> result) throws IOException {
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			workbook.createSheet();
			MyUtil.setCellValue(workbook, 0, 0, 0, "组名");
			MyUtil.setCellValue(workbook, 0, 0, 1, "提测准时率");
			MyUtil.setCellValue(workbook, 0, 0, 2, "冒烟测试通过率");
			MyUtil.setCellValue(workbook, 0, 0, 3, "缺陷reopen率");
			MyUtil.setCellValue(workbook, 0, 0, 4, "缺陷密度");
			MyUtil.setCellValue(workbook, 0, 0, 5, "缺陷平均解决时长");
			MyUtil.setCellValue(workbook, 0, 0, 6, "严重缺陷解决时长");
			int i = 1;
			for (Map<String,Object> map : result) {
				MyUtil.setCellValue(workbook, 0, i, 0, String.valueOf(map.get(Dict.NAME)));// 组名
				MyUtil.setCellValue(workbook, 0, i, 1, MyUtil.formatPercent((String)map.get(Dict.TIMELYRATE)));// 提测准时率
				MyUtil.setCellValue(workbook, 0, i, 2, MyUtil.formatPercent((String)map.get(Dict.SMOKERATE)));// 冒烟测试通过率
				MyUtil.setCellValue(workbook, 0, i, 3, MyUtil.formatPercent((String)map.get(Dict.REOPENRATE)));// 缺陷reopen率
				MyUtil.setCellValue(workbook, 0, i, 4, String.valueOf(map.get(Dict.MANTISRATE)));// 缺陷密度
				MyUtil.setCellValue(workbook, 0, i, 5, String.valueOf(map.get(Dict.NORMALAVGTIME)));// 缺陷平均解决时长
				MyUtil.setCellValue(workbook, 0, i, 6, String.valueOf(map.get(Dict.SEVAVGTIME)));// 严重缺陷解决时长
				i++;
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("质量分析报表","UTF-8") +".xlsx");
		workbook.write(response.getOutputStream());
	}

	@Override
	public void qualityReportNewUnitExport(HttpServletResponse response, Map<String, Object> qualityReportNewUnit, String reportType) throws IOException {
		//根据类型，取不同的数据
		List<Map> data = new ArrayList<>();
		//报表每列所取字段的对应关系
		LinkedHashMap<String, String> cloumMap = new LinkedHashMap<>();
		//设置报表需要的数据和列名等，并返回报表名
		String reportName = MyUtil.setReportDataAndCloum(data, cloumMap, qualityReportNewUnit, reportType);
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			//所取字段key集合
			List<String> keys = new ArrayList<>(cloumMap.keySet());
			workbook.createSheet();
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				MyUtil.setCellValue(workbook, 0, 0, i, cloumMap.get(key));
			}
			for (int i = 0; i < data.size(); i++) {
				Map<String, Object> map = data.get(i);
				for (int j = 0; j < keys.size(); j++) {
					String key = keys.get(j);
					//如果是严重缺陷解决时长报表的严重度或优先级字段，做一下转换
					if (Constants.REPORTTYPE_SEVAVGTIME.equals(reportType) && Dict.SEVERITY.equals(key)) {
						MyUtil.setCellValue(workbook, 0, i+1, j, MyUtil.getSeverityNameCH(String.valueOf(map.get(key))));
					} else if (Constants.REPORTTYPE_SEVAVGTIME.equals(reportType) && Dict.PRIORITY.equals(key)) {
						MyUtil.setCellValue(workbook, 0, i+1, j, MyUtil.getPriorityNameCH(String.valueOf(map.get(key))));
					} else if ((Constants.REPORTTYPE_NORMALAVGTIME.equals(reportType) || Constants.REPORTTYPE_SEVAVGTIME.equals(reportType))
							&& Dict.SOLVETIME.equals(key)) {
						//如果是缺陷平均解决时长报表或者严重缺陷解决时长报表的解决时长字段，加一个天
						MyUtil.setCellValue(workbook, 0, i+1, j, map.get(key) == null ? "" : map.get(key) + "天");
					} else if (Constants.REPORTTYPE_SMOKERATE.equals(reportType) && Dict.REASON.equals(key)) {
						//如果是冒烟测试通过率里的打回列表，转义打回原因
						MyUtil.setCellValue(workbook, 0, i+1, j, MyUtil.mapReason(String.valueOf(map.get(key))));
					} else {
						MyUtil.setCellValue(workbook, 0, i+1, j, map.get(key) == null ? "" : String.valueOf(map.get(key)));
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(reportName,"UTF-8") +".xlsx");
		workbook.write(response.getOutputStream());
	}
}

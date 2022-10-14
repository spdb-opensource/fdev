package com.manager.ftms.service.impl;

import com.manager.ftms.controller.TestCaseController;
import com.manager.ftms.dao.FunctionPointDao;
import com.manager.ftms.dao.SystemModelDao;
import com.manager.ftms.entity.Report;
import com.manager.ftms.entity.Testcase;
import com.manager.ftms.service.ExportExcelService;
import com.manager.ftms.service.TestCaseService;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.dict.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.Util;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ExportExcelServiceImpl implements ExportExcelService {

	@Autowired
	private TestCaseService testCaseService;
	private static Logger logger = LoggerFactory.getLogger(TestCaseController.class);
	@Value("${fjob.email.filePath}")
	private String filePath;
    @Autowired
    private SystemModelDao systemModelDao;
    @Autowired
	private FunctionPointDao functionPointDao;

	@Override
	public void generateProdExcel(Map map, HttpServletResponse resp) throws Exception {
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			List<Testcase> list = new ArrayList<Testcase>();	
			List<Map> planIds = (List<Map>) map.get("planIds");
			if(!Util.isNullOrEmpty(planIds)) {
				for(int i = 0;i < planIds.size();i++) {
					String planName = (String) planIds.get(i).get("planName");
					Sheet sheet = workbook.createSheet(planName);
					Map parameter = new HashMap();
					parameter.put("planId", planIds.get(i).get("planId"));
					list = testCaseService.queryTestcaseByPlanId(parameter);
					setExportWorkBook(workbook, list,i);
				}
			}else {
				setExportWorkBook(workbook, list,0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		writeResponse(resp, workbook, "testcase.xlsx");
	}

	//案例库导出
	@Override
	public void generateCaseExcel(Map map, HttpServletResponse resp) throws Exception{
		XSSFWorkbook workbook = null;
		try {
			// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
			workbook = new XSSFWorkbook();
			// workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			List<Testcase> list = testCaseService.queryTestcaseByFuncId(map);
			setExportWorkBook(workbook, list,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writeResponse(resp, workbook, "allCase.xlsx");
	}



	/**
	 * excel填值
	 */
	private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue){
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet == null) {
			sheet = workbook.createSheet(String.valueOf(sheetIndex));
		}
		if (sheet.getRow(rowIndex) == null) {
			sheet.createRow(rowIndex);
		}
		if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
			sheet.getRow(rowIndex).createCell(cellIndex);
		}
		sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
	}

	/**
	 * 将文件 以流的形式，写到response中 减少重复代码
	 */
	private void writeResponse(HttpServletResponse resp, XSSFWorkbook workbook, String fileName){
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		try {
			workbook.write(resp.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new FtmsException(ErrorConstants.SERVER_ERROR);
		}
	}

	/**
	 * 设置导出文件 减少重复代码
	 */
	private void setExportWorkBook(XSSFWorkbook workbook, List<Testcase> list,int index) throws Exception{

		//前十项配合导入案例
		setCellValue(workbook, index, 0, 0, "系统名称");
		setCellValue(workbook, index, 0, 1, "功能模块");
		setCellValue(workbook, index, 0, 2, "案例名称");
		setCellValue(workbook, index, 0, 3, "案例类型");
		setCellValue(workbook, index, 0, 4, "案例优先级");
		setCellValue(workbook, index, 0, 5, "案例性质");
		setCellValue(workbook, index, 0, 6, "功能点");
		setCellValue(workbook, index, 0, 7, "案例前置条件");
		setCellValue(workbook, index, 0, 8, "案例描述");
		setCellValue(workbook, index, 0, 9, "预期结果");
		setCellValue(workbook, index, 0, 10, "案例编号");
		setCellValue(workbook, index, 0, 11, "案例审核状态");
		setCellValue(workbook, index, 0, 12, "案例备注");
		setCellValue(workbook, index, 0, 13, "案例版本");
		setCellValue(workbook, index, 0, 14, "案例编写人");
		setCellValue(workbook, index, 0, 15, "新建时间");
		setCellValue(workbook, index, 0, 16, "执行结果");
		setCellValue(workbook, index, 0, 17, "执行时间");
		int i = 1;
		for (Testcase testcase : list) {
			// 系统名称
			setCellValue(workbook, index, i, 0,
					systemModelDao.querySysNameByFuncId(String.valueOf(testcase.getTestcaseFuncId())));
			// 功能模块
			setCellValue(workbook, index, i, 1,  makeFullFuncName(testcase.getTestcaseFuncId(), testcase.getTestcaseFuncName()));
			// 案例名称
			setCellValue(workbook, index, i, 2, testcase.getTestcaseName());
			// 案例类型
			setCellValue(workbook, index, i, 3,
					Utils.getParamMapperProperties("Type." + testcase.getTestcaseType()));
			// 案例优先级
			setCellValue(workbook, index, i, 4,
					Utils.getParamMapperProperties("Priority." + testcase.getTestcasePriority()));
			// 案例性质
			setCellValue(workbook, index, i, 5,
					Utils.getParamMapperProperties("Nature." + testcase.getTestcaseNature()));
			// 功能点
			setCellValue(workbook, index, i, 6, testcase.getFuncationPoint());
			// 案例前置条件
			setCellValue(workbook, index, i, 7, testcase.getTestcasePre());
			// 案例描述
			setCellValue(workbook, index, i, 8, testcase.getTestcaseDescribe());
			// 预期结果
			setCellValue(workbook, index, i, 9, testcase.getExpectedResult());
			// 案例编号
			setCellValue(workbook, index, i, 10, testcase.getTestcaseNo());
			// 案例审核状态
			setCellValue(workbook, index, i, 11,
					Utils.getParamMapperProperties("Status." + testcase.getTestcaseStatus()));
			// 案例备注
			setCellValue(workbook, index, i, 12, testcase.getRemark());
			// 案例版本
			setCellValue(workbook, index, i, 13, testcase.getTestcaseVersion());
			// 案例编写人
			setCellValue(workbook, index, i, 14, testcase.getUserName());
			// 新建时间
			setCellValue(workbook, index, i, 15, testcase.getTestcaseDate());
			// 执行结果
			setCellValue(workbook, index, i, 16,
					Utils.getParamMapperProperties("Result." + testcase.getTestcaseExecuteResult()));
			// 执行时间
			setCellValue(workbook, index, i, 17, testcase.getTestcaseExecuteDate());
			i++;
		}
	}

    /**
     * 组装完成的功能模块
     * @param funcId
     * @param funcName
     * @return
     * @throws Exception
     */
	private String makeFullFuncName(Integer funcId, String funcName) throws Exception {
		if(Util.isNullOrEmpty(funcId)){
			return "";
		}
		StringBuilder fullFuncName = new StringBuilder();
        Map<String, Object> parent = functionPointDao.queryParentByFuncName(funcId);
		fullFuncName.append(funcName);
		while(!Util.isNullOrEmpty(parent)){
			fullFuncName.insert(0,parent.get(Dict.PARENTFUNCNAME)+">");
			funcId = Integer.valueOf(parent.get(Dict.PARENTID).toString());
			parent = functionPointDao.queryParentByFuncName(funcId);
		}
		return fullFuncName.toString();
	}



	/**
	 * 设置部分 表头数据， 减少重复代码
	 */
	private void setWorkBookValue(XSSFWorkbook workbook, Report weekReport, int i){
		setCellValue(workbook, 0, i, 6, weekReport.getTestcaseNum().toString());// 编写案例总数数
		setCellValue(workbook, 0, i, 7, weekReport.getExecuteNum().toString());// 执行数
		setCellValue(workbook, 0, i, 8, weekReport.getMantisNum().toString());// 缺陷数
		setCellValue(workbook, 0, i, 9, weekReport.getRequireNum().toString());//业务需求问题
		setCellValue(workbook, 0, i, 10, weekReport.getRequireRoleNum().toString());// 需规问题
		setCellValue(workbook, 0, i, 11, weekReport.getFuncMantisNum().toString());// 功能实现不完整
		setCellValue(workbook, 0, i, 12, weekReport.getErrorNum().toString());// 功能实现错误
		setCellValue(workbook, 0, i, 13, weekReport.getHistoryNum().toString());// 历史遗留问题
		setCellValue(workbook, 0, i, 14,weekReport.getAdviseNum().toString());// 优化建议
		setCellValue(workbook, 0, i, 15, weekReport.getJavaNum().toString());// 后台问题
		setCellValue(workbook, 0, i, 16, weekReport.getPackNum().toString());// 打包问题
		setCellValue(workbook, 0, i, 17, weekReport.getDataNum().toString());// 数据问题
		setCellValue(workbook, 0, i, 18, weekReport.getEnNum().toString());// 环境问题
		setCellValue(workbook, 0, i, 19, weekReport.getOtherNum().toString());// 其他原因
	}
	
	/**
	 * 设置部分 表头数据， 减少重复代码
	 */
	private void setWorkBookValueForAll(XSSFWorkbook workbook, Report weekReport, int i){
		setCellValue(workbook, 0, i, 1, weekReport.getTestcaseNum().toString());// 编写案例总数数
		setCellValue(workbook, 0, i, 2, weekReport.getExecuteNum().toString());// 执行数
		setCellValue(workbook, 0, i, 3, weekReport.getMantisNum().toString());// 缺陷数
		setCellValue(workbook, 0, i, 4, weekReport.getRequireNum().toString());//业务需求问题
		setCellValue(workbook, 0, i, 5, weekReport.getRequireRoleNum().toString());// 需规问题
		setCellValue(workbook, 0, i, 6, weekReport.getFuncMantisNum().toString());// 功能实现不完整
		setCellValue(workbook, 0, i, 7, weekReport.getErrorNum().toString());// 功能实现错误
		setCellValue(workbook, 0, i, 8, weekReport.getHistoryNum().toString());// 历史遗留问题
		setCellValue(workbook, 0, i, 9,weekReport.getAdviseNum().toString());// 优化建议
		setCellValue(workbook, 0, i, 10, weekReport.getJavaNum().toString());// 后台问题
		setCellValue(workbook, 0, i, 11, weekReport.getPackNum().toString());// 打包问题
		setCellValue(workbook, 0, i, 12, weekReport.getDataNum().toString());// 数据问题
		setCellValue(workbook, 0, i, 13, weekReport.getEnNum().toString());// 环境问题
		setCellValue(workbook, 0, i, 14, weekReport.getOtherNum().toString());// 其他原因
	}

	/**
	 * 组装excel并上送指定路径
	 * @param plans
	 * @throws Exception
	 */
	public String sendCaseExcelToPath(List<Map<String, String>> plans) throws  Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Integer sheetPage = 0;
		boolean noCase = true;
		//遍历计划，每个计划对应excel的一个sheet
		for(Map plan : plans){
			Sheet sheet = workbook.createSheet(plan.get(Dict.PLANNAME).toString());
			//查询计划下的案例列表
			Map map = new HashMap();
			map.put(Dict.PLANID, plan.get(Dict.PLANID));
			List<Testcase> caseList = null;
			try {
				caseList = testCaseService.queryTestcaseByPlanId(map);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new FtmsException(com.manager.ftms.util.ErrorConstants.QUERY_DATA_EXCEPTION, new String[]{"根据planId查询案例信息失败"});
			}
			if(caseList.isEmpty()){
				continue;
			}
			noCase = false;
			//将案例列表写入sheet
			writeCaseIntoSheet(sheetPage, workbook, caseList);
			sheetPage++;
		}
		//如果一条案例都没有，则报错
		if(noCase){
			throw new FtmsException(com.manager.ftms.util.ErrorConstants.CASE_NOT_EXIST_ERROR);
		}
		String fileName = "testCase"+ System.currentTimeMillis()+".xlsx";
		saveExcelToPath(filePath, fileName, workbook);
		return fileName;
	}

	/**
	 * 将案例列表写入sheet
	 * @param sheetPage
	 * @param workbook
	 * @param caseList
	 * @throws Exception
	 */
	private void writeCaseIntoSheet(Integer sheetPage, XSSFWorkbook workbook, List<Testcase> caseList) throws Exception {
		setCellValue(workbook, sheetPage, 0, 0, "案例编号");
		setCellValue(workbook, sheetPage, 0, 1, "案例名称");
		setCellValue(workbook, sheetPage, 0, 2, "案例审核状态");
		setCellValue(workbook, sheetPage, 0, 3, "案例类型");
		setCellValue(workbook, sheetPage, 0, 4, "案例优先级");
		setCellValue(workbook, sheetPage, 0, 5, "案例前置条件");
		setCellValue(workbook, sheetPage, 0, 6, "案例性质");
		setCellValue(workbook, sheetPage, 0, 7, "功能点");
		setCellValue(workbook, sheetPage, 0, 8, "案例描述");
		setCellValue(workbook, sheetPage, 0, 9, "案例备注");
		setCellValue(workbook, sheetPage, 0, 10, "案例版本");
		setCellValue(workbook, sheetPage, 0, 11, "案例编写人");
		setCellValue(workbook, sheetPage, 0, 12, "新建时间");
		setCellValue(workbook, sheetPage, 0, 13, "功能模块");
		setCellValue(workbook, sheetPage, 0, 14, "预期结果");
		setCellValue(workbook, sheetPage, 0, 15, "执行结果");
		setCellValue(workbook, sheetPage, 0, 16, "执行时间");
		int i = 1;
		for (Testcase testcase : caseList) {
			setCellValue(workbook, sheetPage, i, 0, testcase.getTestcaseNo());// 案例编号
			setCellValue(workbook, sheetPage, i, 1, testcase.getTestcaseName());// 案例名称
			setCellValue(workbook, sheetPage, i, 2,
					Utils.getParamMapperProperties("Status." + testcase.getTestcaseStatus()));// 案例审核状态
			setCellValue(workbook, sheetPage, i, 3, Utils.getParamMapperProperties("Type." + testcase.getTestcaseType()));// 案例类型
			setCellValue(workbook, sheetPage, i, 4,
					Utils.getParamMapperProperties("Priority." + testcase.getTestcasePriority()));// 案例优先级
			setCellValue(workbook, sheetPage, i, 5, testcase.getTestcasePre());// 案例前置条件
			setCellValue(workbook, sheetPage, i, 6,
					Utils.getParamMapperProperties("Nature." + testcase.getTestcaseNature()));// 案例性质
			setCellValue(workbook, sheetPage, i, 7, testcase.getFuncationPoint());// 功能点
			setCellValue(workbook, sheetPage, i, 8, testcase.getTestcaseDescribe());// 案例描述
			setCellValue(workbook, sheetPage, i, 9, testcase.getRemark());// 案例备注
			setCellValue(workbook, sheetPage, i, 10, testcase.getTestcaseVersion());// 案例版本
			setCellValue(workbook, sheetPage, i, 11, testcase.getUserName());// 案例编写人
			setCellValue(workbook, sheetPage, i, 12, testcase.getTestcaseDate());// 新建时间
			setCellValue(workbook, sheetPage, i, 13, testcase.getTestcaseFuncName()==null?"":testcase.getTestcaseFuncName());// 功能模块
			setCellValue(workbook, sheetPage, i, 14, testcase.getExpectedResult());// 预期结果
			setCellValue(workbook, sheetPage, i, 15,
					Utils.getParamMapperProperties("Result." + testcase.getTestcaseExecuteResult()));// 执行结果
			setCellValue(workbook, sheetPage, i, 16, testcase.getTestcaseExecuteDate());// 执行时间
			i++;
		}
	}

	/**
	 * 写文件到 指定的目录，返回存储路径
	 */
	private String saveExcelToPath(String filePath, String fileName, XSSFWorkbook workbook) throws Exception {
		FileOutputStream output = null;
		ByteArrayOutputStream bs = null;
		String savePath = filePath;
		try {
			savePath = filePath + fileName;
			File file = new File(savePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			file.setWritable(true, false);
			file.setReadable(true, false);
			bs = new ByteArrayOutputStream();
			workbook.write(bs);
			output = new FileOutputStream(file);
			output.write(bs.toByteArray());
			logger.info("======="+savePath);
			return savePath;
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(output!=null){
				output.flush();
				output.close();
			}
			if(bs!=null){
				bs.close();
			}
		}
		return savePath;
	}
}



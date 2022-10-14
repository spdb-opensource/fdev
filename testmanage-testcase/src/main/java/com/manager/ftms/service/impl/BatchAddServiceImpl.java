package com.manager.ftms.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.manager.ftms.dao.PlanlistTestcaseRelationMapper;
import com.manager.ftms.entity.PlanlistTestcaseRelation;
import com.manager.ftms.entity.TestcaseExeRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.ftms.dao.TestcaseMapper;
import com.manager.ftms.entity.Testcase;
import com.manager.ftms.service.BatchAddService;
import com.manager.ftms.service.FunctionPointService;
import com.manager.ftms.service.IPlanlistTestcaseRelationServier;
import com.manager.ftms.service.ISystemModelService;
import com.manager.ftms.util.Constants;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.util.Util;

@Service
public class BatchAddServiceImpl implements BatchAddService {
	@Autowired
	private FunctionPointService functionPointService;
	@Autowired
	private TestcaseMapper testcaseMapper;
	@Autowired
	private RedisUtils redisUtils;
	@Autowired
	private IPlanlistTestcaseRelationServier planlistTestcaseRelationServier;
	@Autowired
	private ISystemModelService systemModelService;
	@Autowired
	private Utils utils;
	@Autowired
	private PlanlistTestcaseRelationMapper planlistTestcaseRelationMapper;
	
	private static Logger logger = LoggerFactory.getLogger(BatchAddServiceImpl.class);

	@Override
	public void downloadTemplate(HttpServletResponse resp) throws Exception {
		ClassPathResource resource = new ClassPathResource("file/template.xlsx");
        try ( OutputStream output = resp.getOutputStream();
        	  InputStream in = resource.getInputStream();) {
            // 读取文件
        	resp.reset();
        	resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Headers", "content-type");
            resp.setHeader("Access-Control-Allow-Methods", "*");
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/vnd.ms-excel;charset=utf-8");
            resp.setHeader("Content-Disposition",
                    "attachment;filename=" +"template.xlsx");
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            resp.flushBuffer();
        } catch (Exception e) {
        	logger.error("e"+e);
        	throw new FtmsException(ErrorConstants.BATCH_ADD_TEMPLATE_DOWNLOAD_ERROR);
        }

	}
	


	@Override
	@Transactional
	public void batchAdd(String planId, String workNo, MultipartFile file,HttpServletResponse resp) throws Exception {
		List<Testcase> testcaseList = new ArrayList<Testcase>();
		StringBuilder errMessage = new StringBuilder();
	    String userName = utils.getCurrentUserEnName();
			InputStream inputStream = null;
		try {
            inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            sheet.getRowSumsBelow();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (!Utils.isEmpty(row)) {
                    if(isRowNull(row)) {
                        break;
                    }
                    Testcase testcase = new Testcase();
                    Cell systemName = row.getCell(0);
                    Cell testcaseFuncName = row.getCell(1);
                    Cell testcaseName = row.getCell(2);
                    Cell testcaseType = row.getCell(3);
                    Cell testcasePriority = row.getCell(4);
                    Cell testcaseNature = row.getCell(5);
                    Cell funcationPoint = row.getCell(6);
                    Cell testcasePre = row.getCell(7);
                    Cell testcaseDescribe = row.getCell(8);
                    Cell expectedResult = row.getCell(9);
                    testcase.setSystemName(readCell(systemName));
                    testcase.setTestcaseFuncName(readCell(testcaseFuncName));
                    testcase.setTestcaseName(readCell(testcaseName));
                    testcase.setTestcaseType(readCell(testcaseType));
                    testcase.setTestcasePriority(readCell(testcasePriority));
                    testcase.setTestcaseNature(readCell(testcaseNature));
                    testcase.setFuncationPoint(readCell(funcationPoint));
                    testcase.setExpectedResult(readCell(expectedResult));
                    testcase.setTestcasePre(readCell(testcasePre));
                    testcase.setTestcaseDescribe(readCell(testcaseDescribe));
                    testcase.setCreateOpr(userName);
                    errMessage.append(checkData(testcase, i+1));
                    if(Utils.isEmpty(errMessage.toString())) {
                        testcaseList.add(testcase);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("e"+e);
            throw new FtmsException(ErrorConstants.BATCH_ADD_ERROR);
        } finally {
            if(inputStream != null) {
                inputStream.close();
            }
        }
		if(!Util.isNullOrEmpty(errMessage.toString())) {
			throw new FtmsException(ErrorConstants.BATCH_ADD_FORMAT_ERROR,new String [] {errMessage.toString()});
		}
		for (Testcase testcase : testcaseList) {
			testcase = setData(testcase);
			addTestcase(testcase);
			Map<String,Object> planMap = new HashMap<>();
			planMap.put(Dict.TESTCASENO, testcase.getTestcaseNo());
			planMap.put(Dict.PLANID, planId);
			planMap.put(Dict.WORKNO, workNo);
			planMap.put(Dict.TESTCASEEXECUTERESULT, "0");
			planMap.put("createTm", Utils.formatDate(Utils.FULL_TIME));
			planMap.put(Dict.CREATEOPR, testcase.getCreateOpr());
			planlistTestcaseRelationServier.addPlanlistTsetcaseRelation(planMap);
		}
	}
	
	public boolean isRowNull(Row row) {
		for(int i=row.getFirstCellNum();i<row.getLastCellNum();i++) {
			Cell cell = row.getCell(i);
			if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				  return false;
			}
		}
		return true;
	}
	
	public String readCell(Cell cell) {
		String value = "";
		if(Utils.isEmpty(cell)) {
			return value;
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			 value = cell.getStringCellValue();
		break;
		case Cell.CELL_TYPE_NUMERIC:
			 cell.setCellType(Cell.CELL_TYPE_STRING);
			 value = cell.getStringCellValue();
		break;	 
		default :
			value = "";
			break;
		}
		return value;
		
	}

	public StringBuilder checkData(Testcase testcase, int i) throws Exception {
		StringBuilder errMsg = new StringBuilder();
		if(Utils.isEmpty(testcase.getSystemName())) {
		    errMsg.append("第【").append(i).append("】行，【系统名称】字段为空\r\n");
		}else {
			String sys_id = checkTestcaseSysName(testcase);
			if(Util.isNullOrEmpty(sys_id)) {
				errMsg.append("第【").append(i).append("】行，【系统名称】字段异常\r\n");
			}else {
				testcase.setSystemId(Integer.parseInt(sys_id));
			}
		}
		
		if(!Util.isNullOrEmpty(testcase.getSystemId())) {
			if(Utils.isEmpty(testcase.getTestcaseFuncName())) {
				errMsg.append("第【").append(i).append("】行，【功能模块】字段为空\r\n");
			}else {
				String func_id = queryFuncId(testcase.getTestcaseFuncName(), String.valueOf(testcase.getSystemId()));
				if(Util.isNullOrEmpty(func_id)) {
					errMsg.append("第【").append(i).append("】行，【功能模块】字段异常\r\n");
				}
			}
		}
		
		if(Utils.isEmpty(testcase.getTestcaseName())) {
			errMsg.append("第【").append(i).append("】行，【案例名称】字段为空\r\n");
		}
		
		if(Utils.isEmpty(testcase.getTestcaseType())) {
			errMsg.append("第【").append(i).append("】行，【案例类型】字段为空\r\n");
		} else {
			String type = checkTestcaseType(testcase);
			if(Utils.isEmpty(type)) {
				errMsg.append("第【").append(i).append("】行，【案例类型】字段异常\r\n");
			}
		}
		
	
		
		if(Utils.isEmpty(testcase.getTestcasePriority())) {
			errMsg.append("第【").append(i).append("】行，【优先级】字段为空\r\n");
		} else {
			String priority = checkTestcasePriority(testcase);
			if(Utils.isEmpty(priority)) {
				errMsg.append("第【").append(i).append("】行，【优先级】字段异常\r\n");
			}
		}
		
		if(Utils.isEmpty(testcase.getTestcaseNature())) {
			errMsg.append("第【").append(i).append("】行，【案例性质】字段为空\r\n");
		} else {
			String nature = checkTestcaseNature(testcase);
			if(Utils.isEmpty(nature)) {
				errMsg.append("第【").append(i).append("】行，【案例性质】字段异常\r\n");
			}
		}
		
		if(Utils.isEmpty(testcase.getTestcaseDescribe())) {
			errMsg.append("第【").append(i).append("】行，【案例描述】字段为空\r\n");
		}
		
		if(Utils.isEmpty(testcase.getExpectedResult())) {
			errMsg.append("第【").append(i).append("】行，【预期结果】字段为空\r\n");
		}
		
		
		
	
	
		return errMsg;
	}
	
	public Testcase setData(Testcase testcase) throws Exception {
		String type = checkTestcaseType(testcase);
		testcase.setTestcaseType(type);
		String priority = checkTestcasePriority(testcase);
		testcase.setTestcasePriority(priority);
		String nature = checkTestcaseNature(testcase);
		testcase.setTestcaseNature(nature);
		String sys_id = checkTestcaseSysName(testcase);
		testcase.setSystemId(Integer.parseInt(sys_id));
		String func_id = queryFuncId(testcase.getTestcaseFuncName(), String.valueOf(sys_id));
		testcase.setTestcaseFuncId(Integer.parseInt(func_id));
		return testcase;
	}
	

	public String checkTestcaseType(Testcase testcase) {
		String testcaseType = testcase.getTestcaseType();
		String type = null;
		switch (testcaseType) {
		case "页面":
		     type = "1";
		break;
		case "功能":
			 type = "2";
		break;	 
		case "流程":
			 type = "3";
		break;
		case "链接":
			 type = "4";
		break;
		case "接口":
			 type = "5";
		break;
		case "批量":
			 type = "6";
		break;
		default :
			 type = "";
		}
		return type;
	}

	public String checkTestcasePriority(Testcase testcase) {
		String testcasePriority = testcase.getTestcasePriority();
		String priority = null;
		switch (testcasePriority) {
		case "高":
			priority = "1";
		break;
		case "中":
			priority = "2";
		break;	 
		case "低":
			priority = "3";
		break;
		default :
			priority = "";
		}
		return priority;
	}
	
	public String checkTestcaseNature(Testcase testcase) {
		String testcaseNature = testcase.getTestcaseNature();
		String nature = null;
		switch (testcaseNature) {
		case "正案例":
			nature = "1";
		break;
		case "反案例":
			nature = "2";
		break;
		default :
			nature = "";
		}
		return nature;
	}

	public String checkTestcaseSysName(Testcase testcase) throws Exception {
		return systemModelService.querySysIdBySysName(testcase.getSystemName());
	}
	
	public String queryFuncId(String testcaseFuncName,String sys_id) throws Exception {
		String[] funcNames = testcaseFuncName.split(">");
		String parentId = "0";
		String funcId = null;
		for(int i = 0;i < funcNames.length;i++) {
			funcId = functionPointService.queryFuncIdByNameAndPid(funcNames[i], parentId, sys_id);
			parentId = funcId;
		}
		return funcId;
	}

	@Override
	public void addTestcase(Testcase testcase) throws Exception {
		 String testcaseNo = this.generateTestcaseNo(testcase);
	        //根据testcaseNo查询是否生成库里面存在的案例编号
	        Testcase tc = testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
	        while (null!=tc){
	           testcaseNo = this.generateTestcaseNo(testcase);
	           tc = testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
	        }
	        testcase.setTestcaseNo(testcaseNo);
	        testcase.setTestcaseStatus("0");
	        testcase.setTestcaseDate(Utils.dateUtil(new Date()));
	        testcase.setTestcaseVersion("1");
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
	        String user_en_name = null;
	        try {
	        	user_en_name = (String)redisUtils.getCurrentUserInfoMap().get(Dict.USER_NAME_EN);//案例编写人
			} catch (Exception e) {
				throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
			} 
	        testcase.setTestcasePeople(user_en_name);
	        try {
	            int num = testcaseMapper.addTestcase(testcase);
	        } catch (Exception e) {
	        	logger.error("testcase add error,name:"+testcase.getTestcaseName());
	            e.printStackTrace();
	            throw new FtmsException(ErrorConstants.ADD_ERROR);
	        }
		
	}


	public String generateTestcaseNo(Testcase testcase) throws Exception{
	        if (Util.isNullOrEmpty(testcase)) {
	            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String [] {Dict.TESTCASE});
	        }
	        //系统id
	        String sysId = testcase.getSystemId().toString();
	        if (sysId.length()==1) {
				sysId = "00"+sysId;
			}else if (sysId.length()==2) {
				sysId = "0"+sysId;
			}
	        if (Util.isNullOrEmpty(sysId)) {
	            throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String [] {Dict.SYSID} );
	        }
	        String random = (int) ((Math.random() * 9 + 1) * 10000) + "";
	        String testcaseNo = null;
	        //生成 流水工单号
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        String testcaseDate = sdf.format(new Date());
	        testcaseNo =  testcaseDate+sysId+random;
	        return testcaseNo;
	    }

	/**
	 * 补充存量数据案例执行记录
 	 * @throws Exception
	 */
	@Override
	public void supplementTestcaseExeRecord() throws Exception {
		/**
		 * 补充步骤：
		 * 1、查询全量案例关系 也就是表ftms_plan_result中的数据
		 * 2、根据每条记录的执行次数，造出相应的执行记录
		 * 3、执行记录中的状态优先考虑 失败 及 阻塞状态，剩余的全部使用成功补充
		 */
		List<PlanlistTestcaseRelation> list = planlistTestcaseRelationMapper.selectAll();
		list.containsAll(list);
		int n = 0;
		for (PlanlistTestcaseRelation fpr : list) {
			n++;
			//未执行的案例不记录
			if(fpr.getTestcaseExecuteResult().equals("0")){
				continue;
			}
			int exeNum = fpr.getExeNum();
			if(exeNum == 1){
				TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
				testcaseExeRecord.setStatus(fpr.getTestcaseExecuteResult());
				testcaseExeRecord.setWorkNo(fpr.getWorkNo());
				testcaseExeRecord.setOpr(fpr.getFnlOpr());
				testcaseExeRecord.setDate(fpr.getFstTm());
				testcaseExeRecord.setFprId(fpr.getPlanlistTestcaseId().toString());
				testcaseExeRecord.setPlanId(fpr.getPlanId().toString());
				testcaseExeRecord.setTestcaseNo(fpr.getTestcaseNo());
				testcaseExeRecord.setTestcaseNo(Constants.TESTCASE_NEW);
				planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
				continue;
			}
			int failExeNum = 0;
			if(!Util.isNullOrEmpty(fpr.getFailExeNum())){
				failExeNum = fpr.getFailExeNum();
			}
			int blockExeNum = 0;
			if(!Util.isNullOrEmpty(fpr.getBlockExeNum())){
				blockExeNum = fpr.getBlockExeNum();
			}
			for (int i = 1 ; i<= exeNum; i++){
				if(failExeNum != 0){
					TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
					testcaseExeRecord.setStatus("3");
					testcaseExeRecord.setWorkNo(fpr.getWorkNo());
					testcaseExeRecord.setOpr(fpr.getFnlOpr());
					testcaseExeRecord.setDate(fpr.getFstTm());
					testcaseExeRecord.setFprId(fpr.getPlanlistTestcaseId().toString());
					testcaseExeRecord.setPlanId(fpr.getPlanId().toString());
					testcaseExeRecord.setTestcaseNo(fpr.getTestcaseNo());
					testcaseExeRecord.setTestcaseNo(Constants.TESTCASE_NEW);
					planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
					failExeNum --;
					continue;
				}else if(blockExeNum != 0){
					TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
					testcaseExeRecord.setStatus("2");
					testcaseExeRecord.setWorkNo(fpr.getWorkNo());
					testcaseExeRecord.setOpr(fpr.getFnlOpr());
					testcaseExeRecord.setDate(fpr.getFstTm());
					testcaseExeRecord.setFprId(fpr.getPlanlistTestcaseId().toString());
					testcaseExeRecord.setPlanId(fpr.getPlanId().toString());
					testcaseExeRecord.setTestcaseNo(fpr.getTestcaseNo());
					testcaseExeRecord.setTestcaseNo(Constants.TESTCASE_NEW);
					planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
					blockExeNum --;
					continue;
				}
				if(i< exeNum){
					TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
					testcaseExeRecord.setStatus("1");
					testcaseExeRecord.setWorkNo(fpr.getWorkNo());
					testcaseExeRecord.setOpr(fpr.getFnlOpr());
					testcaseExeRecord.setDate(fpr.getFstTm());
					testcaseExeRecord.setFprId(fpr.getPlanlistTestcaseId().toString());
					testcaseExeRecord.setPlanId(fpr.getPlanId().toString());
					testcaseExeRecord.setTestcaseNo(fpr.getTestcaseNo());
					testcaseExeRecord.setTestcaseNo(Constants.TESTCASE_NEW);
					planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
					continue;
				}
					//最后一个记录的是当前案例的状态
					TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
					testcaseExeRecord.setStatus(fpr.getTestcaseExecuteResult());
					testcaseExeRecord.setWorkNo(fpr.getWorkNo());
					testcaseExeRecord.setOpr(fpr.getFnlOpr());
					testcaseExeRecord.setDate(fpr.getFstTm());
					testcaseExeRecord.setFprId(fpr.getPlanlistTestcaseId().toString());
					testcaseExeRecord.setPlanId(fpr.getPlanId().toString());
					testcaseExeRecord.setTestcaseNo(fpr.getTestcaseNo());
					testcaseExeRecord.setTestcaseNo(Constants.TESTCASE_NEW);
					planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
			}

		}
		System.out.println("------------------"+n);
	}
}

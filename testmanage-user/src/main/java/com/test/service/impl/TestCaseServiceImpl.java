package com.test.service.impl;

import com.test.dao.TempUserDao;
import com.test.dao.TestCaseDao;
import com.test.dict.Dict;
import com.test.dict.ErrorConstants;
import com.test.service.MantisUserService;
import com.test.service.TestCaseService;
import com.test.service.UserService;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.Util;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestCaseServiceImpl implements TestCaseService{
	@Autowired
	private TestCaseDao testCaseDao;
	@Autowired
	private MantisUserService mantisUserService;
	private static final Logger logger = LoggerFactory.getLogger(TestCaseServiceImpl.class);
	@Autowired
	private UserService userService;
	@Autowired
	private TempUserDao tempUserDao;

	@Override
	public List<Map> countUserTestCaseByTime(String groupId, String startDate, String endDate, String username, boolean isParent) throws Exception {
		List<String> collect =  new ArrayList<>();
		if(Util.isNullOrEmpty(username)){
			List<Map> users = userService.queryGroupTester(groupId, isParent);
			if(Util.isNullOrEmpty(users)){
				return null;
			}
			collect = users.stream().map(e -> (String)e.get(Dict.USER_NAME_EN)).collect(Collectors.toList());
		}else{
			collect.add(username);
		}
		List<Map> list = testCaseDao.countUserTestCaseByTime(startDate + " 00:00:00", endDate + " 23:59:59", collect);
		Map<String, Integer> mantisMap = mantisUserService.countReporterSum(startDate, endDate, collect);
		//批量查询用户信息
		Map<String, Map> userInfoMap = userService.queryUserByNameEns(collect);
		List<String> groupIds = new ArrayList<>();
		for (Map userInfo : userInfoMap.values()) {
			groupIds.add((String) userInfo.get(Dict.GROUP_ID));
		}
		Map<String,String> groupNameMap = userService.queryGroupNameByIds(groupIds);
		for (Map map : list) {
			String userName = (String)map.get(Dict.USER_NAME);
			Integer issueNum = 0;
			if(!Util.isNullOrEmpty(mantisMap) && !Util.isNullOrEmpty(mantisMap.get(userName))){
				issueNum = mantisMap.get(userName);
			}
			map.put(Dict.USER_NAME_EN, userName);
			map.put(Dict.ISSUE_NUM, issueNum);
			Map user = userInfoMap.get(userName);
			if(!Util.isNullOrEmpty(user)){
				map.put(Dict.USER_NAME, user.get(Dict.USER_NAME_CN));
				map.put(Dict.GROUP_NAME, groupNameMap.get((String)user.get(Dict.GROUP_ID)));
			}
		}
		return list;
	}

	@Override
	public void exportExcelUser(String startDate, String endDate,String groupId, String username, boolean isParent, HttpServletResponse resp) throws Exception {
		List<Map> list = countUserTestCaseByTime( groupId, startDate, endDate, username, isParent);
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			setCellValue(workbook, 0, 0, 0, "用户名");
			setCellValue(workbook, 0, 0, 1, "姓名");
			setCellValue(workbook, 0, 0, 2, "小组");
			setCellValue(workbook, 0, 0, 3, "编写案例数");
			setCellValue(workbook, 0, 0, 4, "当前删除案例数");
			setCellValue(workbook, 0, 0, 5, "当前修改案例数");
			setCellValue(workbook, 0, 0, 6, "案例执行数");
			setCellValue(workbook, 0, 0, 7, "案例执行阻塞数");
			setCellValue(workbook, 0, 0, 8, "案例执行失败数");
			setCellValue(workbook, 0, 0, 9, "有效缺陷总数");
			int i = 1;
			for (Map map : list) {
				setCellValue(workbook, 0, i, 0, String.valueOf(map.get("user_name_en")));
				setCellValue(workbook, 0, i, 1, String.valueOf(map.get("user_name")));
				setCellValue(workbook, 0, i, 2, String.valueOf(map.get("group_name")));
				setCellValue(workbook, 0, i, 3, String.valueOf(map.get("add_num")));
				setCellValue(workbook, 0, i, 4, String.valueOf(map.get("delete_num")));
				setCellValue(workbook, 0, i, 5, String.valueOf(map.get("modify_num")));
				setCellValue(workbook, 0, i, 6, String.valueOf(map.get("exe_num")));
				setCellValue(workbook, 0, i, 7, String.valueOf(map.get("block_num")));
				setCellValue(workbook, 0, i, 8, String.valueOf(map.get("fail_num")));
				setCellValue(workbook, 0, i, 9, String.valueOf(map.get("issue_num")));
				i++;
			}
		} catch (Exception e) {
			logger.error("e"+e);
			throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
		}
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Disposition", "attachment;filename=" + "userReport.xlsx");
		workbook.write(resp.getOutputStream());
	}

	/**
	 * excel填值
	 * 
	 * @param workbook excel对象
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
	private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue)
			throws Exception {
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

}

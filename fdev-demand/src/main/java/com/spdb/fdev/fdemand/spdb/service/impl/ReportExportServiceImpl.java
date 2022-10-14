package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.service.ReportExportService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReportExportServiceImpl implements ReportExportService {
	
	private static final Logger logger = LoggerFactory.getLogger(ReportExportServiceImpl.class);
	
    @Override
    public String emailDemandExport(List<DemandBaseInfo> rqrmnt, List<Map> queryUser, String flag, String groupName, String filePath) {
        XSSFWorkbook workbook = null;
        String excelPath = "";//存放路径
        String filename = "需求超期通知.xlsx";//文件名
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.RELEASEDATE);
        if (!CommonUtils.isNullOrEmpty(flag)) {
            switch (flag) {
                case "expired-dev": filename = "需求计划启动通知-" +  groupName + sdf.format(new Date()) + ".xlsx";
                    break;
                case "expired-sit": filename = "需求计划提测通知-" +  groupName + sdf.format(new Date()) + ".xlsx";
                    break;
                case "overdue-dev": filename = "需求延期启动通知-" +  groupName + sdf.format(new Date()) + ".xlsx";
                    break;
                case "overdue-sit": filename = "需求延期提测通知-" +  groupName + sdf.format(new Date()) + ".xlsx";
                    break;
                default : filename = "需求超期通知.xlsx";
                    break;
            }
        }
        FileOutputStream output = null;
        ByteArrayOutputStream bs = null;
        try {
            workbook = new XSSFWorkbook();
            XSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "需求编号");
            setCellValue(workbook, 0, 0, 1, "需求名称");
            setCellValue(workbook, 0, 0, 2, "需求科技负责人");
            String content1 = "";
            if (!CommonUtils.isNullOrEmpty(flag)) {
                switch (flag) {
                    case "expired-dev": content1 = "计划启动日期";
                        break;
                    case "expired-sit": content1 = "计划提测日期";
                        break;
                    case "overdue-dev": content1 = "计划启动日期";
                        break;
                    case "overdue-sit": content1 = "计划提测日期";
                        break;
                    default : content1 = "";
                        break;
                }
            }
            setCellValue(workbook, 0, 0, 3, content1);//计划启动日期,计划提测日期
            String content2 = "";
            if (!CommonUtils.isNullOrEmpty(flag)) {
                switch (flag) {
                    case "expired-dev": content2 = "还剩几天延期";
                        break;
                    case "expired-sit": content2 = "还剩几天延期";
                        break;
                    case "overdue-dev": content2 = "延期天数";
                        break;
                    case "overdue-sit": content2 = "延期天数";
                        break;
                    default : content2 = "";
                        break;
                }
            }
            setCellValue(workbook, 0, 0, 4, content2);//还剩几天延期,延期天数
            int i = 1;
            String formatDate = CommonUtils.formatDate(CommonUtils.DATE_PARSE);//获取当前日期
            if(!CommonUtils.isNullOrEmpty(rqrmnt)) {
                for (DemandBaseInfo info : rqrmnt) {
                    String appendUserCn = CommonUtils.appendUserCn(info, queryUser);
                    setCellValue(workbook, 0, i, 0, info.getOa_contact_no());// 需求编号
                    setCellValue(workbook, 0, i, 1, info.getOa_contact_name());// 需求名称
                    if (CommonUtils.isNullOrEmpty(appendUserCn)) {
                        setCellValue(workbook, 0, i, 2, "无");
                    }else {
                        setCellValue(workbook, 0, i, 2, appendUserCn);// 需求科技负责人
                    }
                    String estDate = "";
                    if (!CommonUtils.isNullOrEmpty(flag)) {
                        switch (flag) {
                            case "expired-dev": estDate = CommonUtils.getSubstring(info.getPlan_start_date());
                                break;
                            case "expired-sit": estDate = CommonUtils.getSubstring(info.getPlan_inner_test_date());
                                break;
                            case "overdue-dev": estDate = CommonUtils.getSubstring(info.getPlan_start_date());
                                break;
                            case "overdue-sit": estDate = CommonUtils.getSubstring(info.getPlan_inner_test_date());
                                break;
                            default : estDate = "";
                                break;
                        }
                    }
                    setCellValue(workbook, 0, i, 3, estDate);// 计划日期
                    long countDay = 0;
                    if (!CommonUtils.isNullOrEmpty(flag)) {
                        if (!CommonUtils.isNullOrEmpty(estDate)) {
                            switch (flag) {
                                case "expired-dev": countDay = CommonUtils.countDay(estDate, formatDate);
                                    break;
                                case "expired-sit": countDay = CommonUtils.countDay(estDate, formatDate);
                                    break;
                                case "overdue-dev": countDay = CommonUtils.countDay(formatDate, estDate);
                                    break;
                                case "overdue-sit": countDay = CommonUtils.countDay(formatDate, estDate);
                                    break;
                                default : countDay = 0;
                                    break;
                            }
                        }
                    }
                    setCellValue(workbook, 0, i, 4, String.valueOf(countDay));// 延期天数
                    i++;
                }
            }
            String savePath = filePath + filename;
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
            excelPath = savePath;
        } catch (Exception e) {
        	logger.error("需求相关通知失败" + e.getMessage());
        } finally {
            try {
                output.flush();
                output.close();
                bs.close();
            } catch (IOException e) {
            	logger.error("需求相关通知失败" + e.getMessage());
            }
        }
        return excelPath;
    }

    /**
     * excel填值
     *
     * @param workbook
     *            excel对象
     * @param sheetIndex
     * @param rowIndex
     * @param cellIndex
     * @param cellValue
     * @throws Exception
     */
    private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue)	throws Exception {
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

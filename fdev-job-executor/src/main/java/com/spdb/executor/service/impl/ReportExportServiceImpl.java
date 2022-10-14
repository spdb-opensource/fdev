package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.service.ReportExportService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class ReportExportServiceImpl  implements ReportExportService {
    private Logger logger = LoggerFactory.getLogger(ReportExportServiceImpl.class);


    /**
     * 周统计---人力资源 导出
     */
    @Override
    public String weekUserResourceToPath(String filePath, Map<String, List<Map>> excelDate) {

        XSSFWorkbook workbook = null;
        String excelPath = filePath;//存放路径

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = "用户未来一周任务情况" + sdf.format(new Date()) + ".xlsx";  //文件名称

        try {
            workbook = new XSSFWorkbook();
            XSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            int sheetNum = 0;
            for (Map.Entry<String, List<Map>> item : excelDate.entrySet()) {
                Sheet sheet = workbook.createSheet(item.getKey()); //设置sheet
                setCellValue(workbook, sheetNum, 0, 0, "姓名");
                setCellValue(workbook, sheetNum, 0, 1, "公司");
                setCellValue(workbook, sheetNum, 0, 2, "小组");
                int dayline = 3;
                for(String day : DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM)){
                    setCellValue(workbook, sheetNum, 0, dayline++, day);
                }
                //写入数据
                List<Map> userDate = item.getValue();
                int index = 1;
                if(!CommonUtils.isNullOrEmpty(userDate)) {
                    for (Map info : userDate) {
                        dayline = 3;
                        setCellValue(workbook, sheetNum, index, 0, info.get(Dict.USER_NAME_CN).toString());// 用户中文名
                        setCellValue(workbook, sheetNum, index, 1, info.get(Dict.COMPANY).toString());// 公司
                        setCellValue(workbook, sheetNum, index, 2, info.get(Dict.GROUP).toString());// 组名
                        for(String day : DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM)){
                            setCellValue(workbook, sheetNum, index, dayline++, info.get(day).toString());
                        }
                        index++;
                    }
                }

                sheetNum++;
            }

            excelPath = saveExcelToPath(filePath, filename, workbook);

        } catch (Exception e) {
            logger.error("e:"+e);
            e.printStackTrace();
        }
        return excelPath;
    }

    /**
     * 周统计---组资源 导出
     */
    @Override
    public String weekGroupResourceToPath(String filePath, Map<String, List<Map>> excelDate) {

        XSSFWorkbook workbook = null;
        String excelPath = filePath;//存放路径

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = "组未来一周任务情况" + sdf.format(new Date()) + ".xlsx";  //文件名称

        try {
            workbook = new XSSFWorkbook();
            XSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            int sheetNum = 0;
            for (Map.Entry<String, List<Map>> item : excelDate.entrySet()) {
                Sheet sheet = workbook.createSheet(item.getKey()); //设置sheet
                setCellValue(workbook, sheetNum, 0, 0, "组名");
                setCellValue(workbook, sheetNum, 0, 1, "小组总的"+item.getKey()+"资源数量");
                int dayline = 2;
                for(String day : DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM)){
                    setCellValue(workbook, sheetNum, 0, dayline++, day);
                }
                //写入数据
                List<Map> userDate = item.getValue();
                int index = 1;//起始写入行
                if(!CommonUtils.isNullOrEmpty(userDate)) {
                    for (Map info : userDate) {
                        dayline = 2;
                        setCellValue(workbook, sheetNum, index, 0, info.get(Dict.NAME).toString());// 组名
                        setCellValue(workbook, sheetNum, index, 1, info.get("totalUser").toString());// 组名
                        for(String day : DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM)){
                            setCellValue(workbook, sheetNum, index, dayline++, info.get(day).toString());
                        }
                        index++;
                    }
                }

                sheetNum++;
            }

            excelPath = saveExcelToPath(filePath, filename, workbook);
        } catch (Exception e) {
            logger.error("e:"+e);
            e.printStackTrace();
        }
        return excelPath;
    }


    /**
     * excel填值
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


    /**
     * 写文件到 指定的目录，返回存储路径
     */
    private String saveExcelToPath(String filePath, String fileName, XSSFWorkbook workbook){
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
            return savePath;
        }catch (Exception e) {
            logger.error("saveExcelToPath error:"+e);
            e.printStackTrace();
        } finally {
            try {
                output.flush();
                output.close();
                bs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return savePath;
    }

}

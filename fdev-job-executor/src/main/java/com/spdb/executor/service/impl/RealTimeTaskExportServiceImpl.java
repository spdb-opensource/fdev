package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.RealTimeTaskExportService;
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

@Service
public class RealTimeTaskExportServiceImpl implements RealTimeTaskExportService {

    private static Logger logger = LoggerFactory.getLogger(RealTimeTaskExportServiceImpl.class);


    @Override
    public String realTimeTaskExport(List<Map> creatorRealTimeTasks, List<Map> testerRealTimeTasks, List<Map> developRealTimeTasks, List<Map> masterRealTimeTasks, List<Map> spdb_managerRealTimeTasks, String filePath) {
        XSSFWorkbook workbook = null;
        String excelPath = "";//存放路径
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = "全量人员的实时任务数量" + sdf.format(new Date()) + ".xlsx";
        FileOutputStream output = null;
        ByteArrayOutputStream bs = null;
        try {
            workbook = new XSSFWorkbook();
            XSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            //创建人，开发人员，测试人员，行内负责人，任务负责人
            workbook.createSheet("创建人");
            workbook.createSheet("开发人员");
            workbook.createSheet("测试人员");
            workbook.createSheet("行内负责人");
            workbook.createSheet("任务负责人");
            String role = "";
            for (int i = 0; i < 5; i++) {
                setCellValue(workbook, i, 0, 0, "姓名");
                setCellValue(workbook, i, 0, 1, "公司");
                setCellValue(workbook, i, 0, 2, "小组");
                setCellValue(workbook, i, 0, 3, "待实施任务数量");
                setCellValue(workbook, i, 0, 4, "开发中任务数量");
                setCellValue(workbook, i, 0, 5, "SIT任务数量");
                setCellValue(workbook, i, 0, 6, "UAT任务数量");
                setCellValue(workbook, i, 0, 7, "REL任务数量");
                setCellValue(workbook, i, 0, 8, "已投产任务数量");
                if (i == 0) {
                    role = Dict.CREATOR;
                }
                if (i == 1) {
                    role = Dict.DEVELOPER;
                }
                if (i == 2) {
                    role = Dict.TESTER;
                }
                if (i == 3) {
                    role = Dict.SPDB_MASTER;
                }
                if (i == 4) {
                    role = Dict.MASTER;
                }
                switch (role) {
                    case Dict.CREATOR:
                        saveData(workbook, creatorRealTimeTasks, 0);
                    case Dict.DEVELOPER:
                        saveData(workbook, developRealTimeTasks, 1);
                    case Dict.TESTER:
                        saveData(workbook, testerRealTimeTasks, 2);
                    case Dict.SPDB_MASTER:
                        saveData(workbook, spdb_managerRealTimeTasks, 3);
                    case Dict.MASTER:
                        saveData(workbook, masterRealTimeTasks, 4);
                        break;
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
        return excelPath;

    }

    private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue) throws Exception {
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

    private void saveData(XSSFWorkbook workbook, List<Map> realTimeTask, int i) throws Exception {
        int s = 1;
        for (Map map : realTimeTask) {
            setCellValue(workbook, i, s, 0, String.valueOf(map.get(Dict.NAME_CN)));//姓名
            setCellValue(workbook, i, s, 1, String.valueOf(map.get(Dict.COMPANY)));//公司
            setCellValue(workbook, i, s, 2, String.valueOf(map.get(Dict.GROUP)));//小组
            setCellValue(workbook, i, s, 3, String.valueOf(map.get(Dict.TODO)));//待实施任务数量
            setCellValue(workbook, i, s, 4, String.valueOf(map.get(Dict.DEVELOP)));//开发中任务数量
            setCellValue(workbook, i, s, 5, String.valueOf(map.get(Dict.SIT)));//sit任务数量
            setCellValue(workbook, i, s, 6, String.valueOf(map.get(Dict.UAT)));//uat任务数量
            setCellValue(workbook, i, s, 7, String.valueOf(map.get(Dict.REL)));//rel任务数量
            setCellValue(workbook, i, s, 8, String.valueOf(map.get(Dict.PRODUCTION)));//已投产任务数量
            s++;
        }
    }
}

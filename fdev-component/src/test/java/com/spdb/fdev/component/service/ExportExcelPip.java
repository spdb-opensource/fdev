package com.spdb.fdev.component.service;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ExportExcelPip {
    /***
     * 构造方法
     */
    public ExportExcelPip() {

    }

    /***
     * 工作簿
     */
    private static HSSFWorkbook workbook;

    /***
     * sheet
     */
    private static HSSFSheet sheet;
    /***
     * 标题行开始位置
     */
    private static final int TITLE_START_POSITION = 0;

    /***
     * 时间行开始位置
     */
    private static final int DATEHEAD_START_POSITION = 1;

    /***
     * 表头行开始位置
     */
    private static final int HEAD_START_POSITION = 2;

    /***
     * 文本行开始位置
     */
    private static final int CONTENT_START_POSITION = 3;


    /**
     * @param dataList  对象集合
     * @param titleMap  表头信息（对象属性名称->要显示的标题值)[按顺序添加]
     * @param sheetName sheet名称和表头值
     */
    public static void excelExport(List<Map> dataList, Map<String, String> titleMap, String sheetName) {
        // 初始化workbook
        initHSSFWorkbook(sheetName);
        // 标题行
        createTitleRow(titleMap, sheetName);
        // 时间行
        createDateHeadRow(titleMap);
        // 表头行
        createHeadRow(titleMap);
        // 文本行
        createContentRow(dataList);
        //设置自动伸缩
        autoSizeColumn(titleMap.size());
        // 写入处理结果
        try {
            String filename = "组件使用汇总" + ".xls";
            OutputStream out = new FileOutputStream("D:\\java\\" + filename);
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     *
     * @param sheetName
     *        sheetName
     */
    private static void initHSSFWorkbook(String sheetName) {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet(sheetName);
    }

    /**
     * 生成标题（第零行创建）
     *
     * @param titleMap  对象属性名称->表头显示名称
     * @param sheetName sheet名称
     */
    private static void createTitleRow(Map<String, String> titleMap, String sheetName) {
        CellRangeAddress titleRange = new CellRangeAddress(0, 0, 0, titleMap.size() - 1);
        sheet.addMergedRegion(titleRange);
        HSSFRow titleRow = sheet.createRow(TITLE_START_POSITION);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetName);
    }

    /**
     * 创建时间行（第一行创建）
     *
     * @param titleMap 对象属性名称->表头显示名称
     */
    private static void createDateHeadRow(Map<String, String> titleMap) {
        CellRangeAddress dateRange = new CellRangeAddress(1, 1, 0, titleMap.size() - 1);
        sheet.addMergedRegion(dateRange);
        HSSFRow dateRow = sheet.createRow(DATEHEAD_START_POSITION);
        HSSFCell dateCell = dateRow.createCell(0);
        dateCell.setCellValue(new SimpleDateFormat("yyyy年MM月dd日").format(new Date()));
    }

    /**
     * 创建表头行（第二行创建）
     *
     * @param titleMap 对象属性名称->表头显示名称
     */
    private static void createHeadRow(Map<String, String> titleMap) {
        // 第1行创建
        HSSFRow headRow = sheet.createRow(HEAD_START_POSITION);
        int i = 0;
        for (Map.Entry<String, String> entry : titleMap.entrySet()) {
            HSSFCell headCell = headRow.createCell(i);
            headCell.setCellValue(entry.getValue());
            i++;
        }
    }

    /**
     * @param dataList 对象数据集合
     */
    private static void createContentRow(List<Map> dataList) {
        try {
            int i = 0;
            for (Map map : dataList) {
                HSSFRow textRow = sheet.createRow(CONTENT_START_POSITION + i);
                HSSFCell oneCell = textRow.createCell(0);//path
                oneCell.setCellValue((String) map.get("app_name_cn"));
                HSSFCell twoCell = textRow.createCell(1);//项目名
                twoCell.setCellValue((String) map.get("app_name_en"));
                HSSFCell threeCell = textRow.createCell(2);//项目Id
                threeCell.setCellValue((String) map.get("dev"));
                HSSFCell fourCell = textRow.createCell(3);//web_url
                fourCell.setCellValue((String) map.get("spdb"));
                HSSFCell fiveCell = textRow.createCell(4);//full_path
                fiveCell.setCellValue((String) map.get("name_cn"));
                HSSFCell sixCell = textRow.createCell(5);//namespace
                sixCell.setCellValue((String) map.get("name_en"));
                HSSFCell sevenCell = textRow.createCell(6);//namespace
                sevenCell.setCellValue((String) map.get("manager"));
                HSSFCell eightlCell = textRow.createCell(7);//namespace
                eightlCell.setCellValue((String) map.get("component_version"));
                HSSFCell nineCell = textRow.createCell(8);//namespace
                nineCell.setCellValue((String) map.get("type_name"));
                HSSFCell tenCell = textRow.createCell(9);//namespace
                tenCell.setCellValue((String) map.get("group_name"));
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动伸缩列（耗内存）
     *
     * @param size 列数
     */
    private static void autoSizeColumn(Integer size) {
        for (int j = 0; j < size; j++) {
            sheet.autoSizeColumn(j);
        }
    }
}

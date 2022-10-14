package com.spdb.fdev.gitlabwork.service.impl;

import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * ExportExcelGitlabWork
 *
 * @blame Android Team
 */
public class ExportExcelGitlabWork {
    /***
     * 构造方法
     */
    public ExportExcelGitlabWork() {

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
    public static void excelExport(String startDate, String endDate, List<GitlabWork> dataList, Map<String, String> titleMap, String sheetName, HttpServletResponse response) {
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
        //autoSizeColumn(titleMap.size());
        // 写入处理结果
        try {
            String filename = startDate + "-" + endDate + "Gitlab代码提交汇总" + ".xls";
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/x-download");
            response.addHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(filename, "UTF-8"));
            OutputStream out = response.getOutputStream();
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
    private static void createContentRow(List<GitlabWork> dataList) {
        try {
            int i = 0;
            for (GitlabWork gitlabWork : dataList) {
                HSSFRow textRow = sheet.createRow(CONTENT_START_POSITION + i);
                HSSFCell nameCell = textRow.createCell(0);//姓名
                nameCell.setCellValue(gitlabWork.getNickName());
                HSSFCell companyCell = textRow.createCell(1);//所属公司
                companyCell.setCellValue(gitlabWork.getCompanyname());
                HSSFCell groupCell = textRow.createCell(2);//组别
                groupCell.setCellValue(gitlabWork.getGroupname());
                HSSFCell roleCell = textRow.createCell(3);//角色
                roleCell.setCellValue(gitlabWork.getRolename());
                HSSFCell totalCell = textRow.createCell(4);//总行数
                totalCell.setCellValue(gitlabWork.getTotal());
                HSSFCell additionsCell = textRow.createCell(5);//添加行数
                additionsCell.setCellValue(gitlabWork.getAdditions());
                HSSFCell deletionsCell = textRow.createCell(6);//删除行数
                deletionsCell.setCellValue(gitlabWork.getDeletions());
                HSSFCell startDate = textRow.createCell(7);//开始日期
                startDate.setCellValue(gitlabWork.getStartDate());
                HSSFCell endDate = textRow.createCell(8);//结束日期
                endDate.setCellValue(gitlabWork.getEndDate());
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

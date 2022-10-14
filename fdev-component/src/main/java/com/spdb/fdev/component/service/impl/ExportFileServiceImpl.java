package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.service.IExportFileService;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExportFileServiceImpl implements IExportFileService {

    /***
     * 表头行开始位置
     */
    private final int HEAD_START_POSITION = 0;

    /***
     * 文本行开始位置
     */
    private final int CONTENT_START_POSITION = 1;


    @Override
    public void exportExcelByComponent(List<Map> dataList, Map<String, String> titleMap, String sheetName, HttpServletResponse resp) {
        // 初始化workbook
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);
        // 表头行
        HSSFRow headRow = sheet.createRow(HEAD_START_POSITION);
        headRow.createCell(0).setCellValue((String) titleMap.get(Dict.NAME_CN));   //组件中文名
        headRow.createCell(1).setCellValue((String) titleMap.get(Dict.NAME_EN));   //组件英文名
        headRow.createCell(2).setCellValue((String) titleMap.get(Dict.MANAGER));   //组件负责人
        headRow.createCell(3).setCellValue((String) titleMap.get(Dict.COMPONENT_VERSION)); //组件版本
        headRow.createCell(4).setCellValue((String) titleMap.get(Dict.TYPE_NAME));     //组件版本类型
        headRow.createCell(5).setCellValue((String) titleMap.get(Dict.APP_NAME_CN));   //应用中文名
        headRow.createCell(6).setCellValue((String) titleMap.get(Dict.APP_NAME_EN));   //应用英文名
        headRow.createCell(7).setCellValue((String) titleMap.get(Dict.DEV));       //应用负责人
        headRow.createCell(8).setCellValue((String) titleMap.get(Dict.SPDB));      //应用行内负责人
        headRow.createCell(9).setCellValue((String) titleMap.get(Dict.GROUP_NAME));    //项目所属小组

        // 文本行
        int i = 0;
        for (Map map : dataList) {
            HSSFRow textRow = sheet.createRow(CONTENT_START_POSITION + i);
            textRow.createCell(0).setCellValue((String) map.get(Dict.NAME_CN));
            textRow.createCell(1).setCellValue((String) map.get(Dict.NAME_EN));
            textRow.createCell(2).setCellValue((String) map.get(Dict.MANAGER));
            textRow.createCell(3).setCellValue((String) map.get(Dict.COMPONENT_VERSION));
            textRow.createCell(4).setCellValue((String) map.get(Dict.TYPE_NAME));
            textRow.createCell(5).setCellValue((String) map.get(Dict.APP_NAME_CN));
            textRow.createCell(6).setCellValue((String) map.get(Dict.APP_NAME_EN));
            textRow.createCell(7).setCellValue((String) map.get(Dict.DEV));
            textRow.createCell(8).setCellValue((String) map.get(Dict.SPDB));
            textRow.createCell(9).setCellValue((String) map.get(Dict.GROUP_NAME));
            i++;
        }
        //设置自动伸缩
//        autoSizeColumn(sheet, titleMap.size());
        // 写入处理结果
        try {
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" + "componentApplication.xls");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通用导出方法
     *
     * @param dataList
     * @param titleMap
     * @param sheetName
     * @param resp
     */
    @Override
    public void exportExcel(List<Map> dataList, Map<Integer, String> titleMap, String sheetName, HttpServletResponse resp, String fileName) {
        // 初始化workbook
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);
        // 表头行
        HSSFRow headRow = sheet.createRow(HEAD_START_POSITION);
        Set<Map.Entry<Integer, String>> entries = titleMap.entrySet();
        if (!CommonUtils.isNullOrEmpty(entries)) {
            for (int i = 0; i < entries.size(); i++) {
                headRow.createCell(i).setCellValue(titleMap.get(i).split("--")[1]);
            }
        }
        // 文本行
        if (!CommonUtils.isNullOrEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); i++) {
                Map map = dataList.get(i);
                HSSFRow textRow = sheet.createRow(CONTENT_START_POSITION + i);
                if (!CommonUtils.isNullOrEmpty(entries)) {
                    for (int j = 0; j < entries.size(); j++) {
                        textRow.createCell(j).setCellValue((String) map.get(titleMap.get(j).split("--")[0]));
                    }
                }
            }
        }
        //设置自动伸缩
        //autoSizeColumn(sheet, titleMap.size());
        // 写入处理结果
        try {
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 自动伸缩列（耗内存）
     *
     * @param size 列数
     */
    private void autoSizeColumn(HSSFSheet sheet, Integer size) {
        for (int j = 0; j < size; j++) {
            sheet.autoSizeColumn(j);
        }
    }

}

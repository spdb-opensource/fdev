package com.manager.ftms.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.lang.StringUtils;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xxx on 2018/1/18.
 */
public class PoiExcelUtil {

    public static final String XLS_SUFFIX = "xls";
    public static final String XLSX_SUFFIX = "xlsx";

    /**
     * 传入文件地址，解析excel数据
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param colMapped 列名映射（表头列名-数据库列名）
     * @return Map<String,List<Map>>
     */
    public  static Map<Integer, List<Map<String, Object>>> getExcelData(String filePath, String fileName, Map<String, String> colMapped){
        //file 转 stream
        if(isNullOrEmpty(filePath) || isNullOrEmpty(fileName)){
            throw new RuntimeException("excel文件名为空");
        }
        File path = new File(filePath);
        if(!path.exists()){
            throw new RuntimeException("excel文件路径不存在");
        }
        String s1 = File.pathSeparator;
        String s2 = File.separator;
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()-1);
        if(fileName.contains(File.separator) || fileName.contains("..")){
            throw new RuntimeException("文件名非法");
        }
        if(XLS_SUFFIX.equalsIgnoreCase(suffix) || XLSX_SUFFIX.equalsIgnoreCase(suffix)){
            File excel = new File(filePath + File.separator + fileName);
            try(FileInputStream inputStream = new FileInputStream(excel)) {
                return getExcelData(inputStream, colMapped);
            } catch (IOException e) {
                throw new RuntimeException("文件不存在",e);
            }
        }else{
            throw new RuntimeException("不支持的文件类型");
        }
    }

    /**
     * 根据传入的输入流，解析其excel数据并返回
     * @param inputStream excel输入流
     * @param colMapped 列名映射（表头列名-数据库列名）
     * @return Map<String,List<Map>>
     */
    public static Map<Integer, List<Map<String, Object>>> getExcelData(InputStream inputStream, Map<String, String> colMapped){
        Workbook workbook = null;
        Map<Integer, List<Map<String, Object>>> result = new HashMap<Integer, List<Map<String, Object>>>();
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("创建workbook失败",e);
        } catch (InvalidFormatException e) {
            throw new RuntimeException("创建workbook失败",e);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int sheets = workbook.getNumberOfSheets();
        for(int i=0;i<sheets;i++){
            List<Map<String, Object>> sheet = getSheetData(workbook.getSheetAt(i), colMapped);
            result.put(i,sheet);
        }
        return result;
    }

    /**
     * 给定excel的sheet表，解析其数据并返回
     * @param sheet sheet工作表
     * @param colMapped 列名映射（表头列名-数据库列名）
     * @return List<Map>
     */
    private static List<Map<String, Object>> getSheetData(Sheet sheet, Map<String, String> colMapped){
        if(isSheetEmpty(sheet)){
            return null;
        }
        Map<String,String> colTransMapped = colTransMapped(sheet,colMapped);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        int rowNum = sheet.getLastRowNum();
        for(int i=1;i<=rowNum;i++){
            Row row = sheet.getRow(i);
            if(row==null){
                continue;
            }
            result.add(getRowData(row,colTransMapped));
        }
        return result;
    }

    /**
     * 转换列名，将数据下标与列名对应
     * @param sheet sheet工作表
     * @param colMap 列名映射（表头列名-数据库列名）
     * @return Map<String,String>
     */
    private static Map<String,String> colTransMapped(Sheet sheet , Map<String,String> colMap){
        Row head = sheet.getRow(0);
        Map<String,String> result = new HashMap<String, String>();

        //
        int ii = head.getLastCellNum();

        for(int i=0;i<head.getLastCellNum();i++){
            Cell cell = head.getCell(i);
            if(cell==null){
                continue;
            }
            Object cellValue = parseCellData(cell);
            //如果表格中列名没有找到配置，则不取。
            String dbCol = colMap.get(cellValue.toString());
            if(dbCol == null || "".equalsIgnoreCase(dbCol)){
                continue;
            }
            result.put(String.valueOf(i),dbCol);
        }
        return result;
    }

    /**
     * 获取某行数据
     * @param row 行
     * @param colTransMapped 列名映射（表头列名-数据库列名）
     * @return Map<String,Object>
     */
    private static Map<String,Object> getRowData(Row row, Map<String,String> colTransMapped){
        int colNum = row.getLastCellNum();
        Map<String,Object> rowData = new HashMap<String, Object>();
        for(int c=0;c<colNum;c++){
            Cell cell = row.getCell(c);
            if(cell == null){
                continue;
            }
            String key = colTransMapped.get(String.valueOf(c));
            if(key == null || "".equals(key)){
                continue;
            }
            rowData.put(key, parseCellData(cell));
        }
        return rowData;
    }

    /**
     * 获取单元格数据，包括日期格式
     * @param cell 单元格
     * @return Object
     */
    private static Object parseCellData(Cell cell){
        Object result = null;
        switch(cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)){
                    SimpleDateFormat simpleDateFormat = null;
                    if(cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd")){
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    }else{
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }
                    Date date = cell.getDateCellValue();
                    result = simpleDateFormat.format(date);
                }else if(cell.getCellStyle().getDataFormat()==31 || cell.getCellStyle().getDataFormat()==14){// yyyy年MM月dd日 格式
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    double value = cell.getNumericCellValue();
                    Date date = DateUtil.getJavaDate(value);
                    simpleDateFormat.format(date);
                }else{
                    Double value = cell.getNumericCellValue();
                    DecimalFormat format = new DecimalFormat();
                    CellStyle style = cell.getCellStyle();
                    String temp = style.getDataFormatString();
                    if("General".equalsIgnoreCase(temp)){
                        format.applyPattern("#");
                    }
                    result = format.format(value);
                }
                break;
            case Cell.CELL_TYPE_STRING:
                result = StringUtils.trimToNull(cell.getRichStringCellValue().toString());
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    /**
     * sheet表格是否为空
     * @param sheet
     * @return
     */
    private static boolean isSheetEmpty(Sheet sheet){
        if(sheet == null || sheet.getLastRowNum()==0){
            return true;
        }
        return false;
    }


    /**
     * 判断变量是否为空
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj){
        if(obj == null){
            return true;
        }
        if(obj instanceof String){
            return "".equals(obj);
        }
        if(obj instanceof List){
            return 0 == ((List)obj).size();
        }
        if(obj instanceof Map){
            return 0 == ((Map)obj).size();
        }
        return false;
    }
    /**
     * 输入数据集，获取工作簿
     * @param data
     * @param colKey
     * @return
     */
    public static Workbook writeDataMenus(List<Map<String,Object>> data, List<String> colKey,List<String> colZh){
        Workbook workbook = writeData(data, colKey, colZh, 300);
        return workbook;
    }   

    /**
     * 输入数据集，获取工作簿
     * @param data
     * @param colKey
     * @return
     */
    public static Workbook writeData(List<Map<String,Object>> data, List<String> colKey,List<String> colZh){
        Workbook workbook = writeData(data, colKey, colZh, 2000);
        return workbook;
    }

    /**
     * 获取数据集 减少代码重复率
     */
    private static Workbook writeData(List<Map<String, Object>> data, List<String> colKey, List<String> colZh, int num){
        Workbook workbook = new XSSFWorkbook();
        CellStyle style = getCellStyle(workbook);
        Sheet sheet1 = workbook.createSheet("Sheet1");
        //根据传入格式生成表头
        Row head = sheet1.createRow(0);
        CellStyle headStyle = getHeadStyle(workbook);
        for(int j=0;j<colZh.size();j++){
            Cell cell = head.createCell(j);
            String value = colZh.get(j);
            setData(cell,value,headStyle);
        }
        for(int i=1;i<data.size()+1;i++){
            Row row = sheet1.createRow(i);
            row.setHeight((short)(num));
            Map<String,Object> rowData = data.get(i-1);
            for(int j=0;j<colKey.size();j++){
                Cell cell = row.createCell(j);
                String key = colKey.get(j);
                Object value = rowData.get(key);
                setData(cell,value,style);
            }
        }
        //重设列宽，但是因为该功能只能针对数字和字母计算列宽，中文情况下宽度不足，此处补充6个字符的宽度，满足表头不至于太窄。其他情况需要另外考虑。
        for(int j=0;j<colZh.size();j++) {
            sheet1.autoSizeColumn(j, true);
            sheet1.setColumnWidth(j, sheet1.getColumnWidth(j) + 6 * 256);
        }
        return workbook;
    }

    /**
     * 将数据写入输出流中
     * @param outputStream 接收数据的输出流
     * @param data 数据
     * @param colKey 数据map的key
     * @param colZh 数据map的key对应的中文，将作为表头存在。
     */
    public static void writeData2Stream(OutputStream outputStream, List<Map<String,Object>> data, List<String> colKey, List<String> colZh){
        Workbook workbook = writeDataMenus(data, colKey, colZh);
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断数据格式，将数据设置到单元格中
     * @param cell
     * @param obj
     */
    private static void setData(Cell cell, Object obj, CellStyle style) {
        cell.setCellStyle(style);
        if (obj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String value = sdf.format((Date)obj);
            cell.setCellValue(value);
        }else if(obj instanceof String){
            cell.setCellValue(((String)obj).trim());
        }else if(obj instanceof Boolean){
            cell.setCellValue((Boolean)obj);
        }else if(obj instanceof Double){
            cell.setCellValue((Double)obj);
        }else{
            cell.setCellValue("");
        }
    }

    /**
     * 传入数据 表头格式和文件路径，写出文件。
     * @param data
     * @param filePath
     * @param fileName
     * @param colKey
     */
    public static void writeData2File(List<Map<String,Object>> data, String filePath, String fileName, List<String> colKey, List<String> colZh) throws Exception {
        //file 转 stream
        if(isNullOrEmpty(filePath) || isNullOrEmpty(fileName)){
            throw new RuntimeException("excel文件名为空");
        }
        File path = new File(filePath);
        if(!path.exists()){
            throw new RuntimeException("excel文件路径不存在");
        }
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length());
        if(fileName.contains(File.separator) || fileName.contains("..")){
            throw new RuntimeException("文件名非法");
        }
        if(XLS_SUFFIX.equalsIgnoreCase(suffix) || XLSX_SUFFIX.equalsIgnoreCase(suffix)){
            File excel = new File(filePath + File.separator + fileName);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(excel);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("文件不存在",e);
            }
            Workbook workbook = writeData(data,colKey,colZh);
            try {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream!=null){
                    outputStream.close();
                }
            }
        }else{
            throw new RuntimeException("不支持的文件类型");
        }

    }

    /**
     * 封装表头样式，设置边框，对齐居中，设置字体加粗，背景色填充等……
     * @param workbook
     * @return
     */
    private static CellStyle getHeadStyle(Workbook workbook){
        CellStyle styleHead = workbook.createCellStyle();
        styleHead.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleHead.setBorderRight(HSSFCellStyle.BORDER_THIN);
        styleHead.setBorderTop(HSSFCellStyle.BORDER_THIN);
        styleHead.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleHead.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        styleHead.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        styleHead.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleHead.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
        return styleHead;
    }

    /**
     * 封装单元格样式，添加边框并且设置对其为左上。
     * @param workbook
     * @return
     */
    private static CellStyle getCellStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        short df = workbook.createDataFormat().getFormat("");
        style.setDataFormat(df);
        return style;
    }

}

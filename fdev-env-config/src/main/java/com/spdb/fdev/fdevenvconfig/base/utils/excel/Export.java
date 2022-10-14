package com.spdb.fdev.fdevenvconfig.base.utils.excel;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/8/5 15:19
 */
@Component
@RefreshScope
public class Export {

    private static String tmpPath;

    @Value("${path.excel.save}")
    public void setTmpPath(String tmpPath) {
        Export.tmpPath = tmpPath;
    }

    private static Logger logger = LoggerFactory.getLogger(Export.class);

    /**
     * 公用的导出方法
     *
     * @param headers   表头
     * @param excelPath 表格存放路径
     * @param sheetName 表格页名称
     * @param maps      数据
     */
    public static void export(String[] headers, String excelPath, String sheetName, List<Map> maps) {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet(sheetName);
            try {
                File tf = new File(excelPath);
                File fp = tf.getParentFile();// tmp
                if (!fp.exists()) {
                    if (!fp.mkdirs()) {
                        logger.error("创建 {} 目录失败", excelPath);
                        throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"创建目录失败"});
                    }
                }
                File file = new File(tmpPath);
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File ff : files) {
                        if (!ff.delete()) {
                            logger.error("删除 {} 文件失败", tmpPath);
                            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + tmpPath + "文件失败"});
                        }
                    }
                    if (!tf.createNewFile()) {
                        logger.error("创建 {} 文件失败", excelPath);
                        throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                    }
                } else {
                    if (!tf.createNewFile()) {
                        logger.error("创建 {} 文件失败", excelPath);
                        throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"创建" + excelPath + " 文件失败"});
                    }
                }
                tableHeader(headers, workbook, sheet);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"生成文件失败\n" + e.toString()});
            }
            // 做数据处理
            int n = 1;
            for (Map map : maps) {
                tableBody(n, map, sheet, headers);
                n++;
            }
            // 写入
            try (OutputStream out = new FileOutputStream(excelPath)) {
                workbook.write(out);
                out.flush();
            }
        } catch (IOException e) {

            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"导出文件失败\n" + e.toString()});
        }
    }

    /**
     * excel表体填充
     *
     * @param n       excel行
     * @param map     数据
     * @param sheet   sheet
     * @param headers
     */
    private static void tableBody(int n, Map map, HSSFSheet sheet, String[] headers) {
        HSSFRow row = sheet.createRow(n);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            if (i == 0) {
                cell.setCellValue((String) map.get(Dict.NAME_ZH));
            } else if (i == 1) {
                cell.setCellValue((String) map.get(Dict.NAME_EN));
            } else if (i == 2) {
                List<Map> mapdata = (List<Map>) map.get(Dict.DEV_MANAGERS);
                StringBuilder stringBuilder = new StringBuilder();
                for (Map<String, String> mapdatum : mapdata) {
                    stringBuilder.append(mapdatum.get(Dict.USER_NAME_CN)).append(",");
                }
                String devManagerName = stringBuilder.toString();
                if (StringUtils.isNotEmpty(devManagerName)) {
                    devManagerName = devManagerName.substring(0, devManagerName.length() - 1);
                }
                cell.setCellValue(devManagerName);
            } else if (i == 3) {
                List<Map> mapdata = (List<Map>) map.get(Dict.SPDB_MANAGERS);
                StringBuilder stringBuilder = new StringBuilder();
                for (Map<String, String> mapdatum : mapdata) {
                    stringBuilder.append(mapdatum.get(Dict.USER_NAME_CN)).append(",");
                }
                String spdbManagerName = stringBuilder.toString();
                if (StringUtils.isNotEmpty(spdbManagerName)) {
                    spdbManagerName = spdbManagerName.substring(0, spdbManagerName.length() - 1);
                }
                cell.setCellValue(spdbManagerName);
            } else if (i == 4) {
                cell.setCellValue((String) map.get(Dict.GROUP));
            } else if (i == 5) {
                cell.setCellValue((String) map.get(Dict.GIT));
            } else if (i == 6) {
                cell.setCellValue((String) map.get(Dict.BRANCH));
            }

        }
    }

    /**
     * 创建表头
     *
     * @param headers  表头字段
     * @param workbook workbook
     * @param sheet    表格页名称
     */
    private static void tableHeader(String[] headers, HSSFWorkbook workbook, HSSFSheet sheet) {
        sheet.setDefaultColumnWidth((short) 20); // 设置表格默认列宽度为15个字节
        HSSFCellStyle style = workbook.createCellStyle(); // 生成一个样式
        // 设置样式
        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.VIOLET.index);
        font.setFontHeightInPoints((short) 12);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            String textValue = text.toString();
            cell.setCellValue(textValue);
        }
    }

    /**
     * 下载excel
     *
     * @param fileName  excel名称
     * @param excelPath excel路径
     * @param response  请求头
     */
    public static void commonDown(String fileName, String excelPath, HttpServletResponse response) throws Exception {
        File file = new File(excelPath);
        if (!file.exists()) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"{} 文件不存在", fileName});
        }
        try (OutputStream output = response.getOutputStream();
             FileInputStream in = new FileInputStream(excelPath)) {
            // 读取文件
            response.reset();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "content-type");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            response.flushBuffer();
            logger.info("导出成功");
        } catch (Exception e2) {
            logger.error("导出失败: {}", e2.getMessage());
        }
        File execle = new File(excelPath);
        if (!execle.delete()) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + excelPath + "文件失败"});
        }
    }

    /**
     * 下载world
     *
     * @param fileName  world名称
     * @param worldpath world路径
     * @param response  请求头
     */
    public static void downloadWorld(String fileName, String worldpath, HttpServletResponse response) throws Exception {
        File file = new File(worldpath);
        if (!file.exists()) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"{} 文件不存在", fileName});
        }
        try (OutputStream output = response.getOutputStream();
             FileInputStream in = new FileInputStream(worldpath)) {
            // 读取文件
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/msword");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            response.flushBuffer();
            logger.info("导出成功");
        } catch (Exception e2) {
            logger.error("导出失败: {}", e2.getMessage());
        }
        File execle = new File(worldpath);
        if (!execle.delete()) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{"删除" + execle + "文件失败"});
        }
    }

}

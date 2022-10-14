package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.dao.DatabaseDao;
import com.fdev.database.spdb.dao.TableInfoDao;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.entity.Database;
import com.fdev.database.spdb.entity.TableInfo;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.DatabaseService;
import com.fdev.database.util.EntityUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DatabaseServiceImpl implements DatabaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private DatabaseDao databaseDao;

    @Resource
    private AppService appService;

    @Resource
    private TableInfoDao tableInfoDao;

    @Autowired
    private RestTransport restTransport;

    @Override
    public Database add(Database database) throws Exception {
        return databaseDao.add(database);
    }

    @Override
    public List<Database> queryName(Database database) throws Exception {
        return databaseDao.queryName(database);
    }

    @Override
    public List<Map> query(Database database) throws Exception {
        return databaseDao.query(database);
    }

    @Override
    public void delete(Database database) throws Exception {
        databaseDao.delete(database);
    }

    @Override
    public Database update(Database database) throws Exception{
        return databaseDao.update(database);
    }

    @Override
    public void Confirm(Database database) {
        databaseDao.Confirm(database);
    }

    @Override
    public List<Database> queryAll(Database database) throws Exception {
        return databaseDao.queryAll(database);
    }

    @Override
    public Map queryDetail(Database database) throws Exception {
        Map result = new HashMap();
        List<Map> query = databaseDao.query(database);
        TableInfo tableInfo = new TableInfo();
        tableInfo.setDatabase_type((String) query.get(0).get(Dict.DATABASE_TYPE));
        tableInfo.setDatabase_name((String) query.get(0).get(Dict.DATABASE_NAME));
        tableInfo.setTable_name((String) query.get(0).get(Dict.TABLE_NAME));
        List<TableInfo> tableInfoList = tableInfoDao.query(tableInfo);
        result.putAll(query.get(0));
        result.putAll(EntityUtils.entityToMap(tableInfoList.get(0)));
        return result;
    }

    @Override
    public List<Database> queryTbName(Database database) throws Exception {
        return databaseDao.queryTbName(database);
    }

    @Override
    public Map<String, Object> queryInfo(String appid, String database_type, String database_name, String table_name, String status, Map spdb_manager, int page, int per_page) throws Exception{
        return databaseDao.queryInfo(appid, database_type, database_name, table_name, status, spdb_manager, page, per_page);
    }

    @Override
    public void exportExcel(List<Map> databases, HttpServletResponse resp) throws Exception {
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "应用英文名称");
            setCellValue(workbook, 0, 0, 1, "应用中文名称");
            setCellValue(workbook, 0, 0, 2, "数据库类型");
            setCellValue(workbook, 0, 0, 3, "库名");
            setCellValue(workbook, 0, 0, 4, "表名");
            int i = 1;
            for (Map database : databases) {
                App app = (App)database.get(Dict.APPINFO);
                setCellValue(workbook, 0, i, 0, app.getAppName_en());
                setCellValue(workbook, 0, i, 1, app.getAppName_cn());
                setCellValue(workbook, 0, i, 2, database.get(Dict.DATABASE_TYPE).toString());
                setCellValue(workbook, 0, i, 3, database.get(Dict.DATABASE_NAME).toString());
                setCellValue(workbook, 0, i, 4, database.get(Dict.TABLE_NAME).toString());
              i++;
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FdevException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "finishedOrderReport.xlsx");
        workbook.write(resp.getOutputStream());
    }

    /**
     * excel填值
     *
     * @param workbook   excel对象
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

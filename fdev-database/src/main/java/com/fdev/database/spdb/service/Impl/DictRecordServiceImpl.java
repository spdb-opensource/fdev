package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.DataDictDao;
import com.fdev.database.spdb.dao.DictRecordDao;
import com.fdev.database.spdb.dao.SysInfoDao;
import com.fdev.database.spdb.entity.DataDict;
import com.fdev.database.spdb.entity.DictRecord;
import com.fdev.database.spdb.entity.SysInfo;
import com.fdev.database.spdb.service.DictRecordService;
import com.fdev.database.spdb.service.UserService;
import com.fdev.database.util.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.transport.RestTransport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class DictRecordServiceImpl implements DictRecordService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;

    @Resource
    private DictRecordDao dictRecordDao;

    @Resource
    private DataDictDao dataDictDao;

    @Resource
    private SysInfoDao sysInfoDao;

    @Resource
    private UserService userService;

    @Override
    public Map queryDictRecord(String sys_id, String database_type, String database_name, String field_en_name, int per_page, int page) throws Exception {
        return dictRecordDao.queryDictRecord(sys_id, database_type, database_name, field_en_name, per_page, page);
    }

    @Override
    public void add(DictRecord dictRecord) {
        dictRecordDao.add(dictRecord);
    }

    @Override
    public List<DictRecord> query(DictRecord dictRecord) throws Exception {
        return dictRecordDao.query(dictRecord);
    }

    @Override
    public void update(DictRecord dictRecord) {
        dictRecordDao.update(dictRecord);
    }

    @Override
    public void impDictRecords(MultipartFile[] files) {
        for (MultipartFile file : files) {
            InputStream inputStream = null;
            try {
                inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                sheet.getRowSumsBelow();
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (!CommonUtils.isNullOrEmpty(row)) {
                        if(isRowNull(row)) {
                            break;
                        }

                        Cell database_type = row.getCell(0);
                        Cell database_name = row.getCell(1);
                        Cell field_en_name = row.getCell(2);
                        Cell field_ch_name = row.getCell(3);
                        Cell field_type = row.getCell(4);
                        Cell field_length = row.getCell(5);
                        Cell is_list_field = row.getCell(6);
                        Cell list_field_desc = row.getCell(7);
                        Cell remark = row.getCell(8);
                        Cell userNameEn = row.getCell(9);
                        //字典表
                        DataDict dataDict = new DataDict();
                        dataDict.setDatabase_type(readCell(database_type));
                        dataDict.setDatabase_name(readCell(database_name));
                        dataDict.setField_en_name(readCell(field_en_name));
                        List<DataDict> dictlist = dataDictDao.queryByKey(dataDict);
                        //字典为空新增字典
                        DataDict  dataDict1 = new DataDict();
                       if(CommonUtils.isNullOrEmpty(dictlist)){
                           dataDict.setField_ch_name(readCell(field_ch_name));
                           dataDict.setField_type(readCell(field_type));
                           dataDict.setField_length(readCell(field_length));
                           dataDict.setStatus("1");
                           dataDict1 = dataDictDao.add(dataDict);
                       } else {
                           dataDict1 = dictlist.get(0);
                       }
                        //获取模版中维护人信息
                        List<Map> userlist = new ArrayList<>();
                        if(!CommonUtils.isNullOrEmpty(readCell(userNameEn))){
                            userlist = userService.queryUserInfo(readCell(userNameEn));
                        }
                       //系统表
                        SysInfo sysInfo = new SysInfo();
                        sysInfo.setDatabase_name(readCell(database_name));
                        List<SysInfo> syslist = sysInfoDao.query(sysInfo);
                        if(CommonUtils.isNullOrEmpty(syslist)){
                            sysInfo.setSystem_name_cn("系统待定");
                            sysInfo =sysInfoDao.add(sysInfo);

                            //记录表
                            String crrentTime = CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss");

                            if(!CommonUtils.isNullOrEmpty(readCell(userNameEn))){
                                if(!CommonUtils.isNullOrEmpty(userlist)){
                                    DictRecord dictRecord = new DictRecord("", sysInfo.getSys_id(), dataDict1.getDict_id()
                                            , readCell(is_list_field), readCell(list_field_desc), readCell(remark), crrentTime, readCell(userNameEn), userlist.get(0).get(Dict.USER_NAME_CN).toString(),"", "", "" ,"1");
                                    dictRecordDao.add(dictRecord);
                                }
                            } else {
                                User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                                        .getSession().getAttribute(Dict._USER);
                                DictRecord dictRecord = new DictRecord("", sysInfo.getSys_id(), dataDict1.getDict_id()
                                        , readCell(is_list_field), readCell(list_field_desc), readCell(remark), crrentTime, user.getUser_name_en(), user.getUser_name_cn(),"", "", "" ,"1");
                                dictRecordDao.add(dictRecord);
                            }
                        } else {
                             for(SysInfo sysInfo1 : syslist){
                                 //校验导入数据是否存在
                                 DictRecord dictRecord = new DictRecord();
                                 dictRecord.setSys_id(sysInfo1.getSys_id());
                                 dictRecord.setDict_id(dataDict1.getDict_id());
                                 List<DictRecord>  recordList = dictRecordDao.query(dictRecord);
                                 if(CommonUtils.isNullOrEmpty(recordList)){
                                     String crrentTime = CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss");
                                     User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                                             .getSession().getAttribute(Dict._USER);
                                     logger.info("字段"+ dataDict1.getField_en_name() +"被导入新增");
                                     if(!CommonUtils.isNullOrEmpty(readCell(userNameEn))){
                                         if(!CommonUtils.isNullOrEmpty(userlist)){
                                             DictRecord dictRecord1 = new DictRecord("", sysInfo1.getSys_id(), dataDict1.getDict_id()
                                                     , readCell(is_list_field), readCell(list_field_desc), readCell(remark), crrentTime, readCell(userNameEn), userlist.get(0).get(Dict.USER_NAME_CN).toString(),"", "", "" ,"1");
                                             dictRecordDao.add(dictRecord1);
                                         }
                                     } else {
                                         DictRecord dictRecord1 = new DictRecord("", sysInfo1.getSys_id(), dataDict1.getDict_id()
                                                 , readCell(is_list_field), readCell(list_field_desc), readCell(remark), crrentTime, user.getUser_name_en(), user.getUser_name_cn(),"", "", "" ,"1");
                                         dictRecordDao.add(dictRecord1);
                                     }
                                 }
                             }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("异常"+e);
            }

        }
    }

    public boolean isRowNull(Row row) {
        for(int i=row.getFirstCellNum();i<row.getLastCellNum();i++) {
            Cell cell = row.getCell(i);
            if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                return false;
            }
        }
        return true;
    }

    public String readCell(Cell cell) {
        String value = "";
        if(CommonUtils.isNullOrEmpty(cell)) {
            return value;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                value = cell.getStringCellValue();
                break;
            default :
                value = "";
                break;
        }
        return value;

    }


}

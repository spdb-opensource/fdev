package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.dao.*;
import com.fdev.database.spdb.entity.*;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.FileService;
import com.fdev.database.spdb.service.ScannParseService;
import com.fdev.database.spdb.service.SendEmailService;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.DatabaseUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;


@Service
@RefreshScope
public class FileServiceImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    private static ResourceBundle bundle;

    @Resource
    private DatabaseDao databaseDao;

    @Resource
    private TableInfoDao tableInfoDao;

    @Resource
    private DataDictDao dataDictDao;

    @Resource
    private DictRecordDao dictRecordDao;

    @Resource
    private DictUseRecordDao dictUseRecordDao;

    @Resource
    private SysInfoDao sysInfoDao;

    @Resource
    private SendEmailService sendEmailService;

    @Resource
    private ScannParseService scannParseService;

    @Resource
    private AppService appService;

    @Value("${fdatabase.scann.schema.filepath}")
    private String schemaFilePath;

    @Value("${fdatabase.upload.hyperlink}")
    private String hyperlink;

    @Override
    public JsonResult fileUpload(MultipartFile[] files, String database_type, User user) {
        // 判断 文件夹是否存在，
        String fileTemp = this.schemaFilePath + database_type;
        File file1 = new File(fileTemp);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        try {
            saveFile(fileTemp, files, user);
            logger.info("文件保存成功！");
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.FILE_ERROR, new String[]{"文件保存失败"});
        }

        Map<String, Object> map = new HashMap<>();
        try {
            map.put(Dict.SPDB_MANAGERS, user);
            map.put(Dict.HYPERLINK, hyperlink);
            String fileName = "";
            for (MultipartFile multipartFile : files) {
                fileName += multipartFile.getOriginalFilename() + "、";
            }
            // 给应用负责人发送通知提醒审核
            sendEmailService.sendUserNotify(new HashMap(map));
            // 给应用负责人发送邮件
            if (!CommonUtils.isNullOrEmpty(fileName)) {
                map.put(Dict.FILENAME, fileName.substring(0, fileName.lastIndexOf("、")));
                sendEmailService.sendEmail(new HashMap(map));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("通知发送失败！");
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 保存文件流
     *
     * @param dir
     * @param files
     * @return
     * @throws Exception
     */
    private String saveFile(String dir, MultipartFile[] files, User user) {
        // 保存文件
        StringBuilder builder = new StringBuilder();
        for (MultipartFile multipartFile : files) {
            if (!"".equals(multipartFile.getOriginalFilename())) {
                builder.append(multipartFile.getOriginalFilename());
                File file = new File(dir + "/" + multipartFile.getOriginalFilename());
                try {
                    boolean existflag = false;
                    String[] str = dir.split("/");
                    String dataType = str[str.length - 1].trim();
                    String dataName = "";
                    if(file.getName().contains(".schema")){
                        dataName = file.getName().replace(".schema", "").trim();
                    } else if(file.getName().contains(".sql")){
                        dataName = file.getName().replace(".sql", "").trim();
                    }
                    //文件替换
                    if (file.exists()) {
                        existflag = true;
                        Database database = new Database();
                        database.setDatabase_type(dataType);
                        database.setDatabase_name(dataName);
                        database.setAutoflag("1");
                        databaseDao.deleteBydb(database);

                        TableInfo tableInfo = new TableInfo();
                        tableInfo.setDatabase_type(dataType);
                        tableInfo.setDatabase_name(dataName);
                        tableInfoDao.delete(tableInfo);
                    }
                    //转存文件
                    multipartFile.transferTo(file.getAbsoluteFile());
                    // 1.读取新增文件，解析数据存库
                    logger.info("文件保存完成，开始扫描" + dataName + ".schema文件！");
                    Map tableNames = DatabaseUtil.parseSchema(dir + "/" + file.getName(), dataType);
                    //获取当前库名的系统信息
                    SysInfo sysInfo = new SysInfo();
                    sysInfo.setDatabase_name(dataName);
                    List<SysInfo> sysList = sysInfoDao.query(sysInfo);
                    //录入库中
                    if (tableNames.size() > 0) {
                        List<Map> tableInfos = (List<Map>) tableNames.get("tableInfo");
                        List<Map> primaryKeys = (List<Map>) tableNames.get("primaryKeys");
                        List<Map> uniqueIndexs = (List<Map>) tableNames.get("uniqueIndex");
                        List<Map> indexs = (List<Map>) tableNames.get("index");
                        for (Map tableMap : tableInfos) {
                            Database database = new Database();
                            database.setDatabase_type(dataType);
                            database.setDatabase_name(dataName);
                            database.setTable_name(tableMap.get(Dict.TABLENAME).toString());
                            database.setAutoflag("0");
                            database.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
                            databaseDao.add(database);

                            TableInfo tableInfo = new TableInfo();
                            tableInfo.setDatabase_type(dataType);
                            tableInfo.setDatabase_name(dataName);
                            tableInfo.setTable_name(tableMap.get(Dict.TABLENAME).toString());
                            tableInfo.setColumns((List<Map>) tableMap.get(Dict.COLUMNS));
                            List<String> primaryKey = (List<String>) tableMap.get("primaryKey");
                            if(!CommonUtils.isNullOrEmpty(primaryKeys)){
                                for(Map primaryKey1 : primaryKeys){
                                    if(primaryKey1.get(Dict.TABLENAME).toString().equals(tableMap.get(Dict.TABLENAME).toString())){
                                        primaryKey.add(primaryKey1.get("key").toString());
                                    }
                                }
                            }
                            tableInfo.setPrimaryKey(primaryKey);
                            List<Map> uniqueIndexList = new ArrayList<>();
                            if(!CommonUtils.isNullOrEmpty(uniqueIndexs)){
                                for (Map uniqueIndex : uniqueIndexs) {
                                    if (uniqueIndex.get(Dict.TABLENAME).toString().equals(tableMap.get(Dict.TABLENAME).toString())) {
                                        uniqueIndexList.add(uniqueIndex);
                                    }
                                }
                            }
                            tableInfo.setUniqueIndex(uniqueIndexList);
                            List<Map> indexList = new ArrayList<>();
                            if(!CommonUtils.isNullOrEmpty(indexs)){
                                for (Map index : indexs) {
                                    if (index.get(Dict.TABLENAME).toString().equals(tableMap.get(Dict.TABLENAME).toString())) {
                                        indexList.add(index);
                                    }
                                }
                            }
                            tableInfo.setIndex(indexList);
                            tableInfo.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
                            tableInfoDao.add(tableInfo);
                            //当首次上传schema文件时，录入信息到数据字典表 、数据字典记录表、数据字典使用记录表
                            if (!existflag) {
                                List<Map> Fields = (List<Map>) tableMap.get(Dict.COLUMNS);
                                for(Map Field : Fields){
                                    //校验数据字典是否存在
                                    DataDict dataDict = new DataDict();
                                    dataDict.setDatabase_type(dataType);
                                    dataDict.setDatabase_name(dataName);
                                    dataDict.setField_en_name((String) Field.get("column"));
                                    List<DataDict> dictList = dataDictDao.query(dataDict);
                                    if(CommonUtils.isNullOrEmpty(dictList)){
                                        dataDict.setField_ch_name("");
                                        String[] strs = ((String) Field.get("columnType")).split("\\(");
                                        dataDict.setField_type(strs[0]);
                                        if(strs.length > 1)
                                            dataDict.setField_length(strs[1].substring(0, strs[1].indexOf(")")));
                                        dataDict.setStatus("1");   //在使用
                                        dataDict = dataDictDao.add(dataDict);
                                    }else {
                                        dataDict =  dictList.get(0);
                                    }
                                    //系统表
                                    if(!CommonUtils.isNullOrEmpty(sysList)) {
                                        for (SysInfo sysInfo1 : sysList) {
                                            //校验导入数据是否存在
                                            DictRecord dictRecord = new DictRecord();
                                            dictRecord.setSys_id(sysInfo1.getSys_id());
                                            dictRecord.setDict_id(dataDict.getDict_id());
                                            List<DictRecord>  recordList = dictRecordDao.query(dictRecord);
                                            //记录表
                                            if(CommonUtils.isNullOrEmpty(recordList)) {
                                                createDictRecord(sysInfo1, dataDict, user, tableMap);
                                            }
                                        }
                                    }else {
                                       //库名找不到相关系统
                                        SysInfo sysInfo1 = new SysInfo("", "系统待定", dataName);
                                        SysInfo sysInfo2 = sysInfoDao.add(sysInfo1);
                                        sysList.add(sysInfo2);
                                        //记录表
                                        createDictRecord(sysInfo2, dataDict, user, tableMap);
                                    }
                                }
                            }
                        }
//                        System.out.println(file.getName() + " created.");
                        logger.info("文件保存后，开始扫描应用！");
                        //根据库类型和库名进行xml扫描匹配
                        scannParseService.ScannAndGetxml(dataType, dataName);
                    }
                } catch (Exception e) {
                    logger.error("schema文件扫描异常！", e);
                    throw new FdevException(ErrorConstants.FILE_ERROR, new String[]{"文件保存失败"});
                }
            }
        }
        return builder.toString();
    }

    private void createDictRecord(SysInfo sysInfo1, DataDict dataDict, User user, Map tableMap){
        //插入数据字典记录表
        DictRecord dictRecord = new DictRecord("", sysInfo1.getSys_id(), dataDict.getDict_id()
        , "N", "", "", CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"), user.getUser_name_en(),
                user.getUser_name_cn(), "", "","",
                "1");
        dictRecordDao.add(dictRecord);

        //插入数据字典使用记录表
//        DictUseRecord dictUseRecord = new DictUseRecord("", sysInfo1.getSys_id(), dataDict.getDict_id(),
//                tableMap.get(Dict.TABLENAME).toString(), "N", "", CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"),
//                user.getUser_name_en(), user.getUser_name_cn(), "", "", "","1");
//        dictUseRecordDao.add(dictUseRecord);
    }

}

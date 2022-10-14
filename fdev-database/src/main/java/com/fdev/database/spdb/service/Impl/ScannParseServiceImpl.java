package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.dao.DatabaseDao;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.entity.Database;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.GitlabService;
import com.fdev.database.spdb.service.ScannParseService;
import com.fdev.database.util.Common;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.FileUtils;
import com.fdev.database.util.DatabaseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;


@Service
@RefreshScope
public class ScannParseServiceImpl implements ScannParseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private DatabaseDao databaseDao;

    @Resource
    private AppDao appDao;

    @Resource
    private AppService appService;

    @Resource
    private GitlabService gitlabService;

    @Resource
    private Common common;

    @Value("${fdatabase.scann.schema.filepath}")
    private String schema_filepath;

    @Value("${nas.apps.path}")
    private String nas_apps_path;
    
    @Value("${xmlfile.maxsize}")
    private String maxSize;    //XML扫描最大文件限制

    /**
     * 扫描xml获取应用和库表的关联信息
     *
     * @param databaseType 库类型
     * @param databaseName 库名称
     */
    @Override
    public void ScannAndGetxml(String databaseType, String databaseName) {
        //扫描应用下的xml文件
        List<Map<String, Object>> appList = null;
        String name_en = "";
        try {
            appList = appService.getAppList("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appList != null && appList.size() > 0) {
            //记录超出限定大小的文件系信息
            StringBuffer contentBuffer = new StringBuffer();
            contentBuffer.append("超过大小限制文件").append("\r\n");
            int length = appList.size();
            for (int i = 0; i < length; i++) {
                try {
                    Map map = appList.get(i);
                    String id = (String) map.get(Dict.ID);
                    name_en = (String) map.get(Dict.NAME_EN);
                    String name_cn = (String) map.get(Dict.NAME_ZH);
                    String git = (String) map.get(Dict.GIT);
                    String type_name = (String) map.get(Dict.TYPE_NAME);
                    List<Map> spdbmanagers = (List<Map>) map.get(Dict.SPDB_MANAGERS);
                    String status = (String) map.get(Dict.STATUS);
                    logger.info("开始扫描第" + (i + 1) + "个应用:" + name_en + "，共" + length + "个应用");
                    //判断是否跳过扫描的应用类型，如Vue，IOS
                    if (DatabaseUtil.isSkip(type_name)) {
                        continue;
                    }
                    //判断是否废弃的应用
                    if (StringUtils.isNotBlank(status) && "0".equals(status) && StringUtils.isNotBlank(name_en)) {
                        continue;
                    }
                    gitlabService.gitOperation(name_en, git, nas_apps_path, Dict.MASTER.toUpperCase());//进行git clone或git pull操作,本地存储项目文件
                    //解析本地存储应用的xml文件,并匹配Schema文件获取的表名进行存库
                    List<String> tableNames = ScannAndParseXml(nas_apps_path + Dict.MASTER.toUpperCase() + "/" + name_en, databaseType, databaseName, contentBuffer);
                    if (tableNames.size() > 0) {
                        App app = new App();
                        app.setId(id);
                        List<App> apps = appDao.queryAppName(app);
                        if (CommonUtils.isNullOrEmpty(apps)) {
                            app.setAppName_en(name_en);
                            app.setAppName_cn(name_cn);
                            app.setSpdb_managers(spdbmanagers);
                            appDao.save(app);
                        }
                        Map<String, Integer> map1 = new HashMap();
                        for (String temp : tableNames) {
                            Integer count = map1.get(temp);
                            map1.put(temp, (count == null) ? 1 : count + 1);
                        }
                        Set<String> set = new HashSet(tableNames);
                        for (String tableName : set) {
                            Database database = new Database();
                            database.setTable_name(tableName);
                            database.setAppid(id);
                            database.setTbNum(map1.get(tableName));
                            database.setAutoflag("0");
                            database.setStatus("0");
                            database.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
                            database.setDatabase_type(databaseType);
                            database.setDatabase_name(databaseName);
                            databaseDao.collectByTableName(database);
                        }
                    }
                } catch (Exception e) {
                    logger.error(name_en + "应用xml文件扫描异常");
                }
            }
            //将超过限定大小的文件的信息存入指定路径下
            FileUtils.writerFile(contentBuffer.toString(), nas_apps_path+ Dict.MASTER.toUpperCase() + "/");
        }

    }

    private List<String> ScannAndParseXml(String nas_apps_path, String databaseType, String databaseName, StringBuffer contentBuffer) throws Exception {
        File file = new File(nas_apps_path);
        List<String> XmlTableName = new ArrayList<>();
        //遍历扫描文件下的所有xml
        XmlTableName = ScannXml(XmlTableName, file, databaseType, databaseName, contentBuffer);
        return XmlTableName;
    }

    private List<String> ScannXml(List<String> xmlTableName, File file, String databaseType, String databaseName, StringBuffer contentBuffer) throws Exception {
        File[] tempLists = file.listFiles();
        for (File file1 : tempLists) {
            String[] txt = file1.getName().split("\\.");
            if (file1.isDirectory()) {
                ScannXml(xmlTableName, file1, databaseType, databaseName, contentBuffer);
            } else if (file1.isFile() && txt[txt.length - 1].trim().equals("xml")) {
//                logger.info("开始扫描xml文件:" + file1.getPath());
                //从schema统计库中的表名去匹配xml文件
                xmlTableName = parseXml(xmlTableName, file1, databaseType, databaseName, contentBuffer);
            }
        }
        return xmlTableName;
    }

    //schema文件上传扫描解析项目中xml文件
    private List<String> parseXml(List<String> xmlTableName, File file, String databaseType, String databaseName, StringBuffer contentBuffer) {
        try {
            if (file.length() < Integer.parseInt(maxSize)) {
                Database xmldatabase = new Database();
                xmldatabase.setDatabase_type(databaseType);
                xmldatabase.setDatabase_name(databaseName);
                List<Database> databases = databaseDao.queryAll(xmldatabase);
                // 根据库类型、库名、表名进行除重（查询所有库表，不重复）
                databases = DatabaseUtil.setDatabase(databases);
                List<String> TableNameList = DatabaseUtil.getTableInXml(file, databases);
                xmlTableName.addAll(TableNameList);
            } else {
                contentBuffer.append(file.getPath()+",文件大小:"+file.length()).append("\r\n");
            }
        } catch (Exception e) {
            logger.error("上传扫描xml文件异常", e);
        }
        return xmlTableName;
    }

}

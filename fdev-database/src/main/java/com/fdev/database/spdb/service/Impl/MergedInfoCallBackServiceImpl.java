package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.dao.DatabaseDao;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.entity.Database;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.GitlabService;
import com.fdev.database.spdb.service.MergedInfoCallBackService;
import com.fdev.database.util.Common;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.DatabaseUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
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
public class MergedInfoCallBackServiceImpl implements MergedInfoCallBackService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AppService appService;

    @Resource
    private GitlabService gitlabService;

    @Resource
    private DatabaseDao databaseDao;

    @Resource
    private Common common;

    @Resource
    private AppDao appDao;

    @Value("${apps.new.path}")
    private String apps_newpath;

    @Value("${nas.apps.path}")
    private String apps_oldpath;

    @Value("${xmlfile.maxsize}")
    private String maxSize;        //XML扫描最大文件限制

    @Override
    public JsonResult mergedCallBack(Integer projectId, String sourceBranch, String mergeBranch) throws Exception {
        List<Map<String, Object>> appList = appService.getAppList(String.valueOf(projectId));
        String id = (String) appList.get(0).get(Dict.ID);
        String AppName = (String) appList.get(0).get(Dict.NAME_EN);
        String name_cn = (String) appList.get(0).get(Dict.NAME_ZH);
        List<Map> spdbmanagers = (ArrayList<Map>) appList.get(0).get(Dict.SPDB_MANAGERS);
        String git = (String) appList.get(0).get(Dict.GIT);
        String name_en = (String) appList.get(0).get(Dict.NAME_EN);
        String type_name = (String) appList.get(0).get(Dict.TYPE_NAME);
        String status = (String) appList.get(0).get(Dict.STATUS);
        //判断是否跳过扫描的应用类型，如Vue，IOS
        if (DatabaseUtil.isSkip(type_name)) {
            return null;
        }
        //判断是否废弃的应用
        if (StringUtils.isNotBlank(status) && "0".equals(status) && StringUtils.isNotBlank(name_en)) {
            return null;
        }
        try {
            //比较分支提交记录
            Map<String, Object> commitCompareList = gitlabService.compareBranch(projectId, sourceBranch, mergeBranch);
            Map commitMap = (Map) commitCompareList.get(Dict.COMMIT);
            String commitId = (String) commitMap.get(Dict.ID);
            //获取最新的commit记录及文件差异信息
            List<Map<String, Object>> commitDiffList = gitlabService.findCommitDiff(projectId, commitId);
            for (Map map : commitDiffList) {
                String newfilePath = map.get(Dict.NEW_PATH).toString();
                String oldfilePath = map.get(Dict.OLD_PATH).toString();
                boolean newfileFlag = (boolean) map.get(Dict.NEW_FILE);
                boolean delfileFlag = (boolean) map.get(Dict.DELETED_FILE);
//                boolean renamefileFlag = (boolean) map.get("renamed_file");
                //文件变动 （new_path和old_path在文件新增或删除时都存在）
                if (newfilePath.substring(newfilePath.lastIndexOf(".") + 1).equals("xml")) {
                    //clone或pull项目文件到备份路径
                    gitlabService.gitOperation(AppName, git, apps_newpath, mergeBranch.toUpperCase());
                    //记录表名重复出现的次数（表名相同，库类型或库名不同）
                    List<Database> databases = databaseDao.queryAll(new Database());
                    databases = DatabaseUtil.setDatabase(databases);
                    Map<String, Integer> tableCount = new HashMap();
                    for (Database database : databases) {
                        Integer count = tableCount.get(database.getTable_name());
                        tableCount.put(database.getTable_name(), (count == null) ? 1 : count + 1);
                    }
                    if (newfileFlag) {      //文件为新增
                        File newfile = new File(apps_newpath + mergeBranch.toUpperCase() + "/" + AppName + "/" + newfilePath);
                        List<String> tableNames = parseXml(newfile);
                        if (tableNames.size() > 0) {
                            Map<String, Integer> map2 = new HashMap();
                            for (String tableName : tableNames) {
                                Integer count = map2.get(tableName);
                                map2.put(tableName, (count == null) ? 1 : count + 1);
                            }
                            createTab(id, AppName, name_cn, spdbmanagers, tableNames, map2, tableCount);
                        }
                    } else if (delfileFlag) {      //文件为删除
                        File newfile = new File(apps_oldpath + mergeBranch.toUpperCase() + "/" + AppName + "/" + newfilePath);
                        List<String> tableNames = parseXml(newfile);
                        if (tableNames.size() > 0) {
                            Map<String, Integer> map2 = new HashMap();
                            for (String temp : tableNames) {
                                Integer count = map2.get(temp);
                                map2.put(temp, (count == null) ? 1 : count + 1);
                            }
                            deleteTab(id, tableNames, map2, tableCount);
                        }
                    } else {  //文件为修改
                        File newfile = new File(apps_newpath + mergeBranch.toUpperCase() + "/" + AppName + "/" + newfilePath);
                        File oldfile = new File(apps_oldpath + mergeBranch.toUpperCase() + "/" + AppName + "/" + oldfilePath);
                        List<String> tableNames = parseXml(newfile);
                        List<String> oldtableNames = parseXml(oldfile);
                        if (tableNames.size() > 0 || oldtableNames.size() > 0) {
                            Map<String, Integer> newcount = new HashMap();
                            for (String tableName : tableNames) {
                                Integer count = newcount.get(tableName);
                                newcount.put(tableName, (count == null) ? 1 : count + 1);
                            }
                            createTab(id, AppName, name_cn, spdbmanagers, tableNames, newcount, tableCount);

                            Map<String, Integer> oldcount = new HashMap();
                            for (String oldtableName : oldtableNames) {
                                Integer count = oldcount.get(oldtableName);
                                oldcount.put(oldtableName, (count == null) ? 1 : count + 1);
                            }
                            deleteTab(id, oldtableNames, oldcount, tableCount);
                        }
                    }
                }
            }
            //更新老版本对比的备份文件
            gitlabService.gitOperation(AppName, git, apps_oldpath, mergeBranch.toUpperCase());
        } catch (Exception e) {
            logger.error("合并扫描异常" + e);
        }
        return JsonResultUtil.buildSuccess();
    }

    @Override
    public JsonResult mergedCallBackSit(Integer projectId, String sourceBranch, String mergeBranch) throws Exception {
        mergedCallBack(projectId, sourceBranch, mergeBranch);
        return JsonResultUtil.buildSuccess();
    }

    // 表名新增
    public void createTab(String id, String AppName, String name_cn, List<Map> spdbmanagers, List<String> tableNames, Map<String, Integer> map2, Map<String, Integer> tableCount) throws Exception {
        App app = new App();
        app.setId(id);
        List<App> apps = appDao.queryAppName(app);
        if (apps.size() > 0) {
            Set<String> set = new HashSet(tableNames);
            for (String tableName : set) {
                //获取库中表名的出现次数
                Database database = new Database();
                database.setAppid(id);
                database.setTable_name(tableName);
//                database.setAutoflag("0");
                List<Database> databaseMap = databaseDao.queryAll(database);
                if (databaseMap.size() > 0) {
                    for (Database database1 : databaseMap) {
                        database1.setAutoflag("2");
                        database1.setTbNum(map2.get(tableName) / tableCount.get(tableName) + database1.getTbNum());
                        databaseDao.updateByTbNameAndAppid(database1);
                    }
                } else {
                    database.setAutoflag("2");
                    database.setStatus("0");
                    database.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
                    database.setTbNum(map2.get(tableName) / tableCount.get(tableName));
                    databaseDao.collectByTableName(database);
                }
            }
        } else {
            app.setAppName_en(AppName);
            app.setAppName_cn(name_cn);
            app.setSpdb_managers(spdbmanagers);
            appDao.save(app);
            Set<String> set = new HashSet(tableNames);
            for (String tableName : set) {
                Database database = new Database();
                database.setAppid(id);
                database.setTable_name(tableName);
                database.setTbNum(map2.get(tableName) / tableCount.get(tableName));
                database.setAutoflag("2");
                database.setStatus("0");
                database.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
                databaseDao.collectByTableName(database);
            }
        }
    }

    // 表名删除
    public void deleteTab(String id, List<String> tableNames, Map<String, Integer> map2, Map<String, Integer> tableCount) throws Exception {
        App app = new App();
        app.setId(id);
        List<App> apps = appDao.queryAppName(app);
        Set<String> set = new HashSet(tableNames);
        if (apps.size() > 0) {
            for (String tableName : set) {
                Database database = new Database();
                database.setAppid(id);
                database.setTable_name(tableName);
                database.setAutoflag("1");
                List<Database> databaseMap = databaseDao.queryByAutoflag(database);
                if (databaseMap.size() > 0) {
                    for (Database database1 : databaseMap) {
                        int tbNum = database1.getTbNum() - map2.get(tableName) / tableCount.get(tableName);
                        if (tbNum > 0) {
                            database1.setTbNum(tbNum);
                            databaseDao.updateById(database1);
                        } else {
                            databaseDao.delete(database1);
                        }
                    }
                }
            }
        }
    }


    // kafaka监听merge提交时项目中匹配xml文件中的表名
    public List<String> parseXml(File file) {
        List<String> xmlTableName = new ArrayList<>();
        try {
            if (file.length() < Integer.parseInt(maxSize)) {
                List<Database> databases = databaseDao.queryAll(new Database());
                databases = DatabaseUtil.setDatabase(databases);
                xmlTableName = DatabaseUtil.getTableInXml(file, databases);
            }
        } catch (Exception e) {
            logger.error("kafaka监听merge的项目中xml文件扫描异常", e);
        }
        return xmlTableName;
    }

}

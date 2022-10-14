package com.fdev.database.spdb.web;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.entity.Database;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.DatabaseService;
import com.fdev.database.spdb.service.TableInfoService;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.DatabaseUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Api(tags = "数据库管理接口")
@RequestMapping("/api/database")
@RestController
@RefreshScope
public class DatabaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
    @Resource
    private DatabaseService databaseService;

    @Resource
    private AppService appService;
    
    @Resource
    private TableInfoService tableInfoService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Value("${fdatabase.types}")
    private String databaseTypes;


    /* 库名信息查询 */
    @ApiOperation(value = "库名信息查询")
    @RequestMapping(value = "/queryDbName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryDbName(
            @RequestBody @ApiParam(name = "database", value = "例如:{\"database_name\":\"fdev-database_sit\"}") Database database) throws Exception {
        List<Database> databases = databaseService.queryName(database);
        Set set = new HashSet();
        for(Database database1 : databases){
            set.add(database1.getDatabase_name());
        }
        return JsonResultUtil.buildSuccess(set);
    }

    /* 表名信息查询 */
    @ApiOperation(value = "表名信息查询")
    @RequestMapping(value = "/queryName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryName(
            @RequestBody @ApiParam(name = "database", value = "例如:{\"database_name\":\"fdev-database_sit\"}") Database database) throws Exception {
        List<Database> databases = databaseService.queryTbName(database);
        databases = DatabaseUtil.setDatabase(databases);

        //获取已存在库中的表名,并根据表名过滤（存在为匹配应用id和多个应用id都使用同一表名的情况）
        if(!CommonUtils.isNullOrEmpty(database.getAppid())){
            List<Database> existTbNames = databaseService.queryAll(database);
             if(!CommonUtils.isNullOrEmpty(existTbNames)){
                 for(Database tableName : existTbNames){
                       Iterator<Database> iterator =  databases.iterator();
                       while (iterator.hasNext()) {
                           Database database2 = iterator.next();
                           if(database2.getTable_name().equals(tableName.getTable_name())){
                               iterator.remove();
                           }
                       }
                 }
             }
        }
        return JsonResultUtil.buildSuccess(databases);
    }

    /* 数据库信息查询 */
    @ApiOperation(value = "数据库信息查询")
    @RequestMapping(value = "/queryInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryInfo(@RequestBody Map<String, Object> requestParam) throws Exception {
        int page = (int) requestParam.get(Dict.PAGE);
        int per_page = (int) requestParam.get(Dict.PER_PAGE);
        String appid = (String) requestParam.get(Dict.APPID);
        String database_type = (String) requestParam.get(Dict.DATABASE_TYPE);
        String database_name = (String) requestParam.get(Dict.DATABASE_NAME);
        String table_name = (String) requestParam.get(Dict.TABLE_NAME);
        if(!CommonUtils.isNullOrEmpty(table_name))
            table_name=table_name.toUpperCase();
        String status = (String) requestParam.get(Dict.STATUS);
        Map spdb_manager = null;
        if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.SPDB_MANAGER))){
            spdb_manager = (Map) requestParam.get(Dict.SPDB_MANAGER);
        }
        Map<String, Object> databases = databaseService.queryInfo(appid, database_type, database_name, table_name, status, spdb_manager, page, per_page);
        return JsonResultUtil.buildSuccess(databases);
    }

    /* 新增数据库信息 */
    @OperateRecord(operateDiscribe="数据库管理-数据库信息录入")
    @ApiOperation(value = "新增数据库信息")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult add(@RequestBody @ApiParam(name = "database", value = "例如:{\"database_name\":\"fdev-database_sit\"}") Database database) throws Exception {
         App app = new App();
         app.setId(database.getAppid());
         List<App> applist = appService.query(app);
         if(CommonUtils.isNullOrEmpty(applist)) {
             throw new FdevException(ErrorConstants.APP_NOTEXISTED);
         }
        List<Map> dblist = databaseService.query(database);
        if(!CommonUtils.isNullOrEmpty(dblist)){
            throw new FdevException(ErrorConstants.DATABASE_EXISTED);
        }
        //角色权限判断（应用负责人）
          if(!checkRole(database.getAppid())){
              throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
          }
        database.setAutoflag("1");
        database.setStatus("0");
        database.setCreateTime(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
        Database addDb = databaseService.add(database);
        return JsonResultUtil.buildSuccess(addDb);

    }

    /* 修改数据库信息 */
    @OperateRecord(operateDiscribe="数据库管理-数据库信息修改")
    @ApiOperation(value = "修改数据库信息")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult update(
            @RequestBody @ApiParam(name = "database", value = "例如:{\"database_name\":\"fdev-database_sit\"}") Database database) throws Exception {
        //角色权限判断（应用负责人）
        if(!checkRole(database.getAppid())){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        Database database1 = new Database();
        database1.setAppid(database.getAppid());
        database1.setDatabase_type(database.getDatabase_type());
        database1.setDatabase_name(database.getDatabase_name());
        database1.setTable_name(database.getTable_name());
        List<Database> querylist = databaseService.queryName(database1);
        if(querylist.size()>0){
            throw new FdevException(ErrorConstants.DATABASE_EXISTED, new String[]{"修改信息已存在"});
        }
        Database database2 = new Database();
        database2.setId(database.getId());
        databaseService.delete(database2);
        database1.setAutoflag("1");
        database1.setStatus("0");
        Database addDb = databaseService.add(database1);
        return JsonResultUtil.buildSuccess(addDb);
    }

    @OperateRecord(operateDiscribe="数据库管理-数据库信息删除")
    @ApiOperation(value = "删除数据库信息")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult delete(
            @RequestBody @ApiParam(name = "database", value = "例如:{\"database_name\":\"fdev-database_sit\"}")  List<Database> databaseList) throws Exception {
        for (Database database : databaseList) {
            //角色权限判断（应用负责人）
            if(!checkRole(database.getAppid())){
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
            }
            if (CommonUtils.isNullOrEmpty(database.getId())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id为空"});
            }
            databaseService.delete(database);
        }
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "根据库类型获取信息")
    @RequestMapping(value = "/queryDbType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryDbType(@RequestBody Database database) throws Exception {
        List<String> Typelist = new ArrayList<>();
        String[] types = databaseTypes.split(",");
         for(String str : types){
             Typelist.add(str);
         }
        return  JsonResultUtil.buildSuccess(Typelist);
    }

    @ApiOperation(value = "应用信息查询")
    @RequestMapping(value = "/queryAppName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryAppName(@RequestBody  App app) throws Exception {
        List<App> apps = appService.query(app);
        return JsonResultUtil.buildSuccess(apps);
    }

    @ApiOperation(value = "根据用户英文名查询应用信息")
    @RequestMapping(value = "/queryAppByUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryAppByUser(@RequestBody  Map<String, String> Param) throws Exception {
        String user_name_en = Param.get(Dict.USER_NAME_EN);
        List<App> apps = appService.queryAppByUser(user_name_en);
        return JsonResultUtil.buildSuccess(apps);
    }


    @ApiOperation(value = "导出查询的全部信息")
    @RequestMapping(value = "/loadTab", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult loadTab(@RequestBody Database database, HttpServletResponse resp) throws Exception {
        List<Map> databases = databaseService.query(database);
        databaseService.exportExcel(databases, resp);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @ 判断操作权限，用户角色为应用负责人（行内负责人）或卡点管理员可进行新增、修改、删除、上传、确认
     */
    private boolean checkRole(String Appid) throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        //卡点管理员
        if( userVerifyUtil.userRoleIsSPM((List<String>) user.getRole_id())) {
            return true;
        }
        if(checkmManager(Appid)){
            return true;
        }
        return false;
    }

    @ApiOperation(value = "应用信息确认")
    @RequestMapping(value = "/Confirm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult Confirm(@RequestBody List<Database> databaseList) throws Exception {
        if(CommonUtils.isNullOrEmpty(databaseList)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"确认信息不能为空"});
        }
        for(Database database : databaseList){
            if(!checkRole(database.getAppid())){
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
            }
            if (CommonUtils.isNullOrEmpty(database.getId())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id为空"});
            }
            databaseService.Confirm(database);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @ 判断操作权限，用户为行内负责人（应用负责人）可以操作按钮
     */
    private boolean checkmManager(String Appid) throws Exception {
        App app = new App();
        app.setId(Appid);
        List<App> appQuery = appService.query(app);
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        //行内负责人
        List<Map> spdb_managers = appQuery.get(0).getSpdb_managers();
        for(Map manager : spdb_managers){
            if(user.getUser_name_en().equals(manager.get(Dict.USER_NAME_EN))){
                return true;
            }
        }
        return false;
    }

    @ApiOperation(value = "应用负责人查询")
    @RequestMapping(value = "/queryManager", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryManager(@RequestBody App app) throws Exception {
        List<App> appQuery = appService.query(app);
        Set<Map> reslut = new HashSet<>();
        for(App app1 : appQuery){
           List<Map> spdb_managers = app1.getSpdb_managers();
           for(Map manager : spdb_managers){
               reslut.add(manager);
           }
        }
        return  JsonResultUtil.buildSuccess(reslut);
    }

    @ApiOperation(value = "应用与数据库关系详情查询")
    @RequestMapping(value = "/queryDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryDetail(@RequestBody Database database) throws Exception {
        if(CommonUtils.isNullOrEmpty(database.getId())){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id为空"});
        }
        Map detailInfo = databaseService.queryDetail(database);
        return  JsonResultUtil.buildSuccess(detailInfo);
    }

    @ApiOperation(value = "同步修改应用负责人")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.SPDB_MANAGERS})
    @RequestMapping(value = "/updateManager", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateManager(@RequestBody Map<String, Object> requestParam) throws Exception {
        String appId = (String) requestParam.get(Dict.ID);
        List<Map> spdb_managers = (List<Map>) requestParam.get(Dict.SPDB_MANAGERS);
        appService.updateManager(appId, spdb_managers);
        return  JsonResultUtil.buildSuccess();
    }

}

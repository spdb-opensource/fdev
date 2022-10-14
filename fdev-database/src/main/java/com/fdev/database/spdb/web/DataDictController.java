package com.fdev.database.spdb.web;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.entity.DataDict;
import com.fdev.database.spdb.entity.DictRecord;
import com.fdev.database.spdb.entity.DictUseRecord;
import com.fdev.database.spdb.entity.SysInfo;
import com.fdev.database.spdb.service.*;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.DictUtils;
import com.fdev.database.util.ValidateUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Api(tags = "库表信息接口")
@RequestMapping("/api/dataDict")
@RestController
public class DataDictController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Resource
    private SysInfoService sysInfoService;

    @Resource
    private DictRecordService dictRecordService;

    @Resource
    private DictUseRecordService dictUseRecordService;

    @Resource
    private DataDictService dataDictService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "数据字典信息查询")
    @RequestMapping(value = "/queryDictRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryDictRecord(@RequestBody Map<String, Object> requestParam) throws Exception {
       String sys_id = (String) requestParam.get(Dict.SYS_ID);
       String database_type = (String) requestParam.get(Dict.DATABASE_TYPE);
       String database_name = (String) requestParam.get(Dict.DATABASE_NAME);
       String field_en_name = (String) requestParam.get(Dict.FIELD_EN_NAME);
       int page = (int) requestParam.get(Dict.PAGE);
       int per_page = (int) requestParam.get(Dict.PER_PAGE);
       Map result = dictRecordService.queryDictRecord(sys_id, database_type, database_name, field_en_name, per_page, page);
       return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "数据字典信息新增、克隆")
    @RequestValidate(NotEmptyFields = {Dict.DATABASE_TYPE, Dict.DATABASE_NAME, Dict.FIELD_EN_NAME})
    @RequestMapping(value = "/addDictRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addDictRecord(@RequestBody Map<String, Object> requestParam) throws Exception {
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
//        String sys_id = (String) requestParam.get(Dict.SYS_ID);
        String database_type = (String) requestParam.get(Dict.DATABASE_TYPE);
        String database_name = (String) requestParam.get(Dict.DATABASE_NAME);
        String field_en_name = (String) requestParam.get(Dict.FIELD_EN_NAME);
        String field_ch_name = (String) requestParam.get(Dict.FIELD_CH_NAME);
        String field_type = (String) requestParam.get(Dict.FIELD_TYPE);
        String field_length = (String) requestParam.get(Dict.FIELD_LENGTH);
        String is_list_field = (String) requestParam.get(Dict.IS_LIST_FIELD);
        String list_field_desc = (String) requestParam.get(Dict.LIST_FIELD_DESC);
        String remark = (String) requestParam.get(Dict.REMARK);
        //判断字典是否已存在
        DataDict dataDict = new DataDict();
        dataDict.setDatabase_type(database_type);
        dataDict.setDatabase_name(database_name);
        dataDict.setField_en_name(field_en_name);
        List<DataDict> dictlist = dataDictService.queryByKey(dataDict);
        if(!CommonUtils.isNullOrEmpty(dictlist)){
            throw new FdevException(ErrorConstants.DICT_EXIST, new String[]{"该数据字典已存在"});
        }
        dataDict.setField_ch_name(field_ch_name);
        dataDict.setField_type(field_type);
        dataDict.setField_length(field_length);
        dataDict.setStatus("1");
        DataDict dataDict1 = dataDictService.add(dataDict);
        String dict_id = dataDict1.getDict_id();
        //获取系统id
        SysInfo sysInfo = new SysInfo();
        sysInfo.setDatabase_name(database_name);
        List<SysInfo> syslist = sysInfoService.query(sysInfo);
        if(CommonUtils.isNullOrEmpty(syslist)){
            throw new FdevException(ErrorConstants.DICT_EXIST, new String[]{"该库名无对应的系统"});
        }
        String sys_id = syslist.get(0).getSys_id();
        String crrentTime = CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss");
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        DictRecord dictRecord = new DictRecord("", sys_id, dict_id, is_list_field, list_field_desc,
                remark, crrentTime, user.getUser_name_en(), user.getUser_name_cn(),"", "", "" ,"1");
        logger.info("字段"+ field_en_name +"新增");
        dictRecordService.add(dictRecord);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "数据字典信息修改")
    @RequestValidate(NotEmptyFields = {Dict.RECORD_ID, Dict.SYS_ID, Dict.DICT_ID})
    @RequestMapping(value = "/updateDictRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateDictRecord(@RequestBody Map<String, Object> requestParam) throws Exception {
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        String record_id = (String) requestParam.get(Dict.RECORD_ID);
        String sys_id = (String) requestParam.get(Dict.SYS_ID);
//        String system_name_cn = (String) requestParam.get("system_name_cn");
        String dict_id = (String) requestParam.get(Dict.DICT_ID);
//        String database_type = (String) requestParam.get(Dict.DATABASE_TYPE);
//        String database_name = (String) requestParam.get(Dict.DATABASE_NAME);
//        String field_en_name = (String) requestParam.get(Dict.FIELD_EN_NAME);
        String field_ch_name = (String) requestParam.get(Dict.FIELD_CH_NAME);
//        String field_type = (String) requestParam.get(Dict.FIELD_TYPE);
        String field_length = (String) requestParam.get(Dict.FIELD_LENGTH);
        String is_list_field = (String) requestParam.get(Dict.IS_LIST_FIELD);
        String list_field_desc = (String) requestParam.get(Dict.LIST_FIELD_DESC);
        String remark = (String) requestParam.get(Dict.REMARK);
        
        SysInfo sysInfo = new SysInfo();
        sysInfo.setSys_id(sys_id);
        List<SysInfo> sysList = sysInfoService.query(sysInfo);
        if(CommonUtils.isNullOrEmpty(sysList)){
            throw new FdevException(ErrorConstants.DICT_EXIST, new String[]{"该系统数据不存在"});
        }

        DataDict dataDict = new DataDict();
        dataDict.setDict_id(dict_id);
        List<DataDict> dictlist = dataDictService.queryByKey(dataDict);
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if(!CommonUtils.isNullOrEmpty(dictlist)){
            //修改字典中字段中文名
            dataDict.setField_ch_name(field_ch_name);
            dataDict.setField_length(field_length);
            dataDictService.update(dataDict);
        }
        //修改旧记录
        DictRecord dictRecord = new DictRecord();
        dictRecord.setRecord_id(record_id);
        dictRecord.setIs_list_field(is_list_field);
        dictRecord.setList_field_desc(list_field_desc);
        dictRecord.setRemark(remark);
        dictRecord.setLast_modi_userName(user.getUser_name_cn());
        dictRecord.setLast_modi_userNameEn(user.getUser_name_en());
        dictRecord.setLast_modi_time(CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss"));
        dictRecordService.update(dictRecord);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "数据字典信息批量导入")
    @RequestMapping(value = "/impDictRecords", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult impDictRecords(@RequestParam(Dict.FILE) MultipartFile[] files) throws Exception {
        if(CommonUtils.isNullOrEmpty(files)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        dictRecordService.impDictRecords(files);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "系统和数据库名信息查询")
    @RequestMapping(value = "/querySystem", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult querySystem(@RequestBody SysInfo sysInfo) throws Exception {
        return JsonResultUtil.buildSuccess(sysInfoService.query(sysInfo));
    }

    @ApiOperation(value = "系统信息新增")
    @RequestMapping(value = "/addSys", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addSys(@RequestBody SysInfo sysInfo) throws Exception {
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        sysInfoService.add(sysInfo);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "字段类型查询")
    @RequestMapping(value = "/queryFieldType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryFieldType(@RequestBody Map<String, String> Param){
       String fieldType = Param.get("fieldType");
        List<String> FieldTypes = dataDictService.queryFieldType(fieldType);
        return JsonResultUtil.buildSuccess(FieldTypes);
    }

    @ApiOperation(value = "批量导入模版下载")
    @PostMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse resp){
        dataDictService.downloadTemplate(resp);
    }

    @ApiOperation(value = "数据字典登记表信息生成")
    @PostMapping("/addUseRecord")
    public JsonResult addUseRecord(@RequestBody DictUseRecord dictUseRecord) throws Exception {
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        DictUseRecord dictUseRecord1 = dictUseRecordService.queryByKey(dictUseRecord);
        if(!CommonUtils.isNullOrEmpty(dictUseRecord1)){
            throw new FdevException(ErrorConstants.DICT_EXIST);
        }
        String crrentTime = CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss");
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        dictUseRecord.setFirst_modi_time(crrentTime);
        dictUseRecord.setFirst_modi_userName(user.getUser_name_cn());
        dictUseRecord.setFirst_modi_userNameEn(user.getUser_name_en());
        dictUseRecord.setIs_new_table("Y");
        dictUseRecord.setStatus("1");
        dictUseRecordService.add(dictUseRecord);

        //记录变动日志
        List<Map> mapList = dictUseRecord.getAll_field();
        StringBuffer str = new StringBuffer();
        if(!CommonUtils.isNullOrEmpty(mapList)){
            int i = 0;
            for(Map map : mapList){
                if(i == 0){
                    str.append(map.get(Dict.FIELD));
                    i++;
                } else {
                    str.append("、"+map.get(Dict.FIELD));
                }
            }
        }
        logger.info("生成数据字典登记表，表名为"+dictUseRecord.getTable_name() +",添加字段为"+ str);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "数据字典登记表信息查询（分页）")
    @PostMapping("/queryUseRecord")
    public JsonResult queryUseRecord(@RequestBody Map<String, Object> requestParam) {
        int page = (int) requestParam.get(Dict.PAGE);
        int per_page = (int) requestParam.get(Dict.PER_PAGE);
        String sys_id = (String) requestParam.get(Dict.SYS_ID);
        String database_type = (String) requestParam.get(Dict.DATABASE_TYPE);
        String database_name = (String) requestParam.get(Dict.DATABASE_NAME);
        String table_name = (String) requestParam.get(Dict.TABLE_NAME);
        Map<String, Object> UseRecordInfo = dictUseRecordService.queryUseRecordByPage(sys_id, database_type,
                database_name, table_name, page, per_page);
        return JsonResultUtil.buildSuccess(UseRecordInfo);
    }

    @ApiOperation(value = "数据字典登记表信息修改")
    @PostMapping("/updateUseRecord")
    public JsonResult updateUseRecord(@RequestBody DictUseRecord dictUseRecord) throws Exception {
        //角色权限判断
        if(!checkPermission()){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        DictUseRecord dictUseRecord1 = dictUseRecordService.queryById(dictUseRecord.getUseRecord_id());
        if(CommonUtils.isNullOrEmpty(dictUseRecord1)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
         //校验存在相同字段
        List<Map> all_field = dictUseRecord.getAll_field();
        if(ValidateUtils.checkRepeatKey(all_field, Dict.FIELD)){
            throw new FdevException(ErrorConstants.DICT_EXIST, new String[]{"存在重复字段"});
        }
        String crrentTime = CommonUtils.formatDate("yyyy/MM/dd HH:mm:ss");
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        dictUseRecord.setLast_modi_time(crrentTime);
        dictUseRecord.setLast_modi_userName(user.getUser_name_cn());
        dictUseRecord.setLast_modi_userNameEn(user.getUser_name_en());
        dictUseRecordService.updateUseRecord(dictUseRecord);
       //记录变动日志
        List<Map> oldFieldMap = dictUseRecord1.getAll_field();
        List<Map> newFieldMap = dictUseRecord.getAll_field();
        //修改前所有字段和修改后所有字段
        List<String> oldFields = DictUtils.collectByKey(oldFieldMap, Dict.FIELD);
        List<String> newFields = DictUtils.collectByKey(newFieldMap, Dict.FIELD);
        Map fieldMap = DictUtils.compareFields(oldFields, newFields);
        List<String> addFields = (List<String>) fieldMap.get("addField");
        List<String> delFields = (List<String>) fieldMap.get("delField");
        logger.info("修改，数据字典登记表中的表"+dictUseRecord.getTable_name()
                +"被添加字段"+ DictUtils.StrBuf(addFields)+",删除字段" +DictUtils.StrBuf(delFields));
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "数据字典登记表的表名查询")
    @PostMapping("/queryUseRecordTable")
    public JsonResult queryUseRecordTable(@RequestBody DictUseRecord dictUseRecord) throws Exception {
        Set<String> tableNames = new HashSet<>();
        List<DictUseRecord> tables = dictUseRecordService.queryUseRecord(dictUseRecord);
        for(DictUseRecord dictUseRecord1 : tables){
            tableNames.add(dictUseRecord1.getTable_name());
        }
        return JsonResultUtil.buildSuccess(tableNames);
    }

    /**
     * 权限判断，校验用户为是否为DBA审核人或卡点管理员
     * @return
     */
    public boolean checkPermission() throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        //卡点管理员
        if( userVerifyUtil.userRoleIsSPM((List<String>) user.getRole_id())) {
            return true;
        }
        //DBA审核人
        List<Map> rolelist = userService.queryRoleInfo("DBA审核人");
        if(!CommonUtils.isNullOrEmpty(rolelist) && user.getRole_id().contains(rolelist.get(0).get(Dict.ID))){
            return true;
        }
        return false;
    }

}

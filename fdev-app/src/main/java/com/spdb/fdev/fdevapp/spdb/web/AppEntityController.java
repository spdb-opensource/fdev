package com.spdb.fdev.fdevapp.spdb.web;

import com.csii.pe.redis.util.Util;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.service.IAppEntityService;
import com.spdb.fdev.fdevapp.spdb.service.IUserService;
import com.spdb.fdev.fdevapp.spdb.service.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;

@Api(tags = "应用实体接口")
@RequestMapping("/api/app")
@RestController
@RefreshScope
public class AppEntityController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private IAppEntityService appEntityService;
    private static final String EN_REGEX = "^[A-Za-z-]+$";
    private static final String CN_REGEX = "^[\u4e00-\u9fa5]+$";

    @Autowired
    private TestService testService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;
    
    @Autowired
    private IUserService userService;
    
    @Value("${fdev.user.groups}")
	private String groups;
    
    @Value("${fdev.app.names}")
	private String appNames;
    

    @PostMapping("/findbyid")
    @NoNull(require = {Dict.ID}, parameter = AppEntity.class)
    public JsonResult findById(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.findById(appEntity.getId()));
    }

    @OperateRecord(operateDiscribe = "应用模块-应用更新")
    @PostMapping("/update")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult update(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.update(requestParam));
    }

    @PostMapping("/updateForEnv")
    @NoNull(require = {Dict.ID}, parameter = AppEntity.class)
    public JsonResult updateForEnv(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.updateForEnv(appEntity));
    }

    @PostMapping("/query")
    public JsonResult query(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.query(appEntity));
    }

    @PostMapping("/queryForSelect")
    public JsonResult queryForSelect() throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryForSelect());
    }

    @PostMapping("/queryApps")
    public JsonResult queryApps(@RequestBody AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryApps(appEntity));
    }

    /**
     * 根据英文名称模糊查询应用信息
     *
     * @param appEntity
     * @return
     * @throws Exception
     */
    @PostMapping("/search_en")
    @NoNull(require = {Dict.NAME_EN}, parameter = AppEntity.class)
    public JsonResult queryByAppNameEn(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryByAppNameEn(appEntity.getName_en()));
    }

    /**
     * 根据英文信息精准查询应用信息
     *
     * @param appEntity
     * @return
     */
    @PostMapping("/search")
    @NoNull(require = {Dict.NAME_EN}, parameter = AppEntity.class)
    public JsonResult queryByAppName(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryByAppName(appEntity.getName_en()));
    }

    /**
     * 根据中文名称模糊查询应用信息
     *
     * @param appEntity
     * @return
     * @throws Exception
     */
    @PostMapping("/search_zh")
    @NoNull(require = {Dict.NAME_ZH}, parameter = AppEntity.class)
    public JsonResult queryByAppNameZh(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryByAppNameZh(appEntity.getName_zh()));
    }

    /**
     * @param jsonMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryAppInfo")
    @RequestValidate(NotEmptyFields = {Dict.NAME})
    public JsonResult queryAppInfo(@RequestBody Map jsonMap) throws Exception {
        List<AppEntity> result = null;
        String name = (String) jsonMap.get(Dict.NAME);

        boolean en_matches = Pattern.matches(EN_REGEX, name);
        boolean cn_matches = Pattern.matches(CN_REGEX, name);
        if (en_matches) {
            result = this.appEntityService.queryByAppNameEn(name);
        }
        if (cn_matches) {
            result = this.appEntityService.queryByAppNameZh(name);
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 通过应用 gitlab_id 分支名 自动false或者定时true状态 获取 env_name
     *
     * @param jsonParam
     * @return
     * @throws Exception
     */
    @PostMapping("/get_sit_slug")
    @RequestValidate(NotEmptyFields = {Dict.CI_PROJECT_ID, Dict.CI_COMMIT_REF_NAME, Dict.CI_SCHEDULE})
    public JsonResult getSitSlug(@RequestBody Map jsonParam) throws Exception {
        Integer ci_project_id = (Integer) jsonParam.get(Dict.CI_PROJECT_ID);
        String ci_commit_ref_name = (String) jsonParam.get(Dict.CI_COMMIT_REF_NAME);
        Boolean ci_schedule = (Boolean) jsonParam.get(Dict.CI_SCHEDULE);
        return JsonResultUtil.buildSuccess(this.appEntityService.getSitSlug(ci_project_id, ci_commit_ref_name, ci_schedule));
    }

    /**
     * 根据gitlab id 获取 应用信息
     *
     * @param jsonParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping("/getAppByGitId")
    public JsonResult getAppByGitlabId(@RequestBody Map<String, Integer> jsonParam) throws Exception {
        Integer id = jsonParam.get(Dict.ID);
        return JsonResultUtil.buildSuccess(this.appEntityService.getAppByGitlabId(id));
    }

    /**
     * 根据应用id 去获取分支名
     *
     * @param jsonParam
     * @return
     * @throws Exception
     */
    @PostMapping("/getBranchNameByAppId")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult getBranchNameByAppId(@RequestBody Map<String, String> jsonParam) throws Exception {
        String id = jsonParam.get(Dict.ID);
        return JsonResultUtil.buildSuccess(this.appEntityService.getBranchNameByAppId(id));
    }

    /**
     * 录入已有应用
     *
     * @param
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "应用模块-录入已有应用")
    @PostMapping("/add")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.NAME_ZH, Dict.NAME_EN, Dict.GROUP, Dict.SPDB_MANAGERS, Dict.DEV_MANAGERS, Dict.GITLABCI_ID})
    public JsonResult add(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.add(requestParam));
    }


    /**
     * 查询各组近6周的应用数量
     *
     * @param requestParam 二级组对应的id集合
     * @return 小组维度的list，map中是日期为key，数量为value
     * @throws Exception
     */
    @ApiOperation(value = "查询各组近6周的任务数量")
    @RequestMapping(value = "/queryAppNum", method = RequestMethod.POST)
    public JsonResult queryAppNum(@RequestBody @ApiParam Map requestParam) throws Exception {
        List params = (List) requestParam.get(Dict.IDS);
        boolean flag = requestParam.get(Dict.ISINCLUDECHILDREN) == null ? false : (Boolean) requestParam.get(Dict.ISINCLUDECHILDREN);
        if (CommonUtils.isNullOrEmpty(requestParam)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "二级组id不能为空！"});
        }
        Map<String, Map> result = appEntityService.queryAppNum(params, flag);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 异步创建 应用
     *
     * @param
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "应用模块-异步创建应用")
    @PostMapping("/saveByAsync")
    @RequestValidate(NotEmptyFields = {Dict.NAME_ZH, Dict.NAME_EN, Dict.GROUP, Dict.GIT,
            Dict.SPDB_MANAGERS, Dict.DEV_MANAGERS, Dict.ARCHETYPE_ID, "system"})
    public JsonResult saveByAsync(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.saveByAsync(requestParam));
    }


    /**
     * 自动部署开关，true为打开，默认不打开
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "应用模块-应用详情\"自动部署开关\"")
    @RequestMapping(value = "/createPipelineSchedule", method = RequestMethod.POST)
    public JsonResult createPipelineSchedule(@RequestBody @ApiParam Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.createPipelineSchedule(requestParam));
    }

    /**
     * 查询新老持续集成 0 代表旧的  1 代表新
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping("/customDeplayment")
    public JsonResult customDeplayment(@RequestBody Map requestParam) throws Exception {
        String id = (String) requestParam.get(Dict.ID);
        return JsonResultUtil.buildSuccess(this.appEntityService.customDeplayment(id));
    }


    /**
     * 根据应用id 删除应用 只修改状态 "0"表示已废弃
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "应用模块-根据应用id 删除应用 只修改状态 \"0\"表示已废弃")
    @PostMapping("/deleteAppById")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult deleteAppById(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.deleteAppById(requestParam));
    }

    /**
     * 查询废弃应用
     *
     * @param appEntity
     * @return
     * @throws Exception
     */
    @PostMapping("/queryAbandonApp")
    public JsonResult queryAbandonApp(@RequestBody @ApiParam AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryAbandonApp(appEntity));
    }

    /**
     * 查询应用缺陷
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/queryAppMantis")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryAbandonApp(@RequestBody @ApiParam Map request) throws Exception {
        Map app = appEntityService.findById((String) request.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(app)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用不存在"});
        }
        String isTest = (String) app.get("isTest");
        //判断当不涉及内测的时候，直接返回让前端全部自己展示0
        if (!CommonUtils.isNullOrEmpty(isTest) && isTest.equals("0")) {
            return JsonResultUtil.buildSuccess();
        }
        List tasks = appEntityService.queryAppTasks((String) request.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(tasks)) {
            return JsonResultUtil.buildSuccess();
        }
        return JsonResultUtil.buildSuccess(this.testService.queryMantisByTaskList(tasks));
    }

    @ApiOperation(value = "批量任务", notes = "每天定时轮询fdev上管理的微服务应用,对前一天sit分支有代码变动应用，执行定时持续集成")
    @PostMapping("/scheduleJob")
    public JsonResult scheduleJob() throws Exception {
        this.appEntityService.scheduleJob();
        return JsonResultUtil.buildSuccess();
    }
    
    @ApiOperation(value = "批量任务", notes = "每天定时部署测试环境")
    @PostMapping("/autoDeploy")
    public JsonResult autoDeploy() throws Exception {
        this.appEntityService.autoDeploy();
        return JsonResultUtil.buildSuccess();
    }


    @ApiOperation(value = "根据标签id查询应用")
    @PostMapping("/queryAppByLabelId")
    @NoNull(require = {Dict.LABEL}, parameter = AppEntity.class)
    public JsonResult queryAppByLabel(@RequestBody AppEntity appEntity) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryAppByLabel(appEntity));
    }

    @ApiOperation(value = "根据应用id或gitlabId的list集合查询应用", notes = "type =id 根据id查询；type=gitlab_project_id 根据gitlabId查询")
    @PostMapping("/getAppByIdsOrGitlabIds")
    public JsonResult getAppByIdsOrGitlabIds(@RequestBody @ApiParam Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(appEntityService.getAppByIdsOrGitlabIds(requestParam));
    }

    @ApiOperation(value = "根据应用英文名的list集合查询应用")
    @PostMapping("/getAppByNameEns")
    public JsonResult getAppByNameEns(@RequestBody @ApiParam Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(appEntityService.getAppByNameEns(requestParam));
    }


    @PostMapping("/queryMyApps")
    @RequestValidate(NotEmptyFields = {Dict.USER_ID})
    public JsonResult queryMyApps(@RequestBody Map<String, String> map) throws Exception {
        String user_id = map.get(Dict.USER_ID);
        List<Map> list = appEntityService.queryAppsByUserId(user_id);
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping("/queryPagination")
    public JsonResult queryPagination(@RequestBody Map<String, Object> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryPagination(map));
    }


    //返回标签不包括“不涉及环境部署” 的应用。只返回应用id，英文名，中文名
    @PostMapping("/queryWithEnv")
    public JsonResult getAppInfoWithEnv() {
        return JsonResultUtil.buildSuccess(this.appEntityService.getNoInvolveEnvApp());
    }

    //应用绑定所属系统
    @PostMapping("/bindSystem")
    @RequestValidate(NotEmptyFields = {"appId", "systemId"})
    public JsonResult bindSystem(@RequestBody Map<String, Object> requestMap) throws Exception {
        String appId = (String) requestMap.get("appId");
        String systemId = (String) requestMap.get("systemId");
        this.appEntityService.bindAppSystem(systemId, appId);
        return JsonResultUtil.buildSuccess("绑定成功");
    }
    //获取应用是否涉及内测字段
    @PostMapping("/getTestFlag")
    @RequestValidate(NotEmptyFields = {"id"})
    public JsonResult getTestFlag(@RequestBody Map<String, Object> requestMap) throws Exception {
        String appId = (String) requestMap.get("id");
        return JsonResultUtil.buildSuccess(this.appEntityService.getTestFlag(appId));
    }


    //获取appCiType的字段给runner，返回不用JsonResultUtil，返回 code
    @PostMapping("/getAppCiTypeCode")
    public void getAppCiType(@RequestBody Map<String, Object> requestMap, HttpServletResponse response) {
        logger.info("in getAppCiTypeCode..");
        Integer projectId = (Integer) requestMap.get("project_id");
        logger.info("projectId:" + projectId);
        Integer resultCode = 201;
        if (CommonUtils.isNullOrEmpty(projectId)) {
            response.setStatus(resultCode);
            return;
        }
        String code = this.appEntityService.getResultCodeForCiType(projectId);
        resultCode = Integer.valueOf(code);
        response.setStatus(resultCode);
    }

    @PostMapping("/queryDutyAppBaseInfo")
    public JsonResult queryDutyAppBaseInfo() throws Exception {
        return JsonResultUtil.buildSuccess(this.appEntityService.queryDutyAppBaseInfo());
    }
    
    /**
     * @param requestMap
     * @return 
     * @throws Exception
     */
    @PostMapping("/queryAppAscriptionGroup")
    @RequestValidate(NotEmptyFields = {Dict.NAME})
    public JsonResult queryAppAscriptionGroup(@RequestBody Map<String, Object> requestMap) throws Exception {
        String name = (String) requestMap.get(Dict.NAME);
        Boolean isA5Flag = false;
        Boolean isWhiteFlag = false;
        Boolean flag = false;
        List<AppEntity> result = this.appEntityService.queryByAppName(name);        
        if(!Util.isNullOrEmpty(result)) {
        	String groupId = result.get(0).getGroup();
        	Map<String,Object> userGroup = userService.getThreeLevelGroup(groupId);
        	if(!Util.isNullOrEmpty(userGroup)) {
        		String ascriptionGroupId = (String) userGroup.get(Dict.PARENT_ID);
        		if(groups.contains(ascriptionGroupId) || groups.contains(groupId)) {
        			isA5Flag = true;
        		}
        	}
        }
        //校验A5指定应用
        if(appNames.contains(name)) {
        	isWhiteFlag = true;
        }
        
        if(isA5Flag && isWhiteFlag ) {
        	flag = true;
        }
        
        return JsonResultUtil.buildSuccess(flag);
    }
}

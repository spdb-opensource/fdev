package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.*;

@RequestMapping("/api/v2/appProInfo")
@RestController
public class DeployInformationController {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private QueryDeployInfoService QueryDeployInfoService;


    @Autowired
    private IAppDeployMappingService iAppDeployMappingService;

    @Autowired
    private IModelService iModelService;

    @Autowired
    private IRequestService iRequestService;

    @Autowired
    private BindService bindService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @PostMapping("/query")
    @RequestValidate(NotEmptyFields = {Dict.PAGE, Dict.PER_PAGE})
    public JsonResult query(@RequestBody Map<String, Object> requestParam) {
        return JsonResultUtil.buildSuccess(this.QueryDeployInfoService.query(requestParam));
    }

    @PostMapping("/queryDeploy")
    @RequestValidate(NotEmptyFields = {Dict.APPID})
    public JsonResult queryDeploy(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.QueryDeployInfoService.queryDeployByAppId(requestParam));
    }

    @PostMapping("/bind")
    @RequestValidate(NotEmptyFields = {Dict.APPID, Dict.MODELS_INFO})
    public JsonResult bind(@RequestBody Map requestParam) throws Exception {

        User user = userVerifyUtil.getRedisUser();
        if (null == user) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户身份验证失败"});
        }
        String id = user.getId();
        String appId = (String) requestParam.get(Dict.APPID);
        if (CommonUtils.isNullOrEmpty(appId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.APPID, "应用id不能为空"});
        }
        JSONObject app = iRequestService.findByAppId(appId);
        List<Map> spdbManagers = (List<Map>) app.get(Dict.SPDB_MANAGERS);
        List<Map> devManagers = (List<Map>) app.get(Dict.DEV_MANAGERS);
        HashSet<String> set = new HashSet<>();
        for (Map spdbManager : spdbManagers) {
            set.add((String) spdbManager.get(Dict.ID));
        }
        for (Map devManager : devManagers) {
            set.add((String) devManager.get(Dict.ID));
        }
        Map map = new HashMap();
        map.put(Dict.ID, id);
        ArrayList role_id = (ArrayList) iRequestService.queryUserById(map).get(Dict.ROLE_ID);
        if (!set.contains(id) && !role_id.contains(Dict.ENV_MANAGER_ROLE)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }
        requestParam.put(Dict.USER_ID, id);
        bindService.bind(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryBindMsgByApp")
    @RequestValidate(NotEmptyFields = {Dict.APPID})
    public JsonResult queryBindMsgByApp(@RequestBody Map requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam.get(Dict.APPID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用id不能为空"});
        }
        return JsonResultUtil.buildSuccess(this.QueryDeployInfoService.queryBindMsgByApp(requestParam));
    }

    @PostMapping("/queryRealTimeBindMsg")
    @RequestValidate(NotEmptyFields = {Dict.APPID})
    public JsonResult queryRealTimeBindMsg(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.QueryDeployInfoService.queryRealTimeBindMsg(requestParam));
    }

    @PostMapping("/definedDeploy")
    @RequestValidate(NotEmptyFields = {Dict.APP_ID, Dict.REF, Dict.VARIABLES, Dict.CONFIG_UPDATE_FLAG, Dict.RE_DEPLOY_FLAG})
    public JsonResult definedDeploy(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.QueryDeployInfoService.definedDeploy(requestParam));
    }


}

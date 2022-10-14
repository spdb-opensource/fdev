package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.AutomationParam;
import com.spdb.fdev.release.service.IAutomationParamService;
import com.spdb.fdev.release.service.IRoleService;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/automationparam")
@RestController
public class AutomationParamController {

    @Autowired
    private IAutomationParamService automationParamService;

    @Autowired
    private IRoleService roleService;

    @PostMapping(value = "/query")
    public JsonResult query()  {
        return JsonResultUtil.buildSuccess(automationParamService.query());
    }

    @OperateRecord(operateDiscribe="投产模块-参数新增")
    @RequestValidate(NotEmptyFields = {Dict.KEY, Dict.VALUE, Dict.DESCRIPTION})
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String key = requestParam.get(Dict.KEY);
        String value = requestParam.get(Dict.VALUE);
        String description = requestParam.get(Dict.DESCRIPTION);
        ObjectId id = new ObjectId();
        try {
            automationParamService.save(new AutomationParam(id, id.toString(), key, value, description));
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{key + "已存在！"});
        }
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe="投产模块-参数更新")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.KEY, Dict.VALUE, Dict.DESCRIPTION})
    @PostMapping(value = "/update")
    public JsonResult update(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = requestParam.get(Dict.ID);
        String key = requestParam.get(Dict.KEY);
        String value = requestParam.get(Dict.VALUE);
        String description = requestParam.get(Dict.DESCRIPTION);
        automationParamService.update(new AutomationParam(id, key, value, description));
        return JsonResultUtil.buildSuccess();
    }

    @OperateRecord(operateDiscribe="投产模块-参数删除")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = requestParam.get(Dict.ID);
        automationParamService.deleteById(id);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/queryToMap")
    public JsonResult queryToMap() {
        List<AutomationParam> list = automationParamService.query();
        Map<String, String> map = new HashMap<>();
        for(AutomationParam automationParam : list) {
            map.put(automationParam.getKey(), automationParam.getValue());
        }
        return JsonResultUtil.buildSuccess(map);
    }

    @PostMapping(value = "/queryByKey")
    public JsonResult queryByKey(@RequestBody @ApiParam Map<String, String> requestParam) {
        String key = requestParam.get(Dict.KEY);
        return JsonResultUtil.buildSuccess(automationParamService.queryByKey(key));
    }

}

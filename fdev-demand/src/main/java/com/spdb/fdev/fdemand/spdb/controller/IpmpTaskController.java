package com.spdb.fdev.fdemand.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;

import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.fdemand.spdb.service.IpmpTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/ipmptask")
public class IpmpTaskController {

    @Autowired
    private IpmpTaskService ipmpTaskService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private IRoleService roleService;


    /**
     * 添加任务集
     *
     * @param ipmpTask
     * @return
     * @throws Exception
     */
    @PostMapping("/addIpmpTask")
    public JsonResult addIpmpTask(@RequestBody IpmpTask ipmpTask) throws Exception {
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员"});
        }
        return JsonResultUtil.buildSuccess(ipmpTaskService.add(ipmpTask));
    }

    /**
     * 通过组id查询任务集
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryByGroupId")
    public JsonResult queryIpmpTaskByGroupId(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpTaskService.queryIpmpTaskByGroupId(params));
    }

    /**
     * 通过任务集id查询实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryUnitByIpmpTaskId")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryUnitByTakskId(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpTaskService.queryUnitByTakskId(params));
    }

    /**
     * 删除实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/deleteUnitById")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.IPMP_IMPLELMENT_UNIT_NO})
    public JsonResult deleteIpmpUnitById(@RequestBody Map params) throws Exception {
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员"});
        }
        return JsonResultUtil.buildSuccess(ipmpTaskService.deleteIpmpUnitById(params));
    }

    /**
     * 添加实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/addUnit")
    @RequestValidate(NotEmptyFields = {Dict.GROUP_ID, Dict.ID,Dict.IPMP_IMPLELMENT_NAME, Dict.IPMP_IMPLELMENT_UNIT_NO, Dict.IPMP_IMPLELMENT_UNIT_CONTENT})
    public JsonResult addIpmpUnitById(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpTaskService.addUnit(params));
    }

    /**
     * 根据任务集名称或者编号/实施单元编号或内容模糊l搜索
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/search")
    public JsonResult searchTaskUnit(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpTaskService.searchTaskUnit(params));
    }


}

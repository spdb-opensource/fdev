package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.service.DashboardDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/dashboard")
public class DashboradDemandController {

    @Autowired
    private DashboardDemandService dashboardDemandService;


    /**
     * 查询需求统计
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryDemandStatis", method = RequestMethod.POST)
    public JsonResult queryDemandStatis(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.getDemandStatis(map));
    }

    /**
     * 各组对应阶段实施需求数量
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryGroupDemand", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"groupIds", "isParent"})
    public JsonResult queryGroupRqrmnt(@RequestBody Map requestParam) throws Exception {
        List ids = (List) requestParam.get("groupIds");
        String priority = "";
        if (!CommonUtils.isNullOrEmpty(requestParam.get("priority")))
            priority = (String) requestParam.get("priority");
        boolean isParent = (boolean) requestParam.get("isParent");
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.queryGroupDemand(ids, priority, isParent));
    }


    /**
     * 查询实施单元统计
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryImplUnit", method = RequestMethod.POST)
    public JsonResult queryImplUnit(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.queryImplUnit(map));
    }


    /**
     * 需求超期通知邮件
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/sendDemandEmail", method = RequestMethod.POST)
    public JsonResult sendDemandEmail() throws Exception{
        dashboardDemandService.sendDemandEmail();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询已投产需求全景图
     *zhanghp4
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryEndDemandDashboard", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.START_DATE, Dict.END_DATE})
    public JsonResult queryEndDemandDashboard(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.queryEndDemandDashboard(map));
    }
    /**
     * 查询实施中需求全景图
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryImpingDemandDashboard", method = RequestMethod.POST)
    public JsonResult queryImpingDemandDashboard(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.queryImpingDemandDashboard());
    }
    /**
     * 查询需求全景图所需GroupId
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryIntGroupId", method = RequestMethod.POST)
    public JsonResult queryIntGroupId(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.dashboardDemandService.queryIntGroupId());
    }
}

package com.spdb.fdev.fdemand.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.service.IIpmpUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/ipmpUnit")
public class IpmpUnitController {

    @Autowired
    private IIpmpUnitService ipmpUnitService;

    /**
     * 查看实施单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryIpmpUnitByDemandId")
    public JsonResult queryIpmpUnitByDemandId(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryIpmpUnitByDemandId(params));
    }

    /**
     * 查看实施单元详情
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryIpmpUnitById")
    @RequestValidate(NotEmptyFields = {Dict.IMPLUNITNUM})
    public JsonResult queryIpmpUnitById(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryIpmpUnitById(params));
    }

    /**
     * 修改实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/updateIpmpUnit")
    @RequestValidate(NotEmptyFields = {Dict.IMPLUNITNUM,Dict.LEADERGROUP})
    public JsonResult updateIpmpUnit(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.updateIpmpUnit(params));
    }

    /**
     * 研发单元、实施单元、需求状态推动
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/updateStatus")
    @RequestValidate(NotEmptyFields = {Dict.STAGE,Dict.FDEV_IMPLEMENT_UNIT_NO})
    public JsonResult updateStatus(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.updateStatus(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 定时任务发送邮件提醒用户挂载研发单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/sendUserMountDevUnit")
    public JsonResult sendUserMountDevUnit(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.sendUserMountDevUnit(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据实施单元编号查询任务列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTaskByIpmpUnitNo")
    public JsonResult queryTaskByIpmpUnitNo(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryTaskByIpmpUnitNo(params));
    }

    /**
     * 从IPMP定时同步实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/syncAllIpmpInfo")
    public JsonResult syncAllIpmpInfo(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.syncAllIpmpInfo(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据实施单元编号\研发单元编号、需求id查实施单元\研发单元信息及需求信息
     * 输出实施单元\研发单元信息和所属需求的信息
     *
     * @param param
     * @return
     * @author t-guohh1
     */
    @PostMapping("/queryByUnitNoAndDemandId")
    public JsonResult queryByUnitNoAndDemandId(@RequestBody Map<String, String> param) throws Exception {
        String demandId = param.get(Dict.DEMAND_ID);
        String unitNo = param.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryByUnitNoAndDemandId(demandId, unitNo));
    }

    /**
     * 查询需求下当前登录人是否有牵头的实施单元未核算
     *
     * @param param
     * @return JsonResult
     * @author t-huyz
     */
    @PostMapping("/queryIpmpUnitIsCheck")
    @RequestValidate(NotEmptyFields = {Dict.INFORMATIONNUM})
    public JsonResult queryIpmpUnitIsCheck(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryIpmpUnitIsCheck(param));
    }

    /**
     * 向IPMP同步实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/syncIpmpUnit")
    @RequestValidate(NotEmptyFields = {Dict.IMPLUNITNUM})
    public JsonResult syncIpmpUnit(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.syncIpmpUnit(params));
    }

    /**
     * 查看实施单元列表2
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryIpmpUnitList")
    public JsonResult queryIpmpUnitList(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.queryIpmpUnitList(params));
    }

    /**
     * 导出实施单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/exportIpmpUnitList")
    public JsonResult exportIpmpUnitList(@RequestBody Map<String,Object> params, HttpServletResponse resp) throws Exception {
        ipmpUnitService.exportIpmpUnitList(params,resp);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 定时获取云测试平台的实施单元（09:00、11:00、13:00、15:00、17:00、19:00、23：30）
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getTestFinishDateByUpdateTime")
    public JsonResult getTestFinishDateByUpdateTime(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.getTestFinishDateByUpdateTime(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 失败的实施单元重新请求云测试平台（10:00、12:00、14:00、15:00、18:00、20:00）
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getTestFinishDate")
    public JsonResult getTestFinishDate(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.getTestFinishDate(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询技术方案编号
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getSchemeReview")
    public JsonResult getSchemeReview(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.getSchemeReview(params));
    }

    /**
     * 查询审核人
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getCloudCheckers")
    public JsonResult getCloudCheckers(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.getCloudCheckers(params));
    }

    /**
     * 调整排期
     * @return
     * @throws Exception
     */
    @PostMapping("/adjustDate")
    @RequestValidate(NotEmptyFields = {"implChangeType","implChangeReason","id"})
    public JsonResult adjustDate(@RequestParam String planDevelopDateAdjust, @RequestParam String planTestStartDateAdjust,
                                 @RequestParam String planTestFinishDateAdjust, @RequestParam String planProductDateAdjust,
                                 @RequestParam List<String> implChangeType, @RequestParam String implChangeReason,
                                 @RequestParam String id, @RequestParam MultipartFile[] businessEmail) throws Exception {

        IpmpUnit ipmpUnit = new IpmpUnit();
        if("null".equals(planDevelopDateAdjust)){
            planDevelopDateAdjust = "";
        }
        if("null".equals(planTestStartDateAdjust)){
            planTestStartDateAdjust = "";
        }
        if("null".equals(planTestFinishDateAdjust)){
            planTestFinishDateAdjust = "";
        }
        if("null".equals(planProductDateAdjust)){
            planProductDateAdjust = "";
        }
        ipmpUnit.setPlanDevelopDateAdjust(planDevelopDateAdjust);
        ipmpUnit.setPlanTestStartDateAdjust(planTestStartDateAdjust);
        ipmpUnit.setPlanTestFinishDateAdjust(planTestFinishDateAdjust);
        ipmpUnit.setPlanProductDateAdjust(planProductDateAdjust);
        ipmpUnit.setImplChangeType(implChangeType);
        ipmpUnit.setImplChangeReason(implChangeReason);
        ipmpUnit.setId(id);

        return JsonResultUtil.buildSuccess(ipmpUnitService.adjustDate(ipmpUnit, businessEmail));
    }

    /**
     * 确认延期
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/confirmDelay")
    @RequestValidate(NotEmptyFields = {"implDelayTypeName","implDelayReason"})
    public JsonResult confirmDelay(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.confirmDelay(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 删除确认延期邮件
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/deleteEmailFile")
    @RequestValidate(NotEmptyFields = {"id","businessEmailName","businessEmailPath"})
    public JsonResult deleteEmailFile(@RequestBody Map<String,Object> params) throws Exception {
        ipmpUnitService.deleteEmailFile(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据实施单元编号和任务集编号校验是否可以挂接
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getImplUnitRelatSpFlag")
    @RequestValidate(NotEmptyFields = {Dict.IMPLUNITNUM,Dict.PRJNUM})
    public JsonResult getImplUnitRelatSpFlag(@RequestBody Map<String,String> params) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpUnitService.getImplUnitRelatSpFlag( params.get(Dict.IMPLUNITNUM), params.get(Dict.PRJNUM)));
    }

}
